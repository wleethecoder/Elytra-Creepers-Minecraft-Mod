package com.leecrafts.elytracreepers.event;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.item.custom.NeuralElytra;
import com.leecrafts.elytracreepers.util.NeuralNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class ModEvents {

    private static final boolean TRAINING = true;
    private static final BlockPos SPAWN_POS = new BlockPos(-24, -23, 3);

//    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.MOD)
//    public static class ModBusEvents {
//
//    }

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void spawnAgents(PlayerInteractEvent.RightClickItem event) {
            if (TRAINING &&
                    event.getEntity() instanceof Player player &&
                    player.level() instanceof ServerLevel serverLevel) {
                if (event.getItemStack().is(Items.FEATHER)) {
                    Config.spawnedElytraEntityType.spawn(serverLevel, SPAWN_POS, MobSpawnType.MOB_SUMMONED);
                }
            }
        }

        @SubscribeEvent
        public static void putElytraTest(PlayerInteractEvent.EntityInteract event) {
            if (event.getTarget() instanceof LivingEntity livingEntity &&
                    !livingEntity.level().isClientSide &&
                    event.getItemStack().is(ModItems.NEURAL_ELYTRA)) {
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

        // this cannot be implemented in the NeuralElytra class because Item#inventoryTick is only called when an item is in a
        // player's inventory
        @SubscribeEvent
        public static void fallFlyingTest(EntityTickEvent.Pre event) {
            if (NeuralElytra.isNonPlayerLivingEntity(event.getEntity())) {
                LivingEntity livingEntity = (LivingEntity) event.getEntity();
                if (!livingEntity.level().isClientSide &&
                        livingEntity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.NEURAL_ELYTRA.asItem())) {
                    if (!livingEntity.onGround() && !livingEntity.isFallFlying()) {
                        CompoundTag compoundTag = livingEntity.saveWithoutId(new CompoundTag());
                        compoundTag.putBoolean("FallFlying", true);
                        livingEntity.load(compoundTag);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void creeperTickTest(EntityTickEvent.Pre event) {
            Entity entity = event.getEntity();
            if (event.getEntity().getType() == Config.spawnedElytraEntityType && !entity.level().isClientSide) {
                NeuralNetwork neuralNetwork = entity.getData(ModAttachments.NEURAL_NETWORK);
                if (entity.tickCount % 40 == 0) {
//                    neuralNetwork.printWeights();
                }
            }
        }

    }

}