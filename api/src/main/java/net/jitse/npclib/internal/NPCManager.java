/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.internal;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jitse Boonstra
 */
public final class NPCManager {

    private static Set<NPCBase> npcs = new HashSet<>();

    public static Set<NPCBase> getAllNPCs() {
        return npcs;
    }

    public static Set<NPCBase> getShownToPlayer(Player player) {
        Set<NPCBase> set = Collections.emptySet();
        for (NPCBase npc : getAllNPCs()) {
            if (npc.getShown().contains(player.getUniqueId())) {
                set.add(npc);
            }
        }
        return set;
    }

    public static void add(NPCBase npc) {
        npcs.add(npc);
    }

    public static void remove(NPCBase npc) {
        npcs.remove(npc);
    }

    private NPCManager() {
        throw new SecurityException("You cannot initialize this class.");
    }

}
