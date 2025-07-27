package net.jeake.wright.mixin;

import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void applyAirplaneRollToHand(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        // Check if the camera's focused entity is riding an airplane
        if (camera.getFocusedEntity() != null && camera.getFocusedEntity().getVehicle() instanceof AbstractAirplaneEntity airplane) {
            // Apply roll rotation to the hand rendering for immersion
            float roll = airplane.getRoll();
            if (Math.abs(roll) > 0.1f) { // Only apply if roll is significant
                // Apply roll effect to the projection matrix
                matrix4f.rotateZ((float) Math.toRadians(roll * 0.5f)); // Reduced effect for hand
            }
        }
    }
}
