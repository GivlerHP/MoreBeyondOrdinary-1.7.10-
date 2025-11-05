package ru.givler.mbo.registry;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

import static ru.givler.mbo.registry.ArmorRegistry.*;
import static ru.givler.mbo.registry.BlockRegistry.*;
import static ru.givler.mbo.registry.DrinkRegistry.*;
import static ru.givler.mbo.registry.FoodRegistry.*;
import static ru.givler.mbo.registry.ItemRegistry.*;
import static ru.givler.mbo.registry.ModelRegistry.*;

public class CreativeTabRegistry {
    public static final CreativeTabs tabMBOblocks = new CreativeTabs("MBOblocks") {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(BlockImperialBrick);
        }

        @Override
        @SideOnly(Side.CLIENT)
        //list.add(new ItemStack());
        public void displayAllReleventItems(List list) {
            list.add(new ItemStack(BlockGreyStone));
            list.add(new ItemStack(StairsStone));
            list.add(new ItemStack(SlabStone));

            list.add(new ItemStack(BlockGreyCobblestone));
            list.add(new ItemStack(BlockGreyCobblesMossy));
            list.add(new ItemStack(StairsGreyCobblestone));
            list.add(new ItemStack(SlabCobblestone));

            for(int i=0; i<=3; i++) { list.add(new ItemStack(BlockStonebrick, 0, i)); }
            list.add(new ItemStack(StairsStonebrick));
            list.add(new ItemStack(SlabStonebrick));
            list.add(new ItemStack(WallStonebrick));
            list.add(new ItemStack(TotemStone));

            for(int i=0; i<=2; i++) { list.add(new ItemStack(BlockSandstone, 0, i)); }
            list.add(new ItemStack(StairsSandstone));
            list.add(new ItemStack(SlabSandstone));
            list.add(new ItemStack(WallSandstone));

            list.add(new ItemStack(BlockImperialBrick));
            list.add(new ItemStack(StairsImperialBrick));
            list.add(new ItemStack(SlabImperialBrick));

            list.add(new ItemStack(BlockHeneizenBrick));
            list.add(new ItemStack(StairsHeneizenBrick));
            list.add(new ItemStack(SlabImperialBrick));

            list.add(new ItemStack(BlockIrgadBrick));
            list.add(new ItemStack(StairsIrgadBrick));
            list.add(new ItemStack(SlabIrgadBrick));

            list.add(new ItemStack(BlockEndbrick));
            list.add(new ItemStack(StairsEndbrick));
            list.add(new ItemStack(SlabEndbrick));

            list.add(new ItemStack(BlockFiredClay));
            list.add(new ItemStack(StairsFiredClay));
            list.add(new ItemStack(SlabFiredClay));
            list.add(new ItemStack(WallFiredClay));

            list.add(new ItemStack(BlockClayWall));
            list.add(new ItemStack(BlockFogWhite));
            list.add(new ItemStack(BlockFogGrey));

            list.add(new ItemStack(BooksheelSkull));
            list.add(new ItemStack(BooksheelVoid));
            list.add(new ItemStack(BooksheelWeb));
            list.add(new ItemStack(BooksheelCandle));
            list.add(new ItemStack(BooksheelSkullWeb));
            list.add(new ItemStack(BooksheelSkullCandle));

            list.add(new ItemStack(DebarkedOak));
            list.add(new ItemStack(DebarkedSpruce));
            list.add(new ItemStack(DebarkedBirch));
            list.add(new ItemStack(DebarkedJungle));
            list.add(new ItemStack(DebarkedAcacia));
            list.add(new ItemStack(DebarkedBigOak));

            list.add(new ItemStack(WoodTotemOak));
            list.add(new ItemStack(WoodTotemSpruce));
            list.add(new ItemStack(WoodTotemBirch));
            list.add(new ItemStack(WoodTotemJungle));
            list.add(new ItemStack(WoodTotemAcacia));
            list.add(new ItemStack(WoodTotemBigOak));

            for(int i=0; i<=5; i++) { list.add(new ItemStack(RoofWood, 0, i)); }
            for (int i = 0; i <=5; i++) { list.add(new ItemStack(StairsRoofWood[i])); }
            for (int i = 0; i <=5; i++) { list.add(new ItemStack(SlabRoofWood[i])); }

            for(int i=0; i<=2; i++) { list.add(new ItemStack(RoofUnfired, 0, i)); }
            for(int i=0; i<=2; i++) { list.add(new ItemStack(RoofStandart, 0, i)); }
            for (int i = 0; i <=2; i++) { list.add(new ItemStack(StairsRoofStandart[i])); }
            for (int i = 0; i <=2; i++) { list.add(new ItemStack(SlabRoofStandart[i])); }

            for(int i=0; i<=15; i++) { list.add(new ItemStack(RoofLaminated, 0, i)); }
            for (int i = 0; i <=15; i++) { list.add(new ItemStack(StairsRoofLaminated[i])); }
            for (int i = 0; i <=15; i++) { list.add(new ItemStack(SlabRoofLaminated[i])); }

            for(int i=0; i<=15; i++) { list.add(new ItemStack(RoofSheet, 0, i)); }
            for (int i = 0; i <=15; i++) { list.add(new ItemStack(StairsRoofSheet[i])); }
            for (int i = 0; i <=15; i++) { list.add(new ItemStack(SlabRoofSheet[i])); }

            for(int i=0; i<=15; i++) { list.add(new ItemStack(RoofFlake, 0, i)); }
            for (int i = 0; i <=15; i++) { list.add(new ItemStack(StairsRoofFlake[i])); }
            for (int i = 0; i <=15; i++) { list.add(new ItemStack(SlabRoofFlake[i])); }




        }
    };

    public static final CreativeTabs tabMBOitems = new CreativeTabs("MBOitems") {
        @Override
        public Item getTabIconItem() {
            return WeaponRapier;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllReleventItems(List list) {

            list.add(new ItemStack(GlyphAmphibian));
            list.add(new ItemStack(GlyphDragon));
            list.add(new ItemStack(GlyphHawk));
            list.add(new ItemStack(GlyphMiner));
            list.add(new ItemStack(GlyphOwl));
            list.add(new ItemStack(GlyphWeapon));
            list.add(new ItemStack(GlyphCleansing));
            list.add(new ItemStack(GlyphHealing));
            list.add(new ItemStack(GlyphVoid));

            for(int i=0; i<=0; i++) { list.add(new ItemStack(Metal, 0, i)); }
            list.add(new ItemStack(SapphireEye));
            list.add(new ItemStack(SapphireHeart));
            for(int i=0; i<=14; i++) { list.add(new ItemStack(Crystall, 0, i)); }


            list.add(new ItemStack(SmallBasicRing));
            list.add(new ItemStack(BasicRing));

            list.add(new ItemStack(LifeSmallRing));
            list.add(new ItemStack(StaminaSmallRing));
            list.add(new ItemStack(DamageSmallRing));
            list.add(new ItemStack(SpeedSmallRing));

            list.add(new ItemStack(LifeRing));
            list.add(new ItemStack(StaminaRing));
            list.add(new ItemStack(DamageRing));
            list.add(new ItemStack(SpeedRing));

            list.add(new ItemStack(GoldBasicAmulet));
            list.add(new ItemStack(SilverBasicAmulet));

            list.add(new ItemStack(HealingAmulet));
            list.add(new ItemStack(StrengthAmulet));
            list.add(new ItemStack(StaminaAmulet));

            list.add(new ItemStack(VampirismAmulet));
            list.add(new ItemStack(CleansingAmulet));
            list.add(new ItemStack(PhoenixAmulet));
            list.add(new ItemStack(CowardAmulet));
            list.add(new ItemStack(DragonAmulet));
            list.add(new ItemStack(VeilAmulet));
            list.add(new ItemStack(ThornsAmulet));
            list.add(new ItemStack(MercenaryAmulet));

            list.add(new ItemStack(FertilityBelt));
            list.add(new ItemStack(FallBelt));
            list.add(new ItemStack(MinerBelt));
            list.add(new ItemStack(WaterminerBelt));
            list.add(new ItemStack(KnightBelt));

            list.add(new ItemStack(BrokenLongsword));
            list.add(new ItemStack(BrokenSword));
            list.add(new ItemStack(BrokenDagger));
            list.add(new ItemStack(BrokenRapier));
            list.add(new ItemStack(BrokenMace));
            list.add(new ItemStack(BrokenAxe));
            list.add(new ItemStack(BrokenCudgel));
            list.add(new ItemStack(BrokenBowHunting));
            list.add(new ItemStack(BrokenStaffHealing));

            if (Loader.isModLoaded("customnpcs")) {
            list.add(new ItemStack(BrokenStaffFire));
            list.add(new ItemStack(BrokenGrimoireWater));
            }

            list.add(new ItemStack(WeaponRapier));

            list.add(new ItemStack(KnightHelmet));
            list.add(new ItemStack(KnightChest));
            list.add(new ItemStack(KnightLegs));
            list.add(new ItemStack(KnightBoots));

            list.add(new ItemStack(MercenaryHelmet));
            list.add(new ItemStack(MercenaryChest));
            list.add(new ItemStack(MercenaryLegs));
            list.add(new ItemStack(MercenaryBoots));

            list.add(new ItemStack(WandererHelmet));
            list.add(new ItemStack(WandererChest));
            list.add(new ItemStack(WandererLegs));
            list.add(new ItemStack(WandererBoots));

            list.add(new ItemStack(PyromancerHelmet));
            list.add(new ItemStack(PyromancerChest));
            list.add(new ItemStack(PyromancerLegs));
            list.add(new ItemStack(PyromancerBoots));

            list.add(new ItemStack(ClericHelmet));
            list.add(new ItemStack(ClericChest));
            list.add(new ItemStack(ClericLegs));
            list.add(new ItemStack(ClericBoots));

            list.add(new ItemStack(WizardHelmet));
            list.add(new ItemStack(WizardChest));
            list.add(new ItemStack(WizardLegs));
            list.add(new ItemStack(WizardBoots));

            list.add(new ItemStack(Uchigatana));


        }
    };

    public static final CreativeTabs tabMBOfoods = new CreativeTabs("MBOfoods") {
        @Override
        public Item getTabIconItem() {
            return DrinkWine;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllReleventItems(List list) {
            list.add(new ItemStack(FoodBacon));
            list.add(new ItemStack(FoodBlackBread));
            list.add(new ItemStack(FoodBurger));
            list.add(new ItemStack(FoodChowder));
            list.add(new ItemStack(FoodDeliciousChicken));
            list.add(new ItemStack(FoodDeliciousSalad));
            list.add(new ItemStack(FoodDivineSteak));
            list.add(new ItemStack(FoodFreshBread));
            list.add(new ItemStack(FoodFriedSausage));
            list.add(new ItemStack(FoodCheese));
            list.add(new ItemStack(FoodSoup));
            list.add(new ItemStack(FoodMeatPie));
            list.add(new ItemStack(FoodStrangeFish));

            list.add(new ItemStack(DrinkWine));
            list.add(new ItemStack(DrinkAle));
            list.add(new ItemStack(DrinkMead));
            list.add(new ItemStack(DrinkCider));
            list.add(new ItemStack(DrinkBrandy));
            list.add(new ItemStack(DrinkMulledWine));
            list.add(new ItemStack(DrinkTincure));
            list.add(new ItemStack(DrinkBreathWyvern));
            list.add(new ItemStack(DrinkBeer));
            list.add(new ItemStack(DrinkBarrelBeer));
        }
    };

    public static final CreativeTabs tabMBOdecors = new CreativeTabs("MBOdecors") {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(ModelDummy);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllReleventItems(List list) {
            list.add(new ItemStack(ModelThreads));
            list.add(new ItemStack(ModelTailorShelf));
            list.add(new ItemStack(ModelCloth));
            list.add(new ItemStack(ModelDummy));
            list.add(new ItemStack(ModelHangers));
            list.add(new ItemStack(ModelPillow));
            list.add(new ItemStack(ModelRulers));
            list.add(new ItemStack(ModelScissors));

            list.add(new ItemStack(ModelIngredients));
            list.add(new ItemStack(ModelCauldron));
            list.add(new ItemStack(ModelBottles));
            list.add(new ItemStack(ModelBooks));
            list.add(new ItemStack(ModelAlchemistShelf));
            list.add(new ItemStack(ModelAlchemicalFlag));

            list.add(new ItemStack(ModelArrow));
            list.add(new ItemStack(ModelBowWall));
            list.add(new ItemStack(ModelBow));
            list.add(new ItemStack(ModelDucks));
            list.add(new ItemStack(ModelFurKnife));
            list.add(new ItemStack(ModelFur));
            list.add(new ItemStack(ModelHorn));
            list.add(new ItemStack(ModelLeatherDryer));
            list.add(new ItemStack(ModelRabbits));
            list.add(new ItemStack(ModelMooseHead));

            list.add(new ItemStack(ModelMagnifyinGlass));
            list.add(new ItemStack(ModelBagGold));
            list.add(new ItemStack(ModelCoins));
            list.add(new ItemStack(ModelSmallChest));
            list.add(new ItemStack(ModelScales));

            list.add(new ItemStack(ModelBagsPotatoes));
            list.add(new ItemStack(ModelBasketApples));
            list.add(new ItemStack(ModelBasketBerries));
            list.add(new ItemStack(ModelBucket));
            list.add(new ItemStack(ModelCarrot));
            list.add(new ItemStack(ModelGarlic));
            list.add(new ItemStack(ModelHay));
            list.add(new ItemStack(ModelHayfork));
            list.add(new ItemStack(ModelJugs));
            list.add(new ItemStack(ModelShelfFlower));
            list.add(new ItemStack(ModelWateringСan));
            list.add(new ItemStack(ModelWheelBarrow));

            list.add(new ItemStack(ModelFilledChest));
            list.add(new ItemStack(ModelPliers));
            list.add(new ItemStack(ModelJewelryHammer));
            list.add(new ItemStack(ModelAmulet));
            list.add(new ItemStack(ModelInstruments));

            list.add(new ItemStack(ModelLute));
            list.add(new ItemStack(ModelBroom));
            list.add(new ItemStack(ModelWanted));
            list.add(new ItemStack(ModelPapers));
            list.add(new ItemStack(ModelKeys));
            list.add(new ItemStack(ModelDeskBell));

            list.add(new ItemStack(ModelSword));
            list.add(new ItemStack(ModelSwords));
            list.add(new ItemStack(ModelShield1));
            list.add(new ItemStack(ModelShield2));
            list.add(new ItemStack(ModelShield3));
            list.add(new ItemStack(ModelHelmet));
            list.add(new ItemStack(ModelHammer));
            list.add(new ItemStack(ModelDragonSlayer));
            list.add(new ItemStack(ModelAxe));

            list.add(new ItemStack(ModelGas));
            list.add(new ItemStack(ModelOiler));
            list.add(new ItemStack(ModelGears));
            list.add(new ItemStack(ModelDrawing1));
            list.add(new ItemStack(ModelDrawing2));
            list.add(new ItemStack(ModelClock));
            list.add(new ItemStack(ModelBrokenMechanism));

        }
    };

    public static void init(FMLInitializationEvent event) {

    }
}