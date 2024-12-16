package com.leecrafts.elytracreepers.neat.util;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.event.ModEvents;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.neat.calculations.Calculator;
import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class NEATUtil {

    public static final boolean TRAINING = false;
    public static final boolean PRODUCTION = true;
    public static final boolean RANDOM_MODE = true;

    private static final String BASE_DIRECTORY_PATH = new File(System.getProperty("user.dir")).getParent();
    private static final String ASSETS_DIRECTORY_PATH = "/assets/elytracreepers/";
    private static final File OBJECT_DIRECTORY_PATH = new File(BASE_DIRECTORY_PATH, "src/main/resources" + ASSETS_DIRECTORY_PATH);
    private static final String AGENT_BASE_NAME = "agent";
    private static final String NEATCONTROLLER_BASE_NAME = "neatcontroller";
    public static final String AGENT_REGEX = "^%s-(\\d+)\\.dat$";
    public static final String NEATCONTROLLER_REGEX = "^%s-(\\d+)\\-(\\d+)\\.dat$";

    // neatController is saved every N generations
    private static final int N = 25;

    public static final File OVERALL_METRICS_LOG_PATH = new File(System.getProperty("user.dir"), "metricslog/overall.csv");
    public static final File PER_SPECIES_METRICS_LOG_PATH = new File(System.getProperty("user.dir"), "metricslog/per_species.csv");

    public static final int POPULATION_SIZE = 500;
    public static final int NUM_GENERATIONS = 1000;
    public static final int INPUT_SIZE = 7;
    public static final int OUTPUT_SIZE = 4;

    // TODO adjust hyperparameters
    public static final double FAST_FALL_PUNISHMENT = 5;
    public static final double DISTANCE_PUNISHMENT = 1;
    public static final double TIME_PUNISHMENT = 0.05;

    // I am trying out a naive method of adding randomness into the training process.
    // First of all, without the randomization of agent spawn points and target movement, convergence was reached after 330 generations.
    // Therefore, I want the randomness to be the smallest at generation 0 and the largest at generation 330.
    // For example, at generation 0 the range of horizontal distances between the target and agent spawn points is [100, 100].
    // But eventually, that range would increase, becoming [50, 100] at generation 165 and [0, 100] at generation 330.
    public static final int GENERATIONAL_RANDOMNESS_BOUND = 330;

    public static final double AGENT_SPAWN_DISTANCE = 100;
    public static final double AGENT_SPAWN_Y_OFFSET = 50;
    // According to the Minecraft Wiki, jumping while sprinting allows the player to move with an average speed of 7.127 m/s.
    public static final double MAX_TARGET_SPEED = 7.127 / TICKS_PER_SECOND;

    public static void initializeEntityPopulation(ServerLevel serverLevel, NEATController neatController, ServerPlayer trackingPlayer) {
        // initialize population

        ModEvents.REMAINING_AGENTS = neatController.getPopulationSize();

        // getting target
        List<ArmorStand> candidates = trackingPlayer.level().getEntitiesOfClass(
                ArmorStand.class, trackingPlayer.getBoundingBox().inflate(500));
        if (!candidates.isEmpty()) {
            // initialize target initial position and speed
            int index = trackingPlayer.getRandom().nextInt(candidates.size());
            ArmorStand armorStand = candidates.get(index);
            if (RANDOM_MODE) {
                armorStand.moveTo(Vec3.atBottomCenterOf(ModEvents.TARGET_INIT_POS));
                double angle = Math.random() * 2 * Math.PI;
                double magnitude = degreeOfRandomness() * Math.random() * MAX_TARGET_SPEED;
                double xSpeed = magnitude * Math.cos(angle);
                double zSpeed = magnitude * Math.sin(angle);
                armorStand.setData(ModAttachments.TARGET_MOVEMENT, new Vec3(xSpeed, 0, zSpeed));
            }
            for (int i = 0; i < neatController.getPopulationSize(); i++) {
                Entity entity = Config.spawnedElytraEntityType.spawn(serverLevel, ModEvents.AGENT_SPAWN_POS, MobSpawnType.MOB_SUMMONED);
                if (entity instanceof LivingEntity livingEntity) {
                    if (RANDOM_MODE) {
                        livingEntity.setPos(livingEntity.getX() +
                                        degreeOfRandomness() * Math.random() * AGENT_SPAWN_DISTANCE,
                                livingEntity.getY() +
                                        degreeOfRandomness() * Math.random() * AGENT_SPAWN_Y_OFFSET * (livingEntity.getRandom().nextBoolean() ? 1 : -1),
                                livingEntity.getZ());
                    }
                    livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));

                    // setting agent data attachment
                    livingEntity.setData(ModAttachments.AGENT, neatController.getAgent(i));

                    // setting target
                    livingEntity.setData(ModAttachments.TARGET_ENTITY, armorStand);

                    if (livingEntity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }
                }
            }
        }

        trackingPlayer.displayClientMessage(Component.literal("Generation " + (generationNumber() + 1)), true);
    }

    public static void recordFitness(LivingEntity livingEntity, float fastFallDistance, int timeElapsed, ServerLevel serverLevel, NEATController neatController, ServerPlayer trackingPlayer) {
        Agent agent = livingEntity.getData(ModAttachments.AGENT);
        Entity target = livingEntity.getData(ModAttachments.TARGET_ENTITY);
        if (agent != null && target != null) {
            agent.setScore(calculateFitness(livingEntity, target, fastFallDistance, timeElapsed));
        }
        livingEntity.discard();

        ModEvents.REMAINING_AGENTS--;

        if (ModEvents.REMAINING_AGENTS <= 0 && neatController != null) {
            ModEvents.REMAINING_GENERATIONS--;

            int generationNumber = generationNumber();
            System.out.println("GENERATION " + generationNumber);
//            neatController.printSpecies();
            logMetrics(neatController);
            if (generationNumber < NUM_GENERATIONS) {
                neatController.evolve();
                initializeEntityPopulation(serverLevel, neatController, trackingPlayer);
            } else {
//                saveAgent(neatController.getBestAgent());
            }

            if ((generationNumber - 1) % N == 0 || generationNumber == NUM_GENERATIONS) {
                saveNEATController(neatController, generationNumber);
            }
        }
    }

    private static double calculateFitness(LivingEntity livingEntity, Entity target, float fastFallDistance, int timeElapsed) {
        return -(FAST_FALL_PUNISHMENT * Math.max(0, fastFallDistance - 2) +
                DISTANCE_PUNISHMENT * livingEntity.distanceTo(target) +
                TIME_PUNISHMENT * timeElapsed);
    }

    private static int generationNumber() {
        return NUM_GENERATIONS - ModEvents.REMAINING_GENERATIONS;
    }

    private static double degreeOfRandomness() {
        return 1.0 * Math.min(GENERATIONAL_RANDOMNESS_BOUND, generationNumber()) / GENERATIONAL_RANDOMNESS_BOUND;
    }

    // saving the agent itself has become redundant because the NEATController object is already being saved
    // I can just use NEATUtil.java to extract an agent from the NEATController object of my choosing
//    private static void saveAgent(Agent agent) {
//        File file = objectFile(
//                String.valueOf(getNewestAgentNumber() + 1),
//                AGENT_BASE_NAME);
//        saveObject(agent, file, AGENT_BASE_NAME);
//    }

    private static void saveNEATController(NEATController neatController, int generationNumber) {
        int[] numbers = getNewestNEATControllerNumberAndGenerationNumber();
        int maxNum = numbers[0];
        int maxGenerationNumber = numbers[1];
        if (generationNumber > 1 && generationNumber <= maxGenerationNumber) {
            maxNum++;
        }
        File file = objectFile(
                String.format("%d-%d", maxNum, generationNumber),
                NEATCONTROLLER_BASE_NAME);
        saveObject(neatController, file, NEATCONTROLLER_BASE_NAME);
    }

    private static void saveObject(Object object, File file, String type) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(object);
            System.out.println(type + " saved to " + file.getPath());
        } catch (IOException e) {
            System.out.println("Error saving " + type + ": " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    // production mode only
    public static Calculator loadAgent(int agentNumber) {
        return (Calculator) loadObject(AGENT_BASE_NAME, String.valueOf(agentNumber));
    }

    // this should be used outside of the training process
    // with the Frame class, you can visualize the loaded neatController's genomes
    public static NEATController loadNEATController(int neatControllerNumber, int generationNumber) {
        return (NEATController) loadObject(NEATCONTROLLER_BASE_NAME, String.format("%d-%d", neatControllerNumber, generationNumber));
    }

    // this is used for the neatcontroller to pick up from where it left off during the training process
    public static NEATController loadNEATController() {
        int[] numbers = getNewestNEATControllerNumberAndGenerationNumber();
        ModEvents.REMAINING_GENERATIONS = NUM_GENERATIONS - numbers[1];
        return (NEATController) loadObject(NEATCONTROLLER_BASE_NAME, String.format("%d-%d", numbers[0], numbers[1]));
    }

    public static Object loadObject(String type, String numberString) {
        Object object = null;
        if (PRODUCTION) {
            try (InputStream inputStream = NEATUtil.class.getResourceAsStream(
                    String.format("%s/%s/%s-%s.dat", ASSETS_DIRECTORY_PATH, type, type, numberString));
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                System.out.println("existing " + type + " in resources folder found");
                object = objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading object: " + e.getMessage());
            }
        }
        else {
            File file = objectFile(numberString, type);
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                object = in.readObject();
                System.out.println(type + " loaded from " + file.getPath());
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading " + type + ": " + e.getMessage());
            }
        }
        return object;
    }

    private static File objectFile(String numberString, String type) {
        return new File(OBJECT_DIRECTORY_PATH, String.format("%s/%s-%s.dat", type, type, numberString));
    }

    private static int getNewestAgentNumber() {
        File[] files = new File(OBJECT_DIRECTORY_PATH, AGENT_BASE_NAME).listFiles();  // Get all files in the directory
        int maxNum = 0;
        if (files == null) {
            return maxNum;
        }

        Pattern pattern = Pattern.compile(String.format(AGENT_REGEX, AGENT_BASE_NAME));  // Regex to find files
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

    private static int getNewestNEATControllerNumber(int currentGenerationNumber) {
        int[] numbers = getNewestNEATControllerNumberAndGenerationNumber();
        int maxNum = numbers[0];
        int maxGenerationNumber = numbers[1];
        if (currentGenerationNumber <= maxGenerationNumber) {
            maxNum++;
        }
        return maxNum;
    }

    private static int[] getNewestNEATControllerNumberAndGenerationNumber() {
        File[] files = new File(OBJECT_DIRECTORY_PATH, NEATCONTROLLER_BASE_NAME).listFiles();  // Get all files in the directory
        int maxNum = 1;
        int maxGenerationNumber = 1;
        if (files != null) {
            Pattern pattern = Pattern.compile(String.format(NEATCONTROLLER_REGEX, NEATCONTROLLER_BASE_NAME));  // Regex to find files
            for (File file : files) {
                if (file.isFile()) {
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.matches()) {
                        int number = Integer.parseInt(matcher.group(1));
                        int generationNumber = Integer.parseInt(matcher.group(2));
                        if (number > maxNum) {
                            maxNum = number;
                            maxGenerationNumber = generationNumber;
                        } else if (number == maxNum) {
                            maxGenerationNumber = Math.max(maxGenerationNumber, generationNumber);
                        }
                    }
                }
            }
        }
        return new int[] {maxNum, maxGenerationNumber};
    }

    public static void logMetrics(NEATController neatController) {
        // OVERALL
        // generation number
        // mean, std, and median scores of population
        // mean, std, and median scores of best species
        // best agent score
        // species count
        int generationNumber = generationNumber();
        try {
            FileWriter writer = new FileWriter(OVERALL_METRICS_LOG_PATH, true);
            if (generationNumber == 1) {
                writer.append(neatController.hyperparametersString())
                        .append("\n")
                        .append("Generation,Population Mean Score,Population Std Score,Population Median Score,Best Species Mean Score,Best Species Std Score,Best Species Median Score,Best Agent Score,Num Species")
                        .append("\n");
            }
            double[] populationMetrics = neatController.populationMetrics();
            double[] bestSpeciesMetrics = neatController.bestSpeciesMetrics();
            String bestSpeciesMean = ""; // blank values in a csv file are NaN
            String bestSpeciesStd = "";
            String bestSpeciesMedian = "";
            if (bestSpeciesMetrics != null) {
                bestSpeciesMean = String.valueOf(bestSpeciesMetrics[0]);
                bestSpeciesStd = String.valueOf(bestSpeciesMetrics[1]);
                bestSpeciesMedian = String.valueOf(bestSpeciesMetrics[2]);
            }
            writer.append(String.valueOf(generationNumber))
                    .append(",")
                    .append(String.valueOf(populationMetrics[0]))
                    .append(",")
                    .append(String.valueOf(populationMetrics[1]))
                    .append(",")
                    .append(String.valueOf(populationMetrics[2]))
                    .append(",")
                    .append(bestSpeciesMean)
                    .append(",")
                    .append(bestSpeciesStd)
                    .append(",")
                    .append(bestSpeciesMedian)
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
        // species name
        // mean, std, and median scores
        // best agent score
        // size
        try {
            FileWriter writer = new FileWriter(PER_SPECIES_METRICS_LOG_PATH, true);
            if (generationNumber == 1) {
                writer.append(neatController.hyperparametersString())
                        .append("\n")
                        .append("Species Name,Mean Score,Std Score,Median Score,Best Agent Score,Size")
                        .append("\n");
            }
            writer.append("GENERATION ")
                    .append(String.valueOf(generationNumber))
                    .append("\n")
                    .append(neatController.perSpeciesMetricsString())
                    .append("\n");
            writer.close();
            System.out.println("Per species metrics have been logged.");
        } catch (IOException e) {
            System.out.println("Error logging per species metrics: " + e.getMessage());
        }
    }

}
