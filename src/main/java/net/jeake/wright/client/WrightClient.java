package net.jeake.wright.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.jeake.wright.entity.ModEntities;
import net.jeake.wright.entity.client.MonoplaneModel;
import net.jeake.wright.entity.client.MonoplaneRenderer;
import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.jeake.wright.event.KeyInputHandler;
import net.jeake.wright.networking.ModPackets;
import net.jeake.wright.networking.packet.PlaneSyncS2CPayload;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class WrightClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityModelLayerRegistry.registerModelLayer(MonoplaneModel.PLANE, MonoplaneModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.PLANE, MonoplaneRenderer::new);

        KeyInputHandler.register();

        // Register the airplane HUD
        AirplaneHudRenderer.register();

        ModPackets.registerS2CPackets();
        ClientPlayNetworking.registerGlobalReceiver(PlaneSyncS2CPayload.ID, WrightClient::onPlaneSync);
    }

    private static void onPlaneSync(PlaneSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            Entity entity = context.player().getWorld().getEntityById(payload.entityId());
            if (entity instanceof AbstractAirplaneEntity airplane) {
                airplane.setPitch(payload.pitch());
                airplane.setRoll(payload.roll());
                airplane.setYaw(payload.yaw());
                airplane.setPos(payload.pos().x, payload.pos().y, payload.pos().z);

                airplane.setBoundingBox(
                        new Box(
                                payload.pos().x - airplane.getWidth() / 2,
                                payload.pos().y,
                                payload.pos().z - airplane.getWidth() / 2,
                                payload.pos().x + airplane.getWidth() / 2,
                                payload.pos().y + airplane.getHeight(),
                                payload.pos().z + airplane.getWidth() / 2
                        )
                );
            }
        });
    }
}
