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
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.TextWindowElement;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.mixin.AccessorSceneBuilder;
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderOverlayElement_Duck;
import dev.ithundxr.createnumismatics.mixin_interfaces.SceneBuilder_Duck;
import dev.ithundxr.createnumismatics.mixin_interfaces.TextWindowElement_Builder_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.elements.TextComponentWindowElement;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement;
import dev.ithundxr.createnumismatics.ponder.utils.instructions.TextComponentInstruction;
import dev.ithundxr.createnumismatics.ponder.utils.instructions.VirtualScreenInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiFunction;

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

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity & MenuProvider> VirtualScreenElement<M, S, B>.Builder showContainerMenu(int duration, BlockPos bePos, BlockEntityType<B> beType, BiFunction<B, Inventory, M> menuFactory, VirtualScreenElement.ScreenFactory<M, S> screenFactory) {
        VirtualScreenElement<M, S, B> element = new VirtualScreenElement<>(bePos, beType, menuFactory, screenFactory);
        VirtualScreenInstruction<M, S, B> instruction = new VirtualScreenInstruction<>(element, duration);
        wrapped.addInstruction(instruction);
        PonderScene scene = ((AccessorSceneBuilder) wrapped).numismatics$getScene();
        return element.new Builder(scene, instruction.createLink(scene));
    }

    public <M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity & MenuProvider> VirtualScreenElement<M, S, B>.Builder showSelectionWithContainerMenu(int duration, Selection selection, BlockPos bePos, BlockEntityType<B> beType, BiFunction<B, Inventory, M> menuFactory, VirtualScreenElement.ScreenFactory<M, S> screenFactory) {
        VirtualScreenElement<M, S, B> element = new VirtualScreenElement<>(bePos, beType, menuFactory, screenFactory);
        VirtualScreenInstruction<M, S, B> instruction = new VirtualScreenInstruction<>(element, duration, selection);
        wrapped.addInstruction(instruction);
        PonderScene scene = ((AccessorSceneBuilder) wrapped).numismatics$getScene();
        return element.new Builder(scene, instruction.createLink(scene));
    }
}
