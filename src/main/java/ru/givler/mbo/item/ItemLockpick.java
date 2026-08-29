package ru.givler.mbo.item;

import net.minecraft.item.Item;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemLockpick extends Item {
    public ItemLockpick() {
        setUnlocalizedName("Lockpick");
        setTextureName(MoreBeyondOrdinary.MODID + ":lockpick");
        setCreativeTab(CreativeTabRegistry.tabMBOitems);
        setMaxStackSize(16);
    }
}
