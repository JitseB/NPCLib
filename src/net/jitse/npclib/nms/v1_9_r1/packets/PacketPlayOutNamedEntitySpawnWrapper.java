package net.jitse.npclib.nms.v1_9_r1.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.Location;

import java.util.UUID;

public class PacketPlayOutNamedEntitySpawnWrapper {

    public PacketPlayOutNamedEntitySpawn create(UUID uuid, Location location, int entityId) {
        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn();

        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "a", int.class)
                .set(packetPlayOutNamedEntitySpawn, entityId);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "b", UUID.class)
                .set(packetPlayOutNamedEntitySpawn, uuid);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "c", double.class)
                .set(packetPlayOutNamedEntitySpawn, location.getX());
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "d", double.class)
                .set(packetPlayOutNamedEntitySpawn, location.getY());
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "e", double.class)
                .set(packetPlayOutNamedEntitySpawn, location.getZ());
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "f", byte.class)
                .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "g", byte.class)
                .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

        // Todo: This DataWatcher object doesn't register correctly.
        DataWatcher dataWatcher = new DataWatcher(null);
        DataWatcherObject<Byte> object = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.a);
        dataWatcher.register(object, (byte) 127);

        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "h", DataWatcher.class)
                .set(packetPlayOutNamedEntitySpawn, dataWatcher);

        return packetPlayOutNamedEntitySpawn;
    }
}
