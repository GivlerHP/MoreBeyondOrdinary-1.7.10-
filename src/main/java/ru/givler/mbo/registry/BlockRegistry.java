package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import ru.givler.mbo.block.*;
import ru.givler.mbo.block.specialblocks.*;

public class BlockRegistry {
    //переменные для блоков
    public static Block BlockGreyStone, BlockFogWhite, BlockFogGrey, RoofStandart, RoofLaminated, RoofSheet, RoofFlake, BlockGreyCobblestone, BlockSandstone,
            BlockStonebrick, BlockEndbrick, BlockGreyCobblesMossy,  BlockImperialBrick, BlockHeneizenBrick, BlockIrgadBrick, RoofWood;
    //переменные для ступенек
    public static Block StairsStone, StairsSandstine, StairsStonebrick, StairsIrgadBrick, StairsGreyCobblestone,
            StairsEndbrick,  StairsImperialBrick, StaisrHeneizenBrick;
    //переменные для плит
    public static Block SlabStone, SlabCobblestone, SlabStonebrick, SlabSandstone, SlabIrgadBrick, SlabEndbrick,
             SlabImperialBrick, SlabHeneizenBrick;
    //переменные для мультитекстурных блоков
    public static Block BooksheelSkull, TotemStone, DebarkedOak, DebarkedSpruce, DebarkedBirch, DebarkedJungle, DebarkedAcacia, DebarkedBigOak,
            WoodTotemOak, WoodTotemSpruce, WoodTotemBirch, WoodTotemJungle, WoodTotemAcacia, WoodTotemBigOak;
    public static Block WallStonebrick, WallSandstone;
    @Mod.EventHandler
    public static void preLoad(FMLInitializationEvent event) {
        BlockGreyStone = new BlockBasic(Material.rock, "BlockGreyStone", "stone/stone");
        BlockGreyCobblestone = new BlockBasic(Material.rock, "BlockGreyCobblestone", "stone/cobblestone");
        BlockEndbrick = new BlockBasic(Material.rock, "BlockEndbrick", "stone/end_bricks");
        BlockGreyCobblesMossy = new BlockBasic(Material.rock, "BlockGreyCobblesMossy", "stone/cobblestone_mossy");

        BlockImperialBrick = new BlockBasic(Material.rock, "BlockImperialBrick", "stone/imperial_brick");
        BlockHeneizenBrick = new BlockBasic(Material.rock, "BlockHeneizenBrick", "stone/heneizen_brick");
        BlockIrgadBrick = new BlockBasic(Material.rock, "BlockIrgadBrick", "stone/irgad_brick");

        RoofStandart = new BlockMeta(Material.rock, "StandartRoof", "roof/roofk", 3);
        RoofLaminated = new BlockMeta(Material.rock, "LaminatedRoof", "roof/roof1", 16);
        RoofSheet = new BlockMeta(Material.rock, "SheetRoof", "roof/roof2", 16);
        RoofFlake = new BlockMeta(Material.rock, "FlakeRoof", "roof/roof3", 16);
        RoofWood = new BlockMeta(Material.wood, "RoofWood", "wood/roofwood", 6).setStepSound(Block.soundTypeWood);

        BlockSandstone = new BlockMeta(Material.rock, "BlockSandstone", "stone/sandstone", 3);
        BlockStonebrick = new BlockMeta(Material.rock, "BlockStonebrick", "stone/stonebrick", 4);

        BooksheelSkull = new BlockMultiTexture(Material.wood, "BooksheelSkull", "wood/planks_oak", "wood/bookshelf1").setLightLevel(0.5F);
        TotemStone = new BlockMultiTexture(Material.rock, "TotemStone", "stone/stone_slab_top", "stone/totem_truesight");

        DebarkedOak = new BlockRotatableWood("DebarkedOak", "wood/log_oak_top",  "wood/scratched_log_oak_side");
        DebarkedSpruce = new BlockRotatableWood("DebarkedSpruce", "wood/log_spruce_top",  "wood/scratched_log_spruce_side");
        DebarkedBirch = new BlockRotatableWood("DebarkedBirch", "wood/log_birch_top",  "wood/scratched_log_birch_side");
        DebarkedJungle = new BlockRotatableWood("DebarkedJungle", "wood/log_jungle_top",  "wood/scratched_log_jungle_side");
        DebarkedAcacia = new BlockRotatableWood("DebarkedAcacia", "wood/log_acacia_top",  "wood/scratched_log_acacia_side");
        DebarkedBigOak = new BlockRotatableWood("DebarkedBigOak", "wood/log_big_oak_top",  "wood/scratched_log_dark_oak_side");

        WoodTotemOak = new BlockRotatableWood("WoodTotemOak", "wood/log_oak_top",  "wood/carved_log_oak_side");
        WoodTotemSpruce = new BlockRotatableWood("WoodTotemSpruce", "wood/log_spruce_top",  "wood/carved_log_spruce_side");
        WoodTotemBirch = new BlockRotatableWood("WoodTotemBirch", "wood/log_birch_top",  "wood/carved_log_birch_side");
        WoodTotemJungle = new BlockRotatableWood("WoodTotemJungle", "wood/log_jungle_top",  "wood/carved_log_jungle_side");
        WoodTotemAcacia = new BlockRotatableWood("WoodTotemAcacia", "wood/log_acacia_top",  "wood/carved_log_acacia_side");
        WoodTotemBigOak = new BlockRotatableWood("WoodTotemBigOak", "wood/log_big_oak_top",  "wood/carved_log_dark_oak_side");

        BlockFogWhite = new BlockFog(Material.web, "BlockFogWhite", "another/fogwhite");
        BlockFogGrey = new BlockFogGrey(Material.web, "BlockFogGrey", "another/foggrey");

        //НИЖЕ НАХОДИТСЯ СТУПЕНЬКИ
        StairsStone = new BlockBasicStairs((BlockBasic) BlockGreyStone);
        StairsGreyCobblestone = new BlockBasicStairs((BlockBasic) BlockGreyCobblestone);;
        StairsIrgadBrick = new BlockBasicStairs((BlockBasic) BlockIrgadBrick);
        StairsEndbrick = new BlockBasicStairs((BlockBasic) BlockEndbrick);
        StairsImperialBrick = new BlockBasicStairs((BlockBasic) BlockImperialBrick);
        StaisrHeneizenBrick = new BlockBasicStairs((BlockBasic) BlockHeneizenBrick);

        StairsSandstine = new BlockMetaStairs((BlockMeta) BlockSandstone, 0);
        StairsStonebrick = new BlockMetaStairs((BlockMeta) BlockStonebrick, 0);

        BlockMetaStairs.registerStairs((BlockMeta) RoofStandart, 3);
        BlockMetaStairs.registerStairs((BlockMeta) RoofLaminated, 16);
        BlockMetaStairs.registerStairs((BlockMeta) RoofSheet, 16);
        BlockMetaStairs.registerStairs((BlockMeta) RoofFlake, 16);
        BlockMetaStairs.registerStairs((BlockMeta) RoofWood, 6);


        //НИЖЕ НАХОДИТСЯ ПОЛУБЛОКИe
        SlabCobblestone = new BlockBasicSlab(false,"SlabCobblestone", "stone/cobblestone");
        SlabStone = new BlockBasicSlab(false,"SlabStone", "stone/stone");
        SlabStonebrick = new BlockBasicSlab(false,"SlabStonebrick", "stone/stonebrick_0");
        SlabSandstone = new BlockBasicSlab(false,"SlabSandstone", "stone/sandstone_0");

        SlabIrgadBrick = new BlockBasicSlab(false,"SlabIrgadBrick", "stone/irgad_brick");
        SlabEndbrick = new BlockBasicSlab(false,"SlabEndbrick", "stone/end_bricks");
        SlabImperialBrick = new BlockBasicSlab(false,"SlabImperialBrick", "stone/imperial_brick");
        SlabHeneizenBrick = new BlockBasicSlab(false,"SlabHeneizenBrick", "stone/heneizen_brick");

        BlockMetaSlab.registerSlabs((BlockMeta) RoofStandart, 3, "roof/roofk" );
        BlockMetaSlab.registerSlabs((BlockMeta) RoofLaminated, 16, "roof/roof1" );
        BlockMetaSlab.registerSlabs((BlockMeta) RoofSheet, 16, "roof/roof2" );
        BlockMetaSlab.registerSlabs((BlockMeta) RoofFlake, 16, "roof/roof3" );
        BlockMetaSlab.registerSlabs((BlockMeta) RoofWood, 6, "wood/roofwood" );

        //НИЖЕ НАХОИДТСЯ ЗАБОР
        WallStonebrick = new BlockBasicWall(Blocks.stone, "WallStonebrick", "stone/stonebrick_0");
        WallSandstone = new BlockBasicWall(Blocks.stone, "WallSandstone", "stone/sandstone_0");
    }

}

