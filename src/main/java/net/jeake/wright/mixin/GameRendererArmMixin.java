package net.jeake.wright.mixin;

import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererArmMixin {
    
    @Shadow @Final
    private MinecraftClient client;
    
    /**
     * Hide first-person arm rendering when in an airplane
     */
    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void hideHandInAirplane(Camera camera, float tickDelta, Matrix4f matrix, CallbackInfo ci) {
        Entity cameraEntity = this.client.getCameraEntity();
        if (cameraEntity != null && cameraEntity.getVehicle() instanceof AbstractAirplaneEntity) {
            ci.cancel();
        }
    }
}
