package dev.ithundxr.createnumismatics.registry;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerEditPacket;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.registry.packets.*;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.BasePacketPayload.PacketTypeProvider;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum NumismaticsPackets implements PacketTypeProvider {
    // C2S
    SLIDER_STYLE_PRICE_CONFIGURATION(SliderStylePriceConfigurationPacket.class, SliderStylePriceConfigurationPacket.STREAM_CODEC),
    BLAZE_BANKER_EDIT(BlazeBankerEditPacket.class, BlazeBankerEditPacket.STREAM_CODEC),
    ANDESITE_DEPOSITOR_CONFIGURATION(AndesiteDepositorConfigurationPacket.class, AndesiteDepositorConfigurationPacket.STREAM_CODEC),
    OPEN_TRUST_LIST(OpenTrustListPacket.class, OpenTrustListPacket.STREAM_CODEC),
    VENDOR_CONFIGURATION(VendorConfigurationPacket.class, VendorConfigurationPacket.STREAM_CODEC),
    
    // S2C
    BANK_ACCOUNT_LABEL(BankAccountLabelPacket.class, BankAccountLabelPacket.STREAM_CODEC),
    VAR_INT_CONTAINER_SET_DATA(VarIntContainerSetDataPacket.class, VarIntContainerSetDataPacket.STREAM_CODEC),
    
    VENDOR_CONTAINER_SET_SLOT(VendorContainerSetSlotPacket.class, VendorContainerSetSlotPacket.STREAM_CODEC),
    VENDOR_CONTAINER_SET_CONTENT(VendorContainerSetContentPacket.class, VendorContainerSetContentPacket.STREAM_CODEC);

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> NumismaticsPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(Numismatics.asResource(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(Numismatics.MOD_ID, 2); // increment version on changes
        for (NumismaticsPackets packet : NumismaticsPackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }
}
