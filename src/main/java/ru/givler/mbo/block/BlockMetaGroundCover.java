package ru.givler.mbo.block;

import net.minecraft.block.Block;

/** A cover with a fixed source variant and independent layer metadata. */
public final class BlockMetaGroundCover extends BlockGroundCover {
    public BlockMetaGroundCover(Block source, int sourceMeta, boolean tintedTop) {
        super(source, sourceMeta, tintedTop);
    }
    public BlockMetaGroundCover(Block source, int sourceMeta, boolean tintedTop, SurfaceEffect effect) {
        super(source, sourceMeta, tintedTop, effect);
    }
}
