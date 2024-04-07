package dev.ithundxr.createnumismatics;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.tterrag.registrate.providers.ProviderType;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.base.data.NumismaticsTagGen;
import dev.ithundxr.createnumismatics.base.data.lang.NumismaticsLangGen;
import dev.ithundxr.createnumismatics.base.data.recipe.NumismaticsSequencedAssemblyRecipeGen;
import dev.ithundxr.createnumismatics.base.data.recipe.NumismaticsStandardRecipeGen;
import dev.ithundxr.createnumismatics.content.backend.GlobalBankManager;
import dev.ithundxr.createnumismatics.multiloader.Loader;
import dev.ithundxr.createnumismatics.registry.NumismaticsAdvancements;
import dev.ithundxr.createnumismatics.registry.NumismaticsCommands;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs.Tabs;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class Numismatics {
    public static final String MOD_ID = "numismatics";
    public static final String NAME = "Create: Numismatics";
    public static final String VERSION = findVersion();
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final GlobalBankManager BANK = new GlobalBankManager();

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
        Tabs.MAIN.use();
    }

    public static void init() {
        LOGGER.info("{} {} initializing! Create version: {} on platform: {}", NAME, VERSION, Create.VERSION, Loader.getCurrent());

        ModSetup.register();
        finalizeRegistrate();

        registerCommands(NumismaticsCommands::register);
        NumismaticsPackets.PACKETS.registerC2SListener();

        if (Utils.isDevEnv() && Loader.FABRIC.isCurrent()) {
            SharedConstants.IS_RUNNING_IN_IDE = false; // enable this to test commands
        }
    }

    public static void postRegistrationInit() {
        ModSetupLate.registerPostRegistration();
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    @ExpectPlatform
    public static String findVersion() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void finalizeRegistrate() {
        throw new AssertionError();
    }

    public static void gatherData(DataGenerator.PackGenerator gen) {
        REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, NumismaticsTagGen::generateBlockTags);
        REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, NumismaticsTagGen::generateItemTags);
        REGISTRATE.addDataGenerator(ProviderType.LANG, NumismaticsLangGen::generate);
        PonderLocalization.provideRegistrateLang(REGISTRATE);
        gen.addProvider(NumismaticsSequencedAssemblyRecipeGen::new);
        gen.addProvider(NumismaticsStandardRecipeGen::new);
        gen.addProvider(NumismaticsAdvancements::new);
    }

    public static ResourceLocation asResource(String path) {
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
