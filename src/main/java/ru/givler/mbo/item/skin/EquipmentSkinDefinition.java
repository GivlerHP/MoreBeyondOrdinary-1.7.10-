package ru.givler.mbo.item.skin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;

/** Shared catalog entry. Pose values are in item render units and degrees. */
public final class EquipmentSkinDefinition {
    public enum Category { WEAPON, ARMOR, TOOL, SHIELD }
    public enum ArmorGroup { LIGHT, MEDIUM_HEAVY, MAGIC }

    public final String id;
    public final Category category;
    private final int armorSlots;
    public final ArmorGroup armorGroup;
    public final Class<? extends Item> weaponType;
    public final ResourceLocation model, texture;
    public final Pose firstPerson, thirdPerson, inventory, ground;

    public EquipmentSkinDefinition(String id, Class<? extends Item> weaponType,
                                Pose firstPerson, Pose thirdPerson, Pose inventory, Pose ground) {
        this(id, Category.WEAPON, weaponType, firstPerson, thirdPerson, inventory, ground);
    }

    public EquipmentSkinDefinition(String id, Category category, Class<? extends Item> weaponType,
                                Pose firstPerson, Pose thirdPerson, Pose inventory, Pose ground) {
        if (category == Category.ARMOR) throw new IllegalArgumentException("Armor skins require armor slots");
        this.id = id;
        this.category = category;
        this.armorSlots = 0;
        this.armorGroup = null;
        this.weaponType = weaponType;
        this.firstPerson = firstPerson;
        this.thirdPerson = thirdPerson;
        this.inventory = inventory;
        this.ground = ground;
        model = new ResourceLocation("mbo", "geo/weapon/" + id + ".geo.json");
        texture = new ResourceLocation("mbo", "textures/models/weapon/" + id + ".png");
    }

    public EquipmentSkinDefinition(String id, int armorSlots, Pose inventory) {
        this(id, ArmorGroup.MAGIC, armorSlots, inventory);
    }

    public EquipmentSkinDefinition(String id, ArmorGroup armorGroup, int armorSlots, Pose inventory) {
        this.id = id;
        this.category = Category.ARMOR;
        this.armorSlots = armorSlots;
        this.armorGroup = armorGroup;
        this.weaponType = ItemArmor.class;
        this.inventory = inventory;
        this.firstPerson = this.thirdPerson = this.ground = inventory;
        model = new ResourceLocation("mbo", "geo/armor/" + id + ".geo.json");
        texture = new ResourceLocation("mbo", "textures/models/armor/" + id + ".png");
    }

    public boolean supportsArmorSlot(int slot) {
        return slot >= 0 && slot < 4 && (armorSlots & (1 << slot)) != 0;
    }

    public boolean supports(Item item) {
        if (category == Category.ARMOR) {
            return item instanceof ItemArmor && supportsArmorSlot(((ItemArmor) item).armorType);
        }
        return weaponType.isInstance(item);
    }

    public String titleKey() {
        return "mbo.weapon_skin." + id;
    }

    public static final class Pose {
        public final float x, y, z, rotateX, rotateY, rotateZ, scale;
        public Pose(float x, float y, float z, float rotateX, float rotateY, float rotateZ, float scale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotateX = rotateX;
            this.rotateY = rotateY;
            this.rotateZ = rotateZ;
            this.scale = scale;
        }
    }
}
