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

package dev.ithundxr.createnumismatics.compat.jei.forge;

import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.content.backend.IGhostItemMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.GhostItemSubmitPacket;
import dev.ithundxr.createnumismatics.util.ClientCraftingUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GhostIngredientHandler<T extends MenuBase<?> & IGhostItemMenu>
	implements IGhostIngredientHandler<AbstractSimiContainerScreen<T>> {

	@Override
	public <I> List<Target<I>> getTargetsTyped(AbstractSimiContainerScreen<T> gui, ITypedIngredient<I> ingredient,
		boolean doStart) {
        List<Target<I>> targets = new LinkedList<>();
		
		if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
			for (int i = 0; i < gui.getMenu().slots.size(); i++) {
				if (gui.getMenu().slots.get(i).isActive() && gui.getMenu().isSlotGhost(i))
					targets.add(new GhostTarget<>(gui, i));
			}
		}
		
		return targets;
	}

	@Override
	public void onComplete() {}

	@Override
	public boolean shouldHighlightTargets() {
		// TODO change to false and highlight the slots ourself in some better way
		return true;
	}

	private static class GhostTarget<I, T extends MenuBase<?> & IGhostItemMenu> implements Target<I> {

		private final Rect2i area;
		private final AbstractSimiContainerScreen<T> gui;
		private final int slotIndex;

        public GhostTarget(AbstractSimiContainerScreen<T> gui, int slotIndex) {
			this.gui = gui;
			this.slotIndex = slotIndex;
            Slot slot = gui.getMenu().slots.get(slotIndex);
			this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
		}

		@Override
		public Rect2i getArea() {
			return area;
		}

		@Override
		public void accept(I ingredient) {
			ItemStack stack = ((ItemStack) ingredient).copy();
			stack.setCount(1);
			if (!gui.getMenu().isSlotGhost(slotIndex))
				return;

			if (gui.getMenu().shouldGhostApplyEnchantsAndDye(slotIndex) && AllKeys.shiftDown()) {
				ItemStack existing = gui.getMenu().getSlot(slotIndex).getItem();
				ClientCraftingUtils.Result result = ClientCraftingUtils.applyStackingCrafts(existing, stack);
				stack = result.getResult(existing, true);
			}

			gui.getMenu().setGhostStackInSlot(slotIndex, stack);

			// sync new filter contents with server
			NumismaticsPackets.PACKETS.send(new GhostItemSubmitPacket(slotIndex, stack));
		}
	}
}
