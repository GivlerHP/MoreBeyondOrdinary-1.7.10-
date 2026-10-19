package ru.givler.mbo.client.texture;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class NaturalTextureSmoke implements Opcodes {
  static boolean enabled=true;static double observedU,observedV;
  public static void record(double u,double v){observedU=u;observedV=v;}
  public static void scopeBody(){record(NaturalTextureHooks.u(.25,.5),NaturalTextureHooks.v(.25,.5));throw new IllegalStateException("scope test");}
  static IIcon icon(String name) {
    return (IIcon)Proxy.newProxyInstance(NaturalTextureSmoke.class.getClassLoader(),new Class<?>[]{IIcon.class},(p,m,a)->{
      switch(m.getName()) {
        case "getIconName":return name;
        case "getMinU":return .25F;case "getMaxU":return .5F;
        case "getMinV":return .5F;case "getMaxV":return .75F;
        case "getIconWidth":case "getIconHeight":return 16;
        case "toString":return name;
        default:return 0F;
      }
    });
  }
  static class Loader extends ClassLoader {
    Loader(){super(NaturalTextureSmoke.class.getClassLoader());}
    Class<?> define(byte[] bytes){return defineClass(null,bytes,0,bytes.length);}
  }
  public static void main(String[] args)throws Exception {
    Map<String,Integer> parsed=NaturalTextureHooks.parse(new StringReader("# format\nstone=2\nmod:tile=4F\nsand=F\na=4\nb=2F\nbad=3\n"));
    if(parsed.size()!=5 || parsed.get("mod:tile")!=12)throw new AssertionError("Namespaced natural rules or value parsing");
    NaturalTextureHooks.publish(parsed,true);NaturalTextureHooks.configure(()->enabled);
    if(NaturalTextureHooks.allowBuiltin(true))throw new AssertionError("Built-in randomizer must not run twice");
    IBlockAccess world=(IBlockAccess)Proxy.newProxyInstance(NaturalTextureSmoke.class.getClassLoader(),new Class<?>[]{IBlockAccess.class},(p,m,a)->null);
    RenderBlocks renderer=new RenderBlocks(world);IIcon stone=icon("stone");
    double[][] corners={{.25,.5},{.5,.5},{.5,.75},{.25,.75}};
    for(String mode:new String[]{"4","2","F","4F","2F"}) {
      NaturalTextureHooks.publish(NaturalTextureHooks.parse(new StringReader("stone="+mode)),true);
      Set<String> variants=new HashSet<String>();
      for(int x=-32;x<32;x++)for(int face=0;face<6;face++) {
        long token=NaturalTextureHooks.begin(renderer,null,x,-64,7,stone,face);
        Set<String> mapped=new HashSet<String>();String key="";
        for(double[] uv:corners) {
          double u=NaturalTextureHooks.u(uv[0],uv[1]),v=NaturalTextureHooks.v(uv[0],uv[1]);
          if(u<.25 || u>.5 || v<.5 || v>.75)throw new AssertionError("UV escaped atlas sprite");
          String value=u+","+v;mapped.add(value);key+=value+";";
        }
        if(mapped.size()!=4)throw new AssertionError("UV mapping collapsed corners");
        variants.add(key);NaturalTextureHooks.end(token);
      }
      int expected=mode.equals("4F")?8:mode.equals("F") || mode.equals("2")?2:4;
      if(variants.size()!=expected)throw new AssertionError("Natural variants "+mode+": "+variants.size());
    }
    long outer=NaturalTextureHooks.begin(renderer,null,3,-64,7,stone,1);
    double before=NaturalTextureHooks.u(.25,.5);
    long inner=NaturalTextureHooks.begin(new RenderBlocks(),null,0,0,0,stone,1);
    near(.25,NaturalTextureHooks.u(.25,.5));NaturalTextureHooks.end(inner);near(before,NaturalTextureHooks.u(.25,.5));
    inner=NaturalTextureHooks.begin(renderer,null,0,0,0,icon("mcpatcher/ctm/stone/1"),1);
    near(.25,NaturalTextureHooks.u(.25,.5));NaturalTextureHooks.end(inner);NaturalTextureHooks.end(outer);
    near(.25,NaturalTextureHooks.u(.25,.5));enabled=false;
    outer=NaturalTextureHooks.begin(renderer,null,3,-64,7,stone,1);near(.25,NaturalTextureHooks.u(.25,.5));NaturalTextureHooks.end(outer);enabled=true;
    verifyAsm(renderer,stone,world);verifyE7();verifyPack();
    NaturalTextureHooks.publish(Collections.<String,Integer>emptyMap(),false);
    if(!NaturalTextureHooks.allowBuiltin(true) || NaturalTextureHooks.allowBuiltin(false))throw new AssertionError("Absent file changed renderer defaults");
    System.out.println("Natural textures: all modes, atlas containment, deterministic variants, namespaced textures, negative Y, settings disable, nested inventory/CTM isolation, exceptional scope cleanup and UV bytecode passed");
  }
  static void near(double a,double b){if(Math.abs(a-b)>1e-9)throw new AssertionError(a+" != "+b);}
  static void verifyAsm(RenderBlocks renderer,IIcon icon,IBlockAccess world)throws Exception {
    String owner="ru/givler/mbo/client/texture/NaturalTestRenderer";ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);
    w.visit(V1_8,ACC_PUBLIC,owner,null,"net/minecraft/client/renderer/RenderBlocks",null);
    MethodVisitor m=w.visitMethod(ACC_PUBLIC,"<init>","(Lnet/minecraft/world/IBlockAccess;)V",null,null);m.visitCode();
    m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,"net/minecraft/client/renderer/RenderBlocks","<init>","(Lnet/minecraft/world/IBlockAccess;)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
    for(String name:new String[]{"renderFaceYNeg","renderFaceYPos","renderFaceZNeg","renderFaceZPos","renderFaceXNeg","renderFaceXPos"}) {
      m=w.visitMethod(ACC_PUBLIC,name,"(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V",null,null);m.visitCode();
      m.visitMethodInsn(INVOKESTATIC,"ru/givler/mbo/client/texture/NaturalTextureSmoke","scopeBody","()V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
    }
    w.visitEnd();ru.givler.mbo.core.NaturalTextureTransformer transformer=new ru.givler.mbo.core.NaturalTextureTransformer();
    byte[] bytes=transformer.transform(owner,"net.minecraft.client.renderer.RenderBlocks",w.toByteArray());
    if(!Arrays.equals(bytes,transformer.transform(owner,"net.minecraft.client.renderer.RenderBlocks",bytes)))throw new AssertionError("Double natural transformation");
    Class<?> type=new Loader().define(bytes);Object instance=type.getConstructor(IBlockAccess.class).newInstance(world);
    try {type.getMethod("renderFaceYPos",Block.class,double.class,double.class,double.class,IIcon.class).invoke(instance,null,3D,-64D,7D,icon);throw new AssertionError("Expected body exception");}
    catch(InvocationTargetException expected){if(!(expected.getCause() instanceof IllegalStateException))throw expected;}
    near(.25,NaturalTextureHooks.u(.25,.5));
    owner="ru/givler/mbo/client/texture/NaturalTestTessellator";w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V1_8,ACC_PUBLIC,owner,null,"java/lang/Object",null);
    m=w.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
    m=w.visitMethod(ACC_PUBLIC,"addVertexWithUV","(DDDDD)V",null,null);m.visitCode();m.visitVarInsn(DLOAD,7);m.visitVarInsn(DLOAD,9);
    m.visitMethodInsn(INVOKESTATIC,"ru/givler/mbo/client/texture/NaturalTextureSmoke","record","(DD)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();
    bytes=transformer.transform(owner,"net.minecraft.client.renderer.Tessellator",w.toByteArray());type=new Loader().define(bytes);instance=type.getConstructor().newInstance();
    long scope=NaturalTextureHooks.begin(renderer,null,3,-64,7,icon,1);
    double eu=NaturalTextureHooks.u(.25,.5),ev=NaturalTextureHooks.v(.25,.5);
    type.getMethod("addVertexWithUV",double.class,double.class,double.class,double.class,double.class).invoke(instance,0D,0D,0D,.25,.5);
    near(eu,observedU);near(ev,observedV);NaturalTextureHooks.end(scope);
  }
  static void verifyE7()throws Exception {
    String jar=System.getProperty("optifineReference");if(jar==null)return;
    try(ZipFile zip=new ZipFile(jar)) {
      ClassNode source=new ClassNode();try(InputStream in=zip.getInputStream(zip.getEntry("Config.class"))){new ClassReader(in).accept(source,0);}
      String owner="ru/givler/mbo/client/texture/E7RandomReference";ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(V1_8,ACC_PUBLIC,owner,null,"java/lang/Object",null);
      for(MethodNode method:source.methods)if((method.name.equals("intHash") && method.desc.equals("(I)I")) || (method.name.equals("getRandom") && method.desc.equals("(IIII)I"))) {
        for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals("Config"))((MethodInsnNode)i).owner=owner;
        method.accept(writer);
      }
      writer.visitEnd();Method random=new Loader().define(writer.toByteArray()).getMethod("getRandom",int.class,int.class,int.class,int.class);
      for(int x=-32;x<=32;x++)for(int y:new int[]{-64,-1,0,63,255})for(int side=0;side<6;side++)
        if((Integer)random.invoke(null,x,y,17,side)!=NaturalTextureHooks.random(x,y,17,side))throw new AssertionError("E7 seed mismatch");
      System.out.println("OptiFine E7 actual bytecode oracle: 1950 coordinate/face seeds identical");
    }
  }
  static void verifyPack()throws Exception {
    String path=System.getProperty("ctmPack");if(path==null)return;
    try(ZipFile zip=new ZipFile(path)) {
      int ctm=0,random=0,vertical=0,natural=0;
      for(Enumeration<? extends ZipEntry> e=zip.entries();e.hasMoreElements();) {
        ZipEntry entry=e.nextElement();if(!entry.getName().contains("/ctm/") || !entry.getName().endsWith(".properties"))continue;
        Properties properties=new Properties();try(InputStream in=zip.getInputStream(entry)){properties.load(in);}
        switch(properties.getProperty("method","ctm").trim()){case "random":random++;break;case "vertical":vertical++;break;case "ctm":ctm++;break;default:throw new AssertionError("Unexpected pack method");}
      }
      ZipEntry entry=zip.getEntry("assets/minecraft/optifine/natural.properties");
      if(entry!=null)try(Reader reader=new InputStreamReader(zip.getInputStream(entry),"UTF-8")){natural=NaturalTextureHooks.parse(reader).size();}
      if(random!=45 || ctm!=4 || vertical!=3 || natural==0)throw new AssertionError("Unexpected pack inventory");
      System.out.println("Unmodified run resource pack: 45 random + 4 CTM + 3 vertical rules; "+natural+" natural texture entries parsed");
    }
  }
}
