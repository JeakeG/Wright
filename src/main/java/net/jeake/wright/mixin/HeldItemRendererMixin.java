package net.jeake.wright.mixin;

import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    
    /**
     * Cancel rendering of first-person hands/items when riding an airplane
     */
    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void hideHandsInAirplane(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity.getVehicle() instanceof AbstractAirplaneEntity) {
            ci.cancel();
        }
    }
}
