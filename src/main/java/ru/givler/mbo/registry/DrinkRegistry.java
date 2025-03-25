package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import ru.givler.mbo.item.ItemsDrink;

public class DrinkRegistry {
    public static Item DrinkWine, DrinkAle, DrinkMead, DrinkCider, DrinkBarrelBeer, DrinkBrandy, DrinkMulledWine, DrinkTincure, DrinkBreathWyvern, DrinkBeer;  //переменные для напитков и зелий
    @Mod.EventHandler
    public static void preLoad(FMLInitializationEvent event) {
        //НИЖЕ НАХОДИТСЯ НАПИТКИ
        DrinkWine = new ItemsDrink("DrinkWine", "drink/drink_wine")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkAle = new ItemsDrink("DrinkAle", "drink/drink_ale")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkMead = new ItemsDrink("DrinkMead", "drink/drink_mead")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkCider = new ItemsDrink("DrinkCider", "drink/drink_cider")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkBarrelBeer = new ItemsDrink("DrinkBarrelBeer", "drink/drink_barrel_beer")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkBrandy = new ItemsDrink("DrinkBrandy", "drink/drink_brandy")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkMulledWine = new ItemsDrink("DrinkMulledWine", "drink/drink_mulled_wine")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkTincure = new ItemsDrink("DrinkTincure", "drink/drink_tincture")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkBreathWyvern = new ItemsDrink("DrinkBreathWyvern", "drink/drink_breath_wyvern")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);
        DrinkBeer = new ItemsDrink("DrinkBeer", "drink/drink_beer")
                .addPotionEffect(Potion.regeneration.id, 600, 1)
                .addPotionEffect(Potion.moveSpeed.id, 600, 1);


    }
}
