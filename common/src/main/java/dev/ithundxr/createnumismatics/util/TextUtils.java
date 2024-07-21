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

package dev.ithundxr.createnumismatics.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class TextUtils {
    public static String titleCaseConversion(String inputString)
    {
        if (StringUtils.isBlank(inputString)) {
            return "";
        }

        if (StringUtils.length(inputString) == 1) {
            return inputString.toUpperCase();
        }

        inputString = inputString.replaceAll("_", " ");

        StringBuffer resultPlaceHolder = new StringBuffer(inputString.length());

        Stream.of(inputString.split(" ")).forEach(stringPart ->
        {
            if (stringPart.length() > 1)
                resultPlaceHolder.append(stringPart.substring(0, 1)
                        .toUpperCase())
                    .append(stringPart.substring(1)
                        .toLowerCase());
            else
                resultPlaceHolder.append(stringPart.toUpperCase());

            resultPlaceHolder.append(" ");
        });
        return StringUtils.trim(resultPlaceHolder.toString());
    }

    public static void renderMultilineDebugText(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                                double baseY, boolean transparent, String... lines) {
        double y = baseY + (lines.length/4.0D);
        for (String line : lines) {
            renderDebugText(poseStack, buffer, packedLight, y, transparent, line);
            y -= 0.25D;
        }
    }

    public static void renderDebugText(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight,
                                       double y, boolean transparent, String text) {
        poseStack.pushPose();
        poseStack.translate(0.0D, y, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getBlockEntityRenderDispatcher().camera.rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = poseStack.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;
        Font font = Minecraft.getInstance().font;
        float f2 = (float)(-font.width(text) / 2);
//        font.drawInBatch(text, f2, 0, 553648127, false, matrix4f, pBuffer, transparent, j, pPackedLight);
//
//        if (transparent) {
//            font.drawInBatch(text, f2, 0, -1, false, matrix4f, pBuffer, false, 0, pPackedLight);
//        }
        font.drawInBatch(text, f2, 0, 553648127, false, matrix4f, pBuffer, transparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, pPackedLight);

        if (transparent) {
            font.drawInBatch(text, f2, 0, -1, false, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0, pPackedLight);
        }

        poseStack.popPose();
    }

    public static Component translateWithFormatting(String key, Object... args) {
        MutableComponent base = Components.translatable(key, args);
        StringBuilder partsStringBuilder = new StringBuilder();
        base.visit((style, part) -> {
            partsStringBuilder.append(part);
            return Optional.empty();
        }, Style.EMPTY);
        return Components.literal(partsStringBuilder.toString());
    }

    public static String formatInt(int num) {
        return formatInt(num, ",");
    }

    public static String formatInt(int num, String separator) {
        String raw = String.valueOf(num);
        if (raw.length() <= 3)
            return raw;

        int start = raw.length() % 3;
        StringBuilder out = new StringBuilder(raw.length() + (raw.length()/3) * separator.length());
        out.append(raw, 0, start);
        for (int i = 0; i < raw.length() / 3; i++) {
            if (i != 0 || start != 0)
                out.append(separator);
            out.append(raw, i*3 + start, i*3 + start + 3);
        }
        return out.toString();
    }

    public static String leftPad(String s, char c, int width) {
        if (s.length() >= width) return s;
        return String.valueOf(c).repeat(width - s.length()) + s;
    }

    public static boolean isLeftToRight() {
        return Components.translatable("numismatics.special.ltr")
            .getString()
            .toLowerCase(Locale.ROOT)
            .equals("true");
    }
}
