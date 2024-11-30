package com.leecrafts.elytracreepers.entity;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.entity.custom.TraineeEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ElytraCreepers.MODID);

    public static final Supplier<EntityType<TraineeEntity>> TRAINEE_ENTITY = ENTITY_TYPES.register(
            "trainee",
            () -> EntityType.Builder.of(TraineeEntity::new, MobCategory.MISC)
                    .sized(0.5f, 1.5f)
                    .noSave()
                    .canSpawnFarFromPlayer()
                    .clientTrackingRange(8)
                    .build(null)
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
