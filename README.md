![Banner](https://i.imgur.com/WL6QeUA.png)
NPCLib – Basic non-player character library.<br>
[![Release](https://jitpack.io/v/JitseB/NPCLib.svg)](https://github.com/JitseB/NPCLib/releases)
[![Build Status](https://travis-ci.com/JitseB/NPCLib.svg?branch=master)](https://travis-ci.com/JitseB/NPCLib)
[![Versions](https://img.shields.io/badge/MC-1.8.8%20--%20latest-blue.svg)](https://github.com/JitseB/NPCLib/releases)
[![Resource](https://img.shields.io/badge/SpigotMC-Resource-orange.svg)](https://www.spigotmc.org/resources/npclib.55884/)
=

This is an API made specifically for spigot servers (Minecraft). Current supported versions: **1.8.8 - latest**. Lightweight replacement for Citizens. NPCLib only uses packets instead of registering the entity in the actual Minecraft server.

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
        <version>2.11.1-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```
If you do not want to have NPCLib in your `plugins` folder, you need to use the `npclib-plugin` artifact and [shade it](https://maven.apache.org/plugins/maven-shade-plugin/) accordingly.

Always make sure to use the latest stable release. [Click here](https://github.com/JitseB/NPCLib/releases/latest) to view the latest release.

#### Repacking the library
To make sure the classes won't be twice at the same place. I recommend repacking the library into your package. (Otherwise [issue #79](https://github.com/MinecraftLibraries/NPCLib/issues/79) might occur.) You can do that as follow:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.1.0</version>
      <!-- Do not include the <configuration>...</configuration> part if you are using Sponge! -->
      <configuration>
        <relocations>
          <relocation>
            <pattern>net.jitse.npclib</pattern>
            <!-- Replace this with your package! -->
            <shadedPattern>your.package</shadedPattern>
          </relocation>
        </relocations>
      </configuration>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
#### Tutorial
**[Click here](https://github.com/JitseB/NPCLib/blob/master/DOCUMENTATION.md) for an elaborate explanation on how to use NPCLib in your next project.**

### Versioning
For NPCLib I use the follow versioning system:
* 1.x.x: Where the 1 indicates the major version of the library. When this number changes, it's highly likely there're big API changes.
* x.1.x: Where the 1 indicates the minor version of the library. When this number changes, a feature is added or a high priority bug is fixed.
* x.x.1: Where the 1 indicates another minor version of the library. When this number changes, a small bug has been fixed.

### Building your own version

1. [Download](https://github.com/JitseB/NPCLib/archive/master.zip) or clone this repository.
2. You can build the project using `mvn clean install`.

The API JAR will be under `/api/target/` and the plugin JAR (which includes all necessary NMS code) will be under `/plugins/target/`.

## License

NPCLib is licensed under the [MIT license](https://github.com/JitseB/NPCLib/blob/master/LICENSE.md).
Developers are free to use NPCLib for both private and commercial use. However, it would still be nice to acknowledge me.

## Acknowledgement

I thank all those who have [contributed](https://github.com/JitseB/NPCLib/graphs/contributors) to NPCLib over the course of its development.

Please view other credits [here](https://github.com/JitseB/NPCLib/blob/master/CREDITS.md).

## Copyright

Copyright (c) Jitse Boonstra 2018 All rights reserved.
