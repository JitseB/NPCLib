/*
 * Copyright (c) Jitse Boonstra 2018 All rights reserved.
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.UUID;

/**
 * @author Jitse Boonstras
 */
public class ChunkListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        for (NPC npc : NPCManager.getAllNPCs()) {
            Chunk npcChunk = npc.getLocation().getChunk();

            if (chunk.getX() == npcChunk.getX() && chunk.getZ() == npcChunk.getZ()) {
                // Unloaded chunk with NPC in it. Hiding it from all players currently shown to.

                for (UUID uuid : npc.getShown()) {
                    // Safety check so it doesn't send packets if the NPC has already
                    // been automatically despawned by the system.
                    if (npc.getAutoHidden().contains(uuid)) {
                        continue;
                    }

                    npc.hide(Bukkit.getPlayer(uuid), true);
                }
            }
        }
    }
}
