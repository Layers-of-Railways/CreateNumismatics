package dev.ithundxr.createnumismatics;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.base.data.lang.NumismaticsLangPartials;
import dev.ithundxr.createnumismatics.content.bank.GlobalBankManager;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsCommands;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class Numismatics {
    public static final String MOD_ID = "numismatics";
    public static final String NAME = "Create: Numismatics";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final GlobalBankManager BANK = new GlobalBankManager();


    public static void init() {
        LOGGER.info("{} initializing! Create version: {} on platform: {}", NAME, Create.VERSION, Utils.platformName());
        NumismaticsBlocks.init(); // hold registrate in a separate class to avoid loading early on forge
        NumismaticsItems.init();

        registerCommands(NumismaticsCommands::register);
    }

    public static CreateRegistrate registrate() {
        return NumismaticsBlocks.REGISTRATE;
    }

    public static void gatherData(DataGenerator gen) {
        PonderLocalization.provideRegistrateLang(registrate());
        gen.addProvider(true, new LangMerger(gen, MOD_ID, "Numismatics", NumismaticsLangPartials.values()));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void crashDev(String message) {
        if (Utils.isDevEnv()) {
            throw new RuntimeException(message);
        } else {
            LOGGER.error(message);
        }
    }

    @ExpectPlatform
    public static void registerCommands(BiConsumer<CommandDispatcher<CommandSourceStack>, Boolean> consumer) {
        throw new AssertionError();
    }
}
