/*
 * Numismatics
 * Copyright (c) 2025-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.ponder.utils.dev_export.PonderExport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;

import static com.simibubi.create.infrastructure.command.PonderCommand.ITEM_PONDERS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class UtilCommand {
    public static LiteralCommandNode<CommandSourceStack> build() {
        return literal("util")
            .requires(cs -> cs.hasPermission(2))
            .then(ponder())
            .then(export_ponder())
            .build();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> ponder() {
        return literal("ponder")
            .then(argument("pos", BlockPosArgument.blockPos())
                .then(argument("size", IntegerArgumentType.integer(1))
                    .then(literal("named")
                        .then(argument("name", StringArgumentType.greedyString())
                            .executes(ctx -> $ponder(
                                ctx.getSource(),
                                BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                IntegerArgumentType.getInteger(ctx, "size"),
                                StringArgumentType.getString(ctx, "name")
                            ))
                        )
                    )
                    .then(argument("height", IntegerArgumentType.integer(1))
                        .then(literal("named")
                            .then(argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> $ponder(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "size"),
                                    IntegerArgumentType.getInteger(ctx, "height"),
                                    StringArgumentType.getString(ctx, "name")
                                ))
                            )
                        )
                    )
                )
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> export_ponder() {
        return literal("export_ponder")
            .then(argument("scene", ResourceLocationArgument.id())
                .suggests(ITEM_PONDERS)
                .executes(ctx -> $export_ponder(
                    ctx.getSource(),
                    ResourceLocationArgument.getId(ctx, "scene")
                ))
            );
    }

    private static int $ponder(CommandSourceStack source, BlockPos pos, int size, String name) {
        return $ponder(source, pos, size, 2, name);
    }

    private static int $ponder(CommandSourceStack source, BlockPos pos, int size, int height, String name) {
        ServerLevel level = source.getLevel();
        BlockPos structureBlockPos = pos.below();
        BlockPos ponderBase = pos.offset(1, 0, 1);

        BlockState prev = level.getBlockState(structureBlockPos);

        level.setBlock(structureBlockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(
            structureBlockPos,
            Blocks.STRUCTURE_BLOCK.defaultBlockState()
                .setValue(StructureBlock.MODE, StructureMode.SAVE),
            Block.UPDATE_ALL
        );

        if (!(level.getBlockEntity(structureBlockPos) instanceof StructureBlockEntity sbe)) {
            level.setBlock(structureBlockPos, prev, Block.UPDATE_ALL);
            source.sendFailure(Component.literal("Failed to create structure block"));
            return 0;
        }

        sbe.setStructurePos(ponderBase.subtract(structureBlockPos));
        sbe.setMode(StructureMode.SAVE);
        sbe.setStructureSize(new Vec3i(size, height + 1, size));
        sbe.setStructureName(Numismatics.asResource("ponder/"+name));

        BlockState a = Blocks.WHITE_CONCRETE.defaultBlockState();
        BlockState b = Blocks.SNOW_BLOCK.defaultBlockState();

        if (size >= 6 && size % 3 == 0) {
            for (int x = 0; x < size / 3; x++) {
                for (int z = 0; z < size / 3; z++) {
                    BlockState outer = (x + z) % 2 == 0 ? a : b;
                    BlockState inner = (x + z) % 2 == 0 ? b : a;

                    for (int xi = 0; xi < 3; xi++) {
                        for (int zi = 0; zi < 3; zi++) {
                            BlockPos checkPos = ponderBase.offset(x * 3 + xi, 0, z * 3 + zi);
                            if (!level.getBlockState(checkPos).isAir()) continue;

                            level.setBlock(checkPos, (xi == 1 && zi == 1) ? inner : outer, Block.UPDATE_ALL);
                        }
                    }
                }
            }
        } else {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    BlockPos checkPos = ponderBase.offset(x, 0, z);
                    if (!level.getBlockState(checkPos).isAir()) continue;

                    level.setBlock(checkPos, (x + z) % 2 == 0 ? a : b, Block.UPDATE_ALL);
                }
            }
        }

        source.sendSuccess(() -> Component.literal("Ponder template created"), true);

        return 1;
    }

    private static int $export_ponder(CommandSourceStack source, ResourceLocation id) {
        PonderExport.queuePonder(PonderUI.of(id));
        source.sendSuccess(() -> Component.literal("Queued ponder " + id + " for rendering"), true);
        return 1;
    }
}
