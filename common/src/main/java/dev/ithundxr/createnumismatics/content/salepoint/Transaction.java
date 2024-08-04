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

package dev.ithundxr.createnumismatics.content.salepoint;

import com.simibubi.create.AllSoundEvents;
import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;

public class Transaction<T> {
    private final IDeductable deductable;
    private final int multiplier;
    private final T object;
    private final int totalPrice;
    private int progress;

    public Transaction(IDeductable deductable, int multiplier, T object, int totalPrice) {
        this.deductable = deductable;
        this.multiplier = multiplier;
        this.object = object;
        this.totalPrice = totalPrice;
        this.progress = 0;
    }

    public TransactionResult doTransaction(boolean simulate) {
        return doTransaction(simulate, () -> {});
    }

    public TransactionResult doTransaction(boolean simulate, Runnable onDeduct) {
        if (progress >= multiplier)
            return TransactionResult.SUCCESS;

        if (!simulate) {
            TransactionResult result = doTransaction(true);
            if (result == TransactionResult.FAILURE)
                return TransactionResult.FAILURE;
        }

        if (simulate) {
            if (deductable.getMaxWithdrawal() < totalPrice)
                return TransactionResult.FAILURE;
        } else {
            if (!deductable.deduct(totalPrice, ReasonHolder.IGNORED))
                return TransactionResult.FAILURE;
            onDeduct.run();
        }

        if (!simulate)
            progress++;

        return progress >= multiplier ? TransactionResult.SUCCESS : TransactionResult.IN_PROGRESS;
    }

    public IDeductable deductable() {
        return deductable;
    }

    public int multiplier() {
        return multiplier;
    }

    public T object() {
        return object;
    }

    public int totalPrice() {
        return totalPrice;
    }

    public int progress() {
        return progress;
    }

    @Override
    public String toString() {
        return "Transaction[" +
            "deductable=" + deductable + ", " +
            "multiplier=" + multiplier + ", " +
            "object=" + object + ", " +
            "totalPrice=" + totalPrice + ", " +
            "progress=" + progress + ']';
    }

    public enum TransactionResult {
        /** Transaction fully completed */
        SUCCESS(true, AllSoundEvents.CONFIRM, 1.0f, 0.5f),
        /** Transaction failed to complete, due to lack of funds */
        FAILURE(true, AllSoundEvents.DENY),
        /** Transaction is not done yet */
        IN_PROGRESS(false, AllSoundEvents.CONFIRM, 0.75f, 1.0f)
        ;
        public final boolean shouldStop;
        public final AllSoundEvents.SoundEntry sound;
        public final float volume;
        public final float pitch;

        TransactionResult(boolean shouldStop, AllSoundEvents.SoundEntry sound) {
            this(shouldStop, sound, 1.0F, 1.0F);
        }

        TransactionResult(boolean shouldStop, AllSoundEvents.SoundEntry sound, float volume, float pitch) {
            this.shouldStop = shouldStop;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
