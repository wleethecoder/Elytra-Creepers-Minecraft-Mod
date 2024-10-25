package com.leecrafts.elytracreepers.event;

import com.leecrafts.elytracreepers.FlyingCreepers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = FlyingCreepers.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void putElytraTest(PlayerInteractEvent.EntityInteract event) {
            Entity target = event.getTarget();
            if (!target.level().isClientSide && target instanceof LivingEntity creeper) {
                creeper.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
                System.out.println(creeper.getItemBySlot(EquipmentSlot.CHEST).getItem());
            }
        }

        @SubscribeEvent
        public static void fallFlyingTest(LivingEvent.LivingTickEvent event) {
            LivingEntity livingEntity = event.getEntity();
            if (!livingEntity.level().isClientSide &&
                    livingEntity instanceof Creeper creeper &&
                    !creeper.onGround() &&
                    !creeper.isFallFlying()) {
                CompoundTag compoundTag = creeper.saveWithoutId(new CompoundTag());
                compoundTag.putBoolean("FallFlying", true);
                creeper.load(compoundTag);
//                creeper.startFallFlying();
//                ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
//                if (itemstack.canElytraFly(this)) {
//                    this.startFallFlying();
//                    return true;
//                }
            }
        }

    }

}
