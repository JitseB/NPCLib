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
        getLogger().info("NPCLib classes loaded");
    }

    @Override
    public void onDisable() {
        getLogger().info("NPCLib classes unloaded");
    }
}
