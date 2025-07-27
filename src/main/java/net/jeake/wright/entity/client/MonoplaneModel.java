package net.jeake.wright.entity.client;

import net.jeake.wright.Wright;
import net.jeake.wright.entity.custom.MonoplaneEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class MonoplaneModel<T extends MonoplaneEntity> extends SinglePartEntityModel<T> {
    public static final EntityModelLayer PLANE = new EntityModelLayer(Identifier.of(Wright.MOD_ID, "plane"), "main");

    private final ModelPart plane;
    public MonoplaneModel(ModelPart root) {
        this.plane = root.getChild("plane");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData plane = modelPartData.addChild("plane", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData landingGear = plane.addChild("landingGear", ModelPartBuilder.create().uv(6, 18).cuboid(0.0F, -4.0F, 41.0F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(16, 18).cuboid(5.0F, -6.0F, -11.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 14).cuboid(6.0F, -4.0F, -12.0F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -6.0F, -11.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F))
                .uv(12, 10).cuboid(-7.0F, -4.0F, -12.0F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(18, 6).cuboid(-1.0F, -9.0F, 42.0F, 1.0F, 7.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData propeller = plane.addChild("propeller", ModelPartBuilder.create().uv(10, 0).cuboid(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new Dilation(0.0F))
                .uv(58, 0).cuboid(-17.0F, -1.0F, 0.0F, 34.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -11.0F, -17.0F));

        ModelPartData tail = plane.addChild("tail", ModelPartBuilder.create().uv(30, 52).cuboid(-3.0F, -14.0F, 15.0F, 6.0F, 6.0F, 17.0F, new Dilation(0.0F))
                .uv(58, 2).cuboid(-2.0F, -13.0F, 32.0F, 4.0F, 4.0F, 17.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -21.0F, 41.0F, 2.0F, 8.0F, 6.0F, new Dilation(0.0F))
                .uv(83, 10).cuboid(-18.0F, -12.0F, 41.0F, 16.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(83, 2).cuboid(2.0F, -12.0F, 41.0F, 16.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData wings = plane.addChild("wings", ModelPartBuilder.create().uv(0, 40).cuboid(10.0F, -13.0F, -12.0F, 38.0F, 2.0F, 10.0F, new Dilation(0.0F))
                .uv(0, 28).cuboid(-48.0F, -13.0F, -12.0F, 38.0F, 2.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cockpit = plane.addChild("cockpit", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -8.0F, -13.0F, 16.0F, 2.0F, 26.0F, new Dilation(0.0F))
                .uv(56, 52).cuboid(8.0F, -14.0F, -13.0F, 2.0F, 6.0F, 26.0F, new Dilation(0.0F))
                .uv(0, 52).cuboid(-10.0F, -14.0F, -13.0F, 2.0F, 6.0F, 26.0F, new Dilation(0.0F))
                .uv(36, 84).cuboid(-8.0F, -14.0F, 13.0F, 16.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 84).cuboid(-8.0F, -14.0F, -15.0F, 16.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }
    @Override
    public void setAngles(MonoplaneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public ModelPart getPart() {
        return plane;
    }
}
