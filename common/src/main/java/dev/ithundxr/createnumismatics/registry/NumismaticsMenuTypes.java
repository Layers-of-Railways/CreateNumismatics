/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListMenu;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListScreen;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerMenu;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerScreen;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorMenu;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorScreen;
import dev.ithundxr.createnumismatics.content.bank.BankMenu;
import dev.ithundxr.createnumismatics.content.bank.BankScreen;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorMenu;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorScreen;
import dev.ithundxr.createnumismatics.content.vendor.VendorMenu;
import dev.ithundxr.createnumismatics.content.vendor.VendorScreen;
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

    public static final MenuEntry<BrassDepositorMenu> BRASS_DEPOSITOR = register(
        "brass_depositor",
        BrassDepositorMenu::new,
        () -> BrassDepositorScreen::new
    );

    public static final MenuEntry<BankMenu> BANK = register(
        "bank",
        BankMenu::new,
        () -> BankScreen::new
    );

    public static final MenuEntry<TrustListMenu> TRUST_LIST = register(
        "trust_list",
        TrustListMenu::new,
        () -> TrustListScreen::new
    );

    public static final MenuEntry<BlazeBankerMenu> BLAZE_BANKER = register(
        "blaze_banker",
        BlazeBankerMenu::new,
        () -> BlazeBankerScreen::new
    );

    public static final MenuEntry<VendorMenu> VENDOR = register(
        "vendor",
        VendorMenu::new,
        () -> VendorScreen::new
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
