package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.checkout.CheckoutPaymentMethod;
import dev.ithundxr.createnumismatics.multiloader.C2SPacket;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DeferredCheckoutResolutionPacket implements C2SPacket {

    @NotNull
    private final UUID transactionId;

    @NotNull
    private final CheckoutPaymentMethod method;

    @NotNull
    private final UUID bankAccount;

    public DeferredCheckoutResolutionPacket(FriendlyByteBuf buf) {
        this.transactionId = buf.readUUID();
        this.method = buf.readEnum(CheckoutPaymentMethod.class);
        this.bankAccount = buf.readUUID();
    }

    public DeferredCheckoutResolutionPacket(UUID transactionId, CheckoutPaymentMethod method, UUID bankAccount) {
        this.transactionId = transactionId;
        this.method = method;
        this.bankAccount = bankAccount;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(transactionId);
        buffer.writeEnum(method);
        buffer.writeUUID(bankAccount);
    }

    @Override
    public void handle(ServerPlayer player) {
        Numismatics.LOGGER.info("Received checkout packet! Transaction: {}; Method: {}; BankAccount: {}", transactionId, method, bankAccount);
        var order = Numismatics.DEFERRED_ORDERS.getDeferredOrder(transactionId);
        if (order == null)
            return;

        order.completePurchase(method, bankAccount);
        Numismatics.DEFERRED_ORDERS.voidOrder(order);
    }
}
