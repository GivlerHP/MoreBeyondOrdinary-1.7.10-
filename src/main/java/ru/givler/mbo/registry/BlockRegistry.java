package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import ru.givler.mbo.block.*;
import ru.givler.mbo.block.craft.BlockArcanum;
import ru.givler.mbo.block.fauna.BlockPowderSnow;
import ru.givler.mbo.block.fauna.BlockSeagrass;
import ru.givler.mbo.block.fauna.BlockTurtleEgg;
import ru.givler.mbo.block.fauna.BlockFrogspawn;
import ru.givler.mbo.block.fauna.BlockFroglight;
import ru.givler.mbo.block.magic.BlockMagicSnare;
import ru.givler.mbo.block.magic.BlockPetrifiedStatue;
import ru.givler.mbo.block.magic.BlockTemporaryMagic;
import ru.givler.mbo.block.model.BlockModelCollision;
import ru.givler.mbo.block.special.*;
import ru.givler.mbo.item.ItemLeafLitter;
import ru.givler.mbo.item.ItemBlockMetadata;
import ru.givler.mbo.item.fauna.ItemTurtleEgg;
import ru.givler.mbo.item.fauna.ItemFrogspawn;
import ru.givler.mbo.movingplatform.BlockPlatformStation;

public class BlockRegistry {
    public static Block powderSnow, turtleEgg;
    public static Block frogspawn, froglight;
    public static BlockSeagrass seagrassBlock;

    private static void registerFaunaBlocks() {
        seagrassBlock = new BlockSeagrass();
        GameRegistry.registerBlock(seagrassBlock, "seagrass");
        turtleEgg = new BlockTurtleEgg();
        GameRegistry.registerBlock(turtleEgg, ItemTurtleEgg.class, "turtle_egg");
        powderSnow = new BlockPowderSnow();
        GameRegistry.registerBlock(powderSnow, "powder_snow");
        frogspawn = new BlockFrogspawn();
        GameRegistry.registerBlock(frogspawn, ItemFrogspawn.class, "frogspawn");
        froglight = new BlockFroglight();
        GameRegistry.registerBlock(froglight, ItemBlockMetadata.class, "froglight");
    }

    //переменные для блоков
    public static Block BlockGreyStone, BlockFogWhite, BlockFogGrey, RoofStandart, RoofUnfired, RoofLaminated, RoofSheet, RoofFlake, BlockGreyCobblestone,
            BlockSandstone, BlockStonebrick, BlockEndbrick, BlockGreyCobblesMossy,  BlockImperialBrick, BlockHeneizenBrick, BlockIrgadBrick,
            RoofWood, BlockFiredClay, BlockClayWall, BlockGlass, BlockAshgarBrick, BlockWoodenBox,
            BlockTuff, BlockChiseledTuff, BlockChiseledTuffBricks, BlockPrismarine, BlockSeaLantern,
            CoralBlock, DeadCoralBlock, SmoothStone;
    public static BlockBarrier Barrier;
    public static BlockBarrel Barrel;
    public static BlockCampfire Campfire;
    public static BlockMeta FramelessGlass, FramelessStainedGlass, FramelessTintedGlass;
    public static BlockObserver Observer;
    public static BlockLeafLitter LeafLitter;
    public static BlockSlimeMBO SlimeBlock;
    public static BlockBouncyMushroom BouncyBrownMushroomBlock, BouncyRedMushroomBlock;
    public static BlockModelCollision ModelCollisionPart;
    //переменные для ступенек
    public static Block  StairsStone, StairsSandstone, StairsStonebrick, StairsIrgadBrick, StairsGreyCobblestone,
            StairsEndbrick,  StairsImperialBrick, StairsHeneizenBrick, StairsFiredClay, StairsAshgarBrick,
            StairsDirt;
    public static BlockBasicStairs StairsSmoothStone;
    //переменные для ступенек с методанными
    public static Block[] StairsRoofLaminated, StairsRoofStandart, StairsRoofSheet, StairsRoofFlake, StairsRoofWood,
            StairsTuff, StairsPrismarine;
    //переменные для плит
    public static BlockBasicSlab  SlabStone, SlabCobblestone, SlabStonebrick, SlabSandstone, SlabIrgadBrick, SlabEndbrick,
             SlabImperialBrick, SlabHeneizenBrick, SlabFiredClay, SlabAshgarBrick, SlabSmoothStone, SlabDirt;
    //переменные для плит с метаданными
    public static BlockMetaSlab[] SlabRoofLaminated, SlabRoofStandart, SlabRoofSheet, SlabRoofFlake, SlabRoofWood,
            SlabTuff, SlabPrismarine;
    //переменные для мультитекстурных блоков
    public static Block BooksheelSkull, BooksheelVoid, BooksheelWeb, BooksheelCandle, BooksheelSkullWeb, BooksheelSkullCandle,
            TotemStone, DebarkedOak, DebarkedSpruce, DebarkedBirch, DebarkedJungle, DebarkedAcacia, DebarkedBigOak,
            WoodTotemOak, WoodTotemSpruce, WoodTotemBirch, WoodTotemJungle, WoodTotemAcacia, WoodTotemBigOak;
    //переменные для ограды
    public static Block WallStonebrick, WallSandstone, WallVanillaSandstone, WallAshgarBrick, WallFiredClay, WallVanillaStonebrick, WallVanillaBrick, WallSmoothStone;
    public static Block[] WallTuff;
    public static Block[] WallPrismarine;
    public static BlockBasicFence FenceVanilla;
    public static BlockBasicFenceGate FenceGateSpruce, FenceGateBirch, FenceGateJungle, FenceGateAcacia, FenceGateDarkOak;
    //переменные для крафтовых блоков
    public static Block MagicFurnace;
    public static Block MagicSnare;
    public static Block MeteorBlock;
    public static BlockPetrifiedStatue PetrifiedStatue, FrozenStatue;
    public static BlockTemporaryMagic TemporaryCobweb, TemporaryFrost, TemporaryLight, TemporarySpectral;
    public static Block LockableChest, LockableTrapdoor, IronTrapdoor;
    public static BlockMeta CopperBlock, CutCopper, ChiseledCopper, CopperGrate;
    public static BlockMetaStairs[] CutCopperStairs;
    public static BlockMetaSlab[] CutCopperSlabs, CutCopperDoubleSlabs;
    public static TrapDoorBase[] CopperTrapdoors;
    public static DoorBase[] CopperDoors;
    public static BlockCopperBulb[] CopperBulbs;
    public static BlockLockableDoor LockableDoor;
    public static BlockPlatformStation PlatformStation;
    public static BlockInvertedDaylightDetector InvertedDaylightDetector;
    public static BlockColourFire ColourFire, ColourFireVanilla;

    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        registerFaunaBlocks();
        // Keep vanilla attenuation so grass cannot spread beneath water.
        Blocks.water.setLightOpacity(3);
        Blocks.flowing_water.setLightOpacity(3);
        PlatformStation = new BlockPlatformStation();
        ColourFire = new BlockColourFire(true);
        ColourFireVanilla = new BlockColourFire(false);
        GameRegistry.registerBlock(ColourFire, "ColourFire");
        GameRegistry.registerBlock(ColourFireVanilla, "ColourFireVanilla");
        InvertedDaylightDetector = new BlockInvertedDaylightDetector();
        GameRegistry.registerBlock(InvertedDaylightDetector, "InvertedDaylightDetector");
        BlockGreyStone = new BlockBase(Material.rock, "BlockGreyStone", "stone/stone");
        BlockGreyCobblestone = new BlockBase(Material.rock, "BlockGreyCobblestone", "stone/cobblestone");
        BlockEndbrick = new BlockBase(Material.rock, "BlockEndbrick", "stone/end_bricks");
        BlockGreyCobblesMossy = new BlockBase(Material.rock, "BlockGreyCobblesMossy", "stone/cobblestone_mossy");

        BlockImperialBrick = new BlockBase(Material.rock, "BlockImperialBrick", "stone/imperial_brick");
        BlockHeneizenBrick = new BlockBase(Material.rock, "BlockHeneizenBrick", "stone/heneizen_brick");
        BlockIrgadBrick = new BlockBase(Material.rock, "BlockIrgadBrick", "stone/irgad_brick");
        BlockFiredClay = new BlockBase(Material.rock, "BlockFiredClay", "stone/brick_firedclay");
        BlockClayWall = new BlockBase(Material.wood, "BlockClayWall", "wood/clay_wall_old").setStepSound(Block.soundTypeWood);
        BlockAshgarBrick = new BlockBase(Material.rock, "BlockAshgarBrick", "stone/ashgar_brick");

        BlockWoodenBox = new BlockBase(Material.wood, "BlockWoodenBox", "wood/wooden_box").setStepSound(Block.soundTypeWood);
        Barrel = new BlockBarrel();
        GameRegistry.registerBlock(Barrel, "Barrel");
        Campfire = new BlockCampfire();
        GameRegistry.registerBlock(Campfire, "Campfire");
        Observer = new BlockObserver();
        GameRegistry.registerBlock(Observer, "Observer");
        LeafLitter = new BlockLeafLitter();
        GameRegistry.registerBlock(LeafLitter, ItemLeafLitter.class, "LeafLitter");
        ModelCollisionPart = new BlockModelCollision();
        GameRegistry.registerBlock(ModelCollisionPart, "ModelCollisionPart");
        Barrier = new BlockBarrier();
        LockableChest = new BlockLockableChest();
        LockableDoor = new BlockLockableDoor();
        LockableTrapdoor = new BlockLockableTrapdoor();
        IronTrapdoor = new TrapDoorBase(Material.iron, "IronTrapdoor", "iron_trapdoor")
                .requireRedstone("mbo:iron_trapdoor.open", "mbo:iron_trapdoor.close");
        CopperBlock = new BlockMeta(Material.iron, "CopperBlock", copperTextures(
                "copper_block", "exposed_copper", "weathered_copper", "oxidized_copper")).withCopper(false);
        CutCopper = new BlockMeta(Material.iron, "CutCopper", copperTextures(
                "cut_copper", "exposed_cut_copper", "weathered_cut_copper", "oxidized_cut_copper")).withCopper(false);
        ChiseledCopper = new BlockMeta(Material.iron, "ChiseledCopper", copperTextures(
                "chiseled_copper", "exposed_chiseled_copper", "weathered_chiseled_copper", "oxidized_chiseled_copper")).withCopper(false);
        CopperGrate = new BlockMeta(Material.iron, "CopperGrate", copperTextures(
                "copper_grate", "exposed_copper_grate", "weathered_copper_grate", "oxidized_copper_grate")).withCopper(true);
        for (BlockMeta copper : new BlockMeta[] {CopperBlock, CutCopper, ChiseledCopper, CopperGrate}) {
            copper.setHardness(3.0F);
            copper.setResistance(6.0F);
            copper.setHarvestLevel("pickaxe", 1);
            copper.setLightOpacity(copper == CopperGrate ? 0 : 255);
        }
        CutCopperStairs = new BlockMetaStairs[12];
        for (int stage = 0; stage < 4; stage++) {
            CutCopperStairs[stage] = new BlockMetaStairs(CutCopper, stage,
                    "CutCopperStairs" + stage, false).withCopper(stage);
            CutCopperStairs[stage + 8] = new BlockMetaStairs(CutCopper, stage,
                    "CutCopperStairs" + (stage + 8), false).withCopper(stage + 8);
        }
        CutCopperDoubleSlabs = new BlockMetaSlab[12];
        CutCopperSlabs = BlockMetaSlab.registerCopperSlabs(CutCopper, CutCopperDoubleSlabs,
                new String[] {"copper/cut_copper", "copper/exposed_cut_copper",
                        "copper/weathered_cut_copper", "copper/oxidized_cut_copper"});
        CopperTrapdoors = new TrapDoorBase[12];
        String[] copperTrapdoorTextures = {"copper_trapdoor", "exposed_copper_trapdoor",
                "weathered_copper_trapdoor", "oxidized_copper_trapdoor"};
        for (int stage = 0; stage < 4; stage++) {
            CopperTrapdoors[stage] = new TrapDoorBase(Material.iron, "CopperTrapdoor" + stage,
                    "mbo:copper/" + copperTrapdoorTextures[stage]).withCopper(stage, CopperBlock.stepSound);
            CopperTrapdoors[stage + 8] = new TrapDoorBase(Material.iron, "CopperTrapdoor" + (stage + 8),
                    "mbo:copper/" + copperTrapdoorTextures[stage]).withCopper(stage + 8, CopperBlock.stepSound);
        }
        CopperDoors = new DoorBase[12];
        String[] copperDoorTextures = {"copper_door", "exposed_copper_door",
                "weathered_copper_door", "oxidized_copper_door"};
        for (int stage = 0; stage < 4; stage++) {
            CopperDoors[stage] = new DoorBase(Material.iron, "CopperDoor" + stage,
                    "mbo:copper/" + copperDoorTextures[stage], null).withCopper(stage, CopperBlock.stepSound);
            CopperDoors[stage + 8] = new DoorBase(Material.iron, "CopperDoor" + (stage + 8),
                    "mbo:copper/" + copperDoorTextures[stage], null).withCopper(stage + 8, CopperBlock.stepSound);
        }
        CopperBulbs = new BlockCopperBulb[12];
        for (int stage = 0; stage < 4; stage++) {
            CopperBulbs[stage] = new BlockCopperBulb(stage);
            CopperBulbs[stage + 8] = new BlockCopperBulb(stage + 8);
        }
        OreDictionary.registerOre("trapdoorIron", new ItemStack(IronTrapdoor));
        SlimeBlock = new BlockSlimeMBO();
        GameRegistry.registerBlock(SlimeBlock, "SlimeBlock");
        BouncyBrownMushroomBlock = BlockBouncyMushroom.brown();
        BouncyRedMushroomBlock = BlockBouncyMushroom.red();
        GameRegistry.registerBlock(BouncyBrownMushroomBlock, "BouncyBrownMushroomBlock");
        GameRegistry.registerBlock(BouncyRedMushroomBlock, "BouncyRedMushroomBlock");

        RoofStandart = new BlockMeta(Material.rock, "StandartRoof", "roof/roofk", 3);
        RoofUnfired = new BlockMeta(Material.clay, "UnfiredRoof", "roof/roofu", 3).setStepSound(Block.soundTypeGravel);
        RoofLaminated = new BlockMeta(Material.rock, "LaminatedRoof", "roof/roof1", 16);
        RoofSheet = new BlockMeta(Material.rock, "SheetRoof", "roof/roof2", 16);
        RoofFlake = new BlockMeta(Material.rock, "FlakeRoof", "roof/roof3", 16);
        RoofWood = new BlockMeta(Material.wood, "RoofWood", "wood/roofwood", 6).setStepSound(Block.soundTypeWood);

        BlockSandstone = new BlockMeta(Material.rock, "BlockSandstone", "stone/sandstone", 3);
        BlockStonebrick = new BlockMeta(Material.rock, "BlockStonebrick", "stone/stonebrick", 4);
        BlockTuff = new BlockMeta(Material.rock, "BlockTuff", "stone/tuff", 3);
        BlockChiseledTuff = new BlockMultiTexture(Material.rock, "BlockChiseledTuff",
                "stone/chiseled_tuff_top", "stone/chiseled_tuff");
        BlockChiseledTuffBricks = new BlockMultiTexture(Material.rock, "BlockChiseledTuffBricks",
                "stone/chiseled_tuff_bricks_top", "stone/chiseled_tuff_bricks");
        BlockPrismarine = new BlockMeta(Material.rock, "BlockPrismarine", new String[] {
                "stone/prismarine", "stone/prismarine_bricks", "stone/dark_prismarine"
        });
        BlockSeaLantern = new BlockBase(Material.glass, "BlockSeaLantern", "stone/sea_lantern")
                .setHardness(0.3F).setResistance(1.5F).setLightLevel(1.0F)
                .setStepSound(Block.soundTypeGlass);
        DeadCoralBlock = new BlockMeta(Material.rock, "DeadCoralBlock", new String[] {
                "coral/dead_tube_coral_block", "coral/dead_brain_coral_block",
                "coral/dead_bubble_coral_block", "coral/dead_fire_coral_block",
                "coral/dead_horn_coral_block"
        }).setHardness(1.5F).setResistance(6.0F);
        DeadCoralBlock.setHarvestLevel("pickaxe", 0);
        CoralBlock = new BlockCoral("CoralBlock", new String[] {
                "coral/tube_coral_block", "coral/brain_coral_block",
                "coral/bubble_coral_block", "coral/fire_coral_block",
                "coral/horn_coral_block"
        }, DeadCoralBlock);
        SmoothStone = new BlockSmoothStone();

        BooksheelSkull = new BlockMultiTexture(Material.wood, "BooksheelSkull", "wood/planks_oak", "wood/bookshelf_skull")
                .setStepSound(Block.soundTypeWood);
        BooksheelVoid = new BlockMultiTexture(Material.wood, "BooksheelVoid", "wood/planks_oak", "wood/bookshelf_void")
                .setStepSound(Block.soundTypeWood);
        BooksheelWeb = new BlockMultiTexture(Material.wood, "BooksheelWeb", "wood/planks_oak", "wood/bookshelf_web")
                .setStepSound(Block.soundTypeWood);
        BooksheelCandle = new BlockMultiTexture(Material.wood, "BooksheelCandle", "wood/planks_oak", "wood/bookshelf_candle")
                .setLightLevel(0.75F).setStepSound(Block.soundTypeWood);
        BooksheelSkullWeb = new BlockMultiTexture(Material.wood, "BooksheelSkullWeb", "wood/planks_oak", "wood/bookshelf_skull_web")
                .setStepSound(Block.soundTypeWood);
        BooksheelSkullCandle = new BlockMultiTexture(Material.wood, "BooksheelSkullCandle", "wood/planks_oak", "wood/bookshelf_skull_candle")
                .setLightLevel(0.75F).setStepSound(Block.soundTypeWood);


        TotemStone = new BlockMultiTexture(Material.rock, "TotemStone", "stone/stone_slab_top", "stone/totem_truesight");

        DebarkedOak = new BlockRotatableWood("DebarkedOak", "wood/log_oak_top", "wood/scratched_log_oak_side");
        DebarkedSpruce = new BlockRotatableWood("DebarkedSpruce", "wood/log_spruce_top", "wood/scratched_log_spruce_side");
        DebarkedBirch = new BlockRotatableWood("DebarkedBirch", "wood/log_birch_top", "wood/scratched_log_birch_side");
        DebarkedJungle = new BlockRotatableWood("DebarkedJungle", "wood/log_jungle_top", "wood/scratched_log_jungle_side");
        DebarkedAcacia = new BlockRotatableWood("DebarkedAcacia", "wood/log_acacia_top", "wood/scratched_log_acacia_side");
        DebarkedBigOak = new BlockRotatableWood("DebarkedBigOak", "wood/log_big_oak_top", "wood/scratched_log_dark_oak_side");

        WoodTotemOak = new BlockRotatableWood("WoodTotemOak", "wood/log_oak_top", "wood/carved_log_oak_side");
        WoodTotemSpruce = new BlockRotatableWood("WoodTotemSpruce", "wood/log_spruce_top", "wood/carved_log_spruce_side");
        WoodTotemBirch = new BlockRotatableWood("WoodTotemBirch", "wood/log_birch_top", "wood/carved_log_birch_side");
        WoodTotemJungle = new BlockRotatableWood("WoodTotemJungle", "wood/log_jungle_top", "wood/carved_log_jungle_side");
        WoodTotemAcacia = new BlockRotatableWood("WoodTotemAcacia", "wood/log_acacia_top", "wood/carved_log_acacia_side");
        WoodTotemBigOak = new BlockRotatableWood("WoodTotemBigOak", "wood/log_big_oak_top", "wood/carved_log_dark_oak_side");

        BlockFogWhite = new BlockFog(Material.web, "BlockFogWhite", "another/fogwhite");
        BlockFogGrey = new BlockFogGrey(Material.web, "BlockFogGrey", "another/foggrey");
        BlockGlass = new BlockTemporaryGlass("BlockGlass");
        FramelessGlass = new BlockMeta(Material.glass, "FramelessGlass",
                new String[] {"glass/glass"}).withGlass(false);
        FramelessStainedGlass = new BlockMeta(Material.glass, "FramelessStainedGlass",
                new String[] {"glass/white_stained_glass", "glass/orange_stained_glass",
                        "glass/magenta_stained_glass", "glass/light_blue_stained_glass",
                        "glass/yellow_stained_glass", "glass/lime_stained_glass",
                        "glass/pink_stained_glass", "glass/gray_stained_glass",
                        "glass/light_gray_stained_glass", "glass/cyan_stained_glass",
                        "glass/purple_stained_glass", "glass/blue_stained_glass",
                        "glass/brown_stained_glass", "glass/green_stained_glass",
                        "glass/red_stained_glass", "glass/black_stained_glass"}).withGlass(false);
        FramelessTintedGlass = new BlockMeta(Material.glass, "FramelessTintedGlass",
                new String[] {"glass/tinted_glass"}).withGlass(true);
        for (BlockMeta glass : new BlockMeta[] {FramelessGlass, FramelessStainedGlass, FramelessTintedGlass}) {
            glass.setHardness(0.3F);
            glass.setResistance(1.5F);
            glass.setStepSound(Block.soundTypeGlass);
            glass.setLightOpacity(glass == FramelessTintedGlass ? 255 : 0);
        }

        MagicFurnace = new BlockArcanum(Material.rock, "MagicFurnace");
        MagicSnare = new BlockMagicSnare();
        MeteorBlock = new Block(Material.rock) {};
        MeteorBlock.setBlockName("meteor_block").setBlockTextureName("mbo:magic/meteor").setLightLevel(1.0F);
        GameRegistry.registerBlock(MagicSnare, "magic_snare");
        GameRegistry.registerBlock(MeteorBlock, "meteor_block");
        PetrifiedStatue = new BlockPetrifiedStatue();
        FrozenStatue = new BlockPetrifiedStatue(true);
        GameRegistry.registerBlock(PetrifiedStatue, "petrified_statue");
        GameRegistry.registerBlock(FrozenStatue, "frozen_statue");
        TemporaryCobweb = new BlockTemporaryMagic(BlockTemporaryMagic.Kind.COBWEB);
        TemporaryFrost = new BlockTemporaryMagic(BlockTemporaryMagic.Kind.FROST);
        TemporaryLight = new BlockTemporaryMagic(BlockTemporaryMagic.Kind.LIGHT);
        TemporarySpectral = new BlockTemporaryMagic(BlockTemporaryMagic.Kind.SPECTRAL);
        GameRegistry.registerBlock(TemporaryCobweb, "temporary_cobweb");
        GameRegistry.registerBlock(TemporaryFrost, "temporary_frost");
        GameRegistry.registerBlock(TemporaryLight, "temporary_light");
        GameRegistry.registerBlock(TemporarySpectral, "temporary_spectral");

        //НИЖЕ НАХОДИТСЯ СТУПЕНЬКИ
        StairsStone = new BlockBasicStairs((BlockBase) BlockGreyStone);
        StairsGreyCobblestone = new BlockBasicStairs((BlockBase) BlockGreyCobblestone);
        ;
        StairsIrgadBrick = new BlockBasicStairs((BlockBase) BlockIrgadBrick);
        StairsEndbrick = new BlockBasicStairs((BlockBase) BlockEndbrick);
        StairsImperialBrick = new BlockBasicStairs((BlockBase) BlockImperialBrick);
        StairsHeneizenBrick = new BlockBasicStairs((BlockBase) BlockHeneizenBrick);
        StairsFiredClay = new BlockBasicStairs((BlockBase) BlockFiredClay);
        StairsAshgarBrick = new BlockBasicStairs((BlockBase) BlockAshgarBrick);
        StairsSmoothStone = new BlockBasicStairs(SmoothStone);
        StairsDirt = new BlockBasicStairs(Blocks.dirt);


        StairsSandstone = new BlockMetaStairs((BlockMeta) BlockSandstone, 0);
        StairsStonebrick = new BlockMetaStairs((BlockMeta) BlockStonebrick, 0);

        StairsRoofStandart = BlockMetaStairs.registerStairs((BlockMeta) RoofStandart, 3);
        StairsRoofLaminated = BlockMetaStairs.registerStairs((BlockMeta) RoofLaminated, 16);
        StairsRoofSheet = BlockMetaStairs.registerStairs((BlockMeta) RoofSheet, 16);
        StairsRoofFlake = BlockMetaStairs.registerStairs((BlockMeta) RoofFlake, 16);
        StairsRoofWood = BlockMetaStairs.registerStairs((BlockMeta) RoofWood, 6);
        StairsTuff = BlockMetaStairs.registerStairs((BlockMeta) BlockTuff, 3);
        StairsPrismarine = BlockMetaStairs.registerStairs((BlockMeta) BlockPrismarine, 3);


        //НИЖЕ НАХОДИТСЯ ПОЛУБЛОКИe
        SlabCobblestone = BlockBasicSlab.registerPair("SlabCobblestone", "stone/cobblestone");
        SlabStone = BlockBasicSlab.registerPair("SlabStone", "stone/stone");
        SlabStonebrick = BlockBasicSlab.registerPair("SlabStonebrick", "stone/stonebrick_0");
        SlabSandstone = BlockBasicSlab.registerPair("SlabSandstone", "stone/sandstone_0");

        SlabIrgadBrick = BlockBasicSlab.registerPair("SlabIrgadBrick", "stone/irgad_brick");
        SlabEndbrick = BlockBasicSlab.registerPair("SlabEndbrick", "stone/end_bricks");
        SlabImperialBrick = BlockBasicSlab.registerPair("SlabImperialBrick", "stone/imperial_brick");
        SlabHeneizenBrick = BlockBasicSlab.registerPair("SlabHeneizenBrick", "stone/heneizen_brick");
        SlabAshgarBrick = BlockBasicSlab.registerPair("SlabAshgarBrick", "stone/ashgar_brick");
        SlabFiredClay = BlockBasicSlab.registerPair("SlabFiredClay", "stone/brick_firedclay");
        SlabSmoothStone = BlockBasicSlab.registerPair("SlabSmoothStone", "minecraft:stone_slab_top");
        SlabDirt = BlockBasicSlab.registerPair("SlabDirt", "minecraft:dirt", Material.ground);

        SlabRoofStandart  = BlockMetaSlab.registerSlabs((BlockMeta) RoofStandart,  3,  "roof/roofk");
        SlabRoofLaminated = BlockMetaSlab.registerSlabs((BlockMeta) RoofLaminated, 16, "roof/roof1");
        SlabRoofSheet     = BlockMetaSlab.registerSlabs((BlockMeta) RoofSheet,     16, "roof/roof2");
        SlabRoofFlake     = BlockMetaSlab.registerSlabs((BlockMeta) RoofFlake,     16, "roof/roof3");
        SlabRoofWood      = BlockMetaSlab.registerSlabs((BlockMeta) RoofWood,       6, "wood/roofwood");
        SlabTuff          = BlockMetaSlab.registerSlabs((BlockMeta) BlockTuff,       3, "stone/tuff");
        SlabPrismarine    = BlockMetaSlab.registerSlabs((BlockMeta) BlockPrismarine, new String[] {
                "stone/prismarine", "stone/prismarine_bricks", "stone/dark_prismarine"
        });


        //НИЖЕ НАХОИДТСЯ ЗАБОР
        WallStonebrick = new BlockBasicWall(BlockStonebrick, "WallStonebrick", "stone/stonebrick_0");
        WallSandstone = new BlockBasicWall(BlockSandstone, "WallSandstone", "stone/sandstone_0");
        WallVanillaSandstone = new BlockBasicWall(
                Blocks.sandstone, "WallVanillaSandstone", "minecraft:sandstone_normal");
        WallAshgarBrick = new BlockBasicWall(
                BlockAshgarBrick, "WallAshgarBrick", "stone/ashgar_brick");
        WallFiredClay = new BlockBasicWall(BlockFiredClay, "WallFiredClay", "stone/brick_firedclay");
        WallVanillaStonebrick = new BlockBasicWall(
                Blocks.stonebrick, "WallVanillaStonebrick", "minecraft:stonebrick");
        WallVanillaBrick = new BlockBasicWall(
                Blocks.brick_block, "WallVanillaBrick", "minecraft:brick");
        WallSmoothStone = new BlockBasicWall(
                SmoothStone, "WallSmoothStone", "minecraft:stone_slab_top");
        WallTuff = new Block[] {
                new BlockBasicWall(BlockTuff, 0, "WallTuff", "stone/tuff_0"),
                new BlockBasicWall(BlockTuff, 1, "WallSmoothTuff", "stone/tuff_1"),
                new BlockBasicWall(BlockTuff, 2, "WallTuffBricks", "stone/tuff_2")
        };
        WallPrismarine = new Block[] {
                new BlockBasicWall(BlockPrismarine, 0, "WallPrismarine", "stone/prismarine"),
                new BlockBasicWall(BlockPrismarine, 1, "WallPrismarineBricks", "stone/prismarine_bricks"),
                new BlockBasicWall(BlockPrismarine, 2, "WallDarkPrismarine", "stone/dark_prismarine")
        };

        FenceVanilla = new BlockBasicFence("FenceVanilla", Blocks.planks, 1, 2, 3, 4, 5);

        FenceGateSpruce = new BlockBasicFenceGate("FenceGateSpruce", Blocks.planks, 1);
        FenceGateBirch = new BlockBasicFenceGate("FenceGateBirch", Blocks.planks, 2);
        FenceGateJungle = new BlockBasicFenceGate("FenceGateJungle", Blocks.planks, 3);
        FenceGateAcacia = new BlockBasicFenceGate("FenceGateAcacia", Blocks.planks, 4);
        FenceGateDarkOak = new BlockBasicFenceGate("FenceGateDarkOak", Blocks.planks, 5);

    }

    private static String[] copperTextures(String fresh, String exposed, String weathered, String oxidized) {
        String[] stages = {fresh, exposed, weathered, oxidized};
        String[] textures = new String[12];
        for (int i = 0; i < textures.length; i++)
            textures[i] = "copper/" + stages[i & 3];
        return textures;
    }

    public static void initRecipe() {
        BlockMetaSlab.addStandardRecipes(SlabRoofStandart,  (BlockMeta) RoofStandart);
        BlockMetaSlab.addStandardRecipes(SlabRoofLaminated, (BlockMeta) RoofLaminated);
        BlockMetaSlab.addStandardRecipes(SlabRoofSheet,     (BlockMeta) RoofSheet);
        BlockMetaSlab.addStandardRecipes(SlabRoofFlake,     (BlockMeta) RoofFlake);
        BlockMetaSlab.addStandardRecipes(SlabRoofWood,      (BlockMeta) RoofWood);
        BlockBasicSlab.addStandardRecipes(SlabCobblestone,   BlockGreyCobblestone);
        BlockBasicSlab.addStandardRecipes(SlabStone,         BlockGreyStone);
        BlockBasicSlab.addStandardRecipes(SlabStonebrick,    BlockStonebrick);
        BlockBasicSlab.addStandardRecipes(SlabSandstone,     BlockSandstone);

        BlockBasicSlab.addStandardRecipes(SlabIrgadBrick,    BlockIrgadBrick);
        BlockBasicSlab.addStandardRecipes(SlabEndbrick,      BlockEndbrick);
        BlockBasicSlab.addStandardRecipes(SlabImperialBrick, BlockImperialBrick);
        BlockBasicSlab.addStandardRecipes(SlabHeneizenBrick, BlockHeneizenBrick);
        BlockBasicSlab.addStandardRecipes(SlabAshgarBrick, BlockAshgarBrick);
        BlockBasicSlab.addStandardRecipes(SlabFiredClay,     BlockFiredClay);
        BlockBasicSlab.addStandardRecipes(SlabSmoothStone,   SmoothStone);
        BlockBasicSlab.addStandardRecipes(SlabDirt,           Blocks.dirt);

        GameRegistry.addRecipe(new ItemStack(SlimeBlock),
                new Object[]{"SSS", "SSS", "SSS", 'S', Items.slime_ball});
        GameRegistry.addShapelessRecipe(new ItemStack(Items.slime_ball, 9), SlimeBlock);

        FenceVanilla.addStandardRecipes();

        FenceGateSpruce.addStandardRecipe();
        FenceGateBirch.addStandardRecipe();
        FenceGateJungle.addStandardRecipe();
        FenceGateAcacia.addStandardRecipe();
        FenceGateDarkOak.addStandardRecipe();
    }
}
