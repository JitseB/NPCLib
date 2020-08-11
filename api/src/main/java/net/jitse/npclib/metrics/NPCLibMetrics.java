package net.jitse.npclib.metrics;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.NPCLibManager;
import net.jitse.npclib.metrics.bstats.Metrics;

public class NPCLibMetrics {

    private static final int BSTATS_PLUGIN_ID = 7214;
    private static final NPCLib LIBRARY = NPCLibManager.getLibrary();

    public NPCLibMetrics(NPCLib instance) {
        Metrics metrics = new Metrics(instance.getPlugin(), BSTATS_PLUGIN_ID);
        metrics.addCustomChart(new Metrics.SingleLineChart("npcs", () -> LIBRARY.getNPCs().size()));
    }
}
