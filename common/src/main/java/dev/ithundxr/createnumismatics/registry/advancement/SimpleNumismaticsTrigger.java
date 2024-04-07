package dev.ithundxr.createnumismatics.registry.advancement;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleNumismaticsTrigger extends CriterionTriggerBase<SimpleNumismaticsTrigger.Instance> {

	public SimpleNumismaticsTrigger(String id) {
		super(id);
	}

	@Override
	public SimpleNumismaticsTrigger.Instance createInstance(JsonObject json, DeserializationContext context) {
		return new SimpleNumismaticsTrigger.Instance(getId());
	}

	public void trigger(ServerPlayer player) {
		super.trigger(player, null);
	}

	public SimpleNumismaticsTrigger.Instance instance() {
		return new SimpleNumismaticsTrigger.Instance(getId());
	}

	public static class Instance extends CriterionTriggerBase.Instance {

		public Instance(ResourceLocation idIn) {
			super(idIn, ContextAwarePredicate.ANY);
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			return true;
		}
	}
}
