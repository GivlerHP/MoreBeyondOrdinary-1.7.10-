package ru.givler.mbo.integration.angelica.client;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class AngelicaDefaultCtmSmoke {
  static List<IResourcePack> loaded;
  public static class Packs {public static List<IResourcePack> getResourcePacks(String domain){return loaded;}}
  public static class Source extends ResourceLocation {
    final IResourcePack source;
    Source(String path,IResourcePack source){super("minecraft",path);this.source=source;}
    public IResourcePack getSource(){return source;}
  }
  public static class Properties {final ResourceLocation location;Properties(ResourceLocation location){this.location=location;}public ResourceLocation getResource(){return location;}}
  static IResourcePack pack(String name,String... resources) {
    Set<String> paths=new HashSet<String>(Arrays.asList(resources));
    return (IResourcePack)Proxy.newProxyInstance(AngelicaDefaultCtmSmoke.class.getClassLoader(),new Class<?>[]{IResourcePack.class},(p,m,a)->{
      if(m.getName().equals("getPackName"))return name;
      if(m.getName().equals("resourceExists"))return paths.contains(((ResourceLocation)a[0]).getResourcePath());
      if(m.getName().equals("getResourceDomains"))return Collections.singleton("minecraft");
      return null;
    });
  }
  public static void main(String[] args)throws Exception {
    IResourcePack bundled=pack("FMLFileResourcePack:NotFine"),user=pack("Cinnersis_v1.7.10.zip","textures/blocks/glass.png","textures/blocks/glass_blue.png");
    Field field=AngelicaDefaultCtmRules.class.getDeclaredField("packs");field.setAccessible(true);Object previous=field.get(null);
    field.set(null,Packs.class.getMethod("getResourcePacks",String.class));
    try {
      check(AngelicaDefaultCtmRules.bundled("FMLFileResourcePack:Angelica"),"Angelica standalone pack");
      check(AngelicaDefaultCtmRules.bundled("FMLFileResourcePack:MCPatcherForge"),"MCPatcher standalone pack");
      loaded=Arrays.asList(bundled,user);
      Properties rule=new Properties(new Source("mcpatcher/ctm/glass/glass.properties",bundled));
      check(AngelicaDefaultCtmRules.skip(rule),"Bundled clear glass must not replace user texture");
      check(AngelicaDefaultCtmRules.skip(new Properties(new Source("mcpatcher/ctm/glass/glasspane.properties",bundled))),"Pane fallback");
      check(AngelicaDefaultCtmRules.skip(new Properties(new Source("mcpatcher/ctm/stained_glass/glass_blue/blue_stained_glass.properties",bundled))),"Blue fallback");
      check(!AngelicaDefaultCtmRules.skip(new Properties(new Source("mcpatcher/ctm/stained_glass/glass_red/red_stained_glass.properties",bundled))),"Red base not replaced");
      check(!AngelicaDefaultCtmRules.skip(new Properties(new Source("mcpatcher/ctm/glass/glass.properties",user))),"User rule at identical path must survive");
      check(!AngelicaDefaultCtmRules.skip(new Properties(new Source("mcpatcher/ctm/custom/block20.properties",bundled))),"Other paths must survive");
      check(!AngelicaDefaultCtmRules.skip(new Properties(new ResourceLocation("mcpatcher/ctm/glass/glass.properties"))),"Unknown source must survive");
      loaded=Collections.singletonList(bundled);check(!AngelicaDefaultCtmRules.skip(rule),"Vanilla defaults must return after resource reload");
      check(AngelicaDefaultCtmRules.baseTexture("some_mod","mcpatcher/ctm/glass/glass.properties")==null,"Other namespaces must survive");
      check("textures/blocks/glass_silver.png".equals(AngelicaDefaultCtmRules.baseTexture("minecraft","mcpatcher/ctm/stained_glass/glass_light_gray/light_gray_stained_glass.properties")),"Light gray texture name");
    }finally {field.set(null,previous);}
    verifyActual();
    verifyNumericSelectors();
    verifyPaneMenu();
    verifyPaneDiagnostics();
    System.out.println("Angelica glass: pack source and base-texture overrides, panes/stained glass, per-color isolation, unchanged user CTM and reload defaults passed");
  }
  static void check(boolean good,String message){if(!good)throw new AssertionError(message);}
  static void verifyPaneDiagnostics()throws Exception {
    String name="com.prupe.mcpatcher.ctm.GlassPaneRenderer";
    try(InputStream in=AngelicaDefaultCtmSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
      if(in==null)return;
      ClassReader reader=new ClassReader(in);ClassWriter source=new ClassWriter(0);reader.accept(source,0);
      ru.givler.mbo.integration.angelica.core.AngelicaCtmTransformer transformer=new ru.givler.mbo.integration.angelica.core.AngelicaCtmTransformer();
      byte[] patched=transformer.transform(name,name,source.toByteArray());
      check(Arrays.equals(patched,transformer.transform(name,name,patched)),"Pane dispatch diagnostics idempotence");
      ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);
      for(MethodNode method:node.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,method);
      System.out.println("Actual pane dispatch diagnostic: all-method stack verification and idempotence passed");
    }
  }
  static void verifyPaneMenu()throws Exception {
    try{Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures");}catch(ClassNotFoundException absent){return;}
    Field locale=net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale");locale.setAccessible(true);
    locale.set(null,new net.minecraft.client.resources.Locale());
    Field setting=Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures").getField("glassPane");
    Class<?> savedConfig=ru.givler.mbo.integration.angelica.client.gui.AngelicaCtmOptions.configClass();
    @SuppressWarnings("unchecked") Class<? extends java.lang.annotation.Annotation> annotation=(Class<? extends java.lang.annotation.Annotation>)Class.forName("com.gtnewhorizon.gtnhlib.config.Config");
    check(savedConfig.isAnnotationPresent(annotation),"Saved CTM config must carry GTNHLib @Config");
    check(savedConfig==setting.getDeclaringClass(),"Save the configuration that owns the pane field");
    boolean before=setting.getBoolean(null);
    Object page=ru.givler.mbo.integration.angelica.client.gui.AngelicaCtmOptions.createPage();
    check("CTM".equals(page.getClass().getMethod("getName").invoke(page)),"CTM page name");
    List<?> options=(List<?>)page.getClass().getMethod("getOptions").invoke(page);
    check(options.size()==1,"Pane menu toggle");
    Object option=options.get(0);Class<?> type=option.getClass();
    check(type.getMethod("getControl").invoke(option)!=null,"Pane checkbox control");
    type.getMethod("setValue",Object.class).invoke(option,!before);
    check(setting.getBoolean(null)==before,"Pane setting must wait for Apply");
    type.getMethod("reset").invoke(option);
    check(Boolean.FALSE.equals(type.getMethod("hasChanged").invoke(option)),"Pane Undo");
    type.getMethod("setValue",Object.class).invoke(option,!before);
    type.getMethod("applyChanges").invoke(option);
    Object storage=type.getMethod("getStorage").invoke(option);
    Class<?> storageType=Class.forName("me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage");
    check(((boolean[])storageType.getMethod("getData").invoke(storage))[0]==!before,"Pane Apply binding");
    System.out.println("Actual Angelica CTM menu: native pane checkbox, pending Apply and Undo passed");
  }
  static class TestBlock extends net.minecraft.block.Block {
    TestBlock(){super(net.minecraft.block.material.Material.glass);}
  }
  static void verifyNumericSelectors()throws Exception {
    Object registry=cpw.mods.fml.common.registry.GameData.getBlockRegistry();
    Method register=registry.getClass().getDeclaredMethod("addObjectRaw",int.class,String.class,Object.class);register.setAccessible(true);
    TestBlock glass=new TestBlock(),mod=new TestBlock();
    register.invoke(registry,20,"minecraft:glass",glass);
    register.invoke(registry,4001,"mbo:test_numeric_ctm",mod);
    check("minecraft:glass:0-4 7-15".equals(AngelicaLegacyBlockIds.resolve("20:0-4 7-15")),"Numeric metadata must survive");
    check("mbo:test_numeric_ctm:2,3".equals(AngelicaLegacyBlockIds.resolve("4001:2,3")),"Mod numeric registry lookup");
    check("mbo:test_numeric_ctm:2".equals(AngelicaLegacyBlockIds.resolve("mbo:test_numeric_ctm:2")),"Named selector must be untouched");
    check("99999999999999".equals(AngelicaLegacyBlockIds.resolve("99999999999999")),"Numeric overflow");
    check("3999".equals(AngelicaLegacyBlockIds.resolve("3999")),"Unknown ID must not become air");
    final String name="com.prupe.mcpatcher.mal.block.BlockAPI";
    try(InputStream in=AngelicaDefaultCtmSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
      if(in==null)return;
      ClassReader reader=new ClassReader(in);ClassWriter writer=new ClassWriter(0);reader.accept(writer,0);
      ru.givler.mbo.integration.angelica.core.AngelicaCtmTransformer transformer=new ru.givler.mbo.integration.angelica.core.AngelicaCtmTransformer();
      final byte[] patched=transformer.transform(name,name,writer.toByteArray());
      check(Arrays.equals(patched,transformer.transform(name,name,patched)),"Numeric parser transform idempotence");
      ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);
      for(MethodNode method:node.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,method);
      ClassLoader loader=new ClassLoader(AngelicaDefaultCtmSmoke.class.getClassLoader()) {
        @Override protected Class<?> loadClass(String n,boolean resolve)throws ClassNotFoundException {
          if(!name.equals(n))return super.loadClass(n,resolve);
          Class<?> loaded=findLoadedClass(n);if(loaded==null)loaded=defineClass(n,patched,0,patched.length);
          if(resolve)resolveClass(loaded);return loaded;
        }
      };
      Class<?> properties=Class.forName("com.prupe.mcpatcher.mal.resource.PropertiesFile");
      Object rule=properties.getConstructor(Class.forName("com.prupe.mcpatcher.MCLogger"),ResourceLocation.class,java.util.Properties.class)
          .newInstance(null,new ResourceLocation("mcpatcher/ctm/block20.properties"),new java.util.Properties());
      Method parse=loader.loadClass(name).getMethod("createMatchers",properties,String.class);
      for(String selector:new String[]{"20:0","4001:2,3","mbo:test_numeric_ctm:2,3"}) {
        List<?> matches=(List<?>)parse.invoke(null,rule,selector);
        check(matches.size()==1,"Actual Angelica parser lost selector "+selector);
        Object match=matches.get(0);
        check(match.getClass().getMethod("getBlock").invoke(match)==(selector.startsWith("20:")?glass:mod),"Actual registry binding "+selector);
        Field bits=match.getClass().getDeclaredField("metadataBits");bits.setAccessible(true);
        check(bits.getInt(match)==(selector.startsWith("20:")?1:12),"Actual metadata binding "+selector);
      }
      System.out.println("Actual Angelica parser: numeric glass/mod IDs, namespaced selectors and metadata masks passed");
    }
  }
  static void verifyActual()throws Exception {
    String name="com.prupe.mcpatcher.ctm.TileOverride";
    try(InputStream in=AngelicaDefaultCtmSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
      if(in==null)return;
      ClassReader reader=new ClassReader(in);ClassWriter original=new ClassWriter(0);reader.accept(original,0);
      ru.givler.mbo.integration.angelica.core.AngelicaCtmTransformer transformer=new ru.givler.mbo.integration.angelica.core.AngelicaCtmTransformer();
      byte[] output=transformer.transform(name,name,original.toByteArray());
      check(Arrays.equals(output,transformer.transform(name,name,output)),"Repeated CTM transform");
      ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);int hooks=0;
      for(MethodNode method:node.methods) {
        new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,method);
        for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.endsWith("AngelicaDefaultCtmRules"))hooks++;
      }
      check(hooks==1,"Exactly one CTM factory guard");
      System.out.println("Actual Angelica CTM factory: all-method verification and idempotence passed");
    }
  }
}
