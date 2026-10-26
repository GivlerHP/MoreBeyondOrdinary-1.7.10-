package ru.givler.mbo.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.BlockConnectedRope;

public class ItemBlockModels extends ItemBlock {
    public ItemBlockModels(Block block) { super(block); }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        super.addInformation(stack, player, lines, advanced);
        addModelDescription(field_150939_a, lines);
    }

    public static void addModelDescription(Block block, List lines) {
        String key = block.getUnlocalizedName() + ".desc";
        if (!StatCollector.canTranslate(key)) return;
        for (String line : StatCollector.translateToLocal(key).replace("\\n", "\n").split("\n")) {
            if (!line.isEmpty()) lines.add(EnumChatFormatting.GRAY + line);
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Block target = world.getBlock(x, y, z);
        if (field_150939_a == ru.givler.mbo.registry.ModelRegistry.ModelTortureRope3
                && target instanceof BlockConnectedRope)
            return ((BlockConnectedRope) target).trySetAnchor(stack,player,world,x,y,z,side);
        if (field_150939_a instanceof BlockConnectedRope && target instanceof BlockConnectedRope
                && ((BlockConnectedRope) target).tryDecorate(stack, player, world, x, y, z, side)) {
            return true;
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world,
                                int x, int y, int z, int side,
                                float hitX, float hitY, float hitZ, int metadata) {
        BlockModels model = (BlockModels) field_150939_a;
        if (!model.canPlaceStructureAt(world, x, y, z, player)) return false;
        boolean placed=super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
        if(placed && model instanceof BlockConnectedRope)
            ((BlockConnectedRope)model).initializePlacement(world,x,y,z,side,player);
        return placed;
    }
}
