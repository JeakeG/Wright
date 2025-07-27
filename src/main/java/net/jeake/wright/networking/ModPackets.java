package net.jeake.wright.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeake.wright.networking.packet.PitchC2SPayload;
import net.jeake.wright.networking.packet.PlaneSyncS2CPayload;
import net.jeake.wright.networking.packet.RollC2SPayload;
import net.jeake.wright.networking.packet.ThrottleC2SPayload;

public class ModPackets {
    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(ThrottleC2SPayload.ID, ThrottleC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RollC2SPayload.ID, RollC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PitchC2SPayload.ID, PitchC2SPayload.CODEC);
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(PlaneSyncS2CPayload.ID, PlaneSyncS2CPayload.CODEC);
    }

//    public static void registerServerListener() {
//        ServerPlayNetworking.registerGlobalReceiver(ThrottleC2SPayload.ID, ThrottleC2SPayload::receive);
//    }
}
