package dev.ithundxr.createnumismatics.content.checkout;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.StreamCodec;

public enum CheckoutPaymentMethod {
    CANCEL_TRANSACTION,
    CARD,
    COINS;

    public static final StreamCodec<ByteBuf, CheckoutPaymentMethod> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(CheckoutPaymentMethod.class);

}
