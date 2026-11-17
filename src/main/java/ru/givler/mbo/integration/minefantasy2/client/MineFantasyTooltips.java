package ru.givler.mbo.integration.minefantasy2.client;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.client.font.TooltipElements;

/** Presents existing MF2/Attack Delay statistics without recomputing their values. */
public final class MineFantasyTooltips {
    private static final java.util.regex.Pattern FORMAT = java.util.regex.Pattern.compile(
            "%(?:[0-9]+\\$)?[-#+ 0,(]*[0-9]*(?:\\.[0-9]+)?[sdf]");
    private static final java.util.regex.Pattern NUMBER = java.util.regex.Pattern.compile(
            "[\\s\\d.,+\\-:%()x\\u00d7/]+");
    private static final String[] CORE = {
        "attribute.name.generic.attackDamage", "attribute.weapon.attackSpeed",
        "item.stat.speed", "item.stat.range",
        "attribute.weapon.extendedReach", "attribute.weapon.penetrateArmor",
        "attribute.weapon.joustDam", "attribute.weapon.mountedBonus",
        "attribute.weapon.cutting", "attribute.weapon.piercing", "attribute.weapon.blunt",
        "attribute.armour.protection", "attribute.armour.rating.0",
        "attribute.armour.rating.1", "attribute.armour.rating.2",
        "attribute.bowPower.name", "attribute.crossbow.power.name", "attribute.crossbow.recoil.name",
        "attribute.crossbow.speed.name", "attribute.crossbow.spread.name",
        "attribute.crossbow.capacity.name", "attribute.crossbow.bash.name"
    };
    private static final String[] RESISTANCES = {
        "attribute.armour.fireresistance", "attribute.armour.magicresistance"
    };
    private static final String[] PASSIVES = {
        "attribute.weapon.backstab", "attribute.weapon.daze", "attribute.modifier.silver"
    };
    private static final String[] WEIGHTS = {
        "attribute.weightKg.name", "attribute.weightg.name", "attribute.weightt.name"
    };

    private MineFantasyTooltips() {}

    public static boolean isEquipment(ItemStack stack) {
        return equipmentKind(stack) != 0;
    }

    private static int equipmentKind(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return 0;
        for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
            String name = type.getName();
            if (name.equals("minefantasy.mf2.api.armour.ItemArmourMFBase")
                    || name.equals("minefantasy.mf2.item.armour.ItemHorseArmorMF")
                    || name.equals("minefantasy.mf2.item.weapon.ItemShieldMF")) return 2;
            if (name.equals("minefantasy.mf2.item.weapon.ItemWeaponMF")
                    || name.equals("minefantasy.mf2.item.archery.ItemBowMF")
                    || name.equals("minefantasy.mf2.item.gadget.ItemCrossbow")) return 1;
        }
        return 0;
    }

    public static void decorate(ItemStack stack, List<String> lines) {
        int kind = equipmentKind(stack);
        if (kind == 0 || lines == null || lines.size() < 2) return;
        for (int i = 1; i < lines.size(); i++) {
            if (TooltipElements.containsTag(lines.get(i))) return;
        }

        // Translate for each tooltip so language/resource-pack reloads cannot leave stale label caches.
        Labels labels = new Labels();
        if (kind == 1) {
            layoutWeapon(lines, labels);
            MineFantasyMaterials.decorate(stack, lines);
            return;
        }
        int previousBlock = 0;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String plain = plain(line);
            int block = matches(plain, labels.core) ? 1
                    : labels.magic.matches(plain) ? 3 : matches(plain, labels.resistances) ? 2 : 0;
            String prefix = "";
            if (block != previousBlock) {
                if (previousBlock != 0) prefix = "<mbo:end>";
                if (block != 0) {
                    String icon = block == 3 ? "magicguard" : block == 2 ? "fireguard" : "armour";
                    prefix += "<mbo:statline><mbo:" + icon + ">";
                }
            }
            previousBlock = block;
            if (block == 0) {
                line = matches(plain, labels.passives) ? "<mbo:passiveatribute> " + line
                        : decorateInline(line, labels);
            }
            lines.set(i, prefix + line);
        }
        MineFantasyMaterials.decorate(stack, lines);
    }

    /** Damage first, handling second, conditional mounted bonuses last. No invented stat rows. */
    private static void layoutWeapon(List<String> lines, Labels labels) {
        List<String> damage = new ArrayList<String>();
        List<String> speed = new ArrayList<String>();
        List<String> reach = new ArrayList<String>();
        List<String> mounted = new ArrayList<String>();
        List<String> remaining = new ArrayList<String>();
        int anchor = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String text = plain(line);
            List<String> target = i == 0 ? null : matches(text, labels.damage) ? damage
                    : matches(text, labels.speed) ? speed : matches(text, labels.reach) ? reach
                    : matches(text, labels.mounted) ? mounted : null;
            if (target != null) {
                if (anchor < 0) anchor = remaining.size();
                if (target == damage && labels.attack.matches(text)) target.add(0, line);
                else target.add(line);
            } else {
                remaining.add(i == 0 ? line : matches(text, labels.passives)
                        ? "<mbo:passiveatribute> " + line : decorateInline(line, labels));
            }
        }
        if (anchor < 0) {
            lines.clear();
            lines.addAll(remaining);
            return;
        }
        // Empty rows formerly separating scattered stats belong to the new groups.
        while (anchor < remaining.size() && plain(remaining.get(anchor)).isEmpty()) remaining.remove(anchor);
        List<String> stats = new ArrayList<String>();
        if (!damage.isEmpty()) {
            stats.add("<mbo:statline><mbo:weapon>" + damage.get(0));
            stats.addAll(damage.subList(1, damage.size()));
        }
        if (!speed.isEmpty() || !reach.isEmpty()) {
            if (!stats.isEmpty()) stats.add("<mbo:end>");
            List<String> handling = new ArrayList<String>(speed);
            handling.addAll(reach);
            stats.add("<mbo:end><mbo:statline><mbo:handling>" + handling.get(0));
            stats.addAll(handling.subList(1, handling.size()));
        }
        if (!mounted.isEmpty()) {
            if (!stats.isEmpty()) stats.add("<mbo:end>");
            stats.add("<mbo:end><mbo:statline><mbo:mounted>" + mounted.get(0));
            stats.addAll(mounted.subList(1, mounted.size()));
        }
        if (anchor < remaining.size()) remaining.set(anchor, "<mbo:end>" + remaining.get(anchor));
        remaining.addAll(anchor, stats);
        lines.clear();
        lines.addAll(remaining);
    }

    private static String decorateInline(String line, Labels labels) {
        String plain = plain(line);
        if (matches(plain, labels.weights)) return "<mbo:weight> " + line;
        String key = labels.hint.value(plain);
        if (key == null) return line;
        String upper = key.toUpperCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "");
        String icon = upper.equals("SHIFT") || upper.equals("LSHIFT") || upper.equals("RSHIFT")
                || upper.equals("LEFTSHIFT") || upper.equals("RIGHTSHIFT") ? "shift"
                : upper.equals("CTRL") || upper.equals("LCONTROL") || upper.equals("RCONTROL")
                || upper.equals("LEFTCONTROL") || upper.equals("RIGHTCONTROL")
                || upper.equals("LEFTCTRL") || upper.equals("RIGHTCTRL") ? "ctrl"
                : upper.equals("ALT") || upper.equals("LMENU") || upper.equals("RMENU")
                || upper.equals("LEFTALT") || upper.equals("RIGHTALT") ? "alt" : null;
        // A rebound key must keep its actual name; do not advertise Shift unconditionally.
        if (icon == null) return line;
        String tag = "<mbo:" + icon + ">";
        String bracketed = "\\[\\s*" + java.util.regex.Pattern.quote(key) + "\\s*\\]";
        return line.replaceAll(bracketed, java.util.regex.Matcher.quoteReplacement(tag)).replace(key, tag);
    }

    private static String plain(String line) {
        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(line);
        return stripped == null ? "" : stripped.trim();
    }

    private static boolean matches(String line, Pattern[] patterns) {
        for (Pattern pattern : patterns) if (pattern.matches(line)) return true;
        return false;
    }

    private static Pattern[] translate(String[] keys) {
        Pattern[] patterns = new Pattern[keys.length];
        for (int i = 0; i < keys.length; i++) patterns[i] = new Pattern(keys[i]);
        return patterns;
    }

    private static final class Labels {
        final Pattern[] core = translate(CORE);
        final Pattern attack = new Pattern("attribute.name.generic.attackDamage");
        final Pattern[] damage = translate(new String[]{
            "attribute.name.generic.attackDamage", "attribute.weapon.penetrateArmor",
            "attribute.weapon.cutting", "attribute.weapon.piercing", "attribute.weapon.blunt",
            "attribute.bowPower.name", "attribute.crossbow.power.name", "attribute.crossbow.bash.name"
        });
        final Pattern[] speed = translate(new String[]{
            "item.stat.speed", "attribute.weapon.attackSpeed", "attribute.crossbow.speed.name"
        });
        final Pattern[] reach = translate(new String[]{"item.stat.range", "attribute.weapon.extendedReach"});
        final Pattern[] mounted = translate(new String[]{"attribute.weapon.mountedBonus", "attribute.weapon.joustDam"});
        final Pattern[] resistances = translate(RESISTANCES);
        final Pattern magic = new Pattern("attribute.armour.magicresistance");
        final Pattern[] passives = translate(PASSIVES);
        final Pattern[] weights = translate(WEIGHTS);
        final Pattern hint = new Pattern("info.tooltip.moreinfo");
    }

    /** Matches translated labels and numeric modifier rows, never recomputing an equipment stat. */
    private static final class Pattern {
        final String label;
        final String prefix;
        final String suffix;

        Pattern(String key) {
            String text = plain(StatCollector.translateToLocal(key));
            Matcher format = FORMAT.matcher(text);
            boolean formatted = format.find();
            label = text.equals(key) ? "" : text;
            prefix = formatted ? text.substring(0, format.start()).replace("%%", "%") : null;
            suffix = formatted ? text.substring(format.end()).replace("%%", "%") : null;
        }

        String value(String line) {
            if (prefix == null || !line.startsWith(prefix) || !line.endsWith(suffix)
                    || line.length() < prefix.length() + suffix.length()) return null;
            return line.substring(prefix.length(), line.length() - suffix.length());
        }

        boolean matches(String line) {
            if (label.isEmpty()) return false;
            if (prefix != null) {
                String value = value(line);
                return value != null && numeric(value);
            }
            if (line.equals(label)) return true;
            if (line.startsWith(label)) return numeric(line.substring(label.length()));
            if (line.endsWith(label)) return numeric(line.substring(0, line.length() - label.length()));
            return false;
        }

        private static boolean numeric(String text) {
            return !text.isEmpty() && NUMBER.matcher(text).matches();
        }
    }
}
