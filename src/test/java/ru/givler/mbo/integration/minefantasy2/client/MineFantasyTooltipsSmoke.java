package ru.givler.mbo.integration.minefantasy2.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minefantasy.mf2.item.armour.ItemArmourMF;
import minefantasy.mf2.item.weapon.ItemShieldMF;
import minefantasy.mf2.item.weapon.ItemSwordMF;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import ru.givler.mbo.client.font.TooltipElements;
import sun.misc.Unsafe;

/** Real MF2 types/localizations and Attack Delay formats; no game, input device or GL context. */
public final class MineFantasyTooltipsSmoke {
    private static Unsafe unsafe;

    public static void main(String[] args) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        TooltipElements.Token armourIcon = TooltipElements.tokenAt("<mbo:armour>", 0);
        Field elementField = TooltipElements.Token.class.getDeclaredField("element");
        elementField.setAccessible(true);
        Object element = elementField.get(armourIcon);
        Field textureField = element.getClass().getDeclaredField("texture");
        textureField.setAccessible(true);
        check("mbo:textures/gui/tooltip/passiveatribute.png".equals(textureField.get(element).toString()),
                "Armour must use the exact supplied tooltip icon");
        check(armourIcon.advance == 16, "Armour icon must align with stat rail padding");
        ItemStack sword = stack(ItemSwordMF.class);
        ItemStack armour = stack(ItemArmourMF.class);
        ItemStack shield = stack(ItemShieldMF.class);
        for (String language : new String[]{"en_US", "ru_RU"}) {
            try (InputStream input = MineFantasyTooltipsSmoke.class.getResourceAsStream(
                    "/assets/minefantasy2/lang/" + language + ".lang")) {
                check(input != null, "MF2 language resource missing");
                StringTranslate.inject(input);
            }
            // These are the actual two Ariamis-attack-delay language formats.
            String translation = language.equals("ru_RU")
                    ? "item.stat.speed=Скорость: %d%%\nitem.stat.range=Дальность: %d%%\n"
                    : "item.stat.speed=Speed: %d%%\nitem.stat.range=Range: %d%%\n";
            StringTranslate.inject(new ByteArrayInputStream(translation.getBytes("UTF-8")));
            weapon(sword);
            lance(sword);
            armour(armour);
            armour(shield);
            boundKeys(sword);
        }
        materials(stack(ItemSwordMF.class));
        equipmentDetails();
        List<String> vanilla = new ArrayList<String>(Arrays.asList("Other item", "+7 Attack Damage"));
        List<String> original = new ArrayList<String>(vanilla);
        MineFantasyTooltips.decorate(new ItemStack(new Item()), vanilla);
        check(vanilla.equals(original), "Changed a non-MF2 tooltip");
        check(!MineFantasyTooltips.isEquipment(null), "Null stack accepted");
        System.out.println("MF2 tooltip icons passed: grouped weapon stats, preserved values/lore, addon percentages, armour comparisons, localization, bound keys, NBT, block endings and idempotence");
    }

    private static ItemStack stack(Class<? extends Item> type) throws Exception {
        final Item item = (Item) unsafe.allocateInstance(type);
        Field delegate = Item.class.getDeclaredField("delegate");
        unsafe.putObject(item, unsafe.objectFieldOffset(delegate),
                new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(item, Item.class));
        ItemStack stack = new ItemStack(item);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("MF_CustomTag0", "Custom lore");
        stack.setTagCompound(tag);
        return stack;
    }

    private static void equipmentDetails() throws Exception {
        ItemStack tool = stack(minefantasy.mf2.item.tool.ItemPickMF.class);
        check(MineFantasyTooltips.isEquipment(tool), "MF2 pickaxe omitted from equipment layout");
        ItemStack weapon = stack(ru.givler.mbo.item.weapon.ItemWeaponBase.class);
        weapon.getItem().setMaxDamage(300);
        weapon.setItemDamage(4);
        weapon.addEnchantment(net.minecraft.enchantment.Enchantment.sharpness, 3);
        String enchantment = net.minecraft.enchantment.Enchantment.sharpness.getTranslatedName(3);
        String durability = tr("tooltip.mbo.durability") + ": 296 / 300";
        List<String> rows = new ArrayList<String>(Arrays.asList("MBO weapon", enchantment,
                "Custom lore", "+2 " + tr("attribute.name.generic.attackDamage"), durability,
                "Durability: 296 / 300"));
        String nbt = weapon.getTagCompound().toString();
        MineFantasyTooltips.decorate(weapon, rows);
        ru.givler.mbo.client.gui.EquipmentTooltipDetails.decorate(weapon, rows, true);
        check(find(rows, "<mbo:durability>") > find(rows, "<mbo:weapon>")
                && find(rows, "<mbo:durability>") < find(rows, "<mbo:enchants>"), "Durability must follow stats and precede enchantments");
        check(find(rows, "<mbo:enchants>") > find(rows, "<mbo:weapon>"), "Enchantments missing their separate group");
        check(rows.get(find(rows, "<mbo:enchants>")).endsWith(enchantment), "Enchantment name or level changed");
        int count = 0;
        for (String row : rows) if (row.contains(durability)) count++;
        check(count == 1, "Durability duplicated");
        for (String row : rows) check(!row.contains("Durability: 296 / 300") || row.contains("<mbo:durability>"),
                "Vanilla 1.7.10 hardcoded advanced durability row survived");
        check(nbt.equals(weapon.getTagCompound().toString()), "Formatting changed enchantment or item NBT");
    }

    private static void weapon(ItemStack stack) {
        String speed = StatCollector.translateToLocalFormatted("item.stat.speed", 75);
        String range = StatCollector.translateToLocalFormatted("item.stat.range", 125);
        List<String> lines = new ArrayList<String>(Arrays.asList("Named sword", "Custom lore", "",
                "\u00a79+7 " + tr("attribute.name.generic.attackDamage"),
                "\u00a7a" + tr("attribute.weapon.penetrateArmor") + " 10%", "",
                "Quality: excellent", "\u00a79" + speed, "\u00a79" + range,
                "Crafter: Example", "Durability: 15 / 20"));
        List<String> before = new ArrayList<String>(lines);
        NBTTagCompound nbt = stack.getTagCompound();
        MineFantasyTooltips.decorate(stack, lines);
        check(find(lines, speed) < find(lines, range), "Handling order is speed then reach");
        check(lines.get(3).contains("<mbo:weapon>"), "Damage comes first");
        check(lines.get(find(lines, range)).endsWith(range), "Attack Delay percentage reach was converted");
        check(find(lines, "Quality: excellent") > find(lines, range), "Quality interrupts statistics");
        TooltipElements.expandBlocks(lines);
        sameContent(before, lines);
        check(stack.getTagCompound() == nbt && "Custom lore".equals(nbt.getString("MF_CustomTag0")), "Tooltip changed item data");
        List<String> once = new ArrayList<String>(lines);
        MineFantasyTooltips.decorate(stack, lines);
        TooltipElements.expandBlocks(lines);
        check(lines.equals(once), "Repeated formatting added duplicate icons/padding");
    }

    private static void armour(ItemStack stack) {
        List<String> lines = new ArrayList<String>(Arrays.asList("Named armour", "",
                tr("attribute.armour.protection"),
                "\u00a79" + tr("attribute.armour.rating.0") + " 25 \u00a7a(+5)",
                "\u00a79" + tr("attribute.armour.rating.2") + " 17 \u00a7c(-3)", "",
                tr("attribute.armour.fireresistance") + ": 20%",
                tr("attribute.armour.magicresistance") + ": 30%", "NBT description"));
        List<String> before = new ArrayList<String>(lines);
        MineFantasyTooltips.decorate(stack, lines);
        check(lines.get(2).startsWith("<mbo:statline><mbo:armour>"), "Existing armour heading was not decorated");
        check(lines.get(find(lines, "<mbo:magicguard>")).contains("<mbo:statline>"), "Magic resistance needs its own icon");
        check(lines.get(find(lines, "<mbo:magicguard>") - 1).contains("<mbo:fireguard>"),
                "Fire and magic resistance must occupy consecutive rows");
        check(lines.get(6).contains("<mbo:fireguard>"), "Fire resistance needs the potion icon");
        TooltipElements.expandBlocks(lines);
        sameContent(before, lines);
        check(!lines.get(find(lines, "<mbo:magicguard>")).contains("<mbo:statline"), "Stray rail under standalone magic icon");
        check(!lines.get(find(lines, "NBT description")).contains("<mbo:pad:"), "Resistance block indented unrelated description");
    }

    private static void lance(ItemStack stack) {
        List<String> lines = new ArrayList<String>(Arrays.asList("Lance",
                "+3 " + tr("attribute.weapon.extendedReach"),
                "+4 " + tr("attribute.weapon.mountedBonus"),
                "+26.2 " + tr("attribute.weapon.joustDam"), "",
                "+2 " + tr("attribute.name.generic.attackDamage"),
                tr("attribute.weapon.piercing") + " 90%"));
        List<String> before = new ArrayList<String>(lines);
        MineFantasyTooltips.decorate(stack, lines);
        TooltipElements.expandBlocks(lines);
        sameContent(before, lines);
        check(lines.get(1).contains("<mbo:weapon>"), "Lance damage must come before reach and conditional bonuses");
        check(find(lines, "<mbo:handling>") > 2, "Reach interrupts damage distribution");
        check(find(lines, "<mbo:mounted>") > find(lines, "<mbo:handling>"), "Mounted bonuses need their own group");
        int swords = 0;
        for (String line : lines) if (line.contains("<mbo:weapon>")) swords++;
        check(swords == 1, "Repeated sword icons on lance");
        check(lines.get(2).startsWith("<mbo:pad:16>"), "Damage types lost alignment");
        check(lines.get(lines.size() - 2).startsWith("<mbo:pad:16>"), "Mounted bonuses lost alignment");
        check(lines.get(lines.size() - 1).equals("<mbo:end>"), "Missing bottom space below final rail");
    }

    private static int find(List<String> lines, String text) {
        for (int i = 0; i < lines.size(); i++) if (lines.get(i).contains(text)) return i;
        throw new AssertionError("Missing row: " + text);
    }

    private static void sameContent(List<String> before, List<String> after) {
        List<String> original = content(before);
        List<String> formatted = content(after);
        java.util.Collections.sort(original);
        java.util.Collections.sort(formatted);
        check(original.equals(formatted), "Lost, duplicated or rewrote a statistic/lore row");
    }

    private static List<String> content(List<String> lines) {
        List<String> result = new ArrayList<String>();
        for (String line : lines) {
            String text = line.replaceAll("<mbo:[^>]+>", "").trim();
            if (!text.isEmpty()) result.add(text);
        }
        return result;
    }

    private static void boundKeys(ItemStack stack) {
        String hint = StatCollector.translateToLocalFormatted("info.tooltip.moreinfo", "LSHIFT");
        List<String> lines = new ArrayList<String>(Arrays.asList("Name",
                StatCollector.translateToLocalFormatted("attribute.weightKg.name", "3.5"), hint));
        MineFantasyTooltips.decorate(stack, lines);
        check(lines.size() == 3 && lines.get(1).startsWith("<mbo:weight>"), "Weight row changed or lost icon");
        check(lines.get(2).contains("<mbo:shift>"), "Actual Shift binding was not shown as an icon");
        check(!lines.get(2).matches(".*\\[\\s*<mbo:shift>\\s*\\].*"), "Icon retained redundant brackets");
        for (String key : new String[]{"LEFT SHIFT", "RIGHT SHIFT", "LEFT_SHIFT", "LSHIFT", "RSHIFT"}) {
            List<String> alternate = new ArrayList<String>(Arrays.asList("Name",
                    StatCollector.translateToLocalFormatted("info.tooltip.moreinfo", key)));
            MineFantasyTooltips.decorate(stack, alternate);
            check(alternate.get(1).contains("<mbo:shift>") && !alternate.get(1).contains("["),
                    "Missing icon for client key name: " + key);
        }
        String rebound = StatCollector.translateToLocalFormatted("info.tooltip.moreinfo", "G");
        lines = new ArrayList<String>(Arrays.asList("Name", rebound));
        MineFantasyTooltips.decorate(stack, lines);
        check(lines.get(1).equals(rebound), "Rebound key was incorrectly advertised as Shift");
    }

    private static void materials(ItemStack stack) throws Exception {
        minefantasy.mf2.api.material.CustomMaterial metal = new minefantasy.mf2.api.material.CustomMaterial(
                "smoke_metal", "metal", 1, 1, 1, 1, 1, 1, 1).setColour(30, 60, 90).register();
        minefantasy.mf2.api.material.CustomMaterial wood = new minefantasy.mf2.api.material.CustomMaterial(
                "smoke_wood", "wood", 1, 1, 1, 1, 1, 1, 1).setColour(120, 80, 40).register();
        StringTranslate.inject(new ByteArrayInputStream(("material.smoke_metal.name=Test metal\n"
                + "material.smoke_wood.name=Test wood\nitem.mod_haft.name=Handle: %s\n").getBytes("UTF-8")));
        NBTTagCompound materials = new NBTTagCompound();
        materials.setString("main_metal", metal.getName());
        materials.setString("haft_wood", wood.getName());
        stack.getTagCompound().setTag("MF_CustomMaterials", materials);
        String before = stack.getTagCompound().toString();
        List<String> rows = new ArrayList<String>(Arrays.asList("Weapon", "Test metal", "Handle: Test wood"));
        MineFantasyTooltips.decorate(stack, rows);
        check(rows.get(1).equals("<mbo:ingot:1e3c5a>Test metal"), "Wrong metal icon or material colour");
        check(rows.get(2).equals("<mbo:timber:785028>Test wood"), "Wrong timber icon, colour or redundant handle label");
        check(before.equals(stack.getTagCompound().toString()), "Material decoration changed item NBT");
        TooltipElements.Token token = TooltipElements.tokenAt(rows.get(2), 0);
        Field tint = TooltipElements.Token.class.getDeclaredField("tint");
        tint.setAccessible(true);
        check(tint.getInt(token) == 0x785028 && token.advance == 16, "Renderer lost timber tint/alignment");
        check(TooltipElements.tokenAt("<mbo:timber:invalid>", 0) == null, "Malformed material tag accepted");
    }

    private static void unchangedRows(List<String> before, List<String> after) {
        check(before.size() == after.size(), "Added or removed tooltip sections/rows");
        for (int i = 0; i < before.size(); i++) {
            String text = after.get(i).replaceAll("<mbo:[^>]+>", "").trim();
            check(before.get(i).trim().equals(text), "Reordered or rewrote row " + i + ": " + text);
        }
    }

    private static String tr(String key) { return StatCollector.translateToLocal(key); }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
