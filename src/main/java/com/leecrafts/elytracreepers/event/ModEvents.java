package com.leecrafts.elytracreepers.event;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.item.custom.NeuralElytra;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import com.leecrafts.elytracreepers.neat.util.NEATUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class ModEvents {

//    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.MOD)
//    public static class ModBusEvents {
//
//    }

    private static NEATController neatController;
    public static int REMAINING;
    private static final int SIGHT_DISTANCE = 200;

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void spawnAgents(PlayerInteractEvent.RightClickItem event) {
            if (NEATUtil.TRAINING &&
                    event.getEntity() instanceof Player player &&
                    player.level() instanceof ServerLevel serverLevel) {
                if (event.getItemStack().is(Items.FEATHER)) {
                    neatController = new NEATController(NEATUtil.INPUT_SIZE, NEATUtil.OUTPUT_SIZE, NEATUtil.POPULATION_SIZE);
                    NEATUtil.initializeEntityPopulation(serverLevel, SIGHT_DISTANCE, neatController);
                }
            }
        }

        @SubscribeEvent
        public static void putOnNeuralElytra(PlayerInteractEvent.EntityInteract event) {
            if (event.getTarget() instanceof LivingEntity livingEntity &&
                    !livingEntity.level().isClientSide &&
                    event.getItemStack().is(ModItems.NEURAL_ELYTRA)) {
                livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));
                System.out.println(livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem());
            }
        }

        // this cannot be implemented in the NeuralElytra class because Item#inventoryTick is only called when an item is in a
        // player's inventory
        @SubscribeEvent
        public static void fallFlying(EntityTickEvent.Pre event) {
            if (NeuralElytra.isNonPlayerLivingEntity(event.getEntity())) {
                LivingEntity livingEntity = (LivingEntity) event.getEntity();
                if (!livingEntity.level().isClientSide && NeuralElytra.isWearing(livingEntity)) {
                    // I have to use NBTs because Entity#setSharedFlag is a protected method
                    CompoundTag compoundTag = livingEntity.saveWithoutId(new CompoundTag());
                    if (!livingEntity.onGround() && !livingEntity.isFallFlying()) {
                        compoundTag.putBoolean("FallFlying", true);
                        livingEntity.load(compoundTag);
                        livingEntity.setSharedFlag(7, true);
                    }
                    else if (livingEntity.onGround()) {
                        // TODO change this part
                        compoundTag.putBoolean("FallFlying", false);
                        livingEntity.load(compoundTag);
                        livingEntity.setSharedFlag(7, false);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void agentRunEnd(LivingFallEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (NEATUtil.TRAINING &&
                    livingEntity.level() instanceof ServerLevel serverLevel &&
                    livingEntity.getType() == Config.spawnedElytraEntityType &&
                    NeuralElytra.isWearing(livingEntity)) {
                NEATUtil.recordFitness(livingEntity, serverLevel, SIGHT_DISTANCE, neatController);
            }
        }

    }

}