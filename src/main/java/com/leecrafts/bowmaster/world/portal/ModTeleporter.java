package com.leecrafts.bowmaster.world.portal;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.player.PlayerCap;
import com.leecrafts.bowmaster.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ModTeleporter implements ITeleporter {

    // destination location is inside arena dimension
    private static boolean insideDimension = true;

    // width of arena, including outside walls.
    // because the width is 50, the enclosed area is 48x48
    public static final int ARENA_WIDTH = 50;
    public static final int ARENA_HEIGHT = 100;
    public static final int ARENA_Y = -61;

    private static BlockPos currentPos = BlockPos.ZERO;
    private static BlockPos destinationPos = BlockPos.ZERO;

    public ModTeleporter(boolean insideDim) {
        insideDimension = insideDim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);
//        decideDestinationPos(entity, destWorld);
        entity.setPos(destinationPos.getX(), destinationPos.getY(), destinationPos.getZ()); // I am not sure if this line is even necessary.
        entity.resetFallDistance();

        return entity;
    }

    // I realized getPortalInfo gets called before placeEntity when the player gets transported into another dimension.
    @Override
    public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        decideDestinationPos(entity, destWorld);
        return new PortalInfo(Vec3.atCenterOf(destinationPos), Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    private static void decideDestinationPos(Entity entity, ServerLevel destWorld) {
        RandomSource randomSource = entity.level().getRandom();
        currentPos = entity.blockPosition();
        entity.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
            PlayerCap playerCap = (PlayerCap) iPlayerCap;
            if (insideDimension) { // other dimension -> arena
                playerCap.setOutsideDimBlockPos(currentPos);

                WorldBorder worldBorder = destWorld.getWorldBorder();
                destinationPos = new BlockPos(
                        (int) (worldBorder.getMinX() + randomSource.nextInt((int) worldBorder.getMaxX())),
                        ARENA_Y,
                        (int) (worldBorder.getMinZ() + randomSource.nextInt((int) worldBorder.getMaxZ())));

//                playerCap.setArenaDimBlockPos(destinationPos); // tbh we can just manually check for any entities via AABB
                // TODO check for other players' locations

                placeArena(destWorld, destinationPos);
            }
            else { // arena -> other dimension
                destinationPos = new BlockPos(playerCap.outsideDimBlockPos[0], playerCap.outsideDimBlockPos[1], playerCap.outsideDimBlockPos[2]);
                int tries = 0;
                while((destWorld.getBlockState(destinationPos).getBlock() != Blocks.AIR) &&
                        !destWorld.getBlockState(destinationPos).canBeReplaced(Fluids.WATER) &&
                        destWorld.getBlockState(destinationPos.above()).getBlock() != Blocks.AIR &&
                        !destWorld.getBlockState(destinationPos.above()).canBeReplaced(Fluids.WATER) &&
                        tries < 25) {
                    destinationPos = destinationPos.above(2);
                    tries++;
                }
            }

        });
    }

    public static void teleportPlayer(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ResourceKey<Level> resourceKey = player.level().dimension() == ModDimensions.ARENA_LEVEL_KEY ?
                    Level.OVERWORLD : ModDimensions.ARENA_LEVEL_KEY;
            ServerLevel dimension = serverLevel.getLevel().getServer().getLevel(resourceKey);
            if (dimension != null) {
                player.changeDimension(dimension, new ModTeleporter(
                        resourceKey == ModDimensions.ARENA_LEVEL_KEY));
            }
        }
    }

    public static void placeArena(ServerLevel destWorld, BlockPos centerPos) {
        int xBegin = centerPos.getX() - (ARENA_WIDTH - 1) / 2;
        int xEnd = centerPos.getX() + ARENA_WIDTH / 2;
        int zBegin = centerPos.getZ() - (ARENA_WIDTH - 1) / 2;
        int zEnd = centerPos.getZ() + ARENA_WIDTH / 2;
        for (int y = ARENA_Y; y < ARENA_Y + ARENA_HEIGHT; y++) {
            for (int x = xBegin; x <= xEnd; x++) {
                placeWhiteStainedGlassBlock(x, y, zBegin, destWorld);
                placeWhiteStainedGlassBlock(x, y, zEnd, destWorld);
            }
            for (int z = zBegin + 1; z < zEnd; z++) {
                placeWhiteStainedGlassBlock(xBegin, y, z, destWorld);
                placeWhiteStainedGlassBlock(xEnd, y, z, destWorld);
            }
        }
    }

    public static void placeWhiteStainedGlassBlock(int x, int y, int z, ServerLevel serverLevel) {
        serverLevel.setBlock(new BlockPos(x, y, z), Blocks.WHITE_STAINED_GLASS.defaultBlockState(), 3);
    }

}