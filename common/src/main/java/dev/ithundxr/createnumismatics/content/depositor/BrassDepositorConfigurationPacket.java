package dev.ithundxr.createnumismatics.content.depositor;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.packets.BlockEntityConfigurationPacket;
import net.minecraft.network.FriendlyByteBuf;

public class BrassDepositorConfigurationPacket extends BlockEntityConfigurationPacket<BrassDepositorBlockEntity> {
    private int[] prices;
    public BrassDepositorConfigurationPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public BrassDepositorConfigurationPacket(BrassDepositorBlockEntity brassDepositorBE) {
        super(brassDepositorBE.getBlockPos());
        this.prices = new int[Coin.values().length];
        for (Coin coin : Coin.values()) {
            this.prices[coin.ordinal()] = brassDepositorBE.getPrice(coin);
        }
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        for (int price : prices) {
            buffer.writeVarInt(price);
        }
    }

    @Override
    protected void readSettings(FriendlyByteBuf buf) {
        prices = new int[Coin.values().length];
        for (int i = 0; i < prices.length; i++) {
            prices[i] = buf.readVarInt();
        }
    }

    @Override
    protected void applySettings(BrassDepositorBlockEntity brassDepositorBE) {
        for (Coin coin : Coin.values()) {
            brassDepositorBE.setPrice(coin, prices[coin.ordinal()]);
        }
    }
}
