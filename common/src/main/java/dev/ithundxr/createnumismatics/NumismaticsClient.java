package dev.ithundxr.createnumismatics;

import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.NumismaticsPartialModels;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NumismaticsClient {

    public final static Map<UUID, String> bankAccountLabels = new HashMap<>();

    public static void init() {
        NumismaticsPackets.PACKETS.registerS2CListener();

        NumismaticsPartialModels.init();
    }
}
