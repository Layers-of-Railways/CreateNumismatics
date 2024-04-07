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
