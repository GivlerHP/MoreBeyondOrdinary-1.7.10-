package ru.givler.mbo.handler;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.gui.GuiArcanum;
import ru.givler.mbo.gui.GuiLootContainerConfig;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.tileentity.TileEntityArcanum;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import ru.givler.mbo.banner.ContainerLoom;
import ru.givler.mbo.banner.GuiLoom;
import ru.givler.mbo.stonecutter.ContainerStonecutter;
import ru.givler.mbo.stonecutter.GuiStonecutter;
import ru.givler.mbo.container.ContainerArcanum;

public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == MoreBeyondOrdinary.GUI_LOOM) return new ContainerLoom(player.inventory, world, x, y, z);
        if (ID == MoreBeyondOrdinary.GUI_STONECUTTER) return new ContainerStonecutter(player.inventory, world, x, y, z);
        if (ID == MoreBeyondOrdinary.GUI_LOOT_CONTAINER_CONFIG) {
            return null;
        }
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityArcanum) {
            return new ContainerArcanum(player.inventory, (TileEntityArcanum) tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == MoreBeyondOrdinary.GUI_LOOM) return new GuiLoom(player.inventory, world, x, y, z);
        if (ID == MoreBeyondOrdinary.GUI_STONECUTTER) return new GuiStonecutter(player.inventory, world, x, y, z);
        if (ID == MoreBeyondOrdinary.GUI_LOOT_CONTAINER_CONFIG) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityLootContainer) {
                return new GuiLootContainerConfig(player, (TileEntityLootContainer) tile);
            }
            ItemStack held = player.getCurrentEquippedItem();
            if (held != null && held.getItem() instanceof ItemBlockLootContainer) {
                return new GuiLootContainerConfig(player, null);
            }
            return null;
        }
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityArcanum) {
            return new GuiArcanum(player.inventory, (TileEntityArcanum) tile);
        }
        return null;
    }
}
