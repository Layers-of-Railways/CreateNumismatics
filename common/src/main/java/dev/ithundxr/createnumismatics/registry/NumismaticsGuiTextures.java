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
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.utility.Color;
import dev.ithundxr.createnumismatics.Numismatics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/*
Copied from Create
 */
public enum NumismaticsGuiTextures implements ScreenElement {

    ANDESITE_DEPOSITOR("andesite_depositor", 182, 79),
    BRASS_DEPOSITOR("brass_depositor", 208, 145),
    BANK_TERMINAL("bank_terminal",200, 132),
    SUB_ACCOUNT_LIST("bank_terminal_sub_account_list",234, 220),
    SUB_ACCOUNT_LIST_DELETE("bank_terminal_sub_account_list", 0, 220, 12, 12),
    SUB_ACCOUNT_LIST_DELETE_COLORABLE("bank_terminal_sub_account_list", 12, 220, 12, 12),
    SUB_ACCOUNT_LIST_RESET("bank_terminal_sub_account_list", 0, 232, 12, 12),
    SUB_ACCOUNT_LIST_EDIT("bank_terminal_sub_account_list", 0, 244, 12, 12),
    VALUE_SETTINGS_LABEL_BG_TALL("bank_terminal_sub_account_list", 12, 242, 81, 14),
    SUB_ACCOUNT_LIST_POPUP("bank_terminal_sub_account_list_popup", 250, 193),
    SUB_ACCOUNT_MODE_TRUSTED_PLAYERS("bank_terminal_sub_account_list_popup", 0, 240, 16, 16),
    SUB_ACCOUNT_MODE_TRUSTED_AUTOMATION("bank_terminal_sub_account_list_popup", 20, 240, 16, 16),
    SUB_ACCOUNT_MODE_ANY("bank_terminal_sub_account_list_popup", 40, 240, 16, 16),
    TRUST_LIST("trust_list",200, 110),
    BLAZE_BANKER("blaze_banker",200, 110),
    VENDOR("vendor", 254, 151),
    CREATIVE_VENDOR("creative_vendor", 254, 151),
    ;

    public static final int FONT_COLOR = 0x575F7A;

    public final ResourceLocation location;
    public int width, height;
    public int startX, startY;

    private NumismaticsGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    private NumismaticsGuiTextures(int startX, int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    private NumismaticsGuiTextures(String location, int startX, int startY, int width, int height) {
        this(Numismatics.MOD_ID, location, startX, startY, width, height);
    }

    private NumismaticsGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @Environment(EnvType.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        bind();
        graphics.blit(location, x, y, 0, startX, startY, width, height, 256, 256);
    }

    @Environment(EnvType.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }
}
