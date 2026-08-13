package ru.givler.mbo.client.render.boat;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.boat.EntityMBOBoat;

public class ModelMBOBoat extends ModelBase {
    private final ModelRenderer[] parts = new ModelRenderer[7];
    private final ModelRenderer noWater;

    public ModelMBOBoat() {
        parts[0] = new ModelRenderer(this, 0, 0).setTextureSize(128, 64);
        parts[1] = new ModelRenderer(this, 0, 19).setTextureSize(128, 64);
        parts[2] = new ModelRenderer(this, 0, 27).setTextureSize(128, 64);
        parts[3] = new ModelRenderer(this, 0, 35).setTextureSize(128, 64);
        parts[4] = new ModelRenderer(this, 0, 43).setTextureSize(128, 64);
        parts[0].addBox(-14, -9, -3, 28, 16, 3); parts[0].setRotationPoint(0, 3, 1);
        parts[1].addBox(-13, -7, -1, 18, 6, 2); parts[1].setRotationPoint(-15, 4, 4);
        parts[2].addBox(-8, -7, -1, 16, 6, 2); parts[2].setRotationPoint(15, 4, 0);
        parts[3].addBox(-14, -7, -1, 28, 6, 2); parts[3].setRotationPoint(0, 4, -9);
        parts[4].addBox(-14, -7, -1, 28, 6, 2); parts[4].setRotationPoint(0, 4, 9);
        parts[0].rotateAngleX = (float)Math.PI / 2;
        parts[1].rotateAngleY = (float)Math.PI * 3 / 2;
        parts[2].rotateAngleY = (float)Math.PI / 2;
        parts[3].rotateAngleY = (float)Math.PI;
        parts[5] = paddle(true);  parts[5].setRotationPoint(3, -5, 9);
        parts[6] = paddle(false); parts[6].setRotationPoint(3, -5, -9);
        parts[6].rotateAngleY = (float)Math.PI;
        noWater = new ModelRenderer(this, 0, 0).setTextureSize(128, 64);
        noWater.addBox(-14, -9, -3, 28, 16, 3);
        noWater.setRotationPoint(0, -3, 1);
        noWater.rotateAngleX = (float)Math.PI / 2;
    }

    private ModelRenderer paddle(boolean left) {
        ModelRenderer paddle = new ModelRenderer(this, 62, left ? 0 : 20).setTextureSize(128, 64);
        paddle.addBox(-1, 0, -5, 2, 2, 18);
        paddle.addBox(left ? -1.001F : 0.001F, -3, 8, 1, 6, 7);
        paddle.rotateAngleZ = 0.19634955F;
        return paddle;
    }

    @Override
    public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
        GL11.glPushMatrix();
        GL11.glRotatef(90, 0, 1, 0);
        for (int i = 0; i < 5; i++) parts[i].render(scale);
        EntityMBOBoat boat = (EntityMBOBoat)entity;
        setPaddleRotation(boat, 0, swing);
        setPaddleRotation(boat, 1, swing);
        parts[5].render(scale);
        parts[6].render(scale);
        GL11.glPopMatrix();
    }

    public void renderNoWater(float scale) {
        GL11.glPushMatrix();
        GL11.glRotatef(90, 0, 1, 0);
        GL11.glColorMask(false, false, false, false);
        noWater.render(scale);
        GL11.glColorMask(true, true, true, true);
        GL11.glPopMatrix();
    }

    private void setPaddleRotation(EntityMBOBoat boat, int side, float partial) {
        float rowing = boat.getRowingTime(side, partial);
        parts[5 + side].rotateAngleX = (float)MathHelper.denormalizeClamp(
                -1.04719758D, -0.26179939D, (MathHelper.sin(-rowing) + 1F) / 2F);
        parts[5 + side].rotateAngleY = (float)MathHelper.denormalizeClamp(
                -Math.PI / 4D, Math.PI / 4D, (MathHelper.sin(-rowing + 1F) + 1F) / 2F);
        if (side == 1) parts[6].rotateAngleY = (float)Math.PI - parts[6].rotateAngleY;
    }
}
