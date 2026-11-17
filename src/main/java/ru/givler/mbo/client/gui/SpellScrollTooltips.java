package ru.givler.mbo.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.client.font.ModernFontRenderer;

/** Decorates only the rows emitted by the scroll, preserving spell/element/tier colours. */
public final class SpellScrollTooltips {
    private SpellScrollTooltips() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void decorate(List rows, int start, boolean valid) {
        if (!(Minecraft.getMinecraft().fontRenderer instanceof ModernFontRenderer)) return;
        format(rows, start, valid);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void decorate(List rows, int start, ru.givler.mbo.magic.api.MagicElement element) {
        if (!(Minecraft.getMinecraft().fontRenderer instanceof ModernFontRenderer)) return;
        format(rows, start, true, element);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void format(List rows, int start, boolean valid) {
        format(rows, start, valid, ru.givler.mbo.magic.api.MagicElement.ARCANE);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void format(List rows, int start, boolean valid, ru.givler.mbo.magic.api.MagicElement element) {
        if (start >= rows.size()) return;
        if (!valid) { rows.set(start, "<mbo:voklicatel>" + rows.get(start)); return; }
        rows.set(start, "<mbo:element_" + element.name().toLowerCase(java.util.Locale.ROOT)
                + "><mbo:pad:5>" + rows.get(start));
        if (start + 1 < rows.size()) rows.set(start + 1, "<mbo:upbonus>" + rows.get(start + 1));
        if (start + 2 >= rows.size()) return;
        String hint = StatCollector.translateToLocal("item.spellScroll.holdShift");
        String row = String.valueOf(rows.get(start + 2));
        if (row.endsWith(hint)) {
            rows.set(start + 2, row.replace("SHIFT", "<mbo:shift>"));
        } else {
            rows.add(start + 2, "<mbo:end>");
            rows.set(start + 3, "<mbo:statline><mbo:spellscroll>" + rows.get(start + 3));
            rows.add("<mbo:end>");
        }
    }
}
