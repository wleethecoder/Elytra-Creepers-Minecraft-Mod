package com.leecrafts.elytracreepers.event;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.entity.ModEntities;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.item.custom.NeuralElytra;
import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import com.leecrafts.elytracreepers.neat.util.NEATUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ModEvents {

    private static NEATController neatController;
    private static ServerPlayer trackingPlayer;
    public static int REMAINING_AGENTS;
    public static int REMAINING_GENERATIONS;

    private static final double SIGHT_DISTANCE = 200;
    private static final double SPAWN_DISTANCE = 100;
    public static final BlockPos SPAWN_POS = new BlockPos((int) (-189 - SPAWN_DISTANCE), (int) (-64 + SPAWN_DISTANCE + 1), -2);

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void spawnAgentsTrain(PlayerInteractEvent.RightClickItem event) {
            if (NEATUtil.TRAINING &&
                    event.getEntity() instanceof ServerPlayer serverPlayer) {
                if (event.getItemStack().is(Items.FEATHER)) {
                    neatController = NEATUtil.loadNEATController();
                    if (neatController == null) {
                        neatController = new NEATController(NEATUtil.INPUT_SIZE, NEATUtil.OUTPUT_SIZE, NEATUtil.POPULATION_SIZE);
                        REMAINING_GENERATIONS = NEATUtil.NUM_GENERATIONS;
                    }
                    trackingPlayer = serverPlayer;
                    NEATUtil.initializeEntityPopulation(serverPlayer.serverLevel(), SIGHT_DISTANCE, neatController, trackingPlayer);
                }
            }
        }

        @SubscribeEvent
        public static void spawnAgentsProduction(EntityTickEvent.Pre event) {
            if (NEATUtil.PRODUCTION &&
                    event.getEntity() instanceof ServerPlayer serverPlayer &&
                    serverPlayer.tickCount % (5 * TICKS_PER_SECOND) == 0 &&
                    serverPlayer.getMainHandItem().is(Items.OAK_BUTTON)) {
                double angle = Math.random() * 2 * Math.PI;
                int xOffset = (int) (SPAWN_DISTANCE * Math.cos(angle));
                int zOffset = (int) (SPAWN_DISTANCE * Math.sin(angle));
                BlockPos blockPos = serverPlayer.blockPosition().offset(xOffset, (int) SPAWN_DISTANCE, zOffset);
                Entity entity = Config.spawnedElytraEntityType.spawn(serverPlayer.serverLevel(), blockPos, MobSpawnType.MOB_SUMMONED);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));

                    List<Player> candidates = livingEntity.level().getEntitiesOfClass(
                            Player.class, livingEntity.getBoundingBox().inflate(SIGHT_DISTANCE));
                    if (!candidates.isEmpty()) {
                        int index = entity.getRandom().nextInt(candidates.size());
                        livingEntity.setData(ModAttachments.TARGET_ENTITY, candidates.get(index));
                    }

                    if (livingEntity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }
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

        @SubscribeEvent
        public static void loadAgent(LivingEquipmentChangeEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (livingEntity.getData(ModAttachments.AGENT) == null &&
                    !NEATUtil.TRAINING &&
                    !livingEntity.level().isClientSide &&
                    event.getTo().is(ModItems.NEURAL_ELYTRA.asItem()) &&
                    NeuralElytra.isNonPlayerLivingEntity(livingEntity)) {
                Agent agent = NEATUtil.loadAgent(999);
                livingEntity.setData(ModAttachments.AGENT, agent);
                System.out.println("loaded agent score: " + livingEntity.getData(ModAttachments.AGENT).getScore());

//                NEATController neatController1 = NEATUtil.loadNEATController(1, 301);
//                System.out.println("loaded neatController best agent score: " + neatController1.getBestAgent().getScore());
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

        // if agent lands, it is terminated and its score is recorded
        @SubscribeEvent
        public static void agentRunEndFromLand(LivingFallEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (NEATUtil.TRAINING &&
                    livingEntity.level() instanceof ServerLevel serverLevel &&
                    livingEntity.getType() == Config.spawnedElytraEntityType &&
                    NeuralElytra.isWearing(livingEntity)) {
                NEATUtil.recordFitness(livingEntity, event.getDistance(), serverLevel, SIGHT_DISTANCE, neatController, trackingPlayer);

                // the noise of 500 entities falling onto the ground at once can be a bit distracting
                // see LivingEntity#causeFallDamage
                event.setCanceled(true);
            }
        }

        // agent is automatically terminated if it is too far from the target
        @SubscribeEvent
        public static void agentRunEndFromDistance(EntityTickEvent.Pre event) {
            if (NEATUtil.TRAINING &&
                    event.getEntity() instanceof LivingEntity livingEntity &&
                    livingEntity.level() instanceof ServerLevel serverLevel &&
                    livingEntity.getType() == Config.spawnedElytraEntityType &&
                    NeuralElytra.isWearing(livingEntity)) {
                Entity target = livingEntity.getData(ModAttachments.TARGET_ENTITY);
                if (target != null && livingEntity.distanceTo(target) > SIGHT_DISTANCE) {
                    NEATUtil.recordFitness(livingEntity, (float) Math.abs(livingEntity.getY() - target.getY()), serverLevel, SIGHT_DISTANCE, neatController, trackingPlayer);
                }
            }
        }

    }

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void createDefaultAttributes(EntityAttributeCreationEvent event) {
            event.put(
                    ModEntities.TRAINEE_ENTITY.get(),
                    Mob.createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 1)
                            .build()
            );
        }

    }

}