/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.backend.trust_list;

import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.mixin.AccessorSimpleContainer;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class TrustListContainer extends SimpleContainer {
    private final List<UUID> trustList;
    private final Runnable changeNotifier;
    public TrustListContainer(List<UUID> trustList, Runnable changeNotifier) {
        super(27);
        this.trustList = trustList;
        this.changeNotifier = changeNotifier;
    }

    @Override
    public void setChanged() {
        trustList.clear();
        for (ItemStack stack : ((AccessorSimpleContainer) this).numismatics$getItems()) {
            UUID id;
            if ((id = IDCardItem.get(stack)) != null)
                trustList.add(id);
        }
        changeNotifier.run();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return NumismaticsTags.AllItemTags.ID_CARDS.matches(stack) && IDCardItem.isBound(stack) && !trustList.contains(IDCardItem.get(stack));
    }

    public CompoundTag save(CompoundTag nbt) {
        ContainerHelper.saveAllItems(nbt, ((AccessorSimpleContainer) this).numismatics$getItems());
        return nbt;
    }

    public void load(CompoundTag nbt) {
        ContainerHelper.loadAllItems(nbt, ((AccessorSimpleContainer) this).numismatics$getItems());
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
