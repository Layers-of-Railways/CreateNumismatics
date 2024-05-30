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

package dev.ithundxr.createnumismatics.mixin.client;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @Shadow protected EditBox input;

    @Shadow private String initial;

    @Inject(method = "onEdited", at = @At("RETURN"))
    private void replaceCoinNames(String value, CallbackInfo ci) {
        String string = input.getValue();

        if (!string.startsWith("/") && !string.equals(initial)) {
            int originalLength = string.length();

            for (Coin coin : Coin.values()) {
                string = string.replaceAll(":"+coin.getName()+":", coin.fontChar);
            }

            if (string.length() != originalLength)
                input.setValue(string);
        }
    }
}
