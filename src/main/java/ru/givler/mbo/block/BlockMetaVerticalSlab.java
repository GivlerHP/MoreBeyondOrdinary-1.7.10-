package ru.givler.mbo.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public final class BlockMetaVerticalSlab extends BlockVerticalSlab {
    private final int copperState;

    public BlockMetaVerticalSlab(Block source, int meta) {
        super(source, meta);
        copperState = source instanceof BlockMeta && ((BlockMeta) source).isCopper() ? meta : -1;
        setTickRandomly(copperState >= 0 && (copperState & 8) == 0 && (copperState & 3) < 3);
    }

    public int getCopperState() {
        return copperState;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (copperState >= 0 && !world.isRemote) CopperOxidation.tick(world, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        return copperState >= 0 && CopperOxidation.interact(world, x, y, z, player);
    }
}
