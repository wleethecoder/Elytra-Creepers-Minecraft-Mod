package com.leecrafts.elytracreepers.attachment;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.util.NeuralNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    private static final int INPUT_SIZE = 7; // TODO change if needed
    private static final int OUTPUT_SIZE = 2;
    private static final NeuralNetwork NETWORK =
            new NeuralNetwork(new int[] {INPUT_SIZE, 16, 32, OUTPUT_SIZE}, "linear", "relu");

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ElytraCreepers.MODID);

    public static final Supplier<AttachmentType<NeuralNetwork>> NEURAL_NETWORK = ATTACHMENT_TYPES.register(
            "neural_network", () -> AttachmentType.builder(() -> NETWORK).build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

}
