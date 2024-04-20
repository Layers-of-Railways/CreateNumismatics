package dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals;

import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;

public class BrassDepositorPeripheral extends SyncedPeripheral<BrassDepositorBlockEntity> {

    public BrassDepositorPeripheral(BrassDepositorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    public final void setPrice(String coin, int amount) throws LuaException {
        blockEntity.setPrice(getCoinFromString(coin), amount);

    }

    @LuaFunction
    public final int getPrice(){
        return blockEntity.getTotalPrice();
    }

    @LuaFunction
    public final int getTotalPrice(String coin) throws LuaException {
        return blockEntity.getPrice(getCoinFromString(coin));
    }

    Coin getCoinFromString(String coin) throws LuaException {
        return switch (coin){
            case "spur" -> Coin.SPUR;
            case "bevel" -> Coin.BEVEL;
            case "sprocket" -> Coin.SPROCKET;
            case "cog" -> Coin.COG;
            case "crown" -> Coin.CROWN;
            case "sun" -> Coin.SUN;
            default -> throw new LuaException("incorrect coin name");
        };
    }

    @Override
    public String getType() {
        return "Numismatics_Depositor";
    }
}
