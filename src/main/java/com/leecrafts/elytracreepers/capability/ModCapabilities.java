package com.leecrafts.elytracreepers.capability;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.capability.custom.INeuralHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class ModCapabilities {

    public static final EntityCapability<INeuralHandler, Void> NEURAL_HANDLER_ENTITY =
            EntityCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(ElytraCreepers.MODID, "neural_handler"),
                    INeuralHandler.class
            );

}
