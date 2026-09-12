package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class PaneTransformer implements IClassTransformer, Opcodes {
  private static final String PANE = "net.minecraft.block.BlockPane";
  private static final String RENDER_BLOCKS = "net.minecraft.client.renderer.RenderBlocks";
  private static final String PANE_HOOK = "ru/givler/mbo/core/PaneConnectionHooks";
  private static final String RENDER_HOOK = "ru/givler/mbo/core/PaneRenderHooks";
  private static final String FORGE_DIRECTION = "net/minecraftforge/common/util/ForgeDirection";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null) return null;
    if (PANE.equals(transformedName)) return patchPane(bytes);
    if (RENDER_BLOCKS.equals(transformedName)) return patchRenderer(bytes);
    return bytes;
  }

  private byte[] patchPane(byte[] bytes) {
    ClassNode node = read(bytes);
    int patched = 0;
    for (MethodNode method : node.methods) {
      String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if ("(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)Z"
              .equals(desc)
          && mappedName(node, method, "canPaneConnectTo", "canPaneConnectTo")) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        InsnList code = method.instructions;
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ALOAD, 5));
        code.add(
            new MethodInsnNode(
                INVOKESTATIC,
                PANE_HOOK,
                "canConnect",
                "(Lnet/minecraft/block/BlockPane;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)Z",
                false));
        code.add(new InsnNode(IRETURN));
        patched++;
      } else if ("(Lnet/minecraft/world/IBlockAccess;III)V".equals(desc)
          && mappedName(node, method, "setBlockBoundsBasedOnState", "func_149719_a")) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(
            new MethodInsnNode(
                INVOKESTATIC,
                PANE_HOOK,
                "setIsolatedBounds",
                "(Lnet/minecraft/block/BlockPane;Lnet/minecraft/world/IBlockAccess;III)Z",
                false));
        LabelNode vanilla = new LabelNode();
        code.add(new JumpInsnNode(IFEQ, vanilla));
        code.add(new InsnNode(RETURN));
        code.add(vanilla);
        method.instructions.insert(code);
        patched++;
      } else if ("(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V"
              .equals(desc)
          && mappedName(node, method, "addCollisionBoxesToList", "func_149743_a")) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ALOAD, 5));
        code.add(new VarInsnNode(ALOAD, 6));
        code.add(
            new MethodInsnNode(
                INVOKESTATIC,
                PANE_HOOK,
                "addIsolatedCollision",
                "(Lnet/minecraft/block/BlockPane;Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;)Z",
                false));
        LabelNode vanilla = new LabelNode();
        code.add(new JumpInsnNode(IFEQ, vanilla));
        code.add(new InsnNode(RETURN));
        code.add(vanilla);
        method.instructions.insert(code);
        patched++;
      }
    }
    if (patched != 3) {
      System.err.println("[MBO ASM] Incomplete BlockPane patch: " + patched + "/3 methods");
      return bytes;
    }
    System.out.println("[MBO ASM] Patched pane connections and isolated geometry");
    return write(node);
  }

  private byte[] patchRenderer(byte[] bytes) {
    ClassNode node = read(bytes);
    int patched = 0;
    int connectionCalls = 0;
    for (MethodNode method : node.methods) {
      String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      boolean regular =
          "(Lnet/minecraft/block/BlockPane;III)Z".equals(desc)
              && mappedName(node, method, "renderBlockPane", "func_147767_a");
      boolean stained =
          "(Lnet/minecraft/block/Block;III)Z".equals(desc)
              && mappedName(node, method, "renderBlockStainedGlassPane", "func_147733_k");
      if (!regular && !stained) continue;
      for (AbstractInsnNode instruction = method.instructions.getFirst();
          instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        if (isPaneConnectionCall(call)) {
          call.setOpcode(INVOKESTATIC);
          call.owner = PANE_HOOK;
          call.name = "canConnect";
          call.desc = "(Lnet/minecraft/block/BlockPane;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)Z";
          call.itf = false;
          connectionCalls++;
        } else if (isInlinePaneConnectionCall(call)) {
          MethodInsnNode getBlock = previousMethodCall(call);
          String direction = getBlock == null ? null : inlineDirection(getBlock);
          if (getBlock != null && isGetBlockCall(getBlock) && direction != null) {
            method.instructions.insertBefore(
                getBlock,
                new FieldInsnNode(
                    GETSTATIC,
                    FORGE_DIRECTION,
                    direction,
                    "L" + FORGE_DIRECTION + ";"));
            getBlock.setOpcode(INVOKESTATIC);
            getBlock.owner = PANE_HOOK;
            getBlock.name = "canConnect";
            getBlock.desc =
                "(Lnet/minecraft/block/BlockPane;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)Z";
            getBlock.itf = false;
            method.instructions.remove(call);
            instruction = getBlock;
            connectionCalls++;
          }
        } else if (call.desc.endsWith(")Z")
            && (call.owner.toLowerCase().contains("pane")
                || call.name.toLowerCase().contains("connect")
                || call.desc.contains("ForgeDirection"))) {
          System.out.println("[MBO ASM] Pane-call candidate opcode=" + call.getOpcode()
              + " " + call.owner + "." + call.name + call.desc);
        }
      }
      InsnList code = new InsnList();
      code.add(new VarInsnNode(ALOAD, 0));
      code.add(new VarInsnNode(ALOAD, 1));
      if (stained) code.add(new TypeInsnNode(CHECKCAST, "net/minecraft/block/BlockPane"));
      code.add(new VarInsnNode(ILOAD, 2));
      code.add(new VarInsnNode(ILOAD, 3));
      code.add(new VarInsnNode(ILOAD, 4));
      code.add(
          new MethodInsnNode(
              INVOKESTATIC,
              RENDER_HOOK,
              "renderIsolated",
              "(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/BlockPane;III)Z",
              false));
      LabelNode vanilla = new LabelNode();
      code.add(new JumpInsnNode(IFEQ, vanilla));
      code.add(new InsnNode(ICONST_1));
      code.add(new InsnNode(IRETURN));
      code.add(vanilla);
      method.instructions.insert(code);
      patched++;
    }
    if (patched != 2) {
      System.err.println("[MBO ASM] Incomplete pane renderer patch: " + patched + "/2 methods");
      return bytes;
    }
    System.out.println("[MBO ASM] Patched isolated pane rendering and "
        + connectionCalls + " direct connection calls");
    return write(node);
  }

  private static boolean isPaneConnectionCall(MethodInsnNode call) {
    if (call.getOpcode() != INVOKEVIRTUAL) return false;
    Type[] args = Type.getArgumentTypes(call.desc);
    if (args.length != 5 || Type.getReturnType(call.desc).getSort() != Type.BOOLEAN) return false;
    if (args[1].getSort() != Type.INT
        || args[2].getSort() != Type.INT
        || args[3].getSort() != Type.INT) return false;
    return args[4].getSort() == Type.OBJECT
        && "net/minecraftforge/common/util/ForgeDirection".equals(args[4].getInternalName());
  }

  /** OptiFine E7 inlines BlockPane.canPaneConnectTo as getBlock + canPaneConnectToBlock. */
  private static boolean isInlinePaneConnectionCall(MethodInsnNode call) {
    String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc);
    if (!"(Lnet/minecraft/block/Block;)Z".equals(desc)) return false;
    String name =
        FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner, call.name, call.desc);
    return "canPaneConnectToBlock".equals(call.name)
        || "func_150098_a".equals(call.name)
        || "canPaneConnectToBlock".equals(name)
        || "func_150098_a".equals(name);
  }

  private static MethodInsnNode previousMethodCall(AbstractInsnNode instruction) {
    for (AbstractInsnNode previous = instruction.getPrevious();
        previous != null;
        previous = previous.getPrevious()) {
      if (previous instanceof MethodInsnNode) return (MethodInsnNode) previous;
      if (previous.getOpcode() >= 0) return null;
    }
    return null;
  }

  private static boolean isGetBlockCall(MethodInsnNode call) {
    String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc);
    if (!"(III)Lnet/minecraft/block/Block;".equals(desc)) return false;
    String name =
        FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner, call.name, call.desc);
    return "getBlock".equals(call.name)
        || "func_147439_a".equals(call.name)
        || "getBlock".equals(name)
        || "func_147439_a".equals(name);
  }

  private static String inlineDirection(MethodInsnNode getBlock) {
    AbstractInsnNode cursor = previousCode(getBlock);
    for (int inspected = 0; cursor != null && inspected < 10; inspected++) {
      int opcode = cursor.getOpcode();
      if (opcode == IADD || opcode == ISUB) {
        AbstractInsnNode one = previousCode(cursor);
        AbstractInsnNode coordinate = previousCode(one);
        if (one != null
            && one.getOpcode() == ICONST_1
            && coordinate instanceof VarInsnNode
            && coordinate.getOpcode() == ILOAD) {
          int variable = ((VarInsnNode) coordinate).var;
          if (variable == 2) return opcode == ISUB ? "WEST" : "EAST";
          if (variable == 4) return opcode == ISUB ? "NORTH" : "SOUTH";
        }
      }
      cursor = previousCode(cursor);
    }
    return null;
  }

  private static AbstractInsnNode previousCode(AbstractInsnNode instruction) {
    AbstractInsnNode previous = instruction == null ? null : instruction.getPrevious();
    while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
    return previous;
  }

  private static boolean mappedName(ClassNode owner, MethodNode method, String mcp, String srg) {
    String mapped =
        FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner.name, method.name, method.desc);
    return mcp.equals(method.name)
        || srg.equals(method.name)
        || mcp.equals(mapped)
        || srg.equals(mapped);
  }

  private static ClassNode read(byte[] bytes) {
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    return node;
  }

  private static byte[] write(ClassNode node) {
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    return writer.toByteArray();
  }
}
