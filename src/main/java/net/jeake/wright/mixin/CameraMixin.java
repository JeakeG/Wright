package net.jeake.wright.mixin;

import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setRotation(float yaw, float pitch);

    /**
     * Redirect the first setRotation call inside Camera.update (ordinal=0), which is the one
     * that sets orientation from the entity's yaw/pitch. Replace it with the aircraft's
     * smooth tickDelta-interpolated rotation when the focused entity is riding a plane.
     *
     * The second setRotation call (ordinal=1, the inverseView yaw+180/-pitch flip for
     * front-facing third person) runs afterward using this.yaw / this.pitch, which are now
     * already the aircraft values — so front-facing third person is handled automatically.
     *
     * moveBy() runs after setRotation and uses this.rotation to determine direction, so
     * intercepting here (before moveBy) means third-person camera offset points the right way.
     */
    @Redirect(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0))
    private void redirectSetRotationToAircraft(Camera camera, float yaw, float pitch) {
        Entity focused = camera.getFocusedEntity();
        if (focused != null && focused.getVehicle() instanceof AbstractAirplaneEntity airplane) {
            float tickDelta = camera.getLastTickDelta();
            setRotation(airplane.getVisualYaw(tickDelta), airplane.getVisualPitch(tickDelta));
        } else {
            setRotation(yaw, pitch);
        }
    }
}
