package dev.ithundxr.createnumismatics.content.bank;

import com.mojang.datafixers.util.Pair;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardSlot extends Slot {
    public CardSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return NumismaticsTags.AllItemTags.CARDS.matches(stack);
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, Numismatics.asResource("item/card/outline"));
    }

    public static class UnboundCardSlot extends CardSlot {
        public UnboundCardSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return super.mayPlace(stack) && !CardItem.isBound(stack);
        }
    }
}
