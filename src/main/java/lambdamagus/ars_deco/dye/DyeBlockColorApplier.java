package lambdamagus.ars_deco.dye;

import lambdamagus.ars_deco.network.SyncBlockDyeColorPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.DoorBlock;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class DyeBlockColorApplier {
    private DyeBlockColorApplier() {
    }

    public static void apply(Level level, BlockPos pos, DyeTarget target, DyeColor color) {
        for (BlockPos targetPos : connectedPositions(level, pos, target)) {
            applySingle(level, targetPos, target, color);
        }
    }

    public static void clear(ServerLevel level, BlockPos pos, DyeTarget target) {
        clear(level, pos, target, level.getBlockState(pos));
    }

    public static void clear(ServerLevel level, BlockPos pos, DyeTarget target, BlockState state) {
        for (BlockPos targetPos : connectedPositions(level, pos, target, state)) {
            PlacedDyeColors.get(level).clear(targetPos);
            PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunk(targetPos).getPos(), new SyncBlockDyeColorPayload(targetPos, null));
            level.sendBlockUpdated(targetPos, level.getBlockState(targetPos), level.getBlockState(targetPos), 3);
        }
    }

    private static void applySingle(Level level, BlockPos pos, DyeTarget target, DyeColor color) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ArsDecoDyeableBlockEntity dyeable) {
            dyeable.arsDeco$setColor(color);
            blockEntity.setChanged();
        }

        if (level instanceof ServerLevel serverLevel) {
            PlacedDyeColors.PlacedColor placedColor = new PlacedDyeColors.PlacedColor(target.blockId(), color);
            PlacedDyeColors.get(serverLevel).set(pos, target.blockId(), color);
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunk(pos).getPos(), new SyncBlockDyeColorPayload(pos, placedColor));
        }

        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
    }

    private static List<BlockPos> connectedPositions(Level level, BlockPos pos, DyeTarget target) {
        return connectedPositions(level, pos, target, level.getBlockState(pos));
    }

    private static List<BlockPos> connectedPositions(Level level, BlockPos pos, DyeTarget target, BlockState state) {
        if (!state.hasProperty(DoorBlock.HALF)) {
            return List.of(pos);
        }

        BlockPos otherPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        if (DyeableArsBlocks.target(level.getBlockState(otherPos).getBlock())
                .filter(otherTarget -> target.blockId().equals(otherTarget.blockId()))
                .isEmpty()) {
            return List.of(pos);
        }

        List<BlockPos> positions = new ArrayList<>(2);
        positions.add(pos);
        positions.add(otherPos);
        return positions;
    }
}
