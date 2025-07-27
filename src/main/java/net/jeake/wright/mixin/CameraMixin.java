package net.jeake.wright.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow
    private Entity focusedEntity;

    @Inject(method = "update", at = @At("TAIL"))
    private void updateAirplaneCamera(CallbackInfo ci) {
        // This mixin is currently just a placeholder for future camera enhancements
        // The basic pitch and yaw following is handled in the entity's updatePassengerPosition method
    }
}
