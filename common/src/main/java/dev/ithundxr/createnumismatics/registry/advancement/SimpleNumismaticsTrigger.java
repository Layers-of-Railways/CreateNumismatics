package dev.ithundxr.createnumismatics.registry.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleNumismaticsTrigger extends CriterionTriggerBase<SimpleNumismaticsTrigger.Instance> {

	public SimpleNumismaticsTrigger(String id) {
		super(id);
	}

	public void trigger(ServerPlayer player) {
		super.trigger(player, null);
	}

	public SimpleNumismaticsTrigger.Instance instance() {
		return new SimpleNumismaticsTrigger.Instance();
	}

	@Override
	public Codec<SimpleNumismaticsTrigger.Instance> codec() {
		return SimpleNumismaticsTrigger.Instance.CODEC;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static class Instance extends CriterionTriggerBase.Instance {
		private static final Codec<SimpleNumismaticsTrigger.Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SimpleNumismaticsTrigger.Instance::player)
		).apply(instance, SimpleNumismaticsTrigger.Instance::new));

		private final Optional<ContextAwarePredicate> player;

		public Instance() {
			player = Optional.empty();
		}

		public Instance(Optional<ContextAwarePredicate> player) {
			this.player = player;
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			return true;
		}

		@Override
		public Optional<ContextAwarePredicate> player() {
			return player;
		}
	}
}
