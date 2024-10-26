package com.leecrafts.elytracreepers.event;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = ElytraCreepers.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    @SubscribeEvent
    public static void putElytraTest(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!target.level().isClientSide && target instanceof LivingEntity creeper) {
            creeper.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike) ModItems.NEURAL_ELYTRA));
            System.out.println(creeper.getItemBySlot(EquipmentSlot.CHEST).getItem());
        }
    }

    @SubscribeEvent
    public static void fallFlyingTest(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide &&
                entity instanceof Creeper creeper &&
                !creeper.onGround() &&
                !creeper.isFallFlying()) {
            CompoundTag compoundTag = creeper.saveWithoutId(new CompoundTag());
            compoundTag.putBoolean("FallFlying", true);
            creeper.load(compoundTag);
//            creeper.startFallFlying();
//            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
//            if (itemstack.canElytraFly(this)) {
//                this.startFallFlying();
//                return true;
//            }
        }
    }

}