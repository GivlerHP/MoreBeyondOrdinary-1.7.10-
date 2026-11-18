package ru.givler.mbo.item.skin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.registry.WeaponSkinRegistry;

public final class WeaponSkinData {
    private static final String KEY = "MBO_WeaponSkin";

    private WeaponSkinData() {}

    public static String id(ItemStack stack) {
        return stack != null && stack.hasTagCompound()
                ? WeaponSkinRegistry.canonicalId(stack.getTagCompound().getString(KEY)) : "";
    }

    public static WeaponSkinDefinition resolve(ItemStack stack) {
        WeaponSkinDefinition skin = WeaponSkinRegistry.find(id(stack));
        return skin != null && skin.supports(stack.getItem()) ? skin : null;
    }

    public static boolean set(ItemStack stack, String id) {
        if (stack == null) return false;
        if (id.isEmpty()) {
            if (stack.hasTagCompound()) {
                stack.getTagCompound().removeTag(KEY);
                if (stack.getTagCompound().hasNoTags()) stack.setTagCompound(null);
            }
            return true;
        }
        WeaponSkinDefinition skin = WeaponSkinRegistry.find(id);
        if (skin == null || !skin.supports(stack.getItem())) return false;
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(KEY, id);
        return true;
    }
}
