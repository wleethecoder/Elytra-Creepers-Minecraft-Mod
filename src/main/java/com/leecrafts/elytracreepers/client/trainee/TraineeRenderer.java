package com.leecrafts.elytracreepers.client.trainee;

import com.leecrafts.elytracreepers.entity.custom.TraineeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TraineeRenderer extends GeoEntityRenderer<TraineeEntity> {

    public TraineeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TraineeModel());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TraineeEntity animatable) {
        return TraineeModel.TRAINEE_TEXTURE;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, TraineeEntity animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (animatable.isFallFlying()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(-animatable.getXRot() + 90));
        }
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
