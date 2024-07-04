
/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.util;

import com.google.gson.JsonParser;
import dev.ithundxr.createnumismatics.NumismaticsClient;
import dev.ithundxr.createnumismatics.multiloader.Env;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.mutable.MutableObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public enum UsernameUtils {
    INSTANCE;

    private final HashMap<UUID, String> uuidNameMap = new HashMap<>();
    private final Set<UUID> tried = new HashSet<>();

    private static final String url = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public String getName(UUID uuid) {
        return getName(uuid, uuid == null ? "Unknown" : "[id="+uuid+"]");
    }

    public String getName(UUID uuid, String defaultName) {
        if (uuid == null) return defaultName;
        if (!uuidNameMap.containsKey(uuid)) {
            MutableObject<String> result = new MutableObject<>(null);
            Env.CLIENT.runIfCurrent(() -> () -> {
                if (Minecraft.getInstance().getUser().getUuid().equals(uuid.toString())) {
                    uuidNameMap.put(uuid, Minecraft.getInstance().getUser().getName());
                    result.setValue(uuidNameMap.get(uuid));
                }
                if (NumismaticsClient.bankAccountLabels.containsKey(uuid)) {
                    result.setValue(NumismaticsClient.bankAccountLabels.get(uuid));
                }
            });
            if (result.getValue() != null) {
                return result.getValue();
            }
            if (!tried.contains(uuid)) {
                CompletableFuture.runAsync(() -> {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder(URI.create(url + uuid.toString().replace("-", "")))
                        .GET()
                        .build();
                    try {
                        String body = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join().body();
                        uuidNameMap.put(uuid, JsonParser.parseString(body).getAsJsonObject().get("name").getAsString());
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                });
                tried.add(uuid);
            }
            return defaultName;
        }
        return uuidNameMap.get(uuid);
    }
}
