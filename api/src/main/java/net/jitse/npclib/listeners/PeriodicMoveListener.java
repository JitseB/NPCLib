package net.jitse.npclib.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import net.jitse.npclib.NPCLib;

public class PeriodicMoveListener extends HandleMoveBase implements Listener {

	private final NPCLib instance;
	private final long updateInterval;

	private final HashMap<UUID, BukkitTask> tasks = new HashMap<>();

	public PeriodicMoveListener(NPCLib instance, long updateInterval) {
		this.instance = instance;
		this.updateInterval = updateInterval;
	}

	private void startTask(UUID uuid) {
		// purposefully using UUIDs and not holding player references
		tasks.put(uuid, Bukkit.getScheduler().runTaskTimer(instance.getPlugin(), () -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) { // safety check
				handleMove(player);
			}
		}, 1L, updateInterval));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		startTask(evt.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt) {
		BukkitTask task = tasks.remove(evt.getPlayer().getUniqueId());
		if (task != null) {
			task.cancel();
		}
	}

}
