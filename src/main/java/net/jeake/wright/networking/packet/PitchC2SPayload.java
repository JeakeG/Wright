package net.jeake.wright.networking.packet;

import net.jeake.wright.Wright;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PitchC2SPayload(boolean pitch) implements CustomPayload {
    public static final CustomPayload.Id<PitchC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(Wright.MOD_ID, "pitch_packet_payload"));
    public static final PacketCodec<RegistryByteBuf, PitchC2SPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, PitchC2SPayload::pitch, PitchC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
