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

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerEditPacket;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.multiloader.PacketSet;
import dev.ithundxr.createnumismatics.registry.packets.*;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.*;

public class NumismaticsPackets {
    public static final PacketSet PACKETS = PacketSet.builder(Numismatics.MOD_ID, 4) // increment version on changes

        .c2s(SliderStylePriceConfigurationPacket.class, SliderStylePriceConfigurationPacket::new)
        .c2s(BlazeBankerEditPacket.class, BlazeBankerEditPacket::new)
        .c2s(AndesiteDepositorConfigurationPacket.class, AndesiteDepositorConfigurationPacket::new)
        .c2s(OpenTrustListPacket.class, OpenTrustListPacket::new)
        .c2s(VendorConfigurationPacket.class, VendorConfigurationPacket::new)
        .c2s(OpenSubAccountsMenuPacket.class, OpenSubAccountsMenuPacket::new)
        .c2s(OpenSubAccountEditScreenPacket.class, OpenSubAccountEditScreenPacket::new)
        .c2s(RemoveSubAccountPacket.class, RemoveSubAccountPacket::new)
        .c2s(ResetSubAccountSpendingPacket.class, ResetSubAccountSpendingPacket::new)
        .c2s(ConfigureSubAccountPacket.class, ConfigureSubAccountPacket::new)
        .c2s(AddSubAccountPacket.class, AddSubAccountPacket::new)
        .c2s(ScrollSlotPacket.class, ScrollSlotPacket::new)

        .s2c(BankAccountLabelPacket.class, BankAccountLabelPacket::new)
        .s2c(VarIntContainerSetDataPacket.class, VarIntContainerSetDataPacket::new)

        .s2c(BigStackSizeContainerSetSlotPacket.class, BigStackSizeContainerSetSlotPacket::new)
        .s2c(BigStackSizeContainerSetContentPacket.class, BigStackSizeContainerSetContentPacket::new)
        .s2c(UpdateSubAccountsPacket.class, UpdateSubAccountsPacket::new)

        .build();
}
