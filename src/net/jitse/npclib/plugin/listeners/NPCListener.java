/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.plugin.listeners;

import net.jitse.npclib.events.NPCDestroyEvent;
import net.jitse.npclib.events.NPCInteractEvent;
import net.jitse.npclib.events.NPCSpawnEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Jitse Boonstra
 */
public class NPCListener implements Listener {

    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent event) {
        event.getPlayer().sendMessage(ChatColor.GREEN + "Spawned NPC " + event.getNPC().getEntityId());
    }

    @EventHandler
    public void onNPCDestroy(NPCDestroyEvent event) {
        event.getPlayer().sendMessage(ChatColor.RED + "Destroyed NPC " + event.getNPC().getEntityId());
    }

    @EventHandler
    public void onNPCInteract(NPCInteractEvent event) {
        event.getWhoClicked().sendMessage(ChatColor.BLUE + "Interacted with NPC "
                + event.getNPC().getEntityId() + " type " + event.getClickType());
    }
}
