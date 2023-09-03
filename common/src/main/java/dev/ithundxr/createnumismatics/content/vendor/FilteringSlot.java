package dev.ithundxr.createnumismatics.content.vendor;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FilteringSlot extends Slot {

    protected final Predicate<ItemStack> accepts;

    public FilteringSlot(Container container, int slot, int x, int y, Predicate<ItemStack> accepts) {
        super(container, slot, x, y);
        this.accepts = accepts;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return super.mayPlace(stack) && accepts.test(stack);
    }
}
