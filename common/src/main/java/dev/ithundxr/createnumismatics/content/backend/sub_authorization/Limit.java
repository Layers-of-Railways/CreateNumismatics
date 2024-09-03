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

package dev.ithundxr.createnumismatics.content.backend.sub_authorization;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class Limit {
    @Nullable
    private Integer limit;
    private int spent;

    public Limit(@Nullable Integer limit) {
        this(limit, 0);
    }

    public Limit(@Nullable Integer limit, int spent) {
        this.limit = limit;
        this.spent = spent;

        if (limit == null) {
            resetSpent();
        }
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public int getSpent() {
        return spent;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public boolean spend(int amount) {
        return spend(amount, false);
    }

    public boolean spend(int amount, boolean simulate) {
        if (amount < 0) {
            return false;
        }

        Integer limit = getLimit();
        if (limit != null && amount + spent > limit) {
            return false;
        }

        if (!simulate && limit != null)
            spent += amount;

        return true;
    }

    public void setLimit(@Nullable Integer limit, boolean resetSpent) {
        this.limit = limit;

        if (resetSpent || limit == null) {
            resetSpent();
        }
    }

    public void resetSpent() {
        spent = 0;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(limit != null);
        if (limit != null)
            buf.writeInt(limit);
        buf.writeInt(spent);
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        if (limit != null)
            tag.putInt("limit", limit);
        tag.putInt("spent", spent);
        return tag;
    }

    public static Limit read(CompoundTag tag) {
        Integer limit;
        if (tag.contains("limit"))
            limit = tag.getInt("limit");
        else
            limit = null;

        return new Limit(limit, tag.getInt("spent"));
    }

    public static Limit read(FriendlyByteBuf buf) {
        Integer limit;
        if (buf.readBoolean())
            limit = buf.readInt();
        else
            limit = null;

        return new Limit(limit, buf.readInt());
    }

    public MutableComponent describe() {
        return describe(true);
    }

    public MutableComponent describe(boolean monetary) {
        if (limit == null) {
            return Components.translatable("gui.numismatics.limit.none");
        } else {
            if (monetary) {
                Couple<Integer> cogsAndSpursSpent = Coin.COG.convert(spent);
                int cogsSpent = cogsAndSpursSpent.getFirst();
                int spursSpent = cogsAndSpursSpent.getSecond();

                Couple<Integer> cogsAndSpursLimit = Coin.COG.convert(limit);
                int cogsLimit = cogsAndSpursLimit.getFirst();
                int spursLimit = cogsAndSpursLimit.getSecond();

                return Components.translatable(
                    "gui.numismatics.limit.monetary",
                    TextUtils.formatInt(cogsSpent), Coin.COG.getName(cogsSpent), spursSpent,
                    TextUtils.formatInt(cogsLimit), Coin.COG.getName(cogsLimit), spursLimit
                );
            } else {
                return Components.translatable("gui.numismatics.limit", spent, limit);
            }
        }
    }
}
