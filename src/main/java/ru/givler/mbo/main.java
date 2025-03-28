package ru.givler.mbo;
import cpw.mods.fml.common.SidedProxy;

import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.item.Item;
import ru.givler.mbo.proxy.CommonProxy;

import static ru.givler.mbo.registry.DrinkRegistry.DrinkWine;
import static ru.givler.mbo.registry.ModelRegistry.ModelDummy;
import static ru.givler.mbo.registry.BlockRegistry.BlockImperialBrick;
import static ru.givler.mbo.registry.ItemRegistry.WeaponRapier;


@Mod(modid = main.MODID, name = main.MODNAME, version = main.VERSION)
public class main {

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

    @SidedProxy(clientSide = "ru.givler.mbo.proxy.ClientProxy", serverSide = "ru.givler.mbo.proxy.CommonProxy")
    public static CommonProxy proxy;


    public static final String MODID = "mbo";
    public static final String MODNAME = "MBO";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void preLoad(FMLInitializationEvent event) { proxy.preInit(event); }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLInitializationEvent event){
        proxy.postInit(event);
    }

}