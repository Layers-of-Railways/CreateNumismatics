package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlockEntity;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;

public class NumismaticsBlockEntities {
    private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

/*    public static final BlockEntityEntry<SemaphoreBlockEntity> SEMAPHORE = REGISTRATE.blockEntity("semaphore", SemaphoreBlockEntity::new)
        .validBlocks(CRBlocks.SEMAPHORE)
        .renderer(() -> SemaphoreRenderer::new)
        .register();*/

    public static final BlockEntityEntry<AndesiteDepositorBlockEntity> ANDESITE_DEPOSITOR = REGISTRATE.blockEntity("andesite_depositor", AndesiteDepositorBlockEntity::new)
        .validBlocks(NumismaticsBlocks.ANDESITE_DEPOSITOR)
        .register();

    public static final BlockEntityEntry<BrassDepositorBlockEntity> BRASS_DEPOSITOR = REGISTRATE.blockEntity("brass_depositor", BrassDepositorBlockEntity::new)
        .validBlocks(NumismaticsBlocks.BRASS_DEPOSITOR)
        .register();

    public static final BlockEntityEntry<BlazeBankerBlockEntity> BLAZE_BANKER = REGISTRATE.blockEntity("blaze_banker", BlazeBankerBlockEntity::new)
        .validBlocks(NumismaticsBlocks.BLAZE_BANKER)
        .register();


    @SuppressWarnings("EmptyMethod")
    public static void register() {}
}
