package ru.givler.mbo.integration.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.block.model.BlockModelCollision;

public final class ModelCollisionWailaProvider implements IWailaDataProvider {
    private static final ModelCollisionWailaProvider INSTANCE = new ModelCollisionWailaProvider();

    public static void register(IWailaRegistrar registrar) {
        registrar.registerStackProvider(INSTANCE, BlockModelCollision.class);
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!(accessor.getBlock() instanceof BlockModelCollision)) return null;
        return ((BlockModelCollision) accessor.getBlock()).getOwnerStack(
                accessor.getWorld(), accessor.getPosition().blockX, accessor.getPosition().blockY,
                accessor.getPosition().blockZ, accessor.getPlayer());
    }

    @Override public List<String> getWailaHead(ItemStack stack, List<String> tip,
            IWailaDataAccessor accessor, IWailaConfigHandler config) { return tip; }
    @Override public List<String> getWailaBody(ItemStack stack, List<String> tip,
            IWailaDataAccessor accessor, IWailaConfigHandler config) { return tip; }
    @Override public List<String> getWailaTail(ItemStack stack, List<String> tip,
            IWailaDataAccessor accessor, IWailaConfigHandler config) { return tip; }
    @Override public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag,
            World world, int x, int y, int z) { return tag; }
}
