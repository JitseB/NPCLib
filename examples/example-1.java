package net.jitse.npclib.example;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Jitse Boonstra
 */
public class NPCLibTest extends JavaPlugin implements Listener {

    /*
        The most basic example of spawning an NPC with NPCLib.
        It will spawn an NPC on un-sneaking at the player's location.
        And it will send a message with its ID on interact.
     */

    private NPCLib npclib;

    @Override
    public void onEnable() {
        this.npclib = new NPCLib(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            return;
        }

        NPC npc = npclib.createNPC();
        npc.setLocation(event.getPlayer().getLocation());
        npc.create();
        npc.show(event.getPlayer());
    }
}
