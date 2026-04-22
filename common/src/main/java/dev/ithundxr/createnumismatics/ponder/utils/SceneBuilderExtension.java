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

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.Selection;
import dev.ithundxr.createnumismatics.mixin.AccessorSceneBuilder;
import dev.ithundxr.createnumismatics.ponder.utils.elements.TextComponentWindowElement;
import dev.ithundxr.createnumismatics.ponder.utils.instructions.TextComponentInstruction;

public class SceneBuilderExtension {
    private final SceneBuilder wrapped;

    public SceneBuilderExtension(SceneBuilder wrapped) {
        this.wrapped = wrapped;
    }

    public TextComponentWindowElement.Builder showTextComponent(int duration) {
        TextComponentWindowElement textWindowElement = new TextComponentWindowElement();
        wrapped.addInstruction(new TextComponentInstruction(textWindowElement, duration));
        return textWindowElement.new Builder(((AccessorSceneBuilder) wrapped).numismatics$getScene());
    }

    public TextComponentWindowElement.Builder showSelectionWithTextComponent(Selection selection, int duration) {
        TextComponentWindowElement textWindowElement = new TextComponentWindowElement();
        wrapped.addInstruction(new TextComponentInstruction(textWindowElement, duration, selection));
        return textWindowElement.new Builder(((AccessorSceneBuilder) wrapped).numismatics$getScene()).pointAt(selection.getCenter());
    }
}
