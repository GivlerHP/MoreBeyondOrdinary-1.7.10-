package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WaterloggingNeighborTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.world.World";
  private static final String HOOKS = "ru/givler/mbo/core/WaterloggingFlowHooks";
  private static final String NOTIFY_DESC = "(IIILnet/minecraft/block/Block;)V";
  private static final String DIRECTIONAL_NOTIFY_DESC = "(IIILnet/minecraft/block/Block;I)V";
  private static final String METADATA_DESC = "(IIIII)Z";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    boolean notifyPatched = false;
    boolean metadataPatched = false;
    for (MethodNode method : node.methods) {
      String mappedName =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      String mappedDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if ((NOTIFY_DESC.equals(mappedDesc) || DIRECTIONAL_NOTIFY_DESC.equals(mappedDesc))
          && ("notifyBlocksOfNeighborChange".equals(method.name)
              || "func_147459_d".equals(method.name)
              || "notifyBlocksOfNeighborChange".equals(mappedName)
              || "func_147459_d".equals(mappedName))) {
        InsnList hook = coordinatesHook("onNeighborNotification", "(Lnet/minecraft/world/World;III)V");
        method.instructions.insert(hook);
        notifyPatched = true;
      } else if (METADATA_DESC.equals(mappedDesc)
          && ("setBlockMetadataWithNotify".equals(method.name)
              || "func_72921_c".equals(method.name)
              || "setBlockMetadataWithNotify".equals(mappedName)
              || "func_72921_c".equals(mappedName))) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
            instruction != null;
            instruction = instruction.getNext()) {
          if (instruction.getOpcode() != IRETURN) continue;
          InsnList hook = new InsnList();
          hook.add(new InsnNode(DUP));
          hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(new VarInsnNode(ILOAD, 1));
          hook.add(new VarInsnNode(ILOAD, 2));
          hook.add(new VarInsnNode(ILOAD, 3));
          hook.add(
              new MethodInsnNode(
                  INVOKESTATIC,
                  HOOKS,
                  "onMetadataChanged",
                  "(ZLnet/minecraft/world/World;III)V",
                  false));
          method.instructions.insertBefore(instruction, hook);
        }
        metadataPatched = true;
      }
    }
    if (!notifyPatched || !metadataPatched) {
      System.err.println(
          "[MBO ASM] Waterlogging neighbor patch incomplete: notify="
              + notifyPatched
              + ", metadata="
              + metadataPatched);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    System.out.println("[MBO ASM] Patched waterlogged outlet neighbor updates");
    return writer.toByteArray();
  }

  private static InsnList coordinatesHook(String method, String descriptor) {
    InsnList hook = new InsnList();
    hook.add(new VarInsnNode(ALOAD, 0));
    hook.add(new VarInsnNode(ILOAD, 1));
    hook.add(new VarInsnNode(ILOAD, 2));
    hook.add(new VarInsnNode(ILOAD, 3));
    hook.add(new MethodInsnNode(INVOKESTATIC, HOOKS, method, descriptor, false));
    return hook;
  }
}
