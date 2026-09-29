package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.Random;

public class BlockMetaSlab extends BlockSlab {

    public static class ItemMetaSlab extends ItemSlab {
        public ItemMetaSlab(Block block, BlockMetaSlab single, BlockMetaSlab dbl, Boolean isDouble) {
            super(block, single, dbl, isDouble);
        }
    }

    private BlockMetaSlab singleSlabForDrops;
    private int copperState = -1;

    private BlockMetaSlab(boolean isDouble, BlockMeta baseBlock, String texture, int meta) {
        super(isDouble, baseBlock.getMaterial());

        this.setBlockName(baseBlock.getUnlocalizedName() + "_slab_" + meta);
        this.setHardness(baseBlock.getBlockHardness(null, 0, 0, 0));
        this.setResistance(baseBlock.getExplosionResistance(null));
        this.setStepSound(baseBlock.stepSound);
        if (baseBlock.getMaterial() == Material.rock || baseBlock.getMaterial() == Material.iron) {
            this.setHarvestLevel("pickaxe", 0);
        } else if (baseBlock.getMaterial() == Material.wood) {
            this.setHarvestLevel("axe", 0);
        }
        this.setLightOpacity(0);
        this.useNeighborBrightness = true;
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture);

        if (!isDouble) {
            this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        }
    }

    @Override
    public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock(this);
    }


    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        if (field_150004_a && singleSlabForDrops != null) {
            return Item.getItemFromBlock(singleSlabForDrops);
        }
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random rand) {
        if (field_150004_a && singleSlabForDrops != null) return 2;
        return 1;
    }

    @Override
    public String func_150002_b(int meta) {
        return this.getUnlocalizedName();
    }

    public static BlockMetaSlab[] registerSlabs(BlockMeta baseBlock, int count, String texture) {
        String[] textures = new String[count];
        for (int i = 0; i < count; i++) {
            textures[i] = texture + "_" + i;
        }
        return registerSlabs(baseBlock, textures);
    }

    public int getCopperState() { return copperState; }

    public boolean isDoubleSlab() { return field_150004_a; }

    private BlockMetaSlab withCopper(int state) {
        copperState = state;
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setTickRandomly((state & 8) == 0 && (state & 3) < 3);
        return this;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (copperState >= 0 && !world.isRemote) CopperOxidation.tick(world, x, y, z, random);
        else if (copperState < 0) super.updateTick(world, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        return copperState >= 0 && CopperOxidation.interact(world, x, y, z, player);
    }

    public static BlockMetaSlab[] registerSlabs(BlockMeta baseBlock, String[] textures) {
        int count = textures.length;
        BlockMetaSlab[] result = new BlockMetaSlab[count];

        for (int i = 0; i < count; i++) {
            BlockMetaSlab single = new BlockMetaSlab(false, baseBlock, textures[i], i);
            BlockMetaSlab dbl    = new BlockMetaSlab(true,  baseBlock, textures[i], i);
            dbl.singleSlabForDrops = single;

            String singleName = baseBlock.getUnlocalizedName() + "_slab_" + i;
            String doubleName = baseBlock.getUnlocalizedName() + "_slab_double_" + i;

            GameRegistry.registerBlock(single, ItemMetaSlab.class, singleName, single, dbl, Boolean.FALSE);
            GameRegistry.registerBlock(dbl,    ItemMetaSlab.class, doubleName, single, dbl, Boolean.TRUE);

            result[i] = single;
        }

        return result;
    }

    public static BlockMetaSlab[] registerCopperSlabs(BlockMeta baseBlock, BlockMetaSlab[] doubles,
                                                       String[] textures) {
        BlockMetaSlab[] singles = new BlockMetaSlab[12];
        for (int stage = 0; stage < 4; stage++) for (int wax = 0; wax <= 8; wax += 8) {
            int state = stage + wax;
            BlockMetaSlab single = new BlockMetaSlab(false, baseBlock, textures[stage], state).withCopper(state);
            BlockMetaSlab full = new BlockMetaSlab(true, baseBlock, textures[stage], state).withCopper(state);
            String name = "CutCopperSlab" + state;
            single.setBlockName(name);
            full.setBlockName(name + "Double");
            full.singleSlabForDrops = single;
            GameRegistry.registerBlock(single, ItemMetaSlab.class, name, single, full, false);
            GameRegistry.registerBlock(full, ItemMetaSlab.class, name + "Double", single, full, true);
            singles[state] = single;
            doubles[state] = full;
        }
        return singles;
    }

    public static void addStandardRecipes(BlockMetaSlab[] slabs, BlockMeta parent) {
        for (int i = 0; i < slabs.length; i++) {
            GameRegistry.addRecipe(new ItemStack(slabs[i], 6),
                    new Object[]{"XXX", 'X', new ItemStack(parent, 1, i)});
            GameRegistry.addRecipe(new ItemStack(parent, 1, i),
                    new Object[]{"X", "X", 'X', new ItemStack(slabs[i], 1)});
        }
    }
}
