package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

public class BlockBasicFence extends BlockFence {

    public static class ItemBasicFence extends ItemBlock {
        public ItemBasicFence(Block block) {
            super(block);
            this.setHasSubtypes(true);
            this.setMaxDamage(0);
        }

        @Override
        public int getMetadata(int damage) {
            return damage;
        }

        @Override
        public IIcon getIconFromDamage(int damage) {
            return this.field_150939_a.getIcon(2, damage);
        }

        @Override
        public String getUnlocalizedName(ItemStack stack) {
            return super.getUnlocalizedName() + "." + stack.getItemDamage();
        }
    }

    private final Block planks;
    private final int[] plankMetas;
    private int renderType = 11;

    public BlockBasicFence(String name, Block planks, int... plankMetas) {
        super("planks_oak", Material.wood);

        this.planks = planks;
        this.plankMetas = plankMetas.clone();
        this.setBlockName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(Block.soundTypeWood);
        this.setHarvestLevel("axe", 0);

        GameRegistry.registerBlock(this, ItemBasicFence.class, name);
    }

    public int getVariantCount() {
        return this.plankMetas.length;
    }

    public void setFenceRenderType(int renderType) {
        this.renderType = renderType;
    }

    @Override
    public int getRenderType() {
        return this.renderType;
    }

    private int normalizeMeta(int meta) {
        return meta >= 0 && meta < this.plankMetas.length ? meta : 0;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.planks.getIcon(side, this.plankMetas[this.normalizeMeta(meta)]);
    }

    @Override
    public int damageDropped(int meta) {
        return this.normalizeMeta(meta);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int meta = 0; meta < this.plankMetas.length; ++meta) {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public void addStandardRecipes() {
        for (int meta = 0; meta < this.plankMetas.length; ++meta) {
            GameRegistry.addRecipe(new ItemStack(this, 3, meta),
                    new Object[]{"XSX", "XSX", 'X', new ItemStack(this.planks, 1, this.plankMetas[meta]),
                            'S', net.minecraft.init.Items.stick});
        }
    }

    @Override
    public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z) {
        if (world.getBlock(x, y, z) instanceof BlockBasicFence) {
            return true;
        }
        return super.canConnectFenceTo(world, x, y, z);
    }
}
