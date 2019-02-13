/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib;

import net.jitse.npclib.api.NPC;
import net.jitse.npclib.listeners.ChunkListener;
import net.jitse.npclib.listeners.LegacyPacketListener;
import net.jitse.npclib.listeners.PacketListener;
import net.jitse.npclib.listeners.PlayerListener;
import net.jitse.npclib.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * @author Jitse Boonstra
 */
public class NPCLib {

    private final Server server;
    private final JavaPlugin plugin;
    private final Class<?> npcClass;

    public NPCLib(JavaPlugin plugin) {
        this(plugin, true);
    }

    public NPCLib(JavaPlugin plugin, boolean message) {
        this.plugin = plugin;
        this.server = plugin.getServer();

        String versionName = server.getClass().getPackage().getName().split("\\.")[3];

        Class<?> npcClass = null;

        try {
            npcClass = Class.forName("net.jitse.npclib.nms." + versionName + ".NPC_" + versionName);
        } catch (ClassNotFoundException exception) {
            // Version not supported, error below.
        }

        this.npcClass = npcClass;

        if (npcClass == null) {
            server.getConsoleSender().sendMessage(ChatColor.RED + "NPCLib failed to initiate. Your server's version ("
                    + versionName + ") is not supported.");
            return;
        }

        if (message) {
            server.getConsoleSender().sendMessage(ChatColor.BLUE + "[NPCLib] " + ChatColor.WHITE + "Enabled for version " + versionName + ".");
        }

        registerInternal();
    }

    private void registerInternal() {
        PluginManager pluginManager = server.getPluginManager();

        pluginManager.registerEvents(new PlayerListener(), plugin);
        pluginManager.registerEvents(new ChunkListener(), plugin);

        // Boot the according packet listener.
        if (Bukkit.getBukkitVersion().contains("1.7")) {
            new LegacyPacketListener().start(plugin);
        } else {
            new PacketListener().start(plugin);
        }
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
            return (NPC) npcClass.getConstructors()[0].newInstance(plugin, skin, autoHideDistance, lines);
        } catch (Exception exception) {
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
