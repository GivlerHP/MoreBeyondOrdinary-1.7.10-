package ru.givler.mbo.client.render.monster;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.model.monster.ModelMBOVariantSkeleton;

public final class RenderMBOVariantSkeleton extends RenderSkeleton {
  private final ResourceLocation texture, clothing;
  private final ModelBiped outer;

  public RenderMBOVariantSkeleton(String variant) {
    outer = new ModelBiped(variant.equals("bogged") ? .2F : .25F, 0, 64, 32);
    texture = new ResourceLocation("mbo:textures/entity/skeleton/" + variant + ".png");
    clothing =
        variant.equals("parched")
            ? null
            : new ResourceLocation("mbo:textures/entity/skeleton/" + variant + "_overlay.png");
    modelBipedMain = new ModelMBOVariantSkeleton(variant);
    mainModel = modelBipedMain;
  }

  @Override
  protected ResourceLocation getEntityTexture(EntitySkeleton entity) {
    return texture;
  }

  @Override
  protected void renderEquippedItems(EntityLiving entity, float partial) {
    super.renderEquippedItems(entity, partial);
    if (clothing == null || entity.isInvisible()) return;
    bindTexture(clothing);
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT);
    GL11.glEnable(GL11.GL_ALPHA_TEST);
    GL11.glColor4f(1, 1, 1, 1);
    draw(outer.bipedHead, modelBipedMain.bipedHead);
    draw(outer.bipedHeadwear, modelBipedMain.bipedHeadwear);
    draw(outer.bipedBody, modelBipedMain.bipedBody);
    draw(outer.bipedRightArm, modelBipedMain.bipedRightArm);
    draw(outer.bipedLeftArm, modelBipedMain.bipedLeftArm);
    draw(outer.bipedRightLeg, modelBipedMain.bipedRightLeg);
    draw(outer.bipedLeftLeg, modelBipedMain.bipedLeftLeg);
    GL11.glPopAttrib();
  }

  private static void draw(ModelRenderer layer, ModelRenderer original) {
    layer.rotateAngleX = original.rotateAngleX;
    layer.rotateAngleY = original.rotateAngleY;
    layer.rotateAngleZ = original.rotateAngleZ;
    layer.rotationPointX = original.rotationPointX;
    layer.rotationPointY = original.rotationPointY;
    layer.rotationPointZ = original.rotationPointZ;
    layer.render(.0625F);
  }
}
