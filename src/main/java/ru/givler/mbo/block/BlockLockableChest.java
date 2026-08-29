package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.lockable.LockableAccess;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.TileEntityLockableChest;

public class BlockLockableChest extends BlockChest {
    public BlockLockableChest() {
        super(0);
        setBlockName("LockableChest");
        setHardness(2.5F);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        GameRegistry.registerBlock(this, "LockableChest");
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityLockableChest(); }

    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (Math.abs(dx) + Math.abs(dz) == 1 && world.getBlock(x + dx, y, z + dz) == this) return false;
        }
        return super.canPlaceBlockAt(world, x, y, z);
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
        ILockableTile tile = LockableAccess.get(world, x, y, z);
        if (tile == null) return false;
        if (world.isRemote) return true;
        if (LockableAccess.configureWithAdminKey(player, tile)) return true;
        if (LockableAccess.isAdminKey(player)) { tile.onUnlocked(player); return true; }
        if (tile.getLockData().isLocked()) {
            if (!LockableAccess.openLockpicking(player, tile)) LockableAccess.playLockedSound(world, x, y, z);
            return true;
        }
        ((TileEntityLockableChest) tile).openFor(player);
        return true;
    }
}
