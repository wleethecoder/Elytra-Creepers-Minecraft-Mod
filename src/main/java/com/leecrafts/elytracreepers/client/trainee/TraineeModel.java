package com.leecrafts.elytracreepers.client.trainee;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.entity.custom.TraineeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TraineeModel extends GeoModel<TraineeEntity> {

    private static final ResourceLocation TRAINEE_MODEL = ResourceLocation.fromNamespaceAndPath(ElytraCreepers.MODID, "geo/trainee.geo.json");
    public static final ResourceLocation TRAINEE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ElytraCreepers.MODID, "textures/entity/trainee.png");

    @Override
    public ResourceLocation getModelResource(TraineeEntity animatable) {
        return TRAINEE_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TraineeEntity animatable) {
        return TRAINEE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TraineeEntity animatable) {
        return null;
    }
}
