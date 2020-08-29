package net.jitse.npclib.internal;

import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Player;

import java.util.*;

public class NPCManager {

    // A map with the player's UUID and all NPCs that are auto hidden
    private final Map<UUID, Set<NPC>> hidden = new HashMap<>();

    public void setHidden(Player player, NPC npc, boolean hide) {
        if (hidden.containsKey(player.getUniqueId())) {
            Set<NPC> hiddenNPCs = hidden.get(player.getUniqueId());
            if (hide) hiddenNPCs.add(npc);
            else hidden.remove(npc);
            hidden.replace(player.getUniqueId(), hiddenNPCs);
        } else {
            Set<NPC> hiddenNPCs = new HashSet<>();
            if (hide) hiddenNPCs.add(npc);
            else hidden.remove(npc);
            hidden.put(player.getUniqueId(), hiddenNPCs);
        }
    }

    public boolean isHidden(Player player, NPC npc) {
        if (!hidden.containsKey(player.getUniqueId())) return false;

        for (NPC hiddenNPC : hidden.get(player.getUniqueId())) {
            if (hiddenNPC.equals(npc)) return true;
        }

        return false;
    }
}
