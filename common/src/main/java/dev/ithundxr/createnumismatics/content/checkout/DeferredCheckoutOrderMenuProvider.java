package dev.ithundxr.createnumismatics.content.checkout;

import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record DeferredCheckoutOrderMenuProvider(UUID id, int costInSpurs) implements MenuProvider {

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.numismatics.checkout_screen.header");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CheckoutMenu(NumismaticsMenuTypes.CHECKOUT.get(), i, inventory, this);
    }

    public static DeferredCheckoutOrderMenuProvider clientSide(FriendlyByteBuf buf) {
        return new DeferredCheckoutOrderMenuProvider(buf.readUUID(), buf.readVarInt());
    }

    public void sendToMenu(FriendlyByteBuf buf) {
        buf.writeUUID(this.id);
        buf.writeVarInt(this.costInSpurs);
    }
}
