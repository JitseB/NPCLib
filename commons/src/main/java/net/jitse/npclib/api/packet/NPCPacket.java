/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api.packet;

import org.bukkit.entity.Player;

/**
 * @author Jitse Boonstra
 */
public abstract class NPCPacket {

    private Object packet;

    public void send(Player player) {

    }

    public abstract void create();
}
