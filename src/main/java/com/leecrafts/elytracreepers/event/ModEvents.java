package com.leecrafts.elytracreepers.event;

import com.leecrafts.elytracreepers.Config;
import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.entity.ModEntities;
import com.leecrafts.elytracreepers.entity.custom.TraineeEntity;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.item.custom.NeuralElytra;
import com.leecrafts.elytracreepers.neat.calculations.Calculator;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import com.leecrafts.elytracreepers.neat.util.NEATUtil;
import com.leecrafts.elytracreepers.payload.EntityVelocityPayload;
import com.leecrafts.elytracreepers.payload.ServerPayloadHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.joml.Vector3f;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ModEvents {

    private static NEATController neatController;
    private static ServerPlayer trackingPlayer;
    public static int REMAINING_AGENTS;
    public static int REMAINING_GENERATIONS;

    private static final double SIGHT_DISTANCE = 200;
    public static final BlockPos TARGET_INIT_POS = new BlockPos(-189, -63, -2);
//    public static final BlockPos AGENT_SPAWN_POS = TARGET_INIT_POS.offset(0, (int) NEATUtil.AGENT_SPAWN_DISTANCE, 0);
    public static final BlockPos AGENT_SPAWN_POS_PHASE_1 = TARGET_INIT_POS.offset(-100, 100, 0);
    public static final BlockPos AGENT_SPAWN_POS_PHASE_2 = TARGET_INIT_POS.offset(0, 100, 0);

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
                    NEATUtil.initializeEntityPopulation(serverPlayer.serverLevel(), neatController, trackingPlayer, 0);
                }
            }
        }

        @SubscribeEvent
        public static void spawnAgentsProduction(EntityTickEvent.Pre event) {
            if (NEATUtil.PRODUCTION &&
                    event.getEntity() instanceof ServerPlayer serverPlayer &&
                    serverPlayer.tickCount % (Config.spawnInterval * TICKS_PER_SECOND) == 0 &&
                    !serverPlayer.isSpectator()) {
                // spawning mechanics of elytra entities are similar to those of phantoms (see vanilla PhantomSpawner class)
                ServerLevel serverLevel = serverPlayer.serverLevel();
                BlockPos blockPos = serverPlayer.blockPosition();
                ServerStatsCounter serverStatsCounter = serverPlayer.getStats();
                int timeSinceRest = Mth.clamp(serverStatsCounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) &&
                        serverLevel.dimensionType().hasSkyLight() &&
                        (!Config.nightOnlySpawn || serverLevel.getSkyDarken() >= 5) &&
                        (!Config.insomniaOnlySpawn || timeSinceRest >= 72000) &&
                        blockPos.getY() >= serverLevel.getSeaLevel() &&
                        serverLevel.canSeeSky(blockPos)) {
                    boolean success = false;
                    boolean isEnemy = false;
                    for (int i = 0; i < Config.numEntitiesPerSpawn; i++) {
                        LivingEntity livingEntity = attemptSpawns(serverPlayer, serverLevel, blockPos);
                        if (livingEntity != null) {
                            success = true;
                            isEnemy = livingEntity instanceof Enemy;
                        }
                    }
                    if (success) {
                        if (Config.soundWarn) {
                            serverLevel.playSound(
                                    null,
                                    serverPlayer.blockPosition(),
                                    isEnemy ? SoundEvents.AMBIENT_CAVE.value() : SoundEvents.ARROW_HIT_PLAYER,
                                    SoundSource.HOSTILE,
                                    2.0f,
                                    1.0f);
                        }
                        if (Config.subtitleWarn) {
                            serverPlayer.displayClientMessage(Component.literal("Above you."), true);
                        }
                    }
                }
            }
        }

        private static LivingEntity attemptSpawns(ServerPlayer serverPlayer, ServerLevel serverLevel, BlockPos blockPos) {
            for (int i = 0; i < 10; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = (serverPlayer.getRandom().nextBoolean() ? 1 : 0) * NEATUtil.AGENT_SPAWN_DISTANCE;
                int xOffset = (int) (distance * Math.cos(angle));
                int yOffset = (int) NEATUtil.AGENT_SPAWN_DISTANCE;
                int zOffset = (int) (distance * Math.sin(angle));
                BlockPos blockPos1 = blockPos.offset(xOffset, yOffset, zOffset);
//                Entity entity = Config.spawnedEntityType.spawn(serverPlayer.serverLevel(), blockPos, MobSpawnType.MOB_SUMMONED);
                Entity entity = Config.spawnedEntityType.create(serverLevel, null, blockPos1, MobSpawnType.MOB_SUMMONED, false, false);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));

                    livingEntity.setData(ModAttachments.TARGET_ENTITY, serverPlayer);
                    livingEntity.setData(ModAttachments.HAD_TARGET, true);

                    if (livingEntity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }

                    if (!serverLevel.containsAnyLiquid(livingEntity.getBoundingBox()) && serverLevel.isUnobstructed(livingEntity)) {
                        serverLevel.addFreshEntityWithPassengers(livingEntity);
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1));
                        return livingEntity;
                    }

//                    System.out.println("trying spawn again");
                    livingEntity.discard();
                }
            }

            return null;
        }

        // only for testing purposes
        @SubscribeEvent
        public static void putOnNeuralElytra(PlayerInteractEvent.EntityInteract event) {
            if (event.getTarget() instanceof LivingEntity livingEntity &&
                    !livingEntity.level().isClientSide &&
                    event.getItemStack().is(ModItems.NEURAL_ELYTRA)) {
                livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));
                System.out.println(livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem());
            }
        }

        // loading not the agent object itself (too much unnecessary data), but the agent's calculator object.
        @SubscribeEvent
        public static void loadAgent(LivingEquipmentChangeEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (livingEntity.getData(ModAttachments.CALCULATOR) == null &&
                    !NEATUtil.TRAINING &&
                    !livingEntity.level().isClientSide &&
                    event.getTo().is(ModItems.NEURAL_ELYTRA.asItem()) &&
                    NeuralElytra.isNonPlayerLivingEntity(livingEntity)) {
                Calculator agent = NEATUtil.loadAgent(4);
                livingEntity.setData(ModAttachments.CALCULATOR, agent);
            }
        }

        // this cannot be implemented in the NeuralElytra class because Item#inventoryTick is only called when an item is in a
        // player's inventory
        @SubscribeEvent
        public static void fallFlying(EntityTickEvent.Pre event) {
            if (NeuralElytra.isNonPlayerLivingEntity(event.getEntity())) {
                LivingEntity livingEntity = (LivingEntity) event.getEntity();
                if (!livingEntity.level().isClientSide && NeuralElytra.isWearing(livingEntity)) {
                    if (!livingEntity.onGround() && !livingEntity.isFallFlying()) {
                        livingEntity.setSharedFlag(7, true);
                    }
                    else if (livingEntity.onGround()) {
                        livingEntity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.AIR));
//                        livingEntity.setSharedFlag(7, false);

                        if (NEATUtil.PRODUCTION && livingEntity instanceof Mob mob) {
                            mob.persistenceRequired = false;
                        }
                    }
                }
            }
        }

        // When a flying entity lands, it slides a little on the ground.
        // This means I should terminate an agent's run when it finishes sliding on the ground, rather than as soon as it
        // touches the ground.
        // I wouldn't want a flying creeper slide too far past its victims every time.
        @SubscribeEvent
        public static void agentLand(LivingFallEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (NEATUtil.TRAINING &&
                    livingEntity.level() instanceof ServerLevel serverLevel &&
                    livingEntity.getType() == Config.spawnedEntityType &&
                    NeuralElytra.isWearing(livingEntity)) {
//                NEATUtil.recordFitness(livingEntity, event.getDistance(), serverLevel, SIGHT_DISTANCE, neatController, trackingPlayer);
                livingEntity.setData(ModAttachments.FALL_DISTANCE, event.getDistance());
                livingEntity.setData(ModAttachments.LAND_TIMESTAMP, livingEntity.tickCount);

                // the noise of 500 entities falling onto the ground at once can be a bit distracting
                // see LivingEntity#causeFallDamage
                event.setCanceled(true);
            }
        }

        // After sliding for 0.5 seconds on the ground, the agent's run ends and score is recorded
        @SubscribeEvent
        public static void agentRunEndAfterLanding(EntityTickEvent.Pre event) {
            if (NEATUtil.TRAINING &&
                    event.getEntity() instanceof LivingEntity livingEntity &&
                    livingEntity.level() instanceof ServerLevel serverLevel &&
                    livingEntity.getType() == Config.spawnedEntityType &&
                    livingEntity.onGround()/* &&
                    NeuralElytra.isWearing(livingEntity)*/) {
                int landTimestamp = livingEntity.getData(ModAttachments.LAND_TIMESTAMP);
                if (landTimestamp != -1 &&
                        (livingEntity.tickCount - landTimestamp) > (NeuralElytra.INTERPOLATION_FACTOR * TICKS_PER_SECOND)) {
                    NEATUtil.recordFitness(livingEntity, livingEntity.getData(ModAttachments.FALL_DISTANCE), livingEntity.tickCount, serverLevel, neatController, trackingPlayer);
                }
            }
        }

        // agent is automatically terminated if it is too far from the target
        @SubscribeEvent
        public static void agentRunEndFromDistance(EntityTickEvent.Pre event) {
            if (NEATUtil.TRAINING &&
                    event.getEntity() instanceof LivingEntity livingEntity &&
                    livingEntity.level() instanceof ServerLevel serverLevel &&
                    livingEntity.getType() == Config.spawnedEntityType &&
                    NeuralElytra.isWearing(livingEntity)) {
                Entity target = livingEntity.getData(ModAttachments.TARGET_ENTITY);
                if (target != null && livingEntity.distanceTo(target) > SIGHT_DISTANCE) {
                    NEATUtil.recordFitness(livingEntity, (float) Math.abs(livingEntity.getY() - target.getY()), livingEntity.tickCount, serverLevel, neatController, trackingPlayer);
                }
            }
        }

        // Servers do not normally handle deltamovement
        @SubscribeEvent
        public static void entityMovement(EntityTickEvent.Pre event) {
            Entity entity = event.getEntity();
            if (entity.level().isClientSide) {
                Vec3 previousPos = entity.getData(ModAttachments.ENTITY_PREVIOUS_POS);
                Vec3 newVelocity = previousPos != null ? entity.position().subtract(previousPos) : Vec3.ZERO;
                PacketDistributor.sendToServer(new EntityVelocityPayload.EntityVelocity(
                        entity.getId(),
                        new Vector3f((float) newVelocity.x, (float) newVelocity.y, (float) newVelocity.z)));
                entity.setData(ModAttachments.ENTITY_PREVIOUS_POS, entity.position());
            }

            // armor stand movement during training mode
            if (NEATUtil.TRAINING &&
                    NEATUtil.RANDOM_MODE &&
                    entity instanceof ArmorStand armorStand &&
                    armorStand.level() instanceof ServerLevel serverLevel) {
                // armor stand stops moving after it goes 20 * 8 = 160 blocks so that it will not go too far
                if (armorStand.distanceToSqr(TARGET_INIT_POS.getX(), TARGET_INIT_POS.getY(), TARGET_INIT_POS.getZ()) < 160 * 160) {
//                    armorStand.setDeltaMovement(armorStand.getData(ModAttachments.TARGET_MOVEMENT));
                    armorStand.setPos(armorStand.position().add(armorStand.getData(ModAttachments.TARGET_MOVEMENT)));
                }
            }
        }

        // elytra entities take twice the damage when flying
        @SubscribeEvent
        public static void doubleAirDamage(LivingDamageEvent.Pre event) {
            LivingEntity livingEntity = event.getEntity();
            if (!livingEntity.level().isClientSide &&
                    livingEntity.getData(ModAttachments.HAD_TARGET) &&
                    livingEntity.isFallFlying()) {
                event.setNewDamage(2 * event.getOriginalDamage());
            }
        }

        // If the "griefing" config value is set to false (default), then entities flying on a neural elytra cannot
        // destroy blocks
        // For example, creepers that fly on a neural elytra cannot destroy blocks via exploding
        @SubscribeEvent
        public static void explosionGriefing(ExplosionEvent.Detonate event) {
            Entity entity = event.getExplosion().getDirectSourceEntity();
            // the reason why HAD_TARGET data attachment is needed:
            // There was a bug where, if multiple creepers explode on the player and player dies, the remaining explosions
            // are treated like normal explosions, which can destroy blocks and/or other entities
            if (!entity.level().isClientSide &&
                    entity.getType() == Config.spawnedEntityType &&
                    entity.getData(ModAttachments.HAD_TARGET)) {
                if (!Config.griefing) {
                    event.getAffectedBlocks().clear();
                }
                if (Config.explodeHurtOnlyTarget) {
                    event.getAffectedEntities().clear();
                    Entity target = entity.getData(ModAttachments.TARGET_ENTITY);
                    if (target != null) {
                        event.getAffectedEntities().add(target);
                    }
                }

                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.removeEffect(MobEffects.GLOWING);
                }
            }
        }

        // automatically ignite creeper when it lands on the ground
        @SubscribeEvent
        public static void autoIgnite(LivingFallEvent event) {
            LivingEntity livingEntity = event.getEntity();
            Entity target = livingEntity.getData(ModAttachments.TARGET_ENTITY);
            if (NEATUtil.PRODUCTION &&
                    !livingEntity.level().isClientSide &&
                    livingEntity.getType() == Config.spawnedEntityType &&
                    Config.autoIgnite &&
                    target != null) {
                if (livingEntity instanceof Creeper creeper &&
                        creeper.distanceTo(target) < 7 &&
                        (!(target instanceof Player) || !((Player) target).isCreative())) {
                    creeper.ignite();
                }
            }
        }

        // There were several bugs regarding creeper behaviors and griefing
        // Solving the one described in the explosionGriefing event listener caused another bug: the respawned player
        // would not be damaged from those same group of creepers that included the creeper that had killed the player
        // So my solution is to simply discard those creepers
        // I hope I don't have to explain this to anybody
        @SubscribeEvent
        public static void removeCreepersWithLostTargets(EntityTickEvent.Pre event) {
            if (event.getEntity() instanceof Creeper creeper && !creeper.level().isClientSide) {
                Entity target = creeper.getData(ModAttachments.TARGET_ENTITY);
                if (creeper.getData(ModAttachments.HAD_TARGET) && (target == null || target.isRemoved())) {
                    creeper.discard();
                }
            }
        }

        // preventing elytra entities from dropping neural elytras upon death
        // because i don't want neural elytras to be obtainable by players (for the time being)
        @SubscribeEvent
        public static void elytraEntityDeath(LivingDeathEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (!livingEntity.level().isClientSide && NeuralElytra.isWearing(livingEntity)) {
                livingEntity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            }
        }

    }

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameBusClientEvents {

        @SubscribeEvent
        public static void entityRender(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
            LivingEntity entity = event.getEntity();
            float partialTick = event.getPartialTick();
            PoseStack poseStack = event.getPoseStack();
            if (!(entity instanceof TraineeEntity) && !(entity instanceof Player) && entity.isFallFlying()) {
                // Strangely enough, I had to copy and paste these 2 lines of code to TraineeRenderer#actuallyRender.
                // Otherwise, if I got rid of the "!(entity instanceof TraineeEntity)" condition and didn't modify
                // TraineeRenderer#actuallyRender, the rotations would be inaccurate for the Trainee entity.
                // This behavior may be due to the way GeckoLib entities handle rotations.
                poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getViewYRot(partialTick)));
                poseStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(partialTick) + 90));
            }
        }

    }

    @EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            // Sets the current network version
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToServer(
                    EntityVelocityPayload.EntityVelocity.TYPE,
                    EntityVelocityPayload.EntityVelocity.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ServerPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }

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