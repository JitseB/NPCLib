package com.bnstra.npclib.example;

import com.bnstra.npclib.NPCLib;
import com.bnstra.npclib.api.NPC;
import com.bnstra.npclib.api.events.NPCInteractEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.SortedSet;

/**
 * @author Jitse Boonstra
 */
public class NPCLibTest extends JavaPlugin implements Listener {

    /*
        This is an example class, it spawns an NPC with some text at the player's location when un-sneaking.
        It will also send an interactor a message if it's the first NPC spawned.
     */

    // To keep track of NPC IDs.
    private final SortedSet<String> ids = new TreeSet<>();
    private NPCLib npclib;

    @Override
    public void onEnable() {
        this.npclib = new NPCLib(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            return;
        }

        NPC npc = npclib.createNPC(Arrays.asList(ChatColor.WHITE + "Hi there (#2)", ChatColor.YELLOW + "Click on me!"));
        npc.setLocation(event.getPlayer().getLocation());
        ids.add(npc.getId());
        npc.create();
        npc.show(event.getPlayer());
    }

    @EventHandler
    public void onNPCInteract(NPCInteractEvent event) {
        String firstId = ids.first();
        if (event.getNPC().getId().equals(firstId)) {
            event.getWhoClicked().sendMessage(ChatColor.GREEN + "I'm the first NPC spawned since boot!");
        }
    }
}
