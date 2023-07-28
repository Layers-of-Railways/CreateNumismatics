package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import dev.ithundxr.createnumismatics.registry.packets.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class BlazeBankerEditPacket extends BlockEntityConfigurationPacket<BlazeBankerBlockEntity> {
    /*@Nullable
    private Boolean allowExtraction;*/

    @Nullable
    private String label;
    public BlazeBankerEditPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    /*public BlazeBankerEditPacket(BlockPos pos, boolean allowExtraction) {
        super(pos);
        this.allowExtraction = allowExtraction;
    }*/

    public BlazeBankerEditPacket(BlockPos pos, String label) {
        super(pos);
        this.label = label;
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        /*buffer.writeBoolean(allowExtraction != null);
        if (allowExtraction != null) {
            buffer.writeBoolean(allowExtraction);
            return;
        }*/

        buffer.writeBoolean(label != null);
        if (label != null) {
            buffer.writeUtf(label);
            return;
        }
    }

    @Override
    protected void readSettings(FriendlyByteBuf buf) {
        /*if (buf.readBoolean()) {
            allowExtraction = buf.readBoolean();
            return;
        }*/

        if (buf.readBoolean()) {
            label = buf.readUtf(256);
            return;
        }
    }

    @Override
    protected void applySettings(BlazeBankerBlockEntity blazeBankerBlockEntity) {
//        if (allowExtraction != null)
//            blazeBankerBlockEntity.setAllowExtraction(allowExtraction);

        if (label != null)
            blazeBankerBlockEntity.setLabel(label);
    }
}
