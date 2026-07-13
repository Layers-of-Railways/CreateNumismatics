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
import com.simibubi.create.foundation.ponder.element.OutlinerElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import dev.ithundxr.createnumismatics.mixin.client.AccessorAbstractContainerScreen;
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderUI_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.dev_export.PonderExport;
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
import org.jetbrains.annotations.ApiStatus;
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
    private boolean scaleDown;

    @Nullable Consumer<Inventory> inventoryFiller = null;
    int color = PonderPalette.WHITE.getColor();

    private boolean alreadyWarned = false;
    private @Nullable ActiveState<M, S> state;

    int prevWidth = -1;
    int prevHeight = -1;

    @ApiStatus.Internal
    public @Nullable OutlinerElement outline;

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
        this.scaleDown = NumismaticsConfig.client().scalePonderGui.get() && !PonderExport.active;
    }

    @ApiStatus.Internal
    public void clearState() {
        state = null;
        scaleDown = NumismaticsConfig.client().scalePonderGui.get() && !PonderExport.active;
    }

    public @NotNull M getMenu() {
        if (state == null)
            throw new IllegalStateException("Cannot get menu when screen is not presenting");
        return state.menu;
    }

    public @Nullable M getMenuUnchecked() {
        return state == null ? null : state.menu;
    }

    public <T> @Nullable T applyMenu(Function<M, T> function) {
        if (state == null)
            return null;
        return function.apply(state.menu);
    }

    public boolean runMenu(Consumer<M> consumer) {
        if (state == null)
            return false;
        consumer.accept(state.menu);
        return true;
    }

    public @NotNull S getScreen() {
        if (state == null)
            throw new IllegalStateException("Cannot get screen when screen is not presenting");
        return state.screen;
    }

    public @Nullable S getScreenUnchecked() {
        return state == null ? null : state.screen;
    }

    public @NotNull ActiveState<M, S> getActiveState() {
        if (state == null)
            throw new IllegalStateException("Cannot get state when screen is not presenting");
        return state;
    }

    public @Nullable ActiveState<M, S> getActiveStateUnchecked() {
        return state;
    }

    public @NotNull CursorState getCursorState() {
        if (state == null)
            throw new IllegalStateException("Cannot get cursor state when screen is not presenting");
        return state.cursor;
    }

    public @Nullable CursorState getCursorStateUnchecked() {
        return state == null ? null : state.cursor;
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
        float scale = (float) ((scaleDown ? (guiScale <= 1 ? 0.75 : (guiScale - 1) / guiScale) : 1) * fade.getValue(partialTicks));

        int scaleCenterX = window.getGuiScaledWidth() / 2;
        int scaleCenterY = window.getGuiScaledHeight() / 2;

        int left = ((AccessorAbstractContainerScreen) state.screen).numismatics$getLeftPos();
        int top = ((AccessorAbstractContainerScreen) state.screen).numismatics$getTopPos();
        float x = ((vec.x + left - scaleCenterX) * scale) + scaleCenterX;
        float y = ((vec.y + top  - scaleCenterY) * scale) + scaleCenterY;
        return new Vec2(x, y);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        updateState(scene, Minecraft.getInstance());
        if (state != null) {
            state.cursor.tick();
            state.screen.virtualTick();
        }
    }

    @Override
    protected void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        updateStateWidth(mc, window);

        if (fade < 1 / 16f)
            return;

        if (state != null) {
            PoseStack ms = graphics.pose();
            ms.pushPose();

            ms.translate(0, 0, 1000);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            double guiScale = window.getGuiScale();
            float scale = (float) ((scaleDown ? (guiScale <= 1 ? 0.75 : (guiScale - 1) / guiScale) : 1) * fade);

            int scaleCenterX = window.getGuiScaledWidth() / 2;
            int scaleCenterY = window.getGuiScaledHeight() / 2;

            ms.translate(scaleCenterX, scaleCenterY, 0);
            ms.mulPoseMatrix(new Matrix4f().scaling(scale, scale, 0.01f));
            ms.translate(-scaleCenterX, -scaleCenterY, 0);

            // TODO: is it nicer to immediately floor/round this down?
            double localMouseX, localMouseY;
            NumismaticsIcons cursor;

            if (((PonderUI_Duck) screen).numismatics$isIdentifyMode()) {
                double globalMouseX = mc.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
                double globalMouseY = mc.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();

                localMouseX = (((globalMouseX - scaleCenterX) / scale) + scaleCenterX);
                localMouseY = (((globalMouseY - scaleCenterY) / scale) + scaleCenterY);
                cursor = null;
            } else {
                cursor = switch (state.cursor.cursor) {
                    case HIDDEN -> null;
                    case NORMAL -> NumismaticsIcons.I_CURSOR;
                    case SCROLL_DOWN -> NumismaticsIcons.I_CURSOR_SCROLL_DOWN[(AnimationTickHolder.getTicks(scene.getWorld()) / 5) % 3];
                    case SCROLL_UP -> NumismaticsIcons.I_CURSOR_SCROLL_UP[(AnimationTickHolder.getTicks(scene.getWorld()) / 5) % 3];
                };

                if (cursor == null) {
                    localMouseX = 0;
                    localMouseY = 0;
                } else {
                    int left = ((AccessorAbstractContainerScreen) state.screen).numismatics$getLeftPos();
                    int top = ((AccessorAbstractContainerScreen) state.screen).numismatics$getTopPos();
                    localMouseX = state.cursor.getX(partialTicks) + left;
                    localMouseY = state.cursor.getY(partialTicks) + top;
                }
            }

            // hide MC screen to prevent EMI rendering
            var screen0 = mc.screen;
            mc.screen = null;
            state.screen.render(graphics, (int) localMouseX, (int) localMouseY, partialTicks);
            mc.screen = screen0;

            if (cursor != null) {
                ms.pushPose();
                ms.translate(localMouseX - 1, localMouseY - 1, 2000);
                ms.mulPoseMatrix(new Matrix4f().scaling(1/scale, 1/scale, 1));
                cursor.render(graphics, 0, 0);
                ms.popPose();
            }

            ms.popPose();
        }
    }

    private void updateState(PonderScene scene, Minecraft mc) {
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
            Window window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();
            screen$.init(mc, width, height);
            prevWidth = width;
            prevHeight = height;
            state = new ActiveState<>(menu, screen$, inv, new CursorState());
        } else if (!visible && state != null) {
            state = null;
        }
    }

    private void updateStateWidth(Minecraft mc, Window window) {
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

    public record ActiveState<M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M>>(M menu, S screen, Inventory inv, CursorState cursor) {}

    public enum Cursor {
        HIDDEN,
        NORMAL,
        SCROLL_DOWN,
        SCROLL_UP
    }

    public final static class CursorPhysicsProperties {
        public final double stiffness;
        public final double dampingRatio;
        public final double naturalFreq;
        public final double dampedFreq;

        public CursorPhysicsProperties(double stiffness, double dampingRatio) {
            this.stiffness = stiffness;
            this.dampingRatio = dampingRatio;
            this.naturalFreq = Math.sqrt(this.stiffness);
            this.dampedFreq = this.naturalFreq * this.dampingRatio;
        }

        public static final CursorPhysicsProperties EXPRESSIVE_SPATIAL_SLOWER = new CursorPhysicsProperties(120, 0.8);
        public static final CursorPhysicsProperties EXPRESSIVE_SPATIAL_SLOW = new CursorPhysicsProperties(200, 0.8);
        public static final CursorPhysicsProperties EXPRESSIVE_SPATIAL_MEDIUM = new CursorPhysicsProperties(380, 0.8);
        public static final CursorPhysicsProperties EXPRESSIVE_SPATIAL_FAST = new CursorPhysicsProperties(800, 0.6);
    }

    public static class CursorState {
        private static final double POSITION_THRESHOLD = 1.5;
        // assuming 60 fps, snap if more than a frame to move
        private static final double VELOCITY_THRESHOLD = POSITION_THRESHOLD * 1/16.0;

        private @NotNull Cursor cursor;
        private @NotNull CursorPhysicsProperties physics;

        // prev position
        private double x0, y0;
        // current position
        private double x, y;
        // target
        private int tx, ty;
        // current velocity (pixels / second)
        private double vx, vy;

        private boolean settled;

        public CursorState() {
            this.cursor = Cursor.HIDDEN;
            this.physics = CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW;
            teleport(0, 0);
        }

        public CursorState setPhysics(@NotNull CursorPhysicsProperties physics) {
            this.physics = physics;
            return this;
        }

        public CursorState setCursor(@NotNull Cursor cursor) {
            this.cursor = cursor;
            return this;
        }

        public CursorState teleport(int x, int y) {
            this.x = this.tx = x;
            this.y = this.ty = y;
            this.vx = 0;
            this.vy = 0;
            this.settled = true;
            return this;
        }

        public CursorState teleport() {
            return teleport(tx, ty);
        }

        public CursorState snapFrame() {
            this.x0 = this.x;
            this.y0 = this.y;
            return this;
        }

        public CursorState target(int x, int y) {
            this.tx = x;
            this.ty = y;
            this.settled = false;
            return this;
        }

        public double getX(float partialTicks) {
            return x0 + (x - x0) * partialTicks;
        }

        public double getY(float partialTicks) {
            return y0 + (y - y0) * partialTicks;
        }

        public @NotNull Cursor getCursor() {
            return cursor;
        }

        public boolean isSettled() {
            return settled;
        }

        public void tick() {
            x0 = x;
            y0 = y;

            if (settled)
                return;

            final double delta = 1/20.0;

            // https://en.wikipedia.org/wiki/Mass-spring-damper_model
            double ax = -2 * physics.dampedFreq * vx - physics.stiffness * (x - tx);
            double ay = -2 * physics.dampedFreq * vy - physics.stiffness * (y - ty);

            vx += ax * delta / 2;
            vy += ay * delta / 2;
            x += vx * delta;
            y += vy * delta;
            vx += ax * delta / 2;
            vy += ay * delta / 2;

            if (Math.abs(vx) < VELOCITY_THRESHOLD && Math.abs(vy) < VELOCITY_THRESHOLD && Math.abs(x - tx) < POSITION_THRESHOLD && Math.abs(y - ty) < POSITION_THRESHOLD) {
                settled = true;
                x = tx;
                y = ty;
                vx = 0;
                vy = 0;
            }
        }
    }

    @FunctionalInterface
    public interface ScreenFactory<M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M>> {
        S create(M menu, Inventory inv, Component title);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, M extends AbstractContainerMenu, B extends SmartBlockEntity & MenuProvider> Class<VirtualScreenElement<M,S,B>> genericClass() {
        return (Class<VirtualScreenElement<M, S, B>>) (Class) VirtualScreenElement.class;
    }
}
