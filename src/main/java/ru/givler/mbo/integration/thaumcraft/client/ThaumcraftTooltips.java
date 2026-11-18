package ru.givler.mbo.integration.thaumcraft.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.integration.thaumcraft.item.staff.ItemStaffBasic;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.Thaumcraft;

/** Decorates the values already produced by Thaumcraft and its addons. */
public final class ThaumcraftTooltips {
    private static final String END = "<mbo:end>";

    private ThaumcraftTooltips() {}

    public static void decorate(ItemStack stack, List<String> lines) {
        Item item = stack.getItem();
        boolean wand = item instanceof ItemWandCasting;
        boolean focus = item instanceof ItemFocusBasic;
        boolean essentia = item instanceof IEssentiaContainerItem;

        List<Aspect> stored = wand ? storedAspects(stack) : new ArrayList<Aspect>();
        ItemStack installed = wand ? ((ItemWandCasting) item).getFocusItem(stack) : null;
        String focusName = installed == null ? null : plain(installed.getDisplayName());
        String capacity = StatCollector.translateToLocal("item.capacity.text");
        String castCost = StatCollector.translateToLocal("item.Focus.cost1");
        String tickCost = StatCollector.translateToLocal("item.Focus.cost2");
        String discount = StatCollector.translateToLocal("tc.visdiscount");
        String warp = StatCollector.translateToLocal("item.warping");
        String armorEfficiency = StatCollector.translateToLocal("item.Focus.armorEfficiency");
        ItemStack focusStack = focus ? stack : installed;
        short[] upgrades = focusStack != null && focusStack.getItem() instanceof ItemFocusBasic
                ? ((ItemFocusBasic) focusStack.getItem()).getAppliedUpgrades(focusStack) : new short[0];
        boolean inSection = false;
        int lastWandRow = -1;

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("<mbo:")) continue;
            String label = plain(line);
            String statIcon = null;
            if (label.startsWith(discount + ":") || label.startsWith(discount + " (")) {
                statIcon = icon(new ResourceLocation("thaumcraft", "textures/foci/frugal.png"));
            } else if (label.startsWith(warp + " ")) {
                statIcon = icon(Aspect.ELDRITCH);
            } else if ((focus || installed != null) && label.startsWith(armorEfficiency + " ")) {
                statIcon = "<mbo:armour>";
            }
            if (statIcon != null) {
                lines.set(i, END + statIcon + line);
                if (wand && inSection) lastWandRow = i;
                inSection = false;
                continue;
            }
            if (!wand && !focus && !essentia) continue;
            String section = null;
            if (wand && label.startsWith(capacity + " ")) section = "viscapacity";
            else if ((wand || focus) && (label.equals(castCost) || label.equals(tickCost))) section = "viscost";
            else if (focusName != null && label.equals(focusName)) section = "wandfocus";

            if (section != null) {
                String sectionIcon = section.equals("wandfocus") ? focusIcon(installed, "wandfocus")
                        : focus && section.equals("viscost") ? focusIcon(stack, "viscost")
                        : "<mbo:" + section + ">";
                lines.set(i, END + "<mbo:statline>" + sectionIcon + line);
                if (wand) lastWandRow = i;
                inSection = true;
                continue;
            }
            String decorated = aspectValues(line, label, stored);
            if (decorated.equals(line)) decorated = upgradeIcon(line, label, upgrades);
            if (!decorated.equals(line)) {
                lines.set(i, decorated);
                if (wand && inSection) lastWandRow = i;
            } else if (inSection) {
                lines.set(i, END + line);
                inSection = false;
            }
        }
        if (lastWandRow >= 0 && (!stored.isEmpty() || installed != null) && !Thaumcraft.proxy.isShiftKeyDown()) {
            lines.add(lastWandRow + 1, END + EnumChatFormatting.GRAY + "<mbo:shift> "
                    + StatCollector.translateToLocal("tooltip.mbo.thaumcraft.details"));
        }
    }

    private static String focusIcon(ItemStack focus, String fallback) {
        IIcon sprite = focus.getIconIndex();
        return sprite == null ? "<mbo:" + fallback + ">" : "<mbo:focusicon:" + sprite.getIconName() + ">";
    }

    private static String upgradeIcon(String line, String label, short[] upgrades) {
        for (short id : upgrades) {
            if (id < 0 || id >= FocusUpgradeType.types.length) continue;
            FocusUpgradeType upgrade = FocusUpgradeType.types[id];
            if (upgrade == null) continue;
            String name = plain(upgrade.getLocalizedName());
            if (label.equals(name) || label.matches(java.util.regex.Pattern.quote(name) + " [IVXLCDM0-9]+")) {
                return icon(upgrade.icon) + line;
            }
        }
        return line;
    }

    private static List<Aspect> storedAspects(ItemStack stack) {
        List<Aspect> aspects = new ArrayList<Aspect>();
        if (stack.getItem() instanceof ItemStaffBasic) {
            AspectList cost = ((ItemStaffBasic) stack.getItem()).getVisCost(stack);
            if (cost != null) {
                for (Aspect aspect : cost.getAspects()) if (aspect != null) aspects.add(aspect);
            }
        } else if (stack.hasTagCompound()) {
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (stack.getTagCompound().hasKey(aspect.getTag())) aspects.add(aspect);
            }
        }
        return aspects;
    }

    private static String aspectValues(String line, String label, List<Aspect> stored) {
        if (label.matches("[0-9]+(?:[.,][0-9]+)?(?: \\| [0-9]+(?:[.,][0-9]+)?)*")) {
            String[] values = line.split(" \\| ");
            if (stored.isEmpty() || stored.size() != values.length) return line;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) result.append("  ");
                result.append(icon(stored.get(i))).append(values[i]);
            }
            return result.toString();
        }
        for (Aspect aspect : Aspect.aspects.values()) {
            if (label.startsWith(aspect.getName() + " x ")) return icon(aspect) + line.trim();
        }
        return line;
    }

    private static String icon(Aspect aspect) {
        ResourceLocation image = aspect.getImage();
        if (image == null) return "";
        String tint = String.format(Locale.ROOT, "%06x", aspect.getColor() & 0xffffff);
        return "<mbo:aspect:" + image.getResourceDomain() + ":" + image.getResourcePath() + ":" + tint + ">";
    }

    private static String icon(ResourceLocation image) {
        return image == null ? "" : "<mbo:aspect:" + image.getResourceDomain() + ":" + image.getResourcePath() + ">";
    }

    private static String plain(String line) {
        return EnumChatFormatting.getTextWithoutFormattingCodes(line).trim();
    }
}
