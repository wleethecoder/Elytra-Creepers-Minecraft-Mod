package com.leecrafts.elytracreepers.attachment;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.util.NeuralNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ElytraCreepers.MODID);

    // TODO change
    public static final Supplier<AttachmentType<NeuralNetwork>> NEURAL_NETWORK = ATTACHMENT_TYPES.register(
            "neural_network", () -> AttachmentType.builder(() -> new NeuralNetwork(new int[]{0,0,0}, "linear", "linear")).build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

}
