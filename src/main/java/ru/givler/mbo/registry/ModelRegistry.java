package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.material.Material;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.blockmodels.RotatableModelDragonSlayer;
import static net.minecraft.block.Block.soundTypeAnvil;
import static ru.givler.mbo.proxy.ClientProxy.bindDefaultRender;

public class ModelRegistry {
    //Интерьер портного
    public static BlockModels ModelThreads, ModelTailorShelf, ModelCloth, ModelDummy, ModelHangers, ModelPillow, ModelRulers, ModelScissors;
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


    @Mod.EventHandler
    public static void init(FMLInitializationEvent event){
        //Модели для портного
        ModelThreads = new BlockModels(Material.cloth,"ModelThreads", "threads", "threads");
        ModelThreads.register();
        bindDefaultRender(ModelRegistry.ModelThreads);

        ModelTailorShelf = new BlockModels(Material.cloth,"ModelTailorShelf", "tailor's_shelf", "tailor's_shelf");
        ModelTailorShelf.register();
        bindDefaultRender(ModelRegistry.ModelTailorShelf);

        ModelCloth = new BlockModels(Material.cloth,"ModelCloth", "cloth", "cloth");
        ModelCloth.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelCloth.setCollisionEnabled(false);
        ModelCloth.register();
        bindDefaultRender(ModelRegistry.ModelCloth);

        ModelDummy = new BlockModels(Material.cloth, "ModelDummy", "dummy", "dummy");
        ModelDummy.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 1.6F, 0.8F);
        ModelDummy.register();
        bindDefaultRender(ModelRegistry.ModelDummy);

        ModelHangers = new BlockModels(Material.cloth,"ModelHangers", "hangers", "hangers");
        ModelHangers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelHangers.setCollisionEnabled(false);
        ModelHangers.register();
        bindDefaultRender(ModelRegistry.ModelHangers);

        ModelPillow = new BlockModels(Material.cloth,"ModelPillow", "pillow_for_needles", "pillow_for_needles");
        ModelPillow.register();
        bindDefaultRender(ModelRegistry.ModelPillow);

        ModelRulers = new BlockModels(Material.cloth,"ModelRulers", "rulers", "rulers");
        ModelRulers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelRulers.setCollisionEnabled(false);
        ModelRulers.register();
        bindDefaultRender(ModelRegistry.ModelRulers);

        ModelScissors = new BlockModels(Material.cloth,"ModelScissors", "scissors", "scissors");
        ModelScissors.register();
        bindDefaultRender(ModelRegistry.ModelScissors);

        //Модели для алхимика
        ModelIngredients = new BlockModels(Material.cloth,"ModelIngredients", "ingredients", "ingredients");
        ModelIngredients.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelIngredients.setCollisionEnabled(false);
        ModelIngredients.register();
        bindDefaultRender(ModelRegistry.ModelIngredients);

        ModelCauldron = new BlockModels(Material.cloth,"ModelCauldron", "cauldron", "cauldron");
        ModelCauldron.register();
        bindDefaultRender(ModelRegistry.ModelCauldron);


        ModelBottles = new BlockModels(Material.cloth,"ModelBottles", "bottles", "bottles");
        ModelBottles.register();
        bindDefaultRender(ModelRegistry.ModelBottles);

        ModelBooks = new BlockModels(Material.cloth,"ModelBooks", "books", "books");
        ModelBooks.register();
        bindDefaultRender(ModelRegistry.ModelBooks);

        ModelAlchemistShelf = new BlockModels(Material.cloth,"ModelAlchemistShelf", "alchemist's_shelf", "alchemist's_shelf");
        ModelAlchemistShelf.register();
        bindDefaultRender(ModelRegistry.ModelAlchemistShelf);

        ModelAlchemicalFlag = new BlockModels(Material.cloth,"ModelAlchemicalFlag", "alchemical_flag", "alchemical_flag");
        ModelAlchemicalFlag.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAlchemicalFlag.setCollisionEnabled(false);
        ModelAlchemicalFlag.register();
        bindDefaultRender(ModelRegistry.ModelAlchemicalFlag);

        //ниже модели охотника
        ModelArrow = new BlockModels(Material.cloth,"ModelArrow", "arrows", "arrows");
        ModelArrow.register();
        ModelArrow.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelArrow);

        ModelBowWall = new BlockModels(Material.cloth,"ModelBowWall", "bow", "bow_on_the_wall");
        ModelBowWall.register();
        ModelBowWall.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelBowWall.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelBowWall);

        ModelBow = new BlockModels(Material.cloth,"ModelBow", "bow", "bow");
        ModelBow.register();
        ModelBow.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelBow);

        ModelDucks = new BlockModels(Material.cloth,"ModelDucks", "ducks", "ducks");
        ModelDucks.register();
        ModelDucks.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDucks.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelDucks);

        ModelFurKnife = new BlockModels(Material.cloth,"ModelFurKnife", "fur_with_a_knife", "fur_with_a_knife");
        ModelFurKnife.register();
        ModelFurKnife.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelFurKnife);

        ModelFur = new BlockModels(Material.cloth,"ModelFur", "fur", "fur");
        ModelFur.register();
        ModelFur.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelFur);

        ModelHorn = new BlockModels(Material.cloth,"ModelHorn", "horn", "horn");
        ModelHorn.register();
        ModelHorn.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelHorn.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelHorn);

        ModelLeatherDryer = new BlockModels(Material.cloth,"ModelLeatherDryer", "leather_dryer", "leather_dryer");
        ModelLeatherDryer.register();
        ModelLeatherDryer.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.7F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelLeatherDryer);

        ModelRabbits = new BlockModels(Material.cloth,"ModelRabbits", "rabbits", "rabbits");
        ModelRabbits.register();
        ModelRabbits.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelRabbits.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelRabbits);

        ModelMooseHead = new BlockModels(Material.cloth,"ModelMooseHead", "moose_head", "moose_head");
        ModelMooseHead.register();
        ModelMooseHead.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelMooseHead);

        //ниже модели обменщика
        ModelMagnifyinGlass = new BlockModels(Material.cloth,"ModelMagnifyinGlass", "magnifying_glass", "magnifying_glass");
        ModelMagnifyinGlass.register();
        ModelMagnifyinGlass.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelMagnifyinGlass);

        ModelBagGold = new BlockModels(Material.cloth,"ModelBagGold", "bag_of_gold", "bag_of_gold");
        ModelBagGold.register();
        bindDefaultRender(ModelRegistry.ModelBagGold);

        ModelCoins = new BlockModels(Material.cloth,"ModelCoins", "coins", "coins");
        ModelCoins.register();
        ModelCoins.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelCoins);

        ModelSmallChest = new BlockModels(Material.cloth,"ModelSmallChest", "small_chest", "small_chest");
        ModelSmallChest.register();
        ModelSmallChest.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.5F, 0.8F);
        bindDefaultRender(ModelRegistry.ModelSmallChest);

        ModelScales = new BlockModels(Material.cloth,"ModelScales", "scales", "scales");
        ModelScales.register();
        ModelScales.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.5F, 0.9F);
        bindDefaultRender(ModelRegistry.ModelScales);

        //ниже модели фермера
        ModelBagsPotatoes = new BlockModels(Material.cloth,"ModelBagsPotatoes", "bags_of_potatoes", "bags_of_potatoes");
        ModelBagsPotatoes.register();
        bindDefaultRender(ModelRegistry.ModelBagsPotatoes);

        ModelBasketApples = new BlockModels(Material.cloth,"ModelBasketApples", "basket_of_apples", "basket_of_apples");
        ModelBasketApples.register();
        ModelBasketApples.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.3F, 0.9F);
        bindDefaultRender(ModelRegistry.ModelBasketApples);

        ModelBasketBerries = new BlockModels(Material.cloth,"ModelBasketBerries", "basket_of_berries", "basket_of_berries");
        ModelBasketBerries.register();
        ModelBasketBerries.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.4F, 0.9F);
        bindDefaultRender(ModelRegistry.ModelBasketBerries);

        ModelBucket = new BlockModels(Material.cloth,"ModelBucket", "bucket", "bucket");
        ModelBucket.register();
        ModelBucket.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.4F, 0.7F);
        bindDefaultRender(ModelRegistry.ModelBucket);

        ModelCarrot = new BlockModels(Material.cloth,"ModelCarrot", "carrot", "carrot");
        ModelCarrot.register();
        ModelCarrot.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelCarrot);

        ModelGarlic = new BlockModels(Material.cloth,"ModelGarlic", "garlic", "garlic");
        ModelGarlic.register();
        ModelGarlic.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelGarlic.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelGarlic);

        ModelHay = new BlockModels(Material.cloth,"ModelHay", "hay", "hay");
        ModelHay.register();
        ModelHay.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelHay);

        ModelHayfork = new BlockModels(Material.cloth,"ModelHayfork", "hayfork", "hayfork");
        ModelHayfork.register();
        ModelHayfork.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
        ModelHayfork.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelHayfork);

        ModelJugs = new BlockModels(Material.cloth,"ModelJugs", "jugs", "jugs");
        ModelJugs.register();
        bindDefaultRender(ModelRegistry.ModelJugs);

        ModelShelfFlower = new BlockModels(Material.cloth,"ModelShelfFlower", "shelf_with_flower", "shelf_with_flower");
        ModelShelfFlower.register();
        bindDefaultRender(ModelRegistry.ModelShelfFlower);

        ModelWateringСan = new BlockModels(Material.cloth,"ModelWateringСan", "watering_can", "watering_can");
        ModelWateringСan.register();
        ModelWateringСan.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelWateringСan.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelWateringСan);

        ModelWheelBarrow = new BlockModels(Material.cloth,"ModelWheelBarrow", "wheelbarrow", "wheelbarrow");
        ModelWheelBarrow.register();
        ModelWheelBarrow.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelWheelBarrow);

        //ниже модели ювелира
        ModelFilledChest = new BlockModels(Material.cloth,"ModelFilledChest", "small_filled_chest", "small_filled_chest");
        ModelFilledChest.register();
        ModelFilledChest.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.6F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelFilledChest);

        ModelPliers = new BlockModels(Material.cloth,"ModelPliers", "pliers", "pliers");
        ModelPliers.register();
        ModelPliers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelPliers);

        ModelJewelryHammer = new BlockModels(Material.cloth,"ModelJewelryHammer", "jewelry_hammer", "jewelry_hammer");
        ModelJewelryHammer.register();
        ModelJewelryHammer.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelJewelryHammer);

        ModelAmulet = new BlockModels(Material.cloth,"ModelAmulet", "amulet", "amulet");
        ModelAmulet.register();
        ModelAmulet.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAmulet.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelAmulet);

        ModelInstruments = new BlockModels(Material.cloth,"ModelInstruments", "instruments", "instruments");
        ModelInstruments.register();
        ModelInstruments.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelInstruments.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelInstruments);

        //ниже модели регистраторши
        ModelLute = new BlockModels(Material.cloth,"ModelLute", "lute", "lute");
        ModelLute.register();
        ModelLute.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelLute.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelLute);

        ModelBroom = new BlockModels(Material.cloth,"ModelBroom", "broom", "broom");
        ModelBroom.register();
        ModelBroom.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.5F, 1.0F);
        ModelBroom.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelBroom);

        ModelWanted = new BlockModels(Material.cloth,"ModelWanted", "wanted", "wanted");
        ModelWanted.register();
        ModelWanted.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelWanted.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelWanted);

        ModelPapers = new BlockModels(Material.cloth,"ModelPapers", "papers", "papers");
        ModelPapers.register();
        ModelPapers.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelPapers);

        ModelKeys = new BlockModels(Material.cloth,"ModelKeys", "keys", "keys");
        ModelKeys.register();
        ModelKeys.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelKeys.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelKeys);

        ModelDeskBell = new BlockModels(Material.cloth,"ModelDeskBell", "desk_bell", "desk_bell");
        ModelDeskBell.register();
        ModelDeskBell.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.3F, 0.7F);
        ModelDeskBell.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelDeskBell);

        //ниже модели оружейника
        ModelSword = new BlockModels(Material.cloth,"ModelSword", "sword", "sword");
        ModelSword.register();
        ModelSword.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelSword);

        ModelSwords = new BlockModels(Material.cloth,"ModelSwords", "swords", "swords");
        ModelSwords.register();
        ModelSwords.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelSwords);

        ModelShield1 = new BlockModels(Material.cloth,"ModelShield1", "shield1", "shield");
        ModelShield1.register();
        ModelShield1.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield1.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelShield1);

        ModelShield2 = new BlockModels(Material.cloth,"ModelShield2", "shield2", "shield");
        ModelShield2.register();
        ModelShield2.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield2.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelShield2);

        ModelShield3 = new BlockModels(Material.cloth,"ModelShield3", "shield3", "shield");
        ModelShield3.register();
        ModelShield3.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelShield3.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelShield3);

        ModelHelmet = new BlockModels(Material.cloth,"ModelHelmet", "helmet", "helmet");
        ModelHelmet.register();
        ModelHelmet.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.7F, 0.9F);
        bindDefaultRender(ModelRegistry.ModelHelmet);

        ModelHammer = new BlockModels(Material.cloth,"ModelHammer", "hammer", "hammer");
        ModelHammer.register();
        bindDefaultRender(ModelRegistry.ModelHammer);

        ModelDragonSlayer = new RotatableModelDragonSlayer(Material.cloth,"ModelDragonSlayer", "dragon_slayer", "dragon_slayer");
        ModelDragonSlayer.register();
        ModelDragonSlayer.setStepSound(soundTypeAnvil);
        bindDefaultRender(ModelRegistry.ModelDragonSlayer);

        ModelAxe = new BlockModels(Material.cloth,"ModelAxe", "axe", "axe");
        ModelAxe.register();
        ModelAxe.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelAxe.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelAxe);

        //Ниже модели инженера
        ModelGas = new BlockModels(Material.cloth,"ModelGas", "gas_cylinders", "gas_cylinders");
        ModelGas.register();
        ModelGas.setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.7F, 0.9F);
        bindDefaultRender(ModelRegistry.ModelGas);

        ModelOiler = new BlockModels(Material.cloth,"ModelOiler", "oiler", "oiler");
        ModelOiler.register();
        ModelOiler.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.6F, 0.7F);
        bindDefaultRender(ModelRegistry.ModelOiler);

        ModelGears = new BlockModels(Material.cloth,"ModelGears", "gears", "gears");
        ModelGears.register();
        ModelGears.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        bindDefaultRender(ModelRegistry.ModelGears);

        ModelDrawing1 = new BlockModels(Material.cloth,"ModelDrawing1", "drawing1", "drawing");
        ModelDrawing1.register();
        ModelDrawing1.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDrawing1.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelDrawing1);

        ModelDrawing2 = new BlockModels(Material.cloth,"ModelDrawing2", "drawing2", "drawing");
        ModelDrawing2.register();
        ModelDrawing2.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelDrawing2.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelDrawing2);

        ModelClock = new BlockModels(Material.cloth,"ModelClock", "clock", "clock");
        ModelClock.register();
        ModelClock.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ModelClock.setCollisionEnabled(false);
        bindDefaultRender(ModelRegistry.ModelClock);

        ModelBrokenMechanism = new BlockModels(Material.cloth,"ModelBrokenMechanism", "broken_mechanism", "broken_mechanism");
        ModelBrokenMechanism.register();
        ModelBrokenMechanism.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.3F, 0.7F);
        bindDefaultRender(ModelRegistry.ModelBrokenMechanism);
    }
}
