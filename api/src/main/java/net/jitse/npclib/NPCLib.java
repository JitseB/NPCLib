/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib;

import net.jitse.npclib.NPCLibOptions.MovementHandling;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.listeners.*;
import net.jitse.npclib.metrics.NPCLibMetrics;
import net.jitse.npclib.utilities.Logger;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NPCLib {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final Class<?> npcClass;

    private double autoHideDistance = 50.0;
    private Set<NPC> npcs = new HashSet<>();

    protected NPCLib(JavaPlugin plugin, MovementHandling moveHandling) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(moveHandling, "moveHandling cannot be null");

        this.plugin = plugin;
        this.logger = new Logger("NPCLib");

        String versionName = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];

        Class<?> npcClass = null;

        try {
            npcClass = Class.forName("net.jitse.npclib.nms." + versionName + ".NPC_" + versionName);
        } catch (ClassNotFoundException exception) {
            // Version not supported, error below.
        }

        this.npcClass = npcClass;

        if (npcClass == null) {
            logger.severe("Failed to initiate. Your server's version (" + versionName + ") is not supported");
            return;
        }

        PluginManager pluginManager = plugin.getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerRespawnListener(this), plugin);
        pluginManager.registerEvents(new ChunkListener(this), plugin);
        pluginManager.registerEvents(new PlayerChangeWorldListener(this), plugin);
        pluginManager.registerEvents(new PlayerDeathListener(this), plugin);
        pluginManager.registerEvents(new PlayerQuitListener(this), plugin);
        pluginManager.registerEvents(new PlayerTeleportListener(), plugin);

        if (moveHandling.usePme) {
            pluginManager.registerEvents(new PlayerMoveEventListener(), plugin);
        } else {
            pluginManager.registerEvents(new PeriodicMoveListener(this, moveHandling.updateInterval), plugin);
        }

        // Boot the according packet listener.
        new PacketListener().start(this);

        // Start metrics.
        new NPCLibMetrics(this);

        logger.info("Enabled for Minecraft " + versionName);
    }

    /**
     * @return The JavaPlugin instance.
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Set a new value for the auto-hide distance.
     * A recommended value (and default) is 50 blocks.
     *
     * @param autoHideDistance The new value.
     */
    public void setAutoHideDistance(double autoHideDistance) {
        this.autoHideDistance = autoHideDistance;
    }

    /**
     * @return The auto-hide distance.
     */
    public double getAutoHideDistance() {
        return autoHideDistance;
    }

    /**
     * @return The logger NPCLib uses.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Create a new non-player character (NPC).
     *
     * @param text The text you want to sendShowPackets above the NPC (null = no text).
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC(List<String> text) {
        try {
            NPC npc = (NPC) npcClass.getConstructors()[0].newInstance(this, text);
            npcs.add(npc);
            return npc;
        } catch (Exception exception) {
            logger.warning("Failed to create NPC. Please report the following stacktrace message", exception);
        }

        return null;
    }

    /**
     * Create a new non-player character (NPC).
     *
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC() {
        return createNPC(null);
    }

    /**
     * Get all NPCs created using NPCLib (from all plugins).
     *
     * @return A set with all NPCs.
     */
    public Set<NPC> getNPCs() {
        return npcs;
    }

    /**
     * Destroy the NPC, i.e. remove it from the registry.
     * Sets up object for removal by GC (garbage-collector).
     *
     * @param npc The NPC to destroy.
     */
    public void destroyNPC(NPC npc) {
        // Destroy NPC for every player that is still seeing it.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!npc.isShown(player) || npc.isAutoHidden(player)) {
                continue;
            }

            // destroy the per player holograms and hide the NPC.
            npc.getPlayerHologram(player).hide(player);
            npc.hide(player);
        }
    }

    /**
     * Get all NPCs created using NPCLib (from all plugins) shown to a player.
     * This method does not account for auto hiding of NPCs (these are included).
     *
     * @param player The player who can see the NPCs.
     * @return A set with all visible NPCs to the player.
     */
    public Set<NPC> getNPCs(Player player) {
        Set<NPC> set = Collections.emptySet();
        for (NPC npc : npcs) {
            if (npc.isShown(player)) {
                set.add(npc);
            }
        }
        return set;
    }
}
