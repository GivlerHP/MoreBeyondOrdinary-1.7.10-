package ru.givler.mbo.lighting;
import thaumcraft.api.aspects.*;
import ru.givler.mbo.client.lighting.ThaumcraftNodeLight;
import ru.givler.mbo.config.ColoredLightConfig;
import com.google.gson.JsonObject;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.util.*;
public final class ModRgbSourcesSmoke {
  static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
  public static void main(String[] args)throws Exception {
    Aspect red=new Aspect("mbo_rgb_test_red",0xff0000,(Aspect[])null),blue=new Aspect("mbo_rgb_test_blue",0x0000ff,(Aspect[])null);
    check(ThaumcraftNodeLight.color(new AspectList().add(red,3).add(blue,1))==0xBF003F,"3:1 weighted node color");
    check(ThaumcraftNodeLight.color(new AspectList().add(blue,1).add(red,3))==0xBF003F,"aspect order independent");
    check(ThaumcraftNodeLight.color(new AspectList().add(red,10))==0xff0000,"single aspect color");
    check(ThaumcraftNodeLight.color(new AspectList())==0xffffff,"empty node neutral fallback");
    JsonObject defaults=ColoredLightConfig.createDefaults(true),blocks=defaults.getAsJsonObject("blocks"),items=defaults.getAsJsonObject("items");
    check(blocks.getAsJsonObject("Thaumcraft:blockAiry:3").get("color").getAsString().equals("#FFD6A0"),"invisible lamp light");
    for(int meta=0;meta<16;meta++) {
      String id="customnpcs:npcCrystalBlock:"+meta;
      int expected=net.minecraft.block.material.MapColor.getMapColorForBlockColored(meta).colorValue;
      check(Integer.parseInt(blocks.getAsJsonObject(id).get("color").getAsString().substring(1),16)==expected,"crystal matches visual metadata "+meta);
      check(items.getAsJsonObject(id).get("brightness").getAsInt()==12,"crystal carried brightness");
    }
    String target="thaumcraft.api.TileThaumcraft";byte[] original;
    try(java.io.InputStream in=ModRgbSourcesSmoke.class.getClassLoader().getResourceAsStream(target.replace('.','/')+".class")) {
      java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] b=new byte[8192];for(int n;(n=in.read(b))>=0;)out.write(b,0,n);original=out.toByteArray();
    }
    ru.givler.mbo.integration.thaumcraft.core.ThaumcraftNodeLightTransformer patch=new ru.givler.mbo.integration.thaumcraft.core.ThaumcraftNodeLightTransformer();
    byte[] changed=patch.transform(target,target,original);check(!Arrays.equals(original,changed),"node packet invalidation installed");check(Arrays.equals(changed,patch.transform(target,target,changed)),"node packet hook idempotent");
    ClassNode node=new ClassNode();new ClassReader(changed).accept(node,0);for(MethodNode m:node.methods)if((m.access&(Opcodes.ACC_ABSTRACT|Opcodes.ACC_NATIVE))==0)new Analyzer(new BasicVerifier()).analyze(node.name,m);
    System.out.println("PASS weighted node aspects, all 16 crystal visual colors, lamp light, actual Thaumcraft packet bytecode and idempotency");
  }
}
