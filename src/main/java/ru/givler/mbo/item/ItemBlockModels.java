package ru.givler.mbo.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;

public class ItemBlockModels extends ItemBlock {
    public ItemBlockModels(Block block) { super(block); }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world,
                                int x, int y, int z, int side,
                                float hitX, float hitY, float hitZ, int metadata) {
        BlockModels model = (BlockModels) field_150939_a;
        if (!model.canPlaceStructureAt(world, x, y, z, player)) return false;
        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    }
}
