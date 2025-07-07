package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record VarIntContainerSetDataPacket(int containerId, int id, int value) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, VarIntContainerSetDataPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, i -> (byte) i.containerId,
        ByteBufCodecs.SHORT, i -> (short) i.id,
        ByteBufCodecs.VAR_INT, VarIntContainerSetDataPacket::value,
		(containerId, id, value) -> new VarIntContainerSetDataPacket(containerId, id, value)
    );

    @Environment(EnvType.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        // IntelliJ falsely things that player.containerMenu is never null
        //noinspection ConstantValue
        if (player.containerMenu != null && player.containerMenu.containerId == containerId) {
            player.containerMenu.setData(id, value);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.VAR_INT_CONTAINER_SET_DATA;
    }
}
