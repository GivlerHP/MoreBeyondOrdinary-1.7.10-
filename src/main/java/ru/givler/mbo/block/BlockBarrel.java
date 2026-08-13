package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.TileEntityBarrel;

public class BlockBarrel extends BlockContainer {
    private IIcon front, frontOpen, back;
    private int renderType;

    public BlockBarrel() {
        super(Material.wood);
        setBlockName("Barrel");
        setBlockTextureName(MoreBeyondOrdinary.MODID + ":wood/barrel");
        setHardness(2.5F);
        setResistance(2.5F);
        setHarvestLevel("axe", 0);
        setStepSound(soundTypeWood);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
    }

    public void setBarrelRenderType(int id) { renderType = id; }
    @Override public int getRenderType() { return renderType; }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon(getTextureName() + "_side");
        front = register.registerIcon(getTextureName() + "_top");
        frontOpen = register.registerIcon(getTextureName() + "_top_open");
        back = register.registerIcon(getTextureName() + "_bottom");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        int facing = BlockPistonBase.getPistonOrientation(meta);
        if (facing > 5) facing = 1;
        if (side == facing) return (meta & 8) != 0 ? frontOpen : front;
        if (side == Facing.oppositeSide[facing]) return back;
        return blockIcon;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = BlockPistonBase.determineOrientation(world, x, y, z, placer);
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityBarrel && stack.hasDisplayName()) {
            ((TileEntityBarrel) tile).setCustomName(stack.getDisplayName());
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityBarrel) {
            player.openGui(MoreBeyondOrdinary.instance, MoreBeyondOrdinary.GUI_BARREL, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMeta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityBarrel) {
            TileEntityBarrel barrel = (TileEntityBarrel) tile;
            for (int slot = 0; slot < barrel.getSizeInventory(); slot++) {
                ItemStack stack = barrel.getStackInSlot(slot);
                if (stack == null) continue;
                float ox = world.rand.nextFloat() * 0.8F + 0.1F;
                float oy = world.rand.nextFloat() * 0.8F + 0.1F;
                float oz = world.rand.nextFloat() * 0.8F + 0.1F;
                while (stack.stackSize > 0) {
                    int count = Math.min(stack.stackSize, world.rand.nextInt(21) + 10);
                    stack.stackSize -= count;
                    ItemStack dropped = new ItemStack(stack.getItem(), count, stack.getItemDamage());
                    if (stack.hasTagCompound()) dropped.setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
                    EntityItem entity = new EntityItem(world, x + ox, y + oy, z + oz, dropped);
                    entity.motionX = world.rand.nextGaussian() * 0.05D;
                    entity.motionY = world.rand.nextGaussian() * 0.05D + 0.2D;
                    entity.motionZ = world.rand.nextGaussian() * 0.05D;
                    world.spawnEntityInWorld(entity);
                }
            }
            world.func_147453_f(x, y, z, oldBlock);
        }
        super.breakBlock(world, x, y, z, oldBlock, oldMeta);
    }

    @Override public boolean hasComparatorInputOverride() { return true; }
    @Override public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityBarrel ? Container.calcRedstoneFromInventory((TileEntityBarrel) tile) : 0;
    }

    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityBarrel(); }
}
