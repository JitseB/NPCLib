package net.jitse.npclib.nms.v1_8_R2.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.jitse.npclib.api.state.NPCAnimation;
import net.minecraft.server.v1_8_R2.PacketPlayOutAnimation;

public class PacketPlayOutAnimationWrapper {

    public PacketPlayOutAnimation create(NPCAnimation npcAnimation, int entityId)  {
        int id = npcAnimation.getId();
        if(id == 3) throw new IllegalArgumentException("Offhand Swing Animations are only available on 1.9 and up.");

        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation();

        Reflection.getField(packetPlayOutAnimation.getClass(), "a", int.class)
                .set(packetPlayOutAnimation, entityId);
        Reflection.getField(packetPlayOutAnimation.getClass(), "b", int.class)
                .set(packetPlayOutAnimation, id);

        return packetPlayOutAnimation;
    }

}
