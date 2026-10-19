package ru.givler.mbo.integration.angelica.client;

import java.lang.reflect.Field;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;

/** Bounded diagnostics at the actual pane CTM dispatch, rather than a separate icon lookup. */
public final class AngelicaPaneCtmDiagnostics {
  private static int samples;
  private static Field icons;
  private AngelicaPaneCtmDiagnostics() {}
  public static boolean selected(boolean result,RenderBlocks renderer,Block block,IIcon original,int x,int y,int z) {
    if(samples>=12)return result;
    Minecraft mc=Minecraft.getMinecraft();
    if(mc.thePlayer==null || Math.abs(mc.thePlayer.posX-x)>16 || Math.abs(mc.thePlayer.posZ-z)>16)return result;
    samples++;
    try {
      Class<?> api=Class.forName("com.prupe.mcpatcher.ctm.GlassPaneRenderer");
      if(icons==null){icons=api.getDeclaredField("icons");icons.setAccessible(true);}
      IIcon[] chosen=(IIcon[])icons.get(null);
      StringBuilder line=new StringBuilder("[MBO] Pane CTM dispatch: block=").append(Block.getIdFromBlock(block))
          .append(':').append(renderer.blockAccess.getBlockMetadata(x,y,z)).append(" at=").append(x).append(',').append(y).append(',').append(z)
          .append(" active=").append(result).append(" original=").append(original==null?"null":original.getIconName());
      for(int side=2;side<=5;side++)line.append(" face").append(side).append('=').append(chosen[side]==null?"null":chosen[side].getIconName());
      System.out.println(line);
    }catch(ReflectiveOperationException error){System.err.println("[MBO] Pane CTM diagnostic unavailable: "+error);}
    return result;
  }
}
