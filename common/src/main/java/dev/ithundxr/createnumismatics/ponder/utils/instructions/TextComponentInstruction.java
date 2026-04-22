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

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.OutlinerElement;
import com.simibubi.create.foundation.ponder.instruction.FadeInOutInstruction;
import dev.ithundxr.createnumismatics.ponder.utils.elements.TextComponentWindowElement;

public class TextComponentInstruction extends FadeInOutInstruction {

    private TextComponentWindowElement element;
    private OutlinerElement outline;

    public TextComponentInstruction(TextComponentWindowElement element, int duration) {
        super(duration);
        this.element = element;
    }

    public TextComponentInstruction(TextComponentWindowElement element, int duration, Selection selection) {
        this(element, duration);
        outline = new OutlinerElement(o -> selection.makeOutline(o)
            .lineWidth(1 / 16f));
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (outline != null)
            outline.setColor(element.getColor());
    }

    @Override
    protected void show(PonderScene scene) {
        scene.addElement(element);
        element.setVisible(true);
        if (outline != null) {
            scene.addElement(outline);
            outline.setFade(1);
            outline.setVisible(true);
        }
    }

    @Override
    protected void hide(PonderScene scene) {
        element.setVisible(false);
        if (outline != null) {
            outline.setFade(0);
            outline.setVisible(false);
        }
    }

    @Override
    protected void applyFade(PonderScene scene, float fade) {
        element.setFade(fade);
    }
}
