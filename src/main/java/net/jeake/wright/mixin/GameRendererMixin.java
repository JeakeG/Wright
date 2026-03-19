package net.jeake.wright.mixin;

import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
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
            // Apply roll — CameraMixin handles yaw/pitch, but Camera has no roll concept.
            // In front-facing third person the camera looks backward, so left/right are
            // swapped — negate roll to keep it visually correct.
            float roll = airplane.getVisualRoll(tickDelta);
            if (client.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
                roll = -roll;
            }
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        }
    }
}
