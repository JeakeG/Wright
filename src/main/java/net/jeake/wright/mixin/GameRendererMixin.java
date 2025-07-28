package net.jeake.wright.mixin;

import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    /**
     * Apply camera roll when riding an airplane by transforming the view
     */
    @Inject(method = "bobView", at = @At("HEAD"))
    private void applyAirplaneRoll(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity cameraEntity = client.getCameraEntity();
        
        if (cameraEntity != null && cameraEntity.getVehicle() instanceof AbstractAirplaneEntity airplane) {
            // Apply roll rotation to the camera view
            float roll = airplane.getVisualRoll();
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        }
    }
}
