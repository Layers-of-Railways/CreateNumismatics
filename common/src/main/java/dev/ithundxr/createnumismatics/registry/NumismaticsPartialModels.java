package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.ithundxr.createnumismatics.Numismatics;

public class NumismaticsPartialModels {
    public static final PartialModel
        TOP_HAT = entity("tophat")
        ;

    private static PartialModel createBlock(String path) {
        return PartialModel.of(Create.asResource("block/" + path));
    }

    private static PartialModel block(String path) {
        return PartialModel.of(Numismatics.asResource("block/" + path));
    }

    private static PartialModel entity(String path) {
        return PartialModel.of(Numismatics.asResource("entity/" + path));
    }


    @SuppressWarnings("EmptyMethod")
    public static void init() {
    }
}
