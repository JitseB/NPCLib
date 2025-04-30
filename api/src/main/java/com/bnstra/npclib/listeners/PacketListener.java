/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.listeners;

import com.comphenix.tinyprotocol.Reflection;
import com.comphenix.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import com.bnstra.npclib.NPCLib;
import com.bnstra.npclib.api.events.NPCInteractEvent;
import com.bnstra.npclib.internal.MinecraftVersion;
import com.bnstra.npclib.internal.NPCBase;
import com.bnstra.npclib.internal.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * @author Jitse Boonstra
 */
public class PacketListener {

    // Deduce NMS version
    private final MinecraftVersion version = MinecraftVersion.valueOf(
            Bukkit.getServer().getClass().getPackage().getName()
            .replace("org.bukkit.craftbukkit", "")
            .replace(".", "").toUpperCase());

    // Classes:
    private final Class<?> packetPlayInUseEntityClazz = Reflection.getClass("{nms}.PacketPlayInUseEntity", "net.minecraft.network.protocol.game.PacketPlayInUseEntity");

    // Fields:
    private final Reflection.FieldAccessor<Integer> entityIdField = Reflection
            .getField(packetPlayInUseEntityClazz, version.isAboveOrEqual(MinecraftVersion.V1_20_R4) ? "b" : "a", int.class);
    private final Reflection.FieldAccessor<?> actionField = Reflection.getField(packetPlayInUseEntityClazz,
            Object.class, version.isAboveOrEqual(MinecraftVersion.V1_20_R4) ? 1 : 0);

    // Prevent players from clicking at very high speeds.
    private final Set<UUID> delay = new HashSet<>();

    private Plugin plugin;

    public void start(NPCLib instance) {
        this.plugin = instance.getPlugin();

        new TinyProtocol(this.plugin) {

            @Override
            public Object onPacketInAsync(Player player, Channel channel, Object packet) {
                return handleInteractPacket(player, packet) ? super.onPacketInAsync(player, channel, packet) : null;
            }
        };
    }

    private boolean handleInteractPacket(Player player, Object packet) {
        if (!packetPlayInUseEntityClazz.isInstance(packet) || player == null)
            return true; // We aren't handling the packet.

        NPCBase npc = null;
        int packetEntityId = entityIdField.get(packet);

        // Not using streams here is an intentional choice.
        // Packet listeners is one of the few places where it is important to write optimized code.
        // Lambdas (And the stream api) create a massive amount of objects, especially if it isn't a static lambda.
        // So, we're avoiding them here.
        // ~ Kneesnap, 9 / 20 / 2019.

        for (NPCBase testNPC : NPCManager.getAllNPCs()) {
            if (testNPC.isCreated() && testNPC.isShown(player) && testNPC.getEntityId() == packetEntityId) {
                npc = testNPC;
                break;
            }
        }

        if (npc == null) {
            // Default player, not doing magic with the packet.
            return true;
        }

        if (delay.contains(player.getUniqueId())) { // There is an active delay.
            return false;
        }

        NPCInteractEvent.ClickType clickType;
        if (version.isAboveOrEqual(MinecraftVersion.V1_17_R1)) {
            if (actionField.get(packet).getClass().getTypeName().contains("PacketPlayInUseEntity$1")) {
                clickType = NPCInteractEvent.ClickType.RIGHT_CLICK;
            } else if (actionField.get(packet).getClass().getTypeName().contains("PacketPlayInUseEntity$d")
                    || actionField.get(packet).getClass().getTypeName().contains("PacketPlayInUseEntity$e")) {
                clickType = NPCInteractEvent.ClickType.LEFT_CLICK;
            } else {
                throw new IllegalStateException("Did not recognize click type: " + actionField.get(packet).getClass().getTypeName());
            }

            // Reflections based - but it might be easier to use the code above
            // Every clicktype must be accessed through its own class... stupid...
//            for (Method m : actionField.get(packet).getClass().getMethods()) {
//                Bukkit.broadcastMessage(ChatColor.GRAY + m.toString());
//            }
//
//            // i.e. the version is newer than 1.17 R1
//            Class<?> clazz = Reflection.getClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity$1");
//            Class<?> clazz2 = Reflection.getClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity$b");
//            try {
//                Reflection.MethodInvoker method = Reflection.getTypedMethod(clazz, "a", clazz2);
//                if (Objects.equals(method.invoke(actionField.get(packet)).toString(), "ATTACK"))
//                    clickType = NPCInteractEvent.ClickType.LEFT_CLICK;
//            } catch (Exception e) {
//                clazz = Reflection.getClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity$d");
//                Reflection.MethodInvoker method = Reflection.getTypedMethod(clazz, "a", clazz2);
//                if (method.invoke(actionField.get(packet)).toString().contains("INTERACT"))
//                    clickType = NPCInteractEvent.ClickType.RIGHT_CLICK;
//            }
        }
        else {
            clickType = actionField.get(packet).toString().equals("ATTACK")
                    ? NPCInteractEvent.ClickType.LEFT_CLICK : NPCInteractEvent.ClickType.RIGHT_CLICK;
        }

        delay.add(player.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, new TaskCallNpcInteractEvent(new NPCInteractEvent(player, clickType, npc), this));
        return false;
    }

    // This would be a non-static lambda, and its usage matters, so we'll make it a full class.
    private static final class TaskCallNpcInteractEvent implements Runnable {
        private NPCInteractEvent eventToCall;
        private PacketListener listener;

        private static Location playerLocation = new Location(null, 0, 0, 0);

        TaskCallNpcInteractEvent(NPCInteractEvent eventToCall, PacketListener listener) {
            this.eventToCall = eventToCall;
            this.listener = listener;
        }

        @Override
        public void run() {
            Player player = eventToCall.getWhoClicked();
            this.listener.delay.remove(player.getUniqueId()); // Remove the NPC from the interact cooldown.
            if (!player.getWorld().equals(eventToCall.getNPC().getWorld()))
                return; // If the NPC and player are not in the same world, abort!

            double distance = player.getLocation(playerLocation).distanceSquared(eventToCall.getNPC().getLocation());
            if (distance <= 64) // Only handle the interaction if the player is within interaction range. This way, hacked clients can't interact with NPCs that they shouldn't be able to interact with.
                Bukkit.getPluginManager().callEvent(this.eventToCall);
        }
    }
}
