package com.bnstra.npclib.nms.v1_17_R1.packets;

import java.util.Collection;

import com.bnstra.npclib.api.state.NPCState;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityPose;

public class PacketPlayOutEntityMetadataWrapper {

    public PacketPlayOutEntityMetadata create(Collection<NPCState> activateStates, int entityId) {
        DataWatcher dataWatcher = new DataWatcher(null);
        byte masked = NPCState.getMasked(activateStates);

        // Since 1.17 R1 the skin overlay data was moved to index 17
        dataWatcher.register(new DataWatcherObject<>(17, DataWatcherRegistry.a), (byte) 127);
        dataWatcher.register(new DataWatcherObject<EntityPose>(6, DataWatcherRegistry.s), getMaskedPose(activateStates));
        dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), masked);
        
        return new PacketPlayOutEntityMetadata(entityId, dataWatcher, true);
    }
    
    private EntityPose getMaskedPose(Collection<NPCState> states) {
    	return states.contains(NPCState.CROUCHED) ? EntityPose.f : EntityPose.a;
    }
}
