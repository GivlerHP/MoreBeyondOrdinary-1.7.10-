package ru.givler.mbo.item.weapon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class ItemDragonSlayerMBO extends ItemWeaponBase {

    public ItemDragonSlayerMBO(String name, String texture, ToolMaterial material, int maxDamage, int maxStackSize, float scale) {
        super(name, texture, material, maxDamage, maxStackSize, scale);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        attacker.worldObj.playSoundAtEntity(
                attacker,
                "random.anvil_land",
                1.0F,
                1.0F
        );
        stack.damageItem(1, attacker);
        return true;
    }
}
