package com.leecrafts.bowmaster.entity.goal;

import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;

public class AIRangedBowAttackGoal<T extends SkeletonBowMasterEntity & RangedAttackMob> extends Goal {

    private final T mob;

    public AIRangedBowAttackGoal(T mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.mob.isHolding(is -> is.getItem() instanceof BowItem);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity != null) {
            RandomSource random = this.mob.getRandom();
            boolean useItem = random.nextInt(10) > 0;
            // TODO it can be forward, backward, or nothing
            boolean goForward = random.nextInt(2) > 0;
            // TODO it can be left, right, or nothing
            boolean goLeft = random.nextInt(2) > 0;
            boolean jump = random.nextInt(10) == 0;

            float f = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) (0.25 * f);
            this.mob.setSpeed(f1);

            // I could use MoveControl#strafe, but there are some unwanted hardcoded behaviors

            if (goForward) {
                this.mob.forwardImpulse(random.nextBoolean() ? 1.0f : -1.0f);
            }

            if (goLeft) {
                this.mob.setXxa(random.nextBoolean() ? 1.0f : -1.0f);
            }

            if (jump) {
                this.mob.getJumpControl().jump();
            }

            this.mob.lookAt(livingEntity, 360, 360);
//            this.mob.setXRot(this.mob.getXRot() + 360 * random.nextFloat());
//            this.mob.setYRot(this.mob.getYRot() + 360 * random.nextFloat());

            if (useItem) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
            }
            else {
                if (this.mob.isUsingItem()) {
                    int i = this.mob.getTicksUsingItem();
                    // TODO actually 5??
                    if (i >= 5) {
                        this.mob.performRangedAttack(livingEntity, BowItem.getPowerForTime(i));
                    }
                    this.mob.stopUsingItem();
                }
            }
//            if (this.mob.isUsingItem()) {
//                int i = this.mob.getTicksUsingItem();
//                if (i >= 5) {
//                    this.mob.stopUsingItem();
//                    this.mob.performRangedAttack(livingEntity, BowItem.getPowerForTime(i));
//                }
//            }
//            else {
//                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
//            }
        }
    }
}