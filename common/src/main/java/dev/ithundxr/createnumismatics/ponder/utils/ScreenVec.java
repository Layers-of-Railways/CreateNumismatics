/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.ponder.utils;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement;
import dev.ithundxr.createnumismatics.util.FusedFunction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

public record ScreenVec<
    M extends AbstractContainerMenu,
    S extends AbstractSimiContainerScreen<M> & VirtualizableScreen,
    B extends SmartBlockEntity
>(ElementLink<VirtualScreenElement<M, S, B>> screen, FusedFunction<M, Vec2> vec) {
    public static <
        M extends AbstractContainerMenu,
        S extends AbstractSimiContainerScreen<M> & VirtualizableScreen,
        B extends SmartBlockEntity
    > ScreenVec<M, S, B> relative(ElementLink<VirtualScreenElement<M, S, B>> screen, float x, float y) {
        return new ScreenVec<>(screen, new FusedFunction<>(new Vec2(x, y)));
    }

    public static <
        M extends AbstractContainerMenu,
        S extends AbstractSimiContainerScreen<M> & VirtualizableScreen,
        B extends SmartBlockEntity
        > ScreenVec<M, S, B> slotRelative(ElementLink<VirtualScreenElement<M, S, B>> screen, float x, float y, int slotId) {
        return new ScreenVec<>(screen, new FusedFunction<>(menu -> {
            Slot slot$ = menu.getSlot(slotId);
            return new Vec2(x + slot$.x, y + slot$.y);
        }));
    }

    public @Nullable Vec2 toGlobal(PonderScene scene, float partialTicks) {
        return scene.applyTo(screen, s -> s.guiLocalToGlobal(vec, partialTicks));
    }

    public @Nullable Vec2 toLocal(PonderScene scene) {
        return scene.applyTo(screen, this::toLocal);
    }

    public @Nullable Vec2 toLocal(VirtualScreenElement<M, S, B> vse) {
        return vse.applyMenu(vec);
    }
}
