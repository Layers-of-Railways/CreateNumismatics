package dev.ithundxr.createnumismatics.base.data.lang;

import com.google.gson.JsonElement;
import com.simibubi.create.foundation.data.LangPartial;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.Numismatics;

import java.util.function.Supplier;

public enum NumismaticsLangPartials implements LangPartial {
    INTERFACE("UI & Messages"),
    TOOLTIPS("Item Descriptions"),

    ;

    private final String display;
    private final Supplier<JsonElement> provider;

    private NumismaticsLangPartials(String display) {
        this.display = display;
        this.provider = this::fromResource;
    }

    private NumismaticsLangPartials(String display, Supplier<JsonElement> customProvider) {
        this.display = display;
        this.provider = customProvider;
    }

    public String getDisplayName() {
        return display;
    }

    public JsonElement provide() {
        return provider.get();
    }

    private JsonElement fromResource() {
        String fileName = Lang.asId(name());
        String filepath = "assets/" + Numismatics.MOD_ID + "/lang/default/" + fileName + ".json";
        JsonElement element = FilesHelper.loadJsonResource(filepath);
        if (element == null)
            throw new IllegalStateException(String.format("Could not find default lang file: %s", filepath));
        return element;
    }
}
