package net.jitse.npclib.nms.v1_11_R1.packets;

import net.jitse.npclib.api.state.NPCState;
import net.minecraft.server.v1_11_R1.DataWatcher;
import net.minecraft.server.v1_11_R1.DataWatcherObject;
import net.minecraft.server.v1_11_R1.DataWatcherRegistry;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityMetadata;

public class PacketPlayOutEntityMetadataWrapper {

    public PacketPlayOutEntityMetadata create(NPCState[] activateStates, int entityId) {
        DataWatcher dataWatcher = new DataWatcher(null);
        byte masked = NPCState.getMasked(activateStates);
        dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), masked);

        return new PacketPlayOutEntityMetadata(entityId, dataWatcher, true);
    }
}
