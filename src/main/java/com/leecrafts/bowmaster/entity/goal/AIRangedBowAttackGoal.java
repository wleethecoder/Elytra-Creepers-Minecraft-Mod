package com.leecrafts.bowmaster.entity.goal;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.livingentity.LivingEntityCap;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

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

            float f = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) (0.25 * f);
            this.mob.setSpeed(f1);

            // just some precalculated values
            Vec3 distance = livingEntity.position().subtract(this.mob.position());
            Vec3 distanceNormalized = distance.normalize();
            double pitchFacingTarget = Math.asin(-distanceNormalized.y); // in radians
            double yawFacingTarget = Math.atan2(-distanceNormalized.x, distanceNormalized.z); // in radians

            // observations
            NeuralNetwork network = this.mob.getNetwork();
            double[] observations = getObservations(livingEntity, distance, pitchFacingTarget, yawFacingTarget);

            // actions
            List<double[]> actionProbs = network.feedForward(observations);
            double[] lookActions = actionProbs.get(0);
            double[] rightClickActions = actionProbs.get(1);
            double[] movementActions = actionProbs.get(2);
            double[] strafeActions = actionProbs.get(3);
            double[] jumpActions = actionProbs.get(4);

            // hold off on using epsilon for now
//            RandomSource random = this.mob.getRandom();
//            if (SkeletonBowMasterEntity.TRAINING) {
//                for (int i = 0; i < actionProbs.length; i++) {
//                    if (random.nextDouble() < NeuralNetworkUtil.EPSILON) {
//                        actionProbs[i] = random.nextDouble();
//                    }
//                }
//            }

            int[] actions = new int[actionProbs.size()];
            boolean killerModeEnabled = true; // sounds cool, but it's only for testing
            if (!killerModeEnabled) {
                // handleLookDirection is a continuous action, so it isn't stored in the actions variable
                handleLookDirection(lookActions[0], lookActions[1], pitchFacingTarget, yawFacingTarget);
                actions[1] = handleRightClick(livingEntity, rightClickActions[0], rightClickActions[1]);
                actions[2] = handleMovement(movementActions[0], movementActions[1], movementActions[2]);
            }
            else {
                handleLookDirection(0, 0, pitchFacingTarget, yawFacingTarget);
                spamArrows(livingEntity);
                handleMovement(1, 0, 0);
            }
            actions[3] = handleStrafing(strafeActions[0], strafeActions[1], strafeActions[2]);
            actions[4] = handleJump(jumpActions[0], jumpActions[1]);

            if (SkeletonBowMasterEntity.TRAINING) {
//                double[] logProbabilities = new double[actionProbs.length];
//                for (int i = 0; i < actionProbs.length; i++) {
//                    logProbabilities[i] = Math.log(actionProbs[i]);
//                }

                // update state, action, and reward storage
                this.mob.storeStates(observations);
                this.mob.storeActionProbs(actionProbs);
                this.mob.storeActions(actions);
                this.mob.storeRewards(-0.005);
            }

        }
    }

    public double[] getObservations(LivingEntity target, Vec3 distance, double pitchFacingTarget, double yawFacingTarget) {
        // Distances
        double horizontalDistance = Math.sqrt(distance.x * distance.x + distance.z * distance.z);
        double verticalDistance = distance.y;

        AtomicReference<Vec3> targetVelocity = new AtomicReference<>(Vec3.ZERO);
        target.getCapability(ModCapabilities.LIVING_ENTITY_CAPABILITY).ifPresent(iLivingEntityCap -> {
            LivingEntityCap livingEntityCap = (LivingEntityCap) iLivingEntityCap;
            targetVelocity.set(livingEntityCap.getVelocity());
        });
        double[] target_FB_LR_UD = calculate_FB_LR_UD_ofVelocity(distance, targetVelocity.get());
        double target_fb = target_FB_LR_UD[0];
        double target_lr = target_FB_LR_UD[1];
        double target_ud = target_FB_LR_UD[2];

        // TODO observe agent velocity and opponent's projectile velocity in order to help the agent dodge. (do later)

        // Differences in pitch and yaw
        double pitchDifference = pitchFacingTarget - Math.toRadians(this.mob.getXRot());
        double yawDifference = normalizeAngle(yawFacingTarget - Math.toRadians(this.mob.getYRot()));

        // Health
        double healthPercentage = this.mob.getHealth() / this.mob.getMaxHealth();

        // Bow charge
        double bowCharge = getBowChargeMeter();

        return new double[] {
                horizontalDistance, verticalDistance,
                target_fb, target_lr, target_ud,
                pitchDifference, yawDifference,
                healthPercentage, bowCharge
        };
    }

    // 3 scalar values
    // Calculate v_forwardbackward, the object's forward/backward velocity relative to the agent
    // Calculate v_leftright, the object's left/right velocity relative to the agent
    // Calculate v_updown, the object's up/down velocity relative to the agent
    private static double[] calculate_FB_LR_UD_ofVelocity(Vec3 distance, Vec3 velocity) {
        double v_fb = velocity.dot(distance.normalize());

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 vHorizontal = new Vec3(distance.x, 0, distance.z);
        Vec3 right = vHorizontal.cross(up).normalize();

        double v_lr = velocity.dot(right);
        double v_ud = velocity.dot(up);

        return new double[] {v_fb, v_lr, v_ud};
    }

    // Normalize angle to range [-pi, pi]
    private static double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public int getBowChargeMeter() {
        // FYI bows are fully charged after 20 ticks (1 second)
        return Math.min(this.mob.getTicksUsingItem(), TICKS_PER_SECOND);
    }

    private void handleLookDirection(double xRotOffset, double yRotOffset, double pitchFacingTarget, double yawFacingTarget) {
        System.out.println("xRotOffset: " + xRotOffset + ", yRotOffset: " + yRotOffset);
        this.mob.setXRot((float) Mth.clamp(Math.toDegrees(pitchFacingTarget) + 90 * xRotOffset, -90, 90));
        this.mob.setYRot((float) Math.toDegrees(normalizeAngle(yawFacingTarget + Math.PI * yRotOffset)));
    }

    private int handleRightClick(LivingEntity target, double rightClickProb, double noRightClickProb) {
        System.out.println("rightClickProb: " + rightClickProb + ", noRightClickProb: " + noRightClickProb);
//        boolean press = rightClickProb > noRightClickProb;
        int action = sampleAction(rightClickProb, noRightClickProb);
        boolean press = action == 0;
        if (press) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
        else {
            if (this.mob.isUsingItem()) {
                int i = this.mob.getTicksUsingItem();
                if (i >= 3) {
                    this.mob.performRangedAttack(target, BowItem.getPowerForTime(i));
                }
                this.mob.stopUsingItem();
            }
        }
        return action;
    }

    private int handleMovement(double forwardProb, double backwardProb, double neitherProb) {
        System.out.println("forwardProb: " + forwardProb + ", backwardProb: " + backwardProb + ", neitherProb: " + neitherProb);
        int action = sampleAction(forwardProb, backwardProb, neitherProb);
//        if (forwardProb > backwardProb && forwardProb > neitherProb) {
//            this.mob.forwardImpulse(1.0f);
//        } else if (backwardProb > neitherProb) {
//            this.mob.forwardImpulse(-1.0f);
//        }
        if (action == 0) {
            this.mob.forwardImpulse(1.0f);
        }
        else if (action == 1) {
            this.mob.forwardImpulse(-1.0f);
        }
        return action;
    }

    private int handleStrafing(double leftProb, double rightProb, double neitherProb) {
        System.out.println("leftProb: " + leftProb + ", rightProb: " + rightProb + ", neitherProb: " + neitherProb);
        int action = sampleAction(leftProb, rightProb, neitherProb);
        // I could use MoveControl#strafe, but there are some unwanted hardcoded behaviors
//        if (leftProb > rightProb && leftProb > neitherProb) {
//            this.mob.setXxa(1.0f);
//        } else if (rightProb > neitherProb) {
//            this.mob.setXxa(-1.0f);
//        }
        if (action == 0) {
            this.mob.setXxa(1.0f);
        }
        else if (action == 1) {
            this.mob.setXxa(-1.0f);
        }
        return action;
    }

    private int handleJump(double jumpProb, double noJumpProb) {
        System.out.println("jumpProb: " + jumpProb + ", noJumpProb: " + noJumpProb);
        int action = sampleAction(jumpProb, noJumpProb);
        if (action == 0) {
            this.mob.getJumpControl().jump();
        }
        return action;
    }

    private void spamArrows(LivingEntity target) { // for testing
        if (this.mob.isUsingItem()) {
            int i = this.mob.getTicksUsingItem();
            if (i >= 3) {
                this.mob.stopUsingItem();
//                this.mob.performRangedAttack(target, BowItem.getPowerForTime(i));
                this.mob.performRangedAttack(target, 3);
            }
        }
        else {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
    }

    // draw random sample from distribution
    // e.g. p1 = 0.4, p2 = 0.6, sample = 0.6 -> i = 1
    private static int sampleAction(double... probs) {
        Random random = new Random();
        double sample = random.nextDouble(); // random number from range (0, 1)
        double currentThreshold = 0;
        for (int i = 0; i < probs.length; i++) {
            currentThreshold += probs[i];
            if (sample < currentThreshold) {
                return i;
            }
        }

        // This line shouldn't execute
        return probs.length - 1;
    }

}
