package dev.ithundxr.createnumismatics.registry.advancement;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsAdvancements;
import dev.ithundxr.createnumismatics.registry.NumismaticsTriggers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class NumismaticsAdvancement {

	static final ResourceLocation BACKGROUND = Numismatics.asResource("textures/gui/advancements.png");
	static final String LANG = "advancement." + Numismatics.MOD_ID + ".";
	static final String SECRET_SUFFIX = "\n\u00A77(Hidden Advancement)";

	private Advancement.Builder builder;
	private SimpleNumismaticsTrigger builtinTrigger;
	private NumismaticsAdvancement parent;

	AdvancementHolder datagenResult;

	private String id;
	private String title;
	private String description;

	public NumismaticsAdvancement(String id, UnaryOperator<Builder> b) {
		this.builder = Advancement.Builder.advancement();
		this.id = id;

		Builder t = new Builder();
		b.apply(t);

		if (!t.externalTrigger) {
			builtinTrigger = NumismaticsTriggers.addSimple(id + "_builtin");
			builder.addCriterion("0", builtinTrigger.createCriterion(builtinTrigger.instance()));
		}

		builder.display(t.icon, Component.translatable(titleKey()),
			Component.translatable(descriptionKey()).withStyle(s -> s.withColor(0xDBA213)),
			id.equals("root") ? BACKGROUND : null, t.type.type, t.type.toast, t.type.announce, t.type.hide);

		if (t.type == TaskType.SECRET)
			description += SECRET_SUFFIX;

		NumismaticsAdvancements.ENTRIES.add(this);
	}

	private String titleKey() {
		return LANG + id;
	}

	private String descriptionKey() {
		return titleKey() + ".desc";
	}

	public boolean isAlreadyAwardedTo(Player player) {
		if (!(player instanceof ServerPlayer sp))
			return true;
		AdvancementHolder advancement = sp.getServer()
				.getAdvancements()
				.get(Numismatics.asResource(id));
		if (advancement == null)
			return true;
		return sp.getAdvancements()
				.getOrStartProgress(advancement)
				.isDone();
	}

	public void awardTo(Player player) {
		if (!(player instanceof ServerPlayer sp))
			return;
		if (builtinTrigger == null)
			throw new UnsupportedOperationException(
				"Advancement " + id + " uses external Triggers, it cannot be awarded directly");
		builtinTrigger.trigger(sp);
	}

	@ApiStatus.Internal
	public void save(Consumer<AdvancementHolder> t) {
		if (parent != null)
			builder.parent(parent.datagenResult);
		datagenResult = builder.save(t, Numismatics.asResource(id)
			.toString());
	}

	@ApiStatus.Internal
	public void provideLang(BiConsumer<String, String> consumer) {
		consumer.accept(titleKey(), title);
		consumer.accept(descriptionKey(), description);
	}

	@ApiStatus.Internal
	public enum TaskType {

		SILENT(AdvancementType.TASK, false, false, false),
		NORMAL(AdvancementType.TASK, true, false, false),
		NOISY(AdvancementType.TASK, true, true, false),
		EXPERT(AdvancementType.GOAL, true, true, false),
		SECRET(AdvancementType.GOAL, true, true, true),

		;

		private AdvancementType type;
		private boolean toast;
		private boolean announce;
		private boolean hide;

		TaskType(AdvancementType type, boolean toast, boolean announce, boolean hide) {
			this.type = type;
			this.toast = toast;
			this.announce = announce;
			this.hide = hide;
		}
	}

	@ApiStatus.Internal
	public class Builder {

		private TaskType type = TaskType.NORMAL;
		private boolean externalTrigger;
		private int keyIndex;
		private ItemStack icon;

		@ApiStatus.Internal
		public Builder special(TaskType type) {
			this.type = type;
			return this;
		}

		@ApiStatus.Internal
		public Builder after(NumismaticsAdvancement other) {
			NumismaticsAdvancement.this.parent = other;
			return this;
		}

		@ApiStatus.Internal
		public Builder icon(ItemProviderEntry<?, ?> item) {
			return icon(item.asStack());
		}

		@ApiStatus.Internal
		public Builder icon(ItemLike item) {
			return icon(new ItemStack(item));
		}

		@ApiStatus.Internal
		public Builder icon(ItemStack stack) {
			icon = stack;
			return this;
		}

		@ApiStatus.Internal
		public Builder title(String title) {
			NumismaticsAdvancement.this.title = title;
			return this;
		}

		@ApiStatus.Internal
		public Builder description(String description) {
			NumismaticsAdvancement.this.description = description;
			return this;
		}

		@ApiStatus.Internal
		Builder whenBlockPlaced(Block block) {
			return externalTrigger(ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block));
		}

		@ApiStatus.Internal
		Builder whenIconCollected() {
			return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(icon.getItem()));
		}

		@ApiStatus.Internal
		public Builder whenItemCollected(ItemProviderEntry<?, ?> item) {
			return whenItemCollected(item.asStack()
				.getItem());
		}

		@ApiStatus.Internal
		public Builder whenItemCollected(ItemLike itemProvider) {
			return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(itemProvider));
		}

		@ApiStatus.Internal
		public Builder whenItemCollected(TagKey<Item> tag) {
			return externalTrigger(InventoryChangeTrigger.TriggerInstance
					.hasItems(ItemPredicate.Builder.item().of(tag).build()));
		}

		@ApiStatus.Internal
		public Builder awardedForFree() {
			return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[] {}));
		}

		@ApiStatus.Internal
		public Builder externalTrigger(Criterion<?> trigger) {
			builder.addCriterion(String.valueOf(keyIndex), trigger);
			externalTrigger = true;
			keyIndex++;
			return this;
		}

	}

}
