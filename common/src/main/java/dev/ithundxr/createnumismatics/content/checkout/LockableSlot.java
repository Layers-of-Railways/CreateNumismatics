package dev.ithundxr.createnumismatics.content.checkout;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LockableSlot extends Slot {

    private boolean locked;

    public LockableSlot(Container inventory, int invSlot, int x, int y, boolean initiallyLocked) {
        super(inventory, invSlot, x, y);
        locked = initiallyLocked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (locked)
            return false;
        else
            return super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        if (locked)
            return false;
        else
            return super.mayPickup(player);
    }
}