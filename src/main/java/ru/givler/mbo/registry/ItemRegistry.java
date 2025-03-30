package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraftforge.client.MinecraftForgeClient;
import ru.givler.mbo.item.*;
import ru.givler.mbo.item.totems.*;

import ru.givler.mbo.item.totems.ItemTotemWeapon;
import ru.givler.mbo.render.RenderCrossbow;
import ru.givler.mbo.render.RenderLongsword;


public class ItemRegistry {
    //Ниже переменные для предметов тотемов
    public static Item TotemAmphibian, TotemDragon, TotemHawk, TotemMiner, TotemOwl, TotemWeapon, TotemCleansing;
    //ниже переменные для оружия ближнего боя
    public static ItemsWeapon RustyLongsword;
    //ниже переменные для призрачного оружия
    public static ItemsGhostWeapon WeaponRapier;
    //ниже переменные для луков
    public static ItemBow OldBowHunting ;

    @Mod.EventHandler
    public static void preLoad(FMLInitializationEvent event) {
        //Ниже находятся материалы для оружия
        Item.ToolMaterial RustyMaterial = ItemsWeapon.createMaterial("RustyMaterial", 0, 800, 0.0F, 0.0F, 30);

        //ниже тотемы
        TotemAmphibian = new ItemTotemAmphibian("TotemAmphibian", "staff", 1);
        TotemDragon = new ItemTotemDragon("TotemDragon", "staff", 1);
        TotemHawk = new ItemTotemHawk("TotemHawk", "staff", 1);
        TotemMiner = new ItemTotemMiner("TotemMiner", "staff", 1);
        TotemOwl = new ItemTotemOwl("TotemOwl", "staff", 1);
        TotemWeapon = new ItemTotemWeapon("TotemWeapon", "staff", 1);
        TotemCleansing = new ItemTotemCleansing("TotemCleansing", "staff", 1);

        //ниже ближнее оружие
        RustyLongsword = new ItemsWeapon("RustyLongsword", "weapon/broadsword", RustyMaterial, 80, 1, 1.3F);
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.RustyLongsword, new RenderLongsword(ItemRegistry.RustyLongsword.getScale()));

        //ниже призрачное оружие (пропадает спустя какое-то время)
        WeaponRapier = new ItemsGhostWeapon("WeaponRapier", "weapon/mithrilsword", RustyMaterial, 40, 1, 1.0F);

        //ниже луки
        OldBowHunting = new ItemsBow("BowРunting", "weapon/brokenlittlecrossbow", 8, 40, 3);
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.OldBowHunting, new RenderCrossbow());
    }

}
