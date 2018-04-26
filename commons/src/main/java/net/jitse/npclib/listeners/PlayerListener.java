/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * @author Jitse Boonstra
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (npc.getAutoHidden().contains(player.getUniqueId())) {
                npc.getAutoHidden().remove(player.getUniqueId());
            }

            // Don't need to use NPC#hide since the entity is not registered in the NMS server.
            if (npc.getShown().contains(player.getUniqueId())) {
                npc.getShown().remove(player.getUniqueId());
            }
        }
    }

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
        World world = player.getWorld();
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (!npc.getShown().contains(player.getUniqueId())) {
                continue; // NPC was never supposed to be shown to the player.
            }

            if (!npc.getLocation().getWorld().equals(world)) {
                continue; // NPC is not in the same world.
            }

            // If Bukkit doesn't track the NPC entity anymore, bypass the hiding distance variable.
            // This will cause issues otherwise (e.g. custom skin disappearing).
            double hideDistance = npc.getAutoHideDistance();
            double distanceSquared = player.getLocation().distanceSquared(npc.getLocation());
            boolean inRange = distanceSquared <= (hideDistance * hideDistance) || distanceSquared <= (Bukkit.getViewDistance() << 4);
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
