package com.leecrafts.elytracreepers.neat.util;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.event.ModEvents;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class NEATUtil {

    public static final boolean TRAINING = true;
    public static final BlockPos SPAWN_POS = new BlockPos(-189 - 100, -64 + 100 + 1, -2);

    public static final int POPULATION_SIZE = 500; // TODO change to at least 250
    public static final int NUM_GENERATIONS = 5; // TODO change to 1000
    public static final int INPUT_SIZE = 5;
    public static final int OUTPUT_SIZE = 4;

    // TODO adjust hyperparameters
    public static final double FAST_FALL_PUNISHMENT = 5;
    public static final double DISTANCE_PUNISHMENT = 1;
    public static final double TIME_PUNISHMENT = 0.05;

    public static void initializeEntityPopulation(ServerLevel serverLevel, int sightDistance, NEATController neatController) {
        // initialize population
        ModEvents.REMAINING_AGENTS = neatController.getPopulationSize();
        for (int i = 0; i < neatController.getPopulationSize(); i++) {
            Entity entity = Config.spawnedElytraEntityType.spawn(serverLevel, NEATUtil.SPAWN_POS, MobSpawnType.MOB_SUMMONED);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));

                // setting agent data attachment
                livingEntity.setData(ModAttachments.AGENT, neatController.getAgent(i));

                // setting target
                List<ArmorStand> candidates = livingEntity.level().getEntitiesOfClass(
                        ArmorStand.class, livingEntity.getBoundingBox().inflate(sightDistance));
                // TODO this commented out code is for production mode
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

    public static void recordFitness(LivingEntity livingEntity, float fastFallDistance, ServerLevel serverLevel, int sightDistance, NEATController neatController) {
        Agent agent = livingEntity.getData(ModAttachments.AGENT);
        Entity target = livingEntity.getData(ModAttachments.TARGET_ENTITY);
        if (agent != null && target != null) {
            agent.setScore(calculateFitness(livingEntity, target, fastFallDistance));
        }
        livingEntity.discard();

        ModEvents.REMAINING_AGENTS--;

        if (ModEvents.REMAINING_AGENTS <= 0 && neatController != null) {
            ModEvents.REMAINING_GENERATIONS--;
            if (ModEvents.REMAINING_GENERATIONS > 0) {
                neatController.evolve();
                System.out.println("GENERATION " + (NUM_GENERATIONS - ModEvents.REMAINING_GENERATIONS));
                neatController.printSpecies();
                initializeEntityPopulation(serverLevel, sightDistance, neatController);
            } else {
                // TODO save best agent/genome to file
            }
        }
    }

    private static double calculateFitness(LivingEntity livingEntity, Entity target, float fastFallDistance) {
        return -(FAST_FALL_PUNISHMENT * Math.max(0, fastFallDistance - 3) +
                DISTANCE_PUNISHMENT * livingEntity.distanceTo(target) +
                TIME_PUNISHMENT * livingEntity.tickCount);
    }

}
