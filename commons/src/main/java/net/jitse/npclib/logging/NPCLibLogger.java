/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.logging;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class NPCLibLogger extends Logger {

    public NPCLibLogger(Plugin context) {
        super(context.getClass().getCanonicalName(), null);
        setParent(context.getServer().getLogger());
        setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecord.setMessage("[NPCLib] " + logRecord.getMessage());
        super.log(logRecord);
    }
}
