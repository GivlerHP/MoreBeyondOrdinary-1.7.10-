package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class CauldronTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.block.BlockCauldron";
  private static final String HOOKS = "ru/givler/mbo/core/CauldronHooks";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    boolean renderPatched = false;
    boolean blockPassPatched = false;
    boolean renderPassPatched = false;
    boolean collisionPatched = false;
    boolean levelSyncPatched = false;
    for (MethodNode method : node.methods) {
      String mappedName =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      String mappedDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if (mappedDesc.equals("()I")
          && method.name.equals("getRenderBlockPass")) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.instructions.add(new InsnNode(ICONST_1));
        method.instructions.add(new InsnNode(IRETURN));
        blockPassPatched = true;
      } else if (mappedDesc.equals("()I")
          && (method.name.equals("getRenderType")
              || method.name.equals("func_149645_b")
              || mappedName.equals("getRenderType")
              || mappedName.equals("func_149645_b"))) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.instructions.add(
            new MethodInsnNode(INVOKESTATIC, HOOKS, "getRenderType", "()I", false));
        method.instructions.add(new InsnNode(IRETURN));
        renderPatched = true;
      } else if (mappedDesc.equals("(I)Z") && method.name.equals("canRenderInPass")) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.instructions.add(new VarInsnNode(ILOAD, 1));
        method.instructions.add(
            new MethodInsnNode(INVOKESTATIC, HOOKS, "canRenderInPass", "(I)Z", false));
        method.instructions.add(new InsnNode(IRETURN));
        renderPassPatched = true;
      } else if (mappedDesc.equals(
              "(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V")
          && (method.name.equals("addCollisionBoxesToList")
              || method.name.equals("func_149743_a")
              || mappedName.equals("addCollisionBoxesToList")
              || mappedName.equals("func_149743_a"))) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ALOAD, 5));
        code.add(new VarInsnNode(ALOAD, 6));
        code.add(new VarInsnNode(ALOAD, 7));
        code.add(
            new MethodInsnNode(
                INVOKESTATIC,
                HOOKS,
                "addCollisionBoxes",
                "(Lnet/minecraft/block/BlockCauldron;Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V",
                false));
        code.add(new InsnNode(RETURN));
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.instructions.add(code);
        collisionPatched = true;
      } else if (mappedDesc.equals(
              "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z")
          && (method.name.equals("onBlockActivated")
              || method.name.equals("func_149727_a")
              || mappedName.equals("onBlockActivated")
              || mappedName.equals("func_149727_a"))) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ALOAD, 5));
        code.add(
            new MethodInsnNode(
                INVOKESTATIC,
                HOOKS,
                "handleActivation",
                "(Lnet/minecraft/block/BlockCauldron;Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;)I",
                false));
        int result = method.maxLocals++;
        code.add(new VarInsnNode(ISTORE, result));
        org.objectweb.asm.tree.LabelNode vanilla = new org.objectweb.asm.tree.LabelNode();
        code.add(new VarInsnNode(ILOAD, result));
        code.add(new InsnNode(ICONST_M1));
        code.add(new org.objectweb.asm.tree.JumpInsnNode(IF_ICMPEQ, vanilla));
        code.add(new VarInsnNode(ILOAD, result));
        code.add(new InsnNode(IRETURN));
        code.add(vanilla);
        method.instructions.insert(code);
      } else if (mappedDesc.equals("(Lnet/minecraft/world/World;IIII)V")
          && (method.name.equals("func_150024_a") || mappedName.equals("func_150024_a"))) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ILOAD, 5));
        code.add(
            new MethodInsnNode(
                INVOKESTATIC,
                HOOKS,
                "syncConnectedBeforeChange",
                "(Lnet/minecraft/world/World;IIII)V",
                false));
        method.instructions.insert(code);
        levelSyncPatched = true;
      } else if (mappedDesc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)V")
          && (method.name.equals("onEntityCollidedWithBlock")
              || method.name.equals("func_149670_a")
              || mappedName.equals("onEntityCollidedWithBlock")
              || mappedName.equals("func_149670_a"))) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ALOAD, 5));
        code.add(
            new MethodInsnNode(
                INVOKESTATIC,
                HOOKS,
                "handleCollision",
                "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)Z",
                false));
        org.objectweb.asm.tree.LabelNode vanilla = new org.objectweb.asm.tree.LabelNode();
        code.add(new org.objectweb.asm.tree.JumpInsnNode(IFEQ, vanilla));
        code.add(new InsnNode(RETURN));
        code.add(vanilla);
        method.instructions.insert(code);
      } else if (mappedDesc.equals("(I)I")
          && (method.name.equals("func_150027_b") || mappedName.equals("func_150027_b"))) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.instructions.add(new VarInsnNode(ILOAD, 0));
        method.instructions.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "getLevel", "(I)I", false));
        method.instructions.add(new InsnNode(IRETURN));
      }
    }
    if (!blockPassPatched) {
      MethodNode method = new MethodNode(ACC_PUBLIC, "getRenderBlockPass", "()I", null, null);
      method.instructions.add(new InsnNode(ICONST_1));
      method.instructions.add(new InsnNode(IRETURN));
      node.methods.add(method);
      blockPassPatched = true;
    }
    if (!renderPassPatched) {
      MethodNode method = new MethodNode(ACC_PUBLIC, "canRenderInPass", "(I)Z", null, null);
      method.instructions.add(new VarInsnNode(ILOAD, 1));
      method.instructions.add(
          new MethodInsnNode(INVOKESTATIC, HOOKS, "canRenderInPass", "(I)Z", false));
      method.instructions.add(new InsnNode(IRETURN));
      node.methods.add(method);
      renderPassPatched = true;
    }
    if (!renderPatched
        || !blockPassPatched
        || !renderPassPatched
        || !collisionPatched
        || !levelSyncPatched)
      System.err.println(
          "[MBO ASM] Incomplete BlockCauldron patch: render="
              + renderPatched
              + ", blockPass="
              + blockPassPatched
              + ", renderPass="
              + renderPassPatched
              + ", collision="
              + collisionPatched
              + ", levelSync="
              + levelSyncPatched);
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    return writer.toByteArray();
  }
}
