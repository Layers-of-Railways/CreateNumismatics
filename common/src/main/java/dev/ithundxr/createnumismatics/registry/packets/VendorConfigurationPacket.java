package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;

public class VendorConfigurationPacket extends BlockEntityConfigurationPacket<VendorBlockEntity> {
    private VendorBlockEntity.Mode mode;

    public VendorConfigurationPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public VendorConfigurationPacket(VendorBlockEntity be) {
        super(be.getBlockPos());
        mode = be.getMode();
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeEnum(mode);
    }

    @Override
    protected void readSettings(FriendlyByteBuf buf) {
        mode = buf.readEnum(VendorBlockEntity.Mode.class);
    }

    @Override
    protected void applySettings(VendorBlockEntity vendorBlockEntity) {
        vendorBlockEntity.setMode(mode);
    }
}
