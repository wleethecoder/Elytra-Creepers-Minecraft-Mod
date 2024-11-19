package com.leecrafts.elytracreepers.attachment;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.util.NeuralNetwork;
import com.leecrafts.elytracreepers.util.NeuralNetworkUtil;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ElytraCreepers.MODID);

    public static final Supplier<AttachmentType<NeuralNetwork>> NEURAL_NETWORK = ATTACHMENT_TYPES.register(
            "neural_network", () -> AttachmentType.builder(() -> NeuralNetworkUtil.NETWORK).build()
    );

    public static final Supplier<AttachmentType<Entity>> TARGET_ENTITY = ATTACHMENT_TYPES.register(
            "target_entity", () -> AttachmentType.builder(() -> (Entity) null).build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

}
