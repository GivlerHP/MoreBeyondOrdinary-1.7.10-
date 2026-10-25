package ru.givler.mbo.integration.angelica.core;

import java.lang.reflect.*;
import org.objectweb.asm.tree.ClassNode;

/** Rewrites color consumers added by Angelica mixins after normal coremod transforms. */
public final class AngelicaColorMixinExtension {
    private static boolean installed, unavailable, installing;
    private AngelicaColorMixinExtension() {}
    public static synchronized void install() {
        if (installed || unavailable || installing) return;
        installing = true;
        try {
            ClassLoader loader = AngelicaColorMixinExtension.class.getClassLoader();
            Class<?> launch = Class.forName("net.minecraft.launchwrapper.Launch", false, loader);
            if (launch.getField("classLoader").get(null) == null) return;
            Class<?> environment = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment", false, loader);
            Object current = environment.getMethod("getCurrentEnvironment").invoke(null);
            Object transformer = environment.getMethod("getActiveTransformer").invoke(current);
            if (transformer == null) return;
            Method getter = transformer.getClass().getDeclaredMethod("getExtensions"); getter.setAccessible(true);
            Object extensions = getter.invoke(transformer);
            Class<?> type = Class.forName("org.spongepowered.asm.mixin.transformer.ext.IExtension", false, loader);
            Object extension = Proxy.newProxyInstance(loader, new Class<?>[]{type}, (proxy, method, args) -> {
                if (method.getName().equals("checkActive")) return true;
                if (method.getName().equals("postApply")) {
                    Class<?> contextType = Class.forName("org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext", false, loader);
                    Object value = contextType.getMethod("getClassNode").invoke(args[0]);
                    patchTarget(value);
                }
                if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                if (method.getName().equals("equals")) return proxy == args[0];
                if (method.getName().equals("toString")) return "MBO thread-local block colors";
                return null;
            });
            extensions.getClass().getMethod("add", type).invoke(extensions, extension);
            installed = true;
            System.out.println("[MBO] Registered post-mixin block color compatibility");
        } catch (ClassNotFoundException absent) {
            unavailable = true;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot register post-mixin block color compatibility", error);
        } finally {
            installing = false;
        }
    }
    /** UniMixins can expose either ASM's native tree or its legacy shaded tree API. */
    public static void patchTarget(Object value) throws ReflectiveOperationException {
        if (value instanceof ClassNode) {
            ClassNode node = (ClassNode) value;
            AngelicaColorStateTransformer.patch(node, node.name.replace('/', '.'));
            return;
        }
        String target=(String)value.getClass().getField("name").get(value);
        if(!target.startsWith("net/minecraft/"))return;
        // Patch the live legacy tree directly. Serializing a postApply target here
        // can write unfinished stack-map frames belonging to other mixins.
        ClassLoader loader = value.getClass().getClassLoader();
        Class<?> instruction = Class.forName("org.spongepowered.asm.lib.tree.AbstractInsnNode", false, loader);
        Class<?> fieldType = Class.forName("org.spongepowered.asm.lib.tree.FieldInsnNode", false, loader);
        Class<?> intType = Class.forName("org.spongepowered.asm.lib.tree.IntInsnNode", false, loader);
        Class<?> callType = Class.forName("org.spongepowered.asm.lib.tree.MethodInsnNode", false, loader);
        String[] names = {"blockColor", "isSmooth",
            "colorRedTopLeft", "colorRedBottomLeft", "colorRedBottomRight", "colorRedTopRight",
            "colorGreenTopLeft", "colorGreenBottomLeft", "colorGreenBottomRight", "colorGreenTopRight",
            "colorBlueTopLeft", "colorBlueBottomLeft", "colorBlueBottomRight", "colorBlueTopRight"};
        int count = 0;
        for (Object method : (java.util.List<?>) value.getClass().getField("methods").get(value)) {
            Object instructions = method.getClass().getField("instructions").get(method);
            boolean changed = false;
            for (Object item : (Object[]) instructions.getClass().getMethod("toArray").invoke(instructions)) {
                if (!fieldType.isInstance(item)) continue;
                if (!"com/prupe/mcpatcher/cc/ColorizeBlock".equals(fieldType.getField("owner").get(item))) continue;
                int opcode = (Integer) instruction.getMethod("getOpcode").invoke(item);
                if (opcode != 178 && opcode != 179) continue;
                String name = (String) fieldType.getField("name").get(item);
                int slot = -1;
                for (int i = 0; i < names.length; i++) if (names[i].equals(name)) slot = i;
                if (slot < 0) continue;
                String descriptor = slot == 0 ? "I" : slot == 1 ? "Z" : "F";
                if (!descriptor.equals(fieldType.getField("desc").get(item)))
                    throw new IllegalStateException("Angelica color field changed: " + name);
                boolean read = opcode == 178;
                String kind = slot == 0 ? "Int" : slot == 1 ? "Boolean" : "Float";
                Object index = intType.getConstructor(int.class, int.class).newInstance(16, slot);
                Object call = callType.getConstructor(int.class, String.class, String.class, String.class, boolean.class)
                    .newInstance(184, "ru/givler/mbo/integration/angelica/client/render/ThreadLocalBlockColors",
                        (read ? "get" : "set") + kind, read ? "(I)" + descriptor : "(" + descriptor + "I)V", false);
                instructions.getClass().getMethod("insertBefore", instruction, instruction).invoke(instructions, item, index);
                instructions.getClass().getMethod("set", instruction, instruction).invoke(instructions, item, call);
                changed = true;
                count++;
            }
            if (changed) {
                Field maxStack = method.getClass().getField("maxStack");
                maxStack.setInt(method, maxStack.getInt(method) + 1);
            }
        }
        if (count > 0) System.out.println("[MBO] Thread-local MCPatcher block color results: " + target + " accesses=" + count);
    }
}
