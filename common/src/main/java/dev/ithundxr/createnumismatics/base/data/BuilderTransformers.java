package dev.ithundxr.createnumismatics.base.data;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.content.bank.BankTerminalBlock;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlock;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlock;
import net.minecraft.world.item.BlockItem;

public class BuilderTransformers {
    @ExpectPlatform
    public static <B extends AbstractDepositorBlock<?>, P> NonNullUnaryOperator<BlockBuilder<B, P>> depositor(String material) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <B extends BankTerminalBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> bankTerminal() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <B extends BlazeBankerBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> blazeBanker() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <B extends VendorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> vendor(boolean creative) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <I extends BlockItem, P> NonNullUnaryOperator<ItemBuilder<I, P>> vendorItem(boolean creative) {
        throw new AssertionError();
    }
}
