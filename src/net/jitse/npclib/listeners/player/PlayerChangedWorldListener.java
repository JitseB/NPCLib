/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners.player;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * @author Jitse Boonstra
 */
public class PlayerChangedWorldListener implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();

        // The PlayerTeleportEvent is call, and will handle visibility in the new world.
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (npc.getLocation().getWorld().equals(from)) {
                if (!npc.getAutoHidden().contains(player.getUniqueId())) {
                    npc.getAutoHidden().add(player.getUniqueId());
                    npc.hide(player, true);
                }
            }
        }
    }
}
