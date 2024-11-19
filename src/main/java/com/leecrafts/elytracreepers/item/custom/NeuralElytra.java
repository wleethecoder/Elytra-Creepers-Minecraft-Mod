package com.leecrafts.elytracreepers.item.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NeuralElytra extends ElytraItem {

    public NeuralElytra(Properties properties) {
        super(properties);
    }

    @Override
    public boolean elytraFlightTick(@NotNull ItemStack stack, @NotNull LivingEntity entity, int flightTicks) {
        super.elytraFlightTick(stack, entity, flightTicks);
        if (isNonPlayerLivingEntity(entity)) {
            // Testing if item can control entity
            entity.setXRot(45.0f);
        }
        return true;
    }

    public static boolean isNonPlayerLivingEntity(Entity entity) {
        return entity instanceof LivingEntity livingEntity && !(livingEntity instanceof Player);
    }

}
