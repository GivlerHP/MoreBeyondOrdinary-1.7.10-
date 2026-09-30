package ru.givler.mbo.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockLeafLitter;

/** Clicking an existing patch adds one quarter instead of replacing it. */
public final class ItemLeafLitter extends ItemBlock {
    @SideOnly(Side.CLIENT)
    private IIcon itemIcon;

    public ItemLeafLitter(Block block) { super(block); }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (stack.stackSize <= 0) return false;
        // A leaf patch is nearly flat. Depending on the ray angle Minecraft
        // can target the supporting block instead of the patch above it.
        if (side == 1 && world.getBlock(x, y + 1, z) == field_150939_a) y++;
        if (world.getBlock(x, y, z) == field_150939_a) {
            return ((BlockLeafLitter) field_150939_a).addLayer(world, x, y, z, side, player, stack);
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world,
                                int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        int rotation = MathHelper.floor_double(player.rotationYaw * 4F / 360F + .5D) & 3;
        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, rotation << 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("mbo:foliage/leaf_litter");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) { return itemIcon; }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        return field_150939_a.getRenderColor(0);
    }
}
