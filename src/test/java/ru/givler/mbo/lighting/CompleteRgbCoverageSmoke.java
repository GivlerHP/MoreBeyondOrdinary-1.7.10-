package ru.givler.mbo.lighting;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.config.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
/** Replays the real 1517-block registry/metadata snapshot against the new RGB path. */
public final class CompleteRgbCoverageSmoke {
  static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
  public static class ProbeBlock extends Block {
    int[] levels=new int[16];int tint=0xFFFFFF;
    ProbeBlock(){super(Material.rock);}
    public int getLightValue(IBlockAccess world,int x,int y,int z){return Math.max(0,levels[world.getBlockMetadata(x,y,z)&15]);}
    public int getRenderColor(int meta){return tint;}
  }
  public static void main(String[] args)throws Exception {
    Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)f.get(null);
    // The isolated test has no Forge active mod container: use vanilla registry storage.
    Object initialized=Block.blockRegistry;Field registry=Block.class.getDeclaredField("blockRegistry");
    unsafe.putObject(unsafe.staticFieldBase(registry),unsafe.staticFieldOffset(registry),new net.minecraft.util.RegistryNamespaced());
    Map<String,ProbeBlock> blocks=new LinkedHashMap<>();List<String[]> rows=new ArrayList<>();int id=500;
    for(String line:Files.readAllLines(Paths.get("build/light-audit/live-registry.tsv"),java.nio.charset.StandardCharsets.UTF_8)) {
      if(line.startsWith("id\t"))continue;String[] row=line.split("\t",-1);if(row.length<8)continue;rows.add(row);
      ProbeBlock block=blocks.get(row[0]);if(block==null){block=(ProbeBlock)unsafe.allocateInstance(ProbeBlock.class);block.levels=new int[16];blocks.put(row[0],block);Block.blockRegistry.addObject(id++,row[0],block);}
      block.levels[Integer.parseInt(row[1])]=Integer.parseInt(row[3]);
    }
    Path directory=Files.createTempDirectory("mbo-rgb-coverage-");ColoredLightSources.configure(directory.toFile());
    Path file=ColoredLightSources.file().toPath();Files.createDirectories(file.getParent());
    String custom="{\"blocks\":{\"mbo:froglight\":{\"color\":\"#123456\"}},\"items\":{}}";
    Files.write(file,custom.getBytes(java.nio.charset.StandardCharsets.UTF_8));check(ColoredLightSources.reload(),"reload");
    check(new String(Files.readAllBytes(file),java.nio.charset.StandardCharsets.UTF_8).equals(custom),"user file changed");
    for(int meta=0;meta<16;meta++)check(ColoredLightSources.block(blocks.get("mbo:froglight"),meta).color==0x123456,"wildcard user override lost");
    final Block[] current={null};final int[] metadata={0};final TileEntity[] tile={null};
    IBlockAccess access=(IBlockAccess)Proxy.newProxyInstance(CompleteRgbCoverageSmoke.class.getClassLoader(),new Class[]{IBlockAccess.class},(p,m,a)->{
      String name=m.getName();if(name.equals("getBlock"))return current[0];if(name.equals("getBlockMetadata"))return metadata[0];if(name.equals("getTileEntity"))return tile[0];
      if(m.getReturnType()==boolean.class)return false;if(m.getReturnType()==int.class)return 0;return null;
    });
    int sources=0,dark=0;
    for(String[] row:rows) {
      current[0]=blocks.get(row[0]);metadata[0]=Integer.parseInt(row[1]);int level=Math.max(0,Integer.parseInt(row[3]));
      int rgb=ColoredLightApi.sample(current[0],access,0,64,0),maximum=Math.max(rgb>>16&255,Math.max(rgb>>8&255,rgb&255));
      ColoredLightRule rule=ColoredLightSources.block(current[0],metadata[0]);
      if(rule!=null && rule.brightness>=0)continue;
      check(maximum==Math.min(15,level)*17,"Light level changed or source missing: "+row[0]+":"+metadata[0]+" expected "+level+" got "+maximum);
      if(level>0)sources++;else dark++;
    }
    thaumcraft.api.aspects.Aspect red=new thaumcraft.api.aspects.Aspect("mbo_coverage_red",0xFF0000,(thaumcraft.api.aspects.Aspect[])null);
    current[0]=blocks.get("Thaumcraft:blockAiry");metadata[0]=5;
    thaumcraft.common.tiles.TileNodeEnergized energized=(thaumcraft.common.tiles.TileNodeEnergized)unsafe.allocateInstance(thaumcraft.common.tiles.TileNodeEnergized.class);
    energized.setAspects(new thaumcraft.api.aspects.AspectList().add(red,10));tile[0]=energized;
    int rgb=ColoredLightApi.sample(current[0],access,0,64,0);check((rgb&65535)==0 && (rgb>>16&255)>0,"energized node hue missing");
    current[0]=blocks.get("Thaumcraft:blockJar");metadata[0]=2;
    thaumcraft.common.tiles.TileJarNode jar=(thaumcraft.common.tiles.TileJarNode)unsafe.allocateInstance(thaumcraft.common.tiles.TileJarNode.class);
    jar.setAspects(new thaumcraft.api.aspects.AspectList().add(red,10));tile[0]=jar;
    rgb=ColoredLightApi.sample(current[0],access,0,64,0);check((rgb&65535)==0 && (rgb>>16&255)>0,"jarred node hue missing");
    current[0]=blocks.get("Thaumcraft:blockManaPod");
    thaumcraft.common.tiles.TileManaPod pod=(thaumcraft.common.tiles.TileManaPod)unsafe.allocateInstance(thaumcraft.common.tiles.TileManaPod.class);
    pod.aspect=red;tile[0]=pod;
    for(int growth=0;growth<=7;growth++) {
      metadata[0]=growth;
      check(ColoredLightApi.sample(current[0],access,0,64,0)==growth*17<<16,"bean aspect/growth light "+growth);
    }
    pod.aspect=thaumcraft.api.aspects.Aspect.WATER;metadata[0]=7;
    check(ColoredLightApi.sample(current[0],access,0,64,0)==ColoredLightApi.rgb(ColoredLightApi.emission(pod.aspect.getColor(),7)),"bean aspect change");
    // Native, copied-material brightness and hue are exercised on real wrapper tile types.
    ProbeBlock cover=(ProbeBlock)unsafe.allocateInstance(ProbeBlock.class);cover.levels=new int[16];cover.tint=0xff0000;cover.setLightLevel(1);
    ProbeBlock warded=blocks.get("Thaumcraft:blockWarded");warded.levels[0]=7;current[0]=warded;metadata[0]=0;
    thaumcraft.common.tiles.TileWarded ward=(thaumcraft.common.tiles.TileWarded)unsafe.allocateInstance(thaumcraft.common.tiles.TileWarded.class);ward.block=cover;ward.blockMd=0;ward.light=7;tile[0]=ward;
    rgb=ColoredLightApi.sample(current[0],access,0,64,0);check(rgb==0x770000,"warded material hue/brightness");
    warded.levels[0]=0;check(ColoredLightApi.sample(current[0],access,0,64,0)==0,"warded source extinguishes");
    Class<?> carpenterType=Class.forName("com.carpentersblocks.block.BlockCarpentersBlock");Block carpenter=(Block)unsafe.allocateInstance(carpenterType);
    Block.blockRegistry.addObject(3500,"CarpentersBlocks:blockCarpentersBlock",carpenter);
    ru.givler.mbo.client.lighting.MaterialRgbSources.register();
    Class<?> tileType=Class.forName("com.carpentersblocks.tileentity.TEBase");TileEntity carpenterTile=(TileEntity)unsafe.allocateInstance(tileType);
    Field attributes=tileType.getDeclaredField("cbAttrMap");attributes.setAccessible(true);Map<Byte,Object> attr=new HashMap<>();attributes.set(carpenterTile,attr);
    net.minecraft.item.ItemBlock item=(net.minecraft.item.ItemBlock)unsafe.allocateInstance(net.minecraft.item.ItemBlock.class);
    Field itemBlock=net.minecraft.item.ItemBlock.class.getDeclaredField("field_150939_a");itemBlock.setAccessible(true);itemBlock.set(item,cover);
    Block.blockRegistry.addObject(3501,"rgbtest:cover",cover);
    Object initializedItems=net.minecraft.item.Item.itemRegistry;Field itemRegistry=net.minecraft.item.Item.class.getDeclaredField("itemRegistry");
    unsafe.putObject(unsafe.staticFieldBase(itemRegistry),unsafe.staticFieldOffset(itemRegistry),new net.minecraft.util.RegistryNamespaced());
    net.minecraft.item.Item.itemRegistry.addObject(3501,"rgbtest:cover",item);
    Field delegate=net.minecraft.item.Item.class.getDeclaredField("delegate");delegate.setAccessible(true);
    delegate.set(item,Proxy.newProxyInstance(CompleteRgbCoverageSmoke.class.getClassLoader(),new Class[]{cpw.mods.fml.common.registry.RegistryDelegate.class},(p,m,a)->m.getName().equals("get")?item:m.getName().equals("type")?net.minecraft.item.Item.class:null));
    Class<?> attributeType=Class.forName("com.carpentersblocks.util.Attribute");Object attribute=unsafe.allocateInstance(attributeType);Field contents=attributeType.getDeclaredField("_itemStack");contents.setAccessible(true);contents.set(attribute,new net.minecraft.item.ItemStack(item));attr.put((byte)6,attribute);
    Field cached=tileType.getDeclaredField("lightValue");cached.setAccessible(true);cached.setInt(carpenterTile,7);
    current[0]=carpenter;tile[0]=carpenterTile;
    rgb=ColoredLightApi.sample(current[0],access,0,64,0);check(rgb==0x770000,"Carpenter covered source hue/brightness: "+Integer.toHexString(rgb)+" native="+carpenter.getLightValue(access,0,64,0)+" cover="+cover.getLightValue());
    cached.setInt(carpenterTile,0);check(ColoredLightApi.sample(current[0],access,0,64,0)==0,"Carpenter source extinguishes");
    verifyPacketHook();
    System.out.println("PASS real registry replay: "+blocks.size()+" blocks; "+sources+" positive and "+dark+" dark metadata states; unchanged native brightness, wildcard overrides, jarred and energized aspects");
  }
  static void verifyPacketHook()throws Exception {
    String name="net.minecraft.client.network.NetHandlerPlayClient";byte[] original;
    try(java.io.InputStream in=CompleteRgbCoverageSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
      java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] b=new byte[8192];for(int n;(n=in.read(b))>=0;)out.write(b,0,n);original=out.toByteArray();
    }
    ru.givler.mbo.core.TileRgbUpdateTransformer patch=new ru.givler.mbo.core.TileRgbUpdateTransformer();byte[] changed=patch.transform(name,name,original);
    check(!Arrays.equals(original,changed),"packet hook not installed");check(Arrays.equals(changed,patch.transform(name,name,changed)),"packet hook not idempotent");
    ClassNode node=new ClassNode();new ClassReader(changed).accept(node,0);for(MethodNode m:node.methods)if((m.access&(Opcodes.ACC_ABSTRACT|Opcodes.ACC_NATIVE))==0)new Analyzer(new BasicVerifier()).analyze(node.name,m);
  }
}
