package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class SkeletonPoseTransformer implements IClassTransformer, Opcodes {
  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    boolean entity = "net.minecraft.entity.monster.EntitySkeleton".equals(transformedName);
    boolean skeleton = "net.minecraft.client.model.ModelSkeleton".equals(transformedName);
    boolean biped = "net.minecraft.client.model.ModelBiped".equals(transformedName);
    boolean zombie = "net.minecraft.client.model.ModelZombie".equals(transformedName);
    if (bytes == null || !(entity || skeleton || biped || zombie)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int changes = 0;
    for (MethodNode method : node.methods) {
      String mapped =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (entity && ("entityInit".equals(mapped) || "func_70088_a".equals(mapped))) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
          if (instruction.getOpcode() != RETURN) continue;
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(
              new MethodInsnNode(
                  INVOKESTATIC,
                  "ru/givler/mbo/entity/ai/SkeletonCombatState",
                  "init",
                  "(Lnet/minecraft/entity/monster/EntitySkeleton;)V",
                  false));
          method.instructions.insertBefore(instruction, hook);
          changes++;
        }
      } else if ((skeleton || biped || zombie)
          && ("setRotationAngles".equals(mapped) || "func_78087_a".equals(mapped))
          && "(FFFFFFLnet/minecraft/entity/Entity;)V".equals(method.desc)) {
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(ALOAD, 0));
        hook.add(new VarInsnNode(ALOAD, 7));
        hook.add(
            new MethodInsnNode(
                INVOKESTATIC,
                "ru/givler/mbo/client/render/SkeletonRenderHooks",
                "prepare",
                "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)Z",
                false));
        if (zombie) {
          LabelNode combat = new LabelNode();
          hook.add(new JumpInsnNode(IFNE, combat));
          hook.add(new VarInsnNode(ALOAD, 0));
          for (int index = 1; index <= 6; index++) hook.add(new VarInsnNode(FLOAD, index));
          hook.add(new VarInsnNode(ALOAD, 7));
          hook.add(
              new MethodInsnNode(INVOKESPECIAL, node.superName, method.name, method.desc, false));
          hook.add(new InsnNode(RETURN));
          hook.add(combat);
          hook.add(new FrameNode(F_SAME, 0, null, 0, null));
        } else hook.add(new InsnNode(POP));
        method.instructions.insert(hook);
        changes++;
      }
    }
    if (changes == 0)
      throw new IllegalStateException("Skeleton pose anchor missing: " + transformedName);
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    node.accept(writer);
    return writer.toByteArray();
  }
}
