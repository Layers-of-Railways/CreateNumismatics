package dev.ithundxr.createnumismatics.fabric;

import dev.ithundxr.createnumismatics.NumismaticsClient;
import net.fabricmc.api.ClientModInitializer;

public class NumismaticsClientImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NumismaticsClient.init();
    }
}
