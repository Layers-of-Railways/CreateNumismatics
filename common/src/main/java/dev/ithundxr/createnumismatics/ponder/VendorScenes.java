/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.ponder;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorMenu;
import dev.ithundxr.createnumismatics.content.vendor.VendorScreen;
import dev.ithundxr.createnumismatics.mixin_interfaces.InputWindowElement_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.SceneBuilderExtension;
import dev.ithundxr.createnumismatics.ponder.utils.ScreenVec;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.Cursor;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.CursorPhysicsProperties;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.util.ClientCraftingUtils;
import dev.ithundxr.createnumismatics.util.Utils;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.instruction.ShowInputInstruction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("DuplicatedCode")
public class VendorScenes {
    public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("vendor_intro", "Using Vendors");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos vendorSell = util.grid().at(1, 1, 1);
        BlockPos vendorBuy = util.grid().at(0, 1, 1);
        Vec3 vendorText = util.vector().blockSurface(vendorSell, Direction.WEST).add(0, 0.5, 0);

        // for some reason the lighting is broken if we show it normally
        var vendorSellLink = scene.world().showIndependentSection(util.select().position(vendorSell), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(70)
            .text("This vendor is selling 8 apples for a cog and a bevel.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(80);

        scenex.showTextComponent(100)
            .text(vendorTooltip(vendorSell))
            .item(vendorTooltipItem(vendorSell))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(110);

        scene.overlay().showText(130)
            .text("Use the vendor to buy 8 apples, sneak-use it to buy a whole stack")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(20);

        for (boolean bulk : Iterate.falseAndTrue) {
            tradeInteraction(
                scene,
                util,
                vendorSell,
                (bulk ? Coin.COG : Coin.BEVEL).asStack(9),
                new ItemStack(Items.GOLDEN_APPLE, bulk ? 64 : 8),
                bulk
            );
        }

        scene.idle(20);

        scene.world().hideIndependentSection(vendorSellLink, Direction.EAST);
        scene.idle(5);
        var vendorBuyLink = scene.world().showIndependentSection(util.select().position(vendorBuy), Direction.EAST);
        scene.world().moveSection(vendorBuyLink, Vec3.atLowerCornerOf(vendorSell.subtract(vendorBuy)), 20);
        scene.idle(30);

        scene.overlay().showText(70)
            .text("This vendor is buying 16 oak logs for a bevel.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(80);

        scenex.showTextComponent(100)
            .text(vendorTooltip(vendorBuy))
            .item(vendorTooltipItem(vendorBuy))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(110);

        scene.overlay().showText(130)
            .text("Use the vendor to sell 16 oak logs, sneak-use it to sell a whole stack")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(20);

        for (boolean bulk : Iterate.falseAndTrue) {
            tradeInteraction(
                scene,
                util,
                vendorSell,
                new ItemStack(Items.OAK_LOG, bulk ? 64 : 16),
                Coin.BEVEL.asStack(bulk ? 4 : 1),
                bulk
            );
        }
    }

    public static void configSell(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("vendor_config_sell", "Configuring Vendors for Selling");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos vendorPos = util.grid().at(1, 1, 1);
        Vec3 vendorText = util.vector().blockSurface(vendorPos, Direction.WEST).add(0, 0.5, 0);

        // for some reason the lighting is broken if we show it normally
        scene.world().showIndependentSection(util.select().position(vendorPos), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .text("Players with access to a vendor can sneak-use to configure it.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(50);

        scene.overlay().showControls(util.vector().topOf(vendorPos), Pointing.DOWN, 10)
            .rightClick()
            .whileSneaking();
        scene.idle(15);

        var menu = scenex.showContainerMenu(
                5,
                vendorPos,
                NumismaticsBlockEntities.VENDOR.get(),
                (be, inv) -> new VendorMenu(NumismaticsMenuTypes.VENDOR.get(), -3, inv, be),
                VendorScreen::new
            )
            .inventoryFiller(inv -> {
                inv.setItem(9, new ItemStack(Items.GOLDEN_APPLE, 64));
                inv.setItem(10, new ItemStack(Items.GOLDEN_APPLE, 8));
            })
            .colored(PonderPalette.RED)
            .attachKeyFrame()
            .link();
        scenex.enableScreenOverlayLayer();
        scene.idle(15);

        scenex.modifyCursor(menu, c -> c
            .setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW)
            .teleport(-20, -30)
            .snapFrame());
        scenex.showText(60, ScreenVec.slotRelative(menu, 20, 7, VendorMenu.FILTER_SLOT_INDEX))
            .text("The filter slot controls the item type and count that is sold.")
            .placeNearTarget();
        scene.idle(5);

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scenex.cursorTarget(ScreenVec.relative(menu, -20, 60));
        scene.idle(2);
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, VendorMenu.PLAYER_INV_START_INDEX + 10));
        scene.idle(18);

        // pick up 8 golden apples
        scenex.clickSlot(menu, VendorMenu.PLAYER_INV_START_INDEX + 10);
        scenex.modifyCursor(menu, $ -> $.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOWER));
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, VendorMenu.FILTER_SLOT_INDEX));
        scene.idle(10);

        scenex.showText(60, ScreenVec.slotRelative(menu, 20, 7, VendorMenu.INV_START_INDEX + 5))
            .text("Stock slots hold the inventory.")
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(5);

        // set filter
        scenex.clickSlot(menu, VendorMenu.FILTER_SLOT_INDEX);
        scene.idle(5);

        // place 8 golden apples in stock
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, VendorMenu.INV_START_INDEX));
        scene.idle(10);
        scenex.clickSlot(menu, VendorMenu.INV_START_INDEX);
        scene.idle(5);

        // pick up 64 golden apples
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, VendorMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(15);
        scenex.clickSlot(menu, VendorMenu.PLAYER_INV_START_INDEX + 9);
        scene.idle(5);

        // place 64 golden apples in stock
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, VendorMenu.INV_START_INDEX + 1));
        scene.idle(10);
        scenex.clickSlot(menu, VendorMenu.INV_START_INDEX + 1);
        scene.idle(5);

        scene.overlay().showText(150)
            .text("Configure each coin to set the price")
            .independent(50);
        scene.idle(5);

        scenex.cursorTarget(ScreenVec.relative(menu, 72, 80));
        scene.idle(15);

        scene.addKeyframe();
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.SCROLL_UP));

        for (int i = 1; i <= 8; i++) {
            final int finalI = i;
            scenex.modifyScreen(menu, $ -> $.screen().getVirtualHandle().setPrice(Coin.BEVEL, finalI));
            scene.idle(5);
        }

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(5);
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.SCROLL_DOWN));

        for (int i = 8; i >= 1; i--) {
            final int finalI = i;
            scenex.modifyScreen(menu, $ -> $.screen().getVirtualHandle().setPrice(Coin.BEVEL, finalI));
            scene.idle(5);
        }

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(5);
        scenex.cursorTarget(ScreenVec.relative(menu, 218, 58));
        scene.idle(10);

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.SCROLL_UP));
        scene.idle(5);
        scenex.modifyScreen(menu, $ -> $.screen().getVirtualHandle().setPrice(Coin.COG, 1));
        scene.idle(5);
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(20);

        // hide cursor
        scenex.modifyCursor(menu, c -> c.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_MEDIUM));
        scenex.cursorTarget(ScreenVec.relative(menu, 230, 130));
        scene.idle(10);

        scenex.hideContainerMenu(menu, 5);
        scenex.disableScreenOverlayLayer();
        scene.idle(10);

        // display final result
        scenex.showTextComponent(40)
            .text(vendorTooltip(vendorPos))
            .item(vendorTooltipItem(vendorPos))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(50);
    }

    public static void configBuy(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("vendor_config_buy", "Configuring Vendors for Buying");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos vendorPos = util.grid().at(1, 1, 1);
        Vec3 vendorText = util.vector().blockSurface(vendorPos, Direction.WEST).add(0, 0.5, 0);

        // for some reason the lighting is broken if we show it normally
        scene.world().showIndependentSection(util.select().position(vendorPos), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .text("Players with access to a vendor can sneak-use to configure it.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(50);

        scene.overlay().showControls(util.vector().topOf(vendorPos), Pointing.DOWN, 10)
            .rightClick()
            .whileSneaking();
        scene.idle(15);

        var menu = scenex.showContainerMenu(
                5,
                vendorPos,
                NumismaticsBlockEntities.VENDOR.get(),
                (be, inv) -> new VendorMenu(NumismaticsMenuTypes.VENDOR.get(), -3, inv, be),
                VendorScreen::new
            )
            .inventoryFiller(inv -> {
                inv.setItem(9, Coin.BEVEL.asStack(64));
                inv.setItem(10, new ItemStack(Items.OAK_LOG, 1));
            })
            .attachKeyFrame()
            .link();
        scenex.enableScreenOverlayLayer();
        scene.idle(15);

        scenex.modifyCursor(menu, c -> c
            .setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW)
            .teleport(-20, -30)
            .snapFrame());

        // move to buy/sell toggle
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scenex.cursorTarget(ScreenVec.relative(menu, 30, -30));
        scene.idle(2);
        scenex.showText(60, ScreenVec.relative(menu, 160, 48))
            .text("Configure the vendor to buy from customers")
            .placeNearTarget();
        scenex.cursorTarget(ScreenVec.relative(menu, 115, 53));
        scene.idle(18);

        // scroll to set buy mode
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.SCROLL_DOWN));
        scene.idle(10);
        scenex.modifyScreen(menu, $ -> $.screen().getVirtualHandle().setMode(VendorBlockEntity.Mode.BUY));
        scene.idle(10);
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scenex.modifyCursor(menu, c -> c.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOWER));
        scene.idle(20);

        // start configuring inventory: move to oak logs
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, VendorMenu.PLAYER_INV_START_INDEX + 10));
        scene.idle(15);

        // pick up 1 oak log
        scenex.clickSlot(menu, VendorMenu.PLAYER_INV_START_INDEX + 10);
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, VendorMenu.FILTER_SLOT_INDEX));
        scene.idle(10);

        // set filter
        scene.addInstruction($ -> $.getWorld().random.setSeed(87195871L)); // consistent clearing velocity
        scenex.clickSlot(menu, VendorMenu.FILTER_SLOT_INDEX);
        scene.addInstruction($ -> $.forEachWorldEntity(ItemEntity.class, $$ ->
            $$.setDeltaMovement($$.getDeltaMovement().scale(1.5).add(0, 0.25, 0)))); // add some pizzazz to the cleared items
        scene.idle(5);

        // swap logs for bevels
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, VendorMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(10);
        scenex.clickSlot(menu, VendorMenu.PLAYER_INV_START_INDEX + 9);
        scenex.showText(60, ScreenVec.slotRelative(menu, 18, 7, VendorMenu.COIN_SLOTS - 1))
            .text("Provide funds to pay customers with")
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(5);

        // place bevels in coin supply
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, 1));
        scene.idle(10);
        scenex.clickSlot(menu, 1);
        scene.idle(10);

        // set (cog) prices
        scenex.cursorTarget(ScreenVec.relative(menu, 218, 58));
        scene.idle(20);

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.SCROLL_DOWN));
        scene.idle(5);
        scenex.modifyScreen(menu, $ -> $.screen().getVirtualHandle().setPrice(Coin.COG, 0));
        scene.idle(5);
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(15);

        // configure filter count
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, VendorMenu.FILTER_SLOT_INDEX));
        scenex.showText(60, ScreenVec.slotRelative(menu, 20, 7, VendorMenu.FILTER_SLOT_INDEX))
            .text("Scroll to modify the filter count")
            .placeNearTarget();
        scene.idle(15);
        scene.addKeyframe();

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.SCROLL_UP));
        for (int i = 1; i < 16; i++) {
            scenex.modifyScreen(menu, $ -> $.menu().scrollSlot(VendorMenu.FILTER_SLOT_INDEX, 1, false));
            scene.idle(5);
        }
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(15);

        // move towards Automated Extraction button
        scenex.cursorTarget(ScreenVec.relative(menu, 100, 165));
        scene.idle(2);
        scenex.cursorTarget(ScreenVec.relative(menu, 40, 135));
        scene.idle(4);

        // toggle Automated Extraction
        scenex.showText(80, ScreenVec.relative(menu, 50, 136))
            .text("Hoppers, funnels, etc. can extract stock from buy-mode vendors by default. You can disable this.")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(24);
        scenex.modifyScreen(menu, $ -> $.screen().getVirtualHandle().toggleExtraction());

        scene.idle(70);

        // hide cursor
        scenex.modifyCursor(menu, c -> c.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW));
        scenex.cursorTarget(ScreenVec.relative(menu, 120, 160));
        scene.idle(2);
        scenex.cursorTarget(ScreenVec.relative(menu, 230, 140));
        scene.idle(12);

        scenex.hideContainerMenu(menu, 5);
        scenex.disableScreenOverlayLayer();
        scene.idle(10);

        // display final result
        scenex.showTextComponent(40)
            .text(vendorTooltip(vendorPos))
            .item(vendorTooltipItem(vendorPos))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(50);
    }

    public static void configEmi(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("vendor_config_emi", "Configuring Vendors with EMI/JEI");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos vendorPos = util.grid().at(1, 1, 1);
        Vec3 vendorText = util.vector().blockSurface(vendorPos, Direction.WEST).add(0, 0.5, 0);

        // for some reason the lighting is broken if we show it normally
        scene.world().showIndependentSection(util.select().position(vendorPos), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .text("Players with access to a vendor can sneak-use to configure it.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(50);

        scene.overlay().showControls(util.vector().topOf(vendorPos), Pointing.DOWN, 10)
            .rightClick()
            .whileSneaking();
        scene.idle(15);

        Function<ResourceKey<Enchantment>, Holder<Enchantment>> enchants;
        {
            if (Utils.isDataGen()) {
                enchants = $ -> null;
            } else {
                var regs = Minecraft.getInstance().level.registryAccess();
                enchants = regs.registryOrThrow(Registries.ENCHANTMENT)::getHolderOrThrow;
            }
        }

        List<ItemStack> fakeEmiItems = new ArrayList<>();
        fakeEmiItems.add(new ItemStack(Items.WHITE_DYE));
        fakeEmiItems.add(new ItemStack(Items.LIGHT_GRAY_DYE));
        fakeEmiItems.add(new ItemStack(Items.GRAY_DYE));
        fakeEmiItems.add(new ItemStack(Items.BLACK_DYE));
        fakeEmiItems.add(new ItemStack(Items.LEATHER_CHESTPLATE));

        fakeEmiItems.add(new ItemStack(Items.BROWN_DYE));
        fakeEmiItems.add(new ItemStack(Items.RED_DYE));
        fakeEmiItems.add(new ItemStack(Items.ORANGE_DYE));
        fakeEmiItems.add(new ItemStack(Items.YELLOW_DYE));
        fakeEmiItems.add(enchantedBook(enchants.apply(Enchantments.MENDING), 1));

        fakeEmiItems.add(new ItemStack(Items.LIME_DYE));
        fakeEmiItems.add(new ItemStack(Items.GREEN_DYE));
        fakeEmiItems.add(new ItemStack(Items.CYAN_DYE));
        fakeEmiItems.add(new ItemStack(Items.LIGHT_BLUE_DYE));
        fakeEmiItems.add(enchantedBook(enchants.apply(Enchantments.UNBREAKING), 3));

        fakeEmiItems.add(new ItemStack(Items.BLUE_DYE));
        fakeEmiItems.add(new ItemStack(Items.PURPLE_DYE));
        fakeEmiItems.add(new ItemStack(Items.MAGENTA_DYE));
        fakeEmiItems.add(new ItemStack(Items.PINK_DYE));
        fakeEmiItems.add(enchantedBook(enchants.apply(Enchantments.PROTECTION), 4));

        var menu = scenex.showContainerMenu(
                5,
                vendorPos,
                NumismaticsBlockEntities.VENDOR.get(),
                (be, inv) -> new VendorMenu(NumismaticsMenuTypes.VENDOR.get(), -3, inv, be),
                VendorScreen::new
            )
            .slotFiller(filler -> {
                final Container fakeEmiContainer = new SimpleContainer(fakeEmiItems.size());
                final int x0 = 260;
                final int y0 = 0;
                for (int i = 0; i < fakeEmiItems.size(); i++) {
                    int xi = i % 5;
                    int yi = i / 5;
                    filler.apply(new Slot(fakeEmiContainer, i, x0 + xi * 18, y0 + yi * 18));
                }
            })
            .inventoryFiller(inv -> {})
            .colored(PonderPalette.RED)
            .attachKeyFrame()
            .link();
        scenex.enableScreenOverlayLayer();
        scene.idle(15);

        scenex.modifyCursor(menu, c -> c
            .setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW)
            .teleport(-20, -30)
            .snapFrame());

        scene.overlay().showText(70)
            .text("To set the filter item of a vendor (or salepoint) to an item you don't have...")
            .independent(65);
        scene.idle(20);
        scene.overlay().showText(90)
            .text("...use a recipe viewer such as EMI or JEI and drag items to the filter slot.")
            .independent(105);
        scene.idle(10);
        for (int i = 0; i < fakeEmiItems.size(); i++) {
            final int i$ = i;
            scenex.modifyScreen(menu, $ -> $.menu()
                .getSlot(VendorMenu.PLAYER_INV_END_INDEX + 6 + i$)
                .set(fakeEmiItems.get(i$)));
            scene.idle(1);
        }
        scene.idle(50);
        scene.addKeyframe();

        // move to fake EMI leather chestplate
        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scenex.cursorTarget(ScreenVec.relative(menu, 30, -60));
        scene.idle(2);
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, VendorMenu.PLAYER_INV_END_INDEX + 6 + 4));
        scene.idle(15);

        // pick up fake EMI leather chestplate
        scenex.cloneMenuSlotToCarried(menu, VendorMenu.PLAYER_INV_END_INDEX + 6 + 4);
        scenex.modifyCursor(menu, $ -> $.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOWER));
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, VendorMenu.FILTER_SLOT_INDEX));
        scene.idle(10);

        // set filter
        scenex.clickSlot(menu, VendorMenu.FILTER_SLOT_INDEX);
        scenex.modifyScreen(menu, $ -> $.menu().setCarried(ItemStack.EMPTY));
        scene.idle(10);

        scene.overlay().showText(70)
            .text("Items such as dyes and enchanted books can be sneak-dragged to modify the filter item.")
            .independent(105)
            .attachKeyFrame();
        scene.idle(10);

        scenex.modifyCursor(menu, $ -> $.setPhysics(new CursorPhysicsProperties(60, 0.8)));

        for (int emiIdx : new int[] {6, 7, 17, 9, 14, 19}) {
            // pick up dye/enchanted book
            scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, VendorMenu.PLAYER_INV_END_INDEX + 6 + emiIdx));
            scene.idle(20);
            scenex.cloneMenuSlotToCarried(menu, VendorMenu.PLAYER_INV_END_INDEX + 6 + emiIdx);

            scenex.modifyCursor(menu, c -> c.setSneak(true));
            scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, VendorMenu.FILTER_SLOT_INDEX));
            scene.idle(15);

            // set filter
            scenex.modifyCursor(menu, c -> c.setSneak(false));
            scenex.modifyScreen(menu, $ -> {
                ItemStack carried = $.menu().getCarried();
                $.menu().setCarried(ItemStack.EMPTY);
                Slot slot = $.menu().getSlot(VendorMenu.FILTER_SLOT_INDEX);
                ItemStack existing = slot.getItem();
                ClientCraftingUtils.Result result = ClientCraftingUtils.applyStackingCrafts(existing, carried);
                slot.set(result.getResult(existing, true));
            });
            scene.idle(10);
        }

        scene.idle(40);

        // hide cursor
        scenex.modifyCursor(menu, c -> c.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW));
        scenex.cursorTarget(ScreenVec.relative(menu, 230, 140));
        scene.idle(15);

        scenex.hideContainerMenu(menu, 5);
        scenex.disableScreenOverlayLayer();
        scene.idle(10);

        // display final result
        scenex.showTextComponent(60)
            .text(vendorTooltip(vendorPos))
            .item(vendorTooltipItem(vendorPos))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(70);
    }

    private static ItemStack enchantedBook(Holder<Enchantment> enchantment, int level) {
        if (Utils.isDataGen())
            return new ItemStack(Items.ENCHANTED_BOOK);
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
    }

    private static BiConsumer<PonderScene, List<Component>> vendorTooltip(BlockPos pos) {
        return (scene, tooltip) -> {
            if (scene.getWorld().getBlockEntity(pos) instanceof VendorBlockEntity vbe) {
                vbe.addToTooltip(tooltip, false);
            }
        };
    }

    private static Function<PonderScene, ItemStack> vendorTooltipItem(BlockPos pos) {
        return (scene) -> {
            if (scene.getWorld().getBlockEntity(pos) instanceof VendorBlockEntity vbe) {
                return vbe.getIcon(false);
            } else {
                return ItemStack.EMPTY;
            }
        };
    }

    private static void tradeInteraction(SceneBuilder scene, SceneBuildingUtil util, BlockPos vendorPos,
                                         ItemStack intoVendor, ItemStack fromVendor, boolean bulk) {
        InputWindowElement iwe = new InputWindowElement(util.vector().topOf(vendorPos), Pointing.DOWN);
        iwe.builder()
            .withItem(intoVendor)
            .rightClick();
        if (bulk) iwe.builder().whileSneaking();
        ((InputWindowElement_Duck) iwe).numismatics$showItemCount(true);
        scene.addInstruction(new ShowInputInstruction(iwe, 40));
        scene.idle(6);

        scene.effects().indicateSuccess(vendorPos);
        var item = scene.world().createItemEntity(
            util.vector().blockSurface(vendorPos, Direction.NORTH),
            new Vec3(0, 0.15, -0.15),
            fromVendor
        );
        scene.idle(36);
        scene.idle(10);

        scene.world().modifyEntity(item, Entity::discard);
    }
}
