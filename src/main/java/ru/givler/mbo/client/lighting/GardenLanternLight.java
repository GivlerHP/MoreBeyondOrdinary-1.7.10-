package ru.givler.mbo.client.lighting;

import java.lang.reflect.Method;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.lighting.ColoredLightApi;

/** Optional GardenStuff bridge: the lantern shell is not its light source. */
public final class GardenLanternLight {
  private static Block lantern;
  private static Method tileSource,tileMeta,lookup,removed,level;
  private static Object registry;
  private GardenLanternLight() {}
  public static void register() {
    if(!Block.blockRegistry.containsKey("GardenStuff:lantern"))return;
    try {
      Class<?> tile=Class.forName("com.jaquadro.minecraft.gardenstuff.block.tile.TileEntityLantern");
      tileSource=tile.getMethod("getLightSource");tileMeta=tile.getMethod("getLightSourceMeta");
      Class<?> api=Class.forName("com.jaquadro.minecraft.gardenapi.internal.Api");
      Object instance=api.getField("instance").get(null);if(instance==null)return;
      Object registries=api.getField("registries").get(instance);if(registries==null)return;
      registry=registries.getClass().getField("lanternSources").get(registries);
      if(registry==null)return;
      lookup=registry.getClass().getMethod("getLanternSource",String.class);
      Class<?> source=Class.forName("com.jaquadro.minecraft.gardenapi.api.component.ILanternSource");
      removed=source.getMethod("getRemovedItem",int.class);level=source.getMethod("getLightLevel",int.class);
      lantern=(Block)Block.blockRegistry.getObject("GardenStuff:lantern");
      ColoredLightApi.register(lantern,(world,x,y,z)->{
        TileEntity value=world.getTileEntity(x,y,z);if(!tile.isInstance(value))return 0;
        try {return sourceEmission((String)tileSource.invoke(value),((Number)tileMeta.invoke(value)).intValue(),null);}
        catch(ReflectiveOperationException error){return 0;}
      });
      Item item=Item.getItemFromBlock(lantern);
      if(item!=null)ColoredLightApi.registerItem(item,(stack,holder)->{
        NBTTagCompound tag=stack.getTagCompound();
        return tag==null?0:sourceEmission(tag.getString("src"),tag.getShort("srcMeta"),holder);
      });
    } catch(ReflectiveOperationException | LinkageError error) {
      System.err.println("[MBO] GardenStuff lantern RGB bridge unavailable: "+error);
    }
  }
  private static int sourceEmission(String id,int meta,Entity holder) {
    if(id==null || id.isEmpty())return 0;
    try {
      Object source=lookup.invoke(registry,id);if(source==null)return 0;
      ItemStack contents=(ItemStack)removed.invoke(source,meta);
      if(contents!=null && contents.getItem()==Item.getItemFromBlock(lantern))return 0;
      return contentsEmission(contents,((Number)level.invoke(source,meta)).intValue(),holder);
    } catch(ReflectiveOperationException | LinkageError error){return 0;}
  }
  public static int contentsEmission(ItemStack contents,int light,Entity holder) {
    if(contents==null || light<=0)return 0;
    ru.givler.mbo.lighting.ColoredLightRule rule=ru.givler.mbo.config.ColoredLightSources.item(contents);
    if(rule==null && contents.getItem() instanceof net.minecraft.item.ItemBlock)
      rule=ru.givler.mbo.config.ColoredLightSources.block(Block.getBlockFromItem(contents.getItem()),contents.getItemDamage()&15);
    if(rule!=null)return rule.emission(light);
    int emission=ColoredLightApi.itemEmission(contents,holder);
    return emission!=0?emission:ColoredLightApi.emission(0xFFFFFF,light);
  }
}
