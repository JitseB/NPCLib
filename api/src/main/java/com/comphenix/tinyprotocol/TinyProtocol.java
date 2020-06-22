/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.comphenix.tinyprotocol;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import net.jitse.npclib.NPCLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimized version of TinyProtocol by Kristian suited for NPCLib.
 */
public abstract class TinyProtocol {
    private static final AtomicInteger ID = new AtomicInteger(0);

    // Used in order to lookup a channel
    private static final Reflection.MethodInvoker getPlayerHandle = Reflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
    private static final Reflection.FieldAccessor<Object> getConnection = Reflection.getField("{nms}.EntityPlayer", "playerConnection", Object.class);
    private static final Reflection.FieldAccessor<Object> getManager = Reflection.getField("{nms}.PlayerConnection", "networkManager", Object.class);
    private static final Reflection.FieldAccessor<Channel> getChannel = Reflection.getField("{nms}.NetworkManager", Channel.class, 0);

    // Looking up ServerConnection
    private static final Class<Object> minecraftServerClass = Reflection.getUntypedClass("{nms}.MinecraftServer");
    private static final Class<Object> serverConnectionClass = Reflection.getUntypedClass("{nms}.ServerConnection");
    private static final Reflection.FieldAccessor<Object> getMinecraftServer = Reflection.getField("{obc}.CraftServer", minecraftServerClass, 0);
    private static final Reflection.FieldAccessor<Object> getServerConnection = Reflection.getField(minecraftServerClass, serverConnectionClass, 0);
    // This depends on the arrangement of fields in the ServerConnection class, check in every new version for updates!
    private static final Reflection.FieldAccessor<List> networkMarkers = Reflection.getField(serverConnectionClass, List.class, 1);
//    private static final Reflection.MethodInvoker getNetworkMarkers = Reflection.getTypedMethod(serverConnectionClass, null, List.class, serverConnectionClass);

    // Packets we have to intercept
    private static final Class<?> PACKET_LOGIN_IN_START = Reflection.getMinecraftClass("PacketLoginInStart");
    private static final Reflection.FieldAccessor<GameProfile> getGameProfile = Reflection.getField(PACKET_LOGIN_IN_START, GameProfile.class, 0);

    // Speedup channel lookup
    private Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private Listener listener;

    // Channels that have already been removed
    private Set<Channel> uninjectedChannels = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    // List of network markers
    private List<Object> networkManagers;

    private final NPCLib instance;

    // Injected channel handlers
    private List<Channel> serverChannels = Lists.newArrayList();
    private ChannelInboundHandlerAdapter serverChannelHandler;
    private ChannelInitializer<Channel> beginInitProtocol;
    private ChannelInitializer<Channel> endInitProtocol;

    // Current handler name
    private String handlerName;
    private boolean injected = false;

    private volatile boolean closed;
    protected Plugin plugin;

    protected TinyProtocol(NPCLib instance) {
        this.plugin = instance.getPlugin();
        this.instance = instance;

        // Compute handler name
        this.handlerName = "tiny-" + plugin.getName() + "-" + ID.incrementAndGet();

        // Prepare existing players
        registerBukkitEvents();

        try {
            instance.getLogger().info("Attempting to inject into netty");
            registerChannelHandler();
            registerPlayers(plugin);
            injected = true;
        } catch (IllegalArgumentException ex) {
            // Damn you, late bind
            instance.getLogger().warning("Attempting to delay injection");

            new BukkitRunnable() {
                @Override
                public void run() {
                    registerChannelHandler();
                    registerPlayers(plugin);
                    injected = true;
                    instance.getLogger().info("Injection complete");
                }
            }.runTask(plugin);
        }
    }

    public boolean isInjected() {
        return injected;
    }

    private void createServerChannelHandler() {
        // Handle connected channels
        endInitProtocol = new ChannelInitializer<Channel>() {

            @SuppressWarnings("all")
            @Override
            protected void initChannel(Channel channel) throws Exception {
                try {
                    // This can take a while, so we need to stop the main thread from interfering
                    synchronized (networkManagers) {
                        // Stop injecting channels
                        if (!closed) {
                            channel.eventLoop().submit(() -> injectChannelInternal(channel));
                        }
                    }
                } catch (Exception e) {
                    instance.getLogger().severe("Cannot inject incomming channel " + channel, e);
                }
            }

        };

        // This is executed before Minecraft's channel handler
        beginInitProtocol = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(endInitProtocol);
            }

        };

        serverChannelHandler = new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                Channel channel = (Channel) msg;

                // Prepare to initialize ths channel
                channel.pipeline().addFirst(beginInitProtocol);
                ctx.fireChannelRead(msg);
            }

        };
    }

    private void registerBukkitEvents() {
        listener = new Listener() {

            @EventHandler(priority = EventPriority.MONITOR)
            public final void onPlayerLogin(PlayerLoginEvent event) {
                if (closed)
                    return;

                Channel channel = getChannel(event.getPlayer());

                // Don't inject players that have been explicitly uninjected
                if (!uninjectedChannels.contains(channel)) {
                    injectPlayer(event.getPlayer());
                }
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public final void onPlayerAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
                if (!injected) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "TinyProtocol not injected yet");
                }
            }

            @EventHandler
            public final void onPluginDisable(PluginDisableEvent e) {
                if (e.getPlugin().equals(plugin)) {
                    close();
                }
            }

        };

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    @SuppressWarnings("unchecked")
    private void registerChannelHandler() {
        Object mcServer = getMinecraftServer.get(Bukkit.getServer());
        Object serverConnection = getServerConnection.get(mcServer);
        boolean looking = true;

        // We need to synchronize against this list
        // Update 8/3/20 JMB: Fetch from field, the method doesn't exist...
        // The field getter should do the job, though I'll leave the old code here for now.
        networkManagers = (List<Object>) networkMarkers.get(serverConnection);
//        networkManagers = (List<Object>) getNetworkMarkers.invoke(null, serverConnection);
        createServerChannelHandler();

        // Find the correct list, or implicitly throw an exception
        for (int i = 0; looking; i++) {
            List<Object> list = Reflection.getField(serverConnection.getClass(), List.class, i).get(serverConnection);

            for (Object item : list) {
                if (!(item instanceof ChannelFuture))
                    break;

                // Channel future that contains the server connection
                Channel serverChannel = ((ChannelFuture) item).channel();

                serverChannels.add(serverChannel);
                serverChannel.pipeline().addFirst(serverChannelHandler);
                looking = false;
            }
        }
    }

    private void unregisterChannelHandler() {
        if (serverChannelHandler == null)
            return;

        for (Channel serverChannel : serverChannels) {
            final ChannelPipeline pipeline = serverChannel.pipeline();

            // Remove channel handler
            serverChannel.eventLoop().execute(() -> {
                try {
                    pipeline.remove(serverChannelHandler);
                } catch (NoSuchElementException exception) {
                    // That's fine
                }
            });
        }
    }

    private void registerPlayers(Plugin plugin) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    public Object onPacketInAsync(Player sender, Object packet) {
        return packet;
    }

    private void injectPlayer(Player player) {
        injectChannelInternal(getChannel(player)).player = player;
    }

    private PacketInterceptor injectChannelInternal(Channel channel) {
        try {
            PacketInterceptor interceptor = (PacketInterceptor) channel.pipeline().get(handlerName);

            // Inject our packet interceptor
            if (interceptor == null) {
                interceptor = new PacketInterceptor();
                channel.pipeline().addBefore("packet_handler", handlerName, interceptor);
                uninjectedChannels.remove(channel);
            }
            return interceptor;
        } catch (IllegalArgumentException e) {
            // Try again
            return (PacketInterceptor) channel.pipeline().get(handlerName);
        }
    }

    private Channel getChannel(Player player) {
        Channel channel = channelLookup.get(player.getName());

        // Lookup channel again
        if (channel == null) {
            Object connection = getConnection.get(getPlayerHandle.invoke(player));
            Object manager = getManager.get(connection);

            channelLookup.put(player.getName(), channel = getChannel.get(manager));
        }

        return channel;
    }

    private void close() {
        if (!closed) {
            closed = true;

            // Remove our handlers
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                // No need to guard against this if we're closing
                Channel channel = getChannel(player);
                if (!closed) {
                    uninjectedChannels.add(channel);
                }

                // See ChannelInjector in ProtocolLib, line 590
                channel.eventLoop().execute(() -> channel.pipeline().remove(handlerName));
            }

            // Clean up Bukkit
            HandlerList.unregisterAll(listener);
            unregisterChannelHandler();
        }
    }

    // Keeping this here for testing purposes:
//    public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
//        return packet;
//    }

    private final class PacketInterceptor extends ChannelDuplexHandler {
        // Updated by the login event
        public volatile Player player;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // Intercept channel
            final Channel channel = ctx.channel();
            handleLoginStart(channel, msg);

            try {
                msg = onPacketInAsync(player, msg);
            } catch (Exception e) {
                instance.getLogger().severe("Error in onPacketInAsync().", e);
            }

            if (msg != null) {
                super.channelRead(ctx, msg);
            }
        }

        // Keeping this here for testing purposes:
//        @Override
//        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//            try {
//                msg = onPacketOutAsync(player, ctx.channel(), msg);
//            } catch (Exception e) {
//                instance.getLogger().severe("Error in onPacketOutAsync().", e);
//            }
//
//            if (msg != null) {
//                super.write(ctx, msg, promise);
//            }
//        }

        private void handleLoginStart(Channel channel, Object packet) {
            if (PACKET_LOGIN_IN_START.isInstance(packet)) {
                GameProfile profile = getGameProfile.get(packet);
                channelLookup.put(profile.getName(), channel);
            }
        }
    }
}
