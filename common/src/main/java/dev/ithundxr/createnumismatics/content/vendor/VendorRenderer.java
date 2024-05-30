/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
