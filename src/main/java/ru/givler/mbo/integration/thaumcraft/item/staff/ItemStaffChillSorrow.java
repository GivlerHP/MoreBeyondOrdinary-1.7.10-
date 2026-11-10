package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.givler.mbo.entity.magic.EntityIceCharge;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;

public class ItemStaffChillSorrow extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon frostChillSorrowIcon;

    private static final float DAMAGE_MULTIPLIER = 1.0F;
    private static final float BLAST_MULTIPLIER = 1.0F;

    public ItemStaffChillSorrow() {
        super();
        this.setUnlocalizedName("staffChillSorrow");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffChillSorrow.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.frostChillSorrowIcon = reg.registerIcon("mbo:staff/staffChillSorrow");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.frostChillSorrowIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.frostChillSorrowIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 150 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.WATER, 100).add(Aspect.AIR, 25);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        ItemStack handled = interactWithWandable(itemstack, world, player);
        if (handled != null) return handled;

        if (this.consumeAllVis(itemstack, player, this.getVisCost(itemstack), !world.isRemote, false)) {
            if (!world.isRemote) {
                EntityIceCharge iceCharge = new EntityIceCharge(world, player, DAMAGE_MULTIPLIER, BLAST_MULTIPLIER);
                world.spawnEntityInWorld(iceCharge);
            }
            player.swingItem();
            world.playSoundAtEntity(player, "mbo:ice", 1.0F, world.rand.nextFloat() * 0.4F + 1.4F);
        }

        return itemstack;
    }
}

