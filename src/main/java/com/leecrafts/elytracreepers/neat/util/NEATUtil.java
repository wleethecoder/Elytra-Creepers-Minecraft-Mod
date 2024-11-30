package com.leecrafts.elytracreepers.neat.util;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.event.ModEvents;
import com.leecrafts.elytracreepers.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class NEATUtil {

    public static final boolean TRAINING = true;
    public static final BlockPos SPAWN_POS = new BlockPos(-24, -23, 3);
    public static final int POPULATION_SIZE = 1;
    public static final int INPUT_SIZE = 5; // TODO change if needed
    public static final int OUTPUT_SIZE = 4;

    public static void initializeEntityPopulation(ServerLevel serverLevel, int sightDistance, int populationSize) {
        // initialize population
        ModEvents.REMAINING = populationSize;
        // for loop...entity.setData(new Genome)
        // population.add(entity.getData())
        for (int i = 0; i < populationSize; i++) {
            Entity entity = Config.spawnedElytraEntityType.spawn(serverLevel, NEATUtil.SPAWN_POS, MobSpawnType.MOB_SUMMONED);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));

                // setting target
                List<ArmorStand> candidates = livingEntity.level().getEntitiesOfClass(
                        ArmorStand.class, livingEntity.getBoundingBox().inflate(sightDistance));
                // this commented out code is not for training
//                List<Player> candidates = livingEntity.level().getNearbyPlayers(
//                        TargetingConditions.forNonCombat().ignoreLineOfSight().range(sightDistance),
//                        livingEntity,
//                        livingEntity.getBoundingBox().inflate(sightDistance));
                if (!candidates.isEmpty()) {
                    int index = entity.getRandom().nextInt(candidates.size());
                    livingEntity.setData(ModAttachments.TARGET_ENTITY, candidates.get(index));
                }

                if (livingEntity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                }
            }
        }
    }

    public static void recordFitness(LivingEntity livingEntity, double fitness) {
        ModEvents.REMAINING--;

        if (ModEvents.REMAINING == 0) {
            evolveNewGeneration();
        }
    }

    private static void evolveNewGeneration() {
    }

}
