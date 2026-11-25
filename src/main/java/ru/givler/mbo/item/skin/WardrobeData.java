package ru.givler.mbo.item.skin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.registry.EquipmentSkinRegistry;

public final class WardrobeData {
    private static final String KEY = "MBO_Wardrobe";
    private static final String VISUAL = "MBO_WardrobeVisual";
    private static final String DISABLED = "DisabledSkins";

    private WardrobeData() {}

    public static NBTTagCompound inventory(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10)) data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!persisted.hasKey(KEY, 10)) persisted.setTag(KEY, new NBTTagCompound());
        return persisted.getCompoundTag(KEY);
    }

    public static NBTTagCompound visual(EntityPlayer player) {
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagCompound inventory = inventory(player);
        for (WardrobeSlot slot : WardrobeSlot.ALL) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(inventory.getCompoundTag(slot.key()));
            if (ItemSkinTemplate.slot(stack) == slot && !isDisabled(inventory, slot, stack)) {
                payload.setString(slot.key(), ItemSkinTemplate.skin(stack).id);
            }
        }
        return payload;
    }

    public static boolean isDisabled(NBTTagCompound inventory, WardrobeSlot slot, ItemStack stack) {
        EquipmentSkinDefinition skin = ItemSkinTemplate.skin(stack);
        return skin != null && skin.id.equals(inventory.getCompoundTag(DISABLED).getString(slot.key()));
    }

    public static void setDisabled(NBTTagCompound inventory, WardrobeSlot slot, ItemStack stack, boolean disabled) {
        EquipmentSkinDefinition skin = ItemSkinTemplate.skin(stack);
        if (skin == null || ItemSkinTemplate.slot(stack) != slot) return;
        NBTTagCompound flags = inventory.getCompoundTag(DISABLED);
        if (disabled) flags.setString(slot.key(), skin.id);
        else flags.removeTag(slot.key());
        if (flags.hasNoTags()) inventory.removeTag(DISABLED);
        else inventory.setTag(DISABLED, flags);
    }

    public static void acceptVisual(EntityPlayer player, NBTTagCompound payload) {
        player.getEntityData().setTag(VISUAL, payload);
    }

    public static EquipmentSkinDefinition resolve(ItemStack stack, Entity owner) {
        EquipmentSkinDefinition explicit = EquipmentSkinData.resolve(stack);
        if (explicit != null || stack == null || !(owner instanceof EntityPlayer)) return explicit;
        if (!owner.getEntityData().hasKey(VISUAL, 10)) return null;
        WardrobeSlot slot = WardrobeSlot.forStack(stack);
        if (slot == null) return null;
        EquipmentSkinDefinition skin = EquipmentSkinRegistry.find(owner.getEntityData().getCompoundTag(VISUAL).getString(slot.key()));
        return skin != null && slot.accepts(skin) ? skin : null;
    }

    public static Object heldStack(Object owner, Object object) {
        ItemStack stack = (ItemStack) object;
        if (stack == null || !(owner instanceof EntityPlayer) || !EquipmentSkinData.id(stack).isEmpty()) return stack;
        EquipmentSkinDefinition skin = resolve(stack, (Entity) owner);
        if (skin == null) return stack;
        ItemStack rendered = stack.copy();
        return EquipmentSkinData.set(rendered, skin.id) ? rendered : stack;
    }
}
