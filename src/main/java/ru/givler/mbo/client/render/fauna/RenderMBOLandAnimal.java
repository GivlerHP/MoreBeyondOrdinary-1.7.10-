package ru.givler.mbo.client.render.fauna;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.givler.mbo.client.model.fauna.ModelMBOLandAnimal;
import ru.givler.mbo.entity.fauna.EntityMBOFox;
import ru.givler.mbo.entity.fauna.EntityMBOPanda;
import ru.givler.mbo.integration.minefantasy2.FaunaItemRendering;

public final class RenderMBOLandAnimal extends RenderLiving {
  private static final ResourceLocation ITEM_GLINT =
      new ResourceLocation("textures/misc/enchanted_item_glint.png");
  private final RenderBlocks mouthItemBlocks = new RenderBlocks();
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
      try {
        ((ModelMBOLandAnimal) mainModel).postRenderHead(.0625F);
        boolean baby = ((EntityMBOFox) entity).isChild();
        GL11.glTranslatef(baby ? 0 : .0625F, baby ? .17F : .22F, baby ? -.42F : -.47F);
        GL11.glRotatef(90, 1, 0, 0);
        float size = baby ? .35F : .5F;
        GL11.glScalef(size, size, size);
        IItemRenderer equipped =
            MinecraftForgeClient.getItemRenderer(held, ItemRenderType.EQUIPPED);
        IItemRenderer dropped = MinecraftForgeClient.getItemRenderer(held, ItemRenderType.ENTITY);
        boolean tool =
            held.getItem() instanceof ItemTool
                || held.getItem() instanceof ItemSword
                || held.getItem() instanceof ItemBow
                || held.getItem().isFull3D();
        Block block = Block.getBlockFromItem(held.getItem());
        boolean cube =
            held.getItem() instanceof ItemBlock
                && block != null
                && RenderBlocks.renderItemIn3d(block.getRenderType());

        float[] spriteGrip = tool ? FaunaItemRendering.spriteGrip(held) : new float[] {.5F, .5F, 0};
        FaunaItemRendering.logMouthLayers(
            held,
            equipped != null
                ? equipped.getClass().getName() + ":mouth-direct"
                : dropped != null ? dropped.getClass().getName() : "direct",
            spriteGrip);
        if (equipped != null) {
          // MF2 mirrors U: align the head to the same side as the unmirrored vanilla sword.
          if (tool) GL11.glRotatef(-spriteGrip[2] - 180, 0, 0, 1);
          float[] grip =
              tool ? FaunaItemRendering.grip(equipped, spriteGrip) : new float[] {.5F, .5F};
          GL11.glTranslatef(-grip[0], -grip[1], .03125F);
          Minecraft.getMinecraft()
              .getTextureManager()
              .bindTexture(
                  Minecraft.getMinecraft()
                      .getTextureManager()
                      .getResourceLocation(held.getItemSpriteNumber()));
          GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
          try {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            if (equipped.shouldUseRenderHelper(
                ItemRenderType.EQUIPPED, held, IItemRenderer.ItemRendererHelper.EQUIPPED_BLOCK))
              GL11.glTranslatef(-.5F, -.5F, -.5F);
            // ForgeHooksClient.renderEquippedItem adds the hand pose; call the renderer directly.
            equipped.renderItem(ItemRenderType.EQUIPPED, held, mouthItemBlocks, entity);
          } finally {
            GL11.glPopAttrib();
          }
          return;
        }
        if (dropped != null) {
          RenderManager.instance.itemRenderer.renderItem(entity, held, 0, ItemRenderType.ENTITY);
          return;
        }
        if (!cube) {
          // Draw in mouth coordinates, without ItemRenderer's hand matrix or mirrored UVs.
          if (tool) GL11.glRotatef(spriteGrip[2], 0, 0, 1);
          GL11.glTranslatef(-spriteGrip[0], -spriteGrip[1], .03125F);
          Minecraft.getMinecraft()
              .getTextureManager()
              .bindTexture(
                  Minecraft.getMinecraft()
                      .getTextureManager()
                      .getResourceLocation(held.getItemSpriteNumber()));
        }
        int passes =
            held.getItem().requiresMultipleRenderPasses()
                ? held.getItem().getRenderPasses(held.getItemDamage())
                : 1;
        for (int pass = 0; pass < passes; pass++) {
          int color = held.getItem().getColorFromItemStack(held, pass);
          GL11.glColor4f(
              (color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1);
          if (cube)
            RenderManager.instance.itemRenderer.renderItem(
                entity, held, pass, ItemRenderType.ENTITY);
          else {
            IIcon icon = FaunaItemRendering.spriteIcon(held, pass);
            if (icon != null)
              ItemRenderer.renderItemIn2D(
                  Tessellator.instance,
                  icon.getMinU(),
                  icon.getMinV(),
                  icon.getMaxU(),
                  icon.getMaxV(),
                  icon.getIconWidth(),
                  icon.getIconHeight(),
                  .0625F);
          }
        }
        if (!cube && held.hasEffect(0)) renderSpriteGlint();
      } finally {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
      }
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

  private void renderSpriteGlint() {
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glDepthFunc(GL11.GL_EQUAL);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
    Minecraft.getMinecraft().getTextureManager().bindTexture(ITEM_GLINT);
    GL11.glColor4f(.38F, .19F, .608F, 1);
    GL11.glMatrixMode(GL11.GL_TEXTURE);
    try {
      for (int pass = 0; pass < 2; pass++) {
        GL11.glPushMatrix();
        try {
          GL11.glScalef(.125F, .125F, .125F);
          long period = pass == 0 ? 3000 : 4873;
          float offset = Minecraft.getSystemTime() % period / (float) period * 8;
          GL11.glTranslatef(pass == 0 ? offset : -offset, 0, 0);
          GL11.glRotatef(pass == 0 ? -50 : 10, 0, 0, 1);
          ItemRenderer.renderItemIn2D(Tessellator.instance, 0, 0, 1, 1, 256, 256, .0625F);
        } finally {
          GL11.glPopMatrix();
        }
      }
    } finally {
      GL11.glMatrixMode(GL11.GL_MODELVIEW);
      GL11.glPopAttrib();
    }
  }
}
