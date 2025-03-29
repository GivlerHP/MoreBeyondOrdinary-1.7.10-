package ru.givler.mbo.item;

import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import ru.givler.mbo.main;

public class ItemsWeapon extends ItemSword {
    private float scale;

    public ItemsWeapon(String name, String texture, ToolMaterial material, int maxDamage, int maxStackSize, float scale) {
        super(material);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(main.tabMBOitems);
        this.setMaxDamage(maxDamage);
        this.maxStackSize = maxStackSize;
        this.scale = scale;
        GameRegistry.registerItem(this, name);
    }

    // Методы для создания материалов
    public static ToolMaterial createMaterial(String name, int harvestLevel, int durability, float damage, float efficiency, int enchantability) {
        return EnumHelper.addToolMaterial(name, harvestLevel, durability, damage, efficiency, enchantability);
    }

    // Метод для получения масштаба
    public float getScale() {
        return scale;
    }
}
