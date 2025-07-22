package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import ru.givler.mbo.item.*;
import ru.givler.mbo.item.amulets.*;
import ru.givler.mbo.item.totems.*;

import ru.givler.mbo.item.totems.ItemTotemWeapon;



public class ItemRegistry {
    //Ниже переменные для предметов тотемов
    public static Item TotemAmphibian, TotemDragon, TotemHawk, TotemMiner, TotemOwl, TotemWeapon, TotemCleansing, TotemHealing;
    //ниже переменные для оружия ближнего боя
    public static ItemsWeapon RustyLongsword, Uchigatana;
    //ниже переменные для призрачного оружия
    public static ItemsGhostWeapon WeaponRapier;
    //ниже переменные для луков
    public static ItemBow OldBowHunting ;
    //ниже переменные для материалов
    public static Item Metal;
    //ниже переменные для амулетов
    public static Item HealingAmulet, VampirismAmulet, CleansingAmulet, PhoenixAmulet, CowardAmulet, DragonAmulet;

    static {
        Item.ToolMaterial RustyMaterial = ItemsWeapon.createMaterial("RustyMaterial", 0, 800, 0.0F, 0.0F, 30);
        Item.ToolMaterial Divine = ItemsWeapon.createMaterial("Divine", 3, 10000, 0.0F, 10000.0f, 30);

        RustyLongsword = new ItemsWeapon("RustyLongsword", "weapon/broadsword", RustyMaterial, 80, 1, 1.3F);
        OldBowHunting = new ItemsBow("OldBowHunting", "weapon/brokenlittlecrossbow", 40, 25, -1);
        WeaponRapier = new ItemsGhostWeapon("WeaponRapier", "weapon/mithrilsword", RustyMaterial, 40, 1, 1.0F);
        Uchigatana = new ItemsWeapon("Uchigatana", "weapon/uchigatana", Divine, 10000, 1, 1.6F);

    }

    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        //ниже тотемы
        TotemAmphibian = new ItemTotemAmphibian("TotemAmphibian", "staff", 1);
        TotemDragon = new ItemTotemDragon("TotemDragon", "staff", 1);
        TotemHawk = new ItemTotemHawk("TotemHawk", "staff", 1);
        TotemMiner = new ItemTotemMiner("TotemMiner", "staff", 1);
        TotemOwl = new ItemTotemOwl("TotemOwl", "staff", 1);
        TotemWeapon = new ItemTotemWeapon("TotemWeapon", "staff", 1);
        TotemCleansing = new ItemTotemCleansing("TotemCleansing", "staff", 1);
        TotemHealing = new ItemTotemHealing("TotemHealing", "staff", 1);

        //ниже материлаы
        Metal = new ItemsMeta("Metal", "material/metal", 64, 1);

        HealingAmulet = new ItemHealingAmulet("HealingAmulet", "amulet/amulet_healing");
        VampirismAmulet = new ItemVampirismAmulet("VampirismAmulet", "amulet/amulet_vampirism");
        CleansingAmulet = new ItemCleansingAmulet("CleansingAmulet", "amulet/amulet_cleansing");
        PhoenixAmulet = new ItemPhoenixAmulet("PhoenixAmulet", "amulet/amulet_phoenix");
        CowardAmulet = new ItemCowardAmulet("CowardAmulet", "amulet/amulet_coward");
        DragonAmulet = new ItemDragonAmulet("DragonAmulet", "amulet/amulet_dragon");


        RustyLongsword.register();
        Uchigatana.register();
        ((ItemsBow) OldBowHunting).register();
    }

}
