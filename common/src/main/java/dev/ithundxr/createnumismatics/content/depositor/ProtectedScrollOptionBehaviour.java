package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public class ProtectedScrollOptionBehaviour<E extends Enum<E> & INamedIconOptions> extends ScrollOptionBehaviour<E> {
    protected final Predicate<Player> canInteract;
    public ProtectedScrollOptionBehaviour(Class<E> enum_, Component label, SmartBlockEntity be, ValueBoxTransform slot, Predicate<Player> canInteract) {
        super(enum_, label, be, slot);
        this.canInteract = canInteract;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (canInteract.test(player))
            super.setValueSettings(player, valueSetting, ctrlDown);
    }

    @Override
    public boolean isActive() {
        return super.isActive() && Utils.testClientPlayerOrElse(canInteract, true);
    }
}
