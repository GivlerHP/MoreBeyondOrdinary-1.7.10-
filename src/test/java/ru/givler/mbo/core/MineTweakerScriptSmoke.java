package ru.givler.mbo.core;

import java.net.*;
import java.nio.file.*;
import java.util.*;

/** Exercise the actual GTNH provider sorting empty and populated script directories. */
public final class MineTweakerScriptSmoke {
    public static void main(String[] args) throws Exception {
        final MineTweakerScriptTransformer transformer = new MineTweakerScriptTransformer();
        Path root = Paths.get("build/crafttweaker-update/provider-fixture");
        Files.createDirectories(root.resolve("java"));
        Files.createDirectories(root.resolve("mvel"));
        Files.createDirectories(root.resolve("populated/scripts"));
        Files.write(root.resolve("populated/scripts/nested.zs"), "// nested".getBytes("UTF-8"));
        Files.write(root.resolve("root.zs"), "// root".getBytes("UTF-8"));
        try (URLClassLoader loader = new URLClassLoader(new URL[]{Paths.get(args[0]).toUri().toURL()}, MineTweakerScriptSmoke.class.getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.startsWith("minetweaker.runtime.providers.")) return super.loadClass(name, resolve);
                Class<?> result = findLoadedClass(name);
                if (result == null) try {
                    byte[] bytes;
                    try (java.io.InputStream in = findResource(name.replace('.', '/') + ".class").openStream();
                         java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                        byte[] buffer = new byte[4096]; int n;
                        while ((n = in.read(buffer)) >= 0) out.write(buffer, 0, n);
                        bytes = out.toByteArray();
                    }
                    byte[] patched = transformer.transform(name, name, bytes);
                    if (!Arrays.equals(patched, transformer.transform(name, name, patched))) throw new AssertionError("Patch is not idempotent");
                    result = defineClass(name, patched, 0, patched.length);
                } catch (java.io.IOException e) { throw new ClassNotFoundException(name, e); }
                if (resolve) resolveClass(result);
                return result;
            }
        }) {
            Class<?> provider = loader.loadClass("minetweaker.runtime.providers.ScriptProviderDirectory");
            Class<?> iterator = loader.loadClass("minetweaker.runtime.providers.ScriptIteratorDirectory");
            for (int reload = 0; reload < 2; reload++) {
                Iterator<?> groups = (Iterator<?>) provider.getMethod("getScripts").invoke(provider.getConstructor(java.io.File.class).newInstance(root.toFile()));
                int files = 0, directories = 0;
                while (groups.hasNext()) {
                    Object group = groups.next();
                    if (iterator.isInstance(group)) { iterator.getMethod("getName").invoke(group); directories++; }
                    Class<?> api = Class.forName("minetweaker.runtime.IScriptIterator");
                    while ((Boolean) api.getMethod("next").invoke(group)) {
                        String name = (String) api.getMethod("getName").invoke(group);
                        if (!name.endsWith(".zs")) throw new AssertionError(name);
                        try (java.io.InputStream in = (java.io.InputStream) api.getMethod("open").invoke(group)) {
                            if (in.read() != '/') throw new AssertionError("Script unreadable");
                        }
                        files++;
                    }
                }
                if (files != 2 || directories != 3) throw new AssertionError(files + "/" + directories);
            }
        }
        System.out.println("PASS actual GTNH directory sorting, empty folders, nested scripts, repeat load and idempotence");
    }
}
