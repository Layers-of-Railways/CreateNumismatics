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
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListContainer;
import dev.ithundxr.createnumismatics.multiloader.PlayerSelection;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.BankAccountLabelPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SubAccount {
    private final BankAccount parentAccount;

    @NotNull
    private String label;

    @NotNull
    // In spurs, no limit if null
    private Limit totalLimit;

    // This trust list is special, because the parent account owner is NOT automatically a member.
    private final List<UUID> trustList = new ArrayList<>();
    private final TrustListContainer trustListContainer = new TrustListContainer(trustList, this::markDirty);

    @NotNull
    private final UUID authorizationID;

    @NotNull
    private AuthorizationType authorizationType = AuthorizationType.TRUSTED_PLAYERS;

    private boolean removed = false;

    public SubAccount(BankAccount parentAccount, @NotNull String label, @NotNull UUID authorizationID) {
        this.parentAccount = parentAccount;
        this.label = label;
        this.authorizationID = authorizationID;
        this.totalLimit = new Limit(null);
    }

    public void setLabel(@NotNull String label) {
        if (this.label.equals(label))
            return;

        this.label = label;
        markDirty();

        NumismaticsPackets.PACKETS.sendTo(PlayerSelection.all(), new BankAccountLabelPacket(this));
    }

    public @NotNull String getLabel() {
        return label;
    }

    @NotNull
    public Limit getTotalLimit() {
        return totalLimit;
    }

    @NotNull
    public List<UUID> getTrustList() {
        return trustList;
    }

    public @NotNull UUID getAuthorizationID() {
        return authorizationID;
    }

    public @NotNull AuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(@NotNull AuthorizationType authorizationType) {
        this.authorizationType = authorizationType;
        markDirty();
    }

    public boolean spend(Authorization authorization, Coin coin, int count, ReasonHolder reasonHolder) {
        return spend(authorization, coin, count, false, reasonHolder);
    }

    public boolean spend(Authorization authorization, Coin coin, int count, boolean simulate, ReasonHolder reasonHolder) {
        return spend(authorization, coin.toSpurs(count), simulate, reasonHolder);
    }

    public boolean spend(Authorization authorization, int spurs, ReasonHolder reasonHolder) {
        return spend(authorization, spurs, false, reasonHolder);
    }

    public boolean spend(Authorization authorization, int spurs, boolean simulate, ReasonHolder reasonHolder) {
        if (!isAuthorized(authorization)) {
            reasonHolder.setMessage(Components.translatable("error.numismatics.card.not_authorized"));
            return false;
        }

        if (parentAccount.getBalance() < spurs) {
            return false;
        }

        if (!totalLimit.spend(spurs, simulate)) {
            reasonHolder.setMessage(Components.translatable("error.numismatics.authorized_card.limit_reached"));
            return false;
        }

        if (!simulate) {
            markDirty();
            parentAccount.deduct(spurs, reasonHolder);
        }

        return true;
    }

    public boolean isAuthorized(Authorization request) {
        if (!request.getAuthorizationID().equals(authorizationID)) {
            return false;
        }

        if (removed)
            return false;

        return switch (authorizationType) {
            case TRUSTED_PLAYERS -> {
                if (!request.isHuman())
                    yield false;

                if (request.getPersonalID() == null)
                    yield false;

                yield trustList.contains(request.getPersonalID());
            }
            case TRUSTED_AUTOMATION -> {
                if (request.getPersonalID() == null)
                    yield false;

                yield trustList.contains(request.getPersonalID());
            }
            case ANYBODY -> true;
        };
    }

    public void markDirty() {
        parentAccount.markDirty();
    }

    public TrustListContainer getTrustListContainer() {
        return trustListContainer;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();

        tag.putString("label", label);
        tag.putUUID("authorizationID", authorizationID);

        tag.putString("authorizationType", authorizationType.getSerializedName());

        tag.put("TotalLimit", totalLimit.write());

        if (!trustListContainer.isEmpty()) {
            tag.put("TrustListInv", trustListContainer.save(new CompoundTag()));
        }

        return tag;
    }

    public static SubAccount read(BankAccount parentAccount, CompoundTag tag) {
        SubAccount subAccount = new SubAccount(
            parentAccount,
            tag.getString("label"),
            tag.getUUID("authorizationID")
        );

        subAccount.authorizationType = AuthorizationType.deserialize(tag.getString("authorizationType"));

        if (tag.contains("TotalLimit", Tag.TAG_COMPOUND)) {
            subAccount.totalLimit = Limit.read(tag.getCompound("TotalLimit"));
        }

        subAccount.trustListContainer.clearContent();
        subAccount.trustList.clear();
        if (tag.contains("TrustListInv", Tag.TAG_COMPOUND)) {
            subAccount.trustListContainer.load(tag.getCompound("TrustListInv"));
        }

        return subAccount;
    }

    public @Nullable IDeductable getDeductor(Authorization authorization) {
        if (!isAuthorized(authorization))
            return null;

        return new PreAuthorizedDeductor(authorization);
    }

    public void sendToMenu(FriendlyByteBuf buf) {
        buf.writeUtf(label);
        buf.writeUUID(authorizationID);
        buf.writeEnum(authorizationType);
        totalLimit.write(buf);
    }

    public void updateFrom(SubAccount other) {
        if (this.authorizationID != other.authorizationID) {
            Numismatics.LOGGER.warn("Tried to update a sub account with a different authorization ID");
            return;
        }
        this.label = other.label;
        this.authorizationType = other.authorizationType;
        this.totalLimit = other.totalLimit;
        this.trustListContainer.load(other.trustListContainer.save(new CompoundTag()));
        other.setRemoved();
    }

    public static SubAccount clientSide(BankAccount parentAccount, FriendlyByteBuf buf) {
        SubAccount subAccount = new SubAccount(
            parentAccount,
            buf.readUtf(),
            buf.readUUID()
        );

        subAccount.authorizationType = buf.readEnum(AuthorizationType.class);

        subAccount.totalLimit = Limit.read(buf);

        return subAccount;
    }

    public void setRemoved() {
        this.removed = true;
        markDirty();
    }

    private class PreAuthorizedDeductor implements IDeductable {
        private final Authorization authorization;

        private PreAuthorizedDeductor(Authorization authorization) {
            this.authorization = authorization;
        }

        @Override
        public boolean deduct(Coin coin, int amount, ReasonHolder reasonHolder) {
            return spend(authorization, coin, amount, reasonHolder);
        }

        @Override
        public boolean deduct(int spurs, ReasonHolder reasonHolder) {
            return spend(authorization, spurs, reasonHolder);
        }
    }
}
