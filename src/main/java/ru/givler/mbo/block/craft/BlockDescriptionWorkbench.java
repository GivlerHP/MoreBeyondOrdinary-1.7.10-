package ru.givler.mbo.block.craft;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.block.BlockDecorModel;
import ru.givler.mbo.gui.MboGuiType;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;

public final class BlockDescriptionWorkbench extends BlockDecorModel {
    public BlockDescriptionWorkbench() {
        super(Material.wood, "DescriptionWorkbench", "description_workbench", "description_workbench");
        withMultipartCollisionBoxes(new float[][] {{0, 0, 0, 1, 1, 2}});
        withDynamicModelRender();
    }
    @Override protected int getPlacementFacing(net.minecraft.entity.EntityLivingBase placer) {
        return (super.getPlacementFacing(placer) + 3) & 3;
    }
    @Override public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityDescriptionWorkbench();
    }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
            int side, float hitX, float hitY, float hitZ) {
        if (!TileEntityDescriptionWorkbench.canEdit(player)) return true;
        if (!world.isRemote) player.openGui(MoreBeyondOrdinary.instance, MboGuiType.DESCRIPTION_WORKBENCH.id, world, x, y, z);
        return true;
    }
    @Override public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileEntityDescriptionWorkbench) {
            ItemStack stack = ((TileEntityDescriptionWorkbench) tile).getStackInSlotOnClosing(0);
            if (stack != null) world.spawnEntityInWorld(new EntityItem(world, x + .5, y + .7, z + .5, stack));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
