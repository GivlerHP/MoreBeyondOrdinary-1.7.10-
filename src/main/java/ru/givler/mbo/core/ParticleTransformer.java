package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Reuses the normal particle dispatch/batch, including particles with custom renderers. */
public final class ParticleTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/particle/ParticleRenderHooks";
  private static final String API="ru/givler/mbo/client/particle/ParticleGeometry";
  private static final String LIGHT="ru/givler/mbo/client/particle/ParticleBrightness";
  @Override public byte[] transform(String name,String transformedName,byte[] bytes) {
    if(bytes==null) return null;
    int target="net.minecraft.client.particle.EntityFX".equals(transformedName)?1:
        "net.minecraft.client.particle.EntityDiggingFX".equals(transformedName)?2:
        "net.minecraft.client.renderer.Tessellator".equals(transformedName)?3:
        "net.minecraft.client.particle.EffectRenderer".equals(transformedName)?4:0;
    if(target==0) return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode method:node.methods) {
      if(method.name.startsWith("mbo$particle")) return bytes;
      for(AbstractInsnNode ins:method.instructions.toArray())
        if(ins instanceof MethodInsnNode && HOOK.equals(((MethodInsnNode)ins).owner)) return bytes;
    }
    boolean patched=target==1?geometry(node):target==2?source(node):target==3?tessellator(node):dispatch(node);
    if(!patched) { System.err.println("[MBO] Particle hooks unavailable in "+transformedName);return bytes; }
    ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
  }
  private static FieldNode field(ClassNode node,String mcp,String srg) {
    for(FieldNode field:node.fields) {
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(node.name,field.name,field.desc);
      if(field.name.equals(mcp) || field.name.equals(srg) || mapped.equals(srg)) return field;
    }
    return null;
  }
  private static void getter(ClassNode node,FieldNode field,String name,String type) {
    MethodNode getter=new MethodNode(ACC_PUBLIC|ACC_SYNTHETIC,name,"()"+type,null,null);
    getter.instructions.add(new VarInsnNode(ALOAD,0));
    getter.instructions.add(new FieldInsnNode(GETFIELD,node.name,field.name,field.desc));
    getter.instructions.add(new InsnNode("F".equals(type)?FRETURN:"I".equals(type)?IRETURN:ARETURN));
    node.methods.add(getter);
  }
  private static boolean geometry(ClassNode node) {
    String[][] fields={
      {"particleIcon","field_70550_a","Icon","Lnet/minecraft/util/IIcon;"},
      {"particleScale","field_70544_f","Scale","F"},
      {"particleTextureJitterX","field_70548_b","JitterX","F"},
      {"particleTextureJitterY","field_70549_c","JitterY","F"},
      {"particleAlpha","field_82339_as","Alpha","F"},
      {"particleTextureIndexX","field_94054_b","TextureX","I"},
      {"particleTextureIndexY","field_94055_c","TextureY","I"}};
    for(String[] value:fields) if(field(node,value[0],value[1])==null) return false;
    node.interfaces.add(API);
    for(String[] value:fields) getter(node,field(node,value[0],value[1]),"mbo$particle"+value[2],value[3]);
    MethodNode source=new MethodNode(ACC_PUBLIC|ACC_SYNTHETIC,"mbo$particleBlock","()Lnet/minecraft/block/Block;",null,null);
    source.instructions.add(new InsnNode(ACONST_NULL));source.instructions.add(new InsnNode(ARETURN));
    node.methods.add(source);return true;
  }
  private static boolean source(ClassNode node) {
    FieldNode source=field(node,"field_145784_a","field_145784_a");
    if(source==null) return false;
    getter(node,source,"mbo$particleBlock","Lnet/minecraft/block/Block;");return true;
  }
  private static boolean tessellator(ClassNode node) {
    FieldNode brightness=field(node,"brightness","field_78401_l"),color=field(node,"color","field_78402_m");
    if(brightness==null || color==null) return false;
    int patched=0;
    for(MethodNode method:node.methods) {
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
      if(!method.desc.equals("(IIII)V") || !(method.name.equals("setColorRGBA") || method.name.equals("func_78370_a") || mapped.equals("func_78370_a"))) continue;
      for(AbstractInsnNode ins:method.instructions.toArray()) {
        if(!(ins instanceof FieldInsnNode) || ins.getOpcode()!=PUTFIELD) continue;
        FieldInsnNode write=(FieldInsnNode)ins;
        if(!write.owner.equals(node.name) || !write.name.equals(color.name)) continue;
        method.instructions.insertBefore(ins,new MethodInsnNode(INVOKESTATIC,HOOK,"tint","(I)I",false));patched++;
      }
    }
    if(patched==0) return false;
    node.interfaces.add(LIGHT);getter(node,brightness,"mbo$particleBrightness","I");return true;
  }
  private static boolean dispatch(ClassNode node) {
    int patched=0;
    for(MethodNode method:node.methods) {
      int calls=0;
      for(AbstractInsnNode ins:method.instructions.toArray()) {
        if(!(ins instanceof MethodInsnNode) || ins.getOpcode()!=INVOKEVIRTUAL) continue;
        MethodInsnNode call=(MethodInsnNode)ins;
        String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
        String desc=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc);
        if(!desc.equals("(Lnet/minecraft/client/renderer/Tessellator;FFFFFF)V")
            || !(call.name.equals("renderParticle") || call.name.equals("func_70539_a") || mapped.equals("func_70539_a"))) continue;
        call.setOpcode(INVOKESTATIC);call.owner=HOOK;call.name="render";
        call.desc="(Lnet/minecraft/client/particle/EntityFX;Lnet/minecraft/client/renderer/Tessellator;FFFFFF)V";
        calls++;
      }
      if(calls>0) {
        method.instructions.insert(new MethodInsnNode(INVOKESTATIC,HOOK,"beginPass","()V",false));
        patched+=calls;
      }
    }
    return patched>=2;
  }
}
