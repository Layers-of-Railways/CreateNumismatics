package dev.ithundxr.createnumismatics.registry.neoforge;

import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NumismaticsDataComponentsImpl {
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Numismatics.MOD_ID);

	public static <T> void register(String name, DataComponentType<T> type) {
		DATA_COMPONENTS.register(name, () -> type);
	}
	
	public static void register(IEventBus modEventBus) {
		DATA_COMPONENTS.register(modEventBus);
	}
}
