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

package dev.ithundxr.createnumismatics.content.backend;

import com.simibubi.create.foundation.utility.Components;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A helper class to hold a message indicating the reason a transaction failed.
 * If a method takes this as a parameter and you don't care about the message, you can pass {@link ReasonHolder#IGNORED}.
 */
public class ReasonHolder {

    public static final ReasonHolder IGNORED = new ReasonHolder() {
        @Override
        public void setMessage(@NotNull Component message) {}
    };

    private @Nullable Component message;

    public ReasonHolder() {
        this.message = null;
    }

    public void setMessage(@NotNull Component message) {
        this.message = message;
    }

    @Deprecated
    public void setMessage(@NotNull String message) {
        this.message = Components.literal(message);
    }

    public boolean hasMessage() {
        return message != null;
    }

    public @Nullable Component getMessage() {
        return message;
    }

    public @NotNull MutableComponent getMessageOrDefault() {
        return getMessageOrDefault(Components.translatable("gui.numismatics.vendor.insufficient_funds"));
    }

    public @NotNull MutableComponent getMessageOrDefault(@NotNull Component defaultMessage) {
        return (message != null ? message : defaultMessage).copy();
    }
}
