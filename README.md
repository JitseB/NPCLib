NPCLib – Basic non-player character library.<br>
[![Release](https://jitpack.io/v/JitseB/npclib.svg)](https://github.com/JitseB/npclib/releases)
[![JDK](https://img.shields.io/badge/Using-Java%208-red.svg)](http://jdk.java.net/8/)
=

This is an API made specifically for spigot servers (Minecraft).

Current versions supported: 1.8 - 1.12.

## Usage

```Java
    // First we define our (global) library variable.
    // This is usually done in the onEnable method.
    NPCLib lib = new NPCLib(plugin);
```

```Java
   // If you want to spawn an NPC, you can generate it like so:
   NPC npc = lib.createNPC(skin, autoHideDistance, lines);

   // Then you need to generate the packets for the NPC like so:
   npc.create(location);

   // Then, finally, you can show/hide it to/from players like so:
   npc.show(player);
   npc.hide(player);

   // If you don't use the NPC anymore, destroy it accordingly:
   npc.destroy();
```

## Copyright

Copyright (c) Jitse Boonstra 2018 All rights reserved.