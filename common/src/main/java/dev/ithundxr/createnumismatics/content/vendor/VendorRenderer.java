package dev.ithundxr.createnumismatics.content.vendor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public class VendorRenderer implements BlockEntityRenderer<VendorBlockEntity> {
    public VendorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull VendorBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
    }
}
