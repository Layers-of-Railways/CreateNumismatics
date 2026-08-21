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
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.instruction.TickingInstruction;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class VirtualScreenCloseInstruction<M extends AbstractContainerMenu, S extends AbstractSimiContainerScreen<M> & VirtualizableScreen, B extends SmartBlockEntity> extends TickingInstruction {
    private final ElementLink<VirtualScreenElement<M, S, B>> elementLink;
    private VirtualScreenElement<M, S, B> element;

    public VirtualScreenCloseInstruction(ElementLink<VirtualScreenElement<M, S, B>> elementLink, int fadeOutTicks) {
        super(false, fadeOutTicks);
        this.elementLink = elementLink;
    }

    @Override
    protected void firstTick(PonderScene scene) {
        super.firstTick(scene);
        element = scene.resolve(elementLink);
        if (element == null)
            return;

        element.setVisible(true);
        element.setFade(1);

        if (element.outline != null) {
            element.outline.setFade(1);
            element.outline.setVisible(true);
        }
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (element == null)
            return;
        if (element.outline != null)
            element.outline.setColor(element.getColor());
        float fade = (remainingTicks / (float) totalTicks);
        element.setFade(1 - (1 - fade) * (1 - fade));
        if (remainingTicks == 0) {
            element.setVisible(false);
            element.setFade(0);
            if (element.outline != null) {
                element.outline.setVisible(false);
                element.outline.setFade(0);
            }
        }
    }
}
