package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final NPCLib instance;

    public PlayerDeathListener(NPCLib instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Need to auto hide the NPCs from the player, or else the system will think they can see the NPC on respawn.
        Player player = event.getEntity();
        for (NPC npc : instance.getNPCs()) {
            if (npc.isShown(player) && npc.getWorld().equals(player.getWorld()))
                npc.hide(player);
        }
    }
}
