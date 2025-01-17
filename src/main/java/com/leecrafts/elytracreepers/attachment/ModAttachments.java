package com.leecrafts.elytracreepers.attachment;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.neat.calculations.Calculator;
import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ElytraCreepers.MODID);

    public static final Supplier<AttachmentType<Agent>> AGENT = ATTACHMENT_TYPES.register(
            "agent", () -> AttachmentType.builder(() -> (Agent) null).build()
    );

    public static final Supplier<AttachmentType<Entity>> TARGET_ENTITY = ATTACHMENT_TYPES.register(
            "target_entity", () -> AttachmentType.builder(() -> (Entity) null).build()
    );

    public static final Supplier<AttachmentType<Boolean>> HAD_TARGET = ATTACHMENT_TYPES.register(
            "had_target", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Vec3>> TARGET_MOVEMENT = ATTACHMENT_TYPES.register(
            "target_movement", () -> AttachmentType.builder(() -> Vec3.ZERO).build()
    );

    public static final Supplier<AttachmentType<Float>> FALL_DISTANCE = ATTACHMENT_TYPES.register(
            "fall_distance", () -> AttachmentType.builder(() -> 0f).build()
    );

    public static final Supplier<AttachmentType<Integer>> LAND_TIMESTAMP = ATTACHMENT_TYPES.register(
            "land_timestamp", () -> AttachmentType.builder(() -> -1).build()
    );

    public static final Supplier<AttachmentType<Calculator>> CALCULATOR = ATTACHMENT_TYPES.register(
            "calculator", () -> AttachmentType.builder(() -> (Calculator) null).build()
    );

    public static final Supplier<AttachmentType<Vec3>> ENTITY_VELOCITY = ATTACHMENT_TYPES.register(
            "entity_velocity", () -> AttachmentType.builder(() -> Vec3.ZERO).build()
    );

    public static final Supplier<AttachmentType<Vec3>> ENTITY_PREVIOUS_POS = ATTACHMENT_TYPES.register(
            "entity_previous_pos", () -> AttachmentType.builder(() -> (Vec3) null).build()
    );

    public static final Supplier<AttachmentType<Boolean>> RECENTLY_DAMAGED_MIDAIR = ATTACHMENT_TYPES.register(
            "recently_damaged_midair", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

}
