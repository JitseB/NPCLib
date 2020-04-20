package net.jitse.npclib.listeners;

import org.bukkit.entity.Player;

import net.jitse.npclib.internal.NPCBase;
import net.jitse.npclib.internal.NPCManager;

public class HandleMoveBase {

    void handleMove(Player player) {
        for (NPCBase npc : NPCManager.getAllNPCs()) {
            if (!npc.getShown().contains(player.getUniqueId())) {
                continue; // NPC was never supposed to be shown to the player.
            }

            if (!npc.isShown(player) && npc.inRangeOf(player) && npc.inViewOf(player)) {
                // The player is in range and can see the NPC, auto-show it.
                npc.show(player, true);
            } else if (npc.isShown(player) && !npc.inRangeOf(player)) {
                // The player is not in range of the NPC anymore, auto-hide it.
                npc.hide(player, true);
            }
        }
    }
	
}
