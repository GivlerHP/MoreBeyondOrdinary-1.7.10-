package ru.givler.mbo.item;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

//класс для создания предметов
public class ItemBasic extends Item {

    boolean mode = false;

    public ItemBasic(String name, String texture, int maxStackSize) {
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

}