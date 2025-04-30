package com.bnstra.npclib.nms.v1_20_R4.packets;

import java.util.Arrays;
import java.util.Collection;

import com.bnstra.npclib.api.state.NPCState;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityPose;

public class PacketPlayOutEntityMetadataWrapper {

    public PacketPlayOutEntityMetadata create(Collection<NPCState> activateStates, int entityId) {
        byte masked = NPCState.getMasked(activateStates);
        return new PacketPlayOutEntityMetadata(entityId,
        Arrays.asList(
                DataWatcher.c.a(new DataWatcherObject<>(17, DataWatcherRegistry.a), (byte) 127),
                DataWatcher.c.a(new DataWatcherObject<EntityPose>(6, DataWatcherRegistry.w),
                    getMaskedPose(activateStates)),
                DataWatcher.c.a(new DataWatcherObject<>(0, DataWatcherRegistry.a), masked)
                )
        );
    }
    
    private EntityPose getMaskedPose(Collection<NPCState> states) {
    	return states.contains(NPCState.CROUCHED) ? EntityPose.f : EntityPose.a;
    }
}
