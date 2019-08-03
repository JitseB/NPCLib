![Banner](https://i.imgur.com/WL6QeUA.png)
NPCLib – Basic non-player character library.<br>
=

This is an API made specifically for spigot servers (Minecraft). Current supported versions: **1.7.10\* - 1.14.4**. Lightweight replacement for Citizens. NPCLib only uses packets instead of registering the entity in the actual Minecraft server.

\*NPCLib has basic support for 1.7.10 and is not actively maintained.

## Documentation

### Creating a library instance

To start using NPCLib, you will first need to create a new instance of the library. Do this in your `onEnable` method, as it will register some event listeners.

```Java
private NPCLib library;

@Override
public void onEnable() {
    this.library = new NPCLib(this);
}
```

Do not create a new NPCLib instance for every class! Instead, create a getter for the library instance:

```Java
public NPCLib getNPCLib() {
    return library;
}
```


### Creating your first NPC

Now you have an instance of the library, it is time to create your first NPC! You may create NPCs after your plugin and NPCLib have enabled.

If you want your NPC to have a custom skin, either [create your own Skin object](#create-skin-object) or fetch the data from [mineskin.org](https://mineskin.org) using the [SkinFetcher](#skin-fetcher) class.

```Java
// Lines parameter: The text you would like to display above the NPC's head (optional).
NPC npc = library.createNPC(List<String> lines);
```

Even though you have created the NPC object now, you will not be able to see it just yet. We first need to need to do some other things. The NPC does not show up to players automatically because NPCLib relies on packets rather than registering the actual entity in the Minecraft server.

```Java
npc.setLocation(Location location);
npc.create(); // Generates the packets.
npc.show(Player player);
```

When you want to hide the NPC from the player **temporary**, you can hide it:

```Java
npc.hide(Player player);
```

When you are done using the NPC, you will have to destroy all NPCs accordingly:

```Java
npc.destroy();
```

When you are done with the NPC, you can destroy it accordingly. This will remove the NPC from the internal registry and hide it from all players that can (still) see it:

```Java
npc.destroy();
```

#### NPC handling

Every NPC is given a unique identifier (`NPC#getId`)that should be used throughout your plugin to identify which NPC has been interacted with.

#### Create skin object

The values used for the Skin object originate from the [Mojang sessionserver](https://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape).
```Java
Skin skin = new Skin(String value, String signature);
```

#### Skin fetcher

The Mojang sessionsserver can only be requested a limited amount of times. Furthermore, the skin data is saved in a profile that is linked to a UUID. We cannot save multiple skins without overly costly solutions. Therefore, saving the values and signatures (of the skin) in a third party database is the ultimate solution. Which is exactly what MineSkin is.

I wrote a little utility [class](https://github.com/JitseB/NPCLib/blob/master/commons/src/main/java/net/jitse/npclib/skin/MineSkinFetcher.java) that fetches the skin data from MineSkin. Here is how you use that class:

```Java
MineSkinFetcher.fetchSkinFromIdAsync(int id, skin -> {
    // Create your NPC.
})
```

The ID in this method is the ID that is in the URL of your MineSkin skin (e.g. https://mineskin.org/725954 has ID 725954).

### Useful events

Events you may want to use are `NPCShowEvent`, `NPCHideEvent` and `NPCInteractEvent`.
