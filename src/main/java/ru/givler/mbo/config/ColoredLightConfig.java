package ru.givler.mbo.config;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;

public final class ColoredLightConfig {
  private static File directory;
  public static volatile boolean enabled = true;
  public static volatile boolean entityLighting = true;
  /** Shared texture-state scope for world entities/TESRs; false restores the isolated compatibility path. */
  public static boolean entityRenderPass = true;
  public static int radius = 16, scanBudget = 2048, propagationBudget = 4096, refreshTicks = 2;
  public static int urgentScanBudget = 32768, urgentPropagationBudget = 32768;
  /** 0=low (existing budgets), 1=medium, 2=high; always bounded per tick. */
  public static int updateSpeed = 0;
  public static int scanWorkBudget(boolean discovery) { return scaledBudget(discovery ? urgentScanBudget : scanBudget); }
  public static int propagationWorkBudget(boolean urgent) { return scaledBudget(urgent ? urgentPropagationBudget : propagationBudget); }
  private static int scaledBudget(int base) { return (int)Math.min(262144L, (long)base << Math.max(0,Math.min(2,updateSpeed))); }
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
      light.addProperty("entityLighting",entityLighting);
      light.addProperty("entityRenderPass",entityRenderPass);
      light.addProperty("updateSpeed",updateSpeed);
      JsonObject dynamic = category(root,"dynamic");dynamic.addProperty("enabled",dynamicLights);
      root.add("lighting",light);root.add("dynamic",dynamic); write(root);
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
    light.addProperty("entityLighting",true);
    light.addProperty("entityRenderPass",true);
    light.addProperty("propagationBudget",4096); light.addProperty("refreshTicks",2);
    light.addProperty("urgentScanBudget",32768); light.addProperty("urgentPropagationBudget",32768);
    light.addProperty("updateSpeed",0);
    light.addProperty("strength",1F); light.addProperty("colorFalloff",0.7F); light.addProperty("brightnessGain",1F);
    dynamic.addProperty("enabled",true); dynamic.addProperty("maxSources",4); dynamic.addProperty("updateTicks",4);
    root.add("lighting",light); root.add("dynamic",dynamic); write(root);
    return root;
  }
  /** New built-ins also apply to existing files; explicit entries always win. */
  public static void applyExtraSources(JsonObject root) {
      JsonObject defaults=new JsonObject(), defaultItems=new JsonObject();extraSources(defaults,defaultItems);
      for(String name:new String[]{"blocks","items"}) {
        JsonObject category=category(root,name), additions=name.equals("blocks")?defaults:defaultItems;
        for(java.util.Map.Entry<String,JsonElement> entry:additions.entrySet()) {
          String id=entry.getKey();
          String registryId=ru.givler.mbo.lighting.ColoredLightRule.parse(id,entry.getValue(),name.equals("blocks")).name;
          boolean present=name.equals("blocks")?net.minecraft.block.Block.blockRegistry.containsKey(registryId):net.minecraft.item.Item.itemRegistry.containsKey(registryId);
          if(present && !category.has(id) && !category.has(registryId))category.add(id,entry.getValue());
        }
        if(category.entrySet().size()>0)root.add(name,category);
      }
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
    final boolean entities=l.has("entityLighting") ? l.get("entityLighting").getAsBoolean() : true;
    final boolean sharedPass=l.has("entityRenderPass") ? l.get("entityRenderPass").getAsBoolean() : true;
    final int r=(int)n(l,"radius",16,8,24), s=(int)n(l,"scanBudget",2048,256,8192), p=(int)n(l,"propagationBudget",4096,256,16384),
      t=(int)n(l,"refreshTicks",2,1,200), us=(int)n(l,"urgentScanBudget",32768,2048,65536), up=(int)n(l,"urgentPropagationBudget",32768,4096,65536),
      lim=(int)n(d,"maxSources",4,0,8), interval=(int)n(d,"updateTicks",4,1,20), speed=(int)n(l,"updateSpeed",0,0,2);
    final float sat=(float)n(l,"strength",1,0,1), fall=(float)n(l,"colorFalloff",0.7,0.25,2), gain=(float)n(l,"brightnessGain",1,0,2);
    return () -> { enabled=on; entityLighting=entities; entityRenderPass=sharedPass; dynamicLights=dyn; radius=r; scanBudget=s; propagationBudget=p; refreshTicks=t;
      urgentScanBudget=us; urgentPropagationBudget=up; updateSpeed=speed; dynamicLimit=lim; dynamicInterval=interval; strength=sat; colorFalloff=fall; brightnessGain=gain; };
  }
  public static JsonObject createDefaults(boolean thaumcraft) {
    JsonObject root = new JsonObject(), blocks = new JsonObject(), items = new JsonObject();
    extraSources(blocks,items);
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
  private static void extraSources(JsonObject blocks,JsonObject items) {
    registeredBoth(blocks,items,"minecraft:fire","#FFD6A0");
    registeredBoth(blocks,items,"minecraft:portal","#B06AFF");
    registeredBoth(blocks,items,"minecraft:end_portal","#75D8C8");
    registeredBoth(blocks,items,"minecraft:end_portal_frame","#75D8C8");
    registeredBoth(blocks,items,"minecraft:brewing_stand","#FFD38A");
    registeredBoth(blocks,items,"minecraft:brown_mushroom","#B99C78");
    registeredBoth(blocks,items,"minecraft:dragon_egg","#B685E5");
    registeredBoth(blocks,items,"minecraft:powered_repeater","#FF3030");
    registeredBoth(blocks,items,"minecraft:powered_comparator","#FF3030");
    registeredBoth(blocks,items,"BiomesOPlenty:coral1:15","#C39CFF");
    registeredBoth(blocks,items,"BiomesOPlenty:crystal","#9AEFFF");
    registeredBoth(blocks,items,"BiomesOPlenty:flowers:3","#FFF0A0");
    registeredBoth(blocks,items,"BiomesOPlenty:flowers:11","#A3DFFF");
    registeredBoth(blocks,items,"BiomesOPlenty:flowers2:2","#B2FFD0");
    registeredBoth(blocks,items,"BiomesOPlenty:mushrooms:3","#B6F0FF");
    registeredBoth(blocks,items,"GardenCore:small_fire","#FFD6A0");
    registeredBoth(blocks,items,"GardenStuff:bloomery_furnace","#FFAA62");
    registeredBoth(blocks,items,"GardenStuff:candelabra","#FFD6A0");
    registeredBoth(blocks,items,"mbo:BlockFogWhite","#DFECFF");
    registeredBoth(blocks,items,"mbo:BlockFogGrey","#A8BBCE");
    registeredBoth(blocks,items,"mbo:BooksheelCandle","#FFD6A0");
    registeredBoth(blocks,items,"mbo:BooksheelSkullCandle","#FFD6A0");
    registeredBoth(blocks,items,"mbo:meteor_block","#FF9B45");
    registeredBoth(blocks,items,"mbo:temporary_light","#FFF0CF");
    registeredBoth(blocks,items,"mbo:temporary_spectral","#93E7FF");
    registeredBoth(blocks,items,"mbo:MagicFurnace","#A0D5FF");
    registeredBoth(blocks,items,"darkrootgarden:goblin_light","#A8FF84");
    registeredBoth(blocks,items,"darkrootgarden:arcana_workbench","#A8CAFF");
    registeredBoth(blocks,items,"customnpcs:npcBloodBlock","#C92F3B");
    registeredBoth(blocks,items,"customnpcs:npcCarpentyBench","#FFD6A0");
    registeredBoth(blocks,items,"customnpcs:npcScripted","#FFFFFF");
    registeredBoth(blocks,items,"minefantasy2:oreInferno","#FF9B45");
    registeredBoth(blocks,items,"minefantasy2:oreTear","#B9DFFF");
    registeredBoth(blocks,items,"minefantasy2:firepit","#FFD6A0");
    registeredBoth(blocks,items,"minefantasy2:furnace_heater","#FFAA62");
    registeredBoth(blocks,items,"minefantasy2:furnace_stone","#FFAA62");
    registeredBoth(blocks,items,"minefantasy2:MF_BlastHeaterActive","#FFAA62");
    registeredBoth(blocks,items,"Thaumcraft:blockArcaneFurnace","#FFBA75");
    registeredBoth(blocks,items,"Thaumcraft:blockAlchemyFurnace","#FFBA75");
    registeredBoth(blocks,items,"Thaumcraft:blockCosmeticSolid:2","#B996E5");
    registeredBoth(blocks,items,"Thaumcraft:blockCosmeticSolid:14","#B996E5");
    registeredBoth(blocks,items,"Thaumcraft:blockCrystal","#E5D7FF");
    registeredBoth(blocks,items,"Thaumcraft:blockCustomPlant:1","#D9C8FF");
    registeredBoth(blocks,items,"Thaumcraft:blockEldritch","#A591D2");
    registeredBoth(blocks,items,"Thaumcraft:blockEldritchNothing","#9483B7");
    registeredBoth(blocks,items,"Thaumcraft:blockFluidDeath","#6A4A9C");
    registeredBoth(blocks,items,"Thaumcraft:blockFluidPure","#A4E5FF");
    registeredBoth(blocks,items,"Thaumcraft:blockFluxGas","#AE67B8");
    registeredBoth(blocks,items,"Thaumcraft:blockFluxGoo","#AE67B8");
    registeredBoth(blocks,items,"Thaumcraft:blockHole","#B3A0F0");
    registeredBoth(blocks,items,"Thaumcraft:blockMagicalLeaves","#BEEAFF");
    registeredBoth(blocks,items,"Thaumcraft:blockMagicalLog","#BEEAFF");
    registeredBoth(blocks,items,"Thaumcraft:blockMetalDevice:3","#FFCE8A");
    registeredBoth(blocks,items,"Thaumcraft:blockMetalDevice:14","#A0DBFF");
    registeredBoth(blocks,items,"Thaumcraft:blockPortalEldritch","#B99CFF");
    registeredBoth(blocks,items,"Thaumcraft:blockStoneDevice","#FFC581");
    registeredBoth(blocks,items,"Thaumcraft:blockTaintFibres:2","#C084D8");
    registeredBoth(blocks,items,"Thaumcraft:blockTaintFibres:4","#C084D8");
    registeredBoth(blocks,items,"caveabyss:magma","#FF9B45");
    registeredBoth(blocks,items,"caveabyss:small_amethyst_bud","#C89AFF");
    registeredBoth(blocks,items,"caveabyss:medium_amethyst_bud","#C89AFF");
    registeredBoth(blocks,items,"caveabyss:large_amethyst_bud","#C89AFF");
    registeredBoth(blocks,items,"caveabyss:amethyst_cluster","#C89AFF");
    registeredBoth(blocks,items,"caveabyss:small_aquamarine_bud","#82E0E6");
    registeredBoth(blocks,items,"caveabyss:medium_aquamarine_bud","#82E0E6");
    registeredBoth(blocks,items,"caveabyss:large_aquamarine_bud","#82E0E6");
    registeredBoth(blocks,items,"caveabyss:aquamarine_cluster","#82E0E6");
    registeredBoth(blocks,items,"minefantasy2:MF_CrucibleautoActive","#FFAA62");
    registeredBoth(blocks,items,"minefantasy2:MF_CruciblefireclayActive","#FFAA62");
    registeredBoth(blocks,items,"minefantasy2:MF_CruciblemasterActive","#FFAA62");
    registeredBoth(blocks,items,"minefantasy2:MF_CruciblemythicActive","#FFAA62");
    registeredBoth(blocks,items,"minefantasy2:MF_CruciblestoneActive","#FFAA62");
    registeredBoth(blocks,items,"mbo:froglight:0","#FFE49C");
    registeredBoth(blocks,items,"mbo:froglight:1","#FFBFF4");
    registeredBoth(blocks,items,"mbo:froglight:2","#B6FFB0");
    registeredBoth(blocks,items,"mbo:froglight:3","#FFE49C");
    registeredBoth(blocks,items,"mbo:froglight:4","#FFE49C");
    registeredBoth(blocks,items,"mbo:froglight:5","#FFBFF4");
    registeredBoth(blocks,items,"mbo:froglight:6","#B6FFB0");
    registeredBoth(blocks,items,"mbo:froglight:7","#FFE49C");
    registeredBoth(blocks,items,"mbo:froglight:8","#FFE49C");
    registeredBoth(blocks,items,"mbo:froglight:9","#FFBFF4");
    registeredBoth(blocks,items,"mbo:froglight:10","#B6FFB0");
    registeredBoth(blocks,items,"mbo:froglight:11","#FFE49C");
    registeredBoth(blocks,items,"mbo:froglight:12","#FFE49C");
    registeredBoth(blocks,items,"mbo:froglight:13","#FFBFF4");
    registeredBoth(blocks,items,"mbo:froglight:14","#B6FFB0");
    registeredBoth(blocks,items,"mbo:froglight:15","#FFE49C");
    registeredBoth(blocks,items,"mbo:Glowshroom:0","#9EFFAF");
    registeredBoth(blocks,items,"mbo:Glowshroom:1","#8EDCFF");
    registeredBoth(blocks,items,"mbo:Glowshroom:2","#D1A0FF");
    // Only metadata 1 is nitor; the same block also contains aura nodes and other effects.
    add(blocks,"Thaumcraft:blockAiry:1","#FFD6A0");
    add(items,"Thaumcraft:blockAiry:1","#FFD6A0",15);
    // Metadata 3 is the invisible light created by the arcane lamp, not nitor/node.
    add(blocks,"Thaumcraft:blockAiry:3","#FFD6A0");
    both(blocks,items,"Thaumcraft:blockMetalDevice:1","#FFD6A0");
    // CustomNPC+ BlockCrystal uses the vanilla metadata MapColor palette.
    for(int meta=0;meta<16;meta++) {
      String color=String.format(java.util.Locale.ROOT,"#%06X",net.minecraft.block.material.MapColor.getMapColorForBlockColored(meta).colorValue);
      String id="customnpcs:npcCrystalBlock:"+meta;
      add(blocks,id,color);add(items,id,color,12);
    }
    for(String id:new String[]{"customnpcs:npcCandle","customnpcs:npcCampfire","customnpcs:npcLamp","GardenStuff:candle","customnpcs:npcShortLamp","customnpcs:npcTallLamp"})both(blocks,items,id,"#FFD6A0");
    both(blocks,items,"minecraft:ender_chest","#80FFD0");
    // Cold forge items remain dark; only the active world blocks emit light.
    for(String id:new String[]{"minefantasy2:MF_Forgemetal","minefantasy2:MF_Forgestone","minefantasy2:MF_ForgemetalActive","minefantasy2:MF_ForgestoneActive"})add(blocks,id,"#FF9348");
  }
  private static void add(JsonObject category,String id,String color) { JsonObject rule = new JsonObject(); rule.addProperty("color",color); category.add(id,rule); }
  private static void add(JsonObject category,String id,String color,int brightness) { add(category,id,color); category.getAsJsonObject(id).addProperty("brightness",brightness); }
  // Optional palette entries only apply to registered blocks and their actual items.
  private static void registeredBoth(JsonObject blocks,JsonObject items,String id,String color) {
    String[] parts=id.split(":");String registryId=parts[0]+":"+parts[1];
    if(!net.minecraft.block.Block.blockRegistry.containsKey(registryId))return;
    JsonObject b=new JsonObject();b.addProperty("color",color);blocks.add(id,b);
    if(net.minecraft.item.Item.itemRegistry.containsKey(registryId)) {
      JsonObject i=new JsonObject();i.addProperty("color",color);items.add(id,i);
    }
  }
}
