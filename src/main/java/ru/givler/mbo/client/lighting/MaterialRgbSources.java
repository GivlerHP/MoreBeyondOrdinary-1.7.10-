package ru.givler.mbo.client.lighting;
import java.lang.reflect.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.lighting.ColoredLightApi;
/** Optional wrappers inherit a material hue, never an invented light level. */
public final class MaterialRgbSources {
  private MaterialRgbSources() {}
  public static void register() {
    if(Block.blockRegistry.containsKey("Thaumcraft:blockWarded"))try {
      Class<?> type=Class.forName("thaumcraft.common.tiles.TileWarded");final Field material=type.getField("block"),meta=type.getField("blockMd");
      final Block block=(Block)Block.blockRegistry.getObject("Thaumcraft:blockWarded");
      ColoredLightApi.register(block,(world,x,y,z)->{
        int level=block.getLightValue(world,x,y,z);if(level<=0)return 0;
        TileEntity tile=world.getTileEntity(x,y,z);if(!type.isInstance(tile))return 0;
        try {Block original=(Block)material.get(tile);if(original==null || original==block)return ColoredLightApi.emission(0xFFFFFF,level);
          return ColoredLightApi.emission(NativeRgbSources.materialColor(original,meta.getByte(tile)&15),level);
        }catch(IllegalAccessException e){throw new IllegalStateException(e);}
      });
    }catch(ReflectiveOperationException | LinkageError e){System.err.println("[MBO] Warded RGB adapter unavailable: "+e);}
    if(Block.blockRegistry.containsKey("CarpentersBlocks:blockCarpentersBlock"))try {
      final Class<?> type=Class.forName("com.carpentersblocks.tileentity.TEBase");final Method attribute=type.getMethod("getAttribute",byte.class);
      final Method dye=Class.forName("com.carpentersblocks.util.handler.DyeHandler").getMethod("getColor",ItemStack.class);
      for(Object object:Block.blockRegistry) {
        final Block block=(Block)object;if(!block.getClass().getName().startsWith("com.carpentersblocks.block."))continue;
        ColoredLightApi.register(block,(world,x,y,z)->{
          int level=block.getLightValue(world,x,y,z);if(level<=0)return 0;
          TileEntity tile=world.getTileEntity(x,y,z);if(!type.isInstance(tile))return 0;
          try {
            int r=0,g=0,b=0,count=0,strongest=0;
            for(byte a=0;a<=25;a++) {
              if(a>=7 && a<=13)continue;ItemStack stack=(ItemStack)attribute.invoke(tile,a);if(stack==null)continue;
              boolean illuminator=a==21;
              if(!illuminator && !(stack.getItem() instanceof net.minecraft.item.ItemBlock))continue;
              Block material=Block.getBlockFromItem(stack.getItem());if(!illuminator && (material==null || material.getClass().getName().startsWith("com.carpentersblocks.")))continue;
              int strength=illuminator?15:material.getLightValue();
              if(!illuminator && strength<=0)strength=ColoredLightAuto.itemLevel(stack,null);if(strength<=0 || strength<strongest)continue;
              if(strength>strongest){r=g=b=count=0;strongest=strength;}
              int color=illuminator?0xFFE1A0:NativeRgbSources.materialColor(stack);r+=color>>16&255;g+=color>>8&255;b+=color&255;count++;
            }
            int color=count==0?0xFFFFFF:(r/count)<<16|(g/count)<<8|b/count;
            ItemStack tint=(ItemStack)attribute.invoke(tile,(byte)13);
            if(tint!=null) {int rgb=((Number)dye.invoke(null,tint)).intValue();color=((color>>16&255)*(rgb>>16&255)/255)<<16|((color>>8&255)*(rgb>>8&255)/255)<<8|(color&255)*(rgb&255)/255;}
            return ColoredLightApi.emission(color,level);
          }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        });
      }
    }catch(ReflectiveOperationException | LinkageError e){System.err.println("[MBO] Carpenter RGB adapter unavailable: "+e);}
  }
}
