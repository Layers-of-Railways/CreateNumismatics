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

package dev.ithundxr.createnumismatics.ponder.utils.elements;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.element.AnimatedOverlayElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.mixin.client.AccessorAbstractContainerScreen;
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderUI_Duck;
import dev.ithundxr.createnumismatics.registry.NumismaticsIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class VirtualScreenElement<M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity & MenuProvider> extends AnimatedOverlayElement {
    private final BlockPos bePos;
    private final BlockEntityType<B> beType;
    private final BiFunction<B, Inventory, M> menuFactory;
    private final ScreenFactory<M, S> screenFactory;

    @Nullable Consumer<Inventory> inventoryFiller = null;
    int color = PonderPalette.WHITE.getColor();

    private boolean alreadyWarned = false;
    private @Nullable ActiveState<M, S> state;

    int prevWidth = -1;
    int prevHeight = -1;

    public class Builder {
        private final PonderScene scene;
        private final ElementLink<VirtualScreenElement<M, S, B>> link;

        public Builder(PonderScene scene, ElementLink<VirtualScreenElement<M, S, B>> link) {
            this.scene = scene;
            this.link = link;
        }

        public Builder colored(PonderPalette color) {
            VirtualScreenElement.this.color = color.getColor();
            return this;
        }

        public Builder inventoryFiller(@Nullable Consumer<Inventory> filler) {
            VirtualScreenElement.this.inventoryFiller = filler;
            return this;
        }

        public Builder attachKeyFrame() {
            scene.builder()
                .addLazyKeyframe();
            return this;
        }

        public ElementLink<VirtualScreenElement<M, S, B>> link() {
            return link;
        }
    }

    public VirtualScreenElement(BlockPos bePos, BlockEntityType<B> beType, BiFunction<B, Inventory, M> menuFactory, ScreenFactory<M, S> screenFactory) {
        this.bePos = bePos;
        this.beType = beType;
        this.menuFactory = menuFactory;
        this.screenFactory = screenFactory;
    }

    public @NotNull M getMenu() {
        if (state == null)
            throw new IllegalStateException("Cannot get menu when screen is not presenting");
        return state.menu;
    }

    public @Nullable M getMenuUnchecked() {
        return state == null ? null : state.menu;
    }

    public @NotNull S getScreen() {
        if (state == null)
            throw new IllegalStateException("Cannot get screen when screen is not presenting");
        return state.screen;
    }

    public @Nullable S getScreenUnchecked() {
        return state == null ? null : state.screen;
    }

    public @Nullable Vec2 guiLocalToGlobal(Function<M, Vec2> vec, float partialTicks) {
        if (state == null)
            return null;
        return guiLocalToGlobal(vec.apply(state.menu), partialTicks);
    }

    public @Nullable Vec2 guiLocalToGlobal(Vec2 vec, float partialTicks) {
        if (state == null)
            return null;

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        double guiScale = window.getGuiScale();
        float scale = (float) ((guiScale <= 1 ? 0.75 : (guiScale - 1) / guiScale) * fade.getValue(partialTicks));

        int scaleCenterX = window.getGuiScaledWidth() / 2;
        int scaleCenterY = window.getGuiScaledHeight() / 2;

        int left = ((AccessorAbstractContainerScreen) state.screen).numismatics$getLeftPos();
        int top = ((AccessorAbstractContainerScreen) state.screen).numismatics$getTopPos();
        float x = ((vec.x + left - scaleCenterX) * scale) + scaleCenterX;
        float y = ((vec.y + top  - scaleCenterY) * scale) + scaleCenterY;
        return new Vec2(x, y);
    }

    @Override
    protected void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        updateState(scene, screen, mc, window);

        if (fade < 1 / 16f)
            return;

        if (state != null) {
            PoseStack ms = graphics.pose();
            ms.pushPose();

            ms.translate(0, 0, 1000);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            double guiScale = window.getGuiScale();
            float scale = (float) ((guiScale <= 1 ? 0.75 : (guiScale - 1) / guiScale) * fade);

            int scaleCenterX = window.getGuiScaledWidth() / 2;
            int scaleCenterY = window.getGuiScaledHeight() / 2;

            ms.translate(scaleCenterX, scaleCenterY, 0);
            ms.mulPoseMatrix(new Matrix4f().scaling(scale, scale, 0.01f));
            ms.translate(-scaleCenterX, -scaleCenterY, 0);

            int localMouseX, localMouseY;
            NumismaticsIcons cursor;

            if (((PonderUI_Duck) screen).numismatics$isIdentifyMode()) {
                double globalMouseX = mc.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
                double globalMouseY = mc.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();

                localMouseX = (int) (((globalMouseX - scaleCenterX) / scale) + scaleCenterX);
                localMouseY = (int) (((globalMouseY - scaleCenterY) / scale) + scaleCenterY);
                cursor = NumismaticsIcons.I_CURSOR_SCROLL_DOWN[(AnimationTickHolder.getTicks(true) / 5) % 3];
            } else {
                localMouseX = 0;
                localMouseY = 0;
                cursor = null;
            }

            // hide MC screen to prevent EMI rendering
            var screen0 = mc.screen;
            mc.screen = null;
            state.screen.render(graphics, localMouseX, localMouseY, partialTicks);
            mc.screen = screen0;

            if (cursor != null) {
                ms.pushPose();
                ms.translate(0, 0, 2000);
                cursor.render(graphics, localMouseX - 1, localMouseY - 1);
                ms.popPose();
            }

            ms.popPose();
        }
    }

    private void updateState(PonderScene scene, PonderUI screen, Minecraft mc, Window window) {
        boolean visible = isVisible();
        if (visible && state == null) {
            var be$ = scene.getWorld().getBlockEntity(bePos, beType);
            if (be$.isEmpty()) {
                if (!alreadyWarned) {
                    alreadyWarned = true;
                    Numismatics.LOGGER.warn("VirtualScreenElement could not find expected block entity {} at {}", beType, bePos, new Exception());
                }
                return;
            }
            B be = be$.get();
            Player player = mc.player;
            if (player == null)
                return;

            Inventory inv = new Inventory(player);
            if (this.inventoryFiller != null)
                this.inventoryFiller.accept(inv);

            M menu = menuFactory.apply(be, inv);
            menu.suppressRemoteUpdates();
            S screen$ = screenFactory.create(menu, inv, be.getDisplayName());
            screen$.markVirtual();
            screen$.init(mc, screen.width, screen.height);
            prevWidth = -1;
            prevHeight = -1;
            state = new ActiveState<>(menu, screen$, inv);
        } else if (!visible && state != null) {
            state = null;
        }

        if (state != null) {
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();
            if (width != prevWidth || height != prevHeight) {
                if (prevWidth != -1 && prevHeight != -1) {
                    state.screen.resize(mc, width, height);
                }
                prevWidth = width;
                prevHeight = height;
            }
        }
    }

    public int getColor() {
        return color;
    }

    private record ActiveState<M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M>>(M menu, S screen, Inventory inv) {}

    @FunctionalInterface
    public interface ScreenFactory<M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M>> {
        S create(M menu, Inventory inv, Component title);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, M extends AbstractContainerMenu, B extends SmartBlockEntity & MenuProvider> Class<VirtualScreenElement<M,S,B>> genericClass() {
        return (Class<VirtualScreenElement<M, S, B>>) (Class) VirtualScreenElement.class;
    }
}
