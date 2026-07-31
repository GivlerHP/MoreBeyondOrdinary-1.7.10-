package ru.givler.mbo.banner;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemBlockBanner extends ItemBlock {
    private static final String[] COLORS = {"white","orange","magenta","lightBlue","yellow","lime","pink","gray","silver","cyan","purple","blue","brown","green","red","black"};

    public ItemBlockBanner(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(16);
    }

    @Override public int getMetadata(int damage) { return damage; }

    @Override public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < 16; i++) list.add(new ItemStack(item, 1, i));
    }

    @Override public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.banner." + COLORS[BannerData.getBaseColor(stack)] + ".name");
    }

    @Override public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        NBTTagList patterns = BannerData.getPatterns(stack, false);
        if (patterns == null) return;
        for (int i = 0; i < patterns.tagCount() && i < 6; i++) {
            NBTTagCompound pattern = patterns.getCompoundTagAt(i);
            BannerPattern type = BannerPattern.byId(pattern.getString("Pattern"));
            if (type != null) list.add(StatCollector.translateToLocal("item.banner.pattern." + type.texture));
        }
    }

    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                                       int side, float hitX, float hitY, float hitZ) {
        if (world.getBlock(x, y, z) == Blocks.cauldron && world.getBlockMetadata(x, y, z) > 0
                && BannerData.getPatternCount(stack) > 0) {
            NBTTagList patterns = BannerData.getPatterns(stack, false);
            patterns.removeTag(patterns.tagCount() - 1);
            world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 1, 3);
            return true;
        }
        if (side == 0 || !world.getBlock(x, y, z).getMaterial().isSolid()) return false;
        if (side == 1) y++; else if (side == 2) z--; else if (side == 3) z++; else if (side == 4) x--; else x++;
        if (!player.canPlayerEdit(x, y, z, side, stack) || !world.isAirBlock(x, y, z)) return false;
        int meta = side == 1 ? MathHelper.floor_double((player.rotationYaw + 180) * 16 / 360 + .5) & 15 : side;
        if (!world.setBlock(x, y, z, field_150939_a, meta, 3)) return false;
        TileEntityBanner tile = (TileEntityBanner) world.getTileEntity(x, y, z);
        if (tile != null) {
            tile.standing = side == 1;
            tile.setItemValues(stack);
        }
        if (!player.capabilities.isCreativeMode) stack.stackSize--;
        return true;
    }
}
