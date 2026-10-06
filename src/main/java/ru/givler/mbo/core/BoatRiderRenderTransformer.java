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
          && "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V".equals(method.desc)) {
        for (AbstractInsnNode insn : method.instructions.toArray()) {
          if (insn instanceof VarInsnNode && insn.getOpcode() == FLOAD && ((VarInsnNode) insn).var == 10) {
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 1));
            hook.add(new VarInsnNode(FLOAD, 10));
            hook.add(new VarInsnNode(FLOAD, 9));
            hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "bodyYaw", "(Lnet/minecraft/entity/EntityLivingBase;FF)F", false));
            method.instructions.insertBefore(insn, hook); method.instructions.remove(insn); changes++;
          }
        }
      }
      if (model && ("setRotationAngles".equals(mapped) || "func_78087_a".equals(mapped))
          && "(FFFFFFLnet/minecraft/entity/Entity;)V".equals(method.desc)) {
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
}
