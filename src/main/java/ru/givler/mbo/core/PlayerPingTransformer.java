package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PlayerPingTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraftforge.client.GuiIngameForge";
  private static final String PLAYER_INFO = "net/minecraft/client/gui/GuiPlayerInfo";
  private static final String HOOKS = "ru/givler/mbo/client/render/PlayerPingRenderer";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    for (MethodNode method : node.methods) {
      if (!"renderPlayerList".equals(method.name) || !"(II)V".equals(method.desc)) continue;
      boolean playerCaptured = false;
      MethodInsnNode pingIconCall = null;
      for (AbstractInsnNode instruction = method.instructions.getFirst();
          instruction != null;
          instruction = instruction.getNext()) {
        if (instruction instanceof TypeInsnNode
            && instruction.getOpcode() == CHECKCAST
            && PLAYER_INFO.equals(((TypeInsnNode) instruction).desc)
            && instruction.getNext() instanceof VarInsnNode
            && instruction.getNext().getOpcode() == ASTORE) {
          VarInsnNode store = (VarInsnNode) instruction.getNext();
          InsnList capture = new InsnList();
          capture.add(new VarInsnNode(ALOAD, store.var));
          capture.add(
              new MethodInsnNode(
                  INVOKESTATIC, HOOKS, "setCurrentPlayer", "(L" + PLAYER_INFO + ";)V", false));
          method.instructions.insert(store, capture);
          playerCaptured = true;
        }
        if (instruction instanceof MethodInsnNode) {
          MethodInsnNode call = (MethodInsnNode) instruction;
          if (("drawTexturedModalRect".equals(call.name) || "func_73729_b".equals(call.name))
              && "(IIIIII)V".equals(call.desc)) pingIconCall = call;
        }
      }
      if (playerCaptured && pingIconCall != null) {
        pingIconCall.setOpcode(INVOKESTATIC);
        pingIconCall.owner = HOOKS;
        pingIconCall.name = "renderPing";
        pingIconCall.desc = "(Lnet/minecraftforge/client/GuiIngameForge;IIIIII)V";
        pingIconCall.itf = false;
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        System.out.println("[MBO ASM] Installed numeric ping display in the player list");
        return writer.toByteArray();
      }
    }
    System.err.println("[MBO ASM] GuiIngameForge player-list ping draw was not found");
    return bytes;
  }
}
