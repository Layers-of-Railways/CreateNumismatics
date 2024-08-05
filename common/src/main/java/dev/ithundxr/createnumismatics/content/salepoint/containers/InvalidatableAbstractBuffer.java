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

package dev.ithundxr.createnumismatics.content.salepoint.containers;

public abstract class InvalidatableAbstractBuffer<C> {
    private boolean valid = true;

    public final boolean isValid() {
        return valid;
    }

    public final void invalidate() {
        valid = false;
        afterInvalidate();
    }

    protected void afterInvalidate() {}

    /**
     * Copy as much as possible of the source element into the buffer.<br>
     * <b>Note:</b> It is the caller's responsibility to modify the source,
     * the implementor MUST NOT modify the source argument<br>
     * This is an overload that does not simulate the copy.
     * @param source The source to take from.
     * @return The number of elements copied.
     */
    public final int copyToBuffer(C source) {
        return copyToBuffer(source, false);
    }

    /**
     * Copy as much as possible of the source element into the buffer.<br>
     * <b>Note:</b> It is the caller's responsibility to modify the source,
     * the implementor MUST NOT modify the source argument
     * @param source The source to take from.
     * @param simulate If true, the caller MUST NOT actually add the element into its buffer
     * @return The number of elements copied.
     */
    public final int copyToBuffer(C source, boolean simulate) {
        if (!isValid())
            return 0;

        return copyToBufferInternal(source, simulate);
    }

    /**
     * Copy as much as possible of the source element into the buffer.<br>
     * <b>Note:</b> It is the caller's responsibility to modify the source,
     * the implementor MUST NOT modify the source argument
     * @param source The source to take from.
     * @param simulate If true, the caller MUST NOT actually add the element into its buffer
     * @return The number of elements copied.
     */
    protected abstract int copyToBufferInternal(C source, boolean simulate);

    /**
     * Remove as much as possible of the source element from the buffer, up to maxAmount, i.e. ignoring source's size.<br>
     * <b>Note:</b> It is the caller's responsibility to modify the source,
     * the implementor MUST NOT modify the source argument
     * @param source The source to remove.
     * @param simulate If true, the caller MUST NOT actually remove the element from its buffer
     * @return The number of elements removed.
     */
    protected abstract int removeFromBufferInternal(C source, boolean simulate, final int maxAmount);
}
