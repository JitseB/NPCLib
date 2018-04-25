/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners.player;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Jitse Boonstra
 */
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (npc.getAutoHidden().contains(player.getUniqueId())) {
                npc.getAutoHidden().remove(player.getUniqueId());
            }

            if (npc.isActuallyShown(player)) {
                npc.hide(player);
            }
        }
    }
}
