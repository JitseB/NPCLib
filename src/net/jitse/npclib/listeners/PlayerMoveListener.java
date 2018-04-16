/*
 * Copyright (c) Jitse Boonstra 2018 All rights reserved.
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * @author Jitse Boonstra
 */
public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        handleMove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMove(event.getPlayer());
    }

    private void handleMove(Player player) {
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (!npc.getShown().contains(player.getUniqueId())) {
                continue; // NPC was never supposed to be shown to the player.
            }

            double hideDistance = npc.getAutoHideDistance();
            boolean inRange = player.getLocation().distanceSquared(npc.getLocation()) <= (hideDistance * hideDistance);
            if (npc.getAutoHidden().contains(player.getUniqueId())) {
                // Check if the player and NPC are within the range to sendShowPackets it again.
                if (inRange) {
                    npc.show(player, true);
                    npc.getAutoHidden().remove(player.getUniqueId());
                }
            } else {
                // Check if the player and NPC are out of range to sendHidePackets it.
                if (!inRange) {
                    npc.hide(player, true);
                    npc.getAutoHidden().add(player.getUniqueId());
                }
            }
        }
    }
}
