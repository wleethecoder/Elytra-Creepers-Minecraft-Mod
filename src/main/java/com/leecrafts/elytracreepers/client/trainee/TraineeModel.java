package com.leecrafts.elytracreepers.client.trainee;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.entity.custom.TraineeEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class TraineeModel extends GeoModel<TraineeEntity> {

    private static final ResourceLocation TRAINEE_MODEL = ResourceLocation.fromNamespaceAndPath(ElytraCreepers.MODID, "geo/trainee.geo.json");
    public static final ResourceLocation TRAINEE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ElytraCreepers.MODID, "textures/entity/trainee.png");

    @Override
    public ResourceLocation getModelResource(TraineeEntity animatable, @Nullable GeoRenderer<TraineeEntity> renderer) {
        return TRAINEE_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TraineeEntity animatable, @Nullable GeoRenderer<TraineeEntity> renderer) {
        return TRAINEE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TraineeEntity animatable) {
        return null;
    }

}
