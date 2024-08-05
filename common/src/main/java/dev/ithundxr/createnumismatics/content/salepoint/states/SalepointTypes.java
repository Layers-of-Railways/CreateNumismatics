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

package dev.ithundxr.createnumismatics.content.salepoint.states;

import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SalepointTypes {
    ITEM(ItemSalepointState::new, ItemStack.class),
    FLUID(FluidSalepointState::create, MultiloaderFluidStack.class),
    ;

    @NotNull
    private final Factory factory;
    private final Class<?> contentClass;

    SalepointTypes(@NotNull Factory factory, Class<?> contentClass) {
        this.factory = factory;
        this.contentClass = contentClass;
    }

    public String getId() {
        return name();
    }

    public Class<?> getContentClass() {
        return contentClass;
    }

    @Contract("-> new")
    public @NotNull ISalepointState<?> create() {
        ISalepointState<?> state = factory.create();
        state.init();
        return state;
    }

    @Contract("_ -> new")
    public static @Nullable ISalepointState<?> load(@NotNull CompoundTag tag) {
        if (!tag.contains("id", CompoundTag.TAG_STRING))
            return null;

        String id = tag.getString("id");
        for (SalepointTypes type : values()) {
            if (type.name().equals(id)) {
                ISalepointState<?> state = type.create();
                state.load(tag);
                return state;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface Factory {
        @Contract("-> new")
        ISalepointState<?> create();
    }
}
