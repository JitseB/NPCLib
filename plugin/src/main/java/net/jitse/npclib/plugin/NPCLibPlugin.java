/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.plugin;

import net.jitse.npclib.NPCLibManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCLibPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!NPCLibManager.isInitialized()) {
            NPCLibManager.initialize(this);
        }
    }
}
