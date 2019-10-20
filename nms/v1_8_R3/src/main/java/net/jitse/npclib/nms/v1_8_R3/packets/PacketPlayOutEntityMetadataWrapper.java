package net.jitse.npclib.nms.v1_8_R3.packets;

import net.jitse.npclib.api.state.NPCState;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

public class PacketPlayOutEntityMetadataWrapper {

    public PacketPlayOutEntityMetadata create(NPCState[] activateStates, int entityId) {
        DataWatcher dataWatcher = new DataWatcher(null);
        byte masked = NPCState.getMasked(activateStates);
        dataWatcher.a(0, masked);

        return new PacketPlayOutEntityMetadata(entityId, dataWatcher, true);
    }
}
