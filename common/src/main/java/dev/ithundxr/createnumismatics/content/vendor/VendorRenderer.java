package dev.ithundxr.createnumismatics.content.vendor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class VendorRenderer implements BlockEntityRenderer<VendorBlockEntity> {
    public VendorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull VendorBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float age = AnimationTickHolder.getRenderTime() + blockEntity.hashCode();

        ItemStack itemStack = new ItemStack(Items.DIAMOND);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.7F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age % 360.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), 0);
        poseStack.popPose();
    }
}
