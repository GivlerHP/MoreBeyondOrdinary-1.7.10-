package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class BoatRiderRenderTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK = "ru/givler/mbo/client/render/BoatRiderRenderHooks";
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    boolean render = "net.minecraft.client.renderer.entity.RendererLivingEntity".equals(transformedName);
    boolean model = "net.minecraft.client.model.ModelBiped".equals(transformedName);
    boolean entity = "net.minecraft.entity.Entity".equals(transformedName);
    if (bytes == null || !render && !model && !entity) return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    int changes = 0;
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (entity && ("setAngles".equals(mapped) || "func_70082_c".equals(mapped)) && "(FF)V".equals(method.desc)) {
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn.getOpcode() == RETURN) {
          InsnList hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/entity/boat/MBOBoatRiderView", "clampView", "(Lnet/minecraft/entity/Entity;)V", false));
          method.instructions.insertBefore(insn, hook); changes++;
        }
      }
      if (render && ("doRender".equals(mapped) || "func_76986_a".equals(mapped))
          && "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V".equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc))) {
        int yaw = -1, partial = -1;
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn instanceof MethodInsnNode) {
          MethodInsnNode call = (MethodInsnNode) insn;
          String callName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
          if (!(callName.equals("rotateCorpse") || callName.equals("func_77043_a"))) continue;
          AbstractInsnNode p = previousCode(call), y = previousCode(p);
          if (p instanceof VarInsnNode && p.getOpcode() == FLOAD && y instanceof VarInsnNode && y.getOpcode() == FLOAD) {
            yaw = ((VarInsnNode) y).var; partial = ((VarInsnNode) p).var; break;
          }
        }
        boolean bodyStarted = false;
        for (AbstractInsnNode insn : method.instructions.toArray()) {
          if (insn instanceof VarInsnNode && insn.getOpcode() == FSTORE && ((VarInsnNode) insn).var == yaw) {
            AbstractInsnNode previous = previousCode(insn);
            if (previous instanceof MethodInsnNode && ((MethodInsnNode) previous).desc.equals("(FFF)F")) bodyStarted = true;
          }
          if (bodyStarted && insn instanceof VarInsnNode && insn.getOpcode() == FLOAD && ((VarInsnNode) insn).var == yaw) {
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 1));
            hook.add(new VarInsnNode(FLOAD, yaw));
            hook.add(new VarInsnNode(FLOAD, partial));
            hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "bodyYaw", "(Lnet/minecraft/entity/EntityLivingBase;FF)F", false));
            method.instructions.insertBefore(insn, hook); method.instructions.remove(insn); changes++;
          }
        }
      }
      if (model && ("setRotationAngles".equals(mapped) || "func_78087_a".equals(mapped))
          && "(FFFFFFLnet/minecraft/entity/Entity;)V".equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc))) {
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn.getOpcode() == RETURN) {
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new VarInsnNode(ALOAD, 7));
          hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "pose", "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V", false));
          method.instructions.insertBefore(insn, hook); changes++;
        }
      }
    }
    if (changes == 0) throw new IllegalStateException("MBO boat rider render anchors missing: " + transformedName);
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray();
  }
  private static AbstractInsnNode previousCode(AbstractInsnNode insn) {
    if (insn == null) return null;
    do { insn = insn.getPrevious(); } while (insn != null && insn.getOpcode() < 0);
    return insn;
  }
}
