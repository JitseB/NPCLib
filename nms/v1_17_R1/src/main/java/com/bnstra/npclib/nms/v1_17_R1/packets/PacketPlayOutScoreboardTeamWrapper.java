/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_17_R1.packets;

import java.util.Collection;
import java.util.Collections;

import com.comphenix.tinyprotocol.Reflection;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutScoreboardTeamWrapper {

    public PacketPlayOutScoreboardTeam[] createRegisterTeam(String name, GameProfile profile) {
        ScoreboardTeam team = new ScoreboardTeam(new Scoreboard(), name);
        team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.b);
        team.setCollisionRule(ScoreboardTeamBase.EnumTeamPush.b);
        team.setAllowFriendlyFire(false);
        team.setCanSeeFriendlyInvisibles(false);
        team.setDisplayName(new ChatComponentText(name));
        PacketPlayOutScoreboardTeam createPacket = PacketPlayOutScoreboardTeam.a(team, true);
        PacketPlayOutScoreboardTeam joinPacket = PacketPlayOutScoreboardTeam.a(team, profile.getName(), PacketPlayOutScoreboardTeam.a.a);
        return new PacketPlayOutScoreboardTeam[]{createPacket, joinPacket};
    }

//    public PacketPlayOutScoreboardTeam createUnregisterTeam(String name) {
//        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();
//
//        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "i", int.class)
//                .set(packetPlayOutScoreboardTeam, 1);
//        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "a", String.class)
//                .set(packetPlayOutScoreboardTeam, name);
//
//        return packetPlayOutScoreboardTeam;
//    }
}
