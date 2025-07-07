package dev.ithundxr.createnumismatics.registry;

import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class NumismaticsDataComponents {
	public static final DataComponentType<UUID> ID_CARD_UUID = register(
		"id_card_uuid",
		b -> b.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC)
	);

	public static final DataComponentType<Integer> COIN_DISPLAYED_COUNT = register(
		"coin_displayed_count",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
	);

	public static final DataComponentType<UUID> CARD_ACCOUNT_ID = register(
		"card_account_id",
		b -> b.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC)
	);

	private static <T> DataComponentType<T> register(String name, UnaryOperator<Builder<T>> builder) {
		DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		register(name, type);
		return type;
	}

	@ExpectPlatform
	public static <T> void register(String name, DataComponentType<T> type) {
		throw new AssertionError();
	}
	
	public static void register() {
		Numismatics.LOGGER.info("Registering data components for " + Numismatics.NAME);
	}
}
