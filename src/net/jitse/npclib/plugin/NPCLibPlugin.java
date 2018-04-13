/*
 * Copyright (c) Jitse Boonstra 2018 All rights reserved.
 */

package net.jitse.npclib.plugin;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.plugin.listeners.NPCListener;
import net.jitse.npclib.skin.MineSkinFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * @author Jitse Boonstra
 */
public class NPCLibPlugin extends JavaPlugin implements Listener {

    private NPCLib npcLib;

    @Override
    public void onEnable() {
        this.npcLib = new NPCLib(this);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "NPCLib enabled.");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new NPCListener(), this);
    }

    @EventHandler
    public void onPlayerShift(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            return;
        }

        MineSkinFetcher.fetchSkinFromIdAsync(168841, skin -> {
            NPC npc = npcLib.createNPC(skin, 5, Arrays.asList(
                    ChatColor.BOLD + "NPC Library", "",
                    "Create your own", "non-player characters",
                    "with the simplistic", "API of NPCLib!"
            ));
            npc.create(event.getPlayer().getLocation());
            npc.show(event.getPlayer());
        });
    }
}
