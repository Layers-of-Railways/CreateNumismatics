/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.ponder.utils.dev_export;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.annotation.mixin.StripFromRelease;
import dev.ithundxr.createnumismatics.mixin.client.dev_export.AccessorMinecraft;
import dev.ithundxr.createnumismatics.mixin.client.dev_export.AccessorPonderUI;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.mojang.blaze3d.platform.GlConst.GL_COLOR_BUFFER_BIT;
import static com.mojang.blaze3d.platform.GlConst.GL_DEPTH_BUFFER_BIT;
import static net.minecraft.client.Minecraft.ON_OSX;
import static net.minecraft.client.Screenshot.SCREENSHOT_DIR;

// the renderPonders() and renderPonder(ResourceLocation) methods are stripped
// from release jars to avoid freaking out Modrinth/Curseforge auto moderation
@ApiStatus.Internal
public class PonderExport {
    public static boolean active = false;
    private static final List<PonderUI> tasks = Collections.synchronizedList(new LinkedList<>());

    public static void queuePonder(PonderUI ponderScreen) {
        tasks.add(ponderScreen);
    }

    /** This MUST be called at the very start or end of {@link Minecraft#runTick(boolean)} */
    @StripFromRelease
    public static void renderPonders() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (tasks.isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();
        try {
            active = true;
            if (mc.screen == null) {
                mc.mouseHandler.releaseMouse();
                KeyMapping.releaseAll();
            }
            while (!tasks.isEmpty()) {
                PonderUI ponder = tasks.remove(0);
                if (!renderPonder(ponder)) {
                    Component msg = Component.literal("Failed to render ponder "+loc(ponder))
                        .withStyle(ChatFormatting.RED);
                    mc.execute(() -> mc.gui.getChat().addMessage(msg));
                }
            }
        } finally {
            active = false;
            if (mc.screen == null) {
                mc.mouseHandler.grabMouse();
            }
        }
    }

    private static ResourceLocation loc(PonderUI ponderScreen) {
        return ponderScreen
            .getSubject()
            .getItemHolder()
            .unwrapKey()
            .map(ResourceKey::location)
            .orElseGet(() -> ponderScreen.getActiveScene().getId());
    }

    /** This MUST be called at the very start or end of {@link Minecraft#runTick(boolean)} */
    @StripFromRelease
    @SuppressWarnings("UnusedReturnValue")
    private static boolean renderPonder(PonderUI ponderScreen) {
        RenderSystem.assertOnRenderThreadOrInit();
        boolean ok = true;

        // SETUP
        final Minecraft mc = Minecraft.getInstance();
        final Window window = mc.getWindow();
        final Timer timer = ((AccessorMinecraft) mc).numismatics$getTimer();
        final int width = 1280;
        final int height = 720;
        final int fps = 30;
        final int guiScale = 2;

        File screenshotsDir = new File(mc.gameDirectory, SCREENSHOT_DIR);
        if (!(screenshotsDir.isDirectory() || screenshotsDir.mkdir())) return false;
        File ponderExportsDir = new File(screenshotsDir, "ponder_exports");
        if (!(ponderExportsDir.isDirectory() || ponderExportsDir.mkdir())) return false;

        ResourceLocation ponderLoc = loc(ponderScreen);
        File exportFile = new File(ponderExportsDir, ponderLoc.toDebugFileName() + ".mp4");

        final ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-y",
            "-f", "rawvideo",
            "-pix_fmt", "rgba",
            "-s", width + "x" + height,
            "-r", String.valueOf(fps),
            "-i", "pipe:0",
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            exportFile.getAbsolutePath()
        ).redirectError(ProcessBuilder.Redirect.INHERIT);
        final Process ffmpeg;
        try {
            ffmpeg = pb.start();
        } catch (IOException e) {
            Numismatics.LOGGER.error("Failed to start ffmpeg process for renderPonder({})", ponderScreen.getSubject(), e);
            return false;
        }
        final OutputStream ffmpegOut = new BufferedOutputStream(ffmpeg.getOutputStream());

        final RenderTarget renderTarget = new TextureTarget(width, height, true, ON_OSX);
        renderTarget.setClearColor(0f, 0f, 0f, 0f);

        final Screen screen0 = mc.screen;
        final int winWidth0 = window.getWidth();
        final int winHeight0 = window.getHeight();
        final float partialTick0 = timer.partialTick;

        mc.screen = null;
        window.setWidth(width);
        window.setHeight(height);
        window.setGuiScale(window.calculateScale(guiScale, mc.isEnforceUnicode()));

        ponderScreen.init(mc, window.getGuiScaledWidth(), window.getGuiScaledHeight());

        ByteBuffer bb = ByteBuffer.allocate(width * height * 4)
            .order(ByteOrder.LITTLE_ENDIAN);

        try (NativeImage nativeImage = new NativeImage(width, height, false)) {
            // SHARED-FRAME SETUP
            RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
            RenderSystem.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, ON_OSX);
            renderTarget.clear(ON_OSX);
            renderTarget.bindWrite(true);
            FogRenderer.setupNoFog();
            RenderSystem.enableCull();
            RenderSystem.viewport(0, 0, width, height);

            Matrix4f projMat = new Matrix4f().setOrtho(
                0.0F,
                (float) ((double) window.getWidth() / window.getGuiScale()),
                (float) ((double) window.getHeight() / window.getGuiScale()),
                0.0F,
                1000.0F,
                21000.0F
            );
            RenderSystem.setProjectionMatrix(projMat, VertexSorting.ORTHOGRAPHIC_Z);
            PoseStack msModelView = RenderSystem.getModelViewStack();
            msModelView.pushPose();
            msModelView.setIdentity();
            msModelView.translate(0.0f, 0.0f, -11000.0f);
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
            GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

            int nextTimeout = -1;
            int lastTick = -1;
            Outer: for (int frame = 0; true; frame++) {
                final int tick = frame * 20 / fps;
                final float partialTicks = (frame * 20.0f / fps) - tick;
                timer.partialTick = partialTicks;

                while (tick > lastTick) {
                    lastTick++;
                    if (nextTimeout > 0) {
                        nextTimeout--;
                        if (nextTimeout == 0) {
                            if (!((AccessorPonderUI) ponderScreen).numismatics$scroll(true)) {
                                break Outer;
                            }
                            nextTimeout = -1;
                        }
                    } else if (ponderScreen.getActiveScene().isFinished()) {
                        nextTimeout = 3*20;
                    }
                    ponderScreen.tick();
                }

                // PER-FRAME SETUP
                renderTarget.clear(ON_OSX);
                renderTarget.bindWrite(true);

                // PER-FRAME RENDER
                ponderScreen.render(guiGraphics, -1000, -1000, partialTicks);

                // PER-FRAME TEARDOWN
                guiGraphics.flush();
                renderTarget.unbindWrite();

                takeScreenshot(nativeImage, renderTarget);
                bb.clear();
                bb.asIntBuffer().put(nativeImage.getPixelsRGBA());
                try {
                    ffmpegOut.write(bb.array());
                } catch (IOException e) {
                    ok = false;
                    Numismatics.LOGGER.error("Failed to write to ffmpeg in renderPonder({})", ponderScreen.getSubject(), e);
                    break;
                }
            }

            // SHARED-FRAME TEARDOWN
            msModelView.popPose();
            RenderSystem.applyModelViewMatrix();
        } finally {
            // TEARDOWN
            timer.partialTick = partialTick0;
            window.setWidth(winWidth0);
            window.setHeight(winHeight0);
            window.setGuiScale(window.calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode()));
            mc.screen = screen0;
            renderTarget.destroyBuffers();
            mc.getMainRenderTarget().bindWrite(true);
        }

        // CLEAN UP FFMPEG
        try {
            ffmpegOut.flush();
            ffmpegOut.close();
        } catch (IOException e) {
            Numismatics.LOGGER.error("Failed to close ffmpeg output stream", e);
        }
        int exit;
        while (true) {
            try {
                exit = ffmpeg.waitFor();
                break;
            } catch (InterruptedException ignored) {}
        }

        if (exit != 0) {
            Numismatics.LOGGER.error("ffmpeg exited with code {} for renderPonder({})", exit, ponderScreen.getSubject());
            return false;
        }

        Component component = Component.literal(exportFile.getName())
            .withStyle(ChatFormatting.UNDERLINE)
            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, exportFile.getAbsolutePath())));
        Component msg = Component.literal("Exported ponder as ").append(component);
        mc.execute(() -> mc.gui.getChat().addMessage(msg));

        return ok;
    }

    private static void takeScreenshot(NativeImage nativeImage, RenderTarget framebuffer) {
        RenderSystem.bindTexture(framebuffer.getColorTextureId());
        nativeImage.downloadTexture(0, false);
        nativeImage.flipY();
    }
}
