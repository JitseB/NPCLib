package net.jitse.npclib.example;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.skin.MineSkinFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * @author Jitse Boonstra
 */
public class NPCLibTest extends JavaPlugin {

    /*
        Another basic example of spawning an NPC with NPCLib.
        This class will spawn an NPC with a custom skin on un-sneaking at the player's location.
     */

    private NPCLib npclib;

    @Override
    public void onEnable() {
        this.npclib = new NPCLib(this);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            return;
        }

        int skinId = 188100; // Read DOCUMENTATION.md on how and where to get this number.
        MineSkinFetcher.fetchSkinFromIdAsync(skinId, skin -> {
            NPC npc = npclib.createNPC(Arrays.asList(ChatColor.WHITE + "Hi there (#3)");
            npc.setLocation(event.getPlayer().getLocation());
            npc.setSkin(skin);
            ids.add(npc.getId());
            npc.create();
            // The SkinFetcher fetches the skin async, you can only show the NPC to the player sync.
            Bukkit.getScheduler().runTask(this, () -> npc.show(event.getPlayer()));
        });
    }
}
