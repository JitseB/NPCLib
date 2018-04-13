package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handleEvent(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        handleEvent(event.getPlayer());
    }

    private void handleEvent(Player player) {
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
