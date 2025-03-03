package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.ithundxr.createnumismatics.registry.NumismaticsPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlazeBankerRenderer extends SafeBlockEntityRenderer<BlazeBankerBlockEntity> {

	public BlazeBankerRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(BlazeBankerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource,
		int light, int overlay) {

		Level level = be.getLevel();
		BlockState blockState = be.getBlockState();
		float animation = be.headAnimation.getValue(partialTicks) * .175f;
		float horizontalAngle = AngleHelper.rad(be.headAngle.getValue(partialTicks));
		int hashCode = be.hashCode();

		renderShared(ms, bufferSource,
			level, blockState, animation, horizontalAngle,
			hashCode);
	}

	private static void renderShared(PoseStack ms, MultiBufferSource bufferSource,
									 Level level, BlockState blockState, float animation, float horizontalAngle,
									 int hashCode) {

		boolean blockAbove = animation > 0.125f;
		float time = AnimationTickHolder.getRenderTime(level);
		float renderTick = time + (hashCode % 13) * 16f;
		float offsetMult = HeatLevel.KINDLED.isAtLeast(HeatLevel.FADING) ? 64 : 16;
		float offset = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
		float offset1 = Mth.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
		float offset2 = Mth.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;
		float headY = offset - (animation * .75f);

		VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
		VertexConsumer cutout = bufferSource.getBuffer(RenderType.cutoutMipped());

		ms.pushPose();

		if (blockAbove) {
			SpriteShiftEntry spriteShift = AllSpriteShifts.BURNER_FLAME;

			float spriteWidth = spriteShift.getTarget()
				.getU1()
				- spriteShift.getTarget()
					.getU0();

			float spriteHeight = spriteShift.getTarget()
				.getV1()
				- spriteShift.getTarget()
					.getV0();

			float speed = 1 / 32f + 1 / 64f * HeatLevel.KINDLED.ordinal();

			double vScroll = speed * time;
			vScroll = vScroll - Math.floor(vScroll);
			vScroll = vScroll * spriteHeight / 2;

			double uScroll = speed * time / 2;
			uScroll = uScroll - Math.floor(uScroll);
			uScroll = uScroll * spriteWidth / 2;

			SuperByteBuffer flameBuffer = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState);
            flameBuffer.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll);
			draw(flameBuffer, horizontalAngle, ms, cutout);
		}

		PartialModel blazeModel = blockAbove ? AllPartialModels.BLAZE_ACTIVE : AllPartialModels.BLAZE_IDLE;

        SuperByteBuffer blazeBuffer = CachedBuffers.partial(blazeModel, blockState);
        blazeBuffer.translate(0, headY, 0);
		draw(blazeBuffer, horizontalAngle, ms, solid);

        {
			SuperByteBuffer hatBuffer = CachedBuffers.partial(NumismaticsPartialModels.TOP_HAT, blockState);
            hatBuffer.translate(0, headY, 0);
            hatBuffer.translateY(0.75f);
            hatBuffer
				.rotateCentered(horizontalAngle + Mth.PI, Direction.UP)
				.translate(0.5f, 0, 0.5f)
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, solid);
		}

		{
			PartialModel rodsModel = AllPartialModels.BLAZE_BURNER_RODS;
			PartialModel rodsModel2 = AllPartialModels.BLAZE_BURNER_RODS_2;

			SuperByteBuffer rodsBuffer = CachedBuffers.partial(rodsModel, blockState);
            rodsBuffer.translate(0, offset1 + animation + .125f, 0)
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, solid);

			SuperByteBuffer rodsBuffer2 = CachedBuffers.partial(rodsModel2, blockState);
            rodsBuffer2.translate(0, offset2 + animation - 3 / 16f, 0)
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, solid);
		}

		ms.popPose();
	}

	private static void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ms, VertexConsumer vc) {
		buffer.rotateCentered(horizontalAngle, Direction.UP)
			.light(LightTexture.FULL_BRIGHT)
			.renderInto(ms, vc);
	}
}
