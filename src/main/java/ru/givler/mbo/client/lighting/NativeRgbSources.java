package ru.givler.mbo.client.lighting;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.lighting.*;
import ru.givler.mbo.config.ColoredLightSources;
/** Native brightness remains authoritative; unknown luminous blocks get neutral RGB. */
public final class NativeRgbSources {
  private NativeRgbSources() {}
  public static void register() {
    for(Object object:Block.blockRegistry) {
      if(!(object instanceof Block))continue;final Block block=(Block)object;
      if(block==ru.givler.mbo.registry.BlockRegistry.ColourFire)continue;
      boolean candidate=block.getLightValue()>0;
      try {candidate|=block.getClass().getMethod("getLightValue",IBlockAccess.class,int.class,int.class,int.class).getDeclaringClass()!=Block.class;}
      catch(NoSuchMethodException impossible){throw new IllegalStateException(impossible);}
      if(!candidate)continue;
      ColoredLightApi.registerDefault(block,(world,x,y,z)->ColoredLightApi.emission(nativeColor(block,world.getBlockMetadata(x,y,z)),block.getLightValue(world,x,y,z)));
      Item item=Item.getItemFromBlock(block);
      if(item!=null)ColoredLightApi.registerDefaultItem(item,(stack,holder)->ColoredLightApi.emission(nativeColor(block,stack.getItemDamage()&15),ColoredLightAuto.itemLevel(stack,holder)));
    }
    MaterialRgbSources.register();
  }
  public static int nativeColor(Block block,int meta) {
    int color=block.getRenderColor(meta)&0xFFFFFF;
    return color==0?0xFFFFFF:color;
  }
  public static int materialColor(Block block,int meta) {
    ColoredLightRule rule=ColoredLightSources.block(block,meta);
    return rule!=null?rule.color:nativeColor(block,meta);
  }
  public static int materialColor(ItemStack stack) {
    if(stack==null)return 0xFFFFFF;
    ColoredLightRule rule=ColoredLightSources.item(stack);
    Block block=Block.getBlockFromItem(stack.getItem());
    if(rule==null && block!=null)rule=ColoredLightSources.block(block,stack.getItemDamage()&15);
    return rule!=null?rule.color:block==null?0xFFFFFF:nativeColor(block,stack.getItemDamage()&15);
  }
  public static void updated(net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
    if(net.minecraft.client.Minecraft.getMinecraft().theWorld!=null)
      ColoredLightClient.INSTANCE.changed(packet.func_148856_c(),packet.func_148855_d(),packet.func_148854_e());
  }
}
