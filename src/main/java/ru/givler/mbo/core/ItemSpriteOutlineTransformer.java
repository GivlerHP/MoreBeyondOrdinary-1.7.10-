package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Replaces the strip geometry after vanilla has drawn the front and back of the sprite. */
public final class ItemSpriteOutlineTransformer implements IClassTransformer, Opcodes {
  private static final String ITEM_RENDERER = "net.minecraft.client.renderer.ItemRenderer";
  private static final String TESSELLATOR = "net/minecraft/client/renderer/Tessellator";
  private static final String OFFHAND_RENDERER = "mods.battlegear2.client.utils.BattlegearRenderHelper";
  private static final String ANGELICA_CACHE = "com.gtnewhorizons.angelica.rendering.items.ItemRenderListManager";
  private static final String HOOK = "ru/givler/mbo/client/render/ItemSpriteOutline";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || Boolean.getBoolean("mbo.disableItemSpriteOutline")) return bytes;
    if ("net.minecraft.client.renderer.texture.TextureAtlasSprite".equals(transformedName)) {
      return captureStaticSprite(bytes);
    }
    boolean vanilla = ITEM_RENDERER.equals(transformedName);
    boolean offhand = OFFHAND_RENDERER.equals(transformedName);
    boolean cache = ANGELICA_CACHE.equals(transformedName);
    if (!vanilla && !offhand && !cache) return bytes;

    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
    boolean changed = false;
    for (MethodNode method : node.methods) {
      String mapped = remapper.mapMethodName(node.name, method.name, method.desc);
      String descriptor = remapper.mapMethodDesc(method.desc);
      if (cache && method.name.equals("pre") && method.desc.startsWith("(FFFFIIF)L")) {
        separateAngelicaCache(method);
        changed = true;
      }
      if ((vanilla && "(F)V".equals(descriptor)
          && (method.name.equals("renderItemInFirstPerson") || mapped.equals("func_78440_a")))
          || (offhand && method.name.equals("renderItemInFirstPerson")
              && descriptor.equals("(FLnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/ItemRenderer;)V"))) {
        scopeFirstPerson(method);
        changed = true;
      }
      if (!vanilla || !("(L" + TESSELLATOR + ";FFFFIIF)V").equals(descriptor)
          || !(method.name.equals("renderItemIn2D") || mapped.equals("func_78439_a")
              || method.name.equals("func_78439_a"))) continue;

      int starts = 0;
      MethodInsnNode startSides = null;
      MethodInsnNode lastDraw = null;
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        boolean tessellator = TESSELLATOR.equals(remapper.map(call.owner));
        String callName = remapper.mapMethodName(call.owner, call.name, call.desc);
        if (tessellator && "()I".equals(call.desc)
            && (callName.equals("draw") || callName.equals("func_78381_a"))) lastDraw = call;
        boolean start = tessellator && "()V".equals(call.desc)
            && (callName.equals("startDrawingQuads") || callName.equals("func_78382_b"));
        // Angelica can combine all six vanilla batches into one by redirecting intermediate starts.
        boolean redirectedStart = node.name.equals(call.owner) && call.name.contains("batchDrawCalls2")
            && ("(L" + TESSELLATOR + ";)V").equals(remapper.mapMethodDesc(call.desc));
        if ((start || redirectedStart) && ++starts == 3) startSides = call;
      }
      if (starts != 6 || startSides == null || lastDraw == null) return bytes;
      AbstractInsnNode drawBuffer = lastDraw.getPrevious();
      while (drawBuffer != null && drawBuffer.getOpcode() < 0) drawBuffer = drawBuffer.getPrevious();
      if (!(drawBuffer instanceof VarInsnNode) || drawBuffer.getOpcode() != ALOAD
          || ((VarInsnNode) drawBuffer).var != 0) return bytes;

      LabelNode finish = new LabelNode();
      method.instructions.insertBefore(drawBuffer, finish);
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 0));
      for (int slot = 1; slot <= 4; slot++) hook.add(new VarInsnNode(FLOAD, slot));
      hook.add(new VarInsnNode(ILOAD, 5));
      hook.add(new VarInsnNode(ILOAD, 6));
      hook.add(new VarInsnNode(FLOAD, 7));
      hook.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/render/ItemSpriteOutline",
          "drawSides", "(Ljava/lang/Object;FFFFIIF)Z", false));
      hook.add(new JumpInsnNode(IFNE, finish));
      method.instructions.insert(startSides, hook);

      changed = true;
      System.out.println("[MBO ASM] Installed item sprite outline hook");
    }
    if (!changed) return bytes;
    ClassWriter writer = vanilla ? SafeClassWriter.create() : new ClassWriter(ClassWriter.COMPUTE_MAXS);
    node.accept(writer);
    return writer.toByteArray();
  }

  private static void scopeFirstPerson(MethodNode method) {
    for (AbstractInsnNode insn : method.instructions.toArray()) {
      if (insn.getOpcode() == RETURN) method.instructions.insertBefore(insn, scopeCall("exitFirstPerson"));
    }
    method.instructions.insert(scopeCall("enterFirstPerson"));
  }

  private static MethodInsnNode scopeCall(String name) {
    return new MethodInsnNode(INVOKESTATIC, HOOK, name, "()V", false);
  }

  private static void separateAngelicaCache(MethodNode method) {
    // Angelica treats a null return as "already drawn". Keep its normal draw path
    // and partition the cache using a sign bit that never reaches the renderer.
    InsnList key = new InsnList();
    key.add(new VarInsnNode(FLOAD, 6));
    key.add(new MethodInsnNode(INVOKESTATIC, HOOK, "cacheThickness", "(F)F", false));
    key.add(new VarInsnNode(FSTORE, 6));
    method.instructions.insert(key);
  }

  private byte[] captureStaticSprite(byte[] bytes) {
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (!"()V".equals(method.desc) || !(method.name.equals("clearFramesTextureData")
          || method.name.equals("func_130103_l") || mapped.equals("func_130103_l"))) continue;
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 0));
      hook.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/render/ItemSpriteOutline",
          "beforeClearFrames", "(Ljava/lang/Object;)V", false));
      method.instructions.insert(hook);
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
      node.accept(writer);
      return writer.toByteArray();
    }
    return bytes;
  }
}
