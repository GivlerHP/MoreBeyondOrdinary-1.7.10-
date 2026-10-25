package ru.givler.mbo.integration.angelica.core;

import java.util.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Module selection happens at launch; rendering and resource loading must read live settings. */
public final class AngelicaPackSettingsTransformer implements IClassTransformer, Opcodes {
  private static final String CONFIG="jss/notfine/config/MCPatcherForgeConfig$";
  private static final String MASTER="com/gtnewhorizons/angelica/config/AngelicaConfig";
  private static final String MARKER="mbo$livePackSettings";
  private static final Map<String,Map<String,FieldInsnNode>> SOURCES=new java.util.concurrent.ConcurrentHashMap<>();
  private static Map<String,FieldInsnNode> sources(ClassNode node) {
    Map<String,FieldInsnNode> cached=new HashMap<>();
    for(MethodNode m:node.methods)if(m.name.equals("<clinit>"))
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof FieldInsnNode&&i.getOpcode()==PUTSTATIC) {
        AbstractInsnNode previous=i.getPrevious();while(previous!=null&&previous.getOpcode()<0)previous=previous.getPrevious();
        if(previous instanceof FieldInsnNode) {
          FieldInsnNode read=(FieldInsnNode)previous,write=(FieldInsnNode)i;
          if(read.getOpcode()==GETSTATIC&&read.desc.equals("Z")&&read.owner.startsWith(CONFIG))cached.put(write.name,read);
        }
      }
    return cached;
  }
  private static Map<String,FieldInsnNode> sources(String owner) {
    Map<String,FieldInsnNode> result=SOURCES.get(owner);if(result!=null)return result;
    try(java.io.InputStream in=AngelicaPackSettingsTransformer.class.getClassLoader().getResourceAsStream(owner+".class")) {
      if(in==null)return Collections.emptyMap();
      ClassNode node=new ClassNode();new ClassReader(in).accept(node,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);result=sources(node);SOURCES.put(owner,result);return result;
    }catch(java.io.IOException error){throw new IllegalStateException("Cannot inspect native pack settings: "+owner,error);}
  }
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null||!target.startsWith("com.prupe.mcpatcher."))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(FieldNode f:node.fields)if(f.name.equals(MARKER))return bytes;
    Map<String,FieldInsnNode> cached=sources(node);SOURCES.put(node.name,cached);
    boolean changed=false;
    for(MethodNode m:node.methods) {
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof FieldInsnNode&&i.getOpcode()==GETSTATIC) {
        FieldInsnNode f=(FieldInsnNode)i;
        Map<String,FieldInsnNode> mapping=f.owner.equals(node.name)?cached:f.owner.startsWith("com/prupe/mcpatcher/")?sources(f.owner):Collections.emptyMap();
        if(mapping.containsKey(f.name)) {
          FieldInsnNode source=mapping.get(f.name);f.owner=source.owner;f.name=source.name;f.desc=source.desc;changed=true;
        }
        if(f.desc.equals("Z")&&f.owner.startsWith(CONFIG)) {
          String module=f.owner.substring(CONFIG.length());
          if(module.equals("ConnectedTextures")||module.equals("BetterSkies")||module.equals("CustomColors")||module.equals("RandomMobs")) {
            InsnList gate=new InsnList();
            if(!f.name.equals("enabled")){gate.add(new FieldInsnNode(GETSTATIC,f.owner,"enabled","Z"));gate.add(new InsnNode(IAND));}
            gate.add(new FieldInsnNode(GETSTATIC,MASTER,"enableMCPatcherForgeFeatures","Z"));gate.add(new InsnNode(IAND));
            m.instructions.insert(i,gate);changed=true;
          }
        }
      }
      if(node.name.equals("com/prupe/mcpatcher/ctm/CTMUtils")&&m.name.startsWith("getBlockIcon")&&cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(m.desc).startsWith("(Lnet/minecraft/util/IIcon;")) {
        LabelNode enabled=new LabelNode();InsnList gate=new InsnList();gate.add(new FieldInsnNode(GETSTATIC,CONFIG+"ConnectedTextures","enabled","Z"));
        gate.add(new FieldInsnNode(GETSTATIC,MASTER,"enableMCPatcherForgeFeatures","Z"));gate.add(new InsnNode(IAND));gate.add(new JumpInsnNode(IFNE,enabled));
        gate.add(new VarInsnNode(ALOAD,0));gate.add(new InsnNode(ARETURN));gate.add(enabled);gate.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(gate);changed=true;
      }
      if(node.name.equals("com/prupe/mcpatcher/sky/SkyRenderer")&&m.name.equals("setup")) {
        for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==RETURN) {
          InsnList gate=new InsnList();gate.add(new FieldInsnNode(GETSTATIC,node.name,"active","Z"));
          gate.add(new FieldInsnNode(GETSTATIC,CONFIG+"BetterSkies","enabled","Z"));gate.add(new InsnNode(IAND));
          gate.add(new FieldInsnNode(GETSTATIC,MASTER,"enableMCPatcherForgeFeatures","Z"));gate.add(new InsnNode(IAND));
          gate.add(new MethodInsnNode(INVOKESTATIC,"ru/givler/mbo/integration/angelica/client/AngelicaSkyHooks","enabled","()Z",false));gate.add(new InsnNode(IAND));
          gate.add(new FieldInsnNode(PUTSTATIC,node.name,"active","Z"));m.instructions.insertBefore(i,gate);changed=true;
        }
      }
    }
    if(!changed)return bytes;
    node.fields.add(new FieldNode(ACC_PRIVATE|ACC_STATIC|ACC_FINAL|ACC_SYNTHETIC,MARKER,"Z",null,1));
    // Preserve native frame types; the added boolean expressions do not change locals.
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);
    node.accept(writer);return writer.toByteArray();
  }
}
