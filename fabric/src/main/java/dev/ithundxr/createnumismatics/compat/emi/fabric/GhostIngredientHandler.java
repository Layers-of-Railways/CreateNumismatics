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

package dev.ithundxr.createnumismatics.compat.emi.fabric;

import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.ithundxr.createnumismatics.content.backend.IGhostItemMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.GhostItemSubmitPacket;
import dev.ithundxr.createnumismatics.util.ClientCraftingUtils;
import io.github.fabricators_of_create.porting_lib.mixin.accessors.client.accessor.AbstractContainerScreenAccessor;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GhostIngredientHandler<T extends MenuBase<?> & IGhostItemMenu>
		implements EmiDragDropHandler<AbstractSimiContainerScreen<T>> {

	@Override
	public boolean dropStack(AbstractSimiContainerScreen<T> gui, EmiIngredient ingredient, int x, int y) {
		List<EmiStack> stacks = ingredient.getEmiStacks();
		if (!(gui instanceof AbstractContainerScreenAccessor access) || stacks.size() != 1)
			return false;
		ItemStack stack = stacks.get(0).getItemStack();
		if (stack.isEmpty())
			return false;

		for (int i = 0; i < gui.getMenu().slots.size(); i++) {
			Slot slot = gui.getMenu().slots.get(i);
			if (slot.isActive() && gui.getMenu().isSlotGhost(i)) {
				Rect2i slotArea = new Rect2i(access.port_lib$getGuiLeft() + slot.x, access.port_lib$getGuiTop() + slot.y, 16, 16);
				if (slotArea.contains(x, y)) {
					acceptStack(gui, i, stack);
					return true;
				}
			}
		}

		return false;
	}

	private void acceptStack(AbstractSimiContainerScreen<T> gui, int slotIndex, ItemStack stack) {
		stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
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
