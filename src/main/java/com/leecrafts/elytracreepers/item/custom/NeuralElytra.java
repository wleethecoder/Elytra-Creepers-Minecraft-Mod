package com.leecrafts.elytracreepers.item.custom;

import com.leecrafts.elytracreepers.attachment.ModAttachments;
import net.minecraft.world.entity.Entity;
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

    @Override
    public boolean elytraFlightTick(@NotNull ItemStack stack, @NotNull LivingEntity entity, int flightTicks) {
        super.elytraFlightTick(stack, entity, flightTicks);
        if (isNonPlayerLivingEntity(entity)) {
            Vec3 targetVec = getTargetVec(entity);
            Vec3 distance = targetVec.subtract(entity.position());
            double horizontalDistance = Math.sqrt(distance.x * distance.x + distance.z * distance.z);
            double verticalDistance = distance.y;
        }
        return true;
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

}
