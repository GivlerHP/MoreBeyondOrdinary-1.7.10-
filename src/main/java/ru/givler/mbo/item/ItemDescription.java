package ru.givler.mbo.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

/** Item-owned lore, independent of the item/mod and unrelated NBT. */
public final class ItemDescription {
    public static final int MAX_LINES = 6, MAX_LINE_LENGTH = 96;
    public static final String KEY = "MBO_Description";
    private ItemDescription() {}

    public static List<String> read(ItemStack stack) {
        List<String> lines = new ArrayList<String>();
        if (stack == null || !stack.hasTagCompound()) return lines;
        NBTTagList stored = stack.getTagCompound().getCompoundTag(KEY).getTagList("Lines", 8);
        for (int i = 0; i < Math.min(MAX_LINES, stored.tagCount()); i++) lines.add(clean(stored.getStringTagAt(i)));
        return lines;
    }

    public static int color(ItemStack stack) {
        return stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey(KEY, 10)
                ? stack.getTagCompound().getCompoundTag(KEY).getInteger("Color") & 0xffffff : 0xb0a48c;
    }

    public static void write(ItemStack stack, List<String> lines, int color) {
        write(stack, lines, new ArrayList<int[]>(), color);
    }

    public static List<int[]> colors(ItemStack stack) {
        List<int[]> result = new ArrayList<int[]>();
        List<String> lines = read(stack);
        NBTTagList stored = stack != null && stack.hasTagCompound()
                ? stack.getTagCompound().getCompoundTag(KEY).getTagList("Colors", 10) : new NBTTagList();
        for (int i = 0; i < lines.size(); i++) {
            int[] rgb = new int[lines.get(i).length()];
            java.util.Arrays.fill(rgb, color(stack));
            if (i < stored.tagCount()) {
                int[] saved = stored.getCompoundTagAt(i).getIntArray("RGB");
                for (int j = 0; j < Math.min(rgb.length, saved.length); j++) rgb[j] = saved[j] & 0xffffff;
            }
            result.add(rgb);
        }
        return result;
    }

    /** Preserve colours outside an edit; inserted text uses the selected brush colour. */
    public static int[] editColors(String before, String after, int[] previous, int brush) {
        int prefix = 0, suffix = 0;
        while (prefix < Math.min(before.length(), after.length()) && before.charAt(prefix) == after.charAt(prefix)) prefix++;
        while (suffix < Math.min(before.length(), after.length()) - prefix
                && before.charAt(before.length() - suffix - 1) == after.charAt(after.length() - suffix - 1)) suffix++;
        int[] result = new int[after.length()];
        java.util.Arrays.fill(result, brush & 0xffffff);
        for (int i = 0; i < prefix && i < previous.length; i++) result[i] = previous[i];
        for (int i = 0; i < suffix; i++) {
            int source = before.length() - i - 1;
            if (source < previous.length) result[after.length() - i - 1] = previous[source];
        }
        return result;
    }

    public static void write(ItemStack stack, List<String> lines, List<int[]> colors, int color) {
        if (stack == null) return;
        NBTTagList stored = new NBTTagList();
        int last = Math.min(MAX_LINES, lines.size()) - 1;
        while (last >= 0 && clean(lines.get(last)).isEmpty()) last--;
        if (last < 0) {
            if (stack.hasTagCompound()) stack.getTagCompound().removeTag(KEY);
            return;
        }
        for (int i = 0; i <= last; i++) stored.appendTag(new NBTTagString(clean(lines.get(i))));
        NBTTagCompound description = new NBTTagCompound();
        description.setTag("Lines", stored);
        description.setInteger("Color", color & 0xffffff);
        NBTTagList styles = new NBTTagList();
        for (int i = 0; i <= last; i++) {
            int[] rgb = new int[stored.getStringTagAt(i).length()];
            java.util.Arrays.fill(rgb, color & 0xffffff);
            if (i < colors.size()) for (int j = 0; j < Math.min(rgb.length, colors.get(i).length); j++)
                rgb[j] = colors.get(i)[j] & 0xffffff;
            NBTTagCompound style = new NBTTagCompound(); style.setIntArray("RGB", rgb); styles.appendTag(style);
        }
        description.setTag("Colors", styles);
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setTag(KEY, description);
    }

    private static String clean(String text) {
        if (text == null) return "";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length() && result.length() < MAX_LINE_LENGTH; i++) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < text.length()) { i++; continue; }
            if (c >= 32 && c != 127 && c != '<' && c != '>') result.append(c);
        }
        return result.toString();
    }
}
