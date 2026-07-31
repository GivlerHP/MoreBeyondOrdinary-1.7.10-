package ru.givler.mbo.banner;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class BannerData {
    public static final int MAX_PATTERNS = 6;

    private BannerData() { }

    public static int getBaseColor(ItemStack stack) {
        NBTTagCompound tag = getBlockEntityTag(stack, false);
        return tag != null && tag.hasKey("Base", 99)
                ? tag.getInteger("Base") & 15 : stack.getItemDamage() & 15;
    }

    public static NBTTagList getPatterns(ItemStack stack, boolean create) {
        NBTTagCompound tag = getBlockEntityTag(stack, create);
        if (tag == null) return null;
        if (!tag.hasKey("Patterns", 9) && create) tag.setTag("Patterns", new NBTTagList());
        return tag.hasKey("Patterns", 9) ? tag.getTagList("Patterns", 10) : null;
    }

    public static int getPatternCount(ItemStack stack) {
        NBTTagList list = getPatterns(stack, false);
        return list == null ? 0 : list.tagCount();
    }

    public static ItemStack addPattern(ItemStack source, BannerPattern pattern, int dyeColor) {
        ItemStack result = source.copy();
        result.stackSize = 1;
        if (getPatternCount(result) >= MAX_PATTERNS) return result;
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("Pattern", pattern.id);
        entry.setInteger("Color", dyeColor & 15);
        getPatterns(result, true).appendTag(entry);
        return result;
    }

    public static NBTTagCompound getBlockEntityTag(ItemStack stack, boolean create) {
        if (!stack.hasTagCompound() && create) stack.setTagCompound(new NBTTagCompound());
        if (!stack.hasTagCompound()) return null;
        NBTTagCompound root = stack.getTagCompound();
        if (!root.hasKey("BlockEntityTag", 10) && create)
            root.setTag("BlockEntityTag", new NBTTagCompound());
        return root.hasKey("BlockEntityTag", 10)
                ? root.getCompoundTag("BlockEntityTag") : null;
    }
}
