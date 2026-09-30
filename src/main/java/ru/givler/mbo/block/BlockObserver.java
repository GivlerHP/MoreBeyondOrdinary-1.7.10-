package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.registry.CreativeTabRegistry;

/** Detects changes on its front face and emits a two-tick pulse from its back. */
public final class BlockObserver extends Block {
    private int renderType;
    @SideOnly(Side.CLIENT) private IIcon front, back, backOn, top;

    public BlockObserver() {
        super(Material.rock);
        setBlockName("Observer");
        setHardness(3F);
        setResistance(3F);
        setHarvestLevel("pickaxe", 0);
        setStepSound(soundTypeStone);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
    }

    public void setObserverRenderType(int id) { renderType = id; }
    @Override public int getRenderType() { return renderType; }
    @Override public int tickRate(World world) { return 2; }
    @Override public int damageDropped(int meta) { return 0; }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int direction = BlockPistonBase.determineOrientation(world, x, y, z, placer);
        world.setBlockMetadataWithNotify(x, y, z, direction, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("mbo:redstone/observer_side");
        front = register.registerIcon("mbo:redstone/observer_front");
        back = register.registerIcon("mbo:redstone/observer_back");
        backOn = register.registerIcon("mbo:redstone/observer_back_on");
        top = register.registerIcon("mbo:redstone/observer_top");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        int facing = meta & 7;
        if (facing > 5) facing = 3;
        if (side == facing) return front;
        if (side == Facing.oppositeSide[facing]) return (meta & 8) != 0 ? backOn : back;
        int topSide = facing < 2 ? 2 : 1;
        return side == topSide || side == Facing.oppositeSide[topSide] ? top : blockIcon;
    }

    /** Called by the world-change hook with the exact changed position. */
    public void observedChange(World world, int x, int y, int z, int changedX, int changedY, int changedZ) {
        if (world.isRemote) return;
        int meta = world.getBlockMetadata(x, y, z);
        int facing = meta & 7;
        if (facing > 5 || (meta & 8) != 0) return;
        if (x + Facing.offsetsXForSide[facing] == changedX
                && y + Facing.offsetsYForSide[facing] == changedY
                && z + Facing.offsetsZForSide[facing] == changedZ)
            world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote || world.getBlock(x, y, z) != this) return;
        int meta = world.getBlockMetadata(x, y, z);
        boolean powered = (meta & 8) != 0;
        world.setBlockMetadataWithNotify(x, y, z, powered ? meta & 7 : meta | 8, 2);
        if (!powered) world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
        notifyOutput(world, x, y, z, meta & 7);
    }

    private void notifyOutput(World world, int x, int y, int z, int facing) {
        int backSide = Facing.oppositeSide[facing];
        int bx = x + Facing.offsetsXForSide[backSide];
        int by = y + Facing.offsetsYForSide[backSide];
        int bz = z + Facing.offsetsZForSide[backSide];
        world.notifyBlockOfNeighborChange(bx, by, bz, this);
        world.notifyBlocksOfNeighborChange(bx, by, bz, this, facing);
    }

    @Override public boolean canProvidePower() { return true; }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        return (meta & 8) != 0 && (meta & 7) == side ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        return isProvidingWeakPower(world, x, y, z, side);
    }
}
