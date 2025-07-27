package net.jeake.wright.networking.packet;

import net.jeake.wright.Wright;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record  RollC2SPayload(boolean roll) implements CustomPayload {
    public static final CustomPayload.Id<RollC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(Wright.MOD_ID, "roll_packet_payload"));
    public static final PacketCodec<RegistryByteBuf, RollC2SPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, RollC2SPayload::roll, RollC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}
