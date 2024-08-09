/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.registry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.DelegatedStencilElement;
import com.simibubi.create.foundation.utility.Color;
import dev.ithundxr.createnumismatics.Numismatics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class NumismaticsIcons extends AllIcons {
    public static final ResourceLocation ICON_ATLAS = Numismatics.asResource("textures/gui/icons.png");
    public static final int ICON_ATLAS_SIZE = 256;

    private static int x = 0, y = -1;
    private int iconX;
    private int iconY;
    private boolean isCoin = false;

    /*
    SPUR(1, Rarity.COMMON),
    BEVEL(8, Rarity.COMMON), // 8 spurs
    SPROCKET(16, Rarity.COMMON), // 16 spurs, 2 bevels
    COG(64, Rarity.UNCOMMON), // 64 spurs, 8 bevels, 4 sprockets
    CROWN(512, Rarity.RARE), // 512 spurs, 64 bevels, 32 sprockets, 8 cogs
    SUN(4096, Rarity.EPIC) // 4096 spurs, 512 bevels, 256 sprockets, 64 cogs, 8 crowns
     */

    public static final NumismaticsIcons
        I_COIN_SPUR = newRow(),
        I_COIN_BEVEL = next(),
        I_COIN_SPROCKET = next(),
        I_COIN_COG = next(),
        I_COIN_CROWN = next(),
        I_COIN_SUN = next()
    ;
    public static final NumismaticsIcons
        I_HOPPER = newRow(),
        I_OPEN_SUB_LIST = next(),
        I_SALE_GO = next(),
        I_SALE_ALERT = next(),
        I_SALE_CANCEL = next()
    ;

    public NumismaticsIcons(int x, int y) {
        super(x, y);
        iconX = x * 16;
        iconY = y * 16;
    }

    private static NumismaticsIcons next() {
        return new NumismaticsIcons(++x, y);
    }

    private static NumismaticsIcons newRow() {
        return new NumismaticsIcons(x = 0, ++y);
    }

    @Environment(EnvType.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, ICON_ATLAS);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        if (isCoin) { // essential to make coins look decent in scroll options
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        graphics.blit(ICON_ATLAS, x, y, 0, iconX, iconY, 16, 16, 256, 256);
    }

    @Environment(EnvType.CLIENT)
    public void render(PoseStack ms, MultiBufferSource buffer, int color) {
        VertexConsumer builder = buffer.getBuffer(RenderType.text(ICON_ATLAS));
        Matrix4f matrix = ms.last().pose();
        Color rgb = new Color(color);
        int light = LightTexture.FULL_BRIGHT;

        Vec3 vec1 = new Vec3(0, 0, 0);
        Vec3 vec2 = new Vec3(0, 1, 0);
        Vec3 vec3 = new Vec3(1, 1, 0);
        Vec3 vec4 = new Vec3(1, 0, 0);

        float u1 = iconX * 1f / ICON_ATLAS_SIZE;
        float u2 = (iconX + 16) * 1f / ICON_ATLAS_SIZE;
        float v1 = iconY * 1f / ICON_ATLAS_SIZE;
        float v2 = (iconY + 16) * 1f / ICON_ATLAS_SIZE;

        vertex(builder, matrix, vec1, rgb, u1, v1, light);
        vertex(builder, matrix, vec2, rgb, u1, v2, light);
        vertex(builder, matrix, vec3, rgb, u2, v2, light);
        vertex(builder, matrix, vec4, rgb, u2, v1, light);
    }

    @Environment(EnvType.CLIENT)
    private void vertex(VertexConsumer builder, Matrix4f matrix, Vec3 vec, Color rgb, float u, float v, int light) {
        builder.vertex(matrix, (float) vec.x, (float) vec.y, (float) vec.z)
                .color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255)
                .uv(u, v)
                .uv2(light)
                .endVertex();
    }

    @Environment(EnvType.CLIENT)
    public DelegatedStencilElement asStencil() {
        return new DelegatedStencilElement().withStencilRenderer((ms, w, h, alpha) -> this.render(ms, 0, 0)).withBounds(16, 16);
    }

    public void setCoin() {
        this.isCoin = true;
    }
}
