package dev.ithundxr.createnumismatics.content.vendor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VendorRenderer implements BlockEntityRenderer<VendorBlockEntity> {
    public VendorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull VendorBlockEntity be, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack = be.getSellingItem();
        float age = AnimationTickHolder.getRenderTime();
        float yHeight = 0.65F;

        if (itemStack.getItem() instanceof BlockItem) {
            yHeight = 0.6F;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, yHeight, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age % 360.0F));
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.GROUND, packedLight, packedOverlay,
                poseStack, buffer, be.getLevel(), 0);
        poseStack.popPose();
    }
}
