package dev.ithundxr.createnumismatics.base.data.fabric;

import com.simibubi.create.Create;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.ithundxr.createnumismatics.content.bank.BankTerminalBlock;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlock;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;

public class BuilderTransformersImpl {
    public static <B extends AbstractDepositorBlock<?>, P> NonNullUnaryOperator<BlockBuilder<B, P>> depositor(String material) {
        return a -> a.blockstate((c, p) -> p.getVariantBuilder(c.get())
            .forAllStatesExcept((state) -> ConfiguredModel.builder()
                    .modelFile(p.models()
                        .orientable(c.getName() + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : ""),
                            Create.asResource("block/" + material + "_casing"),
                            p.modLoc("block/depositor/" + material + "_depositor_slot" + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : "")),
                            p.modLoc("block/depositor/" + material + "_depositor_select"))
                        .texture("particle", Create.asResource("block/" + material + "_casing"))
                    )
                    .rotationY((int) state.getValue(AbstractDepositorBlock.HORIZONTAL_FACING).toYRot() + 180)
                    .build(),
                AbstractDepositorBlock.POWERED
            )
        );
    }

    public static <B extends BankTerminalBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> bankTerminal() {
        return a -> a.blockstate((c, p) -> p.getVariantBuilder(c.get())
            .forAllStates((state) -> ConfiguredModel.builder()
                .modelFile(p.models()
                    .orientable(c.getName(), p.modLoc("block/bank_terminal/bank_terminal_side"),
                        p.modLoc("block/bank_terminal/bank_terminal_front"),
                        p.modLoc("block/bank_terminal/bank_terminal_top"))
                    .texture("particle", p.modLoc("block/bank_terminal/bank_terminal_top"))
                )
                .rotationY((int) state.getValue(BankTerminalBlock.HORIZONTAL_FACING).toYRot() + 180)
                .build()
            )
        );
    }

    public static <B extends BlazeBankerBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> blazeBanker() {
        return a -> a.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
                .getExistingFile(Create.asResource("block/blaze_burner/block"))
            )
        );
    }
}
