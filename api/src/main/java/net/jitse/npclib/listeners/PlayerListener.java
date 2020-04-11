/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.internal.NPCBase;
import net.jitse.npclib.internal.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 * @author Jitse Boonstra
 */
public class PlayerListener implements Listener {

    private final NPCLib instance;

    public PlayerListener(NPCLib instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    private void onPlayerLeave(Player player) {
        for (NPCBase npc : NPCManager.getAllNPCs())
            npc.onLogout(player);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();

        // The PlayerTeleportEvent is call, and will handle visibility in the new world.
        for (NPCBase npc : NPCManager.getAllNPCs()) {
            if (npc.getWorld().equals(from)) {
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
        // 11/4/20: Added pitch and yaw to the if statement. If the change in this is 10 or more degrees, check the movement.
        if (to == null || (Math.abs(from.getPitch() - to.getPitch()) <= 10
                || Math.abs(from.getYaw() - to.getYaw()) <= 10
                || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()))
            handleMove(event.getPlayer()); // Verify the player changed which block they are on. Since PlayerMoveEvent is one of the most called events, this is worth it.
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMove(event.getPlayer());
    }

    private void handleMove(Player player) {
        World world = player.getWorld();
        for (NPCBase npc : NPCManager.getAllNPCs()) {
            if (!npc.getShown().contains(player.getUniqueId())) {
                continue; // NPC was never supposed to be shown to the player.
            }

            if (!npc.getWorld().equals(world)) {
                continue; // NPC is not in the same world.
            }

            // If Bukkit doesn't track the NPC entity anymore, bypass the hiding distance variable.
            // This will cause issues otherwise (e.g. custom skin disappearing).
            double hideDistance = instance.getAutoHideDistance();
            double distanceSquared = player.getLocation().distanceSquared(npc.getLocation());

            int tempRange = Bukkit.getViewDistance() << 4;
            boolean inRange = distanceSquared <= (hideDistance * hideDistance) && distanceSquared <= (tempRange * tempRange); // Avoids Math.pow due to how intensive it is. Could make a static utility function for it.
            if (npc.getAutoHidden().contains(player.getUniqueId())) {
                // Check if the player and NPC are within the range to sendShowPackets it again.
                if (inRange) {
                    npc.getAutoHidden().remove(player.getUniqueId());
                    npc.show(player, true);
                }
            } else {
                // Check if the player and NPC are out of range to sendHidePackets it.
                if (!inRange) {
                    npc.getAutoHidden().add(player.getUniqueId());
                    npc.hide(player, true);
                }
            }
        }
    }
}
