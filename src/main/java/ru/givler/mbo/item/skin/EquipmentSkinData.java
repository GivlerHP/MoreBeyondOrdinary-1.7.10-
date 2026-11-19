package ru.givler.mbo.item.skin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.registry.EquipmentSkinRegistry;

public final class EquipmentSkinData {
    private static final String KEY = "MBO_WeaponSkin";

    private EquipmentSkinData() {}

    public static String id(ItemStack stack) {
        return stack != null && stack.hasTagCompound()
                ? EquipmentSkinRegistry.canonicalId(stack.getTagCompound().getString(KEY)) : "";
    }

    public static EquipmentSkinDefinition resolve(ItemStack stack) {
        EquipmentSkinDefinition skin = EquipmentSkinRegistry.find(id(stack));
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
        EquipmentSkinDefinition skin = EquipmentSkinRegistry.find(id);
        if (skin == null || !skin.supports(stack.getItem())) return false;
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(KEY, id);
        return true;
    }
}
