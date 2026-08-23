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
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerEditPacket;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.registry.packets.*;
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
    
    // S2C
    BANK_ACCOUNT_LABEL(BankAccountLabelPacket.class, BankAccountLabelPacket.STREAM_CODEC),
    VAR_INT_CONTAINER_SET_DATA(VarIntContainerSetDataPacket.class, VarIntContainerSetDataPacket.STREAM_CODEC),

    VENDOR_CONTAINER_SET_SLOT(VendorContainerSetSlotPacket.class, VendorContainerSetSlotPacket.STREAM_CODEC),
    VENDOR_CONTAINER_SET_CONTENT(VendorContainerSetContentPacket.class, VendorContainerSetContentPacket.STREAM_CODEC);

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

    /*
    TODO
        .c2s(OpenSubAccountsMenuPacket.class, OpenSubAccountsMenuPacket::new)
        .c2s(OpenSubAccountEditScreenPacket.class, OpenSubAccountEditScreenPacket::new)
        .c2s(RemoveSubAccountPacket.class, RemoveSubAccountPacket::new)
        .c2s(ResetSubAccountSpendingPacket.class, ResetSubAccountSpendingPacket::new)
        .c2s(ConfigureSubAccountPacket.class, ConfigureSubAccountPacket::new)
        .c2s(AddSubAccountPacket.class, AddSubAccountPacket::new)
        .c2s(ScrollSlotPacket.class, ScrollSlotPacket::new)
        .c2s(GhostItemSubmitPacket.class, GhostItemSubmitPacket::new)
        .c2s(SalepointPurchasePacket.class, SalepointPurchasePacket::new)
        .c2s(SalepointFluidFilterPacket.class, SalepointFluidFilterPacket::new)
        .c2s(SalepointEnergyFilterPacket.class, SalepointEnergyFilterPacket::new)

        .s2c(UpdateSubAccountsPacket.class, UpdateSubAccountsPacket::new)
        .s2c(SalepointCardPacket.class, SalepointCardPacket::new)
        .s2c(SetAdminModePacket.class, SetAdminModePacket::new)
     */
}
