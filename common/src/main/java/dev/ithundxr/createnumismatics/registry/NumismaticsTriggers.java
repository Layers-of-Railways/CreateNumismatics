package dev.ithundxr.createnumismatics.registry;

import dev.ithundxr.createnumismatics.registry.advancement.CriterionTriggerBase;
import dev.ithundxr.createnumismatics.registry.advancement.SimpleNumismaticsTrigger;
import net.minecraft.advancements.CriteriaTriggers;

import java.util.LinkedList;
import java.util.List;

public class NumismaticsTriggers {

	private static final List<CriterionTriggerBase<?>> triggers = new LinkedList<>();

	public static SimpleNumismaticsTrigger addSimple(String id) {
		return add(new SimpleNumismaticsTrigger(id));
	}

	private static <T extends CriterionTriggerBase<?>> T add(T instance) {
		triggers.add(instance);
		return instance;
	}

	public static void register() {
		triggers.forEach(CriteriaTriggers::register);
	}

}
