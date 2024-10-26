package com.leecrafts.elytracreepers.item.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;

public class NeuralElytra extends ElytraItem {

    public NeuralElytra(Properties properties) {
        super(properties);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        super.elytraFlightTick(stack, entity, flightTicks);
        // Testing if item can control entity
        entity.setXRot(45.0f);
        return true;
    }
}
