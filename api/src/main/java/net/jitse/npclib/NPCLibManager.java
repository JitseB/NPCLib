package net.jitse.npclib;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCLibManager {

    private static NPCLib library;

    public static NPCLib initialize(JavaPlugin plugin, NPCLibOptions options) {
        Validate.notNull(plugin, "Plugin cannot be null");
        if (options == null) {
            initialize(plugin);
        }

        if (library != null) {
            throw new IllegalStateException("Cannot initialize NPCLib more than once! Use NPCLibManager#getLibrary to get the library instance");
        }

        NPCLibManager.library = new NPCLib(plugin, options.moveHandling);

        return library;
    }

    public static void initialize(JavaPlugin plugin) {
        Validate.notNull(plugin, "Plugin cannot be null");

        if (library != null) {
            throw new IllegalStateException("Cannot initialize NPCLib more than once! Use NPCLibManager#getLibrary to get the library instance");
        }

        NPCLibManager.library = new NPCLib(plugin, NPCLibOptions.MovementHandling.playerMoveEvent());
    }

    public static boolean isInitialized() {
        return library != null;
    }

    public static NPCLib getLibrary() {
        if (library == null) {
            throw new IllegalStateException("Library has not been initialized yet! Use NPCLibManager#initialize to initialize the library instance");
        }
        return library;
    }

    private NPCLibManager() {
        throw new SecurityException("You cannot initialize this class");
    }
}
