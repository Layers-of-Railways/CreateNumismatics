/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListContainer;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlazeBankerBlockEntity extends SmartBlockEntity implements Trusted, TrustListHolder, MenuProvider {

    protected LerpedFloat headAnimation;
    protected LerpedFloat headAngle;

    // only available on client
    private int clientsideBalance = 0;

    // only available on server
    private int lastSentBalance = 0;

    protected BankAccountBehaviour bankAccountBehaviour;

    @Nullable
    protected UUID owner;

    public String getLabel() {
        if (label != null && label.isEmpty())
            label = null;
        return label;
    }

    public void setLabel(String label) {
        if (label.isEmpty())
            label = null;
        if (level != null && !level.isClientSide) {
            getAccount().setLabel(label);
        }
        this.label = label;
        notifyUpdate();
    }

    @Nullable
    protected String label;

    protected final List<UUID> trustList = new ArrayList<>();

    public final TrustListContainer trustListContainer = new TrustListContainer(trustList, this::onTrustListChanged);

    private boolean delayedDataSync = false;

    public BlazeBankerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        headAnimation = LerpedFloat.linear();
        headAngle = LerpedFloat.angular();
    }

    // copied from Create's Blaze Burner
    @Environment(EnvType.CLIENT)
    private void tickAnimation() {
        boolean active = Minecraft.getInstance().screen instanceof BlazeBankerScreen;

        {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (getBlockPos().getX() + 0.5);
                double dz = z - (getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
            headAngle.chase(target, .25f, LerpedFloat.Chaser.exp(5));
            headAngle.tickChaser();
        }

        headAnimation.chase(active ? 1 : 0, .25f, LerpedFloat.Chaser.exp(.25f));
        headAnimation.tickChaser();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        bankAccountBehaviour = new BankAccountBehaviour(this);
        behaviours.add(bankAccountBehaviour);
    }

    private void onTrustListChanged() {
        if (level == null || level.isClientSide)
            return;
        BankAccount account = getAccount();
        account.updateTrustList((accountTrustList) -> {
            accountTrustList.clear();
            accountTrustList.add(owner);
            accountTrustList.addAll(trustList);
        });
        notifyUpdate();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide)
            return;
        if (delayedDataSync) {
            delayedDataSync = false;
            sendData();
        }

        if (owner != null && !getAccount().isAuthorized(owner)) {
            onTrustListChanged();
        }

        if (lastSentBalance != getAccount().getBalance()) {
            lastSentBalance = getAccount().getBalance();
            sendData();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && level.isClientSide) {
            tickAnimation();
            if (!isVirtual())
                spawnParticles(BlazeBurnerBlock.HeatLevel.KINDLED, 1);
            return;
        }
    }

    // copied from Create's Blaze Burner
    protected void spawnParticles(BlazeBurnerBlock.HeatLevel heatLevel, double burstMult) {
        if (level == null)
            return;
        if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE)
            return;

        RandomSource r = level.getRandom();

        Vec3 c = VecHelper.getCenterOf(worldPosition);
        Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f)
            .multiply(1, 0, 1));

        if (r.nextInt(4) != 0)
            return;

        boolean empty = level.getBlockState(worldPosition.above())
            .getCollisionShape(level, worldPosition.above())
            .isEmpty();

        if (empty || r.nextInt(8) == 0)
            level.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);

        double yMotion = empty ? .0625f : r.nextDouble() * .0125f;
        Vec3 v2 = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
                .multiply(1, .25f, 1)
                .normalize()
                .scale((empty ? .25f : .5) + r.nextDouble() * .125f))
            .add(0, .5, 0);

        if (heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.SEETHING)) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING)) {
            level.addParticle(ParticleTypes.FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        }
        return;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (owner != null)
            tag.putUUID("Owner", owner);

        if (!trustListContainer.isEmpty()) {
            tag.put("TrustListInv", trustListContainer.save(new CompoundTag()));
        }
        if (getLabel() != null)
            tag.putString("Label", getLabel());

        if (clientPacket && hasAccount())
            tag.putInt("Balance", getAccount().getBalance());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;

        trustListContainer.clearContent();
        trustList.clear();
        if (tag.contains("TrustListInv", Tag.TAG_COMPOUND)) {
            trustListContainer.load(tag.getCompound("TrustListInv"));
        }

        if (clientPacket) {
            clientsideBalance = tag.getInt("Balance");
        }

        setLabel(tag.getString("Label"));
    }

    public BankAccount getAccount() {
        if (this.isRemoved()) {
            Numismatics.LOGGER.error("Tried to get account from removed banker!");
            return null;
        }
        if (bankAccountBehaviour == null) {
            return null;
        }
        return bankAccountBehaviour.getAccount();
    }

    public boolean hasAccount() {
        if (this.isRemoved()) {
            Numismatics.LOGGER.error("Tried to check account from removed banker!");
            return false;
        }
        if (bankAccountBehaviour == null) {
            return false;
        }
        return bankAccountBehaviour.hasAccount();
    }

    @Override
    public boolean isTrustedInternal(Player player) {
        if (Utils.isDevEnv()) { // easier to test this way in dev
            return player.getItemBySlot(EquipmentSlot.FEET).is(Items.GOLDEN_BOOTS);
        } else {
            return owner == null || owner.equals(player.getUUID()) || trustList.contains(player.getUUID());
        }
    }

    @Override
    public ImmutableList<UUID> getTrustList() {
        return ImmutableList.copyOf(trustList);
    }

    @Override
    public Container getTrustListBackingContainer() {
        return trustListContainer;
    }

    void notifyDelayedDataSync() {
        delayedDataSync = true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Components.translatable("block.numismatics.blaze_banker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BlazeBankerMenu(NumismaticsMenuTypes.BLAZE_BANKER.get(), i, inventory, this);
    }

    @NotNull
    public String getLabelNonNull() {
        return getLabel() == null ? "Blaze Banker" : getLabel();
    }

    public UUID getAccountId() {
        if (bankAccountBehaviour == null) {
            return null;
        }
        return bankAccountBehaviour.getAccountUUID();
    }

    public int getClientsideBalance() {
        return clientsideBalance;
    }

    @Override
    public void openTrustListMenu(ServerPlayer player) {
        if (!isTrusted(player)) {
            return;
        }
        if (level == null || level.isClientSide)
            return;
        Utils.openScreen(player, this, this::sendToMenu);
    }
}
