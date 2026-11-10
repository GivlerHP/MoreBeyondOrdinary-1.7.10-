package ru.givler.mbo.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.boss.BossStatus;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

/** Draw after HUD layout so the boss bar clears health, armour and air rows. */
public final class BottomBossBar extends Gui {
  @SubscribeEvent public void pre(RenderGameOverlayEvent.Pre e) {
    if(ru.givler.mbo.config.HudConfig.bossBarAtBottom && e.type==RenderGameOverlayEvent.ElementType.BOSSHEALTH)e.setCanceled(true);
  }
  @SubscribeEvent public void post(RenderGameOverlayEvent.Post e) {
    if(!ru.givler.mbo.config.HudConfig.bossBarAtBottom || e.type!=RenderGameOverlayEvent.ElementType.ALL
        || BossStatus.bossName==null || BossStatus.statusBarTime<=0)return;
    Minecraft mc=Minecraft.getMinecraft();
    if(mc.gameSettings.hideGUI)return;
    BossStatus.statusBarTime--;
    int w=e.resolution.getScaledWidth(),h=e.resolution.getScaledHeight();
    int x=w/2-91,y=Math.max(18,h-Math.max(43,Math.max(GuiIngameForge.left_height,GuiIngameForge.right_height)-3));
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_CURRENT_BIT);
    try {
      GL11.glColor4f(1,1,1,1);
      mc.getTextureManager().bindTexture(icons);
      drawTexturedModalRect(x,y,0,74,182,5);
      drawTexturedModalRect(x,y,0,74,182,5);
      int fill=Math.max(0,Math.min(183,(int)(BossStatus.healthScale*183)));
      if(fill>0)drawTexturedModalRect(x,y,0,79,fill,5);
      mc.fontRenderer.drawStringWithShadow(BossStatus.bossName,(w-mc.fontRenderer.getStringWidth(BossStatus.bossName))/2,y-10,0xffffff);
    } finally {GL11.glPopAttrib();}
  }
}
