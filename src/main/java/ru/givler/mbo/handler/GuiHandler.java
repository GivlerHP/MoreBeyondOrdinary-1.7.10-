package ru.givler.mbo.handler;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.gui.GuiArcanum;
import ru.givler.mbo.tileentity.TileEntityArcanum;
import ru.givler.mbo.сontainer.ContainerArcanum;

public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityArcanum) {
            return new ContainerArcanum(player.inventory, (TileEntityArcanum) tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityArcanum) {
            return new GuiArcanum(player.inventory, (TileEntityArcanum) tile);
        }
        return null;
    }
}