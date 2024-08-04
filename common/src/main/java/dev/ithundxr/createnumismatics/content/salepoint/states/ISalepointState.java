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

package dev.ithundxr.createnumismatics.content.salepoint.states;

import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableAbstractBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ConcurrentModificationException;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISalepointState<C> {
    /**
     * Initialize the salepoint state.
     * MUST NOT be called in the constructor, or when loading the salepoint. It MUST only be called once, when the state is first created.
     */
    @ApiStatus.OverrideOnly
    void init();

    SalepointTypes getType();

    /**
     * Get the unique identifier of the salepoint state. Allows verifying binding.
     * @return The unique identifier.
     */
    UUID getId();

    InvalidatableAbstractBuffer<C> getBuffer();

    /**
     * Called when the salepoint is destroyed permanently.
     * No guarantees are made whether this is called before or after the block is actually removed.
     * @param level The level of the salepoint.
     * @param pos The position of the salepoint.
     */
    void onDestroy(Level level, BlockPos pos);

    /**
     * Called when the salepoint is unloaded, destroyed, or picked up by a contraption.
     */
    void onUnload();

    /**
     * (re)vitalize the salepoint state. Typically used to create newly valid buffers.
     */
    void keepAlive();

    /**
     * Renders the background of the salepoint's purchase GUI.
     * @param graphics The graphics object to render with.
     * @param partialTicks The partial ticks of the render.
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    @Environment(EnvType.CLIENT)
    default void renderPurchaseBackground(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {}

    /**
     * Renders the foreground of the salepoint's purchase GUI.
     * @param graphics The graphics object to render with.
     * @param partialTicks The partial ticks of the render.
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    @Environment(EnvType.CLIENT)
    default void renderPurchaseForeground(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {}

    /**
     * Renders the background of the salepoint's config GUI.
     * @param graphics The graphics object to render with.
     * @param partialTicks The partial ticks of the render.
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    @Environment(EnvType.CLIENT)
    default void renderConfigBackground(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {}

    /**
     * Renders the foreground of the salepoint's config GUI.
     * @param graphics The graphics object to render with.
     * @param partialTicks The partial ticks of the render.
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    @Environment(EnvType.CLIENT)
    default void renderConfigForeground(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {}

    /**
     * Adds extra widgets to the salepoint's config GUI.
     * @param widgetConsumer The widget consumer to add widgets to.
     */
    @Environment(EnvType.CLIENT)
    default void createConfigWidgets(WidgetConsumer widgetConsumer) {}

    /**
     * Adds extra widgets to the salepoint's purchase GUI.
     * @param widgetConsumer The widget consumer to add widgets to.
     */
    @Environment(EnvType.CLIENT)
    default void createPurchaseWidgets(WidgetConsumer widgetConsumer) {}

    /**
     * Whether the salepoint's config GUI has a filter slot.
     */
    default boolean configGuiHasFilterSlot() {
        return false;
    }

    /**
     * Create the filter slot for the salepoint's config GUI. If {@link #configGuiHasFilterSlot()} returns false, this method MUST not be called.
     *
     * @param salepointLevel (Context) The level of the salepoint.
     * @param salepointPos (Context) The position of the salepoint.
     * @param player (Context) The player that is opening the config GUI.
     * @return A ghost slot for the filter.
     */
    default Slot createConfigGuiFilterSlot(Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        if (configGuiHasFilterSlot()) {
            throw new NotImplementedException("Filter slot not implemented. If configGuiHasFilterSlot() returns true, this method must be implemented.");
        } else {
            throw new UnsupportedOperationException("Filter slot not supported. This method should only be called if configGuiHasFilterSlot() returns true.");
        }
    }

    /**
     * Whether the salepoint's purchase GUI has a display slot.
     */
    default boolean purchaseGuiHasDisplaySlot() {
        return false;
    }

    /**
     * Create the display slot for the salepoint's purchase GUI. If {@link #purchaseGuiHasDisplaySlot()} returns false, this method MUST not be called.
     *
     * @param salepointLevel (Context) The level of the salepoint.
     * @param salepointPos (Context) The position of the salepoint.
     * @param player (Context) The player that is opening the purchase GUI.
     * @return A ghost slot for the display.
     */
    default Slot createPurchaseGuiDisplaySlot(Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        if (purchaseGuiHasDisplaySlot()) {
            throw new NotImplementedException("Display slot not implemented. If purchaseGuiHasDisplaySlot() returns true, this method must be implemented.");
        } else {
            throw new UnsupportedOperationException("Display slot not supported. This method should only be called if purchaseGuiHasDisplaySlot() returns true.");
        }
    }

    /**
     * Check whether it is valid to change the filter to the specified object.
     * This may return false, for example, for fluids, where there is no clear way to empty the buffer tank.
     * @param filter The filter to change to.
     * @return Whether the filter can be changed.
     */
    boolean canChangeFilterTo(C filter);

    /**
     * <p>
         * Actually change the filter to the specified object.
         * The provided `salepointLevel`, `salepointPos`, and `player` can be used to,
         * for example, return buffer items into the player's inventory.
     * </p>
     * <p>
     *     Implementations SHOULD check {@link #canChangeFilterTo(C)} before changing the filter, there is no
     *     requirement for callers to perform that check. If {@link #canChangeFilterTo(C)} returns true,
     *     setFilter SHOULD succeed.
     * </p>
     * <p>
     *     <b>Note:</b> `filter` is treated as a ghost object, and will not be returned into the world when breaking the salepoint.
     * </p>
     *
     * @param filter The filter to change to.
     * @param salepointLevel (Context) The level of the salepoint.
     * @param salepointPos (Context) The position of the salepoint.
     * @param player (Context) The player that is changing the filter.
     * @return Whether the filter was successfully changed.
     */
    boolean setFilter(C filter, Level salepointLevel, BlockPos salepointPos, @Nullable Player player);

    C getFilter();

    @ApiStatus.NonExtendable
    @SuppressWarnings("unchecked")
    default boolean filterMatchesObject(Object object) {
        if (!(getType().getContentClass().isAssignableFrom(object.getClass()))) {
            return false;
        }
        return filterMatches((C) object);
    }

    boolean filterMatches(C object);

    /**
     * Save the state of the salepoint to a {@link CompoundTag}.
     * Implementations MUST include an "id" tag with the return value of calling {@link SalepointTypes#getId()} on {@link #getType()}.
     * @return The saved state.
     */
    CompoundTag save();

    /**
     * Load the state of the salepoint from a {@link CompoundTag}.
     * @param tag The tag to load from.
     */
    @ApiStatus.OverrideOnly
    void load(CompoundTag tag);

    /**
     * If the salepoint can execute a purchase right now, at the specified position.
     * @param level The level of the salepoint and targeted position.
     * @param targetedPos The position of the targeted block (i.e. PortableStorageInterface).
     * @param reasonHolder An out-parameter to hold the reason why a purchase is not valid.
     * @return Whether the salepoint can execute a purchase.
     */
    boolean isValidForPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder);

    /**
     * <p>
     *     Execute a purchase at the specified position.
     * </p>
     * <p>
     *     Implementations SHOULD check {@link #isValidForPurchase(Level, BlockPos, ReasonHolder)} before executing
     *     a purchase, there is no requirement for callers to perform that check.
     * </p>
     * <p>
     *     <b>NOTE:</b> callers are responsible for managing money deduction and storage. The recommended procedure is:
     *     <ol>
     *         <li>Verify that the player has enough money to make the purchase.</li>
     *         <li>Call this method.</li>
     *         <li>If the purchase was successful, deduct the money from the player.</li>
     *     </ol>
     * </p>
     * @param level The level of the salepoint and targeted position.
     * @param targetedPos The position of the targeted block (i.e. PortableStorageInterface).
     * @param reasonHolder An out-parameter to hold the reason why a purchase is not valid.
     * @return Whether the purchase was successful.
     */
    boolean doPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder);

    /**
     * Ensure that the salepoint controls the targeted position when needed.
     * Should be called when the salepoint is placed, loaded, and in a lazy tick method.
     * @param level The level of the salepoint and targeted position.
     * @param targetedPos The position of the targeted block (i.e. PortableStorageInterface).
     */
    void ensureUnderControl(Level level, BlockPos targetedPos);

    /**
     * Relinquish control of the targeted position.
     * Should be called when the salepoint is broken but not when it is unloaded.
     * @param level The level of the salepoint and targeted position.
     * @param targetedPos The position of the targeted block (i.e. PortableStorageInterface).
     */
    void relinquishControl(Level level, BlockPos targetedPos);

    void setChangedCallback(Runnable callback);

    @ApiStatus.NonExtendable
    @Nullable
    default SalepointTargetBehaviour<C> getBehaviour(BlockGetter reader, BlockPos pos) {
        BlockEntity be;
        try {
            be = reader.getBlockEntity(pos);
        } catch (ConcurrentModificationException e) {
            be = null;
        }
        return getBehaviour(be);
    }

    @ApiStatus.NonExtendable
    @Nullable
    @SuppressWarnings("unchecked")
    default SalepointTargetBehaviour<C> getBehaviour(@Nullable BlockEntity be) {
        return SalepointTargetBehaviour.get(be, (Class<C>) getType().getContentClass());
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    interface WidgetConsumer {
        @Environment(EnvType.CLIENT)
        <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);
    }
}
