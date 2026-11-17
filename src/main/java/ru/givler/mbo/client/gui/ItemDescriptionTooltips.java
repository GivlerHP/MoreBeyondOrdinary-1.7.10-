package ru.givler.mbo.client.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import ru.givler.mbo.item.ItemDescription;

/** Existing lore and table-authored descriptions share a scroll section. */
public final class ItemDescriptionTooltips {
    private ItemDescriptionTooltips() {}

    public static void decorate(ItemStack stack, List<String> lines, FontRenderer font) {
        if (stack == null || !stack.hasTagCompound() || lines.isEmpty()) return;
        for (String line : lines) if (line.contains("<mbo:scroll>")) return;
        NBTTagCompound tag = stack.getTagCompound();
        Set<String> original = new HashSet<String>();
        for (int i = 0; i < 5; i++) if (tag.hasKey("MF_CustomTag" + i, 8))
            original.add(plain(tag.getString("MF_CustomTag" + i)));
        NBTTagList lore = tag.getCompoundTag("display").getTagList("Lore", 8);
        for (int i = 0; i < lore.tagCount(); i++) original.add(plain(lore.getStringTagAt(i)));
        original.remove("");
        List<String> description = new ArrayList<String>();
        for (int i = 1; i < lines.size();) {
            String line = lines.get(i);
            if (!line.contains("<mbo:") && original.contains(plain(line))) {
                appendWrapped(description, line, font);
                lines.remove(i);
            } else i++;
        }
        description.addAll(preview(stack, font, 240));
        if (description.isEmpty()) return;
        List<String> section = new ArrayList<String>();
        if (lines.size() > 1 && !plain(lines.get(lines.size() - 1)).isEmpty()) section.add("<mbo:end>");
        section.add("<mbo:statline><mbo:scroll>" + description.get(0));
        section.addAll(description.subList(1, description.size()));
        section.add("<mbo:end>");
        lines.addAll(section);
    }

    /** Wrap plain text first, then emit colour runs; wrapping cannot lose a selected word's RGB. */
    public static List<String> preview(ItemStack stack, FontRenderer font, int width) {
        List<String> result = new ArrayList<String>();
        List<String> text = ItemDescription.read(stack);
        List<int[]> styles = ItemDescription.colors(stack);
        for (int row = 0; row < text.size(); row++) {
            String line = text.get(row);
            int start = 0;
            do {
                int end = start, lastSpace = -1;
                while (end < line.length() && (font == null || end == start
                        || font.getStringWidth(line.substring(start, end + 1)) <= width)) {
                    if (line.charAt(end) == ' ') lastSpace = end;
                    end++;
                }
                if (end < line.length() && lastSpace > start) end = lastSpace + 1;
                StringBuilder formatted = new StringBuilder();
                int previous = -1;
                for (int i = start; i < end; i++) {
                    int color = styles.get(row)[i];
                    if (color != previous) formatted.append("<mbo:color:")
                            .append(String.format(Locale.ROOT, "%06x", color)).append('>');
                    formatted.append(line.charAt(i)); previous = color;
                }
                result.add(formatted.toString());
                start = end;
            } while (start < line.length());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void appendWrapped(List<String> result, String text, FontRenderer font) {
        for (String part : text.replace("\\n", "\n").split("\n", -1)) {
            if (font == null) result.add(part);
            else result.addAll(font.listFormattedStringToWidth(part, 240));
        }
    }
    private static String plain(String text) {
        String value = EnumChatFormatting.getTextWithoutFormattingCodes(text);
        return value == null ? "" : value.trim();
    }
}
