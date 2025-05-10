package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.material.Material;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.blockmodels.RotatableModelDragonSlayer;
import static net.minecraft.block.Block.soundTypeAnvil;

public class ModelRegistry {
    //Интерьер портного
    public static BlockModels ModelThreads;
    public static BlockModels  ModelTailorShelf, ModelCloth, ModelDummy, ModelHangers, ModelPillow, ModelRulers, ModelScissors;
    //Интерьер алхимика
    public static BlockModels ModelIngredients, ModelCauldron, ModelBottles, ModelBooks, ModelAlchemistShelf, ModelAlchemicalFlag;
    //Интерьер охотника
    public static BlockModels ModelArrow, ModelBowWall, ModelBow, ModelDucks, ModelFurKnife, ModelFur, ModelHorn, ModelLeatherDryer,
            ModelRabbits, ModelMooseHead;
    //Интерьер обменщика
    public static BlockModels ModelMagnifyinGlass, ModelBagGold, ModelCoins, ModelSmallChest, ModelScales;
    //Интерьер фермера
    public static BlockModels ModelBagsPotatoes, ModelBasketApples, ModelBasketBerries, ModelBucket, ModelCarrot, ModelGarlic, ModelHay,
            ModelHayfork, ModelJugs, ModelShelfFlower, ModelWateringСan, ModelWheelBarrow;
    //Интерьер ювелира
    public static BlockModels ModelFilledChest, ModelPliers, ModelJewelryHammer, ModelAmulet, ModelInstruments;
    //Интерьер регистраторши
    public static BlockModels ModelLute, ModelBroom, ModelWanted, ModelPapers, ModelKeys, ModelDeskBell;
    //Интерьер оружейника
    public static BlockModels ModelSword, ModelSwords, ModelShield1, ModelShield2, ModelShield3, ModelHelmet, ModelHammer,
            ModelDragonSlayer, ModelAxe;
    //Интерьер инженера
    public static BlockModels ModelGas, ModelOiler, ModelGears, ModelDrawing1, ModelDrawing2, ModelClock, ModelBrokenMechanism;

    public static void preInit(FMLPreInitializationEvent event){
        ModelThreads = new BlockModels(Material.cloth, "ModelThreads", "threads", "threads");
        ModelTailorShelf = new BlockModels(Material.cloth,"ModelTailorShelf", "tailor's_shelf", "tailor's_shelf");
        ModelCloth = new BlockModels(Material.cloth,"ModelCloth", "cloth", "cloth");
        ModelCloth.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelCloth.setCollisionEnabled(false);
        ModelDummy = new BlockModels(Material.cloth, "ModelDummy", "dummy", "dummy");
        ModelDummy.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 1.6F, 0.8F);
        ModelHangers = new BlockModels(Material.cloth,"ModelHangers", "hangers", "hangers");
        ModelHangers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelHangers.setCollisionEnabled(false);
        ModelPillow = new BlockModels(Material.cloth,"ModelPillow", "pillow_for_needles", "pillow_for_needles");
        ModelRulers = new BlockModels(Material.cloth,"ModelRulers", "rulers", "rulers");
        ModelRulers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelRulers.setCollisionEnabled(false);
        ModelScissors = new BlockModels(Material.cloth,"ModelScissors", "scissors", "scissors");

        //алхимик
        ModelIngredients = new BlockModels(Material.cloth,"ModelIngredients", "ingredients", "ingredients");
        ModelIngredients.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelIngredients.setCollisionEnabled(false);
        ModelCauldron = new BlockModels(Material.cloth,"ModelCauldron", "cauldron", "cauldron");
        ModelBottles = new BlockModels(Material.cloth,"ModelBottles", "bottles", "bottles");
        ModelBooks = new BlockModels(Material.cloth,"ModelBooks", "books", "books");

        ModelAlchemistShelf = new BlockModels(Material.cloth,"ModelAlchemistShelf", "alchemist's_shelf", "alchemist's_shelf");

        ModelAlchemicalFlag = new BlockModels(Material.cloth,"ModelAlchemicalFlag", "alchemical_flag", "alchemical_flag");
        ModelAlchemicalFlag.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAlchemicalFlag.setCollisionEnabled(false);

        //ниже модели охотника
        ModelArrow = new BlockModels(Material.cloth,"ModelArrow", "arrows", "arrows");
        ModelArrow.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelBowWall = new BlockModels(Material.cloth,"ModelBowWall", "bow", "bow_on_the_wall");
        ModelBowWall.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelBowWall.setCollisionEnabled(false);

        ModelBow = new BlockModels(Material.cloth,"ModelBow", "bow", "bow");
        ModelBow.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelDucks = new BlockModels(Material.cloth,"ModelDucks", "ducks", "ducks");
        ModelDucks.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDucks.setCollisionEnabled(false);

        ModelFurKnife = new BlockModels(Material.cloth,"ModelFurKnife", "fur_with_a_knife", "fur_with_a_knife");
        ModelFurKnife.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelFur = new BlockModels(Material.cloth,"ModelFur", "fur", "fur");
        ModelFur.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelHorn = new BlockModels(Material.cloth,"ModelHorn", "horn", "horn");
        ModelHorn.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelHorn.setCollisionEnabled(false);

        ModelLeatherDryer = new BlockModels(Material.cloth,"ModelLeatherDryer", "leather_dryer", "leather_dryer");
        ModelLeatherDryer.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.7F, 1.0F);

        ModelRabbits = new BlockModels(Material.cloth,"ModelRabbits", "rabbits", "rabbits");
        ModelRabbits.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelRabbits.setCollisionEnabled(false);

        ModelMooseHead = new BlockModels(Material.cloth,"ModelMooseHead", "moose_head", "moose_head");
        ModelMooseHead.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        //ниже модели обменщика
        ModelMagnifyinGlass = new BlockModels(Material.cloth,"ModelMagnifyinGlass", "magnifying_glass", "magnifying_glass");
        ModelMagnifyinGlass.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelBagGold = new BlockModels(Material.cloth,"ModelBagGold", "bag_of_gold", "bag_of_gold");

        ModelCoins = new BlockModels(Material.cloth,"ModelCoins", "coins", "coins");
        ModelCoins.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelSmallChest = new BlockModels(Material.cloth,"ModelSmallChest", "small_chest", "small_chest");
        ModelSmallChest.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.5F, 0.8F);

        ModelScales = new BlockModels(Material.cloth,"ModelScales", "scales", "scales");
        ModelScales.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.5F, 0.9F);

        //ниже модели фермера
        ModelBagsPotatoes = new BlockModels(Material.cloth,"ModelBagsPotatoes", "bags_of_potatoes", "bags_of_potatoes");

        ModelBasketApples = new BlockModels(Material.cloth,"ModelBasketApples", "basket_of_apples", "basket_of_apples");
        ModelBasketApples.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.3F, 0.9F);

        ModelBasketBerries = new BlockModels(Material.cloth,"ModelBasketBerries", "basket_of_berries", "basket_of_berries");
        ModelBasketBerries.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.4F, 0.9F);

        ModelBucket = new BlockModels(Material.cloth,"ModelBucket", "bucket", "bucket");
        ModelBucket.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.4F, 0.7F);

        ModelCarrot = new BlockModels(Material.cloth,"ModelCarrot", "carrot", "carrot");
        ModelCarrot.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelGarlic = new BlockModels(Material.cloth,"ModelGarlic", "garlic", "garlic");
        ModelGarlic.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelGarlic.setCollisionEnabled(false);

        ModelHay = new BlockModels(Material.cloth,"ModelHay", "hay", "hay");
        ModelHay.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);

        ModelHayfork = new BlockModels(Material.cloth,"ModelHayfork", "hayfork", "hayfork");
        ModelHayfork.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
        ModelHayfork.setCollisionEnabled(false);

        ModelJugs = new BlockModels(Material.cloth,"ModelJugs", "jugs", "jugs");

        ModelShelfFlower = new BlockModels(Material.cloth,"ModelShelfFlower", "shelf_with_flower", "shelf_with_flower");

        ModelWateringСan = new BlockModels(Material.cloth,"ModelWateringСan", "watering_can", "watering_can");
        ModelWateringСan.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelWateringСan.setCollisionEnabled(false);


        ModelWheelBarrow = new BlockModels(Material.cloth,"ModelWheelBarrow", "wheelbarrow", "wheelbarrow");
        ModelWheelBarrow.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);


        //ниже модели ювелира
        ModelFilledChest = new BlockModels(Material.cloth,"ModelFilledChest", "small_filled_chest", "small_filled_chest");
        ModelFilledChest.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.6F, 1.0F);


        ModelPliers = new BlockModels(Material.cloth,"ModelPliers", "pliers", "pliers");
        ModelPliers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);

        ModelJewelryHammer = new BlockModels(Material.cloth,"ModelJewelryHammer", "jewelry_hammer", "jewelry_hammer");
        ModelJewelryHammer.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);

        ModelAmulet = new BlockModels(Material.cloth,"ModelAmulet", "amulet", "amulet");
        ModelAmulet.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAmulet.setCollisionEnabled(false);

        ModelInstruments = new BlockModels(Material.cloth,"ModelInstruments", "instruments", "instruments");
        ModelInstruments.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelInstruments.setCollisionEnabled(false);

        //ниже модели регистраторши
        ModelLute = new BlockModels(Material.cloth,"ModelLute", "lute", "lute");
        ModelLute.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelLute.setCollisionEnabled(false);

        ModelBroom = new BlockModels(Material.cloth,"ModelBroom", "broom", "broom");
        ModelBroom.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.5F, 1.0F);
        ModelBroom.setCollisionEnabled(false);

        ModelWanted = new BlockModels(Material.cloth,"ModelWanted", "wanted", "wanted");
        ModelWanted.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelWanted.setCollisionEnabled(false);

        ModelPapers = new BlockModels(Material.cloth,"ModelPapers", "papers", "papers");
        ModelPapers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelKeys = new BlockModels(Material.cloth,"ModelKeys", "keys", "keys");
        ModelKeys.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelKeys.setCollisionEnabled(false);

        ModelDeskBell = new BlockModels(Material.cloth,"ModelDeskBell", "desk_bell", "desk_bell");
        ModelDeskBell.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.3F, 0.7F);
        ModelDeskBell.setCollisionEnabled(false);

        //ниже модели оружейника
        ModelSword = new BlockModels(Material.cloth,"ModelSword", "sword", "sword");
        ModelSword.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelSwords = new BlockModels(Material.cloth,"ModelSwords", "swords", "swords");
        ModelSwords.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);


        ModelShield1 = new BlockModels(Material.cloth,"ModelShield1", "shield1", "shield");
        ModelShield1.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield1.setCollisionEnabled(false);

        ModelShield2 = new BlockModels(Material.cloth,"ModelShield2", "shield2", "shield");
        ModelShield2.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield2.setCollisionEnabled(false);

        ModelShield3 = new BlockModels(Material.cloth,"ModelShield3", "shield3", "shield");
        ModelShield3.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield3.setCollisionEnabled(false);

        ModelHelmet = new BlockModels(Material.cloth,"ModelHelmet", "helmet", "helmet");
        ModelHelmet.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.7F, 0.9F);

        ModelHammer = new BlockModels(Material.cloth,"ModelHammer", "hammer", "hammer");


        ModelDragonSlayer = new RotatableModelDragonSlayer(Material.cloth,"ModelDragonSlayer", "dragon_slayer", "dragon_slayer");
        ModelDragonSlayer.setStepSound(soundTypeAnvil);

        ModelAxe = new BlockModels(Material.cloth,"ModelAxe", "axe", "axe");
        ModelAxe.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAxe.setCollisionEnabled(false);

        //Ниже модели инженера
        ModelGas = new BlockModels(Material.cloth,"ModelGas", "gas_cylinders", "gas_cylinders");
        ModelGas.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.7F, 0.9F);

        ModelOiler = new BlockModels(Material.cloth,"ModelOiler", "oiler", "oiler");
        ModelOiler.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.6F, 0.7F);

        ModelGears = new BlockModels(Material.cloth,"ModelGears", "gears", "gears");
        ModelGears.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);

        ModelDrawing1 = new BlockModels(Material.cloth,"ModelDrawing1", "drawing1", "drawing");
        ModelDrawing1.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDrawing1.setCollisionEnabled(false);

        ModelDrawing2 = new BlockModels(Material.cloth,"ModelDrawing2", "drawing2", "drawing");
        ModelDrawing2.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDrawing2.setCollisionEnabled(false);

        ModelClock = new BlockModels(Material.cloth,"ModelClock", "clock", "clock");
        ModelClock.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelClock.setCollisionEnabled(false);

        ModelBrokenMechanism = new BlockModels(Material.cloth,"ModelBrokenMechanism", "broken_mechanism", "broken_mechanism");

        ModelBrokenMechanism.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.3F, 0.7F);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event){
        //Модели для портного
        ModelThreads.register();
        ModelTailorShelf.register();
        ModelCloth.register();
        ModelDummy.register();
        ModelHangers.register();
        ModelPillow.register();
        ModelRulers.register();
        ModelScissors.register();

        //Модели для алхимика
        ModelIngredients.register();
        ModelCauldron.register();
        ModelBottles.register();
        ModelBooks.register();
        ModelAlchemistShelf.register();
        ModelAlchemicalFlag.register();


        ModelArrow.register();
        ModelBowWall.register();
        ModelBow.register();
        ModelDucks.register();
        ModelFurKnife.register();
        ModelFur.register();
        ModelHorn.register();
        ModelLeatherDryer.register();
        ModelRabbits.register();
        ModelMooseHead.register();


        ModelMagnifyinGlass.register();
        ModelBagGold.register();
        ModelCoins.register();
        ModelSmallChest.register();
        ModelScales.register();


        ModelBagsPotatoes.register();
        ModelBasketApples.register();
        ModelBasketBerries.register();
        ModelBucket.register();
        ModelCarrot.register();
        ModelGarlic.register();
        ModelHay.register();
        ModelHayfork.register();
        ModelJugs.register();
        ModelShelfFlower.register();
        ModelWateringСan.register();
        ModelWheelBarrow.register();


        ModelFilledChest.register();
        ModelPliers.register();
        ModelJewelryHammer.register();
        ModelAmulet.register();
        ModelInstruments.register();


        ModelLute.register();
        ModelBroom.register();
        ModelWanted.register();
        ModelPapers.register();
        ModelKeys.register();
        ModelDeskBell.register();


        ModelSword.register();
        ModelSwords.register();
        ModelShield1.register();
        ModelShield2.register();
        ModelShield3.register();
        ModelHelmet.register();
        ModelHammer.register();
        ModelDragonSlayer.register();
        ModelAxe.register();


        ModelGas.register();
        ModelOiler.register();
        ModelGears.register();
        ModelDrawing1.register();
        ModelDrawing2.register();
        ModelClock.register();
        ModelBrokenMechanism.register();






    }
}
