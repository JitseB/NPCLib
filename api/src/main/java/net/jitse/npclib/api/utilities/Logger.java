/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api.utilities;

import org.bukkit.Bukkit;

import java.util.logging.Level;

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
        log(Level.INFO, info);
    }

    public void warning(String warning) {
        log(Level.WARNING, warning);
    }

    public void warning(String warning, Throwable throwable) {
        log(Level.WARNING, warning, throwable);
    }

    public void severe(String severe) {
        log(Level.SEVERE, severe);
    }

    public void severe(String severe, Throwable throwable) {
        log(Level.SEVERE, severe, throwable);
    }

    public void log(Level level, String message) {
        if (enabled) {
            Bukkit.getLogger().log(level, prefix + message);
        }
    }

    public void log(Level level, String message, Throwable throwable) {
        if (enabled) {
            Bukkit.getLogger().log(level, prefix + message, throwable);
        }
    }
}
