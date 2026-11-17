package ru.givler.mbo.client.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.integration.minefantasy2.client.MineFantasyTooltips;

/** Secondary equipment information kept outside damage, resistance and lore sections. */
public final class EquipmentTooltipDetails {
    private EquipmentTooltipDetails() {}

    public static void decorate(ItemStack stack, List<String> rows, boolean icons) {
        if (stack == null || rows.size() < 1) return;
        boolean supported = MineFantasyTooltips.isEquipment(stack);
        if (icons && supported) moveEnchantments(stack, rows);
        if (!stack.isItemDamaged()) return;
        int max = stack.getMaxDamage();
        int remaining = max - stack.getItem().getDisplayDamage(stack);
        String values = remaining + " / " + max;
        String label = StatCollector.translateToLocal("tooltip.mbo.durability");
        String vanilla = StatCollector.translateToLocalFormatted("item.durability", remaining, max);
        for (int i = rows.size() - 1; i > 0; i--) {
            String plain = plain(rows.get(i));
            if (plain.equals(label + ": " + values) || plain.equals(vanilla)
                    || plain.equals("Durability: " + values)) {
                if (rows.get(i).contains("<mbo:end>")) rows.set(i, "<mbo:end>");
                else rows.remove(i);
            }
        }
        String line = EnumChatFormatting.GRAY + label + ": " + values;
        if (!icons || !supported) { rows.add(line); return; }
        int anchor = rows.size();
        for (int i = 1; i < rows.size(); i++) {
            String row = rows.get(i);
            if (row.contains("<mbo:enchants>")) {
                anchor = i;
                if (i > 1 && rows.get(i - 1).equals("<mbo:end>")) anchor--;
                break;
            }
        }
        List<String> section = new ArrayList<String>();
        if (anchor > 1 && !plain(rows.get(anchor - 1)).isEmpty()) section.add("<mbo:end>");
        section.add("<mbo:end><mbo:durability>" + line);
        section.add("<mbo:end>");
        rows.addAll(anchor, section);
    }

    private static void moveEnchantments(ItemStack stack, List<String> rows) {
        for (String row : rows) if (row.contains("<mbo:enchants>")) return;
        Set<String> names = new HashSet<String>();
        collect(stack.getEnchantmentTagList(), names);
        if (stack.hasTagCompound()) collect(stack.getTagCompound().getTagList("StoredEnchantments", 10), names);
        if (names.isEmpty()) return;
        List<String> enchantments = new ArrayList<String>();
        for (int i = 1; i < rows.size();) {
            if (names.contains(plain(rows.get(i)))) enchantments.add(rows.remove(i));
            else i++;
        }
        if (enchantments.isEmpty()) return;
        rows.add("<mbo:end>");
        rows.add("<mbo:statline><mbo:enchants>" + enchantments.get(0));
        rows.addAll(enchantments.subList(1, enchantments.size()));
        rows.add("<mbo:end>");
    }

    private static void collect(NBTTagList tags, Set<String> names) {
        if (tags == null) return;
        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound entry = tags.getCompoundTagAt(i);
            int id = entry.getShort("id");
            if (id >= 0 && id < Enchantment.enchantmentsList.length && Enchantment.enchantmentsList[id] != null)
                names.add(plain(Enchantment.enchantmentsList[id].getTranslatedName(entry.getShort("lvl"))));
        }
    }
    private static String plain(String row) {
        String value = EnumChatFormatting.getTextWithoutFormattingCodes(row);
        return value == null ? "" : value.replaceAll("<mbo:[^>]+>", "").trim();
    }
}
