package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorldListener implements Listener {

    private final NPCLib instance;

    public PlayerChangeWorldListener(NPCLib instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();

        // The PlayerTeleportEvent is called, and will handle visibility in the new world.
        for (NPC npc : instance.getNPCs()) {
            if (npc.isShown(player) && npc.getWorld().equals(from)) {
                npc.hide(player);
                // TODO: Expose auto-hidden set?
            }
        }
    }
}
