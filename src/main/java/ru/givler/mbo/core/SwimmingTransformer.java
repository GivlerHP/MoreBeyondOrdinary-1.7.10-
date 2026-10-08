package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Adds swimming at existing player/model hooks without replacing Battlegear's renderer. */
public final class SwimmingTransformer implements IClassTransformer, Opcodes {
  private static final String STATE = "ru/givler/mbo/swimming/SwimmingHooks";
  private static final String RENDER = "ru/givler/mbo/client/render/SwimmingRenderHooks";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    boolean player = "net.minecraft.entity.player.EntityPlayer".equals(transformedName);
    boolean living = "net.minecraft.entity.EntityLivingBase".equals(transformedName);
    boolean model = "net.minecraft.client.model.ModelBiped".equals(transformedName);
    boolean clientPlayer = "net.minecraft.client.entity.EntityPlayerSP".equals(transformedName);
    boolean renderer = "net.minecraft.client.renderer.entity.RenderPlayer".equals(transformedName)
        || "RenderPlayerOF".equals(transformedName);
    boolean camera = "net.minecraft.client.renderer.EntityRenderer".equals(transformedName);
    if (!player && !living && !model && !renderer && !clientPlayer && !camera) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    boolean changed = false;
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
          node.name, method.name, method.desc);
      String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if (player && "()F".equals(desc)
          && named(method.name, mapped, "getEyeHeight", "func_70047_e")) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
          if (insn.getOpcode() != FRETURN) continue;
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(new MethodInsnNode(INVOKESTATIC, STATE, "eyeHeight",
              "(FLnet/minecraft/entity/player/EntityPlayer;)F", false));
          method.instructions.insertBefore(insn, hook);
          changed = true;
        }
      } else if (player && "()Z".equals(desc)
          && named(method.name, mapped, "canTriggerWalking", "func_70041_e_")) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
          if (insn.getOpcode() != IRETURN) continue;
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(new MethodInsnNode(INVOKESTATIC, STATE, "canTriggerWalking",
              "(ZLnet/minecraft/entity/player/EntityPlayer;)Z", false));
          method.instructions.insertBefore(insn, hook);
          changed = true;
        }
      } else if (living && "(FF)V".equals(desc)
          && named(method.name, mapped, "moveEntityWithHeading", "func_70612_e")) {
        changed |= patchWaterMovement(method);
      } else if (clientPlayer && "()V".equals(desc)
          && named(method.name, mapped, "onLivingUpdate", "func_70636_d")) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
          AbstractInsnNode next = insn.getNext();
          if (insn instanceof MethodInsnNode) {
            MethodInsnNode call = (MethodInsnNode) insn;
            String mappedCall = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
                call.owner, call.name, call.desc);
            if (call.getOpcode() == INVOKEVIRTUAL && "()Z".equals(call.desc)
                && named(call.name, mappedCall, "isUsingItem", "func_71039_bw")) {
              method.instructions.set(insn, new MethodInsnNode(INVOKESTATIC,
                  "ru/givler/mbo/entity/fauna/CamelRiderInput", "itemUseSlowsMovement",
                  "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
              changed = true;
            }
          }
          insn = next;
        }
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
          if (!(insn instanceof FieldInsnNode) || insn.getOpcode() != GETFIELD) continue;
          FieldInsnNode field = (FieldInsnNode) insn;
          String mappedField = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(
              field.owner, field.name, field.desc);
          if (!"Z".equals(field.desc)
              || !named(field.name, mappedField, "isCollidedHorizontally", "field_70123_F")) continue;
          method.instructions.set(insn, new MethodInsnNode(INVOKESTATIC, STATE,
              "collisionStopsSprint", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
          changed = true;
          break;
        }
      } else if (model && "(FFFFFFLnet/minecraft/entity/Entity;)V".equals(desc)
          && named(method.name, mapped, "setRotationAngles", "func_78087_a")) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
          if (insn.getOpcode() != RETURN) continue;
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(new VarInsnNode(ALOAD, 7));
          hook.add(new VarInsnNode(FLOAD, 1));
          hook.add(new VarInsnNode(FLOAD, 3));
          hook.add(new MethodInsnNode(INVOKESTATIC, RENDER, "animate",
              "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;FF)V", false));
          method.instructions.insertBefore(insn, hook);
          changed = true;
        }
      } else if (renderer && "(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V".equals(desc)
          && named(method.name, mapped, "rotateCorpse", "func_77043_a")) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
          if (insn.getOpcode() != RETURN) continue;
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 1));
          hook.add(new VarInsnNode(FLOAD, 4));
          hook.add(new MethodInsnNode(INVOKESTATIC, RENDER, "rotate",
              "(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V", false));
          method.instructions.insertBefore(insn, hook);
          changed = true;
        }
      } else if (camera && "(F)V".equals(desc)
          && named(method.name, mapped, "setupViewBobbing", "func_78475_f")) {
        LabelNode continueRender = new LabelNode();
        InsnList hook = new InsnList();
        hook.add(new MethodInsnNode(INVOKESTATIC, RENDER, "skipViewBobbing", "()Z", false));
        hook.add(new JumpInsnNode(IFEQ, continueRender));
        hook.add(new InsnNode(RETURN));
        hook.add(continueRender);
        method.instructions.insert(hook);
        changed = true;
      } else if (camera && "(F)V".equals(desc)
          && named(method.name, mapped, "orientCamera", "func_78467_g")) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
          if (!(insn instanceof LdcInsnNode)) continue;
          Object value = ((LdcInsnNode) insn).cst;
          if (!(value instanceof Float) || Math.abs((Float) value - 1.62F) > 0.00001F) continue;
          method.instructions.set(insn, new MethodInsnNode(INVOKESTATIC, RENDER,
              "cameraEyeHeight", "()F", false));
          changed = true;
          break;
        }
      }
    }
    if (!changed) {
      System.err.println("[MBO ASM] Swimming hook not found in " + transformedName);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    System.out.println("[MBO ASM] Patched swimming in " + transformedName);
    return writer.toByteArray();
  }

  private static boolean patchWaterMovement(MethodNode method) {
    for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
      if (!(insn instanceof MethodInsnNode)) continue;
      MethodInsnNode call = (MethodInsnNode) insn;
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
          call.owner, call.name, call.desc);
      if (!"(FFF)V".equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc))
          || !named(call.name, mapped, "moveFlying", "func_70060_a")) continue;
      // moveFlying already has the entity and three movement arguments on the stack.
      AbstractInsnNode afterMove = insn.getNext();
      method.instructions.set(insn, new MethodInsnNode(INVOKESTATIC, STATE, "swimMoveFlying",
          "(Lnet/minecraft/entity/EntityLivingBase;FFF)V", false));
      int dragFactors = 0;
      for (AbstractInsnNode next = afterMove; next != null; next = next.getNext()) {
        if (!(next instanceof LdcInsnNode)) continue;
        Object value = ((LdcInsnNode) next).cst;
        if (!(value instanceof Double)) continue;
        double constant = (Double) value;
        if (Math.abs(constant - 0.8D) < 0.00001D) {
          dragFactors++;
          if (dragFactors == 1 || dragFactors == 3) {
            InsnList drag = new InsnList();
            drag.add(new VarInsnNode(ALOAD, 0));
            drag.add(new MethodInsnNode(INVOKESTATIC, STATE, "waterDrag",
                "(DLnet/minecraft/entity/EntityLivingBase;)D", false));
            method.instructions.insert(next, drag);
          }
        } else if (dragFactors == 3 && Math.abs(constant - 0.02D) < 0.00001D) {
          InsnList gravity = new InsnList();
          gravity.add(new VarInsnNode(ALOAD, 0));
          gravity.add(new MethodInsnNode(INVOKESTATIC, STATE, "swimGravity",
              "(DLnet/minecraft/entity/EntityLivingBase;)D", false));
          method.instructions.insert(next, gravity);
          return true;
        }
      }
      return false;
    }
    return false;
  }

  private static boolean named(String original, String mapped, String mcp, String srg) {
    return mcp.equals(original) || srg.equals(original) || mcp.equals(mapped) || srg.equals(mapped);
  }
}
