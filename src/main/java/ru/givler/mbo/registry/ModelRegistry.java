package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.material.Material;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.BlockConnectedRope;
import ru.givler.mbo.block.special.BlockDestructibleLootContainer;
import ru.givler.mbo.block.model.BlockModelFood;

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

    // Instruments of torture
    public static BlockModels ModelTortureCorpseTruck;
    public static BlockModels ModelTortureBag;
    public static BlockModels ModelTortureBedOnChains;
    public static BlockModels ModelTortureBeheading;
    public static BlockModels ModelTortureBench;
    public static BlockModels ModelTortureBundleOfKeys;
    public static BlockModels ModelTortureBagsOnTheFeet;
    public static BlockModels ModelTortureDeathHorse;
    public static BlockModels ModelTortureEmbalmedCorpse;
    public static BlockModels ModelTortureGrave;
    public static BlockModels ModelTortureGuillotine;
    public static BlockModels ModelTortureHandcuffs;
    public static BlockModels ModelTortureHangedCorpse;
    public static BlockModels ModelTortureHook;
    public static BlockModels ModelTortureHourglass;
    public static BlockModels ModelTortureIronBall;
    public static BlockModels ModelTortureIronCage;
    public static BlockModels ModelTortureIronCollar;
    public static BlockModels ModelTortureIronMaiden;
    public static BlockModels ModelTortureKeys;
    public static BlockModels ModelTorturePileOfRottingCorpses;
    public static BlockModels ModelTorturePlaceOfSleep;
    public static BlockModels ModelTorturePrisonWagon;
    public static BlockModels ModelTortureRope;
    public static BlockModels ModelTortureRope1;
    public static BlockModels ModelTortureRope2;
    public static BlockModels ModelTortureRope3;
    public static BlockModels ModelTortureRope4;
    public static BlockModels ModelTortureRope5;
    public static BlockModels ModelTortureRopeAroundTheNeck;
    public static BlockModels ModelTortureRopeCore;
    public static BlockModels ModelTortureSharpenedInThePads;
    public static BlockModels ModelTortureStoneGrave;
    public static BlockModels ModelTortureStretchingTable;
    public static BlockModels ModelTortureSuspendedCorpse;
    public static BlockModels ModelTortureToolsForOversupply;
    public static BlockModels ModelTortureToolsForOversupply1;
    public static BlockModels ModelTortureToolsForOversupply2;
    public static BlockModels ModelTortureToolsForOversupply3;
    public static BlockModels ModelTortureToolsForOversupply4;
    public static BlockModels ModelTortureToolsForOversupply5;
    public static BlockModels ModelTortureTortureTable;
    public static BlockModels ModelTortureTortureTable1;
    public static BlockModels ModelTortureTortureWithCoals;
    public static BlockModels ModelTortureWheeledCorpse;
    public static BlockModels ModelTortureWheeling;
    public static BlockModels ModelTortureWitchSChair;

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

        registerTortureModels();
    }

    private static void registerTortureModels() {
        ModelTortureCorpseTruck = new BlockModels(Material.wood, "ModelTortureCorpseTruck", "instruments_of_torture/corpse_truck", "instruments_of_torture/corpse_truck");
        ModelTortureCorpseTruck.withMultipartCollisionBoxes(new float[][] {
            {-0.125000F, 0.000000F, -0.500000F, 0.000000F, 1.000000F, 0.250000F},
            {-0.125000F, 0.125000F, -0.625000F, 0.000000F, 0.875000F, -0.500000F},
            {-0.125000F, 0.125000F, 0.250000F, 0.000000F, 0.875000F, 0.375000F},
            {0.000000F, 0.375000F, -0.250000F, 0.125000F, 0.625000F, 0.000000F},
            {0.000000F, 0.562500F, -0.937500F, 1.250000F, 0.687500F, 0.687500F},
            {0.000000F, 0.562500F, 0.687500F, 0.125000F, 0.687500F, 1.375000F},
            {0.109375F, 0.441078F, -0.296240F, 0.390625F, 0.853717F, 0.521979F},
            {0.125000F, 0.437500F, -0.187500F, 1.125000F, 0.562500F, -0.062500F},
            {0.359375F, 0.671875F, -0.390625F, 0.890625F, 0.953125F, 0.390625F},
            {0.360293F, 0.417402F, 0.262622F, 0.664605F, 1.092543F, 1.069328F},
            {0.451756F, 0.402724F, -0.879034F, 1.065997F, 1.020939F, -0.214145F},
            {0.622644F, 0.402687F, 0.297826F, 0.886675F, 0.817568F, 1.084158F},
            {0.859375F, 0.813936F, -0.506424F, 1.140625F, 1.251471F, 0.316229F},
            {1.125000F, 0.375000F, -0.250000F, 1.250000F, 0.625000F, 0.000000F},
            {1.125000F, 0.562500F, 0.687500F, 1.250000F, 0.687500F, 1.375000F},
            {1.250000F, 0.000000F, -0.500000F, 1.375000F, 1.000000F, 0.250000F},
            {1.250000F, 0.125000F, -0.625000F, 1.375000F, 0.875000F, -0.500000F},
            {1.250000F, 0.125000F, 0.250000F, 1.375000F, 0.875000F, 0.375000F},
        });

        ModelTortureBag = new BlockModels(Material.cloth, "ModelTortureBag", "instruments_of_torture/bag", "instruments_of_torture/bag");
        ModelTortureBag.withModelOffsetY(-0.1250F);
        ModelTortureBag.withMultipartCollisionBoxes(new float[][] {
            {0.250000F, 0.000000F, 0.375000F, 0.625000F, 0.375000F, 0.750000F},
            {0.312500F, 0.375000F, 0.437500F, 0.562500F, 0.437500F, 0.687500F},
            {0.517453F, 0.384662F, 0.425486F, 0.709677F, 0.386662F, 0.587014F},
            {0.552004F, 0.376932F, 0.530250F, 0.737097F, 0.421509F, 0.594750F},
        });
        ModelTortureBag.setCollisionEnabled(false);

        ModelTortureBedOnChains = new BlockModels(Material.wood, "ModelTortureBedOnChains", "instruments_of_torture/bed_on_chains", "instruments_of_torture/bed_on_chains");
        ModelTortureBedOnChains.withModelOffsetY(0.2625F);
        ModelTortureBedOnChains.withSleepingNoRespawn(0.3875F,1,.7F,.5625F);
        ModelTortureBedOnChains.withMultipartCollisionBoxes(new float[][] {
            {0.125F, 0.2625F, 0.0F, 1.875F, 0.3875F, 0.875F},
        });

        ModelTortureBeheading = new BlockModels(Material.wood, "ModelTortureBeheading", "instruments_of_torture/beheading", "instruments_of_torture/beheading");
        ModelTortureBeheading.withMultipartCollisionBoxes(new float[][] {
            {-0.786633F, 1.201307F, 0.768750F, 0.540284F, 1.817903F, 0.856250F},
            {0.000000F, 0.000000F, 0.500000F, 0.250000F, 0.750000F, 1.000000F},
            {0.076416F, 0.563051F, 0.811500F, 1.011116F, 1.430101F, 0.813500F},
            {0.085668F, -0.012500F, 0.966208F, 0.934934F, 0.075000F, 1.815473F},
            {0.085668F, 0.050000F, 0.966208F, 0.415252F, 0.512500F, 1.600214F},
            {0.143410F, 0.050000F, 1.509808F, 0.661932F, 0.512500F, 1.791556F},
            {0.250000F, 0.000000F, 0.500000F, 0.750000F, 0.500000F, 1.000000F},
            {0.250000F, 0.500000F, 0.500000F, 0.750000F, 0.750000F, 0.875000F},
            {0.290723F, 0.237513F, 1.054079F, 0.790723F, 0.890794F, 1.707360F},
            {0.358670F, 0.050000F, 0.990125F, 0.877191F, 0.512500F, 1.271874F},
            {0.476116F, 1.088143F, 0.750000F, 0.721097F, 1.333124F, 0.875000F},
            {0.605350F, 0.050000F, 1.181467F, 0.934934F, 0.512500F, 1.815473F},
            {0.750000F, 0.000000F, 0.500000F, 1.000000F, 0.750000F, 1.000000F},
        });

        ModelTortureBench = new BlockModels(Material.wood, "ModelTortureBench", "instruments_of_torture/bench", "instruments_of_torture/bench");
        ModelTortureBench.withMultipartCollisionBoxes(new float[][] {
            {-0.500000F, 0.375000F, 0.125000F, 1.500000F, 0.500000F, 0.875000F},
            {-0.375000F, 0.000000F, 0.250000F, -0.250000F, 0.375000F, 0.750000F},
            {-0.250000F, 0.125000F, 0.437500F, 1.250000F, 0.250000F, 0.562500F},
            {1.250000F, 0.000000F, 0.250000F, 1.375000F, 0.375000F, 0.750000F},
        });
        ModelTortureBench.withSeats(new float[]{0F,.5F,.5F},new float[]{1F,.5F,.5F});

        ModelTortureBundleOfKeys = new BlockModels(Material.iron, "ModelTortureBundleOfKeys", "instruments_of_torture/bundle_of_keys", "instruments_of_torture/bundle_of_keys");
        ModelTortureBundleOfKeys.withMultipartCollisionBoxes(new float[][] {
            {0.328125F, 0.000000F, -0.006250F, 0.703125F, 0.875000F, 0.056250F},
            {0.359375F, 0.437500F, 0.056250F, 0.671875F, 0.500000F, 0.118750F},
            {0.359375F, 0.500000F, 0.056250F, 0.421875F, 0.687500F, 0.118750F},
            {0.359375F, 0.687500F, 0.056250F, 0.671875F, 0.750000F, 0.118750F},
            {0.382398F, 0.337500F, -0.009883F, 0.511893F, 0.525000F, 0.187262F},
            {0.409375F, 0.675000F, 0.043750F, 0.621875F, 0.762500F, 0.131250F},
            {0.433880F, 0.086500F, 0.058512F, 0.484329F, 0.338500F, 0.176610F},
            {0.453125F, 0.625000F, 0.056250F, 0.578125F, 0.687500F, 0.118750F},
            {0.546875F, 0.343750F, -0.006250F, 0.609375F, 0.531250F, 0.181250F},
            {0.577125F, 0.092750F, 0.055250F, 0.579125F, 0.344750F, 0.182250F},
            {0.609375F, 0.500000F, 0.056250F, 0.671875F, 0.687500F, 0.118750F},
        });
        ModelTortureBundleOfKeys.setCollisionEnabled(false);

        ModelTortureBagsOnTheFeet = new BlockModels(Material.cloth, "ModelTortureBagsOnTheFeet", "instruments_of_torture/bags_on_the_feet", "instruments_of_torture/bags_on_the_feet");
        ModelTortureBagsOnTheFeet.withMultipartCollisionBoxes(new float[][] {
            {0.312500F, 0.000000F, 0.312500F, 0.687500F, 0.375000F, 0.687500F},
            {0.375000F, 0.406250F, 0.375000F, 0.625000F, 0.531250F, 0.625000F},
            {0.437500F, 0.375000F, 0.437500F, 0.562500F, 0.437500F, 0.562500F},
        });
        ModelTortureBagsOnTheFeet.setCollisionEnabled(false);

        ModelTortureDeathHorse = new BlockModels(Material.wood, "ModelTortureDeathHorse", "instruments_of_torture/death_horse", "instruments_of_torture/death_horse");
        ModelTortureDeathHorse.withMultipartCollisionBoxes(new float[][] {
            {0.187500F, 0.000000F, 0.000000F, 0.312500F, 0.625000F, 0.187500F},
            {0.187500F, 0.000000F, 0.812500F, 0.312500F, 0.625000F, 1.000000F},
            {0.187500F, 0.625000F, 0.000000F, 0.812500F, 0.875000F, 0.187500F},
            {0.187500F, 0.625000F, 0.812500F, 0.812500F, 0.875000F, 1.000000F},
            {0.312500F, 0.187500F, 0.062500F, 0.687500F, 0.375000F, 0.125000F},
            {0.312500F, 0.187500F, 0.875000F, 0.687500F, 0.375000F, 0.937500F},
            {0.375000F, 0.625000F, 0.000000F, 0.625000F, 0.875000F, 1.000000F},
            {0.687500F, 0.000000F, 0.000000F, 0.812500F, 0.625000F, 0.187500F},
            {0.687500F, 0.000000F, 0.812500F, 0.812500F, 0.625000F, 1.000000F},
        });

        ModelTortureEmbalmedCorpse = new BlockModels(Material.cloth, "ModelTortureEmbalmedCorpse", "instruments_of_torture/embalmed_corpse", "instruments_of_torture/embalmed_corpse");
        ModelTortureEmbalmedCorpse.withMultipartCollisionBoxes(new float[][] {
            {-0.126000F, 0.000000F, -1.001000F, 1.126000F, 0.001000F, 1.376000F},
            {0.037500F, 0.000000F, -0.337500F, 0.962500F, 0.275000F, 0.462500F},
            {0.225000F, 0.000000F, -0.837500F, 0.775000F, 0.400000F, -0.287500F},
            {0.225000F, 0.000000F, 0.412500F, 0.775000F, 0.275000F, 1.212500F},
        });
        ModelTortureEmbalmedCorpse.setCollisionEnabled(false);

        ModelTortureGrave = new BlockModels(Material.rock, "ModelTortureGrave", "instruments_of_torture/grave", "instruments_of_torture/grave");
        ModelTortureGrave.withMultipartCollisionBoxes(new float[][] {
            {-0.251000F, 0.005250F, -0.251000F, 1.251000F, 0.007250F, 2.001000F},
            {0.000000F, 0.000000F, 0.000000F, 1.000000F, 0.006250F, 1.750000F},
            {0.542826F, 0.000000F, 0.155250F, 0.977658F, 0.378032F, 0.157250F},
            {0.786792F, 0.288640F, 0.112500F, 0.901116F, 0.402964F, 0.200000F},
            {0.803124F, 0.304972F, 0.125000F, 1.195715F, 1.137284F, 0.187500F},
            {1.073805F, 0.981550F, 0.112500F, 1.212047F, 1.153617F, 0.200000F},
        });

        ModelTortureGuillotine = new BlockModels(Material.iron, "ModelTortureGuillotine", "instruments_of_torture/guillotine", "instruments_of_torture/guillotine");
        ModelTortureGuillotine.withModelOffsetY(1.0000F);
        ModelTortureGuillotine.withMultipartCollisionBoxes(new float[][] {
            {-0.375000F, 0.781250F, 0.312500F, -0.187500F, 0.968750F, 0.687500F},
            {-0.325000F, 0.643750F, 0.800000F, -0.237500F, 0.731250F, 1.012500F},
            {-0.312500F, 0.656250F, 0.750000F, -0.250000F, 0.843750F, 0.812500F},
            {-0.312500F, 0.843750F, 0.687500F, -0.250000F, 0.906250F, 0.812500F},
            {-0.250000F, 0.937500F, 0.468750F, -0.187500F, 2.937500F, 0.531250F},
            {-0.187500F, 0.000000F, 0.000000F, 0.062500F, 0.125000F, 0.375000F},
            {-0.187500F, 0.000000F, 0.375000F, 0.062500F, 2.875000F, 0.625000F},
            {-0.187500F, 0.000000F, 0.625000F, 0.062500F, 0.125000F, 1.000000F},
            {-0.187500F, 2.875000F, 0.406250F, -0.062500F, 3.000000F, 0.593750F},
            {-0.063500F, 0.124000F, -0.001000F, -0.061500F, 0.876000F, 1.001000F},
            {-0.062500F, 2.875000F, 0.468750F, 0.500000F, 2.937500F, 0.531250F},
            {0.061500F, 1.936500F, 0.499000F, 1.063500F, 2.438500F, 0.501000F},
            {0.062500F, 0.000000F, 0.437500F, 1.062500F, 0.500000F, 0.562500F},
            {0.062500F, 0.500000F, 0.375000F, 1.062500F, 0.625000F, 0.625000F},
            {0.062500F, 0.625000F, 0.375000F, 1.062500F, 0.875000F, 0.625000F},
            {0.062500F, 0.875000F, 0.375000F, 1.062500F, 1.125000F, 0.625000F},
            {0.062500F, 1.125000F, 0.375000F, 1.062500F, 1.250000F, 0.625000F},
            {0.062500F, 2.437500F, 0.437500F, 1.062500F, 2.625000F, 0.562500F},
            {0.062500F, 2.687500F, 0.375000F, 1.062500F, 2.875000F, 0.625000F},
            {0.500000F, 2.625000F, 0.437500F, 0.625000F, 2.687500F, 0.562500F},
            {0.500000F, 2.875000F, 0.406250F, 0.625000F, 3.000000F, 0.593750F},
            {1.062500F, 0.000000F, 0.000000F, 1.312500F, 0.125000F, 0.375000F},
            {1.062500F, 0.000000F, 0.375000F, 1.312500F, 2.875000F, 0.625000F},
            {1.062500F, 0.000000F, 0.625000F, 1.312500F, 0.125000F, 1.000000F},
            {1.186500F, 0.124000F, -0.001000F, 1.188500F, 0.876000F, 1.001000F},
        });

        ModelTortureHandcuffs = new BlockModels(Material.iron, "ModelTortureHandcuffs", "instruments_of_torture/handcuffs", "instruments_of_torture/handcuffs");
        ModelTortureHandcuffs.withMultipartCollisionBoxes(new float[][] {
            {0.000000F, 0.750000F, 0.000000F, 1.000000F, 1.000000F, 0.062500F},
            {0.050000F, 0.118750F, 0.050000F, 0.325000F, 0.393750F, 0.200000F},
            {0.119795F, 0.380250F, 0.057295F, 0.255205F, 0.569750F, 0.192705F},
            {0.119795F, 0.630250F, 0.057295F, 0.255205F, 0.819750F, 0.192705F},
            {0.186500F, 0.505250F, 0.030250F, 0.188500F, 0.694750F, 0.219750F},
            {0.186500F, 0.755250F, 0.030250F, 0.188500F, 0.944750F, 0.219750F},
            {0.675000F, 0.112500F, 0.050000F, 0.950000F, 0.387500F, 0.200000F},
            {0.744795F, 0.374000F, 0.057295F, 0.880205F, 0.563500F, 0.192705F},
            {0.744795F, 0.624000F, 0.057295F, 0.880205F, 0.813500F, 0.192705F},
            {0.811500F, 0.499000F, 0.030250F, 0.813500F, 0.688500F, 0.219750F},
            {0.811500F, 0.749000F, 0.030250F, 0.813500F, 0.938500F, 0.219750F},
        });
        ModelTortureHandcuffs.setCollisionEnabled(false);

        ModelTortureHangedCorpse = new BlockModels(Material.cloth, "ModelTortureHangedCorpse", "instruments_of_torture/hanged_corpse", "instruments_of_torture/hanged_corpse");
        ModelTortureHangedCorpse.withModelOffsetY(1.0000F);
        ModelTortureHangedCorpse.withRotatingBounds(0.0625F, 0.0000F, 0.2500F, 0.9375F, 1.0000F, 1.0000F);
        ModelTortureHangedCorpse.setCollisionEnabled(false);

        ModelTortureHook = new BlockModels(Material.iron, "ModelTortureHook", "instruments_of_torture/hook", "instruments_of_torture/hook");
        ModelTortureHook.withRotatingBounds(0.4375F, 0.2500F, 0.3750F, 0.5625F, 1.0000F, 0.8750F);
        ModelTortureHook.setCollisionEnabled(false);

        ModelTortureHourglass = new BlockModels(Material.iron, "ModelTortureHourglass", "instruments_of_torture/hourglass", "instruments_of_torture/hourglass");
        ModelTortureHourglass.withMultipartCollisionBoxes(new float[][] {
            {0.187500F, 0.000000F, 0.187500F, 0.812500F, 0.125000F, 0.812500F},
            {0.187500F, 0.125000F, 0.187500F, 0.312500F, 0.750000F, 0.312500F},
            {0.187500F, 0.125000F, 0.687500F, 0.312500F, 0.750000F, 0.812500F},
            {0.187500F, 0.750000F, 0.187500F, 0.812500F, 0.875000F, 0.812500F},
            {0.300000F, 0.112500F, 0.300000F, 0.700000F, 0.387500F, 0.700000F},
            {0.300000F, 0.487500F, 0.300000F, 0.700000F, 0.762500F, 0.700000F},
            {0.437500F, 0.375000F, 0.437500F, 0.562500F, 0.500000F, 0.562500F},
            {0.687500F, 0.125000F, 0.187500F, 0.812500F, 0.750000F, 0.312500F},
            {0.687500F, 0.125000F, 0.687500F, 0.812500F, 0.750000F, 0.812500F},
        });

        ModelTortureIronBall = new BlockModels(Material.iron, "ModelTortureIronBall", "instruments_of_torture/iron_ball", "instruments_of_torture/iron_ball");
        ModelTortureIronBall.withMultipartCollisionBoxes(new float[][] {
            {0.437500F, 0.125000F, 0.312500F, 0.812500F, 0.250000F, 0.687500F},
            {0.749000F, 0.092750F, 0.499000F, 0.938500F, 0.282250F, 0.501000F},
            {0.874000F, 0.119795F, 0.432295F, 1.063500F, 0.255205F, 0.567705F},
            {0.999000F, 0.092750F, 0.499000F, 1.188500F, 0.282250F, 0.501000F},
            {1.124000F, 0.119795F, 0.432295F, 1.313500F, 0.255205F, 0.567705F},
            {1.242417F, 0.000000F, 0.102252F, 1.772748F, 0.375000F, 0.632583F},
        });

        ModelTortureIronCage = new ru.givler.mbo.block.model.BlockDecorCage();

        ModelTortureIronCollar = new BlockModels(Material.iron, "ModelTortureIronCollar", "instruments_of_torture/iron_collar", "instruments_of_torture/iron_collar");
        ModelTortureIronCollar.withMultipartCollisionBoxes(new float[][] {
            {-0.049010F, 0.067750F, 0.109815F, 0.933525F, 0.069750F, 1.092350F},
            {0.000000F, 0.750000F, 0.000000F, 1.000000F, 1.000000F, 0.062500F},
            {0.099285F, -0.006250F, 0.258110F, 0.785230F, 0.143750F, 0.944055F},
            {0.432295F, 0.130250F, 0.145683F, 0.567705F, 0.319750F, 0.281094F},
            {0.432295F, 0.380250F, 0.057295F, 0.567705F, 0.569750F, 0.192705F},
            {0.432295F, 0.630250F, 0.057295F, 0.567705F, 0.819750F, 0.192705F},
            {0.499000F, 0.005250F, 0.155250F, 0.501000F, 0.194750F, 0.344750F},
            {0.499000F, 0.255250F, 0.030250F, 0.501000F, 0.444750F, 0.219750F},
            {0.499000F, 0.505250F, 0.030250F, 0.501000F, 0.694750F, 0.219750F},
            {0.499000F, 0.755250F, 0.030250F, 0.501000F, 0.944750F, 0.219750F},
        });
        ModelTortureIronCollar.setCollisionEnabled(false);

        ModelTortureIronMaiden = new BlockModels(Material.iron, "ModelTortureIronMaiden", "instruments_of_torture/iron_maiden", "instruments_of_torture/iron_maiden");
        ModelTortureIronMaiden.withModelOffsetY(0.8750F);
        ModelTortureIronMaiden.withMultipartCollisionBoxes(new float[][] {
            {-0.812500F, 0.000000F, 0.000000F, 0.687500F, 0.187500F, 1.000000F},
            {-0.812500F, 0.187500F, 0.437500F, -0.687500F, 1.937500F, 0.562500F},
            {-0.751000F, 1.936500F, 0.499000F, 0.626000F, 2.876000F, 0.501000F},
            {-0.687500F, 0.187500F, 0.125000F, -0.562500F, 1.937500F, 0.500000F},
            {-0.687500F, 0.187500F, 0.500000F, -0.562500F, 1.562500F, 0.750000F},
            {-0.687500F, 0.187500F, 0.750000F, -0.062500F, 1.562500F, 0.875000F},
            {-0.687500F, 1.562500F, 0.500000F, -0.562500F, 1.937500F, 0.875000F},
            {-0.562500F, 0.187500F, 0.125000F, 0.437500F, 1.937500F, 0.250000F},
            {-0.562500F, 1.562500F, 0.750000F, -0.062500F, 1.937500F, 0.875000F},
            {-0.562500F, 1.812500F, 0.250000F, -0.312500F, 1.937500F, 0.500000F},
            {-0.562500F, 1.812500F, 0.500000F, -0.312500F, 1.937500F, 0.750000F},
            {-0.437500F, 1.937500F, 0.125000F, -0.312500F, 2.562500F, 0.500000F},
            {-0.437500F, 1.937500F, 0.500000F, -0.312500F, 2.562500F, 0.750000F},
            {-0.437500F, 1.937500F, 0.750000F, 0.312500F, 2.562500F, 0.875000F},
            {-0.376000F, 0.874000F, 0.436500F, -0.374000F, 1.001000F, 0.813500F},
            {-0.376000F, 1.249000F, 0.436500F, -0.374000F, 1.376000F, 0.813500F},
            {-0.376000F, 1.624000F, 0.374000F, -0.374000F, 1.751000F, 0.751000F},
            {-0.313500F, 1.186500F, 0.186500F, -0.311500F, 1.313500F, 0.563500F},
            {-0.313500F, 1.624000F, 0.124000F, -0.311500F, 1.751000F, 0.501000F},
            {-0.312500F, 1.937500F, 0.125000F, 0.187500F, 2.562500F, 0.250000F},
            {-0.312500F, 2.437500F, 0.250000F, 0.187500F, 2.562500F, 0.500000F},
            {-0.312500F, 2.437500F, 0.500000F, 0.187500F, 2.562500F, 0.750000F},
            {-0.251000F, 0.811500F, 0.124000F, -0.249000F, 0.938500F, 0.501000F},
            {-0.188500F, 1.936500F, 0.311500F, -0.186500F, 2.063500F, 0.688500F},
            {-0.126000F, 0.686500F, 0.499000F, -0.124000F, 0.813500F, 0.876000F},
            {-0.126000F, 1.124000F, 0.499000F, -0.124000F, 1.251000F, 0.876000F},
            {-0.126000F, 2.249000F, 0.436500F, -0.124000F, 2.376000F, 0.813500F},
            {-0.063500F, 1.499000F, 0.186500F, -0.061500F, 1.626000F, 0.563500F},
            {-0.062500F, 0.187500F, 0.750000F, 0.437500F, 1.937500F, 0.875000F},
            {-0.001000F, 0.936500F, 0.374000F, 0.001000F, 1.063500F, 0.751000F},
            {0.061500F, 1.311500F, 0.436500F, 0.063500F, 1.438500F, 0.813500F},
            {0.061500F, 1.624000F, 0.436500F, 0.063500F, 1.751000F, 0.813500F},
            {0.061500F, 1.749000F, 0.061500F, 0.063500F, 1.876000F, 0.438500F},
            {0.061500F, 2.061500F, 0.311500F, 0.063500F, 2.188500F, 0.688500F},
            {0.186500F, 0.874000F, 0.186500F, 0.188500F, 1.001000F, 0.563500F},
            {0.186500F, 1.249000F, 0.124000F, 0.188500F, 1.376000F, 0.501000F},
            {0.187500F, 1.812500F, 0.250000F, 0.437500F, 1.937500F, 0.500000F},
            {0.187500F, 1.818750F, 0.500000F, 0.437500F, 1.937500F, 0.750000F},
            {0.187500F, 1.937500F, 0.125000F, 0.312500F, 2.562500F, 0.500000F},
            {0.187500F, 1.937500F, 0.500000F, 0.312500F, 2.562500F, 0.750000F},
            {0.249000F, 0.686500F, 0.436500F, 0.251000F, 0.813500F, 0.813500F},
            {0.311500F, 1.186500F, 0.374000F, 0.313500F, 1.313500F, 0.751000F},
            {0.437500F, 0.187500F, 0.125000F, 0.562500F, 1.937500F, 0.500000F},
            {0.437500F, 0.187500F, 0.500000F, 0.562500F, 1.937500F, 0.875000F},
            {0.562500F, 0.187500F, 0.437500F, 0.687500F, 1.937500F, 0.562500F},
        });

        ModelTortureKeys = new BlockModels(Material.iron, "ModelTortureKeys", "instruments_of_torture/keys", "instruments_of_torture/keys");
        ModelTortureKeys.withMultipartCollisionBoxes(new float[][] {
            {-0.002541F, 0.376536F, 0.092750F, 0.219060F, 0.658598F, 0.094750F},
            {0.000000F, 0.187500F, 0.000000F, 1.000000F, 0.500000F, 0.062500F},
            {0.000000F, 0.562500F, 0.000000F, 1.000000F, 0.875000F, 0.062500F},
            {0.047798F, 0.604442F, 0.062500F, 0.296972F, 0.853616F, 0.125000F},
            {0.247459F, 0.376536F, 0.092750F, 0.469060F, 0.658598F, 0.094750F},
            {0.297798F, 0.604442F, 0.062500F, 0.546972F, 0.853616F, 0.125000F},
            {0.372459F, 0.001536F, 0.092750F, 0.594060F, 0.283598F, 0.094750F},
            {0.422798F, 0.229442F, 0.062500F, 0.671972F, 0.478616F, 0.125000F},
            {0.684959F, 0.376536F, 0.092750F, 0.906560F, 0.658598F, 0.094750F},
            {0.735298F, 0.604442F, 0.062500F, 0.984472F, 0.853616F, 0.125000F},
        });
        ModelTortureKeys.setCollisionEnabled(false);

        ModelTorturePileOfRottingCorpses = new BlockModels(Material.cloth, "ModelTorturePileOfRottingCorpses", "instruments_of_torture/pile_of_rotting_corpses", "instruments_of_torture/pile_of_rotting_corpses");
        ModelTorturePileOfRottingCorpses.withModelOffsetY(0.1875F);
        ModelTorturePileOfRottingCorpses.withMultipartCollisionBoxes(new float[][] {
            {0.089787F, 0.109308F, 0.514342F, 0.504492F, 0.388714F, 0.654967F},
            {0.089787F, 0.109308F, 0.633092F, 0.504492F, 0.388714F, 0.773717F},
            {0.134309F, 0.186500F, 0.321809F, 0.490691F, 0.688500F, 0.678191F},
            {0.242188F, 0.117188F, 0.242188F, 0.382812F, 0.257812F, 0.632812F},
            {0.259309F, 0.436500F, 0.071809F, 0.615691F, 0.938500F, 0.428191F},
            {0.297601F, 0.242188F, 0.259346F, 0.712306F, 0.382812F, 0.538752F},
            {0.316265F, 0.003765F, -0.015625F, 0.683735F, 0.371235F, 0.265625F},
            {0.345436F, 0.242188F, 0.374830F, 0.807977F, 0.382812F, 0.769722F},
            {0.365661F, 0.117188F, 0.569101F, 0.645067F, 0.257812F, 0.983806F},
            {0.367188F, 0.117188F, 0.242188F, 0.632812F, 0.257812F, 0.632812F},
            {0.384309F, 0.186500F, 0.696809F, 0.740691F, 0.688500F, 1.053191F},
            {0.441107F, 0.242188F, 0.605800F, 0.855812F, 0.382812F, 0.885207F},
            {0.475371F, 0.117188F, 0.523657F, 0.754778F, 0.257812F, 0.938362F},
            {0.617188F, 0.117188F, 0.242188F, 0.757812F, 0.257812F, 0.632812F},
            {0.634309F, 0.374000F, 0.384309F, 0.990691F, 0.876000F, 0.740691F},
            {0.690625F, 0.128765F, 0.285015F, 0.971875F, 0.496235F, 0.652485F},
        });
        ModelTorturePileOfRottingCorpses.setCollisionEnabled(false);
        ModelTorturePileOfRottingCorpses.withAnimatedTexture(6, 50);

        ModelTorturePlaceOfSleep = new BlockModels(Material.wood, "ModelTorturePlaceOfSleep", "instruments_of_torture/place_of_sleep", "instruments_of_torture/place_of_sleep");
        ModelTorturePlaceOfSleep.withMultipartCollisionBoxes(new float[][] {
            {-0.001000F, 0.000000F, 0.499000F, 1.001000F, 0.001000F, 2.001000F},
            {0.125000F, 0.000000F, 0.125000F, 0.875000F, 0.125000F, 0.500000F},
        });
        ModelTorturePlaceOfSleep.withSleepingNoRespawn(.125F,2,.5F,.3125F);

        ModelTorturePrisonWagon = new BlockModels(Material.wood, "ModelTorturePrisonWagon", "instruments_of_torture/prison_wagon", "instruments_of_torture/prison_wagon");
        ModelTorturePrisonWagon.withModelOffsetY(0.8750F);
        ModelTorturePrisonWagon.withMultipartCollisionBoxes(new float[][] {
            {-0.375000F, 0.000000F, -0.375000F, -0.250000F, 1.000000F, 0.375000F},
            {-0.375000F, 0.000000F, 1.125000F, -0.250000F, 1.000000F, 1.875000F},
            {-0.375000F, 0.125000F, -0.500000F, -0.250000F, 0.875000F, -0.375000F},
            {-0.375000F, 0.125000F, 0.375000F, -0.250000F, 0.875000F, 0.500000F},
            {-0.375000F, 0.125000F, 1.000000F, -0.250000F, 0.875000F, 1.125000F},
            {-0.375000F, 0.125000F, 1.875000F, -0.250000F, 0.875000F, 2.000000F},
            {-0.375000F, 2.250000F, -0.375000F, 1.375000F, 2.500000F, 1.875000F},
            {-0.250000F, 0.375000F, -0.250000F, 1.250000F, 0.750000F, 0.250000F},
            {-0.250000F, 0.375000F, 1.250000F, 1.250000F, 0.750000F, 1.750000F},
            {-0.250000F, 0.500000F, -0.875000F, 0.000000F, 0.750000F, 1.437500F},
            {-0.250000F, 0.750000F, -0.625000F, 1.250000F, 0.875000F, -0.250000F},
            {-0.250000F, 0.750000F, -0.250000F, 1.250000F, 0.875000F, 1.750000F},
            {-0.250000F, 0.750000F, 1.750000F, 1.250000F, 0.875000F, 2.000000F},
            {-0.250000F, 0.875000F, -0.250000F, -0.125000F, 1.625000F, 1.750000F},
            {-0.250000F, 1.625000F, -0.250000F, -0.125000F, 2.250000F, 0.750000F},
            {-0.250000F, 1.625000F, 0.750000F, -0.125000F, 1.750000F, 1.625000F},
            {-0.250000F, 1.625000F, 1.625000F, -0.125000F, 2.250000F, 1.750000F},
            {-0.250000F, 1.750000F, 0.750000F, -0.125000F, 2.250000F, 0.875000F},
            {-0.250000F, 1.750000F, 1.500000F, -0.125000F, 2.250000F, 1.625000F},
            {-0.218750F, 1.750000F, 0.968750F, -0.156250F, 2.250000F, 1.031250F},
            {-0.218750F, 1.750000F, 1.156250F, -0.156250F, 2.250000F, 1.218750F},
            {-0.218750F, 1.750000F, 1.343750F, -0.156250F, 2.250000F, 1.406250F},
            {-0.187500F, 0.500000F, -1.000000F, -0.062500F, 0.750000F, -0.875000F},
            {-0.125000F, 0.875000F, -0.250000F, 1.125000F, 1.625000F, -0.125000F},
            {-0.125000F, 0.875000F, 1.625000F, 0.000000F, 2.250000F, 1.750000F},
            {-0.125000F, 1.625000F, -0.250000F, 0.062500F, 2.250000F, -0.125000F},
            {-0.062500F, 1.375000F, 1.750000F, 0.062500F, 1.625000F, 1.812500F},
            {0.000000F, 0.500000F, -0.625000F, 1.000000F, 0.750000F, -0.187500F},
            {0.000000F, 0.875000F, 1.625000F, 1.000000F, 1.625000F, 1.750000F},
            {0.000000F, 1.625000F, 1.625000F, 1.000000F, 1.750000F, 1.750000F},
            {0.000000F, 1.750000F, 1.625000F, 0.187500F, 2.250000F, 1.750000F},
            {0.062500F, 1.625000F, -0.250000F, 0.937500F, 1.750000F, -0.125000F},
            {0.062500F, 1.750000F, -0.250000F, 0.187500F, 2.250000F, -0.125000F},
            {0.156250F, 1.875000F, 1.656250F, 0.281250F, 1.937500F, 1.718750F},
            {0.156250F, 2.062500F, 1.656250F, 0.281250F, 2.125000F, 1.718750F},
            {0.281250F, 1.750000F, -0.218750F, 0.343750F, 2.250000F, -0.156250F},
            {0.281250F, 1.750000F, 1.656250F, 0.343750F, 2.250000F, 1.718750F},
            {0.343750F, 1.875000F, 1.656250F, 0.468750F, 1.937500F, 1.718750F},
            {0.343750F, 2.062500F, 1.656250F, 0.468750F, 2.125000F, 1.718750F},
            {0.468750F, 1.750000F, -0.218750F, 0.531250F, 2.250000F, -0.156250F},
            {0.468750F, 1.750000F, 1.656250F, 0.531250F, 2.250000F, 1.718750F},
            {0.531250F, 1.875000F, 1.656250F, 0.656250F, 1.937500F, 1.718750F},
            {0.531250F, 2.062500F, 1.656250F, 0.656250F, 2.125000F, 1.718750F},
            {0.656250F, 1.750000F, -0.218750F, 0.718750F, 2.250000F, -0.156250F},
            {0.656250F, 1.750000F, 1.656250F, 0.718750F, 2.250000F, 1.718750F},
            {0.718750F, 1.875000F, 1.656250F, 0.843750F, 1.937500F, 1.718750F},
            {0.718750F, 2.062500F, 1.656250F, 0.843750F, 2.125000F, 1.718750F},
            {0.812500F, 1.750000F, -0.250000F, 0.937500F, 2.250000F, -0.125000F},
            {0.812500F, 1.750000F, 1.625000F, 1.000000F, 2.250000F, 1.750000F},
            {0.937500F, 1.625000F, -0.250000F, 1.125000F, 2.250000F, -0.125000F},
            {1.000000F, 0.500000F, -0.875000F, 1.250000F, 0.750000F, 1.437500F},
            {1.000000F, 0.875000F, 1.625000F, 1.125000F, 2.250000F, 1.750000F},
            {1.062500F, 0.500000F, -1.000000F, 1.187500F, 0.750000F, -0.875000F},
            {1.125000F, 0.875000F, -0.250000F, 1.250000F, 1.625000F, 1.750000F},
            {1.125000F, 1.625000F, -0.250000F, 1.250000F, 2.250000F, 0.750000F},
            {1.125000F, 1.625000F, 0.750000F, 1.250000F, 1.750000F, 1.625000F},
            {1.125000F, 1.625000F, 1.625000F, 1.250000F, 2.250000F, 1.750000F},
            {1.125000F, 1.750000F, 0.750000F, 1.250000F, 2.250000F, 0.875000F},
            {1.125000F, 1.750000F, 1.500000F, 1.250000F, 2.250000F, 1.625000F},
            {1.156250F, 1.750000F, 0.968750F, 1.218750F, 2.250000F, 1.031250F},
            {1.156250F, 1.750000F, 1.156250F, 1.218750F, 2.250000F, 1.218750F},
            {1.156250F, 1.750000F, 1.343750F, 1.218750F, 2.250000F, 1.406250F},
            {1.250000F, 0.000000F, -0.375000F, 1.375000F, 1.000000F, 0.375000F},
            {1.250000F, 0.000000F, 1.125000F, 1.375000F, 1.000000F, 1.875000F},
            {1.250000F, 0.125000F, -0.500000F, 1.375000F, 0.875000F, -0.375000F},
            {1.250000F, 0.125000F, 0.375000F, 1.375000F, 0.875000F, 0.500000F},
            {1.250000F, 0.125000F, 1.000000F, 1.375000F, 0.875000F, 1.125000F},
            {1.250000F, 0.125000F, 1.875000F, 1.375000F, 0.875000F, 2.000000F},
        });

        ModelTortureRope = new BlockConnectedRope("ModelTortureRope", "instruments_of_torture/rope");

        ModelTortureRope1 = new BlockConnectedRope("ModelTortureRope1", "instruments_of_torture/rope_1");

        ModelTortureRope2 = new BlockConnectedRope("ModelTortureRope2", "instruments_of_torture/rope_2");

        ModelTortureRope3 = new BlockConnectedRope("ModelTortureRope3", "instruments_of_torture/rope_3");

        ModelTortureRope4 = new BlockConnectedRope("ModelTortureRope4", "instruments_of_torture/rope_4");

        ModelTortureRope5 = new BlockConnectedRope("ModelTortureRope5", "instruments_of_torture/rope_5");

        ModelTortureRopeAroundTheNeck = new BlockModels(Material.cloth, "ModelTortureRopeAroundTheNeck", "instruments_of_torture/rope_around_the_neck", "instruments_of_torture/rope_around_the_neck");
        ModelTortureRopeAroundTheNeck.withRotatingBounds(0.2500F, 0.2500F, 0.3750F, 0.7500F, 1.0000F, 0.6250F);
        ModelTortureRopeAroundTheNeck.setCollisionEnabled(true);

        ModelTortureRopeCore = new BlockModels(Material.cloth, "ModelTortureRopeCore", "instruments_of_torture/rope_core", "instruments_of_torture/rope_core");
        ModelTortureRopeCore.withMultipartCollisionBoxes(new float[][] {
            {0.12500000F, 0.00000000F, 0.25000000F, 0.87500000F, 0.50000000F, 0.37500000F},
            {0.62500000F, 0.18750000F, 0.18750000F, 0.75000000F, 0.56250000F, 0.31250000F},
            {0.12500000F, 0.00000000F, 0.81250000F, 0.87500000F, 0.50000000F, 0.93750000F},
            {0.75000000F, 0.00000000F, 0.37500000F, 0.87500000F, 0.50000000F, 0.81250000F},
            {0.12500000F, 0.00000000F, 0.37500000F, 0.25000000F, 0.50000000F, 0.81250000F},
        });
        ModelTortureRopeCore.setCollisionEnabled(true);

        ModelTortureSharpenedInThePads = new BlockModels(Material.wood, "ModelTortureSharpenedInThePads", "instruments_of_torture/sharpened_in_the_pads", "instruments_of_torture/sharpened_in_the_pads");
        ModelTortureSharpenedInThePads.withMultipartCollisionBoxes(new float[][] {
            {-0.062500F, 1.375000F, 0.389343F, 0.062500F, 1.625000F, 0.514343F},
            {0.000000F, 1.121064F, 0.235734F, 0.062500F, 1.366044F, 0.480715F},
            {0.030250F, 1.292985F, 0.306181F, 0.032250F, 1.540578F, 0.553774F},
            {0.062500F, 0.000000F, -0.048157F, 0.312500F, 0.250000F, 0.951843F},
            {0.062500F, 0.937500F, 0.326843F, 1.312500F, 1.062500F, 0.576843F},
            {0.062500F, 1.062500F, 0.326843F, 1.312500F, 1.312500F, 0.576843F},
            {0.062500F, 1.312500F, 0.326843F, 1.312500F, 1.562500F, 0.576843F},
            {0.062500F, 1.562500F, 0.326843F, 1.312500F, 1.687500F, 0.576843F},
            {0.187500F, 0.062500F, 0.326843F, 1.187500F, 0.187500F, 0.576843F},
            {0.562500F, 0.187500F, 0.326843F, 0.812500F, 0.937500F, 0.576843F},
            {1.062500F, 0.000000F, -0.048157F, 1.312500F, 0.250000F, 0.951843F},
        });

        ModelTortureStoneGrave = new BlockModels(Material.rock, "ModelTortureStoneGrave", "instruments_of_torture/stone_grave", "instruments_of_torture/stone_grave");
        ModelTortureStoneGrave.withMultipartCollisionBoxes(new float[][] {
            {-0.250000F, 0.000000F, -0.250000F, 1.250000F, 0.187500F, 0.000000F},
            {-0.250000F, 0.000000F, 0.000000F, 0.000000F, 0.187500F, 1.750000F},
            {-0.250000F, 0.000000F, 1.750000F, 1.250000F, 0.187500F, 2.000000F},
            {0.000000F, 0.000000F, 0.000000F, 1.000000F, 0.125000F, 1.750000F},
            {1.000000F, 0.000000F, 0.000000F, 1.250000F, 0.187500F, 1.750000F},
        });

        ModelTortureStretchingTable = new BlockModels(Material.wood, "ModelTortureStretchingTable", "instruments_of_torture/stretching_table", "instruments_of_torture/stretching_table");
        ModelTortureStretchingTable.withMultipartCollisionBoxes(new float[][] {
            {-0.200000F, 0.498407F, -0.907843F, -0.112500F, 0.754733F, -0.651517F},
            {-0.200000F, 0.498407F, -0.598483F, -0.112500F, 0.754733F, -0.342157F},
            {-0.200000F, 0.807767F, -0.907843F, -0.112500F, 1.064093F, -0.651517F},
            {-0.200000F, 0.807767F, -0.598483F, -0.112500F, 1.064093F, -0.342157F},
            {-0.187500F, 0.648667F, -0.757583F, -0.125000F, 0.913833F, -0.492417F},
            {-0.125000F, 0.000000F, -0.750000F, 0.000000F, 1.000000F, -0.500000F},
            {-0.125000F, 0.000000F, 1.562500F, 0.000000F, 0.687500F, 1.812500F},
            {-0.125000F, 0.375000F, -0.750000F, 1.125000F, 0.562500F, 1.812500F},
            {-0.045483F, 0.550000F, -0.131660F, 0.313822F, 0.700000F, 0.227645F},
            {0.000000F, 0.625000F, -0.781250F, 1.000000F, 0.937500F, -0.468750F},
            {0.050000F, 0.612500F, -0.793750F, 0.950000F, 0.950000F, -0.456250F},
            {0.050000F, 0.612500F, -0.481250F, 0.137500F, 0.637500F, -0.081250F},
            {0.061500F, 0.624000F, -0.501000F, 0.938500F, 0.626000F, 0.001000F},
            {0.237500F, 0.550000F, 1.362500F, 0.762500F, 0.825000F, 1.575000F},
            {0.726598F, 0.550000F, -0.273402F, 1.085902F, 0.700000F, 0.085902F},
            {0.862500F, 0.612500F, -0.481250F, 0.950000F, 0.637500F, -0.206250F},
            {1.000000F, 0.000000F, -0.750000F, 1.125000F, 1.000000F, -0.500000F},
            {1.000000F, 0.000000F, 1.562500F, 1.125000F, 0.687500F, 1.812500F},
            {1.112500F, 0.498407F, -0.907843F, 1.200000F, 0.754733F, -0.651517F},
            {1.112500F, 0.498407F, -0.598483F, 1.200000F, 0.754733F, -0.342157F},
            {1.112500F, 0.807767F, -0.907843F, 1.200000F, 1.064093F, -0.651517F},
            {1.112500F, 0.807767F, -0.598483F, 1.200000F, 1.064093F, -0.342157F},
            {1.125000F, 0.648667F, -0.757583F, 1.187500F, 0.913833F, -0.492417F},
        });

        ModelTortureSuspendedCorpse = new BlockModels(Material.cloth, "ModelTortureSuspendedCorpse", "instruments_of_torture/suspended_corpse", "instruments_of_torture/suspended_corpse");
        ModelTortureSuspendedCorpse.withModelOffsetY(0.1875F);
        ModelTortureSuspendedCorpse.withMultipartCollisionBoxes(new float[][] {
            {0.25000000F, 1.33984375F, 0.20507812F, 0.75000000F, 1.99609375F, 0.85742188F},
            {0.25000000F, 0.75000000F, 0.43945312F, 0.75000000F, 1.50000000F, 0.68945312F},
            {0.49218750F, 1.35546875F, 0.43945312F, 1.01171875F, 2.00000000F, 0.68945312F},
            {0.49218750F, 0.00000000F, 0.43945312F, 0.74218750F, 0.75000000F, 0.68945312F},
            {0.25781250F, 0.00000000F, 0.43945312F, 0.50781250F, 0.75000000F, 0.68945312F},
            {0.24218750F, 0.00000000F, 0.43945312F, 0.49218750F, 0.75000000F, 0.68945312F},
            {0.12500000F, 1.87500000F, 0.37695312F, 0.87500000F, 2.00000000F, 0.75195312F},
            {-0.01171875F, 1.35546875F, 0.43945312F, 0.50781250F, 2.00000000F, 0.68945312F},
        });
        ModelTortureSuspendedCorpse.setCollisionEnabled(false);

        ModelTortureToolsForOversupply = new BlockModels(Material.iron, "ModelTortureToolsForOversupply", "instruments_of_torture/tools_for_oversupply", "instruments_of_torture/tools_for_oversupply");
        ModelTortureToolsForOversupply.withMultipartCollisionBoxes(new float[][] {
            {-0.001000F, 0.000000F, 0.061500F, 1.001000F, 0.001000F, 0.938500F},
            {0.193330F, 0.036500F, 0.138097F, 0.464841F, 0.038500F, 0.443433F},
            {0.224357F, 0.006250F, 0.211157F, 0.521277F, 0.068750F, 0.812499F},
            {0.513770F, 0.006250F, 0.578223F, 0.667183F, 0.068750F, 0.833111F},
            {0.526474F, 0.036500F, 0.206544F, 0.869738F, 0.038500F, 0.685107F},
            {0.527780F, 0.006250F, 0.554305F, 0.609440F, 0.068750F, 0.635965F},
            {0.643265F, 0.006250F, 0.602141F, 0.724925F, 0.068750F, 0.683801F},
        });
        ModelTortureToolsForOversupply.setCollisionEnabled(false);

        ModelTortureToolsForOversupply1 = new BlockModels(Material.iron, "ModelTortureToolsForOversupply1", "instruments_of_torture/tools_for_oversupply_1", "instruments_of_torture/tools_for_oversupply_1");
        ModelTortureToolsForOversupply1.withMultipartCollisionBoxes(new float[][] {
            {-0.001000F, 0.000000F, 0.061500F, 1.001000F, 0.001000F, 0.938500F},
            {0.125000F, 0.006250F, 0.187500F, 0.187500F, 0.068750F, 0.437500F},
            {0.125000F, 0.006250F, 0.562500F, 0.187500F, 0.068750F, 0.812500F},
            {0.187500F, 0.006250F, 0.187500F, 0.250000F, 0.068750F, 0.250000F},
            {0.187500F, 0.006250F, 0.375000F, 0.250000F, 0.068750F, 0.437500F},
            {0.187500F, 0.006250F, 0.562500F, 0.250000F, 0.068750F, 0.625000F},
            {0.187500F, 0.006250F, 0.750000F, 0.250000F, 0.068750F, 0.812500F},
            {0.250000F, 0.006250F, 0.187500F, 0.312500F, 0.068750F, 0.437500F},
            {0.250000F, 0.006250F, 0.562500F, 0.312500F, 0.068750F, 0.812500F},
            {0.311500F, 0.036500F, 0.186500F, 0.876000F, 0.038500F, 0.438500F},
            {0.311500F, 0.036500F, 0.561500F, 0.876000F, 0.038500F, 0.813500F},
        });
        ModelTortureToolsForOversupply1.setCollisionEnabled(false);

        ModelTortureToolsForOversupply2 = new BlockModels(Material.iron, "ModelTortureToolsForOversupply2", "instruments_of_torture/tools_for_oversupply_2", "instruments_of_torture/tools_for_oversupply_2");
        ModelTortureToolsForOversupply2.withMultipartCollisionBoxes(new float[][] {
            {-0.001000F, 0.000000F, 0.061500F, 1.001000F, 0.001000F, 0.938500F},
            {0.155250F, 0.049000F, 0.186500F, 0.469750F, 0.051000F, 0.438500F},
            {0.187500F, 0.018750F, 0.500000F, 0.250000F, 0.081250F, 0.812500F},
            {0.206250F, 0.006250F, 0.362500F, 0.418750F, 0.093750F, 0.575000F},
            {0.375000F, 0.018750F, 0.500000F, 0.437500F, 0.081250F, 0.812500F},
            {0.562814F, 0.012500F, 0.215616F, 0.759959F, 0.075000F, 0.345112F},
            {0.586732F, 0.012500F, 0.321194F, 0.668392F, 0.075000F, 0.402855F},
            {0.610649F, 0.012500F, 0.331101F, 0.807794F, 0.075000F, 0.460597F},
            {0.625000F, 0.012500F, 0.437500F, 0.687500F, 0.075000F, 0.812500F},
            {0.663582F, 0.012500F, 0.418340F, 0.864831F, 0.075000F, 0.788712F},
        });
        ModelTortureToolsForOversupply2.setCollisionEnabled(false);

        ModelTortureToolsForOversupply3 = new BlockModels(Material.iron, "ModelTortureToolsForOversupply3", "instruments_of_torture/tools_for_oversupply_3", "instruments_of_torture/tools_for_oversupply_3");
        ModelTortureToolsForOversupply3.withMultipartCollisionBoxes(new float[][] {
            {-0.001000F, 0.000000F, 0.061500F, 1.001000F, 0.001000F, 0.938500F},
            {0.160227F, 0.006250F, 0.189848F, 0.727744F, 0.131250F, 0.520593F},
            {0.194051F, 0.037500F, 0.299530F, 0.467053F, 0.100000F, 0.843130F},
            {0.279815F, 0.006250F, 0.478561F, 0.847333F, 0.131250F, 0.809305F},
            {0.540506F, 0.037500F, 0.156024F, 0.813508F, 0.100000F, 0.699623F},
        });
        ModelTortureToolsForOversupply3.setCollisionEnabled(false);

        ModelTortureToolsForOversupply4 = new BlockModels(Material.iron, "ModelTortureToolsForOversupply4", "instruments_of_torture/tools_for_oversupply_4", "instruments_of_torture/tools_for_oversupply_4");
        ModelTortureToolsForOversupply4.withMultipartCollisionBoxes(new float[][] {
            {-0.001000F, 0.000000F, 0.061500F, 1.001000F, 0.001000F, 0.938500F},
            {0.177855F, 0.006250F, 0.240747F, 0.432742F, 0.068750F, 0.394160F},
            {0.177855F, 0.006250F, 0.355840F, 0.432742F, 0.068750F, 0.509253F},
            {0.177855F, 0.049590F, 0.343750F, 0.432742F, 0.203003F, 0.406250F},
            {0.186500F, 0.036500F, 0.280250F, 0.376000F, 0.038500F, 0.469750F},
            {0.186500F, 0.036500F, 0.374000F, 0.376000F, 0.163500F, 0.376000F},
            {0.360465F, 0.006250F, 0.616647F, 0.557610F, 0.068750F, 0.746143F},
            {0.375000F, 0.006250F, 0.312500F, 0.500000F, 0.131250F, 0.437500F},
            {0.500000F, 0.037500F, 0.343750F, 0.562500F, 0.100000F, 0.406250F},
            {0.521733F, -0.025000F, 0.539941F, 0.685054F, 0.100000F, 0.703261F},
            {0.562500F, 0.006250F, 0.312500F, 0.625000F, 0.131250F, 0.437500F},
            {0.625259F, -0.056250F, 0.439316F, 0.870240F, 0.131250F, 0.684297F},
        });
        ModelTortureToolsForOversupply4.setCollisionEnabled(false);

        ModelTortureToolsForOversupply5 = new BlockModels(Material.iron, "ModelTortureToolsForOversupply5", "instruments_of_torture/tools_for_oversupply_5", "instruments_of_torture/tools_for_oversupply_5");
        ModelTortureToolsForOversupply5.withMultipartCollisionBoxes(new float[][] {
            {-0.001000F, 0.000000F, 0.061500F, 1.001000F, 0.001000F, 0.938500F},
            {0.205757F, 0.006250F, 0.285022F, 0.335253F, 0.068750F, 0.482167F},
            {0.277511F, 0.006250F, 0.410414F, 0.474656F, 0.068750F, 0.539909F},
            {0.321242F, 0.006250F, 0.237186F, 0.450738F, 0.068750F, 0.434331F},
            {0.349264F, 0.006250F, 0.583641F, 0.594244F, 0.068750F, 0.828622F},
            {0.359171F, 0.006250F, 0.492074F, 0.464749F, 0.068750F, 0.631476F},
            {0.487226F, -0.042192F, 0.162215F, 0.684371F, 0.145308F, 0.291710F},
            {0.568886F, 0.020308F, 0.243875F, 0.841888F, 0.082808F, 0.787475F},
            {0.672142F, 0.007808F, 0.516255F, 0.858220F, 0.095308F, 0.803807F},
        });
        ModelTortureToolsForOversupply5.setCollisionEnabled(false);

        ModelTortureTortureTable = new BlockModels(Material.wood, "ModelTortureTortureTable", "instruments_of_torture/torture_table", "instruments_of_torture/torture_table");
        ModelTortureTortureTable.withMultipartCollisionBoxes(new float[][] {
            {-0.625000F, 0.375000F, 0.062500F, 0.125000F, 0.562500F, 0.562500F},
            {-0.500000F, 0.562500F, 0.187500F, 0.250000F, 0.812500F, 0.437500F},
            {-0.387500F, 0.550000F, 0.112500F, -0.175000F, 0.825000F, 0.512500F},
            {-0.375000F, 0.000000F, 0.187500F, -0.187500F, 0.375000F, 0.437500F},
            {0.125000F, 0.375000F, -0.437500F, 0.875000F, 0.562500F, 1.812500F},
            {0.237500F, 0.550000F, 1.425000F, 0.762500F, 0.825000F, 1.637500F},
            {0.250000F, 0.000000F, -0.312500F, 0.750000F, 0.375000F, -0.125000F},
            {0.250000F, 0.000000F, 1.437500F, 0.750000F, 0.375000F, 1.625000F},
            {0.250000F, 0.377772F, -0.348311F, 0.750000F, 1.031053F, 0.304971F},
            {0.250000F, 0.562500F, 0.187500F, 0.750000F, 0.812500F, 0.937500F},
            {0.250000F, 0.562500F, 0.937500F, 0.500000F, 0.812500F, 1.687500F},
            {0.500000F, 0.562500F, 0.937500F, 0.750000F, 0.812500F, 1.687500F},
            {0.750000F, 0.562500F, 0.187500F, 1.500000F, 0.812500F, 0.437500F},
            {0.875000F, 0.375000F, 0.062500F, 1.625000F, 0.562500F, 0.562500F},
            {1.175000F, 0.550000F, 0.112500F, 1.387500F, 0.825000F, 0.512500F},
            {1.187500F, 0.000000F, 0.187500F, 1.375000F, 0.375000F, 0.437500F},
        });

        ModelTortureTortureTable1 = new BlockModels(Material.wood, "ModelTortureTortureTable1", "instruments_of_torture/torture_table_1", "instruments_of_torture/torture_table_1");
        ModelTortureTortureTable1.withMultipartCollisionBoxes(new float[][] {
            {0.000000F, 0.000000F, -0.875000F, 0.250000F, 0.375000F, -0.625000F},
            {0.000000F, 0.000000F, 0.875000F, 0.250000F, 0.375000F, 1.125000F},
            {0.000000F, 0.375000F, -1.000000F, 1.000000F, 0.562500F, 1.250000F},
            {0.000000F, 0.562500F, -0.375000F, 0.250000F, 0.812500F, 0.375000F},
            {0.062500F, 0.125000F, -0.625000F, 0.125000F, 0.250000F, 0.875000F},
            {0.250000F, 0.125000F, -0.812500F, 0.750000F, 0.250000F, -0.750000F},
            {0.250000F, 0.125000F, 1.000000F, 0.750000F, 0.250000F, 1.062500F},
            {0.250000F, 0.437500F, -0.875000F, 0.750000F, 0.937500F, -0.375000F},
            {0.250000F, 0.562500F, -0.375000F, 0.750000F, 0.812500F, 0.375000F},
            {0.750000F, 0.000000F, -0.875000F, 1.000000F, 0.375000F, -0.625000F},
            {0.750000F, 0.000000F, 0.875000F, 1.000000F, 0.375000F, 1.125000F},
            {0.750000F, 0.562500F, -0.375000F, 1.000000F, 0.812500F, 0.375000F},
            {0.875000F, 0.125000F, -0.625000F, 0.937500F, 0.250000F, 0.875000F},
        });

        ModelTortureTortureWithCoals = new BlockModels(Material.iron, "ModelTortureTortureWithCoals", "instruments_of_torture/torture_with_coals", "instruments_of_torture/torture_with_coals");
        ModelTortureTortureWithCoals.withMultipartCollisionBoxes(new float[][] {
            {0.125000F, 0.000000F, 0.125000F, 0.875000F, 0.250000F, 0.875000F},
            {0.125000F, 0.500000F, 0.125000F, 0.875000F, 0.875000F, 0.875000F},
            {0.125000F, 0.875000F, 0.125000F, 0.250000F, 1.000000F, 0.875000F},
            {0.250000F, 0.250000F, 0.250000F, 0.750000F, 0.500000F, 0.750000F},
            {0.250000F, 0.875000F, 0.125000F, 0.750000F, 1.000000F, 0.250000F},
            {0.250000F, 0.875000F, 0.750000F, 0.750000F, 1.000000F, 0.875000F},
            {0.296875F, 0.819503F, 0.371035F, 0.453125F, 1.023654F, 0.575185F},
            {0.476115F, 0.804077F, 0.546875F, 0.680266F, 1.008227F, 0.703125F},
            {0.549234F, 0.771908F, 0.296875F, 0.753385F, 0.976059F, 0.453125F},
            {0.750000F, 0.875000F, 0.125000F, 0.875000F, 1.000000F, 0.875000F},
            {0.859257F, 0.681140F, 0.206250F, 0.997499F, 0.853207F, 0.418750F},
            {0.907092F, 0.565655F, 0.143750F, 1.045334F, 0.737722F, 0.481250F},
            {0.971260F, 0.466502F, 0.187500F, 1.076838F, 0.605905F, 0.437500F},
            {1.019095F, 0.004562F, 0.187500F, 1.268179F, 0.490420F, 0.250000F},
            {1.019095F, 0.004562F, 0.375000F, 1.268179F, 0.490420F, 0.437500F},
        });

        ModelTortureWheeledCorpse = new BlockModels(Material.cloth, "ModelTortureWheeledCorpse", "instruments_of_torture/wheeled_corpse", "instruments_of_torture/wheeled_corpse");
        ModelTortureWheeledCorpse.withModelOffsetY(0.3750F);
        ModelTortureWheeledCorpse.withMultipartCollisionBoxes(new float[][] {
            {-0.187500F, 0.375000F, -0.062500F, 1.312500F, 0.500000F, 1.187500F},
            {-0.062500F, 0.375000F, -0.187500F, 1.187500F, 0.500000F, -0.062500F},
            {-0.062500F, 0.375000F, 1.187500F, 1.187500F, 0.500000F, 1.312500F},
            {-0.060660F, 0.501350F, 0.025402F, 0.646447F, 0.751350F, 0.732509F},
            {0.202035F, 0.654199F, 0.343750F, 0.855316F, 1.307481F, 0.843750F},
            {0.437500F, 0.500000F, 0.437500F, 0.687500F, 0.875000F, 0.687500F},
            {0.558058F, 0.501350F, -0.062986F, 1.441942F, 0.751350F, 0.820897F},
            {0.911612F, 0.044243F, -0.593316F, 1.161612F, 0.751350F, 0.113790F},
            {1.088388F, 0.001350F, 0.644120F, 1.441942F, 0.751350F, 0.997674F},
            {1.265165F, 0.063850F, 0.394120F, 1.515165F, 0.813850F, 0.644120F},
        });
        ModelTortureWheeledCorpse.setCollisionEnabled(false);

        ModelTortureWheeling = new BlockModels(Material.wood, "ModelTortureWheeling", "instruments_of_torture/wheeling", "instruments_of_torture/wheeling");
        ModelTortureWheeling.withModelOffsetY(1.0000F);
        ModelTortureWheeling.withMultipartCollisionBoxes(new float[][] {
            {0.000000F, 0.000000F, 0.875000F, 0.500000F, 0.250000F, 1.125000F},
            {0.124000F, 0.061500F, 0.999000F, 0.626000F, 0.626000F, 1.001000F},
            {0.437500F, 1.250000F, 0.937500F, 1.500000F, 1.375000F, 1.062500F},
            {0.499000F, 1.374000F, 0.936500F, 0.751000F, 1.501000F, 0.938500F},
            {0.499000F, 1.374000F, 1.061500F, 0.751000F, 1.501000F, 1.063500F},
            {0.500000F, 0.000000F, 0.125000F, 0.750000F, 0.250000F, 1.875000F},
            {0.500000F, 0.250000F, 0.812500F, 0.750000F, 1.500000F, 1.187500F},
            {0.548843F, 1.521788F, 1.108161F, 1.450487F, 1.742187F, 1.455834F},
            {0.624000F, 0.061500F, 0.249000F, 0.626000F, 0.626000F, 1.751000F},
            {0.625000F, 1.386409F, 1.093670F, 0.875000F, 2.174990F, 1.611652F},
            {0.737500F, 1.164963F, 1.501249F, 1.262500F, 1.252463F, 1.963749F},
            {0.737500F, 1.575040F, 1.478192F, 1.262500F, 1.832871F, 1.938971F},
            {0.737500F, 2.022577F, 1.276943F, 1.262500F, 2.280408F, 1.737722F},
            {0.750000F, 0.000000F, 0.250000F, 1.250000F, 0.250000F, 1.750000F},
            {0.750000F, 0.802463F, 1.701249F, 1.000000F, 1.552463F, 1.951249F},
            {0.750000F, 1.456792F, 1.433266F, 1.250000F, 2.245373F, 1.951249F},
            {0.750000F, 1.996226F, 1.020799F, 1.250000F, 2.703332F, 1.727906F},
            {0.798097F, 0.187500F, 0.817258F, 0.961418F, 0.312500F, 0.980578F},
            {0.831922F, 0.187500F, 1.110597F, 0.995242F, 0.312500F, 1.273918F},
            {0.875000F, 0.187500F, 0.875000F, 1.000000F, 0.312500F, 1.000000F},
            {0.875000F, 0.380413F, 0.135563F, 1.000000F, 2.244587F, 1.864437F},
            {0.875000F, 0.448063F, 0.067913F, 1.000000F, 1.650748F, 0.661752F},
            {0.875000F, 0.974252F, 1.338248F, 1.000000F, 2.176937F, 1.932087F},
            {0.995242F, 0.178247F, 0.937500F, 1.158563F, 0.341567F, 1.062500F},
            {1.000000F, 0.187500F, 1.312500F, 1.125000F, 0.312500F, 1.437500F},
            {1.000000F, 0.380413F, 0.135563F, 1.125000F, 2.244587F, 1.864437F},
            {1.000000F, 0.448063F, 0.067913F, 1.125000F, 1.650748F, 0.661752F},
            {1.000000F, 0.802463F, 1.701249F, 1.250000F, 1.552463F, 1.951249F},
            {1.000000F, 0.974252F, 1.338248F, 1.125000F, 2.176937F, 1.932087F},
            {1.125000F, 1.386409F, 1.093670F, 1.375000F, 2.174990F, 1.611652F},
            {1.249000F, 1.374000F, 0.936500F, 1.501000F, 1.501000F, 0.938500F},
            {1.249000F, 1.374000F, 1.061500F, 1.501000F, 1.501000F, 1.063500F},
            {1.250000F, 0.000000F, 0.125000F, 1.500000F, 0.250000F, 1.875000F},
            {1.250000F, 0.250000F, 0.812500F, 1.500000F, 1.500000F, 1.187500F},
            {1.374000F, 0.061500F, 0.999000F, 1.876000F, 0.626000F, 1.001000F},
            {1.374000F, 0.249000F, 0.249000F, 1.376000F, 0.813500F, 1.751000F},
            {1.500000F, 0.000000F, 0.875000F, 2.000000F, 0.250000F, 1.125000F},
            {1.500000F, 0.937500F, 0.937500F, 1.562500F, 1.375000F, 1.062500F},
            {1.537500F, 0.975000F, 0.943750F, 1.962500F, 1.087500F, 1.056250F},
        });

        ModelTortureWitchSChair = new BlockModels(Material.wood, "ModelTortureWitchSChair", "instruments_of_torture/witch_s_chair", "instruments_of_torture/witch_s_chair");
        ModelTortureWitchSChair.withMultipartCollisionBoxes(new float[][] {
            {0.000000F, 0.000000F, 0.000000F, 1.000000F, 0.125000F, 1.000000F},
            {0.000000F, 0.812500F, 0.187500F, 0.250000F, 1.062500F, 0.312500F},
            {0.055626F, 0.314814F, 0.000000F, 0.209039F, 0.569701F, 0.062500F},
            {0.062500F, 0.125000F, 0.187500F, 0.187500F, 0.375000F, 0.375000F},
            {0.062500F, 0.125000F, 0.750000F, 0.187500F, 0.375000F, 0.937500F},
            {0.062500F, 0.375000F, 0.062500F, 0.937500F, 0.625000F, 0.187500F},
            {0.062500F, 0.375000F, 0.187500F, 0.937500F, 0.625000F, 0.937500F},
            {0.062500F, 0.625000F, 0.187500F, 0.187500F, 0.875000F, 0.937500F},
            {0.062500F, 0.760005F, 0.590182F, 0.937500F, 1.018997F, 0.984473F},
            {0.062500F, 0.875000F, 0.812500F, 0.187500F, 1.562500F, 0.937500F},
            {0.124000F, 0.374000F, 0.186500F, 0.251000F, 0.376000F, 0.313500F},
            {0.186500F, 0.624000F, 0.186500F, 0.813500F, 0.751000F, 0.188500F},
            {0.186500F, 0.624000F, 0.311500F, 0.813500F, 0.751000F, 0.313500F},
            {0.186500F, 0.624000F, 0.436500F, 0.813500F, 0.751000F, 0.438500F},
            {0.186500F, 0.624000F, 0.561500F, 0.813500F, 0.751000F, 0.563500F},
            {0.186500F, 0.624000F, 0.686500F, 0.813500F, 0.751000F, 0.688500F},
            {0.186500F, 0.724776F, 0.690951F, 0.813500F, 0.775224F, 0.809049F},
            {0.186500F, 0.974776F, 0.690951F, 0.813500F, 1.025224F, 0.809049F},
            {0.186500F, 1.099776F, 0.690951F, 0.813500F, 1.150224F, 0.809049F},
            {0.186500F, 1.224776F, 0.690951F, 0.813500F, 1.275224F, 0.809049F},
            {0.186500F, 1.349776F, 0.690951F, 0.813500F, 1.400224F, 0.809049F},
            {0.187500F, 0.625000F, 0.812500F, 0.812500F, 1.625000F, 0.937500F},
            {0.749000F, 0.374000F, 0.186500F, 0.876000F, 0.376000F, 0.313500F},
            {0.750000F, 0.812500F, 0.187500F, 1.000000F, 1.062500F, 0.312500F},
            {0.790961F, 0.314814F, 0.000000F, 0.944374F, 0.569701F, 0.062500F},
            {0.812500F, 0.125000F, 0.187500F, 0.937500F, 0.375000F, 0.375000F},
            {0.812500F, 0.125000F, 0.750000F, 0.937500F, 0.375000F, 0.937500F},
            {0.812500F, 0.625000F, 0.187500F, 0.937500F, 0.875000F, 0.937500F},
            {0.812500F, 0.875000F, 0.812500F, 0.937500F, 1.562500F, 0.937500F},
        });

    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event){
        for (BlockModels model : BlockModels.getAllModels()) {
            model.register();
        }
    }
}
