/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api.events;

import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Jitse Boonstra
 */
public class NPCInteractEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final ClickType clickType;
    private final NPC npc;

    public NPCInteractEvent(Player player, ClickType clickType, NPC npc) {
        this.player = player;
        this.clickType = clickType;
        this.npc = npc;
    }

    public Player getWhoClicked() {
        return this.player;
    }

    public ClickType getClickType() {
        return this.clickType;
    }

    public NPC getNPC() {
        return this.npc;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum ClickType {
        LEFT_CLICK, RIGHT_CLICK
    }
}
