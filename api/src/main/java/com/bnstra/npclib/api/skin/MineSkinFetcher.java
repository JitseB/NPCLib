/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.api.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jitse Boonstra
 */
public class MineSkinFetcher {

    private static final String MINESKIN_API = "https://api.mineskin.org/v2/skins/";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static void fetchSkinFromUUIDAsync(String uuid, Callback callback) {
        EXECUTOR.execute(() -> {
            try {
                StringBuilder builder = new StringBuilder();
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MINESKIN_API + uuid).openConnection();
                httpURLConnection.setRequestProperty("User-Agent", "NPCLib/v2.13");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Authorization", "Bearer 0c9a497bdb277481270ce81eaab21b1d6727743c53858cc78d50052befbc73c0");
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
                JsonObject textures = jsonObject.get("skin").getAsJsonObject()
                        .get("texture").getAsJsonObject()
                        .get("data").getAsJsonObject();
                String value = textures.get("value").getAsString();
                String signature = textures.get("signature").getAsString();
                callback.call(new Skin(value, signature));
            } catch (IOException exception) {
                Bukkit.getLogger().severe("Could not fetch skin! (Id: " + uuid + "). Message: " + exception.getMessage());
                exception.printStackTrace();
                callback.failed();
            }
        });
    }

    public static void fetchSkinFromUUIDSync(String uuid, Callback callback) {
        try {
            StringBuilder builder = new StringBuilder();
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MINESKIN_API + uuid).openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "NPCLib/v2.13");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Authorization", "Bearer 0c9a497bdb277481270ce81eaab21b1d6727743c53858cc78d50052befbc73c0");
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
            JsonObject textures = jsonObject.get("skin").getAsJsonObject()
                    .get("texture").getAsJsonObject()
                    .get("data").getAsJsonObject();
            String value = textures.get("value").getAsString();
            String signature = textures.get("signature").getAsString();

            callback.call(new Skin(value, signature));
        } catch (IOException exception) {
            Bukkit.getLogger().severe("Could not fetch skin! (Id: " + uuid + "). Message: " + exception.getMessage());
            exception.printStackTrace();
            callback.failed();
        }
    }

    public interface Callback {

        void call(Skin skinData);

        default void failed() {
        }
    }
}
