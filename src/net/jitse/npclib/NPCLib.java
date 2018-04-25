/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib;

import net.jitse.npclib.api.NPC;
import net.jitse.npclib.listeners.packet.PacketListener;
import net.jitse.npclib.listeners.player.PlayerChangedWorldListener;
import net.jitse.npclib.listeners.player.PlayerMoveOrTeleportListener;
import net.jitse.npclib.listeners.player.PlayerQuitListener;
import net.jitse.npclib.listeners.world.ChunkLoadListener;
import net.jitse.npclib.listeners.world.ChunkUnloadListener;
import net.jitse.npclib.skin.Skin;
import net.jitse.npclib.version.Version;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Jitse Boonstra
 */
public class NPCLib {

    private final Server server;
    private final JavaPlugin plugin;
    private final Version version;

    public NPCLib(JavaPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();

        String versionName = server.getClass().getPackage().getName().split("\\.")[3];
        version = Version.getByName(versionName).orElse(null);

        if (version == null) {
            server.getConsoleSender().sendMessage(ChatColor.RED + "NPCLib failed to initiate. Your server's version ("
                    + versionName + ") is not supported.");
        }

        server.getConsoleSender().sendMessage(ChatColor.BLUE + "[NPCLib] " + ChatColor.WHITE + "Enabled for version " + version.toString() + ".");

        registerInternal();
    }

    private void registerInternal() {
        PluginManager pluginManager = server.getPluginManager();

        pluginManager.registerEvents(new PlayerChangedWorldListener(), plugin);
        pluginManager.registerEvents(new PlayerQuitListener(), plugin);
        pluginManager.registerEvents(new PlayerMoveOrTeleportListener(), plugin);
        pluginManager.registerEvents(new ChunkLoadListener(), plugin);
        pluginManager.registerEvents(new ChunkUnloadListener(), plugin);

        new PacketListener().start(plugin);
    }

    /**
     * Create a new non-player character (NPC).
     *
     * @param skin             The skin you want the NPC to have.
     * @param autoHideDistance Distance from where you want to NPC to hide from the player (50 recommended).
     * @param lines            The text you want to sendShowPackets above the NPC (null = no text).
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC(Skin skin, double autoHideDistance, List<String> lines) {
        try {
            return version.createNPC(plugin, skin, autoHideDistance, lines);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            server.getConsoleSender().sendMessage(ChatColor.RED + "NPCLib failed to create NPC. Please report this stacktrace:");
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * Create a new non-player character (NPC).
     *
     * @param skin  The skin you want the NPC to have.
     * @param lines The text you want to sendShowPackets above the NPC (null = no text).
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC(Skin skin, List<String> lines) {
        return createNPC(skin, 50, lines);
    }


    /**
     * Create a new non-player character (NPC).
     *
     * @param skin The skin you want the NPC to have.
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC(Skin skin) {
        return createNPC(skin, 50, null);
    }


    /**
     * Create a new non-player character (NPC).
     *
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC() {
        return createNPC(null, 50, null);
    }
}
