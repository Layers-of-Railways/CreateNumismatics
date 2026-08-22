package dev.ithundxr.createnumismatics.registry;

import dev.ithundxr.createnumismatics.Numismatics;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class NumismaticsPonderPlugin implements PonderPlugin {
	@Override
	public String getModId() {
		return Numismatics.MOD_ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		NumismaticsPonderScenes.register(helper);
	}
}