/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api.events;

import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Jitse Boonstra
 */
public class NPCShowEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    private final NPC npc;
    private final Player player;
    private final boolean automatic;

    public NPCShowEvent(NPC npc, Player player, boolean automatic) {
        this.npc = npc;
        this.player = player;
        this.automatic = automatic;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public NPC getNPC() {
        return npc;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * @return Value on whether the spawn was triggered automatically.
     */
    public boolean isAutomatic() {
        return automatic;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
