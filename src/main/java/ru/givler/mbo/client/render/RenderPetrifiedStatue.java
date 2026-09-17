package ru.givler.mbo.client.render;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.givler.mbo.tileentity.TileEntityPetrifiedStatue;

/** Renders the captured creature's own model with a stone skin. */
public final class RenderPetrifiedStatue extends TileEntitySpecialRenderer {
  private static final ResourceLocation STONE_32 =
      new ResourceLocation("mbo", "textures/entity/magic/stone_statue_32.png");
  private static final ResourceLocation STONE_64 =
      new ResourceLocation("mbo", "textures/entity/magic/stone_statue_64.png");
  private static final Method PRE_RENDER = ReflectionHelper.findMethod(
      RendererLivingEntity.class, null,
      new String[] {"func_77041_b", "preRenderCallback"},
      net.minecraft.entity.EntityLivingBase.class, float.class);

  @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partial) {
    if (!(tile instanceof TileEntityPetrifiedStatue)) return;
    TileEntityPetrifiedStatue statue = (TileEntityPetrifiedStatue) tile;
    if (statue.section() != 1) return;
    EntityLiving creature = statue.creature();
    if (creature == null) return;
    Render renderer = RenderManager.instance.getEntityRenderObject(creature);
    if (!(renderer instanceof RendererLivingEntity)) return;
    if (tile.getBlockType() instanceof ru.givler.mbo.block.magic.BlockPetrifiedStatue
        && ((ru.givler.mbo.block.magic.BlockPetrifiedStatue)tile.getBlockType()).isIce()) {
      RenderManager.instance.renderEntityWithPosYaw(creature,x+.5D,y,z+.5D,creature.rotationYaw,partial);
      return;
    }

    GL11.glPushMatrix();
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    try {
      GL11.glTranslated(x + 0.5D, y, z + 0.5D);
      GL11.glRotatef(180.0F - creature.rotationYaw, 0, 1, 0);
      GL11.glEnable(GL12.GL_RESCALE_NORMAL);
      GL11.glEnable(GL11.GL_ALPHA_TEST);
      GL11.glDisable(GL11.GL_CULL_FACE);
      GL11.glScalef(-1, -1, 1);
      GL11.glTranslatef(0, -1.5F, 0);

      RendererLivingEntity livingRenderer = (RendererLivingEntity) renderer;
      ModelBase model = ObfuscationReflectionHelper.getPrivateValue(
          RendererLivingEntity.class, livingRenderer, "field_77045_g", "mainModel");
      model.isChild = creature.isChild();
      model.isRiding = creature.isRiding();
      invokePreRender(livingRenderer, creature);
      Minecraft.getMinecraft().renderEngine.bindTexture(
          creature.getClass().getSimpleName().matches(".*(Zombie|Villager|Witch|Horse|Golem|Wither).*")
              ? STONE_64 : STONE_32);
      model.render(creature, 0, 0, 0, 0, 0, 0.0625F);
    } catch (ReflectiveOperationException exception) {
      throw new RuntimeException("Failed to render petrified creature "
          + creature.getClass().getName(), exception);
    } finally {
      GL11.glPopAttrib();
      GL11.glPopMatrix();
    }
  }

  private static void invokePreRender(RendererLivingEntity renderer, EntityLiving creature)
      throws ReflectiveOperationException {
    PRE_RENDER.invoke(renderer, creature, 0.0625F);
  }
}
