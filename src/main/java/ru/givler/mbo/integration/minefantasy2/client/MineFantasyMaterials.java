package ru.givler.mbo.integration.minefantasy2.client;

import java.util.List;
import java.util.Locale;
import minefantasy.mf2.api.material.CustomMaterial;
import minefantasy.mf2.material.BaseMaterialMF;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

/** Uses the same component textures and material colours as MF2's bars and timber. */
final class MineFantasyMaterials {
    private MineFantasyMaterials() {}

    static void decorate(ItemStack stack, List<String> lines) {
        CustomMaterial primary = CustomMaterial.getMaterialFor(stack, "main_metal");
        CustomMaterial haft = CustomMaterial.getMaterialFor(stack, "haft_wood");
        decorateMaterial(lines, primary, false);
        decorateMaterial(lines, haft, true);
    }

    private static void decorateMaterial(List<String> lines, CustomMaterial material, boolean haft) {
        if (material == null) return;
        String name = StatCollector.translateToLocal("material." + material.getName() + ".name");
        String label = haft ? StatCollector.translateToLocalFormatted("item.mod_haft.name", name) : name;
        String joint = StatCollector.translateToLocalFormatted("item.mod_joint.name", name);
        String icon = haft || "wood".equals(material.type) ? "timber" : "ingot";
        String tag = "<mbo:" + icon + ":"
                + String.format(Locale.ROOT, "%06x", material.getColourInt() & 0xffffff) + ">";
        if (icon.equals("ingot")) {
            String key = material.getName();
            BaseMaterialMF base = BaseMaterialMF.getMaterial(key);
            if (key.equals("iron") || key.equals("gold")) {
                tag = "<mbo:itemtex:minecraft:" + key + "_ingot>";
            } else if (base != null) {
                tag = "<mbo:itemtex:minefantasy2:component/ingot" + base.name + ">";
            }
        }
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String text = EnumChatFormatting.getTextWithoutFormattingCodes(line);
            if (text != null && (text.trim().equals(label) || !haft && text.trim().equals(joint))) {
                int colorEnd = 0;
                while (colorEnd + 1 < line.length() && line.charAt(colorEnd) == '\u00a7') colorEnd += 2;
                lines.set(i, tag + (haft ? line.substring(0, colorEnd) + name : line));
                break;
            }
        }
    }
}
