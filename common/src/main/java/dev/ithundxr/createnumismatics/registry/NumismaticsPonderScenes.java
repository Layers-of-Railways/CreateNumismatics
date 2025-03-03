package dev.ithundxr.createnumismatics.registry;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class NumismaticsPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
//        HELPER.forComponents(CRBlocks.SEMAPHORE)
//                .addStoryBoard("semaphore", TrainScenes::signaling);
//        HELPER.forComponents(CRBlocks.TRACK_COUPLER)
//                .addStoryBoard("coupler", TrainScenes::coupling);
//        HELPER.forComponents(CRItems.ITEM_CONDUCTOR_CAP.values())
//                .addStoryBoard("conductor", ConductorScenes::constructing)
//                .addStoryBoard("conductor_redstone", ConductorScenes::redstoning)
//                .addStoryBoard("conductor", ConductorScenes::toolboxing);
//        HELPER.forComponents(
//                        AllBlocks.ANDESITE_DOOR,
//                        AllBlocks.BRASS_DOOR,
//                        AllBlocks.COPPER_DOOR,
//                        AllBlocks.TRAIN_DOOR,
//                        AllBlocks.FRAMED_GLASS_DOOR
//                )
//                .addStoryBoard("door_modes", DoorScenes::modes);
//        HELPER.forComponents(CRBlocks.ANDESITE_SWITCH, CRBlocks.BRASS_SWITCH)
//                .addStoryBoard("switch", TrainScenes::trackSwitch);
    }
}
