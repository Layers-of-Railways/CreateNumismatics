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

package dev.ithundxr.createnumismatics.content.backend.sub_authorization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Authorization {
    boolean isHuman();

    @Nullable
    UUID getPersonalID();

    @NotNull
    UUID getAuthorizationID();

    final class Player implements Authorization {

        @Nullable
        private final UUID uuid;

        @NotNull
        private final UUID authorizationID;

        public Player(@NotNull net.minecraft.world.entity.player.Player player, @NotNull UUID authorizationID) {
            this.uuid = player.getUUID();
            this.authorizationID = authorizationID;
        }

        @Override
        public boolean isHuman() {
            return true;
        }

        @Override
        public @Nullable UUID getPersonalID() {
            return uuid;
        }

        @Override
        public @NotNull UUID getAuthorizationID() {
            return authorizationID;
        }
    }

    final class Automation implements Authorization {

        @Nullable
        private final UUID uuid;

        @NotNull
        private final UUID authorizationID;

        public Automation(@Nullable UUID uuid, @NotNull UUID authorizationID) {
            this.uuid = uuid;
            this.authorizationID = authorizationID;
        }

        @Override
        public boolean isHuman() {
            return false;
        }

        @Override
        public @Nullable UUID getPersonalID() {
            return uuid;
        }

        @Override
        public @NotNull UUID getAuthorizationID() {
            return authorizationID;
        }
    }

    final class Anonymous implements Authorization {

        @NotNull
        private final UUID authorizationID;

        public Anonymous(@NotNull UUID authorizationID) {
            this.authorizationID = authorizationID;
        }

        @Override
        public boolean isHuman() {
            return false;
        }

        @Override
        public @Nullable UUID getPersonalID() {
            return null;
        }

        @Override
        public @NotNull UUID getAuthorizationID() {
            return authorizationID;
        }
    }
}
