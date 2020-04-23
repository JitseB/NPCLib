package net.jitse.npclib.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveEventListener extends HandleMoveBase implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        // Only check movement when the player moves from one block to another. The event is called often
        // as it is also called when the pitch or yaw change. This is worth it from a performance view.
        if (to == null || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ())
            handleMove(event.getPlayer());
    }

}
