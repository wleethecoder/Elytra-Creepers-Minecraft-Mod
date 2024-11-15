package com.leecrafts.elytracreepers.event;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.capability.ModCapabilities;
import com.leecrafts.elytracreepers.capability.custom.INeuralHandler;
import com.leecrafts.elytracreepers.capability.custom.NeuralHandler;
import com.leecrafts.elytracreepers.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class ModEvents {

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            if (!event.isEntityRegistered(ModCapabilities.NEURAL_HANDLER_ENTITY, EntityType.CREEPER)) {
                event.registerEntity(ModCapabilities.NEURAL_HANDLER_ENTITY,
                        EntityType.CREEPER,
                        (entity, ctx) -> new NeuralHandler());
            }
        }

    }

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void putElytraTest(PlayerInteractEvent.EntityInteract event) {
            if (event.getTarget() instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide) {
                livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));
                System.out.println(livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem());
            }
        }

        // armor stands are the only LivingEntity that is not pushable by any condition
        // this is important. Due to the nature of the training process, many agents will be clustered together, causing a lot of collision
        @SubscribeEvent
        public static void armorStandElytraSpawnTest(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof ArmorStand armorStand && !armorStand.level().isClientSide) {
                armorStand.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));
                System.out.println(armorStand.getItemBySlot(EquipmentSlot.CHEST).getItem());
            }
        }

        @SubscribeEvent
        public static void fallFlyingTest(EntityTickEvent.Pre event) {
            if (event.getEntity() instanceof LivingEntity livingEntity &&
                    !livingEntity.level().isClientSide &&
                    livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.NEURAL_ELYTRA.asItem() &&
                    !livingEntity.onGround() &&
                    !livingEntity.isFallFlying()) {
                CompoundTag compoundTag = livingEntity.saveWithoutId(new CompoundTag());
                compoundTag.putBoolean("FallFlying", true);
                livingEntity.load(compoundTag);
            }
        }

        @SubscribeEvent
        public static void creeperTickTest(EntityTickEvent.Pre event) {
            if (event.getEntity() instanceof Creeper creeper && !creeper.level().isClientSide) {
                INeuralHandler handler = creeper.getCapability(ModCapabilities.NEURAL_HANDLER_ENTITY);
                if (handler != null && creeper.tickCount % 40 == 0) {
                    System.out.println("chat is this real " + handler.getNum() + "; " + handler.getNetwork().getWeights());
                }
            }
        }

    }

}