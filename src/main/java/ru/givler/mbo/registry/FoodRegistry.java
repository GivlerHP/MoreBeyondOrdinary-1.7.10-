package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import ru.givler.mbo.item.ItemsFood;

public class FoodRegistry {
    //переменные для еды
    public static Item FoodBacon, FoodBlackBread, FoodBurger, FoodChowder, FoodDeliciousChicken, FoodDeliciousSalad, FoodDivineSteak, FoodFreshBread,
            FoodFriedSausage, FoodCheese, FoodSoup, FoodMeatPie, FoodStrangeFish;
    public static void preLoad(FMLPreInitializationEvent event) {
        FoodBacon = new ItemsFood("FoodBacon", "food/bacon", 8, 0.6F, 64, false);
        FoodBlackBread = new ItemsFood("FoodBlackBread", "food/black_bread", 8, 0.6F, 64, false);
        FoodBurger = new ItemsFood("FoodBurger", "food/burger", 8, 0.6F, 64, false);
        FoodChowder = new ItemsFood("FoodChowder", "food/chowder", 8, 0.6F, 1, false);
        FoodDeliciousChicken = new ItemsFood("FoodDeliciousChicken", "food/delicious_chicken", 8, 0.6F, 64, false);
        FoodDeliciousSalad = new ItemsFood("FoodDeliciousSalad", "food/delicious_salad", 8, 0.6F, 1, false);
        FoodDivineSteak = new ItemsFood("FoodDivineSteak", "food/divine_steak", 8, 0.6F, 64, false);
        FoodFreshBread = new ItemsFood("FoodFreshBread", "food/fresh_bread", 8, 0.6F, 64, false);
        FoodFriedSausage = new ItemsFood("FoodFriedSausage", "food/fried_sausage", 8, 0.6F, 64, false);
        FoodCheese = new ItemsFood("FoodCheese", "food/great_cheese", 8, 0.6F, 64, false);
        FoodSoup = new ItemsFood("FoodSoup", "food/hearty_soup", 8, 0.6F, 1, false);
        FoodMeatPie = new ItemsFood("FoodMeatPie", "food/meat_pie", 8, 0.6F, 64, false);

        FoodStrangeFish = new ItemsFood("FoodStrangeFish", "food/strange_fried_fish", 8, 0.6F, 1, false)
                .addPotionEffect(Potion.regeneration.id, 200, 1)  // Эффект регенерации на 200 тиков с усилением 1
                .addPotionEffect(Potion.moveSpeed.id, 100, 0);  // Эффект ускорения на 100 тиков без усиления

    }
}
