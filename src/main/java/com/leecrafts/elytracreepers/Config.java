package com.leecrafts.elytracreepers;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Optional;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> SPAWNED_ENTITY_TYPE = BUILDER
            .comment("The type of entities that spawn with an elytra")
            .define("spawned_entity_type", "minecraft:creeper");

    private static final ModConfigSpec.BooleanValue GRIEFING = BUILDER
            .comment("Whether or not elytra-wearing entities are able to destroy blocks (e.g. via explosions)")
            .define("griefing", false);

    private static final ModConfigSpec.BooleanValue SMART_IGNITE = BUILDER
            .comment("Whether or not elytra-wearing creepers ignite at a \"smarter\" time so that their explosions are more accurate")
            .define("smart_ignite", true);

    private static final ModConfigSpec.BooleanValue EXPLODE_HURT_ONLY_TARGET = BUILDER
            .comment("Whether or not elytra-wearing creepers hurt only the player target when exploding")
            .define("explode_hurt_only_target", true);

    private static final ModConfigSpec.IntValue NUM_ENTITIES_PER_SPAWN = BUILDER
            .comment("How many elytra-wearing entities spawn at a time")
            .defineInRange("num_entities_per_spawn", 1, 0, 10);

    private static final ModConfigSpec.IntValue SPAWN_INTERVAL = BUILDER
            .comment("Length of time interval (in seconds) between spawns")
            .defineInRange("spawn_interval", 60, 5, 180);

    private static final ModConfigSpec.IntValue MAXIMUM_SPAWN_CAP = BUILDER
            .comment("The maximum amount of elytra-wearing entities that can spawn per player")
            .defineInRange("maximum_spawn_cap", 10, 1, 75);

    private static final ModConfigSpec.BooleanValue NIGHT_ONLY_SPAWN = BUILDER
            .comment("Whether or not elytra-wearing entities spawn only in the night")
            .define("night_only_spawn", true);

    private static final ModConfigSpec.BooleanValue INSOMNIA_ONLY_SPAWN = BUILDER
            .comment("Whether or not elytra-wearing entities spawn only when the player has insomnia")
            .define("insomnia_only_spawn", false);

    private static final ModConfigSpec.BooleanValue SUBTITLE_WARN = BUILDER
            .comment("Displays a subtitle whenever elytra-wearing entities spawn")
            .define("subtitle_warn", true);

    private static final ModConfigSpec.BooleanValue SOUND_WARN = BUILDER
            .comment("Plays a sound whenever elytra-wearing entities spawn")
            .define("sound_warn", true);


    static final ModConfigSpec SPEC = BUILDER.build();

    public static EntityType<?> spawnedEntityType;
    public static boolean griefing;
    public static boolean smartIgnite;
    public static boolean explodeHurtOnlyTarget;
    public static int numEntitiesPerSpawn;
    public static int spawnInterval;
    public static int maximumSpawnCap;
    public static boolean nightOnlySpawn;
    public static boolean insomniaOnlySpawn;
    public static boolean subtitleWarn;
    public static boolean soundWarn;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        Optional<EntityType<?>> entityTypeOptional = EntityType.byString(SPAWNED_ENTITY_TYPE.get());
        if (entityTypeOptional.isPresent()) {
            spawnedEntityType = entityTypeOptional.get();
        }
        else {
            System.out.println("the value entered for 'spawned_entity_type' in elytracreepers-common.toml not recognized, " +
                    "defaulting to minecraft:creeper");
            spawnedEntityType = EntityType.CREEPER;
        }

        griefing = GRIEFING.get();
        smartIgnite = SMART_IGNITE.get();
        explodeHurtOnlyTarget = EXPLODE_HURT_ONLY_TARGET.get();
        numEntitiesPerSpawn = NUM_ENTITIES_PER_SPAWN.get();
        spawnInterval = SPAWN_INTERVAL.get();
        maximumSpawnCap = MAXIMUM_SPAWN_CAP.get();
        nightOnlySpawn = NIGHT_ONLY_SPAWN.get();
        insomniaOnlySpawn = INSOMNIA_ONLY_SPAWN.get();
        subtitleWarn = SUBTITLE_WARN.get();
        soundWarn = SOUND_WARN.get();
    }
}
