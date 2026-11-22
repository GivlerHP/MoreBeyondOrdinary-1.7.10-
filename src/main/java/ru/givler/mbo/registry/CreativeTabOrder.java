package ru.givler.mbo.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/** Explicit positions for block families; unrelated tab entries retain their order. */
public final class CreativeTabOrder {
    private static final List<Family> FAMILIES = new ArrayList<Family>();
    private CreativeTabOrder() {}

    public static void register(CreativeTabs tab, ItemStack anchor, ItemStack... parts) {
        List<ItemStack> entries = new ArrayList<ItemStack>();
        for (ItemStack part : parts) if (part != null && part.getItem() != null) entries.add(part);
        if (!entries.isEmpty()) FAMILIES.add(new Family(tab, anchor, entries));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void arrange(CreativeTabs tab, List items) {
        for (Family family : FAMILIES) {
            if (family.tab != tab) continue;
            for (int i = items.size() - 1; i >= 0; i--) {
                Object value = items.get(i);
                if (!(value instanceof ItemStack)) continue;
                for (ItemStack part : family.parts) {
                    if (same((ItemStack) value, part)) { items.remove(i); break; }
                }
            }
            int at = items.size();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) instanceof ItemStack && same((ItemStack) items.get(i), family.anchor)) at = i + 1;
            }
            for (ItemStack part : family.parts) items.add(at++, part.copy());
        }
    }

    private static boolean same(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
                && ItemStack.areItemStackTagsEqual(a, b);
    }

    private static final class Family {
        final CreativeTabs tab;
        final ItemStack anchor;
        final List<ItemStack> parts;
        Family(CreativeTabs tab, ItemStack anchor, List<ItemStack> parts) {
            this.tab = tab; this.anchor = anchor; this.parts = parts;
        }
    }
}
