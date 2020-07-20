/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.hologram;

import com.comphenix.tinyprotocol.Reflection;
import net.jitse.npclib.internal.MinecraftVersion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Hologram {

    private static final double DELTA = 0.3;

    // Classes:
    private static final Class<?> CHAT_COMPONENT_TEXT_CLASS = Reflection.getMinecraftClass("ChatComponentText");
    private static final Class<?> CHAT_BASE_COMPONENT_CLASS = Reflection.getMinecraftClass("IChatBaseComponent");
    private static final Class<?> ENTITY_ARMOR_STAND_CLASS = Reflection.getMinecraftClass("EntityArmorStand");
    private static final Class<?> ENTITY_LIVING_CLASS = Reflection.getMinecraftClass("EntityLiving");
    private static final Class<?> ENTITY_CLASS = Reflection.getMinecraftClass("Entity");
    private static final Class<?> CRAFT_WORLD_CLASS = Reflection.getCraftBukkitClass("CraftWorld");
    private static final Class<?> CRAFT_PLAYER_CLASS = Reflection.getCraftBukkitClass("entity.CraftPlayer");

    private static final Class<?> BUKKIT_ENTITY_ARMOR_STAND_CLASS = Reflection.getClass("org.bukkit.entity.ArmorStand");

    private static final Class<?> PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS = Reflection.getMinecraftClass(
            "PacketPlayOutSpawnEntityLiving");
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_DESTROY_CLASS = Reflection.getMinecraftClass(
            "PacketPlayOutEntityDestroy");
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_METADATA_CLASS = Reflection.getMinecraftClass(
            "PacketPlayOutEntityMetadata");
    private static final Class<?> DATAWATCHER_CLASS = Reflection.getMinecraftClass("DataWatcher");
    private static final Class<?> ENTITY_PLAYER_CLASS = Reflection.getMinecraftClass("EntityPlayer");
    private static final Class<?> PLAYER_CONNECTION_CLASS = Reflection.getMinecraftClass("PlayerConnection");
    private static final Class<?> PACKET_CLASS = Reflection.getMinecraftClass("Packet");

    // Constructors:
    private static final Reflection.ConstructorInvoker CHAT_COMPONENT_TEXT_CONSTRUCTOR = Reflection
            .getConstructor(CHAT_COMPONENT_TEXT_CLASS, String.class);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, ENTITY_LIVING_CLASS);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_ENTITY_DESTROY_CLASS, int[].class);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, DATAWATCHER_CLASS, boolean.class);

    // Fields:
    private static final Reflection.FieldAccessor<?> PLAYER_CONNECTION_FIELD = Reflection.getField(ENTITY_PLAYER_CLASS,
            "playerConnection", PLAYER_CONNECTION_CLASS);

    // Methods:
    private static final Reflection.MethodInvoker SET_LOCATION_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setLocation", double.class, double.class, double.class, float.class, float.class);
    private static final Reflection.MethodInvoker SET_SMALL_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setSmall", boolean.class);
    private static final Reflection.MethodInvoker SET_INVISIBLE_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setInvisible", boolean.class);
    private static final Reflection.MethodInvoker SET_BASE_PLATE_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setBasePlate", boolean.class);
    private static final Reflection.MethodInvoker SET_ARMS_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setArms", boolean.class);

    private static final Reflection.MethodInvoker GET_BUKKIT_ENTITY = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "getBukkitEntity");

    private static final Reflection.MethodInvoker PLAYER_GET_HANDLE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLASS,
            "getHandle");
    private static final Reflection.MethodInvoker SEND_PACKET_METHOD = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
            "sendPacket", PACKET_CLASS);
    private static final Reflection.MethodInvoker GET_ID_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "getId");
    private static final Reflection.MethodInvoker GET_DATAWATCHER_METHOD = Reflection.getMethod(ENTITY_CLASS,
            "getDataWatcher");

    private final List<Object> armorStands = new ArrayList<>();
    private final List<Object> showPackets = new ArrayList<>();
    private final List<Object> hidePackets = new ArrayList<>();
    private final List<Object> metaPackets = new ArrayList<>();

    private final MinecraftVersion version;
    private final Location start;
    private final Object worldServer;

    private List<String> text;

    public Hologram(MinecraftVersion version, Location location, List<String> text) {
        this.version = version;
        this.start = location;
        this.text = text;

        this.worldServer = Reflection.getMethod(CRAFT_WORLD_CLASS, "getHandle").invoke(location.getWorld());

        createPackets();
    }

    private void createPackets() {
        Reflection.MethodInvoker gravityMethod = (version.isAboveOrEqual(MinecraftVersion.V1_10_R1) ?
                Reflection.getMethod(ENTITY_CLASS, "setNoGravity", boolean.class) :
                Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS, "setGravity", boolean.class));

        Reflection.MethodInvoker SET_MARKER_METHOD = (version.isAboveOrEqual(MinecraftVersion.V1_8_R3) ?
                Reflection.getMethod(BUKKIT_ENTITY_ARMOR_STAND_CLASS,
                        "setMarker", boolean.class) : null);

        Reflection.MethodInvoker customNameMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomName",
                version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ? CHAT_BASE_COMPONENT_CLASS : String.class);

        Reflection.MethodInvoker customNameVisibilityMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomNameVisible", boolean.class);

        Location location = start.clone().add(0, (DELTA * text.size()) + (SET_MARKER_METHOD != null ? 1f : 0f), 0); // markers drop the armor stand's nametag by around 1 block
        Class<?> worldClass = worldServer.getClass().getSuperclass();

        if (start.getWorld().getEnvironment() != World.Environment.NORMAL) {
            worldClass = worldClass.getSuperclass();
        }

//        Reflection.ConstructorInvoker entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
//                Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class, double.class) :
//                Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        // Replacement for issue #59
        Reflection.ConstructorInvoker entityArmorStandConstructor = null;
        try {
            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class, double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        } catch (IllegalStateException exception) {
            worldClass = worldClass.getSuperclass();

            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class, double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        }
        // end #59

        for (String line : text) {
            Object entityArmorStand = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    entityArmorStandConstructor.invoke(worldServer, location.getX(), location.getY(), location.getZ()) :
                    entityArmorStandConstructor.invoke(worldServer));

            if (!version.isAboveOrEqual(MinecraftVersion.V1_14_R1)) {
                SET_LOCATION_METHOD.invoke(entityArmorStand, location.getX(), location.getY(), location.getZ(), 0, 0);
            }

            customNameMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ?
                    CHAT_COMPONENT_TEXT_CONSTRUCTOR.invoke(line) : line);
            customNameVisibilityMethod.invoke(entityArmorStand, true);
            gravityMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_9_R2));
            SET_SMALL_METHOD.invoke(entityArmorStand, true);
            SET_INVISIBLE_METHOD.invoke(entityArmorStand, true);
            SET_BASE_PLATE_METHOD.invoke(entityArmorStand, false);
            SET_ARMS_METHOD.invoke(entityArmorStand, false);

            if (SET_MARKER_METHOD != null) { // setMarker isn't a method in 1.8_R2, so still check if it exists in the first place.
                Object bukkitEntity = GET_BUKKIT_ENTITY.invoke(entityArmorStand); // if it does, grab the Bukkit Entity
                ArmorStand as = (ArmorStand)bukkitEntity; // reflection wasn't working here for some reason- just using a regular ArmorStand object since it's not version-dependent.
                as.setMarker(true); // set the marker state
            }

            armorStands.add(entityArmorStand);

            // Create and add the associated show and hide packets.
            showPackets.add(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR.invoke(entityArmorStand));
            hidePackets.add(PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR
                    .invoke(new int[]{(int) GET_ID_METHOD.invoke(entityArmorStand)}));

            // For 1.15 R1 and up.
            metaPackets.add(PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.invoke(
                    GET_ID_METHOD.invoke(entityArmorStand),
                    GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                    true));

            location.subtract(0, DELTA, 0);
        }
    }

    public List<Object> getUpdatePackets(List<String> text) {
        List<Object> updatePackets = new ArrayList<>();

        if (this.text.size() != text.size()) {
            throw new IllegalArgumentException("When updating the text, the old and new text should have the same amount of lines");
        }

        Reflection.MethodInvoker customNameMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomName",
                version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ? CHAT_BASE_COMPONENT_CLASS : String.class);

        for (int i = 0; i < text.size(); i++) {
            Object entityArmorStand = armorStands.get(i);
            String oldLine = this.text.get(i);
            String newLine = text.get(i);

            customNameMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ?
                    CHAT_COMPONENT_TEXT_CONSTRUCTOR.invoke(newLine) : newLine); // Update the DataWatcher object.
            showPackets.set(i, PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR.invoke(entityArmorStand));

            if (newLine.isEmpty() && !oldLine.isEmpty()) {
                updatePackets.add(hidePackets.get(i));
            } else if (!newLine.isEmpty() && oldLine.isEmpty()) {
                updatePackets.add(showPackets.get(i));
            } else if (!oldLine.equals(newLine)) {
                // Update the line for all players using a Metadata packet.
                updatePackets.add(PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.invoke(
                        GET_ID_METHOD.invoke(entityArmorStand),
                        GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                        true
                ));
            }
        }

        this.text = text;

        return updatePackets;
    }

    public void update(Player player, List<Object> updatePackets) {
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));

        for (Object packet : updatePackets) {
            SEND_PACKET_METHOD.invoke(playerConnection, packet);
        }
    }

    public void show(Player player) {
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));

        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to spawn the line.
            SEND_PACKET_METHOD.invoke(playerConnection, showPackets.get(i));
            if (version.isAboveOrEqual(MinecraftVersion.V1_15_R1)) {
                SEND_PACKET_METHOD.invoke(playerConnection, metaPackets.get(i));
            }
        }
    }

    public void hide(Player player) {
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));

        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to hide the line (as it was never spawned).
            SEND_PACKET_METHOD.invoke(playerConnection, hidePackets.get(i));
        }
    }
}
