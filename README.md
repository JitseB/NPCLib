NPCLib – Basic non-player character library.<br>
[![Release](https://jitpack.io/v/JitseB/npclib.svg)](https://github.com/JitseB/npclib/releases)
[![JDK](https://img.shields.io/badge/Using-Java%208-blue.svg)](http://jdk.java.net/8/)
[![Versions](https://img.shields.io/badge/MC-1.8%20--%201.12-blue.svg)](https://github.com/JitseB/npclib/releases)
=

This is an API made specifically for spigot servers (Minecraft). Current supported versions: **1.8 - 1.12**. Lightweight replacement for Citizens. NPCLib only uses packets instead of registering the entity in the actual Minecraft server.

## Donate

[![PayPal](https://cdn.rawgit.com/twolfson/paypal-github-button/1.0.0/dist/button.svg)](https://paypal.me/JitseB)

Alternatively, you can help the project by starring the repository or telling others about NPCLib :smile:

## Code Example

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

## Usage

Developers are only allowed to use NPCLib if they acknowledge the author of NPCLib (JitseB) with a link to this repository in the description of their resource. NPCLib comes without warranty. I (JitseB) cannot be held responsible for any damage. You may open an [issue](https://github.com/JitseB/npclib/issues) if you are experiencing issues with NPCLib.

You (the developer) can also contact me if you wish to be added to the list below.

Plugin(s) using NPCLib:
 - [PremiumHub](https://www.spigotmc.org/resources/premiumhub-a-new-recode-is-soon-here.32110/) (by Vouchs).

## Copyright

Copyright (c) Jitse Boonstra 2018 All rights reserved.