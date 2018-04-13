package net.jitse.npclib;

import net.jitse.npclib.api.NPC;

import java.util.HashSet;
import java.util.Set;

public class NPCManager {

    private static Set<NPC> npcs = new HashSet<>();

    public static Set<NPC> getAllNPCs() {
        return npcs;
    }

    public static void add(NPC npc) {
        npcs.add(npc);
    }

    public static void remove(NPC npc) {
        npcs.remove(npc);
    }
}
