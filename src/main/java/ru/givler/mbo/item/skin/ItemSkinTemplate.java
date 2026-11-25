package ru.givler.mbo.item.skin;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.registry.EquipmentSkinRegistry;

public final class ItemSkinTemplate extends Item {
    public static final ItemSkinTemplate INSTANCE = new ItemSkinTemplate();

    private ItemSkinTemplate() {
        setUnlocalizedName("mbo.skin_template");
        setTextureName("paper");
        setMaxStackSize(1);
    }

    public static EquipmentSkinDefinition skin(ItemStack stack) {
        if (stack == null || stack.getItem() != INSTANCE || !stack.hasTagCompound()) return null;
        return EquipmentSkinRegistry.find(EquipmentSkinRegistry.canonicalId(stack.getTagCompound().getString("Skin")));
    }

    public static WardrobeSlot slot(ItemStack stack) {
        EquipmentSkinDefinition skin = skin(stack);
        if (skin == null) return null;
        NBTTagCompound tag = stack.getTagCompound();
        WardrobeSlot slot = tag.hasKey("Slot") ? WardrobeSlot.byKey(tag.getString("Slot")) : WardrobeSlot.forClass(skin.weaponType);
        return slot != null && slot.accepts(skin) ? slot : null;
    }

    public static WardrobeSlot target(EquipmentSkinDefinition skin, int part) {
        if (skin == null) return null;
        if (skin.category != EquipmentSkinDefinition.Category.ARMOR) return WardrobeSlot.forClass(skin.weaponType);
        return skin.supportsArmorSlot(part) ? WardrobeSlot.ALL[skin.armorGroup.ordinal() * 4 + part] : null;
    }

    public static boolean set(ItemStack stack, String id, int part) {
        if (stack == null || stack.getItem() != INSTANCE) return false;
        if (id.isEmpty()) {
            if (stack.hasTagCompound()) {
                stack.getTagCompound().removeTag("Skin");
                stack.getTagCompound().removeTag("Slot");
                if (stack.getTagCompound().hasNoTags()) stack.setTagCompound(null);
            }
            return true;
        }
        EquipmentSkinDefinition skin = EquipmentSkinRegistry.find(EquipmentSkinRegistry.canonicalId(id));
        WardrobeSlot target = target(skin, part);
        if (target == null) return false;
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("Skin", skin.id);
        stack.getTagCompound().setString("Slot", target.key());
        return true;
    }

    @Override public String getItemStackDisplayName(ItemStack stack) {
        EquipmentSkinDefinition skin = skin(stack);
        return skin == null ? super.getItemStackDisplayName(stack)
            : StatCollector.translateToLocalFormatted("mbo.wardrobe.template.named", StatCollector.translateToLocal(skin.titleKey()));
    }

    @Override @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        WardrobeSlot slot = slot(stack);
        lines.add(slot == null ? StatCollector.translateToLocal("mbo.wardrobe.template.empty")
            : StatCollector.translateToLocalFormatted("mbo.wardrobe.template.target", slot.label()));
    }
}
