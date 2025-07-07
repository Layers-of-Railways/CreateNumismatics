package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class BankAccountBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<BankAccountBehaviour> TYPE = new BehaviourType<>();
    private UUID accountUUID;
    public BankAccountBehaviour(SmartBlockEntity be) {
        super(be);
    }

    public UUID getAccountUUID() {
        if (accountUUID == null) {
            accountUUID = UUID.randomUUID();
            blockEntity.notifyUpdate();
        }
        return accountUUID;
    }

    public BankAccount getAccount() {
        return Numismatics.BANK.getOrCreateAccount(getAccountUUID(), BankAccount.Type.BLAZE_BANKER);
    }

    public boolean hasAccount() {
        if (accountUUID == null)
            return false;
        return Numismatics.BANK.getAccount(accountUUID) != null;
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.hasUUID("accountUUID")) {
            accountUUID = tag.getUUID("accountUUID");
        }
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (accountUUID != null) {
            tag.putUUID("accountUUID", accountUUID);
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void destroy() {
        super.destroy();
        BankAccount oldAccount = Numismatics.BANK.accounts.remove(accountUUID);
        if (oldAccount != null)
            oldAccount.setLabel(null);
        Numismatics.BANK.markBankDirty();
    }
}
