package ru.givler.mbo.block;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

/**
 * A compact mushroom-cap block with the same landing behaviour as a slime block.
 * The icon is borrowed directly from metadata 14 of the vanilla huge mushroom.
 */
public class BlockBouncyMushroom extends BlockSlimeMBO {
    private final Block mushroom;

    public BlockBouncyMushroom(String name, Block mushroom) {
        super(name, "minecraft:mushroom_block_skin_brown");
        this.mushroom = mushroom;
        setHardness(0.2F);
        setResistance(1.0F);
        setStepSound(Block.soundTypeWood);
    }

    @Override public IIcon getIcon(int side, int meta) {
        return mushroom.getIcon(side, 14);
    }

    @Override public boolean isOpaqueCube() { return true; }
    @Override public boolean renderAsNormalBlock() { return true; }
    @Override public int getRenderBlockPass() { return 0; }
    @Override public int getRenderType() { return 0; }

    public static BlockBouncyMushroom brown() {
        return new BlockBouncyMushroom("BouncyBrownMushroomBlock", Blocks.brown_mushroom_block);
    }

    public static BlockBouncyMushroom red() {
        return new BlockBouncyMushroom("BouncyRedMushroomBlock", Blocks.red_mushroom_block);
    }
}
