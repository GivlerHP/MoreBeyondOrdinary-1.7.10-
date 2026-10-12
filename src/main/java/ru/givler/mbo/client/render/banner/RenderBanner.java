package ru.givler.mbo.client.render.banner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.banner.BannerPattern;
import ru.givler.mbo.tileentity.TileEntityBanner;

public class RenderBanner extends TileEntitySpecialRenderer implements net.minecraft.client.resources.IResourceManagerReloadListener {
  private static final ResourceLocation BASE =
      new ResourceLocation("mbo", "textures/entity/banner_base.png");
  private static final Map<String, ResourceLocation> CACHE =
      new LinkedHashMap<String, ResourceLocation>(64, .75F, true);
  private static final int CACHE_LIMIT = 256;
  private final ModelBanner model = new ModelBanner();

  public RenderBanner() {
    net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    ((net.minecraft.client.resources.IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
        .registerReloadListener(this);
  }

  private static void releaseTexture(ResourceLocation location) {
    net.minecraft.client.renderer.texture.TextureManager manager = Minecraft.getMinecraft().getTextureManager();
    // deleteTexture alone leaves the object in TextureManager on 1.7.10.
    Map objects = cpw.mods.fml.relauncher.ReflectionHelper.getPrivateValue(
        net.minecraft.client.renderer.texture.TextureManager.class, manager, "mapTextureObjects", "field_110585_a");
    Object texture = objects.remove(location);
    if (texture instanceof net.minecraft.client.renderer.texture.AbstractTexture
        && texture != net.minecraft.client.renderer.texture.TextureUtil.missingTexture)
      ((net.minecraft.client.renderer.texture.AbstractTexture) texture).deleteGlTexture();
  }

  static void clearCache() {
    for (ResourceLocation location : CACHE.values()) releaseTexture(location);
    CACHE.clear();
  }

  @Override public void onResourceManagerReload(net.minecraft.client.resources.IResourceManager resources) { clearCache(); }

  @cpw.mods.fml.common.eventhandler.SubscribeEvent
  public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
    if (event.world.isRemote) clearCache();
  }

  private ResourceLocation texture(TileEntityBanner banner) {
    StringBuilder key = new StringBuilder("mbo_banner_").append(banner.getBaseColor());
    List<ResourceLocation> layers = new ArrayList<ResourceLocation>();
    List<Integer> colors = new ArrayList<Integer>();
    layers.add(new ResourceLocation("mbo", "textures/entity/banner/base.png"));
    colors.add(banner.getBaseColor());
    NBTTagList list = banner.getPatterns();
    for (int i = 0; i < list.tagCount(); i++) {
      NBTTagCompound entry = list.getCompoundTagAt(i);
      BannerPattern pattern = BannerPattern.byId(entry.getString("Pattern"));
      if (pattern != null) {
        key.append('_').append(pattern.id).append(entry.getInteger("Color"));
        layers.add(
            new ResourceLocation("mbo", "textures/entity/banner/" + pattern.texture + ".png"));
        colors.add(15 - (entry.getInteger("Color") & 15));
      }
    }
    String cacheKey = key.toString();
    ResourceLocation location = CACHE.get(cacheKey);
    if (location == null) {
      location = new ResourceLocation("mbo_dynamic", cacheKey);
      Minecraft.getMinecraft()
          .getTextureManager()
          .loadTexture(location, new LayeredBannerTexture(BASE, layers, colors));
      CACHE.put(cacheKey, location);
      if (CACHE.size() > CACHE_LIMIT) {
        String oldest = CACHE.keySet().iterator().next();
        releaseTexture(CACHE.remove(oldest));
      }
    }
    return location;
  }

  @Override
  public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float partial) {
    TileEntityBanner banner = (TileEntityBanner) entity;
    int meta = banner.getWorldObj() == null ? 0 : banner.getBlockMetadata();
    GL11.glPushMatrix();
    float scale = .6666667F;
    if (banner.standing) {
      GL11.glTranslatef((float) x + .5F, (float) y + .5F, (float) z + .5F);
      GL11.glRotatef(-(meta * 360 / 16F), 0, 1, 0);
      model.stand.showModel = true;
    } else {
      float rotation = meta == 2 ? 180 : meta == 4 ? 90 : meta == 5 ? -90 : 0;
      GL11.glTranslatef((float) x + .5F, (float) y - .25F * scale, (float) z + .5F);
      GL11.glRotatef(-rotation, 0, 1, 0);
      GL11.glTranslatef(0, -.3125F, -.4375F);
      model.stand.showModel = false;
    }
    long time = banner.getWorldObj() == null ? 0 : banner.getWorldObj().getTotalWorldTime();
    model.slate.rotateAngleX =
        (-.0125F + .01F * MathHelper.cos(((time % 100) + partial) * .02F * (float) Math.PI))
            * (float) Math.PI;
    bindTexture(texture(banner));
    GL11.glPushMatrix();
    GL11.glScalef(scale, -scale, -scale);
    model.renderAll();
    GL11.glPopMatrix();
    GL11.glColor4f(1, 1, 1, 1);
    GL11.glPopMatrix();
  }
}
