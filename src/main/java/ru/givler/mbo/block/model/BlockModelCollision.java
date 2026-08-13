package ru.givler.mbo.block.model;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.TileEntityModelCollision;

public class BlockModelCollision extends Block implements ITileEntityProvider {
    public BlockModelCollision() {
        super(Material.rock);
        setBlockName("ModelCollisionPart");
        setHardness(0.0F);
        setResistance(6000000.0F);
    }

    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public boolean hasTileEntity(int meta) { return true; }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityModelCollision(); }
    @Override public int quantityDropped(java.util.Random random) { return 0; }

    private TileEntityModelCollision tile(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityModelCollision ? (TileEntityModelCollision) tile : null;
    }

    public ItemStack getOwnerStack(IBlockAccess world, int x, int y, int z, EntityPlayer player) {
        TileEntityModelCollision collision = tile(world, x, y, z);
        BlockModels owner = collision == null ? null : collision.getOwnerBlock();
        if (owner == null) return null;
        World ownerWorld = world instanceof World ? (World) world : null;
        if (ownerWorld == null) return new ItemStack(owner, 1, 0);
        MovingObjectPosition ownerHit = new MovingObjectPosition(
                collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ(), 1,
                net.minecraft.util.Vec3.createVectorHelper(
                        collision.getOwnerX() + 0.5D,
                        collision.getOwnerY() + 0.5D,
                        collision.getOwnerZ() + 0.5D));
        ItemStack stack = owner.getPickBlock(ownerHit, ownerWorld,
                collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ(), player);
        return stack == null ? new ItemStack(owner, 1, 0) : stack;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition hit, World world, int x, int y, int z,
                                  EntityPlayer player) {
        return getOwnerStack(world, x, y, z, player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer renderer) {
        TileEntityModelCollision collision = tile(world, target.blockX, target.blockY, target.blockZ);
        if (collision == null || collision.getOwnerBlock() == null) return true;
        setBlockBoundsBasedOnState(world, target.blockX, target.blockY, target.blockZ);
        double px = target.blockX + getBlockBoundsMinX()
                + world.rand.nextDouble() * (getBlockBoundsMaxX() - getBlockBoundsMinX());
        double py = target.blockY + getBlockBoundsMinY()
                + world.rand.nextDouble() * (getBlockBoundsMaxY() - getBlockBoundsMinY());
        double pz = target.blockZ + getBlockBoundsMinZ()
                + world.rand.nextDouble() * (getBlockBoundsMaxZ() - getBlockBoundsMinZ());
        addOwnerParticle(world, collision, renderer, px, py, pz, 0.0D, 0.0D, 0.0D, true);
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer renderer) {
        TileEntityModelCollision collision = tile(world, x, y, z);
        if (collision == null || collision.getOwnerBlock() == null) return true;
        setBlockBoundsBasedOnState(world, x, y, z);
        for (int ix = 0; ix < 4; ix++) for (int iy = 0; iy < 4; iy++) for (int iz = 0; iz < 4; iz++) {
            double px = x + getBlockBoundsMinX() + (ix + 0.5D) / 4.0D
                    * (getBlockBoundsMaxX() - getBlockBoundsMinX());
            double py = y + getBlockBoundsMinY() + (iy + 0.5D) / 4.0D
                    * (getBlockBoundsMaxY() - getBlockBoundsMinY());
            double pz = z + getBlockBoundsMinZ() + (iz + 0.5D) / 4.0D
                    * (getBlockBoundsMaxZ() - getBlockBoundsMinZ());
            addOwnerParticle(world, collision, renderer, px, py, pz,
                    px - x - 0.5D, py - y - 0.5D, pz - z - 0.5D, false);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    private void addOwnerParticle(World world, TileEntityModelCollision collision, EffectRenderer renderer,
                                  double x, double y, double z, double mx, double my, double mz,
                                  boolean hitParticle) {
        BlockModels owner = collision.getOwnerBlock();
        int meta = world.getBlockMetadata(collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ());
        EntityDiggingFX particle = new EntityDiggingFX(world, x, y, z, mx, my, mz, owner, meta);
        particle.applyColourMultiplier(collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ());
        if (hitParticle) particle.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
        renderer.addEffect(particle);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntityModelCollision tile = tile(world, x, y, z);
        float[] b = tile == null ? new float[]{0, 0, 0, 1, 1, 1} : tile.getBounds();
        setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        TileEntityModelCollision tile = tile(world, x, y, z);
        BlockModels owner = tile == null ? null : tile.getOwnerBlock();
        if (owner == null || !owner.isModelCollisionEnabled()) return null;
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        TileEntityModelCollision collision = tile(world, x, y, z);
        BlockModels owner = collision == null ? null : collision.getOwnerBlock();
        return owner == null ? super.getBlockHardness(world, x, y, z)
                : owner.getBlockHardness(world, collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ());
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
        TileEntityModelCollision collision = tile(world, x, y, z);
        BlockModels owner = collision == null ? null : collision.getOwnerBlock();
        return owner == null ? super.getPlayerRelativeBlockHardness(player, world, x, y, z)
                : owner.getPlayerRelativeBlockHardness(player, world,
                        collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ());
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        TileEntityModelCollision tile = tile(world, x, y, z);
        if (tile != null && tile.getOwnerBlock() != null) {
            world.func_147480_a(tile.getOwnerX(), tile.getOwnerY(), tile.getOwnerZ(), true);
            return true;
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public ArrayList<net.minecraft.item.ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<net.minecraft.item.ItemStack>();
    }
}
