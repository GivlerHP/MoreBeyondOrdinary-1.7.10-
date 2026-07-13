package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.integration.thaumcraft.entities.EntityPechShatter;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;

public class ItemStaffNaturalMoon extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon narureMoonIcon;

    private static final float DAMAGE_MULTIPLIER = 1.0F;
    private static final float BLAST_MULTIPLIER = 1.0F;

    public ItemStaffNaturalMoon() {
        super();
        this.setUnlocalizedName("staffNaturalMoon");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffNaturalMoon.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.narureMoonIcon = reg.registerIcon("mbo:staff/staffNaturalMoon");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.narureMoonIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.narureMoonIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 125 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.WATER, 80).add(Aspect.EARTH, 80).add(Aspect.ENTROPY, 20);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            int i = mop.blockX;
            int j = mop.blockY;
            int k = mop.blockZ;
            Block bi = world.getBlock(i, j, k);

            if (bi instanceof IWandable) {
                ItemStack is = ((IWandable) bi).onWandRightClick(world, itemstack, player);
                if (is != null) {
                    return is;
                }
            }

            TileEntity tile = world.getTileEntity(i, j, k);
            if (tile != null && tile instanceof IWandable) {
                ItemStack is = ((IWandable) tile).onWandRightClick(world, itemstack, player);
                if (is != null) {
                    return is;
                }
            }
        }

        if (this.consumeAllVis(itemstack, player, this.getVisCost(itemstack), !world.isRemote, false)) {
            if (!world.isRemote) {
                EntityPechShatter shatter = new EntityPechShatter(world, player, 0, 0, false);
                world.spawnEntityInWorld(shatter);
                world.playSoundAtEntity(player, "thaumcraft:wandfail", 1.0F,
                        0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
            }
            player.swingItem();
        }

        return itemstack;
    }
}
