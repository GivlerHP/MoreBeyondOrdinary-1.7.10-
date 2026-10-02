package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WaterloggingCameraTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.client.renderer.EntityRenderer";
  private static final String ACTIVE_RENDER_INFO = "net/minecraft/client/renderer/ActiveRenderInfo";
  private static final String DESCRIPTION =
      "(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;F)Lnet/minecraft/block/Block;";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if ("net.minecraft.client.renderer.ItemRenderer".equals(transformedName))
      return patchOverlay(bytes);
    if ("net.minecraft.client.renderer.RenderGlobal".equals(transformedName))
      return patchUnderwaterSky(bytes);
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode instruction = method.instructions.getFirst();
          instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
        String mappedOwner = remapper.mapType(call.owner);
        String mappedDescription = remapper.mapMethodDesc(call.desc);
        if (call.getOpcode() != INVOKESTATIC
            || !(ACTIVE_RENDER_INFO.equals(call.owner)
                || ACTIVE_RENDER_INFO.equals(mappedOwner))
            || !(DESCRIPTION.equals(call.desc) || DESCRIPTION.equals(mappedDescription))) continue;
        call.owner = "ru/givler/mbo/core/WaterloggingCameraHooks";
        call.name = "getViewBlock";
        call.itf = false;
        patched++;
      }
    if (patched == 0 || !moveFogUpdateAfterCamera(node)) {
      System.err.println("[MBO ASM] EntityRenderer camera hooks were not found");
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    System.out.println("[MBO ASM] Patched " + patched + " waterlogged camera material calls");
    return writer.toByteArray();
  }

  private byte[] patchUnderwaterSky(byte[] bytes) {
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
          node.name, method.name, method.desc);
      if (!"(F)V".equals(method.desc)
          || !("renderSky".equals(method.name) || "func_72714_a".equals(method.name)
              || "renderSky".equals(mapped) || "func_72714_a".equals(mapped))) continue;
      int horizons = 0;
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        String callName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
            call.owner, call.name, call.desc);
        if (call.getOpcode() != INVOKEVIRTUAL || !"()D".equals(call.desc)
            || !("getHorizon".equals(call.name) || "func_72919_O".equals(call.name)
                || "getHorizon".equals(callName) || "func_72919_O".equals(callName))) continue;
        InsnList horizonHook = new InsnList();
        horizonHook.add(new VarInsnNode(FLOAD, 1));
        horizonHook.add(new MethodInsnNode(INVOKESTATIC,
            "ru/givler/mbo/core/WaterloggingCameraHooks", "swimmingSkyHorizon", "(DF)D", false));
        method.instructions.insert(call, horizonHook);
        horizons++;
      }
      if (horizons != 1) continue;
      InsnList hook = new InsnList();
      LabelNode render = new LabelNode();
      hook.add(new VarInsnNode(FLOAD, 1));
      hook.add(new MethodInsnNode(INVOKESTATIC,
          "ru/givler/mbo/core/WaterloggingCameraHooks", "shouldRenderSky", "(F)Z", false));
      hook.add(new JumpInsnNode(IFNE, render));
      hook.add(new InsnNode(RETURN));
      hook.add(render);
      method.instructions.insert(hook);
      patched++;
    }
    if (patched != 1) {
      System.err.println("[MBO ASM] Underwater sky hook count: " + patched);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    return writer.toByteArray();
  }

  /** Update fog after ActiveRenderInfo has captured the current F5 camera. */
  private static boolean moveFogUpdateAfterCamera(ClassNode node) {
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
          node.name, method.name, method.desc);
      if (!"(FJ)V".equals(method.desc)
          || !("renderWorld".equals(method.name) || "func_78471_a".equals(method.name)
              || "renderWorld".equals(mapped) || "func_78471_a".equals(mapped))) continue;
      MethodInsnNode fog = null, clear = null, camera = null;
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        String callName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
            call.owner, call.name, call.desc);
        if (fog == null && "(F)V".equals(call.desc)
            && ("updateFogColor".equals(call.name) || "func_78466_h".equals(call.name)
                || "updateFogColor".equals(callName) || "func_78466_h".equals(callName))) fog = call;
        else if (fog != null && clear == null && "org/lwjgl/opengl/GL11".equals(call.owner)
            && "glClear".equals(call.name)) clear = call;
        else if ((ACTIVE_RENDER_INFO.equals(call.owner)
                || ACTIVE_RENDER_INFO.equals(FMLDeobfuscatingRemapper.INSTANCE.mapType(call.owner)))
            && ("updateRenderInfo".equals(call.name) || "func_74583_a".equals(call.name)
                || "updateRenderInfo".equals(callName))) camera = call;
      }
      if (fog == null || clear == null || camera == null) return false;
      AbstractInsnNode fogFloat = previousOpcode(fog);
      AbstractInsnNode fogThis = previousOpcode(fogFloat);
      AbstractInsnNode clearMask = previousOpcode(clear);
      if (fogFloat == null || fogThis == null || clearMask == null
          || fogFloat.getOpcode() != FLOAD || fogThis.getOpcode() != ALOAD
          || !(clearMask instanceof IntInsnNode)
          || ((IntInsnNode) clearMask).operand != 16640) return false;
      method.instructions.remove(fogThis);
      method.instructions.remove(fogFloat);
      method.instructions.remove(fog);
      method.instructions.remove(clearMask);
      method.instructions.remove(clear);
      InsnList moved = new InsnList();
      moved.add(fogThis);
      moved.add(fogFloat);
      moved.add(fog);
      moved.add(clearMask);
      moved.add(clear);
      InsnList capture = new InsnList();
      capture.add(new VarInsnNode(FLOAD, 1));
      MethodInsnNode captureCall = new MethodInsnNode(INVOKESTATIC,
          "ru/givler/mbo/core/WaterloggingCameraHooks", "captureCamera", "(F)V", false);
      capture.add(captureCall);
      method.instructions.insert(camera, capture);
      method.instructions.insert(captureCall, moved);
      return true;
    }
    return false;
  }

  private static AbstractInsnNode previousOpcode(AbstractInsnNode instruction) {
    if (instruction == null) return null;
    AbstractInsnNode previous = instruction.getPrevious();
    while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
    return previous;
  }

  private byte[] patchOverlay(byte[] bytes) {
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    int overlayPatched = 0;
    for (MethodNode method : node.methods) {
      if (!"(F)V".equals(method.desc)) continue;
      boolean brightnessOverlay = false;
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
            call.owner, call.name, call.desc);
        if ("getBrightness".equals(call.name) || "func_70013_c".equals(call.name)
            || "getBrightness".equals(mapped) || "func_70013_c".equals(mapped))
          brightnessOverlay = true;
      }
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        if (brightnessOverlay && call.getOpcode() == INVOKESTATIC
            && "org/lwjgl/opengl/GL11".equals(call.owner)
            && "glColor4f".equals(call.name)) {
          AbstractInsnNode alpha = previousOpcode(call);
          if (alpha instanceof LdcInsnNode
              && Float.valueOf(0.5F).equals(((LdcInsnNode) alpha).cst)) {
            ((LdcInsnNode) alpha).cst = 0.1F;
            overlayPatched++;
          }
        }
        String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
            call.owner, call.name, call.desc);
        if (call.getOpcode() != INVOKEVIRTUAL
            || !("isInsideOfMaterial".equals(call.name)
                || "func_70055_a".equals(call.name)
                || "isInsideOfMaterial".equals(mapped))
            || !"(Lnet/minecraft/block/material/Material;)Z".equals(
                FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc))) continue;
        method.instructions.insertBefore(call, new VarInsnNode(FLOAD, 1));
        call.setOpcode(INVOKESTATIC);
        call.owner = "ru/givler/mbo/core/WaterloggingCameraHooks";
        call.name = "isCameraInsideWater";
        call.desc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/material/Material;F)Z";
        call.itf = false;
        patched++;
      }
    }
    if (patched != 1 || overlayPatched != 1) {
      System.err.println("[MBO ASM] ItemRenderer water overlay patch counts: "
          + patched + ", " + overlayPatched);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    return writer.toByteArray();
  }
}
