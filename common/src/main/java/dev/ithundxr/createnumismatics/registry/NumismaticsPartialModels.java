package dev.ithundxr.createnumismatics.registry;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.Create;
import dev.ithundxr.createnumismatics.Numismatics;

public class NumismaticsPartialModels {
    public static final PartialModel
        TOP_HAT = entity("tophat")
        ;

    private static PartialModel createBlock(String path) {
        return new PartialModel(Create.asResource("block/" + path));
    }

    private static PartialModel block(String path) {
        return new PartialModel(Numismatics.asResource("block/" + path));
    }

    private static PartialModel entity(String path) {
        return new PartialModel(Numismatics.asResource("entity/" + path));
    }


    @SuppressWarnings("EmptyMethod")
    public static void init() {
    }
}
