package ru.givler.mbo.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.container.loom.ContainerLoom;
import ru.givler.mbo.client.gui.GuiLoom;
import ru.givler.mbo.client.gui.*;
import ru.givler.mbo.container.ContainerArcanum;
import ru.givler.mbo.container.ContainerLockConfig;
import ru.givler.mbo.container.ContainerLockpicking;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.container.stonecutter.ContainerStonecutter;
import ru.givler.mbo.client.gui.GuiStonecutter;
import ru.givler.mbo.tileentity.*;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.tileentity.TileEntityPlatformStation;
import ru.givler.mbo.container.ContainerPlatformStation;

public class MboGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        MboGuiType gui = MboGuiType.byId(id);
        if (gui == null) return null;
        if (gui == MboGuiType.WARDROBE) return new ru.givler.mbo.container.ContainerWardrobe(player);
        TileEntity tile = world.getTileEntity(x, y, z);
        switch (gui) {
            case DOLL:
                return tile instanceof TileDoll && ((TileDoll) tile).isUseableByPlayer(player)
                    && ru.givler.mbo.block.BlockDoll.canEdit(player, world, x, y, z, 1)
                    ? new ru.givler.mbo.container.ContainerDoll(player, (TileDoll) tile) : null;
            case DESCRIPTION_WORKBENCH:
                return tile instanceof TileEntityDescriptionWorkbench && TileEntityDescriptionWorkbench.canEdit(player)
                        ? new ru.givler.mbo.container.ContainerDescriptionWorkbench(player.inventory, (TileEntityDescriptionWorkbench) tile) : null;
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
                return tile instanceof ILockableTile ? new ContainerLockpicking(player.inventory, (ILockableTile) tile) : null;
            case LOCKABLE_CHEST:
                return tile instanceof TileEntityLockableChest ? new ContainerChest(player.inventory, (TileEntityLockableChest) tile) : null;
            case LOCK_CONFIG:
                if (tile instanceof TileEntityLockableChest) return new ContainerLockConfig(player.inventory, (TileEntityLockableChest) tile);
                return tile instanceof ILockableTile ? new ContainerLockpicking(player.inventory) : null;
            case MOVING_PLATFORM:
                return null;
            case PLATFORM_STATION:
                return tile instanceof TileEntityPlatformStation
                        ? new ContainerPlatformStation(player.inventory, (TileEntityPlatformStation) tile) : null;
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        MboGuiType gui = MboGuiType.byId(id);
        if (gui == null) return null;
        if (gui == MboGuiType.WARDROBE) return new GuiWardrobe(player);
        TileEntity tile = world.getTileEntity(x, y, z);
        switch (gui) {
            case DOLL:
                return tile instanceof TileDoll ? new GuiDoll(player, (TileDoll) tile) : null;
            case DESCRIPTION_WORKBENCH:
                return tile instanceof TileEntityDescriptionWorkbench
                        ? new GuiDescriptionWorkbench(player.inventory, (TileEntityDescriptionWorkbench) tile) : null;
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
            case MOVING_PLATFORM:
                net.minecraft.entity.Entity entity = world.getEntityByID(x);
                return entity instanceof EntityMovingPlatform ? new GuiMovingPlatform((EntityMovingPlatform) entity) : null;
            case PLATFORM_STATION:
                return tile instanceof TileEntityPlatformStation
                        ? new GuiPlatformStation(player.inventory, (TileEntityPlatformStation) tile) : null;
            default:
                return null;
        }
    }
}
