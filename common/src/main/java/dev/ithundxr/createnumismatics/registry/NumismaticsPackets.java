package dev.ithundxr.createnumismatics.registry;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerEditPacket;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.multiloader.PacketSet;
import dev.ithundxr.createnumismatics.registry.packets.AndesiteDepositorConfigurationPacket;
import dev.ithundxr.createnumismatics.registry.packets.BankAccountLabelPacket;
import dev.ithundxr.createnumismatics.registry.packets.OpenTrustListPacket;
import dev.ithundxr.createnumismatics.registry.packets.VarIntContainerSetDataPacket;

public class NumismaticsPackets {
    public static final PacketSet PACKETS = PacketSet.builder(Numismatics.MOD_ID, 1) // increment version on changes (keep on version 1 until first release)

        .c2s(SliderStylePriceConfigurationPacket.class, SliderStylePriceConfigurationPacket::new)
        .c2s(BlazeBankerEditPacket.class, BlazeBankerEditPacket::new)
        .c2s(AndesiteDepositorConfigurationPacket.class, AndesiteDepositorConfigurationPacket::new)
        .c2s(OpenTrustListPacket.class, OpenTrustListPacket::new)

        .s2c(BankAccountLabelPacket.class, BankAccountLabelPacket::new)
        .s2c(VarIntContainerSetDataPacket.class, VarIntContainerSetDataPacket::new)

        .build();
}
