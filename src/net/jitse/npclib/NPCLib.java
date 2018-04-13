package net.jitse.npclib;

import net.jitse.npclib.api.NPC;
import net.jitse.npclib.listeners.PacketListener;
import net.jitse.npclib.listeners.PlayerLeaveListener;
import net.jitse.npclib.listeners.PlayerMoveListener;
import net.jitse.npclib.skin.Skin;
import net.jitse.npclib.version.Version;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class NPCLib {

    private final JavaPlugin plugin;
    private final Version version;

    public NPCLib(JavaPlugin plugin) {
        this.plugin = plugin;

        String versionName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        version = Version.getByName(versionName).orElse(null);

        if (version == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "NPCLib failed to initiate. Your server's version ("
                    + versionName + ") is not supported.");
        }

        registerInternal();
    }

    private void registerInternal() {
        plugin.getServer().getPluginManager().registerEvents(new PlayerMoveListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), plugin);

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
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "NPCLib failed to create NPC. Please report this stacktrace:");
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
}
