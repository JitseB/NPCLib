/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.internal;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jitse Boonstra
 */
public final class NPCManager {

    private static Set<SimpleNPC> npcs = new HashSet<>();

    public static Set<SimpleNPC> getAllNPCs() {
        return npcs;
    }

    public static void add(SimpleNPC npc) {
        npcs.add(npc);
    }

    public static void remove(SimpleNPC npc) {
        npcs.remove(npc);
    }

    private NPCManager() {
        throw new SecurityException("You cannot initialize this class.");
    }

}
