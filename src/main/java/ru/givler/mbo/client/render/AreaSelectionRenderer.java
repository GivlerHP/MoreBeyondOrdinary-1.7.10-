package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.editor.AreaSelection;
import ru.givler.mbo.item.ItemAreaEditor;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

public final class AreaSelectionRenderer {
  @SubscribeEvent
  public void render(RenderWorldLastEvent event) {
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayer p = mc.thePlayer;
    if (p == null) return;
    ItemStack held = p.getCurrentEquippedItem();
    if (held == null || !(held.getItem() instanceof ItemAreaEditor)) return;
    ItemAreaEditor editor = (ItemAreaEditor) held.getItem();
    AreaSelection area = editor.selection(held, p.dimension);
    EntityMovingPlatform platform =
        held.getItem() instanceof ItemPlatformEditor ? linkedPlatform(mc, held) : null;
    if (platform == null
        && held.getItem() instanceof ItemPlatformEditor
        && ItemPlatformEditor.getLinkedPlatform(held) != null) return;
    if (platform == null && area == null) return;
    double px = p.lastTickPosX + (p.posX - p.lastTickPosX) * event.partialTicks,
        py = p.lastTickPosY + (p.posY - p.lastTickPosY) * event.partialTicks,
        pz = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * event.partialTicks,
        sx =
            platform == null
                ? 0
                : platform.prevPosX + (platform.posX - platform.prevPosX) * event.partialTicks,
        sy =
            platform == null
                ? 0
                : platform.prevPosY + (platform.posY - platform.prevPosY) * event.partialTicks,
        sz =
            platform == null
                ? 0
                : platform.prevPosZ + (platform.posZ - platform.prevPosZ) * event.partialTicks;
    double x0 = platform != null ? sx - px : area.minX - px,
        y0 = platform != null ? sy - py : area.minY - py,
        z0 = platform != null ? sz - pz : area.minZ - pz,
        x1 = platform != null ? sx + platform.getSizeX() - px : area.maxX + 1 - px,
        y1 = platform != null ? sy + platform.getSizeY() - py : area.maxY + 1 - py,
        z1 = platform != null ? sz + platform.getSizeZ() - pz : area.maxZ + 1 - pz;
    int color = editor.selectionColor();
    float r = ((color >> 24) & 255) / 255F,
        g = ((color >> 16) & 255) / 255F,
        b = ((color >> 8) & 255) / 255F,
        a = (color & 255) / 255F;
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glColor4f(r, g, b, a);
    GL11.glLineWidth(2F);
    Tessellator t = Tessellator.instance;
    t.startDrawing(GL11.GL_LINES);
    edge(t, x0, y0, z0, x1, y0, z0);
    edge(t, x0, y1, z0, x1, y1, z0);
    edge(t, x0, y0, z1, x1, y0, z1);
    edge(t, x0, y1, z1, x1, y1, z1);
    edge(t, x0, y0, z0, x0, y1, z0);
    edge(t, x1, y0, z0, x1, y1, z0);
    edge(t, x0, y0, z1, x0, y1, z1);
    edge(t, x1, y0, z1, x1, y1, z1);
    edge(t, x0, y0, z0, x0, y0, z1);
    edge(t, x1, y0, z0, x1, y0, z1);
    edge(t, x0, y1, z0, x0, y1, z1);
    edge(t, x1, y1, z0, x1, y1, z1);
    t.draw();
    GL11.glPopAttrib();
  }

  private static EntityMovingPlatform linkedPlatform(Minecraft mc, ItemStack stack) {
    java.util.UUID id = ItemPlatformEditor.getLinkedPlatform(stack);
    if (id != null)
      for (Object value : mc.theWorld.loadedEntityList)
        if (value instanceof EntityMovingPlatform
            && id.equals(((EntityMovingPlatform) value).getPlatformId()))
          return (EntityMovingPlatform) value;
    return null;
  }

  private static void edge(
      Tessellator t, double a, double b, double c, double d, double e, double f) {
    t.addVertex(a, b, c);
    t.addVertex(d, e, f);
  }
}
