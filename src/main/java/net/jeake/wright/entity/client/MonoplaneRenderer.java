package net.jeake.wright.entity.client;

import net.jeake.wright.Wright;
import net.jeake.wright.entity.custom.MonoplaneEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class MonoplaneRenderer extends EntityRenderer<MonoplaneEntity> {
    private final MonoplaneModel<MonoplaneEntity> model;
    public MonoplaneRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8f;
        this.model = new MonoplaneModel<>(ctx.getPart(MonoplaneModel.PLANE));
    }

    @Override
    public void render(MonoplaneEntity planeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(planeEntity, f, g, matrixStack, vertexConsumerProvider, i);
        matrixStack.push();

        // Move model to hitbox center
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        matrixStack.translate(0.0F, -1.5F, 0.0F);

        // Rotate model to match entity rotation
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f));
//        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(planeEntity.getPitch()));
//        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(planeEntity.getRoll()));

        // Other stuff
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(this.getTexture(planeEntity)));
        this.model.setAngles(planeEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);

        matrixStack.pop();
        super.render(planeEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(MonoplaneEntity entity) {
        return Identifier.of(Wright.MOD_ID, "textures/entity/monoplane/monoplane.png");
    }
}
