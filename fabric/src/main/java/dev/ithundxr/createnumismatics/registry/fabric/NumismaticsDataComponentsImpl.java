package dev.ithundxr.createnumismatics.registry.neoforge;

import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class NumismaticsDataComponentsImpl {
	public static <T> void register(String name, DataComponentType<T> type) {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Numismatics.asResource(name), type);
	}
}