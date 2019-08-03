/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api.utilities;

import org.bukkit.Bukkit;

public class Logger {

    private final String prefix;

    private boolean enabled = true;

    public Logger(String prefix) {
        this.prefix = prefix + " ";
    }

    public void disable() {
        this.enabled = false;
    }

    public void info(String info) {
        if (!enabled) {
            return;
        }

        Bukkit.getLogger().info(prefix + info);
    }

    public void warning(String warning) {
        if (!enabled) {
            return;
        }

        Bukkit.getLogger().warning(prefix + warning);
    }

    public void severe(String severe) {
        if (!enabled) {
            return;
        }

        Bukkit.getLogger().severe(prefix + severe);
    }
}
