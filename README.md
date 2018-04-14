NPCLib – Basic non-player character library.<br>
[![Release](https://jitpack.io/v/JitseB/npclib.svg)](https://github.com/JitseB/npclib/releases)
[![JDK](https://img.shields.io/badge/Using-Java%208-blue.svg)](http://jdk.java.net/8/)
[![Versions](https://img.shields.io/badge/MC-1.8%20--%201.12-blue.svg)](https://github.com/JitseB/npclib/releases)
=

This is an API made specifically for spigot servers (Minecraft). Current supported versions: **1.8 - 1.12**.

## Donate

[![PayPal](https://cdn.rawgit.com/twolfson/paypal-github-button/1.0.0/dist/button.svg)](https://paypal.me/JitseB)

Alternatively, you can help the project by starring the repository or telling others about NPCLib :smile:

## Usage

```Java
    // First we define our (global) library variable.
    // This is usually done in the onEnable method.
    NPCLib lib = new NPCLib(plugin);
```

```Java

   // Creating an NPC.
   NPC npc = lib.createNPC(skin, lines);

   // The generate the packets for the NPC.
   npc.create(location);

   // Then *finally* you can show/hide it to/from players.
   npc.show(player);
   npc.hide(player);

   // If you don't use the NPC anymore, destroy the NPC accordingly.
   npc.destroy();
```

Usable events: `NPCSpawnEvent`, `NPCDestroyEvent` and `NPCInteractEvent`.

## Copyright

Copyright (c) Jitse Boonstra 2018 All rights reserved.