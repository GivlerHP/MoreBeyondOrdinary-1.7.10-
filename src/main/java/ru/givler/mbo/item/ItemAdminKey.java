package ru.givler.mbo.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketOpenLockConfig;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemAdminKey extends Item {
    public ItemAdminKey() {
        setUnlocalizedName("AdminKey");
        setTextureName(MoreBeyondOrdinary.MODID + ":admin_key");
        setCreativeTab(CreativeTabRegistry.tabMBOitems);
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world,
                                  int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) return false;

        if (world.getBlock(x, y, z) == BlockRegistry.LockableDoor
                && (world.getBlockMetadata(x, y, z) & 8) != 0) {
            y--;
        }

        boolean lockableBlock = world.getBlock(x, y, z) == BlockRegistry.LockableDoor
                || world.getBlock(x, y, z) == BlockRegistry.LockableTrapdoor
                || world.getBlock(x, y, z) == BlockRegistry.LockableChest;
        if (!lockableBlock) return false;

        if (world.isRemote) {
            PacketManager.INSTANCE.sendToServer(new PacketOpenLockConfig(x, y, z));
        }
        return true;
    }
}
