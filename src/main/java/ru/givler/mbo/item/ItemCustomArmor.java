package ru.givler.mbo.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemCustomArmor extends ItemArmor {
    private String textureName;
    private String armorTexture;


    public ItemCustomArmor(String name, String texture, String armorTexture, ArmorMaterial material, int renderIndex, int armorType) {
        super(material, renderIndex, armorType);
        this.textureName = texture;
        this.armorTexture = armorTexture;
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":armor/" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
    }

    public static ArmorMaterial createArmorMaterial(String name, int durability, int[] reductionAmounts, int enchantability) {
        return EnumHelper.addArmorMaterial(name, durability, reductionAmounts, enchantability);
    }

    public void register() {
        GameRegistry.registerItem(this, this.getUnlocalizedName().substring(5));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        if (slot == 2) {
            return MoreBeyondOrdinary.MODID + ":textures/models/armor/" + armorTexture + "_2.png";
        }
        return MoreBeyondOrdinary.MODID + ":textures/models/armor/" + armorTexture + "_1.png";
    }
}