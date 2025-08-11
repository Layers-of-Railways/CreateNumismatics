package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.checkout.CheckoutPaymentMethod;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record DeferredCheckoutResolutionPacket(
        UUID transactionId, CheckoutPaymentMethod method, UUID bankAccount) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, DeferredCheckoutResolutionPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DeferredCheckoutResolutionPacket::transactionId,
            CheckoutPaymentMethod.STREAM_CODEC, DeferredCheckoutResolutionPacket::method,
            UUIDUtil.STREAM_CODEC, DeferredCheckoutResolutionPacket::bankAccount,
            DeferredCheckoutResolutionPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        Numismatics.LOGGER.info("Received checkout packet! Transaction: {}; Method: {}; BankAccount: {}", transactionId, method, bankAccount);
        var order = Numismatics.DEFERRED_ORDERS.getDeferredOrder(transactionId);
        if (order == null)
            return;

        order.completePurchase(method, bankAccount);
        Numismatics.DEFERRED_ORDERS.voidOrder(order);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.DEFERRED_CHECKOUT_RESOLUTION;
    }
}
