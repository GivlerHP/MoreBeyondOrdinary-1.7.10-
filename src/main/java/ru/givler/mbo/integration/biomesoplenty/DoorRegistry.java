package ru.givler.mbo.integration.biomesoplenty;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.block.DoorBase;
import ru.givler.mbo.block.TrapDoorBase;
import ru.givler.mbo.config.*;
import ru.givler.mbo.item.DoorItemBase;
import scala.Int;

public class DoorRegistry {

    // Блоки дверей BoP
    public static Block doorSacredBlock, doorCherryBlock, doorDarkBlock, doorFirBlock,
            doorEtherealBlock, doorMagicBlock, doorMangroveBlock, doorPalmBlock,
            doorRedWoodBlock, doorWillowBlock, doorBambooBlock, doorPineBlock,
            doorHellbarkBlock, doorJacarandaBlock, doorMahoganyBlock;

    // Предметы дверей BoP
    public static Item doorSacredItem, doorCherryItem, doorDarkItem, doorFirItem,
            doorEtherealItem, doorMagicItem, doorMangroveItem, doorPalmItem,
            doorRedWoodItem, doorWillowItem, doorBambooItem, doorPineItem,
            doorHellbarkItem, doorJacarandaItem, doorMahoganyItem;

    // Люки BoP
    public static Block trapdoorSacred, trapdoorCherry, trapdoorDark, trapdoorFir,
            trapdoorEthereal, trapdoorMagic, trapdoorMangrove, trapdoorPalm,
            trapdoorRedWood, trapdoorWillow, trapdoorBamboo, trapdoorPine,
            trapdoorHellbark, trapdoorJacaranda, trapdoorMahogany;

    // Ванильные двери и люки
    public static Block doorSpruceBlock, doorBirchBlock, doorJungleBlock, doorAcaciaBlock, doorDarkoakBlock;
    public static Item doorSpruceItem, doorBirchItem, doorJungleItem, doorAcaciaItem, doorDarkoakItem;
    public static Block trapdoorSpruce, trapdoorBirch, trapdoorJungle, trapdoorAcacia, trapdoorDarkoak;

    private static Item[] doorItemsBoP;
    private static Block[] trapdoorBlocksBoP;
    private static Item[] doorItems;
    private static Block[] trapdoorBlocks;

    public static void init() {
        if (Loader.isModLoaded("BiomesOPlenty")) {
            if (IntegrationConfig.enableBoPDoors) {
                doorSacredBlock = new DoorBase(Material.wood, "door_sacred_block", "door_sacred", null);
                doorSacredItem = new DoorItemBase(doorSacredBlock, "sacred_door_item", "door_sacred");
                ((DoorBase) doorSacredBlock).setDropItem(doorSacredItem);

                doorCherryBlock = new DoorBase(Material.wood, "door_cherry_block", "door_cherry", null);
                doorCherryItem = new DoorItemBase(doorCherryBlock, "cherry_door_item", "door_cherry");
                ((DoorBase) doorCherryBlock).setDropItem(doorCherryItem);

                doorDarkBlock = new DoorBase(Material.wood, "door_dark_block", "door_dark", null);
                doorDarkItem = new DoorItemBase(doorDarkBlock, "dark_door_item", "door_dark");
                ((DoorBase) doorDarkBlock).setDropItem(doorDarkItem);

                doorFirBlock = new DoorBase(Material.wood, "door_fir_block", "door_fir", null);
                doorFirItem = new DoorItemBase(doorFirBlock, "fir_door_item", "door_fir");
                ((DoorBase) doorFirBlock).setDropItem(doorFirItem);

                doorEtherealBlock = new DoorBase(Material.wood, "door_ethereal_block", "door_ethereal", null);
                doorEtherealItem = new DoorItemBase(doorEtherealBlock, "ethereal_door_item", "door_ethereal");
                ((DoorBase) doorEtherealBlock).setDropItem(doorEtherealItem);

                doorMagicBlock = new DoorBase(Material.wood, "door_magic_block", "door_magic", null);
                doorMagicItem = new DoorItemBase(doorMagicBlock, "magic_door_item", "door_magic");
                ((DoorBase) doorMagicBlock).setDropItem(doorMagicItem);

                doorMangroveBlock = new DoorBase(Material.wood, "door_mangrove_block", "door_mangrove", null);
                doorMangroveItem = new DoorItemBase(doorMangroveBlock, "mangrove_door_item", "door_mangrove");
                ((DoorBase) doorMangroveBlock).setDropItem(doorMangroveItem);

                doorPalmBlock = new DoorBase(Material.wood, "door_palm_block", "door_palm", null);
                doorPalmItem = new DoorItemBase(doorPalmBlock, "palm_door_item", "door_palm");
                ((DoorBase) doorPalmBlock).setDropItem(doorPalmItem);

                doorRedWoodBlock = new DoorBase(Material.wood, "door_redwood_block", "door_redwood", null);
                doorRedWoodItem = new DoorItemBase(doorRedWoodBlock, "redwood_door_item", "door_redwood");
                ((DoorBase) doorRedWoodBlock).setDropItem(doorRedWoodItem);

                doorWillowBlock = new DoorBase(Material.wood, "door_willow_block", "door_willow", null);
                doorWillowItem = new DoorItemBase(doorWillowBlock, "willow_door_item", "door_willow");
                ((DoorBase) doorWillowBlock).setDropItem(doorWillowItem);

                doorBambooBlock = new DoorBase(Material.wood, "door_bamboo_block", "door_bamboo", null);
                doorBambooItem = new DoorItemBase(doorBambooBlock, "bamboo_door_item", "door_bamboo");
                ((DoorBase) doorBambooBlock).setDropItem(doorBambooItem);


                doorPineBlock = new DoorBase(Material.wood, "door_pine_block", "door_pine", null);
                doorPineItem = new DoorItemBase(doorPineBlock, "pine_door_item", "door_pine");
                ((DoorBase) doorPineBlock).setDropItem(doorPineItem);

                doorHellbarkBlock = new DoorBase(Material.wood, "door_hellbark_block", "door_hellbark", null);
                doorHellbarkItem = new DoorItemBase(doorHellbarkBlock, "hellbark_door_item", "door_hellbark");
                ((DoorBase) doorHellbarkBlock).setDropItem(doorHellbarkItem);

                doorJacarandaBlock = new DoorBase(Material.wood, "door_jacaranda_block", "door_jacaranda", null);
                doorJacarandaItem = new DoorItemBase(doorJacarandaBlock, "jacaranda_door_item", "door_jacaranda");
                ((DoorBase) doorJacarandaBlock).setDropItem(doorJacarandaItem);

                doorMahoganyBlock = new DoorBase(Material.wood, "door_mahogany_block", "door_mahogany", null);
                doorMahoganyItem = new DoorItemBase(doorMahoganyBlock, "mahogany_door_item", "door_mahogany");
                ((DoorBase) doorMahoganyBlock).setDropItem(doorMahoganyItem);

                doorItemsBoP = new Item[]{
                        doorSacredItem, doorCherryItem, doorDarkItem, doorFirItem,
                        doorEtherealItem, doorMagicItem, doorMangroveItem, doorPalmItem,
                        doorRedWoodItem, doorWillowItem, doorBambooItem, doorPineItem,
                        doorHellbarkItem, doorJacarandaItem, doorMahoganyItem
                };
            }

            if (IntegrationConfig.enableBoPTrapdoors) {
                trapdoorSacred = new TrapDoorBase(Material.wood, "trapdoor_sacred", "trapdoor_sacred");
                trapdoorCherry = new TrapDoorBase(Material.wood, "trapdoor_cherry", "trapdoor_cherry");
                trapdoorDark = new TrapDoorBase(Material.wood, "trapdoor_dark", "trapdoor_dark");
                trapdoorFir = new TrapDoorBase(Material.wood, "trapdoor_fir", "trapdoor_fir");
                trapdoorEthereal = new TrapDoorBase(Material.wood, "trapdoor_ethereal", "trapdoor_ethereal");
                trapdoorMagic = new TrapDoorBase(Material.wood, "trapdoor_magic", "trapdoor_magic");
                trapdoorMangrove = new TrapDoorBase(Material.wood, "trapdoor_mangrove", "trapdoor_mangrove");
                trapdoorPalm = new TrapDoorBase(Material.wood, "trapdoor_palm", "trapdoor_palm");
                trapdoorRedWood = new TrapDoorBase(Material.wood, "trapdoor_redwood", "trapdoor_redwood");
                trapdoorWillow = new TrapDoorBase(Material.wood, "trapdoor_willow", "trapdoor_willow");
                trapdoorBamboo = new TrapDoorBase(Material.wood, "trapdoor_bamboo", "trapdoor_bamboo");
                trapdoorPine = new TrapDoorBase(Material.wood, "trapdoor_pine", "trapdoor_pine");
                trapdoorHellbark = new TrapDoorBase(Material.wood, "trapdoor_hellbark", "trapdoor_hellbark");
                trapdoorJacaranda = new TrapDoorBase(Material.wood, "trapdoor_jacaranda", "trapdoor_jacaranda");
                trapdoorMahogany = new TrapDoorBase(Material.wood, "trapdoor_mahogany", "trapdoor_mahogany");

                trapdoorBlocksBoP = new Block[]{
                        trapdoorSacred, trapdoorCherry, trapdoorDark, trapdoorFir,
                        trapdoorEthereal, trapdoorMagic, trapdoorMangrove, trapdoorPalm,
                        trapdoorRedWood, trapdoorWillow, trapdoorBamboo, trapdoorPine,
                        trapdoorHellbark, trapdoorJacaranda, trapdoorMahogany
                };
            }
        }

        if (IntegrationConfig.enableVanillaDoors) {
            doorSpruceBlock = new DoorBase(Material.wood, "door_spruce_block", "door_spruce", null);
            doorSpruceItem = new DoorItemBase(doorSpruceBlock, "spruce_door_item", "door_spruce");
            ((DoorBase) doorSpruceBlock).setDropItem(doorSpruceItem);

            doorBirchBlock = new DoorBase(Material.wood, "door_birch_block", "door_birch", null);
            doorBirchItem = new DoorItemBase(doorBirchBlock, "birch_door_item", "door_birch");
            ((DoorBase) doorBirchBlock).setDropItem(doorBirchItem);

            doorJungleBlock = new DoorBase(Material.wood, "door_jungle_block", "door_jungle", null);
            doorJungleItem = new DoorItemBase(doorJungleBlock, "jungle_door_item", "door_jungle");
            ((DoorBase) doorJungleBlock).setDropItem(doorJungleItem);

            doorAcaciaBlock = new DoorBase(Material.wood, "door_acacia_block", "door_acacia", null);
            doorAcaciaItem = new DoorItemBase(doorAcaciaBlock, "acacia_door_item", "door_acacia");
            ((DoorBase) doorAcaciaBlock).setDropItem(doorAcaciaItem);

            doorDarkoakBlock = new DoorBase(Material.wood, "door_dark_oak_block", "door_dark_oak", null);
            doorDarkoakItem = new DoorItemBase(doorDarkoakBlock, "dark_oak_door_item", "door_dark_oak");
            ((DoorBase) doorDarkoakBlock).setDropItem(doorDarkoakItem);

            doorItems = new Item[]{
                    doorSpruceItem, doorBirchItem, doorJungleItem, doorAcaciaItem, doorDarkoakItem
            };
        }

        if (IntegrationConfig.enableVanillaTrapdoors) {
            trapdoorSpruce = new TrapDoorBase(Material.wood, "trapdoor_spruce", "trapdoor_spruce");
            trapdoorBirch = new TrapDoorBase(Material.wood, "trapdoor_birch", "trapdoor_birch");
            trapdoorJungle = new TrapDoorBase(Material.wood, "trapdoor_jungle", "trapdoor_jungle");
            trapdoorAcacia = new TrapDoorBase(Material.wood, "trapdoor_acacia", "trapdoor_acacia");
            trapdoorDarkoak = new TrapDoorBase(Material.wood, "trapdoor_darkoak", "trapdoor_darkoak");

            trapdoorBlocks = new Block[]{
                    trapdoorSpruce, trapdoorBirch, trapdoorJungle, trapdoorAcacia, trapdoorDarkoak
            };
        }

        registerCrafts();
    }

    private static void registerCrafts() {
        if (Loader.isModLoaded("BiomesOPlenty")) {
            Block bopPlanks = biomesoplenty.api.content.BOPCBlocks.planks;
            if (IntegrationConfig.enableBoPDoors && doorItemsBoP != null) {
                for (int i = 0; i < doorItemsBoP.length; i++) {
                    GameRegistry.addRecipe(new ItemStack(doorItemsBoP[i], 1),
                            "PP",
                            "PP",
                            "PP",
                            'P', new ItemStack(bopPlanks, 1, i)
                    );
                }
            }

            if (IntegrationConfig.enableBoPTrapdoors && trapdoorBlocksBoP != null) {
                for (int i = 0; i < trapdoorBlocksBoP.length; i++) {
                    GameRegistry.addRecipe(new ItemStack(trapdoorBlocksBoP[i], 2),
                            "PPP",
                            "PPP",
                            'P', new ItemStack(bopPlanks, 1, i)
                    );
                }
            }
        }

        int[] plankMetadata = {1, 2, 3, 4, 5};

        for (int i = 0; i < doorItems.length; i++) {
            GameRegistry.addRecipe(new ItemStack(doorItems[i], 1),
                    "PP",
                    "PP",
                    "PP",
                    'P', new ItemStack(Blocks.planks, 1, plankMetadata[i])
            );
        }

        for (int i = 0; i < trapdoorBlocks.length; i++) {
            GameRegistry.addRecipe(new ItemStack(trapdoorBlocks[i], 2),
                    "PPP",
                    "PPP",
                    'P', new ItemStack(Blocks.planks, 1, plankMetadata[i])
            );
        }
    }
}