package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;

public class AndesiteDepositorConfigurationPacket extends BlockEntityConfigurationPacket<AndesiteDepositorBlockEntity> {

    private Coin coin;

    public AndesiteDepositorConfigurationPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public AndesiteDepositorConfigurationPacket(AndesiteDepositorBlockEntity be) {
        super(be.getBlockPos());
        this.coin = be.getCoin();
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeEnum(coin);
    }

    @Override
    protected void readSettings(FriendlyByteBuf buf) {
        coin = buf.readEnum(Coin.class);
    }

    @Override
    protected void applySettings(AndesiteDepositorBlockEntity andesiteDepositorBlockEntity) {
        andesiteDepositorBlockEntity.setCoin(coin);
    }
}
