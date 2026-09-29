package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import cpw.mods.fml.common.registry.GameRegistry;
import ru.givler.mbo.item.ItemFood;

public class FoodRegistry {
    /**
     * Первая характеристика(healAmount) - количество восстановляемого голода.
     * Вторая(saturation) - коэффициент насыщенности
     * Чтобы понять сколько насыщенности будет, восспользуйтесь формулой:
     * healAmount * 2 * saturation  ||  количество восстановляемого голода * 2 * коэффициент насыщенности
     */
    //переменные для еды
    public static Item FoodBacon, FoodBlackBread, FoodBurger, FoodChowder, FoodDeliciousChicken, FoodDeliciousSalad, FoodDivineSteak, FoodFreshBread,
            FoodFriedSausage, FoodCheese, FoodSoup, FoodMeatPie, FoodStrangeFish;
    public static Item RabbitRaw, RabbitCooked, RabbitStew;
    public static void preLoad(FMLPreInitializationEvent event) {
        RabbitRaw = new ItemFood("RabbitRaw", "rabbit/rabbit", 3, 0.3F, 64, true);
        RabbitCooked = new ItemFood("RabbitCooked", "rabbit/cooked_rabbit", 5, 0.6F, 64, true);
        RabbitStew = new ItemSoup(10).setUnlocalizedName("RabbitStew")
                .setTextureName("mbo:rabbit/rabbit_stew")
                .setCreativeTab(CreativeTabRegistry.tabMBOfoods);
        GameRegistry.registerItem(RabbitStew, "RabbitStew");
        GameRegistry.addSmelting(RabbitRaw, new ItemStack(RabbitCooked), 0.35F);
        GameRegistry.addShapelessRecipe(new ItemStack(RabbitStew), RabbitCooked, Items.carrot,
                Items.baked_potato, Blocks.brown_mushroom, Items.bowl);
        GameRegistry.addShapedRecipe(new ItemStack(Items.leather), "HH", "HH", 'H', ItemRegistry.RabbitHide);
        FoodBacon = new ItemFood("FoodBacon", "food/bacon", 6, 0.85F, 64, false);
        FoodBlackBread = new ItemFood("FoodBlackBread", "food/black_bread", 5, 0.7F, 64, false);
        FoodBurger = new ItemFood("FoodBurger", "food/burger", 8, 0.8F, 64, false);
        FoodChowder = new ItemFood("FoodChowder", "food/chowder", 7, 0.8F, 1, false);
        FoodDeliciousChicken = new ItemFood("FoodDeliciousChicken", "food/delicious_chicken", 6, 0.8F, 64, false);
        FoodDeliciousSalad = new ItemFood("FoodDeliciousSalad", "food/delicious_salad", 7, 0.75F, 1, false);
        FoodDivineSteak = new ItemFood("FoodDivineSteak", "food/divine_steak", 8, 0.85F, 64, false);
        FoodFreshBread = new ItemFood("FoodFreshBread", "food/fresh_bread", 5, 0.7F, 64, false);
        FoodFriedSausage = new ItemFood("FoodFriedSausage", "food/fried_sausage", 6, 0.9F, 64, false);
        FoodCheese = new ItemFood("FoodCheese", "food/great_cheese", 6, 0.8F, 64, false);
        FoodSoup = new ItemFood("FoodSoup", "food/hearty_soup", 7, 0.8F, 1, false);
        FoodMeatPie = new ItemFood("FoodMeatPie", "food/meat_pie", 8, 0.8F, 64, false);

        FoodStrangeFish = new ItemFood("FoodStrangeFish", "food/strange_fried_fish", 6, 0.8F, 1, false)
                .addPotionEffect(PotionRegistry.SixthSense.id, 800, 2);
    }
}
