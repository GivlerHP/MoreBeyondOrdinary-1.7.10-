package ru.givler.mbo.client.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.material.Material;
import ru.givler.mbo.core.WaterloggingRenderHooks;

/** Submit pane water to the same sorted translucent buffer as glass and ordinary water. */
public final class WaterloggedPaneChunkHooks {
  private WaterloggedPaneChunkHooks() { }

  public static boolean hasWater(RenderBlocks renderer, Block block, int x, int y, int z) {
    return block instanceof BlockPane && WaterloggingRenderHooks.isWaterlogged(renderer.blockAccess, x, y, z);
  }

  public static boolean filterAngelicaPass(boolean original,Block block,int pass,IBlockAccess view,int x,int y,int z) {
    return original || pass==1 && hasAngelicaWater(block,view,x,y,z);
  }

  public static boolean deferAngelicaBlock(Block block,IBlockAccess view) {
    // Water geometry consults collision/flow information in World. Keep it on the client thread.
    boolean wet=view instanceof WaterloggingRenderHooks.ClientView
        && ((WaterloggingRenderHooks.ClientView)view).mbo$hasWaterlogging();
    return block instanceof BlockPane || wet && (block.getMaterial()==Material.water
        || ru.givler.mbo.waterlogging.WaterloggedBlockSupport.supportsWaterlogging(block));
  }

  private static boolean hasAngelicaWater(Block block,IBlockAccess view,int x,int y,int z) {
    return block.getMaterial()!=Material.water && WaterloggingRenderHooks.isWaterlogged(view,x,y,z);
  }

  /** All Angelica virtual water shares the pane path and the ordinary translucent terrain buffer. */
  public static boolean renderAngelica(RenderBlocks renderer,Block block,int x,int y,int z,int pass) {
    boolean drawn=block.canRenderInPass(pass) &&
        (ru.givler.mbo.integration.angelica.client.render.AngelicaPaneRendering.useNative(renderer,block,x,y,z)
            ?ru.givler.mbo.integration.angelica.client.render.AngelicaPaneRendering.render(renderer,block,x,y,z)
            :renderer.renderBlockByRenderType(block,x,y,z));
    if(pass==1 && hasAngelicaWater(block,renderer.blockAccess,x,y,z)) {
      World world=renderer.blockAccess instanceof World ? (World)renderer.blockAccess : Minecraft.getMinecraft().theWorld;
      if(world!=null) {
        WaterloggedBlockRenderer.renderPaneWaterInChunk(world,x,y,z);
        drawn=true;
      }
    }
    return drawn;
  }

  public static int renderPass(Block block, RenderBlocks renderer, int x, int y, int z) {
    return hasWater(renderer, block, x, y, z) ? 1 : block.getRenderBlockPass();
  }

  public static boolean canRender(Block block, int pass, RenderBlocks renderer, int x, int y, int z) {
    return block.canRenderInPass(pass) || pass == 1 && hasWater(renderer, block, x, y, z);
  }
  public static boolean filterCanRender(boolean original, Block block, int pass, RenderBlocks renderer, int x, int y, int z) {
    return original || pass == 1 && hasWater(renderer, block, x, y, z);
  }

  public static boolean render(RenderBlocks renderer, Block block, int x, int y, int z, int pass) {
    boolean drawn = block.canRenderInPass(pass) && renderer.renderBlockByRenderType(block, x, y, z);
    if (pass == 1 && hasWater(renderer, block, x, y, z)) {
      World world = renderer.blockAccess instanceof World
          ? (World) renderer.blockAccess : Minecraft.getMinecraft().theWorld;
      if (world != null) {
        WaterloggedBlockRenderer.renderPaneWaterInChunk(world, x, y, z);
        drawn = true;
      }
    }
    return drawn;
  }
}
