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
import com.simibubi.create.foundation.gui.menu.MenuBase;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.TextWindowElement;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.mixin.AccessorSceneBuilder;
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderOverlayElement_Duck;
import dev.ithundxr.createnumismatics.mixin_interfaces.SceneBuilder_Duck;
import dev.ithundxr.createnumismatics.mixin_interfaces.TextWindowElement_Builder_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.elements.TextComponentWindowElement;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement;
import dev.ithundxr.createnumismatics.ponder.utils.instructions.TextComponentInstruction;
import dev.ithundxr.createnumismatics.ponder.utils.instructions.VirtualScreenCloseInstruction;
import dev.ithundxr.createnumismatics.ponder.utils.instructions.VirtualScreenOpenInstruction;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec2;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SceneBuilderExtension {
    private final SceneBuilder wrapped;

    public SceneBuilderExtension(SceneBuilder wrapped) {
        this.wrapped = wrapped;
    }

    public void enableScreenOverlayLayer() {
        ((SceneBuilder_Duck) wrapped).numismatics$setOverlayLayer(true);
    }

    public void disableScreenOverlayLayer() {
        ((SceneBuilder_Duck) wrapped).numismatics$setOverlayLayer(false);
    }

    public TextWindowElement.Builder showText(int duration, ScreenVec<?, ?, ?> pixelPointAt) {
        TextWindowElement.Builder builder = wrapped.overlay.showText(duration);
        ((TextWindowElement_Builder_Duck) builder).numismatics$pointAtPixel(pixelPointAt);
        return builder;
    }

    public TextWindowElement.Builder showSelectionWithText(Selection selection, int duration, ScreenVec<?, ?, ?> pixelPointAt) {
        TextWindowElement.Builder builder = wrapped.overlay.showSelectionWithText(selection, duration);
        ((TextWindowElement_Builder_Duck) builder).numismatics$pointAtPixel(pixelPointAt);
        return builder;
    }

    public TextComponentWindowElement.Builder showTextComponent(int duration) {
        TextComponentWindowElement textWindowElement = new TextComponentWindowElement();
        PonderOverlayElement_Duck.numismatics$applyOverlay(wrapped, textWindowElement);
        wrapped.addInstruction(new TextComponentInstruction(textWindowElement, duration));
        return textWindowElement.new Builder(((AccessorSceneBuilder) wrapped).numismatics$getScene());
    }

    public TextComponentWindowElement.Builder showSelectionWithTextComponent(Selection selection, int duration) {
        TextComponentWindowElement textWindowElement = new TextComponentWindowElement();
        PonderOverlayElement_Duck.numismatics$applyOverlay(wrapped, textWindowElement);
        wrapped.addInstruction(new TextComponentInstruction(textWindowElement, duration, selection));
        return textWindowElement.new Builder(((AccessorSceneBuilder) wrapped).numismatics$getScene()).pointAt(selection.getCenter());
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity & MenuProvider> VirtualScreenElement<M, S, B>.Builder showContainerMenu(int fadeInTicks, BlockPos bePos, BlockEntityType<B> beType, BiFunction<B, Inventory, M> menuFactory, VirtualScreenElement.ScreenFactory<M, S> screenFactory) {
        return showContainerMenu(fadeInTicks, bePos, beType, menuFactory, screenFactory, MenuProvider::getDisplayName);
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> VirtualScreenElement<M, S, B>.Builder showContainerMenu(int fadeInTicks, BlockPos bePos, BlockEntityType<B> beType, BiFunction<B, Inventory, M> menuFactory, VirtualScreenElement.ScreenFactory<M, S> screenFactory, Function<B, Component> titleFactory) {
        VirtualScreenElement<M, S, B> element = new VirtualScreenElement<>(bePos, beType, menuFactory, screenFactory, titleFactory);
        VirtualScreenOpenInstruction<M, S, B> instruction = new VirtualScreenOpenInstruction<>(element, fadeInTicks);
        wrapped.addInstruction(instruction);
        PonderScene scene = ((AccessorSceneBuilder) wrapped).numismatics$getScene();
        return element.new Builder(scene, instruction.createLink(scene));
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity & MenuProvider> VirtualScreenElement<M, S, B>.Builder showSelectionWithContainerMenu(int fadeInTicks, Selection selection, BlockPos bePos, BlockEntityType<B> beType, BiFunction<B, Inventory, M> menuFactory, VirtualScreenElement.ScreenFactory<M, S> screenFactory) {
        return showSelectionWithContainerMenu(fadeInTicks, selection, bePos, beType, menuFactory, screenFactory, MenuProvider::getDisplayName);
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> VirtualScreenElement<M, S, B>.Builder showSelectionWithContainerMenu(int fadeInTicks, Selection selection, BlockPos bePos, BlockEntityType<B> beType, BiFunction<B, Inventory, M> menuFactory, VirtualScreenElement.ScreenFactory<M, S> screenFactory, Function<B, Component> titleFactory) {
        VirtualScreenElement<M, S, B> element = new VirtualScreenElement<>(bePos, beType, menuFactory, screenFactory, titleFactory);
        VirtualScreenOpenInstruction<M, S, B> instruction = new VirtualScreenOpenInstruction<>(element, fadeInTicks, selection);
        wrapped.addInstruction(instruction);
        PonderScene scene = ((AccessorSceneBuilder) wrapped).numismatics$getScene();
        return element.new Builder(scene, instruction.createLink(scene));
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void hideContainerMenu(ElementLink<VirtualScreenElement<M, S, B>> link, int fadeOutTicks) {
        VirtualScreenCloseInstruction<M, S, B> instruction = new VirtualScreenCloseInstruction<>(link, fadeOutTicks);
        wrapped.addInstruction(instruction);
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void modifyCursor(ElementLink<VirtualScreenElement<M, S, B>> link, Consumer<VirtualScreenElement.CursorState> modifier) {
        wrapped.addInstruction(scene -> scene.runWith(link, vse -> modifier.accept(vse.getCursorState())));
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void cursorTarget(ScreenVec<M, S, B> vec) {
        wrapped.addInstruction(scene -> scene.runWith(vec.screen(), vse -> {
            Vec2 local = vec.toLocal(vse);
            if (local == null) {
                crashInDev("Cannot target cursor in a closed screen");
                return;
            }
            vse.getCursorState().target((int) local.x, (int) local.y);
        }));
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void modifyScreen(ElementLink<VirtualScreenElement<M, S, B>> link, Consumer<VirtualScreenElement.ActiveState<M, S>> modifier) {
        wrapped.addInstruction(scene -> scene.runWith(link, vse -> {
            var state = vse.getActiveStateUnchecked();
            if (state == null) {
                crashInDev("Cannot modify closed screen");
                return;
            }
            modifier.accept(state);
        }));
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void swapCarriedAndInvSlot(ElementLink<VirtualScreenElement<M, S, B>> link, int slot) {
        modifyScreen(link, $ -> {
            ItemStack invItem = $.inv().removeItemNoUpdate(slot);
            ItemStack carriedItem = $.menu().getCarried();
            $.menu().setCarried(invItem);
            $.inv().setItem(slot, carriedItem);
        });
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void swapCarriedAndMenuSlot(ElementLink<VirtualScreenElement<M, S, B>> link, int slot) {
        modifyScreen(link, $ -> {
            Slot slot$ = $.menu().getSlot(slot);
            ItemStack invItem = slot$.getItem();
            ItemStack carriedItem = $.menu().getCarried();
            $.menu().setCarried(invItem);
            slot$.set(carriedItem);
        });
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void cloneMenuSlotToCarried(ElementLink<VirtualScreenElement<M, S, B>> link, int slot) {
        modifyScreen(link, $ -> {
            Slot slot$ = $.menu().getSlot(slot);
            ItemStack invItem = slot$.getItem();
            $.menu().setCarried(invItem.copy());
        });
    }

    public <M extends MenuBase<? super B>, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void clickSlot(ElementLink<VirtualScreenElement<M, S, B>> link, int slot) {
        clickSlot(link, slot, ClickAction.PRIMARY);
    }

    public <M extends MenuBase<? super B>, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void clickSlot(ElementLink<VirtualScreenElement<M, S, B>> link, int slot, ClickAction action) {
        clickSlot(link, slot, action.ordinal(), ClickType.PICKUP);
    }

    public <M extends MenuBase<? super B>, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> void clickSlot(ElementLink<VirtualScreenElement<M, S, B>> link, int slot, int button, ClickType clickType) {
        modifyScreen(link, $ -> $.menu().clicked(slot, button, clickType, $.menu().player));
    }

    private static void crashInDev(@SuppressWarnings("SameParameterValue") String message) {
        long start = System.currentTimeMillis();
        Numismatics.LOGGER.error(message); // set breakpoint here when developing
        if (Utils.isDevEnv()) {
            long end = System.currentTimeMillis();
            if (end - start < 50) { // crash if breakpoint wasn't set
                throw new RuntimeException("Please set a breakpoint above");
            }
        } else {
            Numismatics.LOGGER.error("Stacktrace: ", new RuntimeException(message));
        }
    }
}
