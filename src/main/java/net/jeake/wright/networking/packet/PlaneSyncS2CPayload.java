package net.jeake.wright.networking.packet;

import net.jeake.wright.Wright;
import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.jeake.wright.networking.ModCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record PlaneSyncS2CPayload(int entityId, float pitch, float roll, float yaw, Vec3d pos) implements CustomPayload {
    public static final CustomPayload.Id<PlaneSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(Wright.MOD_ID, "plane_sync_payload"));

    public static final PacketCodec<RegistryByteBuf, PlaneSyncS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            PlaneSyncS2CPayload::entityId,
            PacketCodecs.FLOAT,
            PlaneSyncS2CPayload::pitch,
            PacketCodecs.FLOAT,
            PlaneSyncS2CPayload::roll,
            PacketCodecs.FLOAT,
            PlaneSyncS2CPayload::yaw,
            ModCodecs.VEC3D,
            PlaneSyncS2CPayload::pos,
            PlaneSyncS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static PlaneSyncS2CPayload sync(AbstractAirplaneEntity entity) {
        return new PlaneSyncS2CPayload(entity.getId(), entity.getPitch(), entity.getRoll(), entity.getYaw(), entity.getPos());
    }
}
