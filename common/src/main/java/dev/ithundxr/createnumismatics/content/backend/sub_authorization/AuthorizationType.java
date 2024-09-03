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

package dev.ithundxr.createnumismatics.content.backend.sub_authorization;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public enum AuthorizationType implements StringRepresentable {
    TRUSTED_PLAYERS(
        "Trusted Players Only",
        "Only players on the trust list",
        NumismaticsGuiTextures.SUB_ACCOUNT_MODE_TRUSTED_PLAYERS
    ),
    TRUSTED_AUTOMATION(
        "Trusted Players + Automation",
        "Players on the trust list and automation placed by them (e.g. Deployers)",
        NumismaticsGuiTextures.SUB_ACCOUNT_MODE_TRUSTED_AUTOMATION
    ),
    ANYBODY(
        "Anybody",
        "Anybody with the ID, including automation such as ComputerCraft computers",
        NumismaticsGuiTextures.SUB_ACCOUNT_MODE_ANY
    );

    private final String englishTitle;
    private final String englishDescription;
    public final NumismaticsGuiTextures icon;

    AuthorizationType(String englishTitle, String englishDescription, NumismaticsGuiTextures icon) {
        this.englishTitle = englishTitle;
        this.englishDescription = englishDescription;
        this.icon = icon;
    }

    private String titleKey() {
        return "numismatics.authorization_type." + name().toLowerCase(Locale.ROOT);
    }

    private String descriptionKey() {
        return titleKey() + ".description";
    }

    public Component title() {
        return Components.translatable(titleKey());
    }

    public Component description() {
        return Components.translatable(descriptionKey());
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (AuthorizationType type : values()) {
            consumer.accept(type.titleKey(), type.englishTitle);
            consumer.accept(type.descriptionKey(), type.englishDescription);
        }
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static AuthorizationType deserialize(String string) {
        if (string == null) {
            return TRUSTED_PLAYERS;
        }

        string = string.toLowerCase(Locale.ROOT);
        for (AuthorizationType type : values()) {
            if (type.getSerializedName().equals(string)) {
                return type;
            }
        }
        return TRUSTED_PLAYERS;
    }

    public static List<Component> labeledComponents() {
        Component[] tmp = new Component[values().length];
        for (AuthorizationType at : values()) {
            tmp[at.ordinal()] = at.title();
        }
        return ImmutableList.copyOf(tmp);
    }
}
