/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.registry.packets.sub_account;

import dev.ithundxr.createnumismatics.content.backend.sub_authorization.AuthorizationType;
import dev.ithundxr.createnumismatics.content.bank.SubAccountListMenu;
import dev.ithundxr.createnumismatics.multiloader.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConfigureSubAccountPacket implements C2SPacket {

    private final @NotNull UUID subAccountID;
    private final @NotNull Type type;

    private @Nullable Integer limit;
    private @Nullable AuthorizationType authorizationType;
    private @Nullable String label;

    public ConfigureSubAccountPacket(@NotNull UUID subAccountID, @Nullable Integer limit) {
        this(subAccountID, Type.LIMIT);

        this.limit = limit;
    }

    public ConfigureSubAccountPacket(@NotNull UUID subAccountID, @NotNull AuthorizationType authorizationType) {
        this(subAccountID, Type.AUTHORIZATION_TYPE);

        this.authorizationType = authorizationType;
    }

    public ConfigureSubAccountPacket(@NotNull UUID subAccountID, @NotNull String label) {
        this(subAccountID, Type.LABEL);

        this.label = label;
    }

    private ConfigureSubAccountPacket(@NotNull UUID subAccountID, @NotNull Type type) {
        this.subAccountID = subAccountID;
        this.type = type;
    }

    public ConfigureSubAccountPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readEnum(Type.class));

        switch (type) {
            case LIMIT:
                if (buf.readBoolean())
                    limit = buf.readVarInt();
                break;
            case AUTHORIZATION_TYPE:
                authorizationType = AuthorizationType.values()[buf.readByte()];
                break;
            case LABEL:
                label = buf.readUtf();
                break;
        }
    }

    @Override
    @SuppressWarnings("DataFlowIssue") // IntelliJ complains about nullability of limit, authorizationType, and label
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(subAccountID);
        buffer.writeEnum(type);

        switch (type) {
            case LIMIT:
                buffer.writeBoolean(limit != null);
                if (limit != null)
                    buffer.writeVarInt(limit);
                break;
            case AUTHORIZATION_TYPE:
                buffer.writeByte(authorizationType.ordinal());
                break;
            case LABEL:
                buffer.writeUtf(label);
                break;
        }
    }

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof SubAccountListMenu subAccountListMenu) {
            switch (type) {
                case LIMIT:
                    subAccountListMenu.setLimit(subAccountID, limit);
                    break;
                case AUTHORIZATION_TYPE:
                    subAccountListMenu.setAuthorizationType(subAccountID, authorizationType);
                    break;
                case LABEL:
                    subAccountListMenu.setLabel(subAccountID, label);
                    break;
            }
        }
    }

    private enum Type {
        LIMIT,
        AUTHORIZATION_TYPE,
        LABEL
    }
}
