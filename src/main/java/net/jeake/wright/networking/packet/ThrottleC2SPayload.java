package net.jeake.wright.networking.packet;

import net.jeake.wright.Wright;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ThrottleC2SPayload(boolean throttle) implements CustomPayload {
    public static final CustomPayload.Id<ThrottleC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(Wright.MOD_ID, "throttle_packet_payload"));
    public static final PacketCodec<RegistryByteBuf, ThrottleC2SPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, ThrottleC2SPayload::throttle,
            ThrottleC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
