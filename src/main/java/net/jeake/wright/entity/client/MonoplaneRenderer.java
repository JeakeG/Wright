package net.jeake.wright.entity.client;

import net.jeake.wright.Wright;
import net.jeake.wright.entity.custom.MonoplaneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class MonoplaneRenderer extends EntityRenderer<MonoplaneEntity> {
    private final MonoplaneModel<MonoplaneEntity> model;
    private final PlayerEntityModel<PlayerEntity> playerModel;
    
    public MonoplaneRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8f;
        this.model = new MonoplaneModel<>(ctx.getPart(MonoplaneModel.PLANE));
        // Create a player model for rendering passengers
        this.playerModel = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false);
    }

    @Override
    public void render(MonoplaneEntity planeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(planeEntity, f, g, matrixStack, vertexConsumerProvider, i);
        matrixStack.push();

        // Move model to hitbox center
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        matrixStack.translate(0.0F, -1.5F, 0.0F);

        // Rotate model to match entity rotation (in proper aircraft order: Yaw, Pitch, Roll)
        // Use tickDelta-interpolated visual rotations for sub-tick smoothness at high framerates
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(planeEntity.getVisualYaw(g)));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(planeEntity.getVisualPitch(g)));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-planeEntity.getVisualRoll(g)));

        // Render the airplane model
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(this.getTexture(planeEntity)));
        this.model.setAngles(planeEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);

        // Render player model if there's a passenger and camera is not in first person
        if (planeEntity.hasPassengers() && planeEntity.getFirstPassenger() instanceof PlayerEntity player) {
            // Only render player model in third person view
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.getPerspective() != Perspective.FIRST_PERSON) {
                renderPassengerModel(player, matrixStack, vertexConsumerProvider, i);
            }
        }

        matrixStack.pop();
        super.render(planeEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
    
    private void renderPassengerModel(PlayerEntity player, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        matrixStack.push();
        
        // Position the player model in the cockpit
        // Adjust these values to position the player correctly in your airplane
        matrixStack.translate(0.0F, 0.3F, 0.4F); // Moved back in cockpit, slightly up
        matrixStack.scale(0.9F, 0.9F, 0.9F); // Slightly smaller to fit in cockpit
        
        // Set up the player model for rendering with sitting pose
        this.playerModel.setAngles(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.playerModel.child = false; // Ensure it's rendered as adult
        
        // Apply sitting pose by adjusting limb rotations
        // Bend legs to sitting position (roughly 90 degrees)
        this.playerModel.leftLeg.pitch = (float) Math.toRadians(-90);
        this.playerModel.rightLeg.pitch = (float) Math.toRadians(-90);
        
        // Adjust lower legs to complete the sitting pose
        this.playerModel.leftLeg.yaw = 0.0F;
        this.playerModel.rightLeg.yaw = 0.0F;
        
        // Position arms in a more relaxed, piloting position
        this.playerModel.leftArm.pitch = (float) Math.toRadians(-10); // Slightly forward
        this.playerModel.rightArm.pitch = (float) Math.toRadians(-10); // Slightly forward
        this.playerModel.leftArm.yaw = (float) Math.toRadians(15); // Slightly outward
        this.playerModel.rightArm.yaw = (float) Math.toRadians(-15); // Slightly outward
        
        // Keep torso upright
        this.playerModel.body.pitch = 0.0F;
        this.playerModel.body.yaw = 0.0F;
        this.playerModel.body.roll = 0.0F;
        
        // Keep head looking forward
        this.playerModel.head.pitch = 0.0F;
        this.playerModel.head.yaw = 0.0F;
        this.playerModel.head.roll = 0.0F;
        
        // Get player's skin texture - use default skin helper for now
        Identifier skinTexture = DefaultSkinHelper.getTexture();
        
        // Render the player model
        VertexConsumer playerVertexConsumer = vertexConsumerProvider.getBuffer(this.playerModel.getLayer(skinTexture));
        this.playerModel.render(matrixStack, playerVertexConsumer, light, OverlayTexture.DEFAULT_UV);
        
        matrixStack.pop();
    }

    @Override
    public Identifier getTexture(MonoplaneEntity entity) {
        return Identifier.of(Wright.MOD_ID, "textures/entity/monoplane/monoplane.png");
    }
}
