package dev.ithundxr.createnumismatics.base.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiBlockEntityRenderBuilder<BE extends BlockEntity> extends GuiGameElement.GuiRenderBuilder {
    private final BE blockEntity;

    protected GuiBlockEntityRenderBuilder(BE blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void render(GuiGraphics graphics) {
        PoseStack matrixStack = graphics.pose();
        prepareMatrix(matrixStack);
        transformMatrix(matrixStack);

        Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 100.0F);
        matrixStack.translate(16.0F, -16.0F, 0.0F);
        matrixStack.scale(16.0F, 16.0F, 16.0F);

//        matrixStack.mulPose(Axis.YP.rotationDegrees(45));

        Minecraft.getInstance().getItemRenderer().getModel(NumismaticsBlocks.BLAZE_BANKER.asStack(), null, null, 0).getTransforms().getTransform(ItemDisplayContext.GUI).apply(false, matrixStack);

        Lighting.setupFor3DItems();

        RenderSystem.enableDepthTest();
        Minecraft.getInstance().getBlockEntityRenderDispatcher().render(blockEntity, AnimationTickHolder.getPartialTicks(),
            matrixStack, graphics.bufferSource());
        RenderSystem.disableDepthTest();
        RenderSystem.enableDepthTest();
        matrixStack.popPose();

        cleanUpMatrix(matrixStack);
    }

    public static <BE extends BlockEntity> GuiBlockEntityRenderBuilder<BE> of(BE blockEntity) {
        return new GuiBlockEntityRenderBuilder<>(blockEntity);
    }
}
