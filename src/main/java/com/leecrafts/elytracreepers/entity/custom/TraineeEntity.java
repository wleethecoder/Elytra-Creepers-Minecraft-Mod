package com.leecrafts.elytracreepers.entity.custom;

import com.leecrafts.elytracreepers.neat.util.NEATUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class TraineeEntity extends Mob implements GeoEntity {

    private final AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);

    public TraineeEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (NEATUtil.PRODUCTION &&
                this.isFallFlying() &&
                this.tickCount > 10 * TICKS_PER_SECOND &&
                this.level() instanceof ServerLevel serverLevel) {
            // during production mode, this entity dies, spawning a cookie in its place
            serverLevel.addFreshEntity(
                    new ItemEntity(serverLevel, this.getX(), this.getY(), this.getZ(), new ItemStack(Items.COOKIE)));
            this.kill();
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        // immune to entity cramming damage
        if (source.is(DamageTypes.CRAMMING)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableInstanceCache;
    }

}
