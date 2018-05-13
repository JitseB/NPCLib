/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api;

import org.bukkit.entity.Player;

/**
 * @author Jitse Boonstra
 */
interface PacketHandler {

    void createPackets();

    void sendShowPackets(Player player);

    void sendHidePackets(Player player, boolean scheduler);
}
