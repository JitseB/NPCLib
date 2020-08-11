/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Jitse Boonstra
 */
public class PlayerRespawnListener extends HandleMoveBase implements Listener {

    private final NPCLib instance;

    public PlayerRespawnListener(NPCLib instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // If the player dies in the server spawn world, the world change event isn't called (nor is the PlayerTeleportEvent).
        Player player = event.getPlayer();
        Location respawn = event.getRespawnLocation();
        if (respawn.getWorld() != null && respawn.getWorld().equals(player.getWorld())) {
            // Waiting until the player is moved to the new location or else it'll mess things up.
            // I.e. if the player is at great distance from the NPC spawning, they won't be able to see it.
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        this.cancel();
                        return;
                    }
                    if (player.getLocation().equals(respawn)) {
                        handleMove(player);
                        this.cancel();
                    }
                }
            }.runTaskTimer(instance.getPlugin(), 0L, 1L);
        }
    }
}
