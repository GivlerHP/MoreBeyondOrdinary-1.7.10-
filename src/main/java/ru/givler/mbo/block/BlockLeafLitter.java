package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.ColorizerFoliage;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.registry.CreativeTabRegistry;

/** Four flat patches of leaf litter; count and rotation fit into one metadata value. */
public final class BlockLeafLitter extends Block {
    private int renderType;
    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public BlockLeafLitter() {
        super(Material.plants);
        setBlockName("LeafLitter");
        setHardness(0.1F);
        setStepSound(soundTypeGrass);
        setLightOpacity(0);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        setBlockBounds(0F, 0F, 0F, 1F, 1F / 16F, 1F);
    }

    public void setLeafLitterRenderType(int renderType) { this.renderType = renderType; }
    @Override public int getRenderType() { return renderType; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && canStay(world, x, y, z);
    }

    private boolean canStay(World world, int x, int y, int z) {
        return y > 0 && world.getBlock(x, y - 1, z).isSideSolid(world, x, y - 1, z, ForgeDirection.UP);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!canStay(world, x, y, z)) {
            if (!world.isRemote) dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getCurrentEquippedItem();
        return held != null && held.getItem() == Item.getItemFromBlock(this)
                && addLayer(world, x, y, z, side, player, held);
    }

    public boolean addLayer(World world, int x, int y, int z, int side,
                            EntityPlayer player, ItemStack stack) {
        if (side == 0 || stack.stackSize <= 0 || !player.canPlayerEdit(x, y, z, side, stack)) return false;
        int meta = world.getBlockMetadata(x, y, z);
        if ((meta & 3) == 3) return true;
        if (!world.isRemote) {
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
            world.playSoundEffect(x + .5D, y + .5D, z + .5D, stepSound.func_150496_b(), .7F, .9F);
            if (!player.capabilities.isCreativeMode) stack.stackSize--;
        }
        return true;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(Item.getItemFromBlock(this), (meta & 3) + 1));
        return drops;
    }

    @Override
    public int damageDropped(int meta) { return 0; }

    @Override
    public int getRenderColor(int meta) { return ColorizerFoliage.getFoliageColorBasic(); }

    @Override
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return world.getBiomeGenForCoords(x, z).getBiomeFoliageColor(x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icon = register.registerIcon("mbo:foliage/leaf_litter");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) { return icon; }
}
