package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.AllShapes.Builder;
import com.simibubi.create.foundation.utility.VoxelShaper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.NORTH;

public class NumismaticsShapes {

    public static final VoxelShaper
        BANK_TERMINAL = shape(0, 0, 0, 16, 16, 8)
        .add(0, 0, 8, 16, 8, 16)
        .add(1, 8, 8, 15, 15, 15)
        .forDirectional(NORTH);

    private static Builder shape(VoxelShape shape) {
        return new Builder(shape);
    }

    public static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }
}
