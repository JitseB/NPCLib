![Banner](https://i.imgur.com/9BgeVVy.png)
NPCLib – Basic non-player character library.<br>
[![Release](https://jitpack.io/v/JitseB/NPCLib.svg)](https://github.com/JitseB/NPCLib/releases)
[![Build Status](https://travis-ci.com/JitseB/NPCLib.svg?branch=master)](https://travis-ci.com/JitseB/NPCLib)
[![JDK](https://img.shields.io/badge/Using-Java%208-blue.svg)](http://jdk.java.net/8/)
[![Versions](https://img.shields.io/badge/MC-1.7.10%20--%201.14.4-blue.svg)](https://github.com/JitseB/NPCLib/releases)
[![Resource](https://img.shields.io/badge/SpigotMC-Resource-orange.svg)](https://www.spigotmc.org/resources/npclib.55884/)
=

This is an API made specifically for spigot servers (Minecraft). Current supported versions: **1.8 - 1.14.4**\*. Lightweight replacement for Citizens. NPCLib only uses packets instead of registering the entity in the actual Minecraft server.

\*You can find a version of NPCLib with basic support for 1.7.10 on the [legacy branch](https://github.com/JitseB/NPCLib/tree/legacy). This branch is not actively maintained as the master branch. This version does not support multi-line text.

### Preview (click to play video)
[![YouTube Video](http://img.youtube.com/vi/LqwdqIxPIvE/0.jpg)](http://www.youtube.com/watch?v=LqwdqIxPIvE "NPCLib – Basic non-player character library (Minecraft).")

## Donate

[![PayPal](https://cdn.rawgit.com/twolfson/paypal-github-button/1.0.0/dist/button.svg)](https://paypal.me/jitseboonstra)

Alternatively, you can help the project by starring the repository or telling others about NPCLib. :smile:

## Developers

### Usage

There are multiple ways you can make use of NPCLib.

1. The first option is to shade `npclib-plugin.jar` in to your plugin.
2. The second option is to put `npclib-plugin.jar` under your `plugins` folder. By doing this, you no longer need to shade the API JAR. Though, do not forget to add `NPCLib` as a dependency in your `plugin.yml`!
3. The third option (and the one I recommend most) is to shade the library using Maven. I recently added NPCLib to the OSSRH (OSS Repository Hosting) which allows you to easily import NPCLib into your project.

#### Maven repository
```xml
<repositories>
    <repository>
        <id>ossrh</id>
        <url>https://oss.sonatype.org/content/groups/public/</url>
    </repository>
</repositories>
```

#### Maven dependency

If you have NPCLib under your `plugins` folder, you may use the following:
```xml
<dependencies>
    <dependency>
        <groupId>net.jitse</groupId>
        <artifactId>npclib-api</artifactId>
        <version>2.3-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```
If you do not want to have NPCLib in your `plugins` folder, you need to use the `npclib-plugin` artifact and [shade it](https://maven.apache.org/plugins/maven-shade-plugin/) accordingly.


[Click here](https://github.com/JitseB/NPCLib/releases/latest) to download the latest release.

**[Click here](https://github.com/JitseB/NPCLib/blob/master/DOCUMENTATION.md) for an elaborate explanation on how to use NPCLib in your next project.**

### Building your own version

1. [Download](https://github.com/JitseB/NPCLib/archive/master.zip) or clone this repository.
2. You can build the project using `mvn clean install`.

The API JAR will be under `/api/target/` and the plugin JAR (which includes all necessary NMS code) will be under `/plugins/target/`.

## License and plugins using NPCLib

NPCLib is licensed under the [MIT license](https://github.com/JitseB/NPCLib/blob/master/LICENSE.md).
Developers are free to use NPCLib for both private and commercial use. However, it would be nice to acknowledge me.

You (the developer) can also contact me if you wish to be added to the list below.

Plugin(s) using NPCLib:
 - [PremiumHub](https://www.spigotmc.org/resources/premiumhub-a-new-recode-is-soon-here.32110/) (by Vouchs).

## Acknowledgement

We thank all those who have [contributed](https://github.com/JitseB/NPCLib/graphs/contributors) to the creation of what NPCLib is today.

Please view the credits [here](https://github.com/JitseB/NPCLib/blob/master/CREDITS.md).

## Copyright

Copyright (c) Jitse Boonstra 2018 All rights reserved.
