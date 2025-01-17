package com.leecrafts.elytracreepers.item.custom;

import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.neat.calculations.Calculator;
import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.leecrafts.elytracreepers.neat.util.NEATUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class NeuralElytra extends ElytraItem {

    private static final int LOWEST_TARGET_POINT = -128;
    public static final double INTERPOLATION_FACTOR = 0.75;

    public NeuralElytra(Properties properties) {
        super(properties);
    }

    public static boolean isWearing(LivingEntity livingEntity) {
        return livingEntity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.NEURAL_ELYTRA.asItem());
    }

    @Override
    public boolean elytraFlightTick(@NotNull ItemStack stack, @NotNull LivingEntity entity, int flightTicks) {
        super.elytraFlightTick(stack, entity, flightTicks);
        if (isNonPlayerLivingEntity(entity) && !entity.level().isClientSide) {
            Entity target = entity.getData(ModAttachments.TARGET_ENTITY);
            Vec3 targetVelocity;
            Vec3 targetVec;
            if (target != null) {
                targetVelocity = target.getData(NEATUtil.TRAINING ?
                        ModAttachments.TARGET_MOVEMENT : ModAttachments.ENTITY_VELOCITY)
                        .multiply(1, 0, 1);
                targetVec = getGroundTargetVec(target, entity.getY())
                        .add(targetVelocity.scale(INTERPOLATION_FACTOR * TICKS_PER_SECOND));
            }
            else {
                targetVelocity = Vec3.ZERO;
                targetVec = getGroundTargetVec(entity, entity.getY());
            }
            Vec3 distance = targetVec.subtract(entity.position());
            Vec3 distanceNormalized = distance.normalize();
            double pitchFacingTarget = Math.asin(-distanceNormalized.y); // in radians
            double yawFacingTarget = Math.atan2(-distanceNormalized.x, distanceNormalized.z); // in radians

            double[] observations = getObservations(entity, distance, pitchFacingTarget, yawFacingTarget, targetVelocity, false);
            double[] outputs = new double[NEATUtil.OUTPUT_SIZE];
            if (NEATUtil.TRAINING) {
                Agent agent = entity.getData(ModAttachments.AGENT);
                if (agent != null) {
                    outputs = agent.calculate(observations);
                }
            }
            else {
                Calculator calculator = entity.getData(ModAttachments.CALCULATOR);
                if (calculator != null) {
                    outputs = calculator.calculate(observations);
                }
            }
            handleOutputs(entity, outputs, pitchFacingTarget, yawFacingTarget);
        }
        return true;
    }

    private static double[] getObservations(LivingEntity entity, Vec3 distance, double pitchFacingTarget, double yawFacingTarget, Vec3 targetVelocity, boolean print) {
        double horizontalDistance = distance.horizontalDistance();
        double verticalDistance = distance.y;

        double pitchDifference = pitchFacingTarget - Math.toRadians(entity.getXRot());
        double yawDifference = normalizeAngle(yawFacingTarget - Math.toRadians(entity.getYRot()));

        double agentSpeed = entity.getDeltaMovement().length();

        double[] target_FB_LR = calculate_FB_LR_ofVelocity(distance, targetVelocity);
        double target_fb = target_FB_LR[0];
        double target_lr = target_FB_LR[1];

        if (print && entity.tickCount % 40 == 0) {
            System.out.println("OBSERVATIONS of entity " + entity.getId());
            System.out.println("horizontalDistance: " + horizontalDistance);
            System.out.println("verticalDistance: " + verticalDistance);
            System.out.println("pitchFacingTarget (degrees): " + Math.toDegrees(pitchFacingTarget));
            System.out.println("yawFacingTarget (degrees): " + Math.toDegrees(yawFacingTarget));
            System.out.println("pitchDifference (degrees): " + Math.toDegrees(pitchDifference));
            System.out.println("yawDifference (degrees): " + Math.toDegrees(yawDifference));
            System.out.println("agentSpeed: " + agentSpeed);
            System.out.println("target_fb: " + target_fb);
            System.out.println("target_lr: " + target_lr);
            System.out.println();
        }
        return new double[] {
                horizontalDistance, verticalDistance,
                pitchDifference, yawDifference,
                agentSpeed,
                target_fb, target_lr
        };
    }

    // 2 scalar values
    // Calculate v_forwardbackward, the object's forward/backward velocity relative to the agent
    // Calculate v_leftright, the object's left/right velocity relative to the agent
    private static double[] calculate_FB_LR_ofVelocity(Vec3 distance, Vec3 velocity) {
        Vec3 distanceHorizontal = new Vec3(-distance.x, 0, -distance.z).normalize();
        Vec3 velocityHorizontal = new Vec3(velocity.x, 0, velocity.z);
        double v_fb = velocityHorizontal.dot(distanceHorizontal);

        // distance vector rotated 90 degrees counterclockwise
        Vec3 perpendicularToDistanceVec = new Vec3(distance.z, 0, -distance.x).normalize();
        double v_lr = velocityHorizontal.dot(perpendicularToDistanceVec);
        return new double[] {v_fb, v_lr};
    }

    private static void handleOutputs(LivingEntity entity, double[] outputs, double pitchFacingTarget, double yawFacingTarget) {
        double xRotOffset = Math.atan2(outputs[0], outputs[1]);
        double yRotOffset = Math.atan2(outputs[2], outputs[3]);
        double xRot = Mth.clamp(Math.toDegrees(pitchFacingTarget + xRotOffset), -90, 90);
        double yRot = Math.toDegrees(normalizeAngle(yawFacingTarget + yRotOffset));
        entity.setXRot((float) xRot);
        entity.setYRot((float) yRot);
    }

    public static boolean isNonPlayerLivingEntity(Entity entity) {
        return entity instanceof LivingEntity livingEntity && !(livingEntity instanceof Player);
    }

    private static Vec3 getGroundTargetVec(Entity entity, double startingY) {
        int i = (int) startingY;
        findHighest:
        while (i >= LOWEST_TARGET_POINT) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos blockPos = entity.blockPosition().offset(-1 + x, 0, -1 + z).atY(i);
                    BlockState blockState = entity.level().getBlockState(blockPos);
                    if (!blockState.isAir() && blockState.getFluidState().isEmpty()) {
                        break findHighest;
                    }
                }
            }
            i--;
        }
        return new Vec3(entity.getX(), i, entity.getZ());
    }

    // Normalize angle to range [-pi, pi]
    private static double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

}
