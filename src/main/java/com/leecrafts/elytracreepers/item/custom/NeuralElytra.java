package com.leecrafts.elytracreepers.item.custom;

import com.leecrafts.elytracreepers.attachment.ModAttachments;
import com.leecrafts.elytracreepers.item.ModItems;
import com.leecrafts.elytracreepers.neat.NeuralNetwork;
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

public class NeuralElytra extends ElytraItem {

    private static final int LOWEST_TARGET_POINT = -128;

    public NeuralElytra(Properties properties) {
        super(properties);
    }

    public static boolean isWearing(LivingEntity livingEntity) {
        return livingEntity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.NEURAL_ELYTRA.asItem());
    }

    @Override
    public boolean elytraFlightTick(@NotNull ItemStack stack, @NotNull LivingEntity entity, int flightTicks) {
        super.elytraFlightTick(stack, entity, flightTicks);
        if (isNonPlayerLivingEntity(entity)) {
            Vec3 targetVec = getTargetVec(entity);
            Vec3 distance = targetVec.subtract(entity.position());
            Vec3 distanceNormalized = distance.normalize();
            double pitchFacingTarget = Math.asin(-distanceNormalized.y); // in radians
            double yawFacingTarget = Math.atan2(-distanceNormalized.x, distanceNormalized.z); // in radians

            double[] observations = getObservations(entity, distance, pitchFacingTarget, yawFacingTarget);
            NeuralNetwork neuralNetwork = entity.getData(ModAttachments.NEURAL_NETWORK);
            double[] outputs = neuralNetwork.forward(observations);
            handleOutputs(entity, outputs, pitchFacingTarget, yawFacingTarget);
        }
        return true;
    }

    private static double[] getObservations(LivingEntity entity, Vec3 distance, double pitchFacingTarget, double yawFacingTarget) {
        double horizontalDistance = Math.sqrt(distance.x * distance.x + distance.z * distance.z);
        double verticalDistance = distance.y;

        double pitchDifference = pitchFacingTarget - Math.toRadians(entity.getXRot());
        double yawDifference = normalizeAngle(yawFacingTarget - Math.toRadians(entity.getYRot()));

        // TODO check if server handles movement speed
        double speed = entity.getDeltaMovement().length();

        if (entity.tickCount % 40 == 0) {
            System.out.println("OBSERVATIONS of entity " + entity.getId());
            System.out.println("horizontalDistance: " + horizontalDistance);
            System.out.println("verticalDistance: " + verticalDistance);
            System.out.println("pitchFacingTarget (degrees): " + Math.toDegrees(pitchFacingTarget));
            System.out.println("yawFacingTarget (degrees): " + Math.toDegrees(yawFacingTarget));
            System.out.println("pitchDifference (degrees): " + Math.toDegrees(pitchDifference));
            System.out.println("yawDifference (degrees): " + Math.toDegrees(yawDifference));
            System.out.println("speed: " + speed);
            System.out.println();
        }
        return new double[] {
                horizontalDistance, verticalDistance,
                pitchDifference, yawDifference,
                speed
        };
    }

    private static void handleOutputs(LivingEntity entity, double[] outputs, double pitchFacingTarget, double yawFacingTarget) {
        double xRotOffset = Math.atan2(outputs[0], outputs[1]);
        double yRotOffset = Math.atan2(outputs[2], outputs[3]);
        // TODO decide if you should use gaussian noise
        double xRot = Mth.clamp(Math.toDegrees(pitchFacingTarget + xRotOffset), -90, 90);
        double yRot = Math.toDegrees(normalizeAngle(yawFacingTarget + yRotOffset));
        entity.setXRot((float) xRot);
        entity.setYRot((float) yRot);
    }

    public static boolean isNonPlayerLivingEntity(Entity entity) {
        return entity instanceof LivingEntity livingEntity && !(livingEntity instanceof Player);
    }

    private static Vec3 getTargetVec(LivingEntity entity) {
        Entity target = entity.getData(ModAttachments.TARGET_ENTITY);
        if (target != null) {
//                if (entity.tickCount % 20 == 0) {
//                    System.out.println(target.getX() + " " + target.getY() + " " + target.getZ());
//                }
            return new Vec3(target.getX(), target.getY(), target.getZ());
        }
        else {
            int i = (int) entity.getY();
            while (i >= LOWEST_TARGET_POINT) {
                BlockState blockState = entity.level().getBlockState(entity.blockPosition().atY(i));
                if (!blockState.isAir() && blockState.getFluidState().isEmpty()) {
                    break;
                }
                i--;
            }
            return new Vec3(entity.getX(), i, entity.getZ());
        }
    }

    // Normalize angle to range [-pi, pi]
    private static double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

}
