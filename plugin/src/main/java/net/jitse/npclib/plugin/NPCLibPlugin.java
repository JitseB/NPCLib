/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.plugin;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Jitse Boonstra
 */
public class NPCLibPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("NPC library loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NPC library unloaded.");
    }
}
