package net.jeake.wright.mixin;

import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    
    /**
     * Hide player entities when they're riding an airplane
     * The player model will be rendered as part of the airplane instead in third person
     */
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", 
            at = @At("HEAD"), cancellable = true)
    private void hidePlayerInAirplane(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        // Cancel rendering for players riding airplanes
        if (livingEntity instanceof PlayerEntity && livingEntity.getVehicle() instanceof AbstractAirplaneEntity) {
            ci.cancel();
        }
    }
}
