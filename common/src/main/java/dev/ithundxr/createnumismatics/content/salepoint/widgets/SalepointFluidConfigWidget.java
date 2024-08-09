/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.salepoint.widgets;

import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.Components;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.base.client.rendering.IItemApplicableWidget;
import dev.ithundxr.createnumismatics.content.salepoint.states.FluidSalepointState;
import dev.ithundxr.createnumismatics.multiloader.fluid.FluidUnits;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.SalepointFluidFilterPacket;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SalepointFluidConfigWidget extends SalepointFluidDisplayWidget implements IItemApplicableWidget {

    private boolean soundPlayed;

    public SalepointFluidConfigWidget(int x, int y, @NotNull FluidSalepointState state) {
        super(x, y, state);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        MultiloaderFluidStack filter = state.getFilter();
        if (filter.isEmpty())
            return false;

        int offset = delta > 0 ? 1 : -1;

        if (AllKeys.shiftDown() && AllKeys.ctrlDown())
            //noinspection DataFlowIssue
            offset *= 1;
        else if (AllKeys.ctrlDown())
            offset *= 10;
        else if (AllKeys.shiftDown())
            offset *= 1000;
        else
            offset *= 100;

        offset = (int) ((long) offset * FluidUnits.bucket() / 1000);

        long oldAmount = filter.getAmount();
        long min = 0;
        long max = FluidSalepointState.getFilterCapacity();
        long amount = Math.max(min, Math.min(oldAmount + offset, max));
        if (oldAmount != amount) {
            NumismaticsPackets.PACKETS.send(new SalepointFluidFilterPacket(filter.copy().setAmount(amount)));
            if (!soundPlayed) {
                Minecraft.getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(),
                        1.5f + 0.1f * (amount - min) / (max - min)));
                soundPlayed = true;
            }
            return true;
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();
        soundPlayed = false;
    }

    @Override
    public void onItemApplied(ItemStack stack) {
        MultiloaderFluidStack fluidStack = getFluidFrom(stack);
        if (fluidStack != null) {
            NumismaticsPackets.PACKETS.send(new SalepointFluidFilterPacket(fluidStack));
            this.playDownSound(Minecraft.getInstance().getSoundManager());
        }
    }

    @ExpectPlatform
    @Environment(EnvType.CLIENT)
    protected static @Nullable MultiloaderFluidStack getFluidFrom(@NotNull ItemStack stack) {
        throw new AssertionError();
    }

    @Override
    public List<Component> getToolTip() {
        MultiloaderFluidStack filter = state.getFilter();
        if (filter.isEmpty())
            return List.of(
                Components.translatable("gui.numismatics.salepoint.fluid_filter_empty.0"),
                Components.translatable("gui.numismatics.salepoint.fluid_filter_empty.1")
            );

        return List.of(
            filter.getDisplayName(),
            Components.literal(TextUtils.formatFluid(filter.getAmount())),
            Components.translatable("create.gui.scrollInput.scrollToAdjustAmount"),
            Components.translatable("create.gui.scrollInput.shiftScrollsFaster")
        );
    }
}
