package ru.givler.mbo.lighting;
import thaumcraft.api.aspects.*;
import ru.givler.mbo.integration.thaumcraft.client.ThaumcraftRgbSources;
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
    check(ThaumcraftRgbSources.color(new AspectList().add(red,3).add(blue,1))==0xBF003F,"3:1 weighted node color");
    check(ThaumcraftRgbSources.color(new AspectList().add(blue,1).add(red,3))==0xBF003F,"aspect order independent");
    check(ThaumcraftRgbSources.color(new AspectList().add(red,10))==0xff0000,"single aspect color");
    check(ThaumcraftRgbSources.color(new AspectList())==0xffffff,"empty node neutral fallback");
    JsonObject defaults=ColoredLightConfig.createDefaults(true),blocks=defaults.getAsJsonObject("blocks"),items=defaults.getAsJsonObject("items");
    check(blocks.getAsJsonObject("Thaumcraft:blockAiry:3").get("color").getAsString().equals("#FFD6A0"),"invisible lamp light");
    for(int meta=0;meta<16;meta++) {
      String id="customnpcs:npcCrystalBlock:"+meta;
      int expected=net.minecraft.block.material.MapColor.getMapColorForBlockColored(meta).colorValue;
      check(Integer.parseInt(blocks.getAsJsonObject(id).get("color").getAsString().substring(1),16)==expected,"crystal matches visual metadata "+meta);
      check(items.getAsJsonObject(id).get("brightness").getAsInt()==12,"crystal carried brightness");
    }
    System.out.println("PASS weighted aspects, crystal visual colors and lamp light");
  }
}
