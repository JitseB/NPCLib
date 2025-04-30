package com.bnstra.npclib.metrics;

import com.bnstra.npclib.NPCLib;
import com.bnstra.npclib.internal.NPCManager;
import org.bstats.bukkit.Metrics;

public class NPCLibMetrics {

    private static final int BSTATS_PLUGIN_ID = 7214;

    public NPCLibMetrics(NPCLib instance) {
        Metrics metrics = new Metrics(instance.getPlugin(), BSTATS_PLUGIN_ID);
        metrics.addCustomChart(new Metrics.SingleLineChart("npcs", () -> NPCManager.getAllNPCs().size()));
    }
}
