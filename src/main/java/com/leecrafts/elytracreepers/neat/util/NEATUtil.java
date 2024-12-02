package com.leecrafts.elytracreepers.neat.util;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.event.ModEvents;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NEATUtil {

    public static final boolean TRAINING = true;
    public static final boolean PRODUCTION = false;

    private static final String BASE_DIRECTORY_PATH = new File(System.getProperty("user.dir")).getParent();
    private static final String ASSETS_DIRECTORY_PATH = "/assets/elytracreepers/agent/";
    private static final File AGENT_DIRECTORY_PATH = new File(BASE_DIRECTORY_PATH, "src/main/resources" + ASSETS_DIRECTORY_PATH);
    private static final String AGENT_BASE_NAME = "agent";

    public static final File OVERALL_METRICS_LOG_PATH = new File(System.getProperty("user.dir"), "metricslog/overall.csv");
    public static final File PER_SPECIES_METRICS_LOG_PATH = new File(System.getProperty("user.dir"), "metricslog/per_species.csv");

    public static final BlockPos SPAWN_POS = new BlockPos(-189 - 100, -64 + 100 + 1, -2);

    public static final int POPULATION_SIZE = 500;
    public static final int NUM_GENERATIONS = 1000;
    public static final int INPUT_SIZE = 5;
    public static final int OUTPUT_SIZE = 4;

    // TODO adjust hyperparameters
    public static final double FAST_FALL_PUNISHMENT = 5;
    public static final double DISTANCE_PUNISHMENT = 1;
    public static final double TIME_PUNISHMENT = 0.05;

    public static void initializeEntityPopulation(ServerLevel serverLevel, int sightDistance, NEATController neatController, ServerPlayer trackingPlayer) {
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

        trackingPlayer.displayClientMessage(Component.literal("Generation " + (NUM_GENERATIONS - ModEvents.REMAINING_GENERATIONS + 1)), true);
    }

    public static void recordFitness(LivingEntity livingEntity, float fastFallDistance, ServerLevel serverLevel, int sightDistance, NEATController neatController, ServerPlayer trackingPlayer) {
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
                initializeEntityPopulation(serverLevel, sightDistance, neatController, trackingPlayer);
            } else {
                saveAgent(neatController.getBestAgent());
            }
            System.out.println("GENERATION " + (NUM_GENERATIONS - ModEvents.REMAINING_GENERATIONS));
            neatController.printSpecies();
            logMetrics(neatController);
        }
    }

    private static double calculateFitness(LivingEntity livingEntity, Entity target, float fastFallDistance) {
        return -(FAST_FALL_PUNISHMENT * Math.max(0, fastFallDistance - 3) +
                DISTANCE_PUNISHMENT * livingEntity.distanceTo(target) +
                TIME_PUNISHMENT * livingEntity.tickCount);
    }

    private static void saveAgent(Agent agent) {
        File file = agentFile(getNewestAgentNumber() + 1);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(agent);
            System.out.println("Agent saved to " + file.getPath());
        } catch (IOException e) {
            System.out.println("Error saving agent: " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    public static Agent loadAgent() {
        Agent agent = null;
        if (PRODUCTION) {
            int bestAgentNumber = 2; // change this number according to the best agent of all best agents
            try (InputStream inputStream = NEATUtil.class.getResourceAsStream(
                    String.format(ASSETS_DIRECTORY_PATH + "%s-%d.dat", AGENT_BASE_NAME, bestAgentNumber));
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                System.out.println("existing agent in resources folder found");
                agent = (Agent) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading agent: " + e.getMessage());
            }
        }
        else {
            File file = agentFile(getNewestAgentNumber());
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                agent = (Agent) in.readObject();
                System.out.println("Agent loaded from " + file.getPath());
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading agent: " + e.getMessage());
            }
        }
        return agent;
    }

    private static File agentFile(int agentNumber) {
        return new File(AGENT_DIRECTORY_PATH, String.format("%s-%d.dat", AGENT_BASE_NAME, agentNumber));
    }

    private static int getNewestAgentNumber() {
        File[] files = AGENT_DIRECTORY_PATH.listFiles();  // Get all files in the directory
        int maxNum = 0;
        if (files == null) {
            return maxNum;
        }

        Pattern pattern = Pattern.compile(String.format("^%s-(\\d+)\\.dat$", AGENT_BASE_NAME));  // Regex to find files
        for (File file : files) {
            if (file.isFile()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    maxNum = Math.max(maxNum, Integer.parseInt(matcher.group(1)));
                }
            }
        }

        return maxNum;
    }

    public static void logMetrics(NEATController neatController) {
        // OVERALL
        // generation number
        // mean and std scores of population
        // mean and std scores of best species
        // species count
        try {
            FileWriter writer = new FileWriter(OVERALL_METRICS_LOG_PATH, true);
            if (OVERALL_METRICS_LOG_PATH.length() == 0) {
                writer.append("Generation,Population Mean Score,Population Std Score,Best Species Mean Score,Best Species Std Score,Best Agent Score,Num Species")
                        .append("\n");
            }
            double[] populationMeanAndStd = neatController.populationMeanAndStd();
            double[] bestSpeciesMeanAndStd = neatController.bestSpeciesMeanAndStd();
            writer.append(String.valueOf(NUM_GENERATIONS - ModEvents.REMAINING_GENERATIONS))
                    .append(",")
                    .append(String.valueOf(populationMeanAndStd[0]))
                    .append(",")
                    .append(String.valueOf(populationMeanAndStd[1]))
                    .append(",")
                    .append(String.valueOf(bestSpeciesMeanAndStd[0]))
                    .append(",")
                    .append(String.valueOf(bestSpeciesMeanAndStd[1]))
                    .append(",")
                    .append(String.valueOf(neatController.getBestAgent().getScore()))
                    .append(",")
                    .append(String.valueOf(neatController.numSpecies()))
                    .append("\n");
            writer.close();
            System.out.println("Overall metrics have been logged.");
        } catch (IOException e) {
            System.out.println("Error logging overall metrics: " + e.getMessage());
        }

        // PER SPECIES
        // mean and std scores and size of each species
        try {
            FileWriter writer = new FileWriter(PER_SPECIES_METRICS_LOG_PATH, true);
            if (PER_SPECIES_METRICS_LOG_PATH.length() == 0) {
                writer.append("Species Name,Mean Score,Std Score,Best Agent Score,Size")
                        .append("\n");
            }
            writer.append("GENERATION ")
                    .append(String.valueOf(NUM_GENERATIONS - ModEvents.REMAINING_GENERATIONS))
                    .append("\n")
                    .append(neatController.perSpeciesMetrics());
            writer.close();
            System.out.println("Per species metrics have been logged.");
        } catch (IOException e) {
            System.out.println("Error logging per species metrics: " + e.getMessage());
        }
    }

}
