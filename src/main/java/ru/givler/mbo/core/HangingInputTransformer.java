package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Suppresses mounted movement input, without canceling breathing or damage timers. */
public final class HangingInputTransformer implements IClassTransformer, Opcodes {
    @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes != null && "net.minecraft.client.renderer.entity.RenderManager".equals(transformedName))
            return playerVisibility(bytes);
        if (bytes != null && "net.minecraft.client.renderer.EntityRenderer".equals(transformedName))
            return camera(bytes);
        if (bytes == null || !"net.minecraft.client.entity.EntityPlayerSP".equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner, call.name, call.desc);
                if (!"()V".equals(call.desc) || !("updatePlayerMoveState".equals(call.name)
                        || "func_78898_a".equals(call.name) || "func_78898_a".equals(mapped))) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 0));
                hook.add(new MethodInsnNode(INVOKESTATIC,
                    "ru/givler/mbo/client/handler/HangingClientEvents", "blockMovement",
                    "(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false));
                method.instructions.insert(insn, hook);
                patched++;
            }
        }
        if (patched != 1) throw new IllegalStateException("Hanging input anchor missing: " + patched);
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] playerVisibility(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
            String descriptor = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
            if (!"(Lnet/minecraft/entity/Entity;DDDFFZ)Z".equals(descriptor)
                    || !("func_147939_a".equals(method.name) || "func_147939_a".equals(mapped)
                        || "doRenderEntity".equals(method.name))) continue;
            LabelNode render = new LabelNode();
            InsnList guard = new InsnList();
            guard.add(new VarInsnNode(ALOAD, 1));
            guard.add(new MethodInsnNode(INVOKESTATIC,
                "ru/givler/mbo/client/handler/HangingClientEvents", "hideFirstPersonPlayer",
                "(Lnet/minecraft/entity/Entity;)Z", false));
            guard.add(new JumpInsnNode(IFEQ, render));
            guard.add(new InsnNode(ICONST_1));
            guard.add(new InsnNode(IRETURN));
            guard.add(render);
            method.instructions.insert(guard);
            patched++;
        }
        if (patched != 1) throw new IllegalStateException("Hanging player render anchor missing: " + patched);
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] camera(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
            if (!"(F)V".equals(method.desc) || !("orientCamera".equals(method.name)
                    || "func_78467_g".equals(method.name) || "func_78467_g".equals(mapped))) continue;
            method.instructions.insert(new MethodInsnNode(INVOKESTATIC,
                "ru/givler/mbo/client/handler/HangingClientEvents", "beginCamera", "()V", false));
            for (AbstractInsnNode insn : method.instructions.toArray())
                if (insn.getOpcode() == RETURN) method.instructions.insertBefore(insn,
                    new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/core/HangingCameraContext",
                        "end", "()V", false));
            patched++;
        }
        if (patched != 1) throw new IllegalStateException("Hanging camera anchor missing: " + patched);
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        return writer.toByteArray();
    }
}
