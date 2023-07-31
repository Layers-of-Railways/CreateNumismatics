package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class BankAccountBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<AdvancementBehaviour> TYPE = new BehaviourType<>();
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
    public void read(CompoundTag nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);
        if (nbt.hasUUID("accountUUID")) {
            accountUUID = nbt.getUUID("accountUUID");
        }
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        if (accountUUID != null) {
            nbt.putUUID("accountUUID", accountUUID);
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
