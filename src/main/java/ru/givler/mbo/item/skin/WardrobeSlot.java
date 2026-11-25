package ru.givler.mbo.item.skin;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import minefantasy.mf2.api.armour.ArmourDesign;
import minefantasy.mf2.api.armour.ItemArmourMFBase;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.StatCollector;

public enum WardrobeSlot {
    LIGHT_HEAD, LIGHT_CHEST, LIGHT_LEGS, LIGHT_FEET,
    HEAVY_HEAD, HEAVY_CHEST, HEAVY_LEGS, HEAVY_FEET,
    MAGIC_HEAD, MAGIC_CHEST, MAGIC_LEGS, MAGIC_FEET,
    SWORD("ItemSwordMF"), GREATSWORD("ItemGreatswordMF"), KATANA("ItemKatanaMF"),
    RAPIER("ItemRapierMF"), DAGGER("ItemDagger"), DUAL_DAGGER("ItemDaggerDual"),
    MACE("ItemMaceMF"), WARHAMMER("ItemWarhammerMF"), WARAXE("ItemWaraxeMF"),
    BATTLEAXE("ItemBattleaxeMF"), SPEAR("ItemSpearMF"), LANCE("ItemLance"),
    HALBERD("ItemHalbeardMF"), WAR_SCYTHE("ItemWarScytheMF"), BOW("ItemBowMF"), CROSSBOW("ItemCrossbow"),
    PICKAXE("ItemPickMF"), AXE("ItemAxeMF"), SHOVEL("ItemSpadeMF"), HOE("ItemHoeMF"),
    SHEARS("ItemShearsMF"), HANDPICK("ItemHandpick"), HEAVY_PICK("ItemHvyPick"),
    HEAVY_SHOVEL("ItemHvyShovel"), LUMBER_AXE("ItemLumberAxe"), MATTOCK("ItemMattock"),
    SCYTHE("ItemScythe"), TROWEL("ItemTrowMF"), LIGHTER("ItemLighterMF"),
    CRAFT_HAMMER("ItemHammer"), KNIFE("ItemKnifeMF"), NEEDLE("ItemNeedle"),
    SAW("ItemSaw"), TONGS("ItemTongs"), SPANNER("ItemSpanner"), PAINT_BRUSH("ItemPaintBrush"),
    SHIELD("ItemShieldMF"), STAFF("ItemStaffBasic");

    public static final WardrobeSlot[] ALL = values();
    private final String itemClass;

    WardrobeSlot() { this(""); }
    WardrobeSlot(String itemClass) { this.itemClass = itemClass; }

    public String key() { return name().toLowerCase(java.util.Locale.ROOT); }
    public String label() { return StatCollector.translateToLocal("mbo.wardrobe.slot." + key()); }
    public boolean armor() { return ordinal() < SWORD.ordinal(); }
    public int part() { return ordinal() % 4; }

    public static WardrobeSlot byKey(String key) {
        for (WardrobeSlot slot : ALL) if (slot.key().equals(key)) return slot;
        return null;
    }

    public static WardrobeSlot forItem(Item item) {
        if (item instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) item;
            GameRegistry.UniqueIdentifier registry = GameRegistry.findUniqueIdentifierFor(item);
            int group = armor.getArmorMaterial() == ItemArmor.ArmorMaterial.CLOTH ? 0 : 1;
            if (Loader.isModLoaded("minefantasy2") && item instanceof ItemArmourMFBase) {
                ArmourDesign design = ((ItemArmourMFBase) item).design;
                if (design != null && ArmourDesign.light.equals(design.getGroup())) group = 0;
            }
            if (registry != null && "Thaumcraft".equalsIgnoreCase(registry.modId)) {
                String name = registry.name.toLowerCase(java.util.Locale.ROOT);
                if (name.contains("robe") || name.contains("goggle") || name.contains("traveller") || name.contains("cloth")) group = 2;
            }
            return armor.armorType >= 0 && armor.armorType < 4 ? ALL[group * 4 + armor.armorType] : null;
        }
        if (item == null) return null;
        WardrobeSlot type = forClass(item.getClass());
        if (type != null) return type;
        return null;
    }

    public static WardrobeSlot forStack(ItemStack stack) {
        if (stack == null) return null;
        WardrobeSlot slot = forItem(stack.getItem());
        if (slot == null && Loader.isModLoaded("Thaumcraft")
                && stack.getItem() instanceof thaumcraft.common.items.wands.ItemWandCasting
                && ((thaumcraft.common.items.wands.ItemWandCasting) stack.getItem()).isStaff(stack)) return STAFF;
        return slot;
    }

    public static WardrobeSlot forClass(Class<?> item) {
        for (Class<?> type = item; type != null; type = type.getSuperclass()) {
            for (WardrobeSlot slot : ALL) {
                if (!slot.itemClass.isEmpty() && slot.itemClass.equals(type.getSimpleName())) return slot;
            }
            if (type == ItemSword.class) return SWORD;
            if (type == ItemBow.class) return BOW;
            if (type == ItemPickaxe.class) return PICKAXE;
            if (type == ItemAxe.class) return AXE;
            if (type == ItemSpade.class) return SHOVEL;
            if (type == ItemHoe.class) return HOE;
            if (type == ItemShears.class) return SHEARS;
        }
        return null;
    }

    public boolean accepts(EquipmentSkinDefinition skin) {
        if (armor()) {
            return skin.category == EquipmentSkinDefinition.Category.ARMOR
                && skin.armorGroup.ordinal() == ordinal() / 4 && skin.supportsArmorSlot(part());
        }
        return skin.category != EquipmentSkinDefinition.Category.ARMOR && forClass(skin.weaponType) == this;
    }

    public Item sample() {
        switch (this) {
            case LIGHT_HEAD: return Items.leather_helmet;
            case LIGHT_CHEST: return Items.leather_chestplate;
            case LIGHT_LEGS: return Items.leather_leggings;
            case LIGHT_FEET: return Items.leather_boots;
            case HEAVY_HEAD: return Items.iron_helmet;
            case HEAVY_CHEST: return Items.iron_chestplate;
            case HEAVY_LEGS: return Items.iron_leggings;
            case HEAVY_FEET: return Items.iron_boots;
            case MAGIC_HEAD: return GameRegistry.findItem("Thaumcraft", "ItemGoggles");
            case MAGIC_CHEST: return GameRegistry.findItem("Thaumcraft", "ItemChestplateRobe");
            case MAGIC_LEGS: return GameRegistry.findItem("Thaumcraft", "ItemLeggingsRobe");
            case MAGIC_FEET: return GameRegistry.findItem("Thaumcraft", "BootsTraveller");
            case SWORD: return Items.iron_sword;
            case BOW: return Items.bow;
            case SHIELD: return GameRegistry.findItem("minefantasy2", "standard_shield");
            default:
                for (Object entry : Item.itemRegistry) {
                    Item item = (Item) entry;
                    if (forItem(item) == this) return item;
                }
                return null;
        }
    }
}
