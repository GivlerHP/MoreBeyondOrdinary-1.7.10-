package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.model.fauna.ModelMBOLandAnimal;
import ru.givler.mbo.entity.fauna.EntityMBOFox;
import ru.givler.mbo.entity.fauna.EntityMBOPanda;

public final class RenderMBOLandAnimal extends RenderLiving {
  private final String species;
  private final ModelMBOLandAnimal adult, cub;

  public RenderMBOLandAnimal(String species) {
    super(new ModelMBOLandAnimal(species, false), .5F);
    this.species = species;
    adult = (ModelMBOLandAnimal) mainModel;
    cub = new ModelMBOLandAnimal(species, true);
  }

  @Override
  public void doRender(
      EntityLiving entity, double x, double y, double z, float yaw, float partial) {
    mainModel = ((EntityAnimal) entity).isChild() ? cub : adult;
    super.doRender(entity, x, y, z, yaw, partial);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    boolean baby = ((EntityAnimal) entity).isChild();
    String texture = species;
    if (entity instanceof EntityMBOPanda) {
      int variant = ((EntityMBOPanda) entity).variant();
      if (variant != 0) texture = EntityMBOPanda.GENES[variant] + "_panda";
    }
    if (entity instanceof EntityMBOFox) {
      EntityMBOFox fox = (EntityMBOFox) entity;
      texture = (fox.snowy() ? "fox_snow" : "fox") + (fox.state() == 1 ? "_sleep" : "");
    }
    return new ResourceLocation(
        "mbo", "textures/entity/" + species + "/" + texture + (baby ? "_baby" : "") + ".png");
  }

  @Override
  protected void rotateCorpse(EntityLivingBase entity, float age, float yaw, float partial) {
    super.rotateCorpse(entity, age, yaw, partial);
    if (entity instanceof EntityMBOFox && ((EntityMBOFox) entity).state() == 3)
      GL11.glRotatef(
          entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partial,
          1,
          0,
          0);
    if (entity instanceof EntityMBOPanda) {
      EntityMBOPanda panda = (EntityMBOPanda) entity;
      float back = panda.onBack(partial);
      if (back > 0) {
        GL11.glTranslatef(0, .35F * back, 0);
        GL11.glRotatef(180 * back, 1, 0, 0);
      }
      if (panda.activity() == 3) {
        float time = panda.activityTime() + partial;
        GL11.glTranslatef(0, .65F, 0);
        GL11.glRotatef(time / 32F * 360, 1, 0, 0);
        GL11.glTranslatef(0, -.65F, 0);
      }
    }
  }

  @Override
  protected void renderEquippedItems(EntityLivingBase entity, float partial) {
    super.renderEquippedItems(entity, partial);
    if (entity instanceof EntityMBOFox) {
      ItemStack held = entity.getHeldItem();
      if (held == null) return;
      GL11.glPushMatrix();
      ((ModelMBOLandAnimal) mainModel).postRenderHead(.0625F);
      GL11.glTranslatef(0, .2F, -.45F);
      GL11.glRotatef(90, 1, 0, 0);
      GL11.glScalef(.5F, .5F, .5F);
      RenderManager.instance.itemRenderer.renderItem(entity, held, 0);
      GL11.glPopMatrix();
      return;
    }
    if (!(entity instanceof EntityMBOPanda)) return;
    EntityMBOPanda panda = (EntityMBOPanda) entity;
    ItemStack held = panda.getHeldItem();
    if (held == null) return;
    GL11.glPushMatrix();
    GL11.glTranslatef(0, panda.isChild() ? .85F : .55F, panda.isChild() ? -.25F : -.65F);
    GL11.glRotatef(90, 1, 0, 0);
    GL11.glScalef(.7F, .7F, .7F);
    RenderManager.instance.itemRenderer.renderItem(entity, held, 0);
    GL11.glPopMatrix();
  }
}
