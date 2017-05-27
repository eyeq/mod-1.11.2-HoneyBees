package eyeq.honeybees.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBee extends ModelBase {
    public static ModelRenderer body;
    public static ModelRenderer windsRight;
    public static ModelRenderer windsLeft;

    public ModelBee() {
        body = new ModelRenderer(this, 0, 0);
        body.addBox(-0.5F, -0.5F, -1.0F, 1, 1, 2);
        body.setRotationPoint(0F, 0F, 0F);
        windsRight = new ModelRenderer(this, 0, 0);
        windsRight.addBox(0F, 0F, 0F, 0, 1, 1);
        windsRight.setRotationPoint(-0.5F, -1F, 0F);
        windsLeft = new ModelRenderer(this, 0, 0);
        windsLeft.addBox(0F, 0F, 0F, 0, 1, 1);
        windsLeft.setRotationPoint(0.5F, -1F, 0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        body.render(scale);
        windsRight.render(scale);
        windsLeft.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        float rotate = MathHelper.cos(ageInTicks + 0.015F * (float) Math.PI) * 0.04F;
        windsRight.rotateAngleZ = -0.2F - rotate;
        windsLeft.rotateAngleZ = 0.2F + rotate;
    }
}
