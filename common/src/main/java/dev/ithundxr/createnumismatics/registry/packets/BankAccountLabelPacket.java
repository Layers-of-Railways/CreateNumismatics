package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.NumismaticsClient;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record BankAccountLabelPacket(UUID id, @Nullable String label) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, BankAccountLabelPacket> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, BankAccountLabelPacket::id,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.STRING_UTF8), BankAccountLabelPacket::label,
        BankAccountLabelPacket::new
    );

    @Environment(EnvType.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        if (label == null) {
            NumismaticsClient.bankAccountLabels.remove(id);
        } else {
            NumismaticsClient.bankAccountLabels.put(id, label);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.BANK_ACCOUNT_LABEL;
    }
}
