/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Objects;

/**
 * @author Jitse Boonstra
 */
public class ChunkListener implements Listener {

    private final NPCLib instance;

    public ChunkListener(NPCLib instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        for (NPC npc : instance.getNPCs()) {
            if (npc.getLocation() == null || !isSameChunk(npc.getLocation(), chunk))
                continue; // We aren't unloading the chunk with the NPC in it.

            // We found an NPC in the chunk being unloaded. Time to hide this NPC from all players.
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (npc.isShown(player) && !npc.isAutoHidden(player)) {
                    npc.hide(player);
                    // TODO: Add to auto-hidden (expose boolean?)
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        for (NPC npc : instance.getNPCs()) {
            if (npc.getLocation() == null || !isSameChunk(npc.getLocation(), chunk))
                continue; // The NPC is not in the loaded chunk.

            // The chunk being loaded has this NPC in it. Showing it to all the players again.
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (npc.isShown(player) && npc.isAutoHidden(player) && Objects.equals(npc.getWorld(), player.getWorld())) {
                    double hideDistance = instance.getAutoHideDistance();
                    double distanceSquared = player.getLocation().distanceSquared(npc.getLocation());
                    boolean inRange = distanceSquared <= (hideDistance * hideDistance) || distanceSquared <= (Bukkit.getViewDistance() << 4);

                    // Show the NPC (if in range).
                    if (inRange) {
                        npc.show(player);
                        // TODO: Remove from auto-hidden
                    }
                }
            }
        }
    }

    private static int getChunkCoordinate(int coordinate) {
        return coordinate >> 4;
    }

    // Using Location#getChunk will load the chunk, which is a pretty hefty task, so we'll want to avoid it.
    // Using Location#getChunk would effectively mean we'd load every chunk an NPC is located in when any chunk is unloaded.
    private static boolean isSameChunk(Location loc, Chunk chunk) {
        return getChunkCoordinate(loc.getBlockX()) == chunk.getX()
                && getChunkCoordinate(loc.getBlockZ()) == chunk.getZ();
    }
}
