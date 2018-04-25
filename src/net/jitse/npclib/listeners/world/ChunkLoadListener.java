/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners.world;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.UUID;

/**
 * @author Jitse Boonstra
 */
public class ChunkLoadListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        for (NPC npc : NPCManager.getAllNPCs()) {
            Chunk npcChunk = npc.getLocation().getChunk();

            if (chunk.equals(npcChunk)) {
                // Loaded chunk with NPC in it. Showing it to the players again.

                for (UUID uuid : npc.getShown()) {
                    // Make sure not to respawn a not-hidden NPC.
                    if (!npc.getAutoHidden().contains(uuid)) {
                        continue;
                    }

                    Player player = Bukkit.getPlayer(uuid);

                    if (!npcChunk.getWorld().equals(player.getWorld())) {
                        continue; // Player and NPC are not in the same world.
                    }

                    double hideDistance = npc.getAutoHideDistance();
                    double distanceSquared = player.getLocation().distanceSquared(npc.getLocation());
                    boolean inRange = distanceSquared <= (hideDistance * hideDistance) || distanceSquared <= (Bukkit.getViewDistance() << 4);

                    // Show the NPC (if in range).
                    if (inRange) {
                        npc.show(Bukkit.getPlayer(uuid), true);
                    }
                }
            }
        }
    }
}
