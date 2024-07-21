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

package dev.ithundxr.createnumismatics.config;

import com.simibubi.create.foundation.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class NumismaticsConfig {
    @ApiStatus.Internal
    public static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    //private static CClient client;
    private static CCommon common;
    private static CServer server;

//    public static CClient client() {
//        return client;
//    }

    public static CCommon common() {
        return common;
    }

    public static CServer server() {
        return server;
    }

    public static ConfigBase byType(ModConfig.Type type) {
        return CONFIGS.get(type);
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    @ApiStatus.Internal
    public static void registerCommon() {
        //client = register(CClient::new, ModConfig.Type.CLIENT);
        common = register(CCommon::new, ModConfig.Type.COMMON);
        server = register(CServer::new, ModConfig.Type.SERVER);
    }

    public static void onLoad(ModConfig modConfig) {
        for (ConfigBase config : CONFIGS.values())
            if (config.specification == modConfig
                    .getSpec())
                config.onLoad();
    }

    public static void onReload(ModConfig modConfig) {
        for (ConfigBase config : CONFIGS.values())
            if (config.specification == modConfig
                    .getSpec())
                config.onReload();
    }

    private static class TomlGroup {
        private final Map<String, TomlGroup> subgroups = new HashMap<>();
        private final Map<String, String> entries = new HashMap<>();
        private final String path;

        private static TomlGroup root() {
            return new TomlGroup("");
        }

        private TomlGroup(String path) {
            this.path = path;
        }

        public boolean isRoot() {
            return path.isEmpty();
        }

        public void add(String key, String value) {
            if (!isRoot())
                throw new NotImplementedException();

            String[] pieces = key.split("\\.");
            String subKey = pieces[pieces.length - 1];
            TomlGroup targetedGroup = this;
            for (int i = 0; i < pieces.length - 1; i++) {
                targetedGroup = targetedGroup.getOrCreateSubGroup(pieces[i]);
            }
            targetedGroup.entries.put(subKey, value);
        }

        private TomlGroup getOrCreateSubGroup(String subKey) {
            return subgroups.computeIfAbsent(subKey, (sk) -> new TomlGroup(path.isEmpty() ? sk : path + "." + sk));
        }

        private void write(StringBuilder b) {
            if (!isRoot()) {
                b.append("\n[").append(path).append("]");
            }

            for (Map.Entry<String, String> entry : entries.entrySet()) {
                b.append("\n").append(entry.getKey()).append(" = ").append(entry.getValue());
            }

            for (TomlGroup subGroup : subgroups.values()) {
                subGroup.write(b);
            }
        }
    }
}
