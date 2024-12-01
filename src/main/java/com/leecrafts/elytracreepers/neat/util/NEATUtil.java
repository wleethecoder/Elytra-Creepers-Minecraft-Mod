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
    public static final BlockPos SPAWN_POS = new BlockPos(-24, -23, 3);
    public static final int POPULATION_SIZE = 250;
    public static final int NUM_GENERATIONS = 1000;
    public static final int INPUT_SIZE = 5;
    public static final int OUTPUT_SIZE = 4;

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

    public static void recordFitness(LivingEntity livingEntity, ServerLevel serverLevel, int sightDistance, NEATController neatController) {
        // TODO calculate fitness
        double fitness = Math.random() * 1000;
        Agent agent = livingEntity.getData(ModAttachments.AGENT);
        if (agent != null) {
            agent.setScore(fitness);
        }
        livingEntity.discard();

        ModEvents.REMAINING_AGENTS--;

        if (ModEvents.REMAINING_AGENTS <= 0 && neatController != null) {
            ModEvents.REMAINING_GENERATIONS--;
            if (ModEvents.REMAINING_GENERATIONS > 0) {
                neatController.evolve();
                initializeEntityPopulation(serverLevel, sightDistance, neatController);
            } else {
                neatController.printSpecies();
                // TODO save best genome to file
            }
        }
    }

}
