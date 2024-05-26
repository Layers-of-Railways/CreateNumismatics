package dev.ithundxr.createnumismatics.registry;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerEditPacket;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.multiloader.PacketSet;
import dev.ithundxr.createnumismatics.registry.packets.*;

public class NumismaticsPackets {
    public static final PacketSet PACKETS = PacketSet.builder(Numismatics.MOD_ID, 2) // increment version on changes

        .c2s(SliderStylePriceConfigurationPacket.class, SliderStylePriceConfigurationPacket::new)
        .c2s(BlazeBankerEditPacket.class, BlazeBankerEditPacket::new)
        .c2s(AndesiteDepositorConfigurationPacket.class, AndesiteDepositorConfigurationPacket::new)
        .c2s(OpenTrustListPacket.class, OpenTrustListPacket::new)
        .c2s(VendorConfigurationPacket.class, VendorConfigurationPacket::new)

        .s2c(BankAccountLabelPacket.class, BankAccountLabelPacket::new)
        .s2c(VarIntContainerSetDataPacket.class, VarIntContainerSetDataPacket::new)

        .s2c(VendorContainerSetSlotPacket.class, VendorContainerSetSlotPacket::new)
        .s2c(VendorContainerSetContentPacket.class, VendorContainerSetContentPacket::new)

        .build();
}
