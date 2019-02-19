![Banner](https://i.imgur.com/WL6QeUA.png)
NPCLib – Basic non-player character library.<br>
=

This is an API made specifically for spigot servers (Minecraft). Current supported versions: **1.7.10\* - 1.13.2**. Lightweight replacement for Citizens. NPCLib only uses packets instead of registering the entity in the actual Minecraft server.

\*NPCLib has basic support for 1.7.10, as it not currently support multi-line text for this version (yet).

## Credits

### [TinyProtocol](https://github.com/aadnk/ProtocolLib/tree/master/modules/TinyProtocol) by Kristian Stangeland
I used this module of ProtocolLib to intercept packets sent by players whilst interacting with NPCs. After retrieving the data from these packets, I can call a new [NPCInteractEvent](https://github.com/JitseB/NPCLib/blob/master/commons/src/main/java/net/jitse/npclib/events/NPCInteractEvent.java).

### [MineSkin](https://mineskin.org) by Haylee Schäfer
I integrated MineSkin into NPCLib as an easy way for developers to give NPCs a custom skin without buying multiple Minecraft accounts or saving the skin data themselves.