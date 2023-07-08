package dev.ithundxr.createnumismatics.registry;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorConfigurationPacket;
import dev.ithundxr.createnumismatics.multiloader.PacketSet;

public class NumismaticsPackets {
    public static final PacketSet PACKETS = PacketSet.builder(Numismatics.MOD_ID, 1) // increment version on changes (keep on version 1 until first release)

        .c2s(BrassDepositorConfigurationPacket.class, BrassDepositorConfigurationPacket::new)

        .build();
}
