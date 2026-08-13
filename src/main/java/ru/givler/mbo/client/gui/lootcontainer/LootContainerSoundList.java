package ru.givler.mbo.client.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SideOnly(Side.CLIENT)
public final class LootContainerSoundList {
    private LootContainerSoundList() {}

    public static List<String> getSounds() {
        Set<String> sounds = new LinkedHashSet<String>();
        sounds.add("dig.stone");
        sounds.add("dig.wood");
        sounds.add("dig.glass");
        sounds.add("dig.gravel");
        sounds.add("random.break");
        try {
            SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
            collect(handler, sounds, Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>()), 0);
        } catch (Throwable ignored) {
        }
        List<String> result = new ArrayList<String>(sounds);
        Collections.sort(result);
        return result;
    }

    private static void collect(Object object, Set<String> out, Set<Object> visited, int depth)
            throws IllegalAccessException {
        if (object == null || depth > 3 || !visited.add(object)) return;
        if (object instanceof ResourceLocation) {
            out.add(toSoundName((ResourceLocation) object));
            return;
        }
        if (object instanceof Map) {
            for (Object key : ((Map<?, ?>) object).keySet()) collect(key, out, visited, depth + 1);
            return;
        }
        Class<?> type = object.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                Class<?> fieldType = field.getType();
                if (!(Map.class.isAssignableFrom(fieldType)
                        || fieldType.getName().contains("Registry")
                        || fieldType == ResourceLocation.class)) continue;
                field.setAccessible(true);
                collect(field.get(object), out, visited, depth + 1);
            }
            type = type.getSuperclass();
        }
    }

    private static String toSoundName(ResourceLocation location) {
        String value = location.toString();
        return value.startsWith("minecraft:") ? value.substring("minecraft:".length()) : value;
    }
}
