package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.givler.mbo.entity.magic.EntityIceShard;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;

public class ItemStaffFrost extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon frostIcon;

    private static final float DAMAGE_MULTIPLIER = 1.0F;
    private static final float RANGE_MULTIPLIER = 1.0F;

    public ItemStaffFrost() {
        super();
        this.setUnlocalizedName("staffFrost");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffFrost.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.frostIcon = reg.registerIcon("mbo:staff/staffFrost");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.frostIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.frostIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 150 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.WATER, 25).add(Aspect.AIR, 10);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        ItemStack handled = interactWithWandable(itemstack, world, player);
        if (handled != null) return handled;

        if (this.consumeAllVis(itemstack, player, this.getVisCost(itemstack), !world.isRemote, false)) {
            if (!world.isRemote) {
                EntityIceShard iceShard = new EntityIceShard(
                        world, player, 2 * RANGE_MULTIPLIER, DAMAGE_MULTIPLIER);
                world.spawnEntityInWorld(iceShard);
                world.playSoundAtEntity(player, "mbo:ice", 1.0F,
                        world.rand.nextFloat() * 0.4F + 1.4F);
            }
            player.swingItem();
        }

        return itemstack;
    }
}

