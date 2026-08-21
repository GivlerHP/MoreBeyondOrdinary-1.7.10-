package ru.givler.mbo.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Creative-only showcase for the image tags supported by MBO tooltips. */
public class ItemTooltipDemo extends ItemBase {
    public ItemTooltipDemo() {
        super("TooltipDemo", "glyph/glyph_void", 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        lines.add(EnumChatFormatting.GRAY + tr("tooltip.mbo.demo.inline"));
        lines.add("<mbo:weight> " + EnumChatFormatting.GOLD + "3.0 kg  "
                + "<mbo:passiveatribute> " + EnumChatFormatting.GREEN + "+10%  "
                + "<mbo:voklicatel> " + EnumChatFormatting.YELLOW + tr("tooltip.mbo.demo.warning"));
        lines.add(EnumChatFormatting.WHITE + "<mbo:alt> <mbo:alt_a>  "
                + "<mbo:ctrl> <mbo:ctrl_a>  <mbo:shift> <mbo:shift_a>");
        lines.add(EnumChatFormatting.WHITE + "<mbo:ctrlalt> <mbo:ctrlalt_a>");

        addSection(lines, "<mbo:swordstat>", "tooltip.mbo.demo.weapon",
                "10.0 " + tr("tooltip.mbo.demo.damage"), "2.0 " + tr("tooltip.mbo.demo.speed"),
                "0.2 " + tr("tooltip.mbo.demo.critical"), tr("tooltip.mbo.demo.range") + ": 2.75");
        addSection(lines, "<mbo:upatribute>", "tooltip.mbo.demo.bonus",
                tr("tooltip.mbo.demo.power") + ": D", tr("tooltip.mbo.demo.intellect") + ": -",
                tr("tooltip.mbo.demo.dexterity") + ": A", tr("tooltip.mbo.demo.spirituality") + ": -");
        addSection(lines, "<mbo:needatribute>", "tooltip.mbo.demo.required",
                tr("tooltip.mbo.demo.power") + ": 10", tr("tooltip.mbo.demo.intellect") + ": -",
                tr("tooltip.mbo.demo.dexterity") + ": 5", tr("tooltip.mbo.demo.spirituality") + ": 3");
        addSection(lines, "<mbo:plaska>", "tooltip.mbo.demo.passive",
                tr("tooltip.mbo.demo.vampirism") + ": 10%");
    }

    private static void addSection(List lines, String tag, String titleKey, String... content) {
        lines.add(tag + EnumChatFormatting.YELLOW + tr(titleKey));
        for (String line : content) lines.add(EnumChatFormatting.GRAY + line);
    }

    private static String tr(String key) {
        return StatCollector.translateToLocal(key);
    }
}
