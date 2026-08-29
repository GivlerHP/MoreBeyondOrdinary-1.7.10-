package ru.givler.mbo.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.lockable.LockableAccess;
import ru.givler.mbo.tileentity.TileEntityLockableDoor;

public class BlockLockableDoor extends DoorBase {
    public BlockLockableDoor() { super(Material.wood, "LockableDoor", "minecraft:door_wood", null); }
    @Override public boolean hasTileEntity(int metadata) { return true; }
    @Override public TileEntity createTileEntity(World world, int metadata) { return new TileEntityLockableDoor(); }

    private ILockableTile lock(World world, int x, int y, int z) {
        if ((world.getBlockMetadata(x, y, z) & 8) != 0) y--;
        return LockableAccess.get(world, x, y, z);
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
        ILockableTile tile = lock(world, x, y, z);
        if (tile == null) return true;
        if (player.isSneaking()) {
            if (!world.isRemote) LockableAccess.configureWithAdminKey(player, tile);
            return true;
        }
        if (world.isRemote) return true;
        if (LockableAccess.isAdminKey(player)) { toggle(world, x, y, z); return true; }
        if (tile.getLockData().isLocked()) {
            if (!LockableAccess.openLockpicking(player, tile)) LockableAccess.playLockedSound(world, x, y, z);
            return true;
        }
        toggle(world, x, y, z);
        return true;
    }

    private void toggle(World world, int x, int y, int z) {
        if ((world.getBlockMetadata(x, y, z) & 8) != 0) y--;
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, meta ^ 4, 2);
        world.markBlockRangeForRenderUpdate(x, y, z, x, y + 1, z);
        world.playAuxSFXAtEntity(null, 1003, x, y, z, 0);
    }
}
