package ru.givler.mbo.lockable;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.handler.MboGui;
import ru.givler.mbo.config.LockSecurityConfig;

public final class LockableAccess {
    private LockableAccess() {}

    public static boolean isAdminKey(EntityPlayer player) {
        ItemStack held = player == null ? null : player.getCurrentEquippedItem();
        return player != null && player.capabilities.isCreativeMode && held != null
                && held.getItem() == ItemRegistry.AdminKey && LockSecurityConfig.isAuthorized(player);
    }

    public static boolean hasLockpick(EntityPlayer player) {
        ItemStack held = player == null ? null : player.getCurrentEquippedItem();
        return held != null && held.getItem() == ItemRegistry.Lockpick;
    }

    public static ILockableTile get(World world, int x, int y, int z) {
        TileEntity tile = world == null ? null : world.getTileEntity(x, y, z);
        return tile instanceof ILockableTile ? (ILockableTile) tile : null;
    }

    public static void playLockedSound(World world, int x, int y, int z) {
        if (world == null || world.isRemote) return;
        String sound = Loader.isModLoaded("Thaumcraft") ? "thaumcraft:doorfail" : "random.click";
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, sound, 0.8F, 0.9F);
    }

    public static boolean openLockpicking(EntityPlayer player, ILockableTile lockable) {
        if (!hasLockpick(player) || lockable == null || !lockable.getLockData().isLocked()) return false;
        TileEntity tile = lockable.asTileEntity();
        player.openGui(MoreBeyondOrdinary.instance, MboGui.LOCKPICKING.id,
                tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        return true;
    }

    public static boolean configureWithAdminKey(EntityPlayer player, ILockableTile lockable) {
        if (!isAdminKey(player) || !player.isSneaking()) return false;
        TileEntity changed = lockable.asTileEntity();
        player.openGui(MoreBeyondOrdinary.instance, MboGui.LOCK_CONFIG.id,
                changed.getWorldObj(), changed.xCoord, changed.yCoord, changed.zCoord);
        return true;
    }
}
