package dev.ithundxr.createnumismatics;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.tterrag.registrate.providers.ProviderType;
import dev.ithundxr.createnumismatics.base.data.lang.NumismaticsLangPartials;
import dev.ithundxr.createnumismatics.util.PlatformUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Numismatics {
    public static final String MOD_ID = "numismatics";
    public static final String NAME = "Create: Numismatics";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);


    public static void init() {
        LOGGER.info("{} initializing! Create version: {} on platform: {}", NAME, Create.VERSION, PlatformUtil.platformName());
        NumismaticsBlocks.init(); // hold registrate in a separate class to avoid loading early on forge
        NumismaticsItems.init();
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
}
