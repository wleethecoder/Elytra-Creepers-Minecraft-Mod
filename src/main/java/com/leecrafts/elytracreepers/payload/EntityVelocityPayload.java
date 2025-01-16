package com.leecrafts.elytracreepers.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class EntityVelocityPayload {

    public record EntityVelocity(int id, Vector3f velocity) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<EntityVelocity> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mymod", "my_data"));

        // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
        // 'name' will be encoded and decoded as a string
        // 'age' will be encoded and decoded as an integer
        // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
        public static final StreamCodec<ByteBuf, EntityVelocity> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                EntityVelocity::id,
                ByteBufCodecs.VECTOR3F,
                EntityVelocity::velocity,
                EntityVelocity::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

}
