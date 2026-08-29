package ru.givler.mbo.handler;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.banner.ContainerLoom;
import ru.givler.mbo.banner.GuiLoom;
import ru.givler.mbo.client.gui.*;
import ru.givler.mbo.container.ContainerArcanum;
import ru.givler.mbo.container.ContainerLockConfig;
import ru.givler.mbo.container.ContainerLockpicking;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.stonecutter.ContainerStonecutter;
import ru.givler.mbo.stonecutter.GuiStonecutter;
import ru.givler.mbo.tileentity.*;

public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        MboGui gui = MboGui.byId(id);
        if (gui == null) return null;
        TileEntity tile = world.getTileEntity(x, y, z);
        switch (gui) {
            case INFUSION_WORKBENCH:
                return tile instanceof TileEntityArcanum ? new ContainerArcanum(player.inventory, (TileEntityArcanum) tile) : null;
            case LOOT_CONTAINER_CONFIG:
                return null;
            case LOOM:
                return new ContainerLoom(player.inventory, world, x, y, z);
            case STONECUTTER:
                return new ContainerStonecutter(player.inventory, world, x, y, z);
            case BARREL:
                return tile instanceof TileEntityBarrel ? new ContainerChest(player.inventory, (TileEntityBarrel) tile) : null;
            case LOCKPICKING:
                return tile instanceof ILockableTile ? new ContainerLockpicking(player.inventory) : null;
            case LOCKABLE_CHEST:
                return tile instanceof TileEntityLockableChest ? new ContainerChest(player.inventory, (TileEntityLockableChest) tile) : null;
            case LOCK_CONFIG:
                if (tile instanceof TileEntityLockableChest) return new ContainerLockConfig(player.inventory, (TileEntityLockableChest) tile);
                return tile instanceof ILockableTile ? new ContainerLockpicking(player.inventory) : null;
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        MboGui gui = MboGui.byId(id);
        if (gui == null) return null;
        TileEntity tile = world.getTileEntity(x, y, z);
        switch (gui) {
            case INFUSION_WORKBENCH:
                return tile instanceof TileEntityArcanum ? new GuiArcanum(player.inventory, (TileEntityArcanum) tile) : null;
            case LOOT_CONTAINER_CONFIG:
                if (tile instanceof TileEntityLootContainer) return new GuiLootContainerConfig(player, (TileEntityLootContainer) tile);
                ItemStack held = player.getCurrentEquippedItem();
                return held != null && held.getItem() instanceof ItemBlockLootContainer ? new GuiLootContainerConfig(player, null) : null;
            case LOOM:
                return new GuiLoom(player.inventory, world, x, y, z);
            case STONECUTTER:
                return new GuiStonecutter(player.inventory, world, x, y, z);
            case BARREL:
                return tile instanceof TileEntityBarrel ? new GuiChest(player.inventory, (TileEntityBarrel) tile) : null;
            case LOCKPICKING:
                return tile instanceof ILockableTile ? new GuiLockpicking(x, y, z, ((ILockableTile) tile).getLockData().getDifficulty().pinCount) : null;
            case LOCKABLE_CHEST:
                return tile instanceof TileEntityLockableChest ? new GuiLockableChest(player.inventory, (TileEntityLockableChest) tile) : null;
            case LOCK_CONFIG:
                if (tile instanceof TileEntityLockableChest) return new GuiLockChestConfig(player.inventory, (TileEntityLockableChest) tile);
                if (tile instanceof ILockableTile) {
                    ILockableTile lockable = (ILockableTile) tile;
                    return new GuiLockConfig(x, y, z, lockable.getLockData().getDifficulty().ordinal(),
                            lockable.getLockData().getRelockDelaySec(), lockable.getLockData().getPlayerRadius());
                }
                return null;
            default:
                return null;
        }
    }
}
