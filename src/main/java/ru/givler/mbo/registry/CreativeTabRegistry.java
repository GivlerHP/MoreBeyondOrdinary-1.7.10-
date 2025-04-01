package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import static ru.givler.mbo.registry.BlockRegistry.BlockImperialBrick;
import static ru.givler.mbo.registry.DrinkRegistry.DrinkWine;
import static ru.givler.mbo.registry.ItemRegistry.WeaponRapier;
import static ru.givler.mbo.registry.ModelRegistry.ModelDummy;

public class CreativeTabRegistry {
    public static final CreativeTabs tabMBOblocks = new CreativeTabs("MBOblocks") {
        @Override
        public Item getTabIconItem() {                          //создаем меню в режиме креатива
            return Item.getItemFromBlock(BlockImperialBrick);
        }
    };
    public static final CreativeTabs tabMBOitems = new CreativeTabs("MBOitems") {
        @Override
        public Item getTabIconItem() {                          //создаем меню в режиме креатива
            return (WeaponRapier);
        }
    };
    public static final CreativeTabs tabMBOfoods = new CreativeTabs("MBOfoods") {
        @Override
        public Item getTabIconItem() {                          //создаем меню в режиме креатива
            return (DrinkWine);
        }
    };
    public static final CreativeTabs tabMBOdecors = new CreativeTabs("MBOdecors") {
        @Override
        public Item getTabIconItem() {                          //создаем меню в режиме креатива
            return Item.getItemFromBlock((ModelDummy));
        }
    };

    public static void init(FMLInitializationEvent event) {

    }
}
