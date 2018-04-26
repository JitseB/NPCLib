/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Jitse Boonstra
 */
public class MineSkinFetcher {

    private static final String MINESKIN_API = "https://api.mineskin.org/get/id/";

    public static void fetchSkinFromIdAsync(int id, Callback callback) {
        new Thread(() -> {
            try {
                StringBuilder builder = new StringBuilder();
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MINESKIN_API + id).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                Scanner scanner = new Scanner(httpURLConnection.getInputStream());
                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine());
                }

                scanner.close();
                httpURLConnection.disconnect();

                JsonObject jsonObject = (JsonObject) new JsonParser().parse(builder.toString());
                JsonObject textures = jsonObject.get("data").getAsJsonObject().get("texture").getAsJsonObject();
                String value = textures.get("value").getAsString();
                String signature = textures.get("signature").getAsString();

                callback.call(new Skin(value, signature));
            } catch (IOException exception) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not fetch skin! (Id: " + id + "). Message: " + exception.getMessage());
                exception.printStackTrace();
                callback.failed();
            }
        }).start();
    }

    public interface Callback {

        void call(Skin skinData);

        default void failed() {
        }
    }
}
