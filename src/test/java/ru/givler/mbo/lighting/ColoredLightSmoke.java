package ru.givler.mbo.lighting;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import ru.givler.mbo.client.lighting.ColoredLightClient;
import ru.givler.mbo.client.lighting.ColoredLightRenderHooks;
import ru.givler.mbo.core.ColoredLightTransformer;
import sun.misc.Unsafe;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

/** Propagation, walls, mixing, native colors and real Forge/OptiFine bytecode regression checks. */
public final class ColoredLightSmoke {
  public static void main(String[] args) throws Exception {
    rules(); defaults(); allocationCounters(); sparseSections(); cacheLookupTransitions(); burning(); mf2Bridge(); propagation(); color(); falloffEquivalence(); sectionTransitions(); incrementalSections(); vanillaBytecode(); customGeometry(); clientNightLighting();
    if (Boolean.getBoolean("mbo.lightPixels")) shaderPrograms();
    if (args.length == 3) optifineBytecode(args);
    System.out.println("Colored light smoke passed");
  }

  private static ColoredLightVolume blank(int size) {
    ColoredLightVolume v = new ColoredLightVolume(size, -size / 2, 0, -size / 2);
    for (int i = 0; i < v.cells(); i++) v.setCell(i, 0, 0);
    return v;
  }
  private static volatile Object allocationSink;
  private static void sparseSections() throws Exception {
    ColoredLightSections sections=new ColoredLightSections();
    sections.install(0,0,0,new byte[4096],new int[4096]);settle(sections);
    Field map=ColoredLightSections.class.getDeclaredField("sections");map.setAccessible(true);
    Object section=((Map<?,?>)map.get(sections)).values().iterator().next();
    Field emission=section.getClass().getDeclaredField("emission"),light=section.getClass().getDeclaredField("light");
    emission.setAccessible(true);light.setAccessible(true);
    check(emission.get(section)==null && light.get(section)==null,"Dark section allocated dense RGB arrays");
    Field queue=section.getClass().getDeclaredField("queue");queue.setAccessible(true);
    check(queue.get(section)==null,"Idle section retained scratch queue");
    int[] sources=new int[4096];Arrays.fill(sources,0x35A7DF);
    sections.install(0,0,0,new byte[4096],sources);settle(sections);
    for(int y=0;y<16;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++)equal(0x35A7DF,sections.sample(x,y,z));
    ColoredLightCache cache=new ColoredLightCache();sections.publish(cache,(x,y,z)->{});
    equal(0x35A7DF,cache.sample(15,15,15));
    Arrays.fill(sources,0);sections.install(0,0,0,new byte[4096],sources);settle(sections);
    sections.publish(cache,(x,y,z)->{});equal(0,cache.sample(15,15,15));
    check(emission.get(section)==null && light.get(section)==null,"Extinguished section retained RGB arrays");
    check(queue.get(section)==null,"Extinguished section retained scratch queue");
    sections.update(15,15,15,1,0xDC6721);settle(sections);equal(0xDC6721,sections.sample(15,15,15));
    for(int sx=1;sx<24;sx++)sections.install(sx,0,0,new byte[4096],new int[4096]);
    settle(sections);
    for(Object value:((Map<?,?>)map.get(sections)).values())check(queue.get(value)==null,"Settled section retained work buffers");
    Field pool=ColoredLightSections.class.getDeclaredField("spareQueues");pool.setAccessible(true);
    check(((java.util.ArrayDeque<?>)pool.get(sections)).size()<=8,"Unbounded spare RGB queue pool");
    System.out.println("Sparse RGB sections: dark arrays absent, full 4096-cell queue wraps, extinction frees arrays and reactivation restores exact colour");
  }
  private static void cacheLookupTransitions() throws Exception {
    ColoredLightCache cache=new ColoredLightCache();equal(0,cache.sample(8,8,8));
    int[] data=new int[4096];Arrays.fill(data,0xFF0000);
    ColoredLightCache.Editor editor=cache.edit();editor.put(0,0,0,data,(x,y,z)->{});editor.commit();
    equal(0xFF0000,cache.sample(8,8,8));equal(0xFF0000,cache.sample(9,8,8));equal(0,cache.sample(16,8,8));
    Arrays.fill(data,0x0000FF);editor=cache.edit();editor.put(0,0,0,data,(x,y,z)->{});editor.commit();
    equal(0x0000FF,cache.sample(8,8,8));
    int local=cache.regionSignature(-2,0,-2,18,20,18);
    editor=cache.edit();editor.put(100,0,100,data,(x,y,z)->{});editor.commit();
    equal(local,cache.regionSignature(-2,0,-2,18,20,18));
    Arrays.fill(data,0x00FF00);editor=cache.edit();editor.put(1,0,0,data,(x,y,z)->{});editor.commit();
    check(local!=cache.regionSignature(-2,0,-2,18,20,18),"Platform region ignored adjacent section RGB");
    local=cache.regionSignature(-2,0,-2,18,20,18);
    editor=cache.edit();editor.remove(1,0,0,(x,y,z)->{});editor.commit();
    check(local!=cache.regionSignature(-2,0,-2,18,20,18),"Platform region ignored section removal");
    equal(0x0000FF,cache.sample(8,8,8));
    cache.clear((x,y,z)->{});equal(0,cache.sample(8,8,8));
    Field lookup=ColoredLightCache.class.getDeclaredField("lookup");lookup.setAccessible(true);
    Object value=((ThreadLocal<?>)lookup.get(cache)).get();Field held=value.getClass().getDeclaredField("data");held.setAccessible(true);
    check(held.get(value)==null,"Cleared RGB lookup retained a section array");
    System.out.println("RGB last-section lookup: absent/new/replaced sections, boundaries and full cache clear passed");
  }
  private static void allocationCounters() throws Exception {
    Class<?> meter=ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.class;
    Field frames=ru.givler.mbo.client.render.decormodels.DecorRenderDiagnostics.class.getDeclaredField("frames");frames.setAccessible(true);
    Object diagnostics=ru.givler.mbo.client.render.decormodels.DecorRenderDiagnostics.INSTANCE, previous=frames.get(diagnostics);
    equal(-1,(int)ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.start());
    try {
      frames.set(diagnostics,new long[3]);ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.reset();
      long start=ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.start();
      if(start<0){System.out.println("JVM allocation counters unavailable; diagnostics gracefully disabled");return;}
      allocationSink=new int[4096];
      ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.end(1,start);
      Field bytes=meter.getDeclaredField("bytes"),calls=meter.getDeclaredField("calls");bytes.setAccessible(true);calls.setAccessible(true);
      check(((long[])bytes.get(null))[1]>=4096L*4,"Allocation meter missed escaped heap array");
      equal(1,(int)((long[])calls.get(null))[1]);
      System.out.println("Allocation diagnostics: real thread allocated-byte counter captures heap arrays; idle path disabled");
    } finally {frames.set(diagnostics,previous);allocationSink=null;ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.reset();}
  }
  private static void falloffEquivalence() {
    float previous=ru.givler.mbo.config.ColoredLightConfig.colorFalloff;
    try {
      for(float exponent:new float[]{.25F,.7F,1F,2F}) {
        ru.givler.mbo.config.ColoredLightConfig.colorFalloff=exponent;
        for(int level=0;level<256;level++)for(boolean little:new boolean[]{true,false})for(float strength:new float[]{.25F,.85F,1F}) {
          int light=level<<16 | (level/2)<<8 | level/3, packed=0xA58D6B4C;
          float weight=Math.min(1,strength)*(float)Math.pow(level/255F,exponent);
          int rs=little?0:24, gs=little?8:16, bs=little?16:8;
          int expected=packed;
          if(level!=0)expected=packed&(little?0xFF000000:0xFF)
              | Math.round((packed>>>rs&255)*(1-weight+weight*(light>>>16&255)/level))<<rs
              | Math.round((packed>>>gs&255)*(1-weight+weight*(light>>8&255)/level))<<gs
              | Math.round((packed>>>bs&255)*(1-weight+weight*(light&255)/level))<<bs;
          equal(expected,ColoredLightRenderHooks.tintColor(packed,light,strength,little));
        }
      }
    } finally {ru.givler.mbo.config.ColoredLightConfig.colorFalloff=previous;}
    System.out.println("RGB falloff lookup: exact previous colors across 256 levels, four exponents, saturation and both byte orders");
  }
  private static void customGeometry() throws Exception {
    Field field=Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true); Unsafe unsafe=(Unsafe)field.get(null);
    RenderBlocks renderer=(RenderBlocks)unsafe.allocateInstance(RenderBlocks.class);
    NightWorld world=(NightWorld)unsafe.allocateInstance(NightWorld.class); renderer.blockAccess=world;
    ColoredLightVolume volume=blank(32); volume.setCell(index(volume,0,8,0),0,0xFF0000); finish(volume);
    ColoredLightClient.volume=volume; ColoredLightClient.cacheActive=false;
    long scope=ColoredLightRenderHooks.begin(renderer,0,8,0,7);
    int worldColor=ColoredLightRenderHooks.tintVertex(-1,0,.5,8.5,.5);
    check(worldColor!=-1,"World custom geometry remained white");
    equal(240,ColoredLightRenderHooks.brightnessVertex(0,.5,8.5,.5)&65535);
    ColoredLightRenderHooks.end(scope);
    equal(-1,ColoredLightRenderHooks.tintVertex(-1,0,.5,8.5,.5));
    world.rgbX=.25; world.rgbY=8; world.rgbZ=.25;
    scope=ColoredLightRenderHooks.begin(renderer,0,0,0,7);
    equal(worldColor,ColoredLightRenderHooks.tintVertex(-1,0,.25,.5,.25));
    ColoredLightRenderHooks.end(scope);
    renderer.blockAccess=null;
    scope=ColoredLightRenderHooks.begin(renderer,0,8,0,7);
    equal(-1,ColoredLightRenderHooks.tintVertex(-1,0,.5,8.5,.5)); ColoredLightRenderHooks.end(scope);
    Method signature=ru.givler.mbo.client.render.RenderMovingPlatform.class.getDeclaredMethod("rgbSignature",double.class,double.class,double.class); signature.setAccessible(true);
    int before=(Integer)signature.invoke(null,0D,8D,0D);
    check(before!=(Integer)signature.invoke(null,.25D,8D,0D),"Moving platform ignored fractional RGB position");
    float old=ru.givler.mbo.config.ColoredLightConfig.strength; ru.givler.mbo.config.ColoredLightConfig.strength=.5F;
    check(before!=(Integer)signature.invoke(null,0D,8D,0D),"Platform cached previous RGB settings");
    ru.givler.mbo.config.ColoredLightConfig.strength=old; ColoredLightClient.volume=null;
    Method region=ru.givler.mbo.client.render.RenderMovingPlatform.class.getDeclaredMethod("rgbSignature",double.class,double.class,double.class,int.class,int.class,int.class);region.setAccessible(true);
    int initial=(Integer)region.invoke(null,0D,8D,0D,5,40,5);
    int[] pixels=new int[4096];Arrays.fill(pixels,0xDC6721);
    ColoredLightCache.Editor edit=ColoredLightClient.CACHE.edit();edit.put(100,2,100,pixels,(x,y,z)->{});edit.commit();
    equal(initial,(Integer)region.invoke(null,0D,8D,0D,5,40,5));
    edit=ColoredLightClient.CACHE.edit();edit.put(0,2,0,pixels,(x,y,z)->{});edit.commit();
    check(initial!=(Integer)region.invoke(null,0D,8D,0D,5,40,5),"Tall platform ignored light near its upper blocks");
    ColoredLightClient.CACHE.clear((x,y,z)->{});
    equal(initial,(Integer)region.invoke(null,0D,8D,0D,5,40,5));
    System.out.println("Platform local RGB signature: distant publication ignored, upper blocks refreshed, full clear restored");
    System.out.println("Custom RGB: World geometry, fractional platform coordinates, inventory isolation and cache settings passed");
  }

  private static void rules() {
    com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
    ColoredLightRule auto = ColoredLightRule.parse("Thaumcraft:blockCandle:3", parser.parse("{\"color\":\"#804020\"}"), true);
    equal(3, auto.metadata); equal(-1, auto.brightness);
    equal(ColoredLightApi.emission(0x804020, 7), auto.emission(7));
    ColoredLightRule fixed = ColoredLightRule.parse("minecraft:wool:14", parser.parse("{\"color\":\"FF0000\",\"brightness\":15}"), true);
    equal(ColoredLightApi.emission(0xFF0000, 15), fixed.emission(0));
    equal(0, ColoredLightRule.parse("minecraft:torch", parser.parse("{\"color\":\"#FFFFFF\",\"brightness\":0}"), true).emission(14));
    equal(100, ColoredLightRule.parse("example:item:100", parser.parse("{\"color\":\"0x123456\",\"brightness\":\"auto\"}"), false).metadata);
    for (String invalid : new String[]{"16", "-1", "1.5"}) {
      boolean rejected = false;
      try { ColoredLightRule.parse("minecraft:torch", parser.parse("{\"color\":\"#FFFFFF\",\"brightness\":" + invalid + "}"), true); }
      catch (IllegalArgumentException expected) { rejected = true; }
      check(rejected, "Invalid brightness accepted: " + invalid);
    }
    System.out.println("Source rules: metadata, automatic/explicit brightness, zero and invalid values passed");
  }

  private static void defaults() throws Exception {
    com.google.gson.JsonObject base = ru.givler.mbo.config.ColoredLightConfig.createDefaults(false);
    com.google.gson.JsonObject full = ru.givler.mbo.config.ColoredLightConfig.createDefaults(true);
    for (String category : new String[]{"blocks", "items"}) for (Map.Entry<String, com.google.gson.JsonElement> rule : full.getAsJsonObject(category).entrySet())
      ColoredLightRule.parse(rule.getKey(), rule.getValue(), category.equals("blocks"));
    com.google.gson.JsonObject blocks = full.getAsJsonObject("blocks");
    ColoredLightRule campfire = ColoredLightRule.parse("mbo:Campfire", blocks.get("mbo:Campfire"), true);
    equal(0, campfire.emission(0)); equal(ColoredLightApi.emission(0xFFD6A0,15), campfire.emission(15));
    int[] bulbLevels = {15,12,8,4};
    for (int stage = 0; stage < 4; stage++) for (int wax : new int[]{0,8}) {
      String id = "mbo:CopperBulb"+(stage+wax);
      ColoredLightRule bulb = ColoredLightRule.parse(id,blocks.get(id),true);
      equal(-1,bulb.brightness); equal(0,bulb.emission(0));
      equal(ColoredLightApi.emission(0xFFD28A,bulbLevels[stage]),bulb.emission(bulbLevels[stage]));
    }
    check(!base.getAsJsonObject("blocks").has("Thaumcraft:blockCrystal:0"), "Absent Thaumcraft must not be seeded");
    for (int meta = 0; meta < 6; meta++) check(blocks.has("Thaumcraft:blockCrystal:" + meta), "Missing aspect cluster");
    for (int meta = 5; meta <= 7; meta++) equal(15, blocks.getAsJsonObject("minecraft:cauldron:" + meta).get("brightness").getAsInt());
    check(!blocks.has("minecraft:cauldron:4") && !blocks.has("minecraft:redstone_wire:0") && !blocks.has("minecraft:furnace") && !blocks.has("minecraft:redstone_ore"), "Inactive states must not emit");
    equal(1, blocks.getAsJsonObject("minecraft:redstone_wire:1").get("brightness").getAsInt());
    equal(7, blocks.getAsJsonObject("minecraft:redstone_wire:15").get("brightness").getAsInt());
    Path directory = Files.createTempDirectory("mbo-light-defaults");
    ru.givler.mbo.config.ColoredLightSources.configure(directory.toFile());
    Path config = ru.givler.mbo.config.ColoredLightSources.file().toPath();
    Files.createDirectories(config.getParent());
    byte[] custom = "{\"blocks\":{},\"items\":{},\"effects\":{\"colored_burning\":{\"enabled\":false,\"brightness\":3}}}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    Files.write(config, custom);
    check(ru.givler.mbo.config.ColoredLightSources.reload(), "Custom config failed");
    check(Arrays.equals(custom, Files.readAllBytes(config)), "Existing config was overwritten");
    check(!ru.givler.mbo.config.ColoredLightSources.coloredBurning, "Effect disable ignored");
    equal(3, ru.givler.mbo.config.ColoredLightSources.burningBrightness);
    Files.write(config, "{\"effects\":{\"colored_burning\":{\"brightness\":16}}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    check(!ru.givler.mbo.config.ColoredLightSources.reload(), "Invalid effect accepted");
    equal(3, ru.givler.mbo.config.ColoredLightSources.burningBrightness);
    Files.write(config, "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    check(ru.givler.mbo.config.ColoredLightSources.reload(), "Legacy config failed");
    check(ru.givler.mbo.config.ColoredLightSources.coloredBurning, "Legacy config lost burning support");
    for (int meta = 0; meta < 16; meta++) check(blocks.has("Thaumcraft:blockCandle:" + meta), "Missing candle color");
    check(blocks.has("Thaumcraft:blockMetalDevice:7") && blocks.has("Thaumcraft:blockCustomPlant:4"), "Missing lamp or bloom");
    check(blocks.has("minecraft:lit_pumpkin") && blocks.has("minecraft:lit_redstone_lamp") && !blocks.has("minecraft:redstone_lamp"), "Incorrect decorative lamp states");
    Files.delete(config);
    Path obsolete = config.resolveSibling("colored-light-sources.json");
    byte[] obsoleteContent = "{invalid legacy file".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    Files.write(obsolete, obsoleteContent);
    com.google.gson.JsonObject fresh = ru.givler.mbo.config.ColoredLightConfig.readOrCreate();
    check(fresh.has("lighting") && fresh.has("dynamic") && fresh.has("blocks") && fresh.has("effects"), "Unified config missing sections");
    equal(16, fresh.getAsJsonObject("lighting").get("radius").getAsInt());
    check(Arrays.equals(obsoleteContent, Files.readAllBytes(obsolete)), "Legacy file was migrated or modified");
    fresh.getAsJsonObject("blocks").add("example:custom", new com.google.gson.JsonParser().parse("{\"color\":\"#123456\"}"));
    ru.givler.mbo.config.ColoredLightConfig.write(fresh);
    ru.givler.mbo.config.ColoredLightConfig.saveUserOptions();
    check(ru.givler.mbo.config.ColoredLightConfig.read(config.toFile()).getAsJsonObject("blocks").has("example:custom"), "Menu save discarded source rules");
    Files.delete(obsolete); Files.delete(config); Files.delete(config.getParent()); Files.delete(directory);
    System.out.println("Default sources: metadata, optional Thaumcraft, preserved user config and atomic effect validation passed");
  }

  private static void burning() throws Exception {
    Field field = Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true);
    Unsafe unsafe = (Unsafe) field.get(null);
    NightWorld world = (NightWorld) unsafe.allocateInstance(NightWorld.class); world.isRemote = true;
    BurningSource entity = (BurningSource) unsafe.allocateInstance(BurningSource.class); entity.worldObj = world; entity.burning = true;
    ru.givler.mbo.client.render.ColoredBurningRenderHooks.record(entity, 3, 20);
    equal(ColoredLightApi.emission(ru.givler.mbo.block.BlockColourFire.COLORS[3], 15), ColoredLightApi.entityEmission(entity));
    ru.givler.mbo.client.render.ColoredBurningRenderHooks.record(entity, 10, 20);
    equal(ColoredLightApi.emission(ru.givler.mbo.block.BlockColourFire.COLORS[10], 15), ColoredLightApi.entityEmission(entity));
    entity.burning = false; equal(0, ColoredLightApi.entityEmission(entity)); entity.burning = true;
    ru.givler.mbo.config.ColoredLightSources.coloredBurning = false; equal(0, ColoredLightApi.entityEmission(entity));
    ru.givler.mbo.config.ColoredLightSources.coloredBurning = true;
    world.time = 21; equal(0, ColoredLightApi.entityEmission(entity));
    System.out.println("Colored burning: palette changes, extinguishing, expiry and disable passed");
  }
  private static final class BurningSource extends net.minecraft.entity.Entity {
    boolean burning;
    BurningSource() { super(null); }
    @Override public boolean isBurning() { return burning; }
    @Override protected void entityInit() {}
    @Override protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) {}
    @Override protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) {}
  }

  private static void mf2Bridge() throws Exception {
    String target = "mods.battlegear2.client.utils.DynamicLightHook";
    ru.givler.mbo.core.ColoredDynamicLightTransformer transformer = new ru.givler.mbo.core.ColoredDynamicLightTransformer();
    byte[] original = resource(target.replace('.', '/'));
    byte[] patched = transformer.transform(target, target, original);
    ClassNode node = new ClassNode(); new ClassReader(patched).accept(node, 0);
    int hooks = 0;
    for (MethodNode method : node.methods) {
      if (!method.name.equals("getExtraLightLevel")) continue;
      new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name, method);
      for (AbstractInsnNode instruction : method.instructions.toArray()) if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).name.equals("optifineLevel")) hooks++;
    }
    check(hooks > 0, "MF2 early-return helper was not patched");
    check(Arrays.equals(patched, transformer.transform(target, target, patched)), "MF2 hook must be idempotent");
    System.out.println("MF2 dynamic-light helper: all returns patched and bytecode verified");
  }

  private static void shaderPrograms() throws Exception {
    org.lwjgl.opengl.Pbuffer context = new org.lwjgl.opengl.Pbuffer(32, 32, new org.lwjgl.opengl.PixelFormat(8, 24, 0), null, null);
    try {
      context.makeCurrent();
      entityLightPixels();
      Class<?> decor=ru.givler.mbo.client.render.decormodels.InstancedDecorRenderer.class;
      Field vertexSource=decor.getDeclaredField("VERTEX"), fragmentSource=decor.getDeclaredField("FRAGMENT");
      vertexSource.setAccessible(true); fragmentSource.setAccessible(true);
      Path decorVertex=Paths.get("build/decor-rgb-test.vsh"), decorFragment=Paths.get("build/decor-rgb-test.fsh");
      Files.write(decorVertex,((String)vertexSource.get(null)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      Files.write(decorFragment,((String)fragmentSource.get(null)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      int decorVs=compileShader(decorVertex,org.lwjgl.opengl.GL20.GL_VERTEX_SHADER), decorFs=compileShader(decorFragment,org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER);
      int decorProgram=org.lwjgl.opengl.GL20.glCreateProgram();
      org.lwjgl.opengl.GL20.glAttachShader(decorProgram,decorVs); org.lwjgl.opengl.GL20.glAttachShader(decorProgram,decorFs);
      org.lwjgl.opengl.GL20.glLinkProgram(decorProgram);
      check(org.lwjgl.opengl.GL20.glGetProgrami(decorProgram,org.lwjgl.opengl.GL20.GL_LINK_STATUS)!=0,"Instanced RGB shader failed: "+org.lwjgl.opengl.GL20.glGetProgramInfoLog(decorProgram,8192));
      check(org.lwjgl.opengl.GL20.glGetAttribLocation(decorProgram,"aTint")>=0,"Instanced RGB attribute missing");
      org.lwjgl.opengl.GL20.glDeleteProgram(decorProgram); org.lwjgl.opengl.GL20.glDeleteShader(decorVs); org.lwjgl.opengl.GL20.glDeleteShader(decorFs);
      System.out.println("Instanced JSON decor: RGB shader compiled and linked on real OpenGL");
      Path directory = Paths.get("shaderpacks/MBO_RGB_Lite/shaders");
      try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*.vsh")) {
        for (Path vertex : files) {
          String base = vertex.getFileName().toString().replace(".vsh", "");
          int vs = compileShader(vertex, org.lwjgl.opengl.GL20.GL_VERTEX_SHADER);
          int fs = compileShader(directory.resolve(base + ".fsh"), org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER);
          int program = org.lwjgl.opengl.GL20.glCreateProgram();
          org.lwjgl.opengl.GL20.glAttachShader(program, vs); org.lwjgl.opengl.GL20.glAttachShader(program, fs);
          org.lwjgl.opengl.GL20.glLinkProgram(program);
          check(org.lwjgl.opengl.GL20.glGetProgrami(program, org.lwjgl.opengl.GL20.GL_LINK_STATUS) != 0,
              base + ": " + org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, 8192));
          org.lwjgl.opengl.GL20.glUseProgram(program);
          org.lwjgl.opengl.GL20.glUniform1i(org.lwjgl.opengl.GL20.glGetUniformLocation(program, "texture"), 0);
          org.lwjgl.opengl.GL20.glUniform1i(org.lwjgl.opengl.GL20.glGetUniformLocation(program, "lightmap"), 1);
          org.lwjgl.opengl.GL20.glUniform1i(org.lwjgl.opengl.GL20.glGetUniformLocation(program, "mboLightVolume"), 15);
          org.lwjgl.opengl.GL20.glValidateProgram(program);
          check(org.lwjgl.opengl.GL20.glGetProgrami(program, org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS) != 0,
              base + " validation: " + org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, 8192));
          org.lwjgl.opengl.GL20.glUseProgram(0); org.lwjgl.opengl.GL20.glDeleteProgram(program);
          org.lwjgl.opengl.GL20.glDeleteShader(vs); org.lwjgl.opengl.GL20.glDeleteShader(fs);
        }
      }
      System.out.println("MBO RGB shaderpack: all GLSL 1.20 programs compiled, linked and sampler bindings validated");
    } finally { context.destroy(); }
  }
  private static int compileShader(Path path, int type) throws IOException {
    int shader = org.lwjgl.opengl.GL20.glCreateShader(type);
    org.lwjgl.opengl.GL20.glShaderSource(shader, new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8));
    org.lwjgl.opengl.GL20.glCompileShader(shader);
    check(org.lwjgl.opengl.GL20.glGetShaderi(shader, org.lwjgl.opengl.GL20.GL_COMPILE_STATUS) != 0,
        path + ": " + org.lwjgl.opengl.GL20.glGetShaderInfoLog(shader, 8192));
    return shader;
  }
  private static void entityLightPixels() throws Exception {
    Field singleton=net.minecraft.client.Minecraft.class.getDeclaredField("theMinecraft"); singleton.setAccessible(true); Object previous=singleton.get(null);
    Field unsafeField=Unsafe.class.getDeclaredField("theUnsafe"); unsafeField.setAccessible(true); Unsafe unsafe=(Unsafe)unsafeField.get(null);
    net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);
    NightWorld world=(NightWorld)unsafe.allocateInstance(NightWorld.class); mc.theWorld=world; singleton.set(null,mc);
    BurningSource entity=(BurningSource)unsafe.allocateInstance(BurningSource.class); entity.worldObj=world;
    entity.posX=entity.lastTickPosX=.5; entity.posY=entity.lastTickPosY=16.5; entity.posZ=entity.lastTickPosZ=.5;
    ColoredLightVolume oldVolume=ColoredLightClient.volume; boolean oldCache=ColoredLightClient.cacheActive, oldEnabled=ru.givler.mbo.config.ColoredLightConfig.enabled;
    float oldStrength=ru.givler.mbo.config.ColoredLightConfig.strength;
    int oldUnit=net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit;
    float oldX=net.minecraft.client.renderer.OpenGlHelper.lastBrightnessX, oldY=net.minecraft.client.renderer.OpenGlHelper.lastBrightnessY;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    int[] textures=new int[2];
    try {
      ColoredLightVolume field=blank(32); field.setCell(index(field,0,16,0),0,0xFF0000); finish(field);
      ColoredLightClient.volume=field; ColoredLightClient.cacheActive=false; ru.givler.mbo.config.ColoredLightConfig.enabled=true; ru.givler.mbo.config.ColoredLightConfig.strength=1;
      GL20.glUseProgram(0); GL11.glViewport(0,0,32,32); GL11.glDisable(GL11.GL_LIGHTING); GL11.glDisable(GL11.GL_FOG); GL11.glDisable(GL11.GL_BLEND); GL11.glDisable(GL11.GL_DEPTH_TEST);
      GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity(); GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glLoadIdentity();
      for(int unit=0;unit<2;unit++) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0+unit); textures[unit]=GL11.glGenTextures(); GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[unit]);
        java.nio.ByteBuffer white=org.lwjgl.BufferUtils.createByteBuffer(4); white.putInt(-1).flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,1,1,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,white);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL11.GL_TEXTURE_ENV_MODE,GL11.GL_MODULATE);
      }
      GL13.glActiveTexture(GL13.GL_TEXTURE2); GL11.glDisable(GL11.GL_TEXTURE_2D); GL13.glActiveTexture(GL13.GL_TEXTURE0);
      net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit=GL13.GL_TEXTURE1;
      net.minecraft.client.renderer.OpenGlHelper.lastBrightnessX=240;net.minecraft.client.renderer.OpenGlHelper.lastBrightnessY=0;
      int token=ru.givler.mbo.client.lighting.ColoredEntityLightHooks.begin(entity,1);
      check(token!=0,"Entity RGB scope did not activate");
      int rgbTexture;GL13.glActiveTexture(GL13.GL_TEXTURE2);rgbTexture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL13.glActiveTexture(GL13.GL_TEXTURE0);
      try { entityQuad(); java.nio.ByteBuffer red=org.lwjgl.BufferUtils.createByteBuffer(4);GL11.glReadPixels(16,16,1,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,red);
        check((red.get(0)&255)>240 && (red.get(1)&255)<10 && (red.get(2)&255)<10,"Model color reset defeated RGB lighting");
      } finally {ru.givler.mbo.client.lighting.ColoredEntityLightHooks.end(token);}
      equal(GL13.GL_TEXTURE0,GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE));
      GL13.glActiveTexture(GL13.GL_TEXTURE2);check(!GL11.glIsEnabled(GL11.GL_TEXTURE_2D),"Entity tint leaked texture state");GL13.glActiveTexture(GL13.GL_TEXTURE0);
      entityQuad();java.nio.ByteBuffer white=org.lwjgl.BufferUtils.createByteBuffer(4);GL11.glReadPixels(16,16,1,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,white);
      check((white.get(0)&255)>240 && (white.get(1)&255)>240 && (white.get(2)&255)>240,"Next entity inherited RGB");
      GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glDisable(GL11.GL_TEXTURE_2D);GL13.glActiveTexture(GL13.GL_TEXTURE0);
      equal(0,ru.givler.mbo.client.lighting.ColoredEntityLightHooks.begin(entity,1));
      GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL13.glActiveTexture(GL13.GL_TEXTURE2);GL11.glEnable(GL11.GL_TEXTURE_2D);GL13.glActiveTexture(GL13.GL_TEXTURE0);
      equal(0,ru.givler.mbo.client.lighting.ColoredEntityLightHooks.begin(entity,1));
      GL13.glActiveTexture(GL13.GL_TEXTURE2);GL11.glDisable(GL11.GL_TEXTURE_2D);GL13.glActiveTexture(GL13.GL_TEXTURE0);
      ru.givler.mbo.config.ColoredLightConfig.entityLighting=false;
      equal(0,ru.givler.mbo.client.lighting.ColoredEntityLightHooks.begin(entity,1));
      check(ColoredLightClient.volume==field && ColoredLightRenderHooks.worldRGB(.5,16.5,.5)!=0,"Entity toggle disabled world RGB");
      ru.givler.mbo.config.ColoredLightConfig.entityLighting=true;
      ru.givler.mbo.config.ColoredLightConfig.enabled=false;equal(0,ru.givler.mbo.client.lighting.ColoredEntityLightHooks.begin(entity,1));
      ru.givler.mbo.client.lighting.ColoredEntityLightHooks.release();check(!GL11.glIsTexture(rgbTexture),"Disabled RGB retained entity GPU texture");
      equal(GL11.GL_NO_ERROR,GL11.glGetError());
      System.out.println("Entity RGB pixels: model color resets preserved, next draw white, GL state restored and disable deletes texture");
    } finally {
      ru.givler.mbo.client.lighting.ColoredEntityLightHooks.release(); for(int texture:textures)if(texture!=0)GL11.glDeleteTextures(texture); GL11.glPopAttrib();
      singleton.set(null,previous);ColoredLightClient.volume=oldVolume;ColoredLightClient.cacheActive=oldCache;ru.givler.mbo.config.ColoredLightConfig.enabled=oldEnabled;ru.givler.mbo.config.ColoredLightConfig.strength=oldStrength;
      net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit=oldUnit;net.minecraft.client.renderer.OpenGlHelper.lastBrightnessX=oldX;net.minecraft.client.renderer.OpenGlHelper.lastBrightnessY=oldY;
    }
  }
  private static void entityQuad() {
    GL11.glColor4f(1,1,1,1); GL11.glBegin(GL11.GL_QUADS);GL11.glVertex2f(-1,-1);GL11.glVertex2f(1,-1);GL11.glVertex2f(1,1);GL11.glVertex2f(-1,1);GL11.glEnd();
  }

  private static int index(ColoredLightVolume v, int x, int y, int z) {
    return ((y - v.y) * v.size + z - v.z) * v.size + x - v.x;
  }

  private static void finish(ColoredLightVolume v) {
    int ticks = 0; while (!v.propagate(7)) if (++ticks > 100000) throw new AssertionError("Flood did not converge");
  }

  private static void propagation() {
    ColoredLightVolume v = blank(32);
    v.setCell(index(v, -2, 16, 0), 0, 0xFF0000);
    check(!v.propagate(1), "Budgeted fill must yield"); finish(v);
    equal(0xEE0000, v.sample(-1, 16, 0)); equal(0, v.sample(13, 16, 0));
    equal(0, v.sample(-17, 16, 0)); equal(0, v.sample(0, -1, 0));
    // Complete solid plane: no path around its edge within the volume.
    ColoredLightVolume wall = blank(32);
    for (int y = 0; y < 32; y++) for (int z = -16; z < 16; z++) wall.setCell(index(wall, 0, y, z), 255, 0);
    wall.setCell(index(wall, -2, 16, 0), 0, 0xFF0000); finish(wall);
    equal(0, wall.sample(0, 16, 0)); equal(0, wall.sample(1, 16, 0));
    wall.setCell(index(wall, 0, 16, 0), 0, 0);
    // Rebuild after opening a door/removing an obstacle.
    ColoredLightVolume open = blank(32);
    for (int y = 0; y < 32; y++) for (int z = -16; z < 16; z++) if (y != 16 || z != 0) open.setCell(index(open, 0, y, z), 255, 0);
    open.setCell(index(open, -2, 16, 0), 0, 0xFF0000); finish(open);
    check(open.sample(1, 16, 0) != 0, "Opening must transmit light");
    ColoredLightVolume mix = blank(32);
    mix.setCell(index(mix, -2, 16, 0), 0, 0xFF0000);
    mix.setCell(index(mix, 2, 16, 0), 0, 0x0000FF); finish(mix);
    equal(0xDD00DD, mix.sample(0, 16, 0));
    ColoredLightVolume removed = blank(32); finish(removed); equal(0, removed.sample(0, 16, 0));
    check(!removed.sameInputs(mix), "Removal must invalidate the cache");
    check(removed.sameInputs(blank(32)), "Unchanged empty area must skip propagation/rebuilds");
    // Dense source volume exercises the bounded circular queue.
    ColoredLightVolume dense = blank(16);
    for (int i = 0; i < dense.cells(); i++) dense.setCell(i, 0, i % 2 == 0 ? 0xFF0000 : 0x0000FF);
    finish(dense); check(dense.sample(0, 8, 0) != 0, "Dense fill failed");
    long start = System.nanoTime();
    for (int n = 0; n < 20; n++) {
      ColoredLightVolume test = blank(32);
      test.setCell(index(test, 0, 16, 0), 0, 0xFF8040);
      while (!test.propagate(4096)) {}
    }
    System.out.printf(java.util.Locale.ROOT, "32^3 volume allocation + single-source fill: %.3f ms average%n", (System.nanoTime() - start) / 20e6);
  }

  private static void color() {
    check(ColoredLightRenderHooks.skyColorWeight(15 << 20, 11) == 1F, "Night ambient sky floor must preserve full saturation");
    check(ColoredLightRenderHooks.skyColorWeight(15 << 20, 0) == 0F, "Full daylight must retain white outdoor lighting");
    check(ColoredLightRenderHooks.skyColorWeight(0, 0) == 1F, "Indoor light must keep full saturation even during daytime");
    float twilight = ColoredLightRenderHooks.skyColorWeight(15 << 20, 5);
    check(twilight > 0 && twilight < 1 && twilight < ColoredLightRenderHooks.skyColorWeight(15 << 20, 6), "Twilight must fade smoothly");
    equal(0xC80000FF, ColoredLightRenderHooks.tintColor(0xC8FFFFFF, 0xFF0000, 1, true));
    equal(0xFF0000C8, ColoredLightRenderHooks.tintColor(0xFFFFFFC8, 0xFF0000, 1, false));
    equal(0xC8FFFFFF, ColoredLightRenderHooks.tintColor(0xC8FFFFFF, 0xFF0000, 0, true));
    equal(0xC8FFFFFF, ColoredLightRenderHooks.tintColor(0xC8FFFFFF, 0, 1, true));
    equal(0xC8404040, ColoredLightRenderHooks.tintColor(0xC8404040, 0xFFFFFF, 1, true));
    check((ColoredLightRenderHooks.tintColor(0xC8FFFFFF, 0x800000, 1, true) >>> 8 & 255) > 0, "Distant light must fade to neutral");
  }

  private static void sectionTransitions() {
    ColoredLightCache cache = new ColoredLightCache();
    ColoredLightCache.Dirty dirty = (x, y, z) -> {};
    ColoredLightVolume first = new ColoredLightVolume(62, -23, 33, -31);
    for (int i = 0; i < first.cells(); i++) first.setCell(i, 0, 0);
    first.setCell(first.index(17, 64, 0), 0, 0x0000FF);
    first.setCell(first.index(0, 64, 0), 0, 0xFF0000);
    while (!first.propagate(32768)) {}
    cache.publish(first, 15, dirty);
    equal(0x0000DD, cache.sample(15, 64, 0)); equal(0x0000EE, cache.sample(16, 64, 0));
    int before = cache.sample(15, 64, 0), retained = cache.sample(-7, 64, 0);
    ColoredLightVolume moved = new ColoredLightVolume(62, -15, 33, -31);
    for (int i = 0; i < moved.cells(); i++) moved.setCell(i, 0, 0);
    moved.setCell(moved.index(17, 64, 0), 0, 0x0000FF);
    moved.setCell(moved.index(0, 64, 0), 0, 0xFF0000);
    while (!moved.propagate(32768)) {}
    cache.publish(moved, 15, dirty);
    equal(before, cache.sample(15, 64, 0)); equal(retained, cache.sample(-7, 64, 0));
    check(retained != 0, "Moving the scan window must retain previously rendered world colors");
    System.out.println("Chunk border 15/16: halo contribution, overlap stability and retained sections passed");
  }

  private static void settle(ColoredLightSections sections) {
    for (int i = 0; !sections.process(257); i++) if (i > 10000) throw new AssertionError("Incremental section queue did not converge");
  }

  private static void incrementalSections() {
    ColoredLightSections sections = new ColoredLightSections();
    ColoredLightCache cache = new ColoredLightCache();
    Set<String> dirty = new HashSet<>();
    ColoredLightCache.Dirty marker = (x, y, z) -> dirty.add((x >> 4) + ":" + (y >> 4) + ":" + (z >> 4));
    for (int y = 3; y <= 5; y++) for (int z = -1; z <= 1; z++) for (int x = -1; x <= 1; x++) sections.install(x, y, z, new byte[4096], new int[4096]);
    settle(sections);
    sections.update(15, 64, 0, 0, 0xFF0000); sections.update(17, 64, 0, 0, 0x0000FF);
    settle(sections); sections.publish(cache, marker);
    equal(0xEE00EE, cache.sample(16, 64, 0));
    ColoredLightVolume reference = new ColoredLightVolume(48, -16, 48, -16);
    for (int i = 0; i < reference.cells(); i++) reference.setCell(i, 0, 0);
    reference.setCell(reference.index(15, 64, 0), 0, 0xFF0000);
    reference.setCell(reference.index(17, 64, 0), 0, 0x0000FF);
    while (!reference.propagate(4096)) {}
    for (int i = 0; i < reference.cells(); i++) equal(reference.sample(reference.worldX(i), reference.worldY(i), reference.worldZ(i)), sections.sample(reference.worldX(i), reference.worldY(i), reference.worldZ(i)));
    // Chunk unloading removes the source and all residual contributions, across borders.
    sections.remove(1, 4, 0); settle(sections); sections.publish(cache, marker);
    equal(0, cache.sample(17, 64, 0)); equal(0xFF0000, cache.sample(15, 64, 0));
    sections.install(1, 4, 0, new byte[4096], new int[4096]); settle(sections); sections.publish(cache, marker);
    equal(0xEE0000, cache.sample(16, 64, 0));
    // Continuous opaque wall blocks propagation; reopening one cell restores it.
    for (int y = 48; y < 96; y++) for (int z = -16; z < 32; z++) sections.update(16, y, z, 255, 0);
    settle(sections); sections.publish(cache, marker); equal(0, cache.sample(17, 64, 0));
    sections.update(16, 64, 0, 0, 0); settle(sections); sections.publish(cache, marker); equal(0xDD0000, cache.sample(17, 64, 0));
    // One converged version is published; removing an emitter cannot leave phantom light.
    dirty.clear(); sections.update(15, 64, 0, 0, 0);
    sections.process(1); sections.publish(cache, marker);
    equal(0xFF0000, cache.sample(15, 64, 0)); check(dirty.isEmpty(), "Partial relaxation must not leak into render snapshots");
    settle(sections); sections.publish(cache, marker);
    for (int y = 48; y < 96; y++) for (int z = -16; z < 32; z++) for (int x = -16; x < 32; x++) equal(0, cache.sample(x, y, z));
    check(dirty.size() > 2, "A single publication must invalidate all affected sections");
    System.out.println("Incremental RGB sections: full reference match, mixed channels, chunk unload/reload, walls and source removal passed");
  }

  private static byte[] resource(String name) throws IOException {
    try (InputStream stream = ColoredLightSmoke.class.getClassLoader().getResourceAsStream(name + ".class")) {
      if (stream == null) throw new IOException(name); return read(stream);
    }
  }

  private static byte[] transform(String name, byte[] bytes, int expected) throws Exception {
    ColoredLightTransformer transformer = new ColoredLightTransformer();
    byte[] result = transformer.transform(name, name, bytes);
    ClassNode node = new ClassNode(); new ClassReader(result).accept(node, 0);
    int count = 0;
    for (MethodNode method : node.methods) {
      boolean hooked = false;
      for (AbstractInsnNode insn : method.instructions.toArray()) if (insn instanceof MethodInsnNode) {
        MethodInsnNode call = (MethodInsnNode) insn;
        if (call.owner.endsWith("ColoredLightRenderHooks") && (call.name.equals("begin") || call.name.equals("tintVertex"))) { count++; hooked = true; }
      }
      if (hooked) new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name, method);
    }
    equal(expected, count);
    check(Arrays.equals(result, transformer.transform(name, name, result)), "Transformer must be idempotent");
    return result;
  }

  private static void vanillaBytecode() throws Exception {
    transform("net.minecraft.client.renderer.RenderBlocks", resource("net/minecraft/client/renderer/RenderBlocks"), 8);
    byte[] tess = transform("net.minecraft.client.renderer.Tessellator", resource("net/minecraft/client/renderer/Tessellator"), 1);
    // Actually execute transformed vertex packing, instead of only checking instruction counts.
    Class<?> type = new ClassLoader(ColoredLightSmoke.class.getClassLoader()) {
      Class<?> define() { return defineClass("net.minecraft.client.renderer.Tessellator", tess, 0, tess.length); }
    }.define();
    Object instance = type.getConstructor().newInstance();
    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe"); unsafeField.setAccessible(true);
    RenderBlocks renderer = (RenderBlocks) ((Unsafe) unsafeField.get(null)).allocateInstance(RenderBlocks.class);
    renderer.blockAccess = (net.minecraft.world.ChunkCache) ((Unsafe) unsafeField.get(null)).allocateInstance(net.minecraft.world.ChunkCache.class);
    ColoredLightVolume v = blank(32); v.setCell(index(v, 0, 16, 0), 0, 0xFF0000); finish(v);
    ColoredLightClient.volume = v;
    type.getMethod("startDrawingQuads").invoke(instance);
    type.getMethod("setColorRGBA", int.class, int.class, int.class, int.class).invoke(instance, 255, 255, 255, 200);
    type.getMethod("setBrightness", int.class).invoke(instance, 0);
    Method vertex = type.getMethod("addVertex", double.class, double.class, double.class);
    long previous = ColoredLightRenderHooks.begin(renderer, 0, 16, 0, 1);
    vertex.invoke(instance, 0.0, 16.0, 0.0);
    ColoredLightRenderHooks.end(previous);
    vertex.invoke(instance, 1.0, 16.0, 0.0);
    Field raw = type.getDeclaredField("rawBuffer"); raw.setAccessible(true);
    int[] data = (int[]) raw.get(instance);
    check(data[5] != data[13], "World face vertex must be tinted; following unrelated vertex must remain neutral");
    equal(0xC8FFFFFF, data[13]);
    type.getMethod("setBrightness", int.class).invoke(instance, 15 << 20);
    previous = ColoredLightRenderHooks.begin(renderer, 0, 16, 0, 1);
    vertex.invoke(instance, 2.0, 16.0, 0.0); ColoredLightRenderHooks.end(previous);
    equal(0xC8FFFFFF, data[21]);
    previous = ColoredLightRenderHooks.begin(renderer, 0, 16, 0, 6);
    equal(0xC80000FF, ColoredLightRenderHooks.tintVertex(0xC8FFFFFF, 0, -0.2, 16.1, 0.3));
    equal(0xC80000FF, ColoredLightRenderHooks.tintVertex(0xC8FFFFFF, 0, 0.8, 16.9, 0.7));
    ColoredLightRenderHooks.end(previous);
    equal(0xC8FFFFFF, ColoredLightRenderHooks.tintVertex(0xC8FFFFFF, 0, 0, 16, 0));
    System.out.println("Crossed plants: source-cell color, random-offset stability, alpha and scope restoration passed");
    // Adjacent quads must agree at shared corners while preserving a smooth gradient.
    ColoredLightVolume gradient = new ColoredLightVolume(48, 0, 48, -16);
    for (int x = 0; x < 48; x++) for (int z = -1; z <= 1; z++) {
      int red = Math.max(0, Math.min(255, 255 - (x - 14) * 64));
      gradient.setCell(gradient.index(x, 64, z), 0, red << 16 | 255 - red);
    }
    ColoredLightClient.volume = gradient;
    Object smooth = type.getConstructor().newInstance();
    type.getMethod("startDrawingQuads").invoke(smooth);
    type.getMethod("setColorRGBA", int.class, int.class, int.class, int.class).invoke(smooth, 255, 255, 255, 200);
    type.getMethod("setBrightness", int.class).invoke(smooth, 0);
    previous = ColoredLightRenderHooks.begin(renderer, 15, 63, 0, 1);
    vertex.invoke(smooth, 15.0, 64.0, 0.0); vertex.invoke(smooth, 16.0, 64.0, 0.0);
    vertex.invoke(smooth, 16.0, 64.0, 1.0); vertex.invoke(smooth, 15.0, 64.0, 1.0);
    ColoredLightRenderHooks.end(previous);
    previous = ColoredLightRenderHooks.begin(renderer, 16, 63, 0, 1);
    vertex.invoke(smooth, 16.0, 64.0, 0.0); vertex.invoke(smooth, 17.0, 64.0, 0.0);
    vertex.invoke(smooth, 17.0, 64.0, 1.0); vertex.invoke(smooth, 16.0, 64.0, 1.0);
    ColoredLightRenderHooks.end(previous);
    int[] smoothData = (int[]) raw.get(smooth);
    equal(smoothData[13], smoothData[37]); equal(smoothData[21], smoothData[61]);
    check(smoothData[5] != smoothData[13], "A face must carry different corner colors, not a flat tint");
    for (int i = 5; i < 64; i += 8) equal(200, smoothData[i] >>> 24);
    // Nested inventory render scopes cannot destroy the outer world face context.
    previous = ColoredLightRenderHooks.begin(renderer, 15, 63, 0, 1);
    int outer = ColoredLightRenderHooks.tintVertex(0xFFFFFFFF, 0, 16, 64, 0);
    RenderBlocks inventory = (RenderBlocks) ((Unsafe) unsafeField.get(null)).allocateInstance(RenderBlocks.class);
    long nested = ColoredLightRenderHooks.begin(inventory, 15, 63, 0, 1);
    equal(0xFFFFFFFF, ColoredLightRenderHooks.tintVertex(0xFFFFFFFF, 0, 16, 64, 0));
    ColoredLightRenderHooks.end(nested);
    equal(outer, ColoredLightRenderHooks.tintVertex(0xFFFFFFFF, 0, 16, 64, 0));
    ColoredLightRenderHooks.end(previous);
    System.out.println("Actual vertex packing: smooth corner colors, shared chunk-border corners, alpha and nested inventory scope passed");
    if (Boolean.getBoolean("mbo.lightPixels")) renderGradient(type, smooth);
    ColoredLightClient.volume = null;
  }

  private static void renderGradient(Class<?> type, Object tessellator) throws Exception {
    org.lwjgl.opengl.Pbuffer buffer = new org.lwjgl.opengl.Pbuffer(128, 64, new org.lwjgl.opengl.PixelFormat(8, 24, 0), null, null);
    try {
      buffer.makeCurrent();
      net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit = org.lwjgl.opengl.GL13.GL_TEXTURE0;
      net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit = org.lwjgl.opengl.GL13.GL_TEXTURE1;
      org.lwjgl.opengl.GL11.glViewport(0, 0, 128, 64);
      org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
      org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_LIGHTING);
      org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
      org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);
      org.lwjgl.opengl.GL11.glClearColor(0, 0, 0, 1);
      org.lwjgl.opengl.GL11.glClear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT);
      org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_PROJECTION);
      org.lwjgl.opengl.GL11.glLoadIdentity(); org.lwjgl.opengl.GL11.glOrtho(15, 17, -1, 0, -128, 128);
      org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);
      org.lwjgl.opengl.GL11.glLoadIdentity(); org.lwjgl.opengl.GL11.glRotatef(90, 1, 0, 0);
      type.getMethod("draw").invoke(tessellator);
      java.nio.ByteBuffer pixels = org.lwjgl.BufferUtils.createByteBuffer(128 * 64 * 4);
      org.lwjgl.opengl.GL11.glReadPixels(0, 0, 128, 64, org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, pixels);
      java.awt.image.BufferedImage picture = new java.awt.image.BufferedImage(128, 64, java.awt.image.BufferedImage.TYPE_INT_RGB);
      for (int y = 0; y < 64; y++) for (int x = 0; x < 128; x++) {
        int i = (y * 128 + x) * 4;
        picture.setRGB(x, 63 - y, (pixels.get(i) & 255) << 16 | (pixels.get(i + 1) & 255) << 8 | pixels.get(i + 2) & 255);
      }
      for (int c = 0; c < 3; c++) {
        int left = pixels.get((32 * 128 + 63) * 4 + c) & 255, right = pixels.get((32 * 128 + 64) * 4 + c) & 255;
        check(Math.abs(left - right) <= 3, "Rendered seam at adjacent chunk quads");
      }
      check((pixels.get((32 * 128 + 32) * 4) & 255) != 0, "Gradient was not drawn");
      javax.imageio.ImageIO.write(picture, "png", new File("build/colored-light-smoothing-preview.png"));
      System.out.println("OpenGL gradient pixels: adjacent-quad seam <= 3 levels; preview saved");
    } finally { buffer.destroy(); }
  }

  private static void optifineBytecode(String[] args) throws Exception {
    // Reconstruct the user's actual E7 replacement classes from its xdelta patches.
    Class<?> patcher = Class.forName("optifine.Patcher");
    Class<?> providerType = Class.forName("optifine.IResourceProvider");
    try (ZipFile of = new ZipFile(args[0]); ZipFile vanilla = new ZipFile(args[1])) {
      Object provider = Proxy.newProxyInstance(providerType.getClassLoader(), new Class[]{providerType}, (p, m, a) -> {
        String path = (String) a[0]; if (path.startsWith("/")) path = path.substring(1);
        ZipEntry entry = vanilla.getEntry(path); return entry == null ? null : vanilla.getInputStream(entry);
      });
      Map<?, ?> config = (Map<?, ?>) patcher.getMethod("getConfigurationMap", ZipFile.class).invoke(null, of);
      Object patterns = patcher.getMethod("getConfigurationPatterns", Map.class).invoke(null, config);
      Method apply = patcher.getMethod("applyPatch", String.class, byte[].class, patterns.getClass(), Map.class, providerType);
      Mapping mapping = new Mapping(Paths.get(args[2]));
      for (String entry : new String[]{"DynamicLights.class", "shadersmod/client/Shaders.class"}) {
        ClassWriter mappedExtra = new ClassWriter(0);
        new ClassReader(read(of.getInputStream(of.getEntry(entry)))).accept(new RemappingClassAdapter(mappedExtra, mapping), ClassReader.EXPAND_FRAMES);
        String target = entry.substring(0, entry.length() - 6).replace('/', '.');
        net.minecraft.launchwrapper.IClassTransformer extra = target.equals("DynamicLights")
            ? new ru.givler.mbo.core.ColoredDynamicLightTransformer() : new ru.givler.mbo.core.ColoredLightShaderTransformer();
        byte[] transformed = extra.transform(target, target, mappedExtra.toByteArray());
        ClassNode extraNode = new ClassNode(); new ClassReader(transformed).accept(extraNode, 0);
        int hooks = 0;
        for (MethodNode method : extraNode.methods) {
          boolean hooked = false;
          for (AbstractInsnNode insn : method.instructions.toArray()) if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).owner.startsWith("ru/givler/mbo/client/lighting/")) { hooks++; hooked = true; }
          if (hooked) new Analyzer<BasicValue>(new BasicVerifier()).analyze(extraNode.name, method);
        }
        check(hooks > 0, "No E7 bridge hooks: " + target);
        check(Arrays.equals(transformed, extra.transform(target, target, transformed)), "E7 bridge must be idempotent: " + target);
        System.out.println("OptiFine E7 bridge verified: " + target + " (" + hooks + " hooks)");
      }
      for (String obf : new String[]{"blm", "bmh"}) {
        byte[] delta = read(of.getInputStream(of.getEntry("patch/" + obf + ".class.xdelta")));
        byte[] patched = (byte[]) apply.invoke(null, obf + ".class", delta, patterns, config, provider);
        ClassWriter mapped = new ClassWriter(0);
        new ClassReader(patched).accept(new RemappingClassAdapter(mapped, mapping), ClassReader.EXPAND_FRAMES);
        String name = mapping.map(obf).replace('/', '.');
        transform(name, mapped.toByteArray(), name.endsWith("RenderBlocks") ? 8 : 1);
        System.out.println("OptiFine E7 verified: " + name);
      }
    }
  }

  private static void clientNightLighting() throws Exception {
    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe"); unsafeField.setAccessible(true);
    Unsafe unsafe = (Unsafe) unsafeField.get(null);
    Field singleton = net.minecraft.client.Minecraft.class.getDeclaredField("theMinecraft"); singleton.setAccessible(true);
    Object previousMinecraft = singleton.get(null);
    ru.givler.mbo.block.BlockColourFire previousFire = ru.givler.mbo.registry.BlockRegistry.ColourFire;
    try {
      net.minecraft.client.Minecraft mc = (net.minecraft.client.Minecraft) unsafe.allocateInstance(net.minecraft.client.Minecraft.class);
      NightWorld world = (NightWorld) unsafe.allocateInstance(NightWorld.class);
      world.present = true; world.metadata = 10; world.isRemote=true;
      world.fire = (ru.givler.mbo.block.BlockColourFire) unsafe.allocateInstance(ru.givler.mbo.block.BlockColourFire.class);
      world.air = (net.minecraft.block.BlockAir) unsafe.allocateInstance(net.minecraft.block.BlockAir.class);
      ru.givler.mbo.registry.BlockRegistry.ColourFire = (ru.givler.mbo.block.BlockColourFire) world.fire;
      net.minecraft.world.WorldProvider provider = new net.minecraft.world.WorldProviderSurface() {
        @Override public float getSunBrightnessFactor(float partialTicks) { return world.daylight ? 1F : 0F; }
      };
      Field providerField = net.minecraft.world.World.class.getDeclaredField("provider"); providerField.setAccessible(true); providerField.set(world, provider);
      world.skylightSubtracted = 0; // Actual WorldClient cached state after receiving night time.
      mc.theWorld = world;
      RecordingRenderGlobal renderGlobal = (RecordingRenderGlobal) unsafe.allocateInstance(RecordingRenderGlobal.class);
      renderGlobal.requests = new HashSet<>(); mc.renderGlobal = renderGlobal;
      mc.renderViewEntity = (net.minecraft.entity.passive.EntityPig) unsafe.allocateInstance(net.minecraft.entity.passive.EntityPig.class);
      mc.renderViewEntity.posY = 65;
      singleton.set(null, mc);
      for (int i = 0; i < 100; i++) ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      Field scratch=ColoredLightClient.class.getDeclaredField("scanEmission");scratch.setAccessible(true);
      Object originalScratch=scratch.get(ColoredLightClient.INSTANCE);
      equal(11, ColoredLightClient.skyDarkness);
      check(ColoredLightClient.cacheActive && ColoredLightClient.CACHE.sample(0, 64, 0) != 0, "Real client ticks must find colored admin fire");
      int stationaryColor = ColoredLightClient.CACHE.sample(0, 64, 0);
      // Flight never evicts RGB belonging to a still-loaded chunk.
      for(int destination : new int[]{160,0,-160,0}) {
        mc.renderViewEntity.posX=destination;
        for(int tick=0;tick<160;tick++)ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
        equal(stationaryColor,ColoredLightClient.CACHE.sample(0,64,0));
      }
      mc.renderViewEntity.posX=160;
      world.present=false;world.observer.markBlockForUpdate(0,64,0);
      for(int tick=0;tick<6;tick++)ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(0,ColoredLightClient.CACHE.sample(0,64,0));
      world.present=true;world.observer.markBlockForUpdate(0,64,0);
      for(int tick=0;tick<6;tick++)ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(stationaryColor,ColoredLightClient.CACHE.sample(0,64,0));
      net.minecraft.world.chunk.Chunk chunk=(net.minecraft.world.chunk.Chunk)unsafe.allocateInstance(net.minecraft.world.chunk.Chunk.class);
      Field chunkWorld=net.minecraft.world.chunk.Chunk.class.getDeclaredField("worldObj");chunkWorld.setAccessible(true);chunkWorld.set(chunk,world);
      ColoredLightClient.INSTANCE.chunkUnloaded(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
      for(int tick=0;tick<6;tick++)ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(0,ColoredLightClient.CACHE.sample(0,64,0));
      mc.renderViewEntity.posX=0;
      ColoredLightClient.INSTANCE.chunkLoaded(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
      for(int tick=0;tick<160;tick++)ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(stationaryColor,ColoredLightClient.CACHE.sample(0,64,0));
      System.out.println("RGB chunk lifetime: flight preserves colour, distant source edits apply, unload clears and reload restores");
      world.blockReads = 0; mc.renderViewEntity.posX = 15;
      ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      check(world.blockReads <= ru.givler.mbo.config.ColoredLightConfig.scanBudget, "Moving inside one chunk must not rebuild/re-read the whole field");
      equal(stationaryColor, ColoredLightClient.CACHE.sample(0, 64, 0));
      mc.renderViewEntity.posX = 16;
      ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(stationaryColor, ColoredLightClient.CACHE.sample(0, 64, 0));
      // Finish discovery before testing a single block edit.
      for (int i = 0; i < 40; i++) ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      RenderBlocks renderer = (RenderBlocks) unsafe.allocateInstance(RenderBlocks.class);
      renderer.blockAccess = (net.minecraft.world.ChunkCache) unsafe.allocateInstance(net.minecraft.world.ChunkCache.class);
      long previous = ColoredLightRenderHooks.begin(renderer, 0, 63, 0, 1);
      int nightColor = ColoredLightRenderHooks.tint(0xFFFFFFFF, 15 << 20);
      ColoredLightRenderHooks.end(previous);
      check(nightColor != 0xFFFFFFFF, "Outdoor sky light 15 must not suppress color at night");
      world.daylight = true;
      ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(0, ColoredLightClient.skyDarkness);
      previous = ColoredLightRenderHooks.begin(renderer, 0, 63, 0, 1);
      equal(0xFFFFFFFF, ColoredLightRenderHooks.tint(0xFFFFFFFF, 15 << 20));
      ColoredLightRenderHooks.end(previous);
      // Do not wait for the background scan/pause after source edits.
      world.present = false;
      world.observer.markBlockForUpdate(0, 64, 0);
      renderGlobal.requests.clear();
      ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      check(renderGlobal.requests.size() > 2, "All affected sections must be invalidated together, not two per tick");
      for (int i = 0; i < 3; i++) ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(0, ColoredLightClient.CACHE.sample(0, 64, 0));
      world.present = true; world.metadata = 11;
      world.observer.markBlockForUpdate(0, 64, 0);
      for (int i = 0; i < 3; i++) ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      check(ColoredLightClient.CACHE.sample(0, 64, 0) != 0, "New fire must bypass background scan delay");
      check(originalScratch==scratch.get(ColoredLightClient.INSTANCE),"Scanner allocated a new array between sections");
      world.missingSourceCell=true;world.present=false;
      for(int i=0;i<2000;i++)ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      equal(0,ColoredLightClient.CACHE.sample(0,64,0));
      world.missingSourceCell=false;world.present=true;world.observer.markBlockForUpdate(0,64,0);
      for(int i=0;i<3;i++)ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
      check(ColoredLightClient.CACHE.sample(0,64,0)!=0,"Reused scan array lost restored source");
      movingSources(mc, world, unsafe);
      if (Boolean.getBoolean("mbo.lightPixels")) shaderBridgeUpload();
      menuAndDisable(mc,world,unsafe);
      System.out.println("Fire removal and placement applied within three client ticks");
      System.out.println("Client ticks: admin fire detected; outdoor night tint and daylight suppression passed");
    } finally {
      singleton.set(null, previousMinecraft); ru.givler.mbo.registry.BlockRegistry.ColourFire = previousFire;
      ColoredLightClient.volume = null; ColoredLightClient.cacheActive = false;
    }
  }

  private static final class NightWorld extends net.minecraft.client.multiplayer.WorldClient implements ColoredLightRenderHooks.WorldCoordinates {
    double rgbX,rgbY,rgbZ;
    public double lightOriginX(){return rgbX;} public double lightOriginY(){return rgbY;} public double lightOriginZ(){return rgbZ;}
    long time;
    @Override public long getTotalWorldTime() { return time; }
    net.minecraft.block.Block fire, air;
    boolean daylight;
    boolean present = true;
    boolean missingSourceCell;
    int metadata = 10;
    int blockReads;
    boolean wall;
    net.minecraft.block.Block barrier;
    net.minecraft.world.IWorldAccess observer;
    private NightWorld() { super(null, null, 0, net.minecraft.world.EnumDifficulty.NORMAL, null); }
    @Override public boolean blockExists(int x, int y, int z) { return !(missingSourceCell && x==0 && y==64 && z==0); }
    @Override public void addWorldAccess(net.minecraft.world.IWorldAccess access) { observer = access; }
    @Override public void removeWorldAccess(net.minecraft.world.IWorldAccess access) { if (observer == access) observer = null; }
    @Override public net.minecraft.block.Block getBlock(int x, int y, int z) { blockReads++; if (wall && x == 1) return barrier; return present && x == 0 && y == 64 && z == 0 ? fire : air; }
    @Override public int getBlockMetadata(int x, int y, int z) { return metadata; }
  }

  private static void movingSources(net.minecraft.client.Minecraft mc, NightWorld world, Unsafe unsafe) throws Exception {
    int previousInterval = ru.givler.mbo.config.ColoredLightConfig.dynamicInterval;
    try {
      ru.givler.mbo.config.ColoredLightConfig.dynamicInterval = 1;
      MovingSource source = (MovingSource) unsafe.allocateInstance(MovingSource.class);
      source.posY = 64.5; source.posX = source.posZ = 0.5;
      ColoredLightApi.registerEntity(MovingSource.class, entity -> ColoredLightApi.emission(0xFF0000, 15));
      List<net.minecraft.entity.Entity> entities = new ArrayList<>(); entities.add(source);
      Field list = net.minecraft.world.World.class.getDeclaredField("loadedEntityList"); list.setAccessible(true); list.set(world, entities);
      ru.givler.mbo.client.lighting.ColoredDynamicLights dynamic = ru.givler.mbo.client.lighting.ColoredDynamicLights.INSTANCE;
      dynamic.tick(mc); equal(0xFF0000, dynamic.sample(0, 64, 0));
      check(dynamic.sample(2, 64, 0) != 0, "Moving source should reach neighboring air");
      world.barrier = new net.minecraft.block.Block(net.minecraft.block.material.Material.rock) {}; world.wall = true;
      dynamic.invalidate(); equal(0, dynamic.sample(2, 64, 0));
      world.wall = false; source.posX = 10; dynamic.tick(mc); equal(0, dynamic.sample(0, 64, 0));
      entities.clear(); dynamic.tick(mc); equal(0, dynamic.sample(10, 64, 0));
      check(dynamic.sources().isEmpty(), "Removed entity must not leave moving RGB");
      System.out.println("Moving RGB: source color, solid wall, movement and removal passed");
    } finally { world.wall = false; ru.givler.mbo.config.ColoredLightConfig.dynamicInterval = previousInterval; }
  }
  private static final class MovingSource extends net.minecraft.entity.Entity {
    MovingSource() { super(null); }
    @Override protected void entityInit() {}
    @Override protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) {}
    @Override protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) {}
    @Override public float getEyeHeight() { return 0; }
  }

  private static void shaderBridgeUpload() throws Exception {
    org.lwjgl.opengl.Pbuffer context = new org.lwjgl.opengl.Pbuffer(32, 32, new org.lwjgl.opengl.PixelFormat(8, 24, 0), null, null);
    try {
      context.makeCurrent();
      Path shaders = Paths.get("shaderpacks/MBO_RGB_Lite/shaders");
      int vs = compileShader(shaders.resolve("gbuffers_terrain.vsh"), org.lwjgl.opengl.GL20.GL_VERTEX_SHADER);
      int fs = compileShader(shaders.resolve("gbuffers_terrain.fsh"), org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER);
      int program = org.lwjgl.opengl.GL20.glCreateProgram();
      org.lwjgl.opengl.GL20.glAttachShader(program, vs); org.lwjgl.opengl.GL20.glAttachShader(program, fs); org.lwjgl.opengl.GL20.glLinkProgram(program);
      org.lwjgl.opengl.GL20.glUseProgram(program);
      org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0 + 2);
      ru.givler.mbo.client.lighting.ColoredLightShaderBridge.useProgram(0, 64, 0);
      check(ru.givler.mbo.client.lighting.ColoredLightShaderBridge.active, "RGB shader interface was not detected");
      equal(org.lwjgl.opengl.GL13.GL_TEXTURE0 + 2, org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE));
      org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0 + 15);
      java.nio.ByteBuffer data = org.lwjgl.BufferUtils.createByteBuffer(64 * 64 * 64 * 4);
      org.lwjgl.opengl.GL11.glGetTexImage(org.lwjgl.opengl.GL12.GL_TEXTURE_3D, 0, org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, data);
      int index = ((24 * 64 + 24) * 64 + 24) * 4;
      int uploaded = (data.get(index) & 255) << 16 | (data.get(index + 1) & 255) << 8 | data.get(index + 2) & 255;
      equal(ColoredLightClient.CACHE.sample(0, 64, 0), uploaded);
      equal(ColoredLightClient.INSTANCE.opacity(0, 64, 0) * 17, data.get(index + 3) & 255);
      equal(org.lwjgl.opengl.GL11.GL_NO_ERROR, org.lwjgl.opengl.GL11.glGetError());
      int oldTexture = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL12.GL_TEXTURE_BINDING_3D);
      ru.givler.mbo.config.ColoredLightConfig.enabled = false;
      ru.givler.mbo.client.lighting.ColoredLightShaderBridge.release();
      check(!org.lwjgl.opengl.GL11.glIsTexture(oldTexture) && !ru.givler.mbo.client.lighting.ColoredLightShaderBridge.retainsField(), "Disabling must delete GPU field and release upload buffer");
      ru.givler.mbo.client.lighting.ColoredLightShaderBridge.useProgram(0,64,0);
      java.nio.FloatBuffer size = org.lwjgl.BufferUtils.createFloatBuffer(1);
      org.lwjgl.opengl.GL20.glGetUniform(program,org.lwjgl.opengl.GL20.glGetUniformLocation(program,"mboLightSize"),size);
      check(size.get(0) == 0F, "Disabled shader must not sample colored volume");
      check(!ru.givler.mbo.client.lighting.ColoredLightShaderBridge.retainsField(), "Disabled shader use must not reallocate field");
      ru.givler.mbo.config.ColoredLightConfig.enabled = true;
      org.lwjgl.opengl.GL20.glUseProgram(0); org.lwjgl.opengl.GL20.glDeleteProgram(program);
      org.lwjgl.opengl.GL20.glDeleteShader(vs); org.lwjgl.opengl.GL20.glDeleteShader(fs);
      ru.givler.mbo.client.lighting.ColoredLightShaderBridge.resetPrograms();
      System.out.println("RGB bridge: real GPU texture readback matches RGB/opacity, active texture restored");
    } finally { ru.givler.mbo.config.ColoredLightConfig.enabled = true; ru.givler.mbo.client.lighting.ColoredLightShaderBridge.active = false; context.destroy(); }
  }

  private static final class RecordingRenderGlobal extends net.minecraft.client.renderer.RenderGlobal {
    Set<String> requests;
    int reloads;
    private RecordingRenderGlobal() { super(null); }
    @Override public void markBlockRangeForRenderUpdate(int x, int y, int z, int x2, int y2, int z2) { requests.add((x >> 4) + ":" + (y >> 4) + ":" + (z >> 4)); }
    @Override public void loadRenderers() { reloads++; }
  }

  private static void menuAndDisable(net.minecraft.client.Minecraft mc,NightWorld world,Unsafe unsafe) throws Exception {
    Method sliderValue=ru.givler.mbo.client.lighting.ColoredLightMenu.LightSlider.class.getDeclaredMethod("mouseValue",int.class,int.class,int.class,float.class);sliderValue.setAccessible(true);
    for(float maximum:new float[]{1F,2F}) {
      check((Float)sliderValue.invoke(null,-100,10,200,maximum)==0F,"RGB slider failed to clamp left edge");
      check((Float)sliderValue.invoke(null,500,10,200,maximum)==maximum,"RGB slider failed to clamp right edge");
      check((Float)sliderValue.invoke(null,110,10,200,maximum)==maximum/2,"RGB slider midpoint is incorrect");
    }
    ru.givler.mbo.client.lighting.ColoredLightMenu menu = new ru.givler.mbo.client.lighting.ColoredLightMenu();
    for (int width : new int[]{320,427,854}) {
      net.minecraft.client.gui.GuiVideoSettings screen = (net.minecraft.client.gui.GuiVideoSettings) unsafe.allocateInstance(net.minecraft.client.gui.GuiVideoSettings.class);
      screen.width = width; screen.height = 240;
      List<net.minecraft.client.gui.GuiButton> buttons = new ArrayList<>();
      menu.init(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(screen,buttons));
      check(buttons.size() == 1, "Video settings must receive one torch button");
      net.minecraft.client.gui.GuiButton button = buttons.get(0);
      check(button.width == 20 && button.height == 20 && button.xPosition >= 0 && button.xPosition+20 <= width, "Torch button must fit scaled screen");
    }
    ru.givler.mbo.config.ColoredLightConfig.enabled = false;
    ColoredLightClient.INSTANCE.applyOptions();
    check(!ColoredLightClient.cacheActive && ColoredLightClient.volume == null && world.observer == null, "Disabling must detach world observer and renderer field");
    equal(0,ColoredLightClient.CACHE.sample(0,64,0));
    check(ColoredLightClient.INSTANCE.statistics().contains("sections=0") && ColoredLightClient.INSTANCE.statistics().contains("pending=0"), "No section data or pending scans while disabled");
    equal(0,ru.givler.mbo.client.lighting.ColoredDynamicLights.INSTANCE.cachedCells());
    check(!ru.givler.mbo.client.lighting.ColoredLightShaderBridge.retainsField(), "Disabled light must not retain GPU/CPU shader field");
    int reads = world.blockReads;
    for (int i = 0; i < 12; i++) ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
    equal(reads,world.blockReads);
    check(world.observer == null, "Disabled ticks must not reattach world listener");
    check(((RecordingRenderGlobal) mc.renderGlobal).reloads > 0, "Disabling must clear all baked chunk colors");
    ru.givler.mbo.config.ColoredLightConfig.enabled = true;
    for (int i = 0; i < 100; i++) ColoredLightClient.INSTANCE.tick(new cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END));
    check(world.observer != null && ColoredLightClient.CACHE.sample(0,64,0) != 0, "Re-enabling must rebuild current world from scratch");
    Field minecraftHome = cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome"); minecraftHome.setAccessible(true);
    Object previousHome = minecraftHome.get(null);
    minecraftHome.set(null,new File("."));
    try {
    ru.givler.mbo.config.ColoredLightConfig.load(new File("build/light-menu-config"));
    ru.givler.mbo.config.ColoredLightConfig.enabled = false;
    ru.givler.mbo.config.ColoredLightConfig.strength = 0.75F;
    ru.givler.mbo.config.ColoredLightConfig.brightnessGain = 1.25F;
    ru.givler.mbo.config.ColoredLightConfig.saveUserOptions();
    ru.givler.mbo.config.ColoredLightConfig.enabled = true;
    ru.givler.mbo.config.ColoredLightConfig.load(new File("build/light-menu-config"));
    check(!ru.givler.mbo.config.ColoredLightConfig.enabled && ru.givler.mbo.config.ColoredLightConfig.strength == 0.75F && ru.givler.mbo.config.ColoredLightConfig.brightnessGain == 1.25F, "GUI options must persist across reload");
    ru.givler.mbo.config.ColoredLightConfig.enabled = true;
    ru.givler.mbo.config.ColoredLightConfig.strength = 1F; ru.givler.mbo.config.ColoredLightConfig.brightnessGain = 1F;
    ru.givler.mbo.config.ColoredLightConfig.saveUserOptions();
    } finally { minecraftHome.set(null,previousHome); }
    System.out.println("Lighting menu: scaled torch button; full disable clears sections/scans/dynamic data, stops world reads, reloads geometry, and re-enables cleanly");
  }

  private static final class Mapping extends Remapper {
    final Map<String, String> classes = new HashMap<>(), fields = new HashMap<>(), methods = new HashMap<>();
    Mapping(Path srg) throws IOException {
      for (String line : Files.readAllLines(srg)) {
        String[] p = line.split(" ");
        if (p[0].equals("CL:")) classes.put(p[1], p[2]);
        else if (p[0].equals("FD:")) fields.put(p[1], p[2].substring(p[2].lastIndexOf('/') + 1));
        else if (p[0].equals("MD:")) methods.put(p[1] + p[2], p[3].substring(p[3].lastIndexOf('/') + 1));
      }
    }
    @Override public String map(String name) { return classes.containsKey(name) ? classes.get(name) : name; }
    @Override public String mapFieldName(String owner, String name, String desc) { return fields.containsKey(owner + "/" + name) ? fields.get(owner + "/" + name) : name; }
    @Override public String mapMethodName(String owner, String name, String desc) { return methods.containsKey(owner + "/" + name + desc) ? methods.get(owner + "/" + name + desc) : name; }
  }

  private static byte[] read(InputStream stream) throws IOException {
    try (InputStream input = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192]; int n; while ((n = input.read(buffer)) >= 0) out.write(buffer, 0, n); return out.toByteArray();
    }
  }
  private static void equal(int expected, int actual) { if (expected != actual) throw new AssertionError(Integer.toHexString(expected) + " != " + Integer.toHexString(actual)); }
  private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
