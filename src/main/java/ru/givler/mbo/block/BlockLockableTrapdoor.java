package ru.givler.mbo.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.lockable.LockableAccess;
import ru.givler.mbo.tileentity.TileEntityLockableTrapdoor;

public class BlockLockableTrapdoor extends TrapDoorBase {
    public BlockLockableTrapdoor() { super(Material.wood, "LockableTrapdoor", "minecraft:trapdoor"); }
    @Override public boolean hasTileEntity(int metadata) { return true; }
    @Override public TileEntity createTileEntity(World world, int metadata) { return new TileEntityLockableTrapdoor(); }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
        ILockableTile tile = LockableAccess.get(world, x, y, z);
        if (tile == null) return true;
        if (player.isSneaking()) {
            if (!world.isRemote) LockableAccess.configureWithAdminKey(player, tile);
            return true;
        }
        if (world.isRemote) return true;
        if (LockableAccess.isAdminKey(player)) {
            int meta = world.getBlockMetadata(x, y, z);
            world.setBlockMetadataWithNotify(x, y, z, meta ^ 4, 2);
            world.playAuxSFXAtEntity(null, 1003, x, y, z, 0);
            return true;
        }
        if (tile.getLockData().isLocked()) {
            if (!LockableAccess.openLockpicking(player, tile)) LockableAccess.playLockedSound(world, x, y, z);
            return true;
        }
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, meta ^ 4, 2);
        world.playAuxSFXAtEntity(null, 1003, x, y, z, 0);
        return true;
    }
}
