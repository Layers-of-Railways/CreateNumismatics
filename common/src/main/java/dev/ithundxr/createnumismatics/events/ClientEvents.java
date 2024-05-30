/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.events;

import dev.ithundxr.createnumismatics.annotation.event.MultiLoaderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientEvents {
    @MultiLoaderEvent
    public static Component clientReceiveMessage(Component message, boolean overlay) {
        String messageString = message.getString();
        String replaceString = "<numismatics replace on client>";

        if (messageString.equals("Hold " + replaceString + " to break this block") && overlay) {
            String keyName = Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage().getString();

            return Component.literal(messageString.replace(replaceString, keyName)).setStyle(message.getStyle());
        }

        return message;
    }
}
