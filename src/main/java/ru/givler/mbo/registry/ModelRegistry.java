package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.material.Material;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.specialblocks.BlockDestructibleLootContainer;
import ru.givler.mbo.block.blockmodels.BlockModelFood;

import static net.minecraft.block.Block.*;

public class ModelRegistry {
    //Интерьер портного
    public static BlockModels ModelThreads;
    public static BlockModels  ModelTailorShelf, ModelCloth, ModelDummy, ModelHangers, ModelPillow, ModelRulers, ModelScissors;
    //Интерьер алхимика
    public static BlockModels ModelIngredients, ModelCauldron, ModelBottles, ModelBooks, ModelAlchemistShelf, ModelAlchemicalFlag;
    //Интерьер охотника
    public static BlockModels ModelArrow, ModelBowWall, ModelBow, ModelDucks, ModelFurKnife, ModelFur, ModelHorn, ModelLeatherDryer,
            ModelRabbits, ModelMooseHead, ModelDeerHead,ModelDeerLegendHead;
    //Интерьер обменщика
    public static BlockModels ModelMagnifyinGlass, ModelBagGold, ModelCoins, ModelSmallChest, ModelScales;
    //Интерьер фермера
    public static BlockModels ModelBagsPotatoes, ModelBasketApples, ModelBasketBerries, ModelBucket, ModelCarrot, ModelGarlic, ModelHay,
            ModelHayfork, ModelJugs, ModelShelfFlower, ModelWateringCan, ModelWheelBarrow;
    //Интерьер ювелира
    public static BlockModels ModelFilledChest, ModelPliers, ModelJewelryHammer, ModelAmulet, ModelInstruments;
    //Интерьер регистраторши
    public static BlockModels ModelLute, ModelBroom, ModelWanted, ModelPapers, ModelKeys, ModelDeskBell;
    //Интерьер оружейника
    public static BlockModels ModelSword, ModelSwords, ModelShield1, ModelShield2, ModelShield3, ModelHelmet, ModelHammer,
            ModelDragonSlayer, ModelAxe;
    //Интерьер инженера
    public static BlockModels ModelGas, ModelOiler, ModelGears, ModelDrawing1, ModelDrawing2, ModelClock, ModelBrokenMechanism;
    //Книжки
    public static BlockModels ModelBook0, ModelBook1, ModelBook2, ModelBook3, ModelBook4, ModelBook5, ModelBook6,
            ModelBook7, ModelBook8, ModelBook9;
    //гриюы
    public static BlockModels ModelVishroom;
    //еда
    public static BlockModels ModelPlateVoid, ModelPlate1, ModelPlate2, ModelPlate3, ModelPlate4, ModelPlate5, ModelPlate6, ModelPlate7,
            ModelPlate8, ModelPlate9, ModelPlate10, ModelCup, ModelBottle;
    //кирпичи
    public static BlockModels ModelBricks1, ModelBricks2, ModelBricks3, ModelBricks4, ModelBricks5, ModelBricks6, ModelBricks7;
    //скелеты
    public static BlockModels ModelPileBones0, ModelPileBones1, ModelPileBones2, ModelPileBones3, ModelPileBones4, ModelPileBones5,
            ModelPileBones6, ModelPileBones7, ModelPileBones8, ModelPileBones9;
    //Урны
    public static BlockModels ModelUrn0, ModelUrn1, ModelUrn2, ModelUrn3, ModelUrn4, ModelFuneraryUrn0,
            ModelFuneraryUrn1, ModelFuneraryUrn2, ModelFuneraryUrn3;
    //склеп
    public static BlockModels ModelAltar, ModelStonePedestal, ModelStoneCoffin, ModelStatue, ModelGraveyardPlate0,
            ModelGraveyardPlate1, ModelGraveyardPlate2, ModelGraveyardPlate3;

    public static BlockModels LootContainer;

    //анимированные модели
    public static BlockModels ModelWisp;

    public static void preInit(FMLPreInitializationEvent event){
        ModelThreads = new BlockModels(Material.cloth, "ModelThreads", "threads", "threads");
        ModelTailorShelf = new BlockModels(Material.wood,"ModelTailorShelf", "tailor's_shelf", "tailor's_shelf");
        ModelCloth = new BlockModels(Material.cloth,"ModelCloth", "cloth", "cloth");
        ModelCloth.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelCloth.setCollisionEnabled(false);
        ModelDummy = new BlockModels(Material.wood, "ModelDummy", "dummy", "dummy");
        ModelDummy.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 1.6F, 0.8F);
        ModelHangers = new BlockModels(Material.wood,"ModelHangers", "hangers", "hangers");
        ModelHangers.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelHangers.setCollisionEnabled(false);
        ModelPillow = new BlockModels(Material.cloth,"ModelPillow", "pillow_for_needles", "pillow_for_needles");
        ModelRulers = new BlockModels(Material.wood,"ModelRulers", "rulers", "rulers");
        ModelRulers.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelRulers.setCollisionEnabled(false);
        ModelScissors = new BlockModels(Material.iron,"ModelScissors", "scissors", "scissors");

        //алхимик
        ModelIngredients = new BlockModels(Material.wood,"ModelIngredients", "ingredients", "ingredients");
        ModelIngredients.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelIngredients.setCollisionEnabled(false);
        ModelCauldron = new BlockModels(Material.iron,"ModelCauldron", "cauldron", "cauldron");
        ModelBottles = new BlockModels(Material.glass,"ModelBottles", "bottles", "bottles");
        ModelBooks = new BlockModels(Material.wood,"ModelBooks", "books", "books");

        ModelAlchemistShelf = new BlockModels(Material.wood,"ModelAlchemistShelf", "alchemist's_shelf", "alchemist's_shelf");

        ModelAlchemicalFlag = new BlockModels(Material.cloth,"ModelAlchemicalFlag", "alchemical_flag", "alchemical_flag");
        ModelAlchemicalFlag.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAlchemicalFlag.setCollisionEnabled(false);

        //ниже модели охотника
        ModelArrow = new BlockModels(Material.wood,"ModelArrow", "arrows", "arrows");
        ModelArrow.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelBowWall = new BlockModels(Material.wood,"ModelBowWall", "bow", "bow_on_the_wall");
        ModelBowWall.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelBowWall.setCollisionEnabled(false);

        ModelBow = new BlockModels(Material.wood,"ModelBow", "bow", "bow");
        ModelBow.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelDucks = new BlockModels(Material.cloth,"ModelDucks", "ducks", "ducks");
        ModelDucks.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDucks.setCollisionEnabled(false);

        ModelFurKnife = new BlockModels(Material.cloth,"ModelFurKnife", "fur_with_a_knife", "fur_with_a_knife");
        ModelFurKnife.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelFur = new BlockModels(Material.cloth,"ModelFur", "fur", "fur");
        ModelFur.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelHorn = new BlockModels(Material.cloth,"ModelHorn", "horn", "horn");
        ModelHorn.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelHorn.setCollisionEnabled(false);

        ModelLeatherDryer = new BlockModels(Material.wood,"ModelLeatherDryer", "leather_dryer", "leather_dryer");
        ModelLeatherDryer.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.7F, 1.0F);

        ModelRabbits = new BlockModels(Material.cloth,"ModelRabbits", "rabbits", "rabbits");
        ModelRabbits.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelRabbits.setCollisionEnabled(false);

        ModelMooseHead = new BlockModels(Material.cloth,"ModelMooseHead", "moose_head", "moose_head");
        ModelMooseHead.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        ModelDeerHead = new BlockModels(Material.cloth,"ModelDeerHead", "deer_head", "deer_head");
        ModelDeerHead.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        ModelDeerLegendHead = new BlockModels(Material.cloth,"ModelDeerLegendHead", "deer_legend_head", "deer_head");
        ModelDeerLegendHead.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        //ниже модели обменщика
        ModelMagnifyinGlass = new BlockModels(Material.glass,"ModelMagnifyinGlass", "magnifying_glass", "magnifying_glass");
        ModelMagnifyinGlass.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelBagGold = new BlockModels(Material.cloth,"ModelBagGold", "bag_of_gold", "bag_of_gold");

        ModelCoins = new BlockModels(Material.iron,"ModelCoins", "coins", "coins");
        ModelCoins.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelSmallChest = new BlockModels(Material.wood,"ModelSmallChest", "small_chest", "small_chest");
        ModelSmallChest.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.5F, 0.8F);

        ModelScales = new BlockModels(Material.iron,"ModelScales", "scales", "scales");
        ModelScales.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.5F, 0.9F);

        //ниже модели фермера
        ModelBagsPotatoes = new BlockModels(Material.cloth,"ModelBagsPotatoes", "bags_of_potatoes", "bags_of_potatoes");

        ModelBasketApples = new BlockModels(Material.cloth,"ModelBasketApples", "basket_of_apples", "basket_of_apples");
        ModelBasketApples.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.3F, 0.9F);

        ModelBasketBerries = new BlockModels(Material.cloth,"ModelBasketBerries", "basket_of_berries", "basket_of_berries");
        ModelBasketBerries.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.4F, 0.9F);

        ModelBucket = new BlockModels(Material.iron,"ModelBucket", "bucket", "bucket");
        ModelBucket.withRotatingBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.4F, 0.7F);

        ModelCarrot = new BlockModels(Material.plants,"ModelCarrot", "carrot", "carrot");
        ModelCarrot.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelGarlic = new BlockModels(Material.plants,"ModelGarlic", "garlic", "garlic");
        ModelGarlic.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelGarlic.setCollisionEnabled(false);

        ModelHay = new BlockModels(Material.grass,"ModelHay", "hay", "hay");
        ModelHay.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelHayfork = new BlockModels(Material.iron,"ModelHayfork", "hayfork", "hayfork");
        ModelHayfork.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
        ModelHayfork.setCollisionEnabled(false);

        ModelJugs = new BlockModels(Material.clay,"ModelJugs", "jugs", "jugs");

        ModelShelfFlower = new BlockModels(Material.wood,"ModelShelfFlower", "shelf_with_flower", "shelf_with_flower");

        ModelWateringCan = new BlockModels(Material.iron,"ModelWateringСan", "watering_can", "watering_can");
        ModelWateringCan.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelWateringCan.setCollisionEnabled(false);


        ModelWheelBarrow = new BlockModels(Material.wood,"ModelWheelBarrow", "wheelbarrow", "wheelbarrow");
        ModelWheelBarrow.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);


        //ниже модели ювелира
        ModelFilledChest = new BlockModels(Material.wood,"ModelFilledChest", "small_filled_chest", "small_filled_chest");
        ModelFilledChest.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.6F, 1.0F);


        ModelPliers = new BlockModels(Material.iron,"ModelPliers", "pliers", "pliers");
        ModelPliers.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);

        ModelJewelryHammer = new BlockModels(Material.iron,"ModelJewelryHammer", "jewelry_hammer", "jewelry_hammer");
        ModelJewelryHammer.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);

        ModelAmulet = new BlockModels(Material.iron,"ModelAmulet", "amulet", "amulet");
        ModelAmulet.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAmulet.setCollisionEnabled(false);

        ModelInstruments = new BlockModels(Material.wood,"ModelInstruments", "instruments", "instruments");
        ModelInstruments.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelInstruments.setCollisionEnabled(false);

        //ниже модели регистраторши
        ModelLute = new BlockModels(Material.wood,"ModelLute", "lute", "lute");
        ModelLute.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelLute.setCollisionEnabled(false);

        ModelBroom = new BlockModels(Material.wood,"ModelBroom", "broom", "broom");
        ModelBroom.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.5F, 1.0F);
        ModelBroom.setCollisionEnabled(false);

        ModelWanted = new BlockModels(Material.cloth,"ModelWanted", "wanted", "wanted");
        ModelWanted.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelWanted.setCollisionEnabled(false);

        ModelPapers = new BlockModels(Material.cloth,"ModelPapers", "papers", "papers");
        ModelPapers.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelKeys = new BlockModels(Material.iron,"ModelKeys", "keys", "keys");
        ModelKeys.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelKeys.setCollisionEnabled(false);

        ModelDeskBell = new BlockModels(Material.iron,"ModelDeskBell", "desk_bell", "desk_bell");
        ModelDeskBell.withRotatingBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.3F, 0.7F);
        ModelDeskBell.setCollisionEnabled(false);

        //ниже модели оружейника
        ModelSword = new BlockModels(Material.iron,"ModelSword", "sword", "sword");
        ModelSword.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelSwords = new BlockModels(Material.iron,"ModelSwords", "swords", "swords");
        ModelSwords.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);


        ModelShield1 = new BlockModels(Material.wood,"ModelShield1", "shield1", "shield");
        ModelShield1.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield1.setCollisionEnabled(false);

        ModelShield2 = new BlockModels(Material.wood,"ModelShield2", "shield2", "shield");
        ModelShield2.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield2.setCollisionEnabled(false);

        ModelShield3 = new BlockModels(Material.wood,"ModelShield3", "shield3", "shield");
        ModelShield3.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield3.setCollisionEnabled(false);

        ModelHelmet = new BlockModels(Material.iron,"ModelHelmet", "helmet", "helmet");
        ModelHelmet.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.7F, 0.9F);

        ModelHammer = new BlockModels(Material.iron,"ModelHammer", "hammer", "hammer");

        ModelDragonSlayer = new BlockModels(Material.iron, "ModelDragonSlayer", "dragon_slayer", "dragon_slayer")
                .setModelHeight(2);
        ModelDragonSlayer.withRotatingBounds(0.0F, 0.0F, 0.4F, 0.7F, 2.0F, 0.6F);

        ModelDragonSlayer.setStepSound(soundTypeAnvil);

        ModelAxe = new BlockModels(Material.iron,"ModelAxe", "axe", "axe");
        ModelAxe.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAxe.setCollisionEnabled(false);

        //Ниже модели инженера
        ModelGas = new BlockModels(Material.iron,"ModelGas", "gas_cylinders", "gas_cylinders");
        ModelGas.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.7F, 0.9F);

        ModelOiler = new BlockModels(Material.iron,"ModelOiler", "oiler", "oiler");
        ModelOiler.withRotatingBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.6F, 0.7F);

        ModelGears = new BlockModels(Material.iron,"ModelGears", "gears", "gears");
        ModelGears.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelDrawing1 = new BlockModels(Material.cloth,"ModelDrawing1", "drawing1", "drawing");
        ModelDrawing1.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDrawing1.setCollisionEnabled(false);

        ModelDrawing2 = new BlockModels(Material.cloth,"ModelDrawing2", "drawing2", "drawing");
        ModelDrawing2.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDrawing2.setCollisionEnabled(false);

        ModelClock = new BlockModels(Material.wood,"ModelClock", "clock", "clock");
        ModelClock.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelClock.setCollisionEnabled(false);

        ModelBrokenMechanism = new BlockModels(Material.iron,"ModelBrokenMechanism", "broken_mechanism", "broken_mechanism");

        ModelBrokenMechanism.withRotatingBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.3F, 0.7F);


        ModelBook0 = new BlockModels(Material.cloth, "ModelBook0", "book_0", "book_0");
        ModelBook0.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.3F, 0.8F);
        ModelBook1 = new BlockModels(Material.cloth, "ModelBook1", "book_1", "book_1");
        ModelBook1.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.5F, 0.8F);
        ModelBook2 = new BlockModels(Material.cloth, "ModelBook2", "book_2", "book_2");
        ModelBook2.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.6F, 0.9F);
        ModelBook3 = new BlockModels(Material.cloth, "ModelBook3", "book_3", "book_3");
        ModelBook3.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.7F, 0.9F);
        ModelBook4 = new BlockModels(Material.cloth, "ModelBook4", "book_4", "book_4");
        ModelBook4.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.65F, 1.0F);
        ModelBook5 = new BlockModels(Material.cloth, "ModelBook5", "book_5", "book_5");
        ModelBook5.withRotatingBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.75F, 0.9F);
        ModelBook6 = new BlockModels(Material.cloth, "ModelBook6", "book_6", "book_6");
        ModelBook6.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.65F, 1.0F);
        ModelBook7 = new BlockModels(Material.cloth, "ModelBook7", "book_7", "book_7");
        ModelBook7.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.65F, 1.0F);
        ModelBook8 = new BlockModels(Material.cloth, "ModelBook8", "book_8", "book_8");
        ModelBook8.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.3F, 0.8F);
        ModelBook9 = new BlockModels(Material.cloth, "ModelBook9", "book_9", "book_9");


        ModelVishroom = new BlockModels(Material.plants, "ModelVishroom", "vishroom", "vishroom");
        ModelVishroom.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.5F, 0.8F);

        ModelPlateVoid = new BlockModels(Material.iron, "ModelPlateVoid", "plate_empty", "plate");
        ModelPlateVoid.setStepSound(soundTypeGlass);
        ModelPlateVoid.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);

        ModelPlate1 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate1", "plate_1", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(6)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate1.setStepSound(soundTypeGlass);
        ModelPlate1.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate2 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate2", "plate_2", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(4)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate2.setStepSound(soundTypeGlass);
        ModelPlate2.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate3 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate3", "plate_3", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(6)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate3.setStepSound(soundTypeGlass);
        ModelPlate3.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate4 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate4", "plate_4", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(7)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate4.setStepSound(soundTypeGlass);
        ModelPlate4.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate5 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate5", "plate_5", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(8)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate5.setStepSound(soundTypeGlass);
        ModelPlate5.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate6 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate6", "plate_6", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(6)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate6.setStepSound(soundTypeGlass);
        ModelPlate6.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate7 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate7", "plate_7", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(7)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate7.setStepSound(soundTypeGlass);
        ModelPlate7.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate8 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate8", "plate_8", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(4)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate8.setStepSound(soundTypeGlass);
        ModelPlate8.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate9 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate9", "plate_9", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(4)
                .setSaturationModifier(0.4F)
                .setEatDelay(30);
        ModelPlate9.setStepSound(soundTypeGlass);
        ModelPlate9.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);
        ModelPlate10 = ((BlockModelFood) new BlockModelFood(Material.cloth, "ModelPlate10", "plate_10", "plate"))
                .setEmptyBlock(ModelPlateVoid)
                .setFoodAmount(4)
                .setSaturationModifier(0.4F)
                .setEatSound("mbo:omnomnom")
                .setEatDelay(30).
                setFoodEffects(
                    new PotionEffect(PotionRegistry.SixthSense.id, 200, 0),
                    new PotionEffect(Potion.confusion.id, 600, 1)
        );
        ModelPlate10.setStepSound(soundTypeGlass);
        ModelPlate10.withRotatingBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.1F, 0.8F);

        ModelWisp = new BlockModels(Material.cloth,"ModelWisp", "wisp", "wisp")
                .withAnimation("idle", true)
                .withAnimatedTexture(6, 100)
                .withParticleTexture("wisp_0");

        ModelCup = new BlockModels(Material.glass, "ModelCup", "cup", "cup");
        ModelCup.withRotatingBounds(0.4F, 0.0F, 0.4F, 0.6F, 0.3F, 0.6F);

        ModelBottle  = new BlockModels(Material.glass, "ModelBottle", "bottle", "bottle");
        ModelBottle.withRotatingBounds(0.4F, 0.0F, 0.4F, 0.6F, 0.5F, 0.6F);

        LootContainer = new BlockDestructibleLootContainer(Material.cloth, "LootContainer");

        ModelBricks1 = new BlockModels(Material.rock, "ModelBricks1", "bricks_1", "bricks_1");
        ModelBricks1.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelBricks1.setStepSound(soundTypeStone);
        ModelBricks2 = new BlockModels(Material.rock, "ModelBricks2", "bricks_2", "bricks_2");
        ModelBricks2.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelBricks2.setStepSound(soundTypeStone);
        ModelBricks3 = new BlockModels(Material.rock, "ModelBricks3", "bricks_3", "bricks_3");
        ModelBricks3.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelBricks3.setStepSound(soundTypeStone);
        ModelBricks4 = new BlockModels(Material.rock, "ModelBricks4", "bricks_4", "bricks_4");
        ModelBricks5 = new BlockModels(Material.rock, "ModelBricks5", "bricks_5", "bricks_5");
        ModelBricks6 = new BlockModels(Material.rock, "ModelBricks6", "bricks_6", "bricks_6");
        ModelBricks6.setStepSound(soundTypeStone);
        ModelBricks7 = new BlockModels(Material.rock, "ModelBricks7", "bricks_7", "bricks_7");
        ModelBricks7.setStepSound(soundTypeStone);

        ModelPileBones0 = new BlockModels(Material.rock, "ModelPileBones0", "pile_of_bones_0", "pile_of_bones_0");
        ModelPileBones0.setStepSound(soundTypeStone);
        ModelPileBones0.withRotatingBounds(0.25F, 0.0F, 0.6F, 0.8F, 0.35F, 0.9F);

        ModelPileBones1 = new BlockModels(Material.rock, "ModelPileBones1", "pile_of_bones_1", "pile_of_bones_1");
        ModelPileBones1.setStepSound(soundTypeStone);
        ModelPileBones1.withRotatingBounds(0.1F, 0.0F, 0.2F, 0.7F, 0.5F, 0.7F);

        ModelPileBones2 = new BlockModels(Material.rock, "ModelPileBones2", "pile_of_bones_2", "pile_of_bones_2");
        ModelPileBones2.setStepSound(soundTypeStone);
        ModelPileBones2.withRotatingBounds(0.2F, 0.0F, 0.0F, 0.8F, 0.8F, 0.35F);

        ModelPileBones3 = new BlockModels(Material.rock, "ModelPileBones3", "pile_of_bones_3", "pile_of_bones_3");
        ModelPileBones3.setStepSound(soundTypeStone);
        ModelPileBones3.withRotatingBounds(0.15F, 0.0F, 0.0F, 0.9F, 0.5F, 0.9F);

        ModelPileBones4 = new BlockModels(Material.rock, "ModelPileBones4", "pile_of_bones_4", "pile_of_bones_4");
        ModelPileBones4.setStepSound(soundTypeStone);
        ModelPileBones4.withRotatingBounds(0.3F, 0.0F, 0.1F, 1.0F, 0.7F, 0.65F);

        ModelPileBones5 = new BlockModels(Material.rock, "ModelPileBones5", "pile_of_bones_5", "pile_of_bones_5");
        ModelPileBones5.setStepSound(soundTypeStone);
        ModelPileBones5.withRotatingBounds(0.15F, 0.0F, 0.0F, 0.85F, 0.25F, 1.0F);

        ModelPileBones6 = new BlockModels(Material.rock, "ModelPileBones6", "pile_of_bones_6", "pile_of_bones_6");
        ModelPileBones6.setStepSound(soundTypeStone);
        ModelPileBones6.withRotatingBounds(0.3F, 0.0F, 0.0F, 0.8F, 0.45F, 1.0F);

        ModelPileBones7 = new BlockModels(Material.rock, "ModelPileBones7", "pile_of_bones_7", "pile_of_bones_7");
        ModelPileBones7.setStepSound(soundTypeStone);
        ModelPileBones7.withRotatingBounds(0.15F, 0.0F, 0.1F, 0.85F, 0.3F, 1.0F);
        ModelPileBones8 = new BlockModels(Material.rock, "ModelPileBones8", "pile_of_bones_8", "pile_of_bones_8");
        ModelPileBones8.setStepSound(soundTypeStone);
        ModelPileBones8.withRotatingBounds(0.15F, 0.0F, 0.0F, 0.85F, 0.25F, 1.0F);
        ModelPileBones9 = new BlockModels(Material.rock, "ModelPileBones9", "pile_of_bones_9", "pile_of_bones_9");
        ModelPileBones9.setStepSound(soundTypeStone);
        ModelPileBones9.withRotatingBounds(0.2F, 0.0F, 0.0F, 0.8F, 0.8F, 0.35F);

        ModelUrn0 = new BlockModels(Material.rock, "ModelUrn0", "urn_0", "urn_0");
        ModelUrn1 = new BlockModels(Material.rock, "ModelUrn1", "urn_1", "urn_1");
        ModelUrn2 = new BlockModels(Material.rock, "ModelUrn2", "urn_2", "urn_1");
        ModelUrn3 = new BlockModels(Material.rock, "ModelUrn3", "urn_3", "urn_1");
        ModelUrn4 = new BlockModels(Material.rock, "ModelUrn4", "urn_4", "urn_4");

        ModelFuneraryUrn0 = new BlockModels(Material.rock, "ModelFuneraryUrn0", "funerary_urn_0", "funerary_urn_0");
        ModelFuneraryUrn1 = new BlockModels(Material.rock, "ModelFuneraryUrn1", "funerary_urn_1", "funerary_urn_1");
        ModelFuneraryUrn2 = new BlockModels(Material.rock, "ModelFuneraryUrn2", "funerary_urn_2", "funerary_urn_2");
        ModelFuneraryUrn3 = new BlockModels(Material.rock, "ModelFuneraryUrn3", "funerary_urn_3", "funerary_urn_3");
        ModelFuneraryUrn3.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelFuneraryUrn3.setModelHeight(2);

        ModelAltar = new BlockModels(Material.rock, "ModelAltar", "altar", "altar")
                .addSideCollision(-1).addSideCollision(1);;
        ModelAltar.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        ModelStonePedestal = new BlockModels(Material.rock, "ModelStonePedestal", "stone_pedestal", "stone_pedestal");
        ModelStonePedestal.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        ModelStoneCoffin = new BlockModels(Material.rock, "ModelStoneCoffin", "stone_coffin", "stone_coffin").
        addForwardCollision(-1);
        ModelStoneCoffin.withRotatingBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        ModelStatue = new BlockModels(Material.rock, "ModelStatue", "statue", "statue");
        ModelStatue.withRotatingBounds(0.17F, 0.0F, 0.32F, 0.83F, 1.0F, 0.68F);
        ModelStatue.addVerticalCollisionUsingBaseBounds(1);


    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event){
        for (BlockModels model : BlockModels.getAllModels()) {
            model.register();
        }
    }
}
