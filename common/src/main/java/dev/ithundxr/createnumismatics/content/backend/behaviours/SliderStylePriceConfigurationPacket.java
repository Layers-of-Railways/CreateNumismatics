package dev.ithundxr.createnumismatics.content.backend.behaviours;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.packets.BlockEntityBehaviourConfigurationPacket;
import net.minecraft.network.FriendlyByteBuf;

public class SliderStylePriceConfigurationPacket extends BlockEntityBehaviourConfigurationPacket<SliderStylePriceBehaviour> {
    private int[] prices;
    public SliderStylePriceConfigurationPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    protected BehaviourType<SliderStylePriceBehaviour> getType() {
        return SliderStylePriceBehaviour.TYPE;
    }

    public SliderStylePriceConfigurationPacket(SyncedBlockEntity be) {
        super(be.getBlockPos());
        this.prices = new int[Coin.values().length];

        SliderStylePriceBehaviour priceBehaviour = BlockEntityBehaviour.get(be, getType());
        for (Coin coin : Coin.values()) {
            this.prices[coin.ordinal()] = priceBehaviour.getPrice(coin);
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
    protected void applySettings(SliderStylePriceBehaviour priceBehaviour) {
        for (Coin coin : Coin.values()) {
            priceBehaviour.setPrice(coin, prices[coin.ordinal()]);
        }
    }
}
