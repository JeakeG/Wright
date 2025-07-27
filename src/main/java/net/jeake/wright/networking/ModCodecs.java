package net.jeake.wright.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.Vec3d;

public class ModCodecs {
    public static PacketCodec<RegistryByteBuf, Vec3d> VEC3D = new PacketCodec<>() {
        @Override
        public Vec3d decode(RegistryByteBuf buffer) {
            double x = buffer.readDouble();
            double y = buffer.readDouble();
            double z = buffer.readDouble();
            return new Vec3d(x, y, z);
        }

        @Override
        public void encode(RegistryByteBuf buffer, Vec3d value) {
            buffer.writeDouble(value.x);
            buffer.writeDouble(value.y);
            buffer.writeDouble(value.z);
        }
    };
}
