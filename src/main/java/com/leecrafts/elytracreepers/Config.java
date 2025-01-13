package com.leecrafts.elytracreepers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    private static final ModConfigSpec.ConfigValue<String> SPAWNED_ENTITY_TYPE = BUILDER
            .comment("The type of entities that will spawn with a neural elytra")
            .define("spawned_entity_type", "minecraft:creeper");

    private static final ModConfigSpec.ConfigValue<Boolean> GRIEFING = BUILDER
            .comment("Whether or not entities flying with a neural elytra are able to destroy blocks (e.g. via explosions)")
            .define("griefing", false);

    private static final ModConfigSpec.ConfigValue<Boolean> AUTO_IGNITE = BUILDER
            .comment("Whether or not creepers flying with a neural elytra ignite automatically when landing on the ground " +
                    "(it also has to be within 7 blocks of its target)")
            .define("auto_ignite", true);

    private static final ModConfigSpec.ConfigValue<Boolean> EXPLODE_HURT_ONLY_TARGET = BUILDER
            .comment("Whether or not creepers flying with a neural elytra hurt only the target when exploding")
            .define("explode_hurt_only_target", true);

    private static final ModConfigSpec.IntValue NUM_ENTITIES_PER_SPAWN = BUILDER
            .comment("How many entities with a neural elytra spawn at a time")
            .defineInRange("num_entities_per_spawn", 1, 0, 10);

    private static final ModConfigSpec.IntValue SPAWN_INTERVAL = BUILDER
            .comment("Length of time interval (in seconds) between spawns")
            .defineInRange("spawn_interval", 60, 5, 180);

    private static final ModConfigSpec.ConfigValue<Boolean> NIGHT_ONLY_SPAWN = BUILDER
            .comment("Whether or not entities with a neural elytra spawn only in the night")
            .define("night_only_spawn", true);

    private static final ModConfigSpec.ConfigValue<Boolean> INSOMNIA_ONLY_SPAWN = BUILDER
            .comment("Whether or not entities with a neural elytra spawn only when the player has insomnia")
            .define("insomnia_only_spawn", false);

    private static final ModConfigSpec.ConfigValue<Boolean> SUBTITLE_WARN = BUILDER
            .comment("Displays a subtitle whenever entities with a neural elytra spawn")
            .define("subtitle_warn", true);

    private static final ModConfigSpec.ConfigValue<Boolean> SOUND_WARN = BUILDER
            .comment("Plays a sound whenever entities with a neural elytra spawn")
            .define("sound_warn", true);


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    public static EntityType<?> spawnedEntityType;
    public static boolean griefing;
    public static boolean autoIgnite;
    public static boolean explodeHurtOnlyTarget;
    public static int numEntitiesPerSpawn;
    public static int spawnInterval;
    public static boolean nightOnlySpawn;
    public static boolean insomniaOnlySpawn;
    public static boolean subtitleWarn;
    public static boolean soundWarn;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());

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
        autoIgnite = AUTO_IGNITE.get();
        explodeHurtOnlyTarget = EXPLODE_HURT_ONLY_TARGET.get();
        numEntitiesPerSpawn = NUM_ENTITIES_PER_SPAWN.get();
        spawnInterval = SPAWN_INTERVAL.get();
        nightOnlySpawn = NIGHT_ONLY_SPAWN.get();
        insomniaOnlySpawn = INSOMNIA_ONLY_SPAWN.get();
        subtitleWarn = SUBTITLE_WARN.get();
        soundWarn = SOUND_WARN.get();
    }
}
