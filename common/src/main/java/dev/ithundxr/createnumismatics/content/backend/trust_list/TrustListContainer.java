package dev.ithundxr.createnumismatics.content.backend.trust_list;

import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
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
        for (ItemStack stack : items) {
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
        ContainerHelper.saveAllItems(nbt, items);
        return nbt;
    }

    public void load(CompoundTag nbt) {
        ContainerHelper.loadAllItems(nbt, items);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
