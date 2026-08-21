package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Fixes duration spacing and shadow in vanilla and DynamicSurroundings potion HUDs. */
public final class PotionDurationFontTransformer implements IClassTransformer, Opcodes {
    private static final String VANILLA = "net.minecraft.client.renderer.InventoryEffectRenderer";
    private static final String DYNAMIC = "org.blockartistry.mod.DynSurround.client.hud.PotionHUD";
    private static final String FONT = "net/minecraft/client/gui/FontRenderer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        boolean dynamic = DYNAMIC.equals(transformedName);
        if (bytes == null || (!VANILLA.equals(transformedName) && !dynamic)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean patched = false;
        boolean positionPatched = false;
        for (MethodNode method : node.methods) {
            int fontDraws = 0;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                if (!FONT.equals(call.owner) || !"(Ljava/lang/String;III)I".equals(call.desc)) continue;
                fontDraws++;
                if (fontDraws != 2) continue;
                // Keep the configured grey foreground, but remove the black
                // shadow which makes modern digits look like a second value.
                call.name = "drawStringWithShadow".equals(call.name)
                        ? "drawString" : "func_78276_b";
                patched = true;
                AbstractInsnNode cursor = call.getPrevious();
                for (int checked = 0; cursor != null && checked < 80; checked++, cursor = cursor.getPrevious()) {
                    if (cursor instanceof IntInsnNode && cursor.getOpcode() == BIPUSH
                            && ((IntInsnNode) cursor).operand == (dynamic ? 10 : 16)) {
                        // DynamicSurroundings uses y + 6 + 10; vanilla uses
                        // y + 16. Move only the duration two pixels upward.
                        ((IntInsnNode) cursor).operand = dynamic ? 8 : 14;
                        positionPatched = true;
                        break;
                    }
                }
            }
        }
        if (!patched) {
            System.err.println("[MBO ASM] Potion duration text was not found in " + transformedName);
            return bytes;
        }
        if (!positionPatched) {
            System.err.println("[MBO ASM] Potion duration shadow removed, but position was not found in "
                    + transformedName);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        System.out.println("[MBO ASM] Adjusted potion duration in " + transformedName);
        return writer.toByteArray();
    }
}
