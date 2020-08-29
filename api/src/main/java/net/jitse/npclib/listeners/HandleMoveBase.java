package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCLibManager;
import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Player;

public class HandleMoveBase {

    void handleMove(Player player) {
        for (NPC npc : NPCLibManager.getLibrary().getNPCs()) {
            if (!npc.isShown(player)) {
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
