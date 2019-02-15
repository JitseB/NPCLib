![Banner](https://i.imgur.com/WL6QeUA.png)
NPCLib – Basic non-player character library.<br>
[![Release](https://jitpack.io/v/JitseB/NPCLib.svg)](https://github.com/JitseB/NPCLib/releases)
[![Build Status](https://travis-ci.com/JitseB/NPCLib.svg?branch=master)](https://travis-ci.com/JitseB/NPCLib)
[![JDK](https://img.shields.io/badge/Using-Java%208-blue.svg)](http://jdk.java.net/8/)
[![Versions](https://img.shields.io/badge/MC-1.7.10%20--%201.13.2-blue.svg)](https://github.com/JitseB/NPCLib/releases)
[![Thread](https://img.shields.io/badge/SpigotMC-Resource-orange.svg)](https://www.spigotmc.org/resources/npclib.55884/)
[![Thread](https://img.shields.io/badge/SpigotMC-Thread-orange.svg)](https://www.spigotmc.org/threads/npclib-–-basic-non-player-character-library.314460/)
=

This is an API made specifically for spigot servers (Minecraft). Current supported versions: **1.7.10\* - 1.13.2**. Lightweight replacement for Citizens. NPCLib only uses packets instead of registering the entity in the actual Minecraft server.

\*NPCLib has basic support for 1.7.10, as it not currently support multi-line text for this version (yet).

### Preview (click to play video)
[![YouTube Video](http://img.youtube.com/vi/LqwdqIxPIvE/0.jpg)](http://www.youtube.com/watch?v=LqwdqIxPIvE "NPCLib – Basic non-player character library (Minecraft).")

## Donate

[![PayPal](https://cdn.rawgit.com/twolfson/paypal-github-button/1.0.0/dist/button.svg)](https://paypal.me/jitseboonstra)

Alternatively, you can help the project by starring the repository or telling others about NPCLib. :smile:

## Roadmap

- :heavy_check_mark: Spawn and destroy NPC (version 1.8 - latest).
- :heavy_check_mark: Autohide NPC when out of range.
- :heavy_check_mark: Add support for 1.7.10 (1.7 R4).
- :construction: Option to rotate head to player (when nearby).
- :construction: Add support for animated text (update-able holograms).
- :x: Give NPC armor and items in hand.
- :x: Multi-line text support for 1.7.10 (1.7 R4).

### Roadmad Legend
:heavy_check_mark: Feature is fully implemented and functional. <br>
:construction: Feature is still in development (or experimental). <br>
:x: Development of feature has yet to be started. <br>

## Developers

### Usage

It is recommended to shade `npclib-api-v*.jar` into your plugin.
Alternatively, you can put `npclib-plugin-v*.jar` under your `plugins` folder. By doing this, you no longer need to shade the API JAR. Though, do not forget to add `NPCLib` as a dependency in your `plugin.yml`!


[Click here](https://github.com/JitseB/NPCLib/releases/latest) to download the latest release.

```Java
    // First we define our (global) library variable.
    // This is usually done in the onEnable method.
    NPCLib lib = new NPCLib(plugin);
```

```Java

   // Creating a new NPC instance.
   // MC 1.7.10 (1.7 R4) only supports single-line text.
   NPC npc = lib.createNPC(skin, lines);

   // Then let the library generate the necessary packets.
   npc.create(location);

   // You are all set! You can now show/hide it to/from players.
   npc.show(player);
   npc.hide(player);

   // If you do not wish to use the NPC anymore, destroy it accordingly.
   npc.destroy();
```

### Events

Events you may want to use are `NPCSpawnEvent`, `NPCDestroyEvent` and `NPCInteractEvent`.

### Building your own version

1. [Download](https://github.com/JitseB/NPCLib/archive/master.zip) or clone this repository.
2. Build the plugin using `sh build.sh`. Alternatively, you can build the API JAR manually using `mvn clean install`.

You can build the plugin using `mvn clean install -pPlugin`.

## License and plugins using NPCLib

NPCLib is licensed under the [MIT license](https://github.com/JitseB/NPCLib/blob/master/LICENSE.md).
Developers are free to use NPCLib for both private and commercial use. However, it would be nice to acknowledge me.

You (the developer) can also contact me if you wish to be added to the list below.

Plugin(s) using NPCLib:
 - [PremiumHub](https://www.spigotmc.org/resources/premiumhub-a-new-recode-is-soon-here.32110/) (by Vouchs).

## Acknowledgement

We thank all those who have [contributed](https://github.com/JitseB/NPCLib/graphs/contributors) to the creation of what NPCLib is today.

## Copyright

Copyright (c) Jitse Boonstra 2018 All rights reserved.
