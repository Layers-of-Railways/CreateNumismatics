package dev.ithundxr.createnumismatics;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.tterrag.registrate.Registrate;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.base.data.lang.NumismaticsLangPartials;
import dev.ithundxr.createnumismatics.content.bank.GlobalBankManager;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsCommands;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
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

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .creativeModeTab(() -> NumismaticsItems.mainCreativeTab, "Create: Numismatics");

    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public static void init() {
        LOGGER.info("{} {} initializing! Create version: {} on platform: {}", NAME, VERSION, Create.VERSION, Utils.platformName());

        ModSetup.register();
        finalizeRegistrate();

        registerCommands(NumismaticsCommands::register);

        if (Utils.isDevEnv()) {
            SharedConstants.IS_RUNNING_IN_IDE = true;
        }
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

    public static void gatherData(DataGenerator gen) {
        PonderLocalization.provideRegistrateLang(REGISTRATE);
        gen.addProvider(true, new LangMerger(gen, MOD_ID, "Numismatics", NumismaticsLangPartials.values()));
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
