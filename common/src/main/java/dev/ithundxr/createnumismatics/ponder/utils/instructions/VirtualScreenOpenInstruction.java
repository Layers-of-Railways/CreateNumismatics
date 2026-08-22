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

package dev.ithundxr.createnumismatics.ponder.utils.instructions;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.ElementLinkImpl;
import net.createmod.ponder.foundation.element.OutlinerElement;
import net.createmod.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class VirtualScreenOpenInstruction<M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> extends TickingInstruction {
    private final VirtualScreenElement<M, S, B> element;
    private ElementLink<VirtualScreenElement<M, S, B>> elementLink;

    public VirtualScreenOpenInstruction(VirtualScreenElement<M, S, B> element, int fadeInTicks) {
        super(false, fadeInTicks);
        this.element = element;
        element.outline = null;
    }

    public VirtualScreenOpenInstruction(VirtualScreenElement<M, S, B> element, int fadeInTicks, Selection selection) {
        this(element, fadeInTicks);
        element.outline = new OutlinerElement(o -> selection.makeOutline(o)
            .lineWidth(1 / 16f));
    }

    @Override
    protected void firstTick(PonderScene scene) {
        super.firstTick(scene);
        scene.addElement(element);
        element.setVisible(true);
        element.setFade(0);
        element.clearState();
        if (element.outline != null) {
            scene.addElement(element.outline);
            element.outline.setFade(1);
            element.outline.setVisible(true);
        }
        if (elementLink != null)
            scene.linkElement(element, elementLink);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (element.outline != null)
            element.outline.setColor(element.getColor());
        float fade = totalTicks == 0 ? 1 : (remainingTicks / (float) totalTicks);
        element.setFade(1 - fade * fade);
        if (remainingTicks == 0) {
            element.setFade(1);
        }
    }

    public ElementLink<VirtualScreenElement<M, S, B>> createLink(PonderScene scene) {
        elementLink = new ElementLinkImpl<>(VirtualScreenElement.genericClass());
        scene.linkElement(element, elementLink);
        return elementLink;
    }
}
