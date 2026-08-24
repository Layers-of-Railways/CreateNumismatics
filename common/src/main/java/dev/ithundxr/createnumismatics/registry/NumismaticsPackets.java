/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerEditPacket;
import dev.ithundxr.createnumismatics.registry.packets.AndesiteDepositorConfigurationPacket;
import dev.ithundxr.createnumismatics.registry.packets.BankAccountLabelPacket;
import dev.ithundxr.createnumismatics.registry.packets.GhostItemSubmitPacket;
import dev.ithundxr.createnumismatics.registry.packets.OpenTrustListPacket;
import dev.ithundxr.createnumismatics.registry.packets.SalepointCardPacket;
import dev.ithundxr.createnumismatics.registry.packets.SalepointEnergyFilterPacket;
import dev.ithundxr.createnumismatics.registry.packets.SalepointFluidFilterPacket;
import dev.ithundxr.createnumismatics.registry.packets.SalepointPurchasePacket;
import dev.ithundxr.createnumismatics.registry.packets.ScrollSlotPacket;
import dev.ithundxr.createnumismatics.registry.packets.SetAdminModePacket;
import dev.ithundxr.createnumismatics.registry.packets.VarIntContainerSetDataPacket;
import dev.ithundxr.createnumismatics.registry.packets.VendorConfigurationPacket;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.AddSubAccountPacket;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.ConfigureSubAccountPacket;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.OpenSubAccountEditScreenPacket;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.OpenSubAccountsMenuPacket;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.RemoveSubAccountPacket;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.ResetSubAccountSpendingPacket;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.UpdateSubAccountsPacket;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.BasePacketPayload.PacketTypeProvider;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum NumismaticsPackets implements PacketTypeProvider {
    // C2S
    SLIDER_STYLE_PRICE_CONFIGURATION(SliderStylePriceConfigurationPacket.class, SliderStylePriceConfigurationPacket.STREAM_CODEC),
    BLAZE_BANKER_EDIT(BlazeBankerEditPacket.class, BlazeBankerEditPacket.STREAM_CODEC),
    ANDESITE_DEPOSITOR_CONFIGURATION(AndesiteDepositorConfigurationPacket.class, AndesiteDepositorConfigurationPacket.STREAM_CODEC),
    OPEN_TRUST_LIST(OpenTrustListPacket.class, OpenTrustListPacket.STREAM_CODEC),
    VENDOR_CONFIGURATION(VendorConfigurationPacket.class, VendorConfigurationPacket.STREAM_CODEC),
    OPEN_SUB_ACCOUNTS_MENU(OpenSubAccountsMenuPacket.class, OpenSubAccountsMenuPacket.STREAM_CODEC),
    OPEN_SUB_ACCOUNT_EDIT_SCREEN(OpenSubAccountEditScreenPacket.class, OpenSubAccountEditScreenPacket.STREAM_CODEC),
    REMOVE_SUB_ACCOUNT(RemoveSubAccountPacket.class, RemoveSubAccountPacket.STREAM_CODEC),
    RESET_SUB_ACCOUNT_SPENDING(ResetSubAccountSpendingPacket.class, ResetSubAccountSpendingPacket.STREAM_CODEC),
    CONFIGURE_SUB_ACCOUNT(ConfigureSubAccountPacket.class, ConfigureSubAccountPacket.STREAM_CODEC),
    ADD_SUB_ACCOUNT(AddSubAccountPacket.class, AddSubAccountPacket.STREAM_CODEC),
    SCROLL_SLOT(ScrollSlotPacket.class, ScrollSlotPacket.STREAM_CODEC),
    GHOST_ITEM_SUBMIT(GhostItemSubmitPacket.class, GhostItemSubmitPacket.STREAM_CODEC),
    SALEPOINT_PURCHASE(SalepointPurchasePacket.class, SalepointPurchasePacket.STREAM_CODEC),
    SALEPOINT_FLUID_FILTER(SalepointFluidFilterPacket.class, SalepointFluidFilterPacket.STREAM_CODEC),
    SALEPOINT_ENERGY_FILTER(SalepointEnergyFilterPacket.class, SalepointEnergyFilterPacket.STREAM_CODEC),

    // S2C
    BANK_ACCOUNT_LABEL(BankAccountLabelPacket.class, BankAccountLabelPacket.STREAM_CODEC),
    VAR_INT_CONTAINER_SET_DATA(VarIntContainerSetDataPacket.class, VarIntContainerSetDataPacket.STREAM_CODEC),

    UPDATE_SUB_ACCOUNTS(UpdateSubAccountsPacket.class, UpdateSubAccountsPacket.STREAM_CODEC),
    SALEPOINT_CARD(SalepointCardPacket.class, SalepointCardPacket.STREAM_CODEC),
    SET_ADMIN_MODE(SetAdminModePacket.class, SetAdminModePacket.STREAM_CODEC);

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> NumismaticsPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(Numismatics.asResource(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(Numismatics.MOD_ID, 2); // increment version on changes
        for (NumismaticsPackets packet : NumismaticsPackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }
}
