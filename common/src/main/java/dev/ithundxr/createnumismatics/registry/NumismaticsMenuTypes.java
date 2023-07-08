package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorMenu;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorScreen;
import dev.ithundxr.createnumismatics.content.bank.BankMenu;
import dev.ithundxr.createnumismatics.content.bank.BankScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class NumismaticsMenuTypes {
    private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

    public static final MenuEntry<AndesiteDepositorMenu> ANDESITE_DEPOSITOR = register(
        "andesite_depositor",
        AndesiteDepositorMenu::new,
        () -> AndesiteDepositorScreen::new
    );

    public static final MenuEntry<BankMenu> BANK = register(
        "bank",
        BankMenu::new,
        () -> BankScreen::new
    );

    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
        String name, MenuBuilder.ForgeMenuFactory<C> factory, NonNullSupplier<MenuBuilder.ScreenFactory<C, S>> screenFactory) {
        return REGISTRATE
            .menu(name, factory, screenFactory)
            .register();
    }

    @SuppressWarnings("EmptyMethod")
    public static void register() {}
}
