/*
 * Copyright (c) Jitse Boonstra 2018 All rights reserved.
 */

package net.jitse.npclib.nms.holograms;

import com.comphenix.tinyprotocol.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jitse Boonstra
 */
public class Hologram {

    private final double delta = 0.3;

    private List<Object> armorStands = new ArrayList<>();
    private Set<Object> spawnPackets = new HashSet<>();
    private Set<Object> destroyPackets = new HashSet<>();

    // Classes:
    private final Class<?> entityArmorStandClazz = Reflection.getMinecraftClass("EntityArmorStand");
    private final Class<?> entityLivingClazz = Reflection.getMinecraftClass("EntityLiving");
    private final Class<?> entityClazz = Reflection.getMinecraftClass("Entity");
    private final Class<?> craftWorldClazz = Reflection.getCraftBukkitClass("CraftWorld");
    private final Class<?> craftPlayerClazz = Reflection.getCraftBukkitClass("entity.CraftPlayer");
    private final Class<?> packetPlayOutSpawnEntityLivingClazz = Reflection.getMinecraftClass(
            "PacketPlayOutSpawnEntityLiving");
    private final Class<?> packetPlayOutEntityDestroyClazz = Reflection.getMinecraftClass(
            "PacketPlayOutEntityDestroy");
    private final Class<?> entityPlayerClazz = Reflection.getMinecraftClass("EntityPlayer");
    private final Class<?> playerConnectionClazz = Reflection.getMinecraftClass("PlayerConnection");
    private final Class<?> packetClazz = Reflection.getMinecraftClass("Packet");

    // Constructors:
    private final Reflection.ConstructorInvoker packetPlayOutSpawnEntityLivingConstructor = Reflection
            .getConstructor(packetPlayOutSpawnEntityLivingClazz, entityLivingClazz);
    private final Reflection.ConstructorInvoker packetPlayOutEntityDestroyConstructor = Reflection
            .getConstructor(packetPlayOutEntityDestroyClazz, int[].class);

    // Fields:
    private final Reflection.FieldAccessor playerConnectionField = Reflection.getField(entityPlayerClazz,
            "playerConnection", playerConnectionClazz);

    // Methods:
    private final Reflection.MethodInvoker setLocationMethod = Reflection.getMethod(entityArmorStandClazz,
            "setLocation", double.class, double.class, double.class, float.class, float.class);
    private final Reflection.MethodInvoker setCustomNameMethod = Reflection.getMethod(entityArmorStandClazz,
            "setCustomName", String.class);
    private final Reflection.MethodInvoker setCustomNameVisibleMethod = Reflection.getMethod(entityArmorStandClazz,
            "setCustomNameVisible", boolean.class);
    private final Reflection.MethodInvoker setSmallMethod = Reflection.getMethod(entityArmorStandClazz,
            "setSmall", boolean.class);
    private final Reflection.MethodInvoker setInvisibleMethod = Reflection.getMethod(entityArmorStandClazz,
            "setInvisible", boolean.class);
    private final Reflection.MethodInvoker setBasePlateMethod = Reflection.getMethod(entityArmorStandClazz,
            "setBasePlate", boolean.class);
    private final Reflection.MethodInvoker setArmsMethod = Reflection.getMethod(entityArmorStandClazz,
            "setArms", boolean.class);
    private final Reflection.MethodInvoker playerGetHandleMethod = Reflection.getMethod(craftPlayerClazz,
            "getHandle");
    private final Reflection.MethodInvoker sendPacketMethod = Reflection.getMethod(playerConnectionClazz,
            "sendPacket", packetClazz);
    private final Reflection.MethodInvoker getIdMethod = Reflection.getMethod(entityArmorStandClazz,
            "getId");

    private final Location start;
    private final List<String> lines;
    private final Object worldServer;

    public Hologram(Location location, List<String> lines) {
        this.start = location;
        this.lines = lines;

        this.worldServer = Reflection.getMethod(craftWorldClazz, "getHandle")
                .invoke(craftWorldClazz.cast(location.getWorld()));

    }

    public void generatePackets(boolean above1_9_r2) {
        Reflection.MethodInvoker gravityMethod = (above1_9_r2 ? Reflection.getMethod(entityClazz,
                "setNoGravity", boolean.class) : Reflection.getMethod(entityArmorStandClazz,
                "setGravity", boolean.class));

        Location location = start.clone().add(0, delta * lines.size(), 0);

        Reflection.ConstructorInvoker entityArmorStandConstructor = Reflection
                .getConstructor(entityArmorStandClazz, worldServer.getClass().getSuperclass());

        for (String line : lines) {
            Object entityArmorStand = entityArmorStandConstructor.invoke(worldServer);

            setLocationMethod.invoke(entityArmorStand, location.getX(), location.getY(), location.getZ(), 0, 0);
            setCustomNameMethod.invoke(entityArmorStand, line);
            setCustomNameVisibleMethod.invoke(entityArmorStand, true);
            gravityMethod.invoke(entityArmorStand, (above1_9_r2 ? true : false));
            setSmallMethod.invoke(entityArmorStand, true);
            setInvisibleMethod.invoke(entityArmorStand, true);
            setBasePlateMethod.invoke(entityArmorStand, false);
            setArmsMethod.invoke(entityArmorStand, false);

            location.subtract(0, delta, 0);

            if (line.isEmpty()) {
                continue;
            }

            armorStands.add(entityArmorStand);

            Object spawnPacket = packetPlayOutSpawnEntityLivingConstructor.invoke(entityArmorStand);
            spawnPackets.add(spawnPacket);

            Object destroyPacket = packetPlayOutEntityDestroyConstructor
                    .invoke(new int[]{(int) getIdMethod.invoke(entityArmorStand)});
            destroyPackets.add(destroyPacket);
        }
    }

    public void spawn(Player player) {
        Object playerConnection = playerConnectionField.get(playerGetHandleMethod
                .invoke(craftPlayerClazz.cast(player)));

        for (Object packet : spawnPackets) {
            sendPacketMethod.invoke(playerConnection, packet);
        }
    }

    public void destroy(Player player) {
        Object playerConnection = playerConnectionField.get(playerGetHandleMethod
                .invoke(craftPlayerClazz.cast(player)));

        for (Object packet : destroyPackets) {
            sendPacketMethod.invoke(playerConnection, packet);
        }
    }
}
