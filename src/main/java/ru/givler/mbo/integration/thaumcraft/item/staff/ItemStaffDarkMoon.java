package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.integration.thaumcraft.client.DarkMoonStaffClientFX;
import ru.givler.mbo.integration.thaumcraft.util.DarkMoonCastQueue;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.EntityUtils;

public class ItemStaffDarkMoon extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon darkMoonIcon;

    static final int MAX_SPHERES = 8;
    static final int TICKS_PER_SPHERE = 8;
    private static final int LAUNCH_INTERVAL_TICKS = 4;
    private static final int STRENGTH = 0;

    private static final String TAG_PAID_SPHERES = "darkMoonPaidSpheres";

    public ItemStaffDarkMoon() {
        super();
        this.setUnlocalizedName("staffDarkMoon");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffDarkMoon.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.darkMoonIcon = reg.registerIcon("mbo:staff/staffDarkMoon");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.darkMoonIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.darkMoonIcon;
    }

    @Override
    public int getMaxVis(ItemStack stack) {
        return 150 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.ORDER, 20).add(Aspect.ENTROPY, 20);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.bow;
    }

    private int getSphereCountForTicks(int ticksUsed) {
        int count = 1 + ticksUsed / TICKS_PER_SPHERE;
        return Math.min(count, MAX_SPHERES);
    }

    private int getPaidSpheres(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }
        return stack.stackTagCompound.getInteger(TAG_PAID_SPHERES);
    }

    private void setPaidSpheres(ItemStack stack, int value) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.stackTagCompound.setInteger(TAG_PAID_SPHERES, value);
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

        if (!world.isRemote) {
            setPaidSpheres(itemstack, 0);
        }

        player.setItemInUse(itemstack, this.getMaxItemUseDuration(itemstack));
        return itemstack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int maxDuration = this.getMaxItemUseDuration(stack);
        int ticksUsed = maxDuration - count;
        int desiredSpheres = getSphereCountForTicks(ticksUsed);

        if (!player.worldObj.isRemote) {
            World world = player.worldObj;

            int paid = getPaidSpheres(stack);
            while (paid < desiredSpheres) {
                if (this.consumeAllVis(stack, player, this.getVisCost(stack), true, false)) {
                    paid++;
                } else {
                    break;
                }
            }
            setPaidSpheres(stack, paid);

            if (ticksUsed % 6 == 0) {
                world.playSoundAtEntity(player, "mbo:shard", 0.15F,
                        1.6F + world.rand.nextFloat() * 0.2F);
            }

            if (ticksUsed > 0 && ticksUsed % TICKS_PER_SPHERE == 0) {
                world.playSoundAtEntity(player, "mbo:shard", 0.4F,
                        0.8F + world.rand.nextFloat() * 0.1F);
            }
            return;
        }

        int paidClientSide = getPaidSpheres(stack);
        int visibleSpheres = (paidClientSide == 0) ? desiredSpheres : Math.min(desiredSpheres, paidClientSide);

        DarkMoonStaffClientFX.renderOrbitingSpheres(player, ticksUsed, visibleSpheres);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft) {
        if (world.isRemote) {
            return;
        }

        int sphereCount = getPaidSpheres(stack);
        setPaidSpheres(stack, 0);

        if (sphereCount <= 0) {
            return;
        }

        Entity look = EntityUtils.getPointedEntity(player.worldObj, player, 0.0D, 32.0D, 1.1F);
        EntityLivingBase target = (look instanceof EntityLivingBase) ? (EntityLivingBase) look : null;

        DarkMoonCastQueue.queueCast(world, player, target, sphereCount, LAUNCH_INTERVAL_TICKS, STRENGTH);

        player.swingItem();
    }
}