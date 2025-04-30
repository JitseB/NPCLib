/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_20_R2.packets;

import com.mojang.authlib.GameProfile;
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
        team.a(ScoreboardTeamBase.EnumNameTagVisibility.b);
        team.a(ScoreboardTeamBase.EnumTeamPush.b);
        team.a(false);
        team.b(false);
        team.a(IChatBaseComponent.b(name));
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
