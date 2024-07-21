package com.leecrafts.bowmaster.world.portal;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.player.PlayerCap;
import com.leecrafts.bowmaster.entity.ModEntityTypes;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ModTeleporter implements ITeleporter {

    // destination location is inside arena dimension
    private static boolean insideDimension = true;

    // width of arena, including outside walls.
    // because the width is 50, the enclosed area is 48x48
    public static final int ARENA_WIDTH = 15;
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
                do {
                    rollDestinationPos(worldBorder, randomSource);
                    playerCap.setArenaDimBlockPos(destinationPos);
                } while (!isSafeToPlaceArena(destWorld, entity, playerCap.arenaDimBlockPos));

                placeArena(destWorld, playerCap.arenaDimBlockPos);
                placePlayerAndSkeletonBowMaster(entity, destWorld);
            }
            else { // arena -> other dimension
                destinationPos = playerCap.getOutsideDimBlockPos();
                int tries = 0;
                while((destWorld.getBlockState(destinationPos).getBlock() != Blocks.AIR) &&
                        !destWorld.getBlockState(destinationPos).canBeReplaced(Fluids.WATER) &&
                        destWorld.getBlockState(destinationPos.above()).getBlock() != Blocks.AIR &&
                        !destWorld.getBlockState(destinationPos.above()).canBeReplaced(Fluids.WATER) &&
                        tries < 25) {
                    destinationPos = destinationPos.above(2);
                    tries++;
                }

                if (entity instanceof ServerPlayer serverPlayer) {
                    removeArena(serverPlayer.serverLevel(), playerCap.arenaDimBlockPos);
                }
            }

        });
    }

    public static void teleportPlayer(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ResourceKey<Level> currentDimensionResourceKey = player.level().dimension();
            ResourceKey<Level> destDimensionResourceKey;
            if (currentDimensionResourceKey == Level.OVERWORLD) {
                destDimensionResourceKey = ModDimensions.ARENA_LEVEL_KEY;
            }
            else if (currentDimensionResourceKey == ModDimensions.ARENA_LEVEL_KEY) {
                destDimensionResourceKey = Level.OVERWORLD;
            }
            else {
                return;
            }
            ServerLevel destDimension = serverLevel.getLevel().getServer().getLevel(destDimensionResourceKey);
            if (destDimension != null) {
                player.changeDimension(destDimension, new ModTeleporter(
                        destDimensionResourceKey == ModDimensions.ARENA_LEVEL_KEY));
            }
        }
    }

    private static void rollDestinationPos(WorldBorder worldBorder, RandomSource randomSource) {
        destinationPos = new BlockPos(
                (int) (worldBorder.getMinX() + randomSource.nextInt((int) worldBorder.getMaxX())),
                ARENA_Y,
                (int) (worldBorder.getMinZ() + randomSource.nextInt((int) worldBorder.getMaxZ())));
//        xBegin = destinationPos.getX() - (ARENA_WIDTH - 1) / 2;
//        xEnd = destinationPos.getX() + ARENA_WIDTH / 2;
//        zBegin = destinationPos.getZ() - (ARENA_WIDTH - 1) / 2;
//        zEnd = destinationPos.getZ() + ARENA_WIDTH / 2;
    }

    private static boolean isSafeToPlaceArena(ServerLevel destWorld, Entity entity, int[] areaDimBlockPos) {
//        for (int x = xBegin; x <= xEnd; x++) {
//            for (int z = zBegin + 1; z < zEnd; z++) {
//                if (destWorld.getBlockState(new BlockPos(x, ARENA_Y, z)).getBlock() != Blocks.AIR) {
//                    return false;
//                }
//            }
//        }
        List<ServerPlayer> players = destWorld.players();
        if (players.isEmpty()) {
            return true;
        }
        for (ServerPlayer serverPlayer : players) {
            if (!serverPlayer.is(entity)) {
                AtomicBoolean safe = new AtomicBoolean(true);
                serverPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    if (Math.abs(playerCap.arenaDimBlockPos[0] - areaDimBlockPos[0]) < ARENA_WIDTH / 2 ||
                            Math.abs(playerCap.arenaDimBlockPos[1] - areaDimBlockPos[1]) < ARENA_WIDTH / 2) {
                        safe.set(false);
                    }
                });
                if (!safe.get()) return false;
            }
        }
        return true;
    }

    public static void placeArena(ServerLevel arenaWorld, int[] arenaDimBlockPos) {
        setArenaBlocks(arenaWorld, arenaDimBlockPos, Blocks.WHITE_STAINED_GLASS);
    }

    public static void removeArena(ServerLevel arenaWorld, int[] arenaDimBlockPos) {
        setArenaBlocks(arenaWorld, arenaDimBlockPos, Blocks.AIR);
    }

    public static void setArenaBlocks(ServerLevel arenaWorld, int[] arenaDimBlockPos, Block block) {
        int xBegin = arenaDimBlockPos[0] - (ARENA_WIDTH - 1) / 2;
        int xEnd = arenaDimBlockPos[0] + ARENA_WIDTH / 2;
        int zBegin = arenaDimBlockPos[2] - (ARENA_WIDTH - 1) / 2;
        int zEnd = arenaDimBlockPos[2] + ARENA_WIDTH / 2;
        for (int y = ARENA_Y; y < ARENA_Y + ARENA_HEIGHT; y++) {
            for (int x = xBegin; x <= xEnd; x++) {
                setBlock(x, y, zBegin, arenaWorld, block);
                setBlock(x, y, zEnd, arenaWorld, block);
            }
            for (int z = zBegin + 1; z < zEnd; z++) {
                setBlock(xBegin, y, z, arenaWorld, block);
                setBlock(xEnd, y, z, arenaWorld, block);
            }
        }
    }

    public static void setBlock(int x, int y, int z, ServerLevel serverLevel, Block block) {
        serverLevel.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), 3);
    }

    private static void placePlayerAndSkeletonBowMaster(Entity entity, ServerLevel destWorld) {
        spawnSkeletonBowMaster(destinationPos, destWorld, -ARENA_WIDTH / 4);

        if (SkeletonBowMasterEntity.TRAINING) {
            spawnSkeletonBowMaster(destinationPos, destWorld, ARENA_WIDTH / 4);

            destinationPos = destinationPos.above(8);
            destinationPos = destinationPos.west(ARENA_WIDTH / 3);
            entity.setYRot(-90);
            entity.setXRot(40);
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.setGameMode(GameType.CREATIVE);
                serverPlayer.getAbilities().flying = true;
            }
        }
        else {
            destinationPos = destinationPos.north(ARENA_WIDTH / 4);
            entity.setYRot(0);
        }
    }

    public static void spawnSkeletonBowMaster(BlockPos center, ServerLevel serverLevel, int offset) {
        SkeletonBowMasterEntity skeletonBowMasterEntity = ModEntityTypes.SKELETON_BOW_MASTER.get().spawn(
                serverLevel, center.north(offset), MobSpawnType.EVENT);
        if (skeletonBowMasterEntity != null) {
            skeletonBowMasterEntity.setYRot(offset >= 0 ? 0 : 180);
        }
    }

}
