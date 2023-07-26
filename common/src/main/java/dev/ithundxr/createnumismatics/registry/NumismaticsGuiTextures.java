package dev.ithundxr.createnumismatics.registry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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

    ANDESITE_DEPOSITOR("andesite_depositor", 182, 44),
    BRASS_DEPOSITOR("brass_depositor", 182, 145),
    BANK_TERMINAL("bank_terminal",200, 132),
    TRUST_LIST("trust_list",200, 110)
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
