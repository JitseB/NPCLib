echo "Building NPCLib jars..."
ls jars >> /dev/null 2>&1 || mkdir jars

echo "Building NPCLib-API.jar..."
mvn clean install >> /dev/null 2>&1 && cp ./api/target/*.jar ./jars && echo "Finished building NPCLib-API.jar." || echo "Failed building NPCLib-API.jar. Please rebuild manually!"

echo "Building NPCLib-Plugin.jar..."
mvn clean install -Pplugin >> /dev/null 2>&1 && cp ./target/*.jar ./jars && echo "Finished building NPCLib-Plugin.jar." || echo "Failed building NPCLib-Plugin.jar. Please rebuild manually!"

echo "Finished building NPCLib jars!"