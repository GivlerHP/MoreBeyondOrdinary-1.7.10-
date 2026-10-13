package ru.givler.mbo.config;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;

public final class ColoredLightConfig {
  private static File directory;
  public static volatile boolean enabled = true;
  public static int radius = 16, scanBudget = 2048, propagationBudget = 4096, refreshTicks = 2;
  public static int urgentScanBudget = 32768, urgentPropagationBudget = 32768;
  public static float strength = 1F, colorFalloff = 0.7F, brightnessGain = 1F;
  public static boolean dynamicLights = true;
  public static int dynamicLimit = 4, dynamicInterval = 4;
  private ColoredLightConfig() {}

  public static void configure(File value) { directory = value; }
  public static File file() { return new File(directory,"MoreBeyondOrdinary/colored-light.json"); }
  public static void load(File value) {
    configure(value);
    try { if (file().isFile()) validate(read(file())).run(); }
    catch (IOException | RuntimeException e) { System.err.println("[MBO] Keeping previous light settings: "+e); }
  }
  public static void saveUserOptions() {
    if (directory == null) return;
    try {
      JsonObject root = readOrCreate(), light = category(root,"lighting");
      light.addProperty("enabled",enabled); light.addProperty("strength",strength); light.addProperty("brightnessGain",brightnessGain);
      root.add("lighting",light); write(root);
    } catch (IOException | RuntimeException e) { System.err.println("[MBO] Could not save light settings: "+e); }
  }
  public static JsonObject read(File value) throws IOException {
    try (Reader in = new InputStreamReader(new FileInputStream(value),StandardCharsets.UTF_8)) { return new JsonParser().parse(in).getAsJsonObject(); }
  }
  public static JsonObject readOrCreate() throws IOException {
    if (file().isFile()) return read(file());
    JsonObject root = createDefaults(net.minecraft.block.Block.blockRegistry.containsKey("Thaumcraft:blockCustomPlant"));
    JsonObject light = new JsonObject(), dynamic = new JsonObject();
    light.addProperty("enabled",true); light.addProperty("radius",16); light.addProperty("scanBudget",2048);
    light.addProperty("propagationBudget",4096); light.addProperty("refreshTicks",2);
    light.addProperty("urgentScanBudget",32768); light.addProperty("urgentPropagationBudget",32768);
    light.addProperty("strength",1F); light.addProperty("colorFalloff",0.7F); light.addProperty("brightnessGain",1F);
    dynamic.addProperty("enabled",true); dynamic.addProperty("maxSources",4); dynamic.addProperty("updateTicks",4);
    root.add("lighting",light); root.add("dynamic",dynamic); write(root);
    return root;
  }
  public static void write(JsonObject root) throws IOException {
    Files.createDirectories(file().toPath().getParent());
    Path temp = Files.createTempFile(file().toPath().getParent(),"colored-light-",".tmp");
    try {
      try (Writer out = new OutputStreamWriter(Files.newOutputStream(temp),StandardCharsets.UTF_8)) { new GsonBuilder().setPrettyPrinting().create().toJson(root,out); }
      try { Files.move(temp,file().toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
      catch (AtomicMoveNotSupportedException e) { Files.move(temp,file().toPath(),StandardCopyOption.REPLACE_EXISTING); }
    } finally { Files.deleteIfExists(temp); }
  }
  private static JsonObject category(JsonObject root,String name) { return root.has(name) ? root.getAsJsonObject(name) : new JsonObject(); }
  private static double n(JsonObject root,String key,double fallback,double min,double max) {
    double value = root.has(key) ? root.get(key).getAsDouble() : fallback;
    if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException("Invalid "+key);
    return Math.max(min,Math.min(max,value));
  }
  public static Runnable validate(JsonObject root) {
    JsonObject l=category(root,"lighting"), d=category(root,"dynamic");
    final boolean on=l.has("enabled") ? l.get("enabled").getAsBoolean() : true, dyn=d.has("enabled") ? d.get("enabled").getAsBoolean() : true;
    final int r=(int)n(l,"radius",16,8,24), s=(int)n(l,"scanBudget",2048,256,8192), p=(int)n(l,"propagationBudget",4096,256,16384),
      t=(int)n(l,"refreshTicks",2,1,200), us=(int)n(l,"urgentScanBudget",32768,2048,65536), up=(int)n(l,"urgentPropagationBudget",32768,4096,65536),
      lim=(int)n(d,"maxSources",4,0,8), interval=(int)n(d,"updateTicks",4,1,20);
    final float sat=(float)n(l,"strength",1,0,1), fall=(float)n(l,"colorFalloff",0.7,0.25,2), gain=(float)n(l,"brightnessGain",1,0,2);
    return () -> { enabled=on; dynamicLights=dyn; radius=r; scanBudget=s; propagationBudget=p; refreshTicks=t;
      urgentScanBudget=us; urgentPropagationBudget=up; dynamicLimit=lim; dynamicInterval=interval; strength=sat; colorFalloff=fall; brightnessGain=gain; };
  }
  public static JsonObject createDefaults(boolean thaumcraft) {
    JsonObject root = new JsonObject(), blocks = new JsonObject(), items = new JsonObject();
    add(blocks,"minecraft:torch","#FFD6A0"); add(items,"minecraft:torch","#FFD6A0");
    add(blocks,"minecraft:lava","#FF9B45"); add(blocks,"minecraft:flowing_lava","#FF9B45");
    add(items,"minecraft:lava_bucket","#FF9B45",15);
    add(blocks,"minecraft:glowstone","#FFE1A0"); add(items,"minecraft:glowstone","#FFE1A0");
    for (int meta = 5; meta <= 7; meta++) add(blocks,"minecraft:cauldron:"+meta,"#FF9B45",15);
    add(blocks,"mbo:BlockSeaLantern","#8DEAFF"); add(items,"mbo:BlockSeaLantern","#8DEAFF");
    add(blocks,"minecraft:redstone_torch","#FF3030"); add(items,"minecraft:redstone_torch","#FF3030");
    add(blocks,"minecraft:lit_redstone_ore","#FF3030");
    for (int power = 1; power <= 15; power++) add(blocks,"minecraft:redstone_wire:"+power,"#FF2020",Math.max(1,(power*7+14)/15));
    add(blocks,"minecraft:lit_furnace","#FF9348");
    add(blocks,"minecraft:beacon","#BCEEFF"); add(items,"minecraft:beacon","#BCEEFF");
    both(blocks,items,"minecraft:lit_pumpkin","#FFC65C");
    add(blocks,"minecraft:lit_redstone_lamp","#FFD98A");
    add(blocks,"mbo:Campfire","#FFD6A0");
    for (int stage = 0; stage < 4; stage++) {
      add(blocks,"mbo:CopperBulb"+stage,"#FFD28A");
      add(blocks,"mbo:CopperBulb"+(stage+8),"#FFD28A");
    }
    if (thaumcraft) {
      both(blocks,items,"Thaumcraft:blockCustomPlant:2","#70CFFF");
      both(blocks,items,"Thaumcraft:blockCustomPlant:3","#FF9B35");
      both(blocks,items,"Thaumcraft:blockCustomPlant:5","#B060FF");
      both(blocks,items,"Thaumcraft:blockCustomPlant:4","#A9FFE1");
      both(blocks,items,"Thaumcraft:blockMetalDevice:7","#FFF0CF");
      // Thaumcraft Utils.colors, indexed by candle metadata (the dye palette).
      int[] candles = {15790320,15435844,12801229,6719955,14602026,4312372,14188952,4408131,10526880,2651799,8073150,2437522,5320730,3887386,11743532,1973019};
      for (int meta = 0; meta < candles.length; meta++) both(blocks,items,"Thaumcraft:blockCandle:"+meta,String.format(java.util.Locale.ROOT,"#%06X",candles[meta]));
      // BlockCustomOreItem.colors[meta+1]: air, fire, water, earth, order, entropy.
      String[] aspects = {"#FFFF7E","#FF3C01","#0090FF","#00A000","#EECCFF","#555577"};
      for (int meta = 0; meta < aspects.length; meta++) both(blocks,items,"Thaumcraft:blockCrystal:"+meta,aspects[meta]);
      add(blocks,"Thaumcraft:blockMetalDevice:8","#A8FF70");
      add(blocks,"Thaumcraft:blockMetalDevice:13","#FF9ACC");
      // Carried lamps have no TileEntity charges; use the uncharged placed level, not full 15.
      add(items,"Thaumcraft:blockMetalDevice:8","#A8FF70",8);
      add(items,"Thaumcraft:blockMetalDevice:13","#FF9ACC",8);
    }
    JsonObject effects = new JsonObject(), burning = new JsonObject();
    burning.addProperty("enabled",true); burning.addProperty("color","auto"); burning.addProperty("brightness",15);
    effects.add("colored_burning",burning);
    root.add("blocks",blocks); root.add("items",items); root.add("entities",new JsonObject()); root.add("effects",effects);
    return root;
  }
  private static void both(JsonObject blocks,JsonObject items,String id,String color) { add(blocks,id,color); add(items,id,color); }
  private static void add(JsonObject category,String id,String color) { JsonObject rule = new JsonObject(); rule.addProperty("color",color); category.add(id,rule); }
  private static void add(JsonObject category,String id,String color,int brightness) { add(category,id,color); category.getAsJsonObject(id).addProperty("brightness",brightness); }
}
