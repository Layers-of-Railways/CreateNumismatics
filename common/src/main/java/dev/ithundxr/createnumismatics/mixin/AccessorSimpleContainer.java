package dev.ithundxr.createnumismatics.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleContainer.class)
public interface AccessorSimpleContainer {
    @Accessor("items")
    NonNullList<ItemStack> numismatics$getItems();
}
