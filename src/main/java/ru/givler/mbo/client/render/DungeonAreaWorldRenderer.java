package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.opengl.*;
import ru.givler.mbo.dungeon.*;
import ru.givler.mbo.movingplatform.PlatformBlock;

public final class DungeonAreaWorldRenderer {
  private final Map<UUID, Cache> cache = new HashMap<UUID, Cache>();

  @SubscribeEvent
  public void unload(WorldEvent.Unload e) {
    if (e.world.isRemote) {
      for (Cache c : cache.values()) GL11.glDeleteLists(c.list, 1);
      cache.clear();
      ClientDungeonAreas.clear();
    }
  }

  @SubscribeEvent
  public void render(RenderWorldLastEvent e) {
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.theWorld == null || mc.renderViewEntity == null) return;
    double
        cx =
            mc.renderViewEntity.lastTickPosX
                + (mc.renderViewEntity.posX - mc.renderViewEntity.lastTickPosX) * e.partialTicks,
        cy =
            mc.renderViewEntity.lastTickPosY
                + (mc.renderViewEntity.posY - mc.renderViewEntity.lastTickPosY) * e.partialTicks,
        cz =
            mc.renderViewEntity.lastTickPosZ
                + (mc.renderViewEntity.posZ - mc.renderViewEntity.lastTickPosZ) * e.partialTicks;
    mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    boolean showTechnical =
        mc.thePlayer != null
            && mc.thePlayer.getCurrentEquippedItem() != null
            && mc.thePlayer.getCurrentEquippedItem().getItem()
                instanceof ru.givler.mbo.item.ItemDungeonEditor
            && ru.givler.mbo.editor.BuilderAccess.canEdit(mc.thePlayer);
    for (DungeonAreaRecord a : ClientDungeonAreas.all()) {
      if (showTechnical)
        renderOutline(
            a,
            cx,
            cy,
            cz,
            a.getType() == DungeonAreaRecord.TRIGGER ? 1F : .75F,
            a.getType() == DungeonAreaRecord.TRIGGER ? .45F : .15F,
            a.getType() == DungeonAreaRecord.TRIGGER ? .05F : 1F);
      if (a.getType() == DungeonAreaRecord.TRIGGER) continue;
      if (!a.shouldRender() || a.containsPoint(cx, cy, cz)) continue;
      Cache c = compiled(a, mc);
      GL11.glPushMatrix();
      GL11.glTranslated(a.getX() - cx, a.getY() - cy, a.getZ() - cz);
      GL11.glDisable(GL11.GL_LIGHTING);
      float alpha = a.fadeAlpha(mc.theWorld, e.partialTicks);
      if (alpha < 1F) {
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendColor(1, 1, 1, alpha);
        GL11.glBlendFunc(0x8003, 0x8004);
        GL11.glDepthMask(false);
      }
      GL11.glCallList(c.list);
      if (alpha < 1F) {
        GL11.glDepthMask(true);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_BLEND);
      }
      GL11.glEnable(GL11.GL_LIGHTING);
      GL11.glPopMatrix();
    }
  }

  private void renderOutline(
      DungeonAreaRecord a, double cx, double cy, double cz, float red, float green, float blue) {
    double x = a.getX() - cx - .002D,
        y = a.getY() - cy - .002D,
        z = a.getZ() - cz - .002D,
        X = x + a.getSizeX() + .004D,
        Y = y + a.getSizeY() + .004D,
        Z = z + a.getSizeZ() + .004D;
    GL11.glPushAttrib(
        GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glLineWidth(2F);
    GL11.glColor4f(red, green, blue, .9F);
    Tessellator t = Tessellator.instance;
    t.startDrawing(GL11.GL_LINES);
    double[][] p = {
      {x, y, z}, {X, y, z}, {X, Y, z}, {x, Y, z}, {x, y, Z}, {X, y, Z}, {X, Y, Z}, {x, Y, Z}
    };
    int[][] edge = {
      {0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}
    };
    for (int[] e : edge) {
      t.addVertex(p[e[0]][0], p[e[0]][1], p[e[0]][2]);
      t.addVertex(p[e[1]][0], p[e[1]][1], p[e[1]][2]);
    }
    t.draw();
    GL11.glPopAttrib();
  }

  private Cache compiled(DungeonAreaRecord a, Minecraft mc) {
    int sig = Arrays.hashCode(a.getLightField());
    for (PlatformBlock b : a.getBlocks()) {
      sig = 31 * sig + net.minecraft.block.Block.getIdFromBlock(b.block);
      sig = 31 * sig + b.meta;
    }
    Cache old = cache.get(a.getId());
    if (old != null && old.sig == sig) return old;
    if (old != null) GL11.glDeleteLists(old.list, 1);
    int list = GL11.glGenLists(1);
    GL11.glNewList(list, GL11.GL_COMPILE);
    RenderBlocks r =
        new RenderBlocks(
            new PlatformBlockAccess(
                a.getBlocks(),
                a.getLightField(),
                a.getSizeX(),
                a.getSizeY(),
                a.getSizeZ(),
                mc.theWorld,
                a.getX(),
                a.getY(),
                a.getZ()));
    r.renderAllFaces = false;
    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    for (PlatformBlock b : a.getBlocks()) r.renderBlockByRenderType(b.block, b.x, b.y, b.z);
    t.draw();
    GL11.glEndList();
    Cache made = new Cache(list, sig);
    cache.put(a.getId(), made);
    return made;
  }

  private static final class Cache {
    final int list, sig;

    Cache(int l, int s) {
      list = l;
      sig = s;
    }
  }
}
