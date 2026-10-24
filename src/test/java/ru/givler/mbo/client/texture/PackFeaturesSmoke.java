package ru.givler.mbo.client.texture;

import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Actual E7 bytecode oracle, real resource pack and installed Angelica bytecode checks. */
public final class PackFeaturesSmoke implements Opcodes {
  static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
  public static void main(String[]args)throws Exception {
    Properties p=new Properties();p.setProperty("startFadeIn","22:00");p.setProperty("endFadeIn","23:00");p.setProperty("endFadeOut","03:00");
    CustomSkyRule sky=new CustomSkyRule(p);
    check(sky.fade(CustomSkyRule.time("22:30"))>.49f&&sky.fade(CustomSkyRule.time("22:30"))<.51f,"linear midnight fade");
    check(sky.fade(CustomSkyRule.time("01:00"))==1,"midnight plateau");
    check(sky.brightness(CustomSkyRule.time("01:00"),1,0)==0,"clear sky hides in rain");
    p.setProperty("weather","rain thunder");p.setProperty("days","0 2-3");p.setProperty("daysLoop","4");p.setProperty("heights","-64--1 10-20");p.setProperty("biomes","IcePlains 127");
    sky=new CustomSkyRule(p);
    check(sky.position(1,"Ice Plains",-10)&&sky.position(127,"Anything",15)&&!sky.position(2,"Desert",15),"sky biome and negative height filters");
    check(sky.brightness(CustomSkyRule.time("01:00"),1,.5f)==1,"rain/thunder partition");
    Properties mob=new Properties();mob.setProperty("skins.1","1-3");mob.setProperty("weights.1","0 3 1");mob.setProperty("biomes.1","IcePlains");mob.setProperty("heights.1","-64-100");mob.setProperty("name.1","ipattern:Bob*");mob.setProperty("professions.1","farmer 3");
    RandomMobRule rule=new RandomMobRule(mob,1);
    check(rule.matches(12,"Ice Plains",-20,"bobby",0)&&!rule.matches(12,"Ice Plains",-20,"Joe",0),"mob names, biomes, negative heights and profession");
    check(!rule.matches(12,"Ice Plains",-20,"Bobby",1),"profession rejection");
    for(int i=0;i<1000;i++)check(rule.choose(i)==(i%4<3?2:3),"weighted mob selection");
    Properties shortWeights=new Properties();shortWeights.setProperty("skins.1","1-3");shortWeights.setProperty("weights.1","2");
    RandomMobRule expanded=new RandomMobRule(shortWeights,1);check(expanded.choose(4)==3,"missing weights expand using mean");
    verifyMobResources();
    verifyLiveSettings();
    if(new java.io.File(args[0]).isFile()&&new java.io.File(args[1]).isFile())verifyPackAndE7(args[0],args[1]);
    verifyAsm(args[2]);
    System.out.println("PASS: sky/weather/day/location rules, mob variants/weights/names/professions and actual backend bytecode");
  }
  static void verifyMobResources()throws Exception {
    Map<String,byte[]> files=new HashMap<>();
    net.minecraft.client.resources.IResourceManager resources=(net.minecraft.client.resources.IResourceManager)Proxy.newProxyInstance(
      PackFeaturesSmoke.class.getClassLoader(),new Class[]{net.minecraft.client.resources.IResourceManager.class},(p,m,a)->{
        if(!m.getName().equals("getResource"))return null;
        byte[] bytes=files.get(a[0].toString());if(bytes==null)throw new java.io.FileNotFoundException(a[0].toString());
        return Proxy.newProxyInstance(PackFeaturesSmoke.class.getClassLoader(),new Class[]{net.minecraft.client.resources.IResource.class},(r,method,args)->{
          if(method.getName().equals("getInputStream"))return new java.io.ByteArrayInputStream(bytes);return null;
        });
      });
    net.minecraft.util.ResourceLocation base=new net.minecraft.util.ResourceLocation("minecraft:textures/entity/zombie/zombie.png");
    files.put("minecraft:mcpatcher/mob/zombie/zombie2.png",new byte[]{1});files.put("minecraft:mcpatcher/mob/zombie/zombie3.png",new byte[]{1});
    RandomMobTextureHooks.Definition automatic=RandomMobTextureHooks.load(base,resources);
    check(automatic.random.size()==3&&automatic.random.get(0).equals(base),"automatic numbered variants retain base as skin 1");
    files.put("minecraft:mcpatcher/mob/zombie/zombie.properties","skins.1=1 3\nweights.1=0 1\n".getBytes("UTF-8"));
    RandomMobTextureHooks.Definition defined=RandomMobTextureHooks.load(base,resources);
    check(defined.rules.size()==1&&defined.skins.get(defined.rules.get(0).choose(123)).getResourcePath().endsWith("zombie3.png"),"actual mob properties select numbered texture");
    files.clear();files.put("mbo:optifine/random/entity/test2.png",new byte[]{1});
    check(RandomMobTextureHooks.load(new net.minecraft.util.ResourceLocation("mbo:textures/entity/test.png"),resources).random.size()==2,"namespaced modern mob directory");
    files.clear();check(RandomMobTextureHooks.load(base,resources).random.isEmpty(),"absent pack preserves base texture");
  }
  static void verifyPackAndE7(String pack,String optifine)throws Exception {
    ClassNode original=new ClassNode();try(ZipFile zip=new ZipFile(optifine)){new ClassReader(zip.getInputStream(zip.getEntry("CustomSkyLayer.class"))).accept(original,0);}
    String oracleName="ru/givler/mbo/client/texture/E7SkyOracle";ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);
    w.visit(V1_8,ACC_PUBLIC,oracleName,null,"java/lang/Object",null);
    for(String f:new String[]{"startFadeIn","endFadeIn","startFadeOut","endFadeOut"})w.visitField(ACC_PUBLIC,f,"I",null,null).visitEnd();
    MethodVisitor ctor=w.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);ctor.visitCode();ctor.visitVarInsn(ALOAD,0);ctor.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V",false);ctor.visitInsn(RETURN);ctor.visitMaxs(0,0);ctor.visitEnd();
    for(MethodNode m:original.methods)if(Arrays.asList("getFadeBrightness","normalizeTime","timeBetween").contains(m.name)) {
      m.access=ACC_PUBLIC;
      for(AbstractInsnNode i:m.instructions.toArray()) {
        if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals("CustomSkyLayer"))((MethodInsnNode)i).owner=oracleName;
        if(i instanceof FieldInsnNode&&((FieldInsnNode)i).owner.equals("CustomSkyLayer"))((FieldInsnNode)i).owner=oracleName;
      }
      m.accept(w);
    }
    w.visitEnd();Class<?> oracle=new ClassLoader(PackFeaturesSmoke.class.getClassLoader()){Class<?> define(byte[] b){return defineClass(null,b,0,b.length);}}.define(w.toByteArray());
    Object instance=oracle.newInstance();Method fade=oracle.getMethod("getFadeBrightness",int.class);int layers=0,comparisons=0;
    try(ZipFile zip=new ZipFile(pack)) {
      for(Enumeration<? extends ZipEntry> entries=zip.entries();entries.hasMoreElements();) {
        ZipEntry entry=entries.nextElement();if(!entry.getName().contains("/sky/")||!entry.getName().endsWith(".properties"))continue;
        Properties p=new Properties();p.load(zip.getInputStream(entry));CustomSkyRule rule=new CustomSkyRule(p);
        oracle.getField("startFadeIn").setInt(instance,rule.start);oracle.getField("endFadeIn").setInt(instance,rule.end);
        oracle.getField("startFadeOut").setInt(instance,rule.startOut);oracle.getField("endFadeOut").setInt(instance,rule.endOut);
        for(int tick=0;tick<24000;tick+=17) {
          float expected=(Float)fade.invoke(instance,tick);check(Math.abs(expected-rule.fade(tick))<1e-6,"E7 fade mismatch: "+entry.getName()+" @"+tick);comparisons++;
        }
        String texture=p.getProperty("source");if(texture.startsWith("./"))texture=entry.getName().substring(0,entry.getName().lastIndexOf('/')+1)+texture.substring(2);
        check(zip.getEntry(texture)!=null,"sky source exists: "+texture);layers++;
      }
    }
    check(layers==26,"all 26 unmodified pack layers parsed");System.out.println("E7 bytecode oracle: "+layers+" real sky layers, "+comparisons+" fade comparisons");
  }
  static void verifyAsm(String path)throws Exception {
    byte[] vanilla;try(java.io.InputStream in=PackFeaturesSmoke.class.getClassLoader().getResourceAsStream("net/minecraft/client/renderer/entity/Render.class")){
      java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();byte[] block=new byte[8192];for(int n;(n=in.read(block))>=0;)b.write(block,0,n);vanilla=b.toByteArray();
    }
    byte[] rendered=new ru.givler.mbo.core.RandomMobTextureTransformer().transform("net.minecraft.client.renderer.entity.Render","net.minecraft.client.renderer.entity.Render",vanilla);
    ClassNode renderedNode=new ClassNode();new ClassReader(rendered).accept(renderedNode,0);
    for(MethodNode m:renderedNode.methods)if((m.access&(ACC_NATIVE|ACC_ABSTRACT))==0)new Analyzer(new BasicVerifier()).analyze(renderedNode.name,m);
    if(!new java.io.File(path).isFile())return;
    try(ZipFile zip=new ZipFile(path)) {
      for(String target:new String[]{"com.prupe.mcpatcher.ctm.CTMUtils","com.prupe.mcpatcher.cc.Colorizer","com.prupe.mcpatcher.cc.ColorizeBlock","com.prupe.mcpatcher.sky.SkyRenderer"}) {
        byte[] bytes=read(zip,target.replace('.','/')+".class");
        net.minecraft.launchwrapper.IClassTransformer patch=new ru.givler.mbo.integration.angelica.core.AngelicaPackSettingsTransformer();
        byte[] out=patch.transform(target,target,bytes);check(!Arrays.equals(bytes,out),"live settings installed: "+target);
        check(Arrays.equals(out,patch.transform(target,target,out)),"live settings patch idempotent");
        if(target.endsWith("SkyRenderer"))out=new ru.givler.mbo.integration.angelica.core.AngelicaSkyTransformer().transform(target,target,out);
        ClassNode c=new ClassNode();new ClassReader(out).accept(c,0);
        for(MethodNode m:c.methods)if((m.access&(ACC_NATIVE|ACC_ABSTRACT))==0)new Analyzer(new BasicVerifier()).analyze(c.name,m);
      }
      for(String target:new String[]{"com.prupe.mcpatcher.sky.SkyRenderer","com.prupe.mcpatcher.sky.SkyRenderer$Layer","com.prupe.mcpatcher.mob.MobRandomizer"}) {
        byte[] bytes=read(zip,target.replace('.','/')+".class");net.minecraft.launchwrapper.IClassTransformer transformer=target.contains("SkyRenderer")
          ?new ru.givler.mbo.integration.angelica.core.AngelicaSkyTransformer():new ru.givler.mbo.core.RandomMobTextureTransformer();
        byte[] out=transformer.transform(target,target,bytes);check(!Arrays.equals(bytes,out),"backend patched: "+target);
        check(Arrays.equals(out,transformer.transform(target,target,out)),"idempotent backend patch");
        ClassNode c=new ClassNode();new ClassReader(out).accept(c,0);
        for(MethodNode m:c.methods)if((m.access&(ACC_NATIVE|ACC_ABSTRACT))==0)new Analyzer(new BasicVerifier()).analyze(c.name,m);
      }
    }
  }
  static void verifyLiveSettings()throws Exception {
    // Isolate native configuration stubs so toggling can be tested without booting Forge/GL.
    final Map<String,byte[]> classes=new HashMap<>();
    String master="com/gtnewhorizons/angelica/config/AngelicaConfig",cfg="jss/notfine/config/MCPatcherForgeConfig$CustomColors",target="com/prupe/mcpatcher/cc/SettingsProbe";
    for(String type:new String[]{master,cfg}) {
      ClassWriter w=new ClassWriter(0);w.visit(V1_8,ACC_PUBLIC,type,null,"java/lang/Object",null);
      for(String f:type.equals(master)?new String[]{"enableMCPatcherForgeFeatures"}:new String[]{"enabled","water"})w.visitField(ACC_PUBLIC|ACC_STATIC,f,"Z",null,null).visitEnd();
      w.visitEnd();classes.put(type.replace('/','.'),w.toByteArray());
    }
    ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V1_8,ACC_PUBLIC,target,null,"java/lang/Object",null);
    w.visitField(ACC_STATIC|ACC_FINAL,"cached","Z",null,null).visitEnd();
    MethodVisitor init=w.visitMethod(ACC_STATIC,"<clinit>","()V",null,null);init.visitCode();init.visitFieldInsn(GETSTATIC,cfg,"water","Z");init.visitFieldInsn(PUTSTATIC,target,"cached","Z");init.visitInsn(RETURN);init.visitMaxs(0,0);init.visitEnd();
    MethodVisitor read=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"read","()Z",null,null);read.visitCode();read.visitFieldInsn(GETSTATIC,target,"cached","Z");read.visitInsn(IRETURN);read.visitMaxs(0,0);read.visitEnd();w.visitEnd();
    String name=target.replace('/','.');classes.put(name,new ru.givler.mbo.integration.angelica.core.AngelicaPackSettingsTransformer().transform(name,name,w.toByteArray()));
    ClassLoader loader=new ClassLoader(null){protected Class<?> findClass(String n)throws ClassNotFoundException {byte[] b=classes.get(n);if(b==null)throw new ClassNotFoundException(n);return defineClass(n,b,0,b.length);}};
    Class<?> settings=loader.loadClass(cfg.replace('/','.')),global=loader.loadClass(master.replace('/','.'));Method method=loader.loadClass(name).getMethod("read");
    for(int mask=0;mask<8;mask++) {
      global.getField("enableMCPatcherForgeFeatures").setBoolean(null,(mask&4)!=0);settings.getField("enabled").setBoolean(null,(mask&2)!=0);settings.getField("water").setBoolean(null,(mask&1)!=0);
      check((Boolean)method.invoke(null)==(mask==7),"live master/module/feature combination "+mask);
    }
    settings.getField("water").setBoolean(null,false);check(!(Boolean)method.invoke(null),"disable after enabling does not retain static cache");
    settings.getField("water").setBoolean(null,true);check((Boolean)method.invoke(null),"re-enable without restarting JVM");
  }
  static byte[] read(ZipFile z,String path)throws Exception {
    java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=z.getInputStream(z.getEntry(path))){byte[] block=new byte[8192];for(int n;(n=in.read(block))>=0;)b.write(block,0,n);}return b.toByteArray();
  }
}
