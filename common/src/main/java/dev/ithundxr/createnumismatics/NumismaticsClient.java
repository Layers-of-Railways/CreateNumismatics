package dev.ithundxr.createnumismatics;

import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;

public class NumismaticsClient {
    public static void init() {
        NumismaticsPackets.PACKETS.registerS2CListener();
    }
}
