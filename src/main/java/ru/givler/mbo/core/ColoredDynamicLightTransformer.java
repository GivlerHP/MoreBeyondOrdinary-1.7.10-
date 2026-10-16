package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Combine OF returns and MF2's helper: its early return is inserted after MBO in some installations. */
public final class ColoredDynamicLightTransformer implements IClassTransformer, Opcodes {
  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes != null && transformedName.startsWith("com.falsepattern.falsetweaks.modules.dynlights.base."))
      return integrateFalseTweaks(transformedName, bytes);
    boolean mf2 = "mods.battlegear2.client.utils.DynamicLightHook".equals(transformedName);
    if (bytes == null || (!mf2 && !"DynamicLights".equals(transformedName))) return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods) {
      if (!method.name.equals(mf2 ? "getExtraLightLevel" : "getLightLevel") || !FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(Lnet/minecraft/entity/Entity;)I")) continue;
      for (AbstractInsnNode instruction : method.instructions.toArray()) {
        if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).owner.equals("ru/givler/mbo/client/lighting/ColoredLightAuto")) return bytes;
        if (instruction.getOpcode() != IRETURN) continue;
        InsnList list = new InsnList(); list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/lighting/ColoredLightAuto", "optifineLevel", "(ILnet/minecraft/entity/Entity;)I", false));
        method.instructions.insertBefore(instruction, list); patched++;
      }
    }
    if (patched == 0) { System.err.println("[MBO ASM] OptiFine colored dynamic light hook unavailable"); return bytes; }
    ClassWriter writer = SafeClassWriter.create(); node.accept(writer); return writer.toByteArray();
  }
  private static final String BRIDGE="ru/givler/mbo/client/lighting/ColoredDynamicLightBridge";
  private byte[] integrateFalseTweaks(String name, byte[] bytes) {
    boolean manager=name.endsWith(".DynamicLights"), source=name.endsWith(".DynamicLight");
    if(!manager && !source)return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode method:node.methods)if(method.name.equals("mbo$unifiedDynamicLights"))return bytes;
    int marks=0,syncs=0,levels=0;
    for(MethodNode method:node.methods) {
      String desc=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      for(AbstractInsnNode instruction:method.instructions.toArray()) {
        if(instruction instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)instruction;
          String owner=FMLDeobfuscatingRemapper.INSTANCE.map(call.owner);
          String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
          if(source && owner.equals("net/minecraft/client/renderer/RenderGlobal") && call.desc.equals("(IIIIII)V")
              && (call.name.equals("markBlockRangeForRenderUpdate") || call.name.equals("func_147585_a") || mapped.equals("func_147585_a"))) {
            method.instructions.set(call,new MethodInsnNode(INVOKESTATIC,BRIDGE,"mark","(Lnet/minecraft/client/renderer/RenderGlobal;IIIIII)V",false));marks++;
          }
        }
        if(manager && instruction.getOpcode()==RETURN &&
            (method.name.equals("update") || method.name.equals("entityRemoved") || method.name.equals("removeLights") || method.name.equals("clear"))) {
          method.instructions.insertBefore(instruction,new MethodInsnNode(INVOKESTATIC,BRIDGE,method.name.equals("update")?"sync":"changed","()V",false));syncs++;
        }
        if(manager && method.name.equals("getLightLevel") && desc.equals("(Lnet/minecraft/entity/Entity;)I") && instruction.getOpcode()==IRETURN) {
          InsnList hook=new InsnList();hook.add(new VarInsnNode(ALOAD,1));
          hook.add(new MethodInsnNode(INVOKESTATIC,BRIDGE,"level","(ILnet/minecraft/entity/Entity;)I",false));
          method.instructions.insertBefore(instruction,hook);levels++;
        }
      }
    }
    if((source && marks!=2) || (manager && (syncs<4 || levels==0))) {
      System.err.println("[MBO] Unsupported FalseTweaks dynamic-light bytecode: "+name);return bytes;
    }
    MethodNode marker=new MethodNode(ACC_PUBLIC|ACC_STATIC|ACC_SYNTHETIC,"mbo$unifiedDynamicLights","()V",null,null);
    marker.instructions.add(new InsnNode(RETURN));node.methods.add(marker);
    // Hooks do not alter control flow; retain FT's precise existing stack-map types.
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
}
