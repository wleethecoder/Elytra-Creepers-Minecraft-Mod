package com.leecrafts.bowmaster.event;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.livingentity.ILivingEntityCap;
import com.leecrafts.bowmaster.capability.livingentity.LivingEntityCapProvider;
import com.leecrafts.bowmaster.capability.player.IPlayerCap;
import com.leecrafts.bowmaster.capability.player.PlayerCap;
import com.leecrafts.bowmaster.capability.player.PlayerCapProvider;
import com.leecrafts.bowmaster.entity.ModEntityTypes;
import com.leecrafts.bowmaster.entity.client.SkeletonBowMasterModel;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;
import com.leecrafts.bowmaster.packet.PacketHandler;
import com.leecrafts.bowmaster.packet.ServerboundLivingEntityVelocityPacket;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import com.leecrafts.bowmaster.world.portal.ModTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import static com.leecrafts.bowmaster.world.portal.ModTeleporter.ARENA_WIDTH;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = SkeletonBowMaster.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEventBusEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IPlayerCap.class);
            event.register(ILivingEntityCap.class);
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player player && !player.getCommandSenderWorld().isClientSide) {
                if (!player.getCapability(ModCapabilities.PLAYER_CAPABILITY).isPresent()) {
                    PlayerCapProvider playerCapProvider = new PlayerCapProvider();
                    event.addCapability(new ResourceLocation(SkeletonBowMaster.MODID, "player"), playerCapProvider);
                }
            }
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventLivingEntity(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity livingEntity && !livingEntity.getCommandSenderWorld().isClientSide) {
                if (!livingEntity.getCapability(ModCapabilities.LIVING_ENTITY_CAPABILITY).isPresent()) {
                    LivingEntityCapProvider livingEntityCapProvider = new LivingEntityCapProvider();
                    event.addCapability(new ResourceLocation(SkeletonBowMaster.MODID, "living_entity"), livingEntityCapProvider);
                    if (!(livingEntity instanceof Player)) {
                        event.addListener(livingEntityCapProvider::invalidate);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void playerInteract(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide && event.getTarget() instanceof SkeletonBowMasterEntity) {
                ModTeleporter.teleportPlayer(player);
            }
        }

        @SubscribeEvent
        public static void hurtEntity(LivingHurtEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (!livingEntity.level().isClientSide &&
                    event.getSource().getEntity() instanceof SkeletonBowMasterEntity skeletonBowMasterEntity &&
                    !livingEntity.is(skeletonBowMasterEntity)) {
                skeletonBowMasterEntity.storeRewards(event.getAmount());
            }
        }

        @SubscribeEvent
        public static void skeletonBowMasterTrainBattleEnd(LivingDeathEvent event) {
            if (SkeletonBowMasterEntity.TRAINING &&
                    event.getEntity() instanceof SkeletonBowMasterEntity loser &&
                    !loser.level().isClientSide &&
                    event.getSource().getEntity() instanceof SkeletonBowMasterEntity winner) {
                // log rewards
                NeuralNetworkUtil.logRewards(winner.getRewards(), loser.getRewards());

                // update network from both the winner's and loser's data
                NeuralNetwork network = winner.getNetwork(); // winner and loser have the same network
                NeuralNetworkUtil.updateNetwork(
                        network, winner.getStates(), winner.getActionProbs(), winner.getActions(), winner.getRewards());
                NeuralNetworkUtil.updateNetwork(
                        network, loser.getStates(), loser.getActionProbs(), loser.getActions(), loser.getRewards());

                // save network
                NeuralNetworkUtil.saveModel(network);
                Player spectator = winner.level().getNearestPlayer(winner, 200);
                if (spectator != null) {
                    spectator.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                        PlayerCap playerCap = (PlayerCap) iPlayerCap;
                        // Starting a timer before spawning new skeleton bow masters
                        // This will ensure that the process of saving the network as a file will complete before the newly
                        // spawned skeleton bow masters load that network.
                        playerCap.afterTrainBattleCounter = 0;
                    });
                }

                winner.kill();
            }
        }

        @SubscribeEvent
        public static void onPlayerCloneEvent(PlayerEvent.Clone event) {
            Player originalPlayer = event.getOriginal();
            Player player = event.getEntity();
            originalPlayer.reviveCaps();
            originalPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap1 -> {
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    PlayerCap playerCap1 = (PlayerCap) iPlayerCap1;
                    playerCap1.outsideDimBlockPos = playerCap.outsideDimBlockPos;
                    playerCap1.arenaDimBlockPos = playerCap.arenaDimBlockPos;
                    playerCap1.afterTrainBattleCounter = playerCap.afterTrainBattleCounter;
                });
            });
            originalPlayer.invalidateCaps();
        }

        @SubscribeEvent
        public static void livingTickEvent(LivingEvent.LivingTickEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (livingEntity.level().isClientSide) {
                // Sending livingEntity deltaMovement (velocity) data to the server so that the skeleton bow master
                // can read the velocity of its opponents
                PacketHandler.INSTANCE.send(new ServerboundLivingEntityVelocityPacket(
                        livingEntity.getId(), livingEntity.getDeltaMovement()), PacketDistributor.SERVER.noArg());
            }
            else {
                if (SkeletonBowMasterEntity.TRAINING &&
                        livingEntity instanceof Player player &&
                        player.level() instanceof ServerLevel serverLevel) {
                    player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                        PlayerCap playerCap = (PlayerCap) iPlayerCap;
                        if (playerCap.afterTrainBattleCounter >= 0) {
                            if (playerCap.afterTrainBattleCounter >= PlayerCap.afterTrainBattleCounterLimit) {
                                if (playerCap.arenaDimBlockPos.length > 0) {
                                    BlockPos center = playerCap.getArenaDimBlockPos();
                                    ModTeleporter.spawnSkeletonBowMaster(center, serverLevel, ARENA_WIDTH / 4);
                                    ModTeleporter.spawnSkeletonBowMaster(center, serverLevel, -ARENA_WIDTH / 4);
                                }
                                playerCap.afterTrainBattleCounter = -1;
                            }
                            else {
                                playerCap.afterTrainBattleCounter++;
                            }
                        }
                    });
                }
            }
        }

    }

    @Mod.EventBusSubscriber(modid = SkeletonBowMaster.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.SKELETON_BOW_MASTER.get(), SkeletonBowMasterEntity.createAttributes().build());
        }

    }

    @Mod.EventBusSubscriber(modid = SkeletonBowMaster.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEventBusClientEvents {

        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(SkeletonBowMasterModel.LAYER_LOCATION, SkeletonBowMasterModel::createBodyLayer);
        }

    }

}
