package ru.givler.mbo.integration.angelica.client.render;

import java.util.function.BooleanSupplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import ru.givler.mbo.client.lighting.ColoredLightRenderHooks;
import ru.givler.mbo.client.texture.ConnectedTextureHooks;

/** GTNHLib's model dispatch otherwise bypasses Angelica's native pane CTM renderer. */
public final class AngelicaPaneRendering {
  private static BooleanSupplier enabled=()->false;
  private AngelicaPaneRendering() {}
  public static void configure(BooleanSupplier setting){enabled=setting;}
  public static boolean useNative(RenderBlocks renderer,Block block,int x,int y,int z) {
    if(!(block instanceof BlockPane) || !enabled.getAsBoolean() || renderer.blockAccess==null || renderer.hasOverrideBlockTexture())return false;
    IIcon base=block.getIcon(2,renderer.blockAccess.getBlockMetadata(x,y,z));
    return base!=null && ConnectedTextureHooks.resolve(base,block,renderer.blockAccess,x,y,z,2)!=base;
  }
  public static boolean render(RenderBlocks renderer,Block block,int x,int y,int z) {
    long scope=ColoredLightRenderHooks.begin(renderer,x,y,z,8);
    try {
      block.setBlockBoundsBasedOnState(renderer.blockAccess,x,y,z);
      renderer.setRenderBoundsFromBlock(block);
      return block instanceof BlockStainedGlassPane
          ?renderer.renderBlockStainedGlassPane(block,x,y,z)
          :renderer.renderBlockPane((BlockPane)block,x,y,z);
    }finally{ColoredLightRenderHooks.end(scope);}
  }
}
