package ru.givler.mbo.registry;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.stonecutter.BlockStonecutter;
import ru.givler.mbo.stonecutter.StonecutterRecipes;

public final class StonecutterRegistry {
    public static BlockStonecutter stonecutter;
    private StonecutterRegistry(){}

    public static void init() {
        stonecutter=new BlockStonecutter();
        GameRegistry.registerBlock(stonecutter,"Stonecutter");
    }

    /** Central place for built-in stonecutter recipes. Add addon recipes through StonecutterRecipes.add. */
    public static void registerRecipes() {
        GameRegistry.addRecipe(new ItemStack(stonecutter),
                " I ","SSS",'I',Items.iron_ingot,'S',Blocks.stone);

        addFamily(new ItemStack(Blocks.stone), new ItemStack(Blocks.stonebrick),
                new ItemStack(Blocks.stone_slab,2,0), new ItemStack(Blocks.stone_brick_stairs) );
        add(new ItemStack(Blocks.stone), new ItemStack(Blocks.stonebrick,1,3));
        add(new ItemStack(Blocks.stone), new ItemStack(BlockRegistry.WallVanillaStonebrick));
        add(new ItemStack(Blocks.stone), new ItemStack(Blocks.stone_slab,2,5));
        addFamily(new ItemStack(Blocks.stonebrick), new ItemStack(Blocks.stonebrick,1,3),
                new ItemStack(Blocks.stone_slab,2,5), new ItemStack(Blocks.stone_brick_stairs));
        add(new ItemStack(Blocks.stonebrick),new ItemStack(BlockRegistry.WallVanillaStonebrick));
        addFamily(new ItemStack(Blocks.cobblestone), null,
                new ItemStack(Blocks.stone_slab,2,3), new ItemStack(Blocks.stone_stairs));
        add(new ItemStack(Blocks.cobblestone),new ItemStack(Blocks.cobblestone_wall));
        addFamily(new ItemStack(Blocks.brick_block), null,
                new ItemStack(Blocks.stone_slab,2,4), new ItemStack(Blocks.brick_stairs));
        add(new ItemStack(Blocks.brick_block),new ItemStack(BlockRegistry.WallVanillaBrick));
        addFamily(new ItemStack(Blocks.sandstone), new ItemStack(Blocks.sandstone,1,2),
                new ItemStack(Blocks.stone_slab,2,1), new ItemStack(Blocks.sandstone_stairs));
        add(new ItemStack(Blocks.sandstone), new ItemStack(BlockRegistry.WallVanillaSandstone));
        addFamily(new ItemStack(Blocks.nether_brick), null,
                new ItemStack(Blocks.stone_slab,2,6), new ItemStack(Blocks.nether_brick_stairs));
        add(new ItemStack(Blocks.nether_brick),new ItemStack(Blocks.nether_brick_fence));
        addFamily(new ItemStack(Blocks.quartz_block), new ItemStack(Blocks.quartz_block,1,2),
                new ItemStack(Blocks.stone_slab,2,7), new ItemStack(Blocks.quartz_stairs));

        StonecutterRecipes.add(new ItemStack(BlockRegistry.SmoothStone),
                new ItemStack(BlockRegistry.SlabSmoothStone,2));
        StonecutterRecipes.add(new ItemStack(BlockRegistry.SmoothStone),
                new ItemStack(BlockRegistry.StairsSmoothStone));
        StonecutterRecipes.add(new ItemStack(BlockRegistry.SmoothStone),
                new ItemStack(BlockRegistry.WallSmoothStone));

        add(new ItemStack(BlockRegistry.BlockSandstone,1,0),
                new ItemStack(BlockRegistry.BlockSandstone,1,1));
        add(new ItemStack(BlockRegistry.BlockSandstone,1,0),
                new ItemStack(BlockRegistry.BlockSandstone,1,2));

        add(new ItemStack(BlockRegistry.BlockGreyStone),
                new ItemStack(BlockRegistry.BlockStonebrick,1,0));
        add(new ItemStack(BlockRegistry.BlockGreyStone),
                new ItemStack(BlockRegistry.BlockStonebrick,1,3));
        addMboFamily(BlockRegistry.BlockGreyStone, BlockRegistry.SlabStone,
                BlockRegistry.StairsStone, BlockRegistry.WallStonebrick);
        addMboFamily(BlockRegistry.BlockGreyCobblestone, BlockRegistry.SlabCobblestone,
                BlockRegistry.StairsGreyCobblestone, null);
        addMboFamily(BlockRegistry.BlockStonebrick, BlockRegistry.SlabStonebrick,
                BlockRegistry.StairsStonebrick, BlockRegistry.WallStonebrick);
        addMboFamily(BlockRegistry.BlockSandstone, BlockRegistry.SlabSandstone,
                BlockRegistry.StairsSandstone, BlockRegistry.WallSandstone);
        addMboFamily(BlockRegistry.BlockIrgadBrick, BlockRegistry.SlabIrgadBrick,
                BlockRegistry.StairsIrgadBrick, null);
        addMboFamily(BlockRegistry.BlockEndbrick, BlockRegistry.SlabEndbrick,
                BlockRegistry.StairsEndbrick, null);
        addMboFamily(BlockRegistry.BlockImperialBrick, BlockRegistry.SlabImperialBrick,
                BlockRegistry.StairsImperialBrick, null);
        addMboFamily(BlockRegistry.BlockHeneizenBrick, BlockRegistry.SlabHeneizenBrick,
                BlockRegistry.StairsHeneizenBrick, null);
        addMboFamily(BlockRegistry.BlockFiredClay, BlockRegistry.SlabFiredClay,
                BlockRegistry.StairsFiredClay, BlockRegistry.WallFiredClay);
        addMboFamily(BlockRegistry.BlockAshgarBrick, BlockRegistry.SlabAshgarBrick,
                BlockRegistry.StairsAshgarBrick, BlockRegistry.WallAshgarBrick);

        registerTuffRecipes();
        registerPrismarineRecipes();
        registerMineFantasyRecipes();
    }
    private static void addFamily(ItemStack input,ItemStack variant,ItemStack slab,ItemStack stairs) {
        if(variant!=null) StonecutterRecipes.add(input,variant);
        if(slab!=null) StonecutterRecipes.add(input,slab);
        if(stairs!=null) StonecutterRecipes.add(input,stairs);
    }
    private static void addMboFamily(net.minecraft.block.Block input,net.minecraft.block.Block slab,
                                     net.minecraft.block.Block stairs,net.minecraft.block.Block wall) {
        if(slab!=null) StonecutterRecipes.add(new ItemStack(input),new ItemStack(slab,2));
        if(stairs!=null) StonecutterRecipes.add(new ItemStack(input),new ItemStack(stairs));
        if(wall!=null) StonecutterRecipes.add(new ItemStack(input),new ItemStack(wall));
    }

    private static void registerTuffRecipes() {
        ItemStack base=new ItemStack(BlockRegistry.BlockTuff,1,0);
        add(base,new ItemStack(BlockRegistry.BlockTuff,1,1));
        add(base,new ItemStack(BlockRegistry.BlockTuff,1,2));
        add(base,new ItemStack(BlockRegistry.BlockChiseledTuff));
        add(base,new ItemStack(BlockRegistry.BlockChiseledTuffBricks));
        for(int meta=0;meta<3;meta++) {
            add(base,new ItemStack(BlockRegistry.StairsTuff[meta]));
            add(base,new ItemStack(BlockRegistry.SlabTuff[meta],2));
            add(base,new ItemStack(BlockRegistry.WallTuff[meta]));
            if(meta>0) {
                ItemStack input=new ItemStack(BlockRegistry.BlockTuff,1,meta);
                add(input,new ItemStack(BlockRegistry.StairsTuff[meta]));
                add(input,new ItemStack(BlockRegistry.SlabTuff[meta],2));
                add(input,new ItemStack(BlockRegistry.WallTuff[meta]));
            }
        }
    }

    private static void registerPrismarineRecipes() {
        ItemStack base=new ItemStack(BlockRegistry.BlockPrismarine,1,0);
        add(base,new ItemStack(BlockRegistry.BlockPrismarine,1,1));
        add(base,new ItemStack(BlockRegistry.BlockPrismarine,1,2));
        for(int meta=0;meta<3;meta++) {
            add(base,new ItemStack(BlockRegistry.StairsPrismarine[meta]));
            add(base,new ItemStack(BlockRegistry.SlabPrismarine[meta],2));
            add(base,new ItemStack(BlockRegistry.WallPrismarine[meta]));
            if(meta>0) {
                ItemStack input=new ItemStack(BlockRegistry.BlockPrismarine,1,meta);
                add(input,new ItemStack(BlockRegistry.StairsPrismarine[meta]));
                add(input,new ItemStack(BlockRegistry.SlabPrismarine[meta],2));
                add(input,new ItemStack(BlockRegistry.WallPrismarine[meta]));
            }
        }
    }

    private static void registerMineFantasyRecipes() {
        if(!Loader.isModLoaded("MineFantasy2") && !Loader.isModLoaded("minefantasy2")) return;
        try {
            Class<?> list=Class.forName("minefantasy.mf2.block.list.BlockListMF");
            Block brick=(Block)list.getField("cobble_brick").get(null);
            Block mosaic=(Block)list.getField("cobble_pavement").get(null);
            add(new ItemStack(Blocks.cobblestone),new ItemStack(brick));
            add(new ItemStack(Blocks.cobblestone),new ItemStack(mosaic));
            add(new ItemStack(Blocks.cobblestone),
                    new ItemStack((Block)list.getField("cobble_brick_stair").get(null)));
            add(new ItemStack(Blocks.cobblestone),
                    new ItemStack((Block)list.getField("cobble_brick_slab").get(null),2));
            add(new ItemStack(Blocks.cobblestone),
                    new ItemStack((Block)list.getField("cobble_pavement_stair").get(null)));
            add(new ItemStack(Blocks.cobblestone),
                    new ItemStack((Block)list.getField("cobble_pavement_slab").get(null),2));

            Block brickStair=(Block)list.getField("cobble_brick_stair").get(null);
            Block brickSlab=(Block)list.getField("cobble_brick_slab").get(null);
            Block mosaicStair=(Block)list.getField("cobble_pavement_stair").get(null);
            Block mosaicSlab=(Block)list.getField("cobble_pavement_slab").get(null);
            add(new ItemStack(brick),new ItemStack(brickStair));
            add(new ItemStack(brick),new ItemStack(brickSlab,2));
            add(new ItemStack(mosaic),new ItemStack(mosaicStair));
            add(new ItemStack(mosaic),new ItemStack(mosaicSlab,2));

            registerMineFantasyLimestone(list);
            registerMineFantasyReinforcedStone(list);
        } catch(ReflectiveOperationException exception) {
            System.err.println("[MBO] Could not register MineFantasy2 stonecutter recipes: "+exception);
        }
    }

    private static void registerMineFantasyLimestone(Class<?> list)
            throws ReflectiveOperationException {
        Block limestone=(Block)list.getField("limestone").get(null);
        Block[] stairs=(Block[])limestone.getClass().getField("stairblocks").get(limestone);
        Block slabs=(Block)limestone.getClass().getField("slabBlock").get(limestone);
        ItemStack cobble=new ItemStack(limestone,1,1);

        add(cobble,new ItemStack(limestone,1,2));
        add(cobble,new ItemStack(limestone,1,3));
        for(int meta=1;meta<=3;meta++) {
            add(cobble,new ItemStack(stairs[meta]));
            add(cobble,new ItemStack(slabs,2,meta));
        }

        ItemStack bricks=new ItemStack(limestone,1,2);
        add(bricks,new ItemStack(stairs[2]));
        add(bricks,new ItemStack(slabs,2,2));

        ItemStack mosaic=new ItemStack(limestone,1,3);
        add(mosaic,new ItemStack(stairs[3]));
        add(mosaic,new ItemStack(slabs,2,3));
    }

    private static void registerMineFantasyReinforcedStone(Class<?> list)
            throws ReflectiveOperationException {
        Block reinforced=(Block)list.getField("reinforced_stone").get(null);
        Block reinforcedBricks=(Block)list.getField("reinforced_stone_bricks").get(null);
        Block reinforcedStair=(Block)list.getField("reinforced_stone_stair").get(null);
        Block reinforcedSlab=(Block)list.getField("reinforced_stone_slab").get(null);
        Block brickStair=(Block)list.getField("reinforced_stone_brick_stair").get(null);
        Block brickSlab=(Block)list.getField("reinforced_stone_brick_slab").get(null);
        ItemStack base=new ItemStack(reinforced,1,0);

        add(base,new ItemStack(reinforcedBricks,1,0));
        for(int meta=1;meta<=4;meta++) add(base,new ItemStack(reinforced,1,meta));
        add(base,new ItemStack(reinforcedStair));
        add(base,new ItemStack(reinforcedSlab,2));
        add(base,new ItemStack(brickStair));
        add(base,new ItemStack(brickSlab,2));

        ItemStack bricks=new ItemStack(reinforcedBricks,1,0);
        add(bricks,new ItemStack(brickStair));
        add(bricks,new ItemStack(brickSlab,2));
    }

    private static void add(ItemStack input,ItemStack output) {
        StonecutterRecipes.add(input,output);
    }
}
