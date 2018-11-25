#!/bin/bash

echo "Building JARs..."

ls jars >> /dev/null 2>&1 || mkdir jars

echo "Building npclib-api.jar..."
/usr/local/apache-maven-3.5.4/bin/mvn clean install >> /dev/null 2>&1 && cp ./api/target/*.jar ./jars && echo "Finished building NPCLib API" || echo "Failed building NPCLib API. Please rebuild manually"

echo "Building npclib-plugin.jar..."
/usr/local/apache-maven-3.5.4/bin/mvn clean install -Pplugin >> /dev/null 2>&1 && cp ./target/*.jar ./jars && echo "Finished building NPCLib plugin" || echo "Failed building NPCLib plugin. Please rebuild manually"