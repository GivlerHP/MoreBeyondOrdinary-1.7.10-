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
                .addPotionEffect(Potion.regeneration.id, 120, 0)
                .addPotionEffect(Potion.weakness.id, 400, 0)
                .addPotionEffect(Potion.confusion.id, 400, 1);
        DrinkAle = new ItemsDrink("DrinkAle", "drink/drink_ale")
                .addPotionEffect(Potion.digSpeed.id, 300, 0)
                .addPotionEffect(Potion.confusion.id, 500, 0);
        DrinkMead = new ItemsDrink("DrinkMead", "drink/drink_mead")
                .addPotionEffect(Potion.resistance.id, 260, 0)
                .addPotionEffect(Potion.moveSlowdown.id, 500, 0)
                .addPotionEffect(Potion.hunger.id, 300, 1);
        DrinkCider = new ItemsDrink("DrinkCider", "drink/drink_cider")
                .addPotionEffect(Potion.moveSpeed.id, 300, 0)
                .addPotionEffect(Potion.moveSlowdown.id, 80, 1)
                .addPotionEffect(Potion.confusion.id, 400, 0);
        DrinkBarrelBeer = new ItemsDrink("DrinkBarrelBeer", "drink/drink_barrel_beer")
                .addPotionEffect(Potion.damageBoost.id, 100, 0)
                .addPotionEffect(Potion.blindness.id, 400, 1)
                .addPotionEffect(Potion.confusion.id, 400, 1)
                .addPotionEffect(Potion.hunger.id, 300, 1);
        DrinkBrandy = new ItemsDrink("DrinkBrandy", "drink/drink_brandy")
                .addPotionEffect(Potion.field_76444_x.id, 300, 0)
                .addPotionEffect(Potion.moveSlowdown.id, 500, 0)
                .addPotionEffect(Potion.confusion.id, 500, 0);
        DrinkMulledWine = new ItemsDrink("DrinkMulledWine", "drink/drink_mulled_wine")
                .addPotionEffect(Potion.jump.id, 300, 1)
                .addPotionEffect(Potion.fireResistance.id, 60, 0)
                .addPotionEffect(Potion.confusion.id, 500, 0);
        DrinkTincure = new ItemsDrink("DrinkTincure", "drink/drink_tincture")
                .addPotionEffect(Potion.regeneration.id, 120, 0)
                .addPotionEffect(Potion.weakness.id, 400, 0)
                .addPotionEffect(Potion.blindness.id, 200, 0);
        DrinkBreathWyvern = new ItemsDrink("DrinkBreathWyvern", "drink/drink_breath_wyvern")
                .addPotionEffect(Potion.fireResistance.id, 200, 0)
                .addPotionEffect(Potion.weakness.id, 400, 1)
                .addPotionEffect(Potion.blindness.id, 200, 0);
        DrinkBeer = new ItemsDrink("DrinkBeer", "drink/drink_beer")
                .addPotionEffect(Potion.nightVision.id, 300, 1)
                .addPotionEffect(Potion.moveSlowdown.id, 400, 0)
                .addPotionEffect(Potion.confusion.id, 400, 1);


    }
}
