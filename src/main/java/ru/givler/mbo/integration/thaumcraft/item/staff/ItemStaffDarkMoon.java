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
import ru.givler.mbo.integration.thaumcraft.util.DarkMoonCastQueue;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandable;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSparkle;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.EntityUtils;

public class ItemStaffDarkMoon extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon darkMoonIcon;

    private static final int MAX_SPHERES = 8;
    private static final int TICKS_PER_SPHERE = 8;      // сколько тиков удержания требуется на каждую следующую сферу
    private static final int LAUNCH_INTERVAL_TICKS = 4; // интервал между запусками сфер при отпускании (0.2 сек)
    private static final int STRENGTH = 0;              // передаётся в снаряд (влияет на доп. урон)
    private static final double ORBIT_RADIUS = 1.3D;
    private static final double ORBIT_ROTATION_SPEED = 0.15D;

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

    /**
     * Стоимость виса ЗА ОДНУ сферу (списывается по мере зарядки, а не одной суммой в конце).
     */
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

    /**
     * Вызывается каждый тик, пока игрок держит ПКМ.
     * На сервере - постепенно списывает вис за каждую новую сферу (если виса не хватает, зарядка перестаёт расти).
     * На клиенте - рисует кружащиеся сферы и проигрывает звуки зарядки.
     */
    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int maxDuration = this.getMaxItemUseDuration(stack);
        int ticksUsed = maxDuration - count;
        int desiredSpheres = getSphereCountForTicks(ticksUsed);

        if (!player.worldObj.isRemote) {

            int paid = getPaidSpheres(stack);
            while (paid < desiredSpheres) {
                if (this.consumeAllVis(stack, player, this.getVisCost(stack), true, false)) {
                    paid++;
                } else {
                    break;
                }
            }
            setPaidSpheres(stack, paid);
            return;
        }

        int paidClientSide = getPaidSpheres(stack);
        int visibleSpheres = Math.max(1, Math.min(desiredSpheres, paidClientSide == 0 ? desiredSpheres : paidClientSide));

        double baseAngle = ticksUsed * ORBIT_ROTATION_SPEED;

        for (int i = 0; i < visibleSpheres; ++i) {
            double angle = baseAngle + (2.0D * Math.PI * i / visibleSpheres);
            double ox = Math.cos(angle) * ORBIT_RADIUS;
            double oz = Math.sin(angle) * ORBIT_RADIUS;

            double px = player.posX + ox;
            double py = player.posY + player.getEyeHeight() - 0.2D;
            double pz = player.posZ + oz;

            FXSparkle fx = new FXSparkle(player.worldObj, px, py, pz, 1.2F, 0, 5);
            fx.setGravity(0.0F);
            ParticleEngine.instance.addEffect(player.worldObj, fx);
        }

        if (ticksUsed % 6 == 0) {
            player.worldObj.playSound(player.posX, player.posY, player.posZ,
                    "mbo:shard", 0.15F, 1.6F + player.worldObj.rand.nextFloat() * 0.2F, false);
        }

        if (ticksUsed > 0 && ticksUsed % TICKS_PER_SPHERE == 0 && desiredSpheres <= MAX_SPHERES) {
            player.worldObj.playSound(player.posX, player.posY, player.posZ,
                    "mbo:shard", 0.4F, 0.8F + player.worldObj.rand.nextFloat() * 0.1F, false);
        }
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