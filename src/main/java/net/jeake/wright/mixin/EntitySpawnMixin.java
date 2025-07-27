package net.jeake.wright.mixin;

import net.jeake.wright.Wright;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class EntitySpawnMixin {
    @Inject(at = @At("TAIL"), method = "onEntitySpawn")
    private void onEntitySpawn(CallbackInfo info) {
        Wright.LOGGER.info("Entity Spawned");
    }
}
