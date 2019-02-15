/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api.wrapper;

import com.comphenix.tinyprotocol.Reflection;
import com.google.common.collect.ForwardingMultimap;
import net.jitse.npclib.skin.Skin;
import org.bukkit.Bukkit;

import java.util.UUID;

public class GameProfileWrapper {

    // Written because of issue#10 (https://github.com/JitseB/NPCLib/issues/10).
    // This class acts as an NMS reflection wrapper for the GameProfileWrapper class.

    // TODO: Add this class to the v1_7_R4 module of NPCLib.

    private final boolean is1_7 = Bukkit.getBukkitVersion().contains("1.7");
    private final Class<?> gameProfileClazz = Reflection.getClass((is1_7 ? "net.minecraft.util." : "") + "com.mojang.authlib.GameProfile");

    Object gameProfile;

    public GameProfileWrapper(UUID uuid, String name) {
        // Only need to check if the version is 1.7, as NPCLib doesn't support any version below this version.
        this.gameProfile = Reflection.getConstructor(gameProfileClazz, UUID.class, String.class).invoke(uuid, name);
    }

    public void addSkin(Skin skin) {
        // Create a new property with the skin data.
        Class<?> propertyClazz = Reflection.getClass((is1_7 ? "net.minecraft.util." : "") + "com.mojang.authlib.properties.Property");
        Object property = Reflection.getConstructor(propertyClazz,
                String.class, String.class, String.class).invoke("textures", skin.getValue(), skin.getSignature());

        // Get the property map from the GameProfileWrapper object.
        Class<?> propertyMapClazz = Reflection.getClass((is1_7 ? "net.minecraft.util." : "") + "com.mojang.authlib.properties.PropertyMap");
        Reflection.FieldAccessor propertyMapGetter = Reflection.getField(gameProfileClazz, "properties",
                propertyMapClazz);
        Object propertyMap = propertyMapGetter.get(gameProfile);

        // Add our new property to the property map.
        Reflection.getMethod(ForwardingMultimap.class, "put", Object.class, Object.class)
                .invoke(propertyMap, "textures", property);

        // Finally set the property map back in the GameProfileWrapper object.
        propertyMapGetter.set(gameProfile, propertyMap);
    }

    public Object getGameProfile() {
        return gameProfile;
    }
}
