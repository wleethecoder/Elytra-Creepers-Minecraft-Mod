package com.leecrafts.elytracreepers.payload;

import com.leecrafts.elytracreepers.attachment.ModAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleDataOnMain(final EntityVelocityPayload.EntityVelocity data, final IPayloadContext context) {
        // Do something with the data, on the main thread
        Entity entity = context.player().level().getEntity(data.id());
        if (entity != null) {
            entity.setData(ModAttachments.ENTITY_VELOCITY, new Vec3(data.velocity()));
        }
    }

}
