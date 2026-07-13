package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.entity.projectile.EntityThunderbolt;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;

public class ItemStaffLightningDragon extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon lightDragonIcon;

    private static final float DAMAGE_MULTIPLIER = 2.0F;
    private static final float BOLT_SPEED = 2.5F;
    private static final int BOLT_COUNT = 5;
    private static final double SPAWN_HEIGHT = 15.0D;
    private static final double SPREAD = 5.0D;
    private static final double CAST_RANGE = 32.0D;

    public ItemStaffLightningDragon() {
        super();
        this.setUnlocalizedName("staffLightningDragon");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffLightningDragon.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.lightDragonIcon = reg.registerIcon("mbo:staff/staffLightningDragon");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.lightDragonIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.lightDragonIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 150 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.AIR, 100).add(Aspect.ENTROPY, 35);
    }

    /**
     * Ищет живую сущность прямо на линии взгляда игрока в пределах range.
     * Возвращает ближайшую подходящую сущность или null, если ничего не найдено.
     */
    private Entity getEntityLookedAt(World world, EntityPlayer player, double range) {
        Vec3 look = player.getLookVec();
        Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);

        AxisAlignedBB searchBox = player.boundingBox.addCoord(look.xCoord * range, look.yCoord * range, look.zCoord * range).expand(1.0D, 1.0D, 1.0D);
        List list = world.getEntitiesWithinAABBExcludingEntity(player, searchBox);

        double closestDist = range;
        Entity closestEntity = null;

        for (Object o : list) {
            Entity e = (Entity) o;
            if (!(e instanceof EntityLivingBase) || !e.canBeCollidedWith()) {
                continue;
            }
            float border = e.getCollisionBorderSize();
            AxisAlignedBB aabb = e.boundingBox.expand((double) border, (double) border, (double) border);
            MovingObjectPosition hit = aabb.calculateIntercept(start, end);

            if (hit != null) {
                double dist = start.distanceTo(hit.hitVec);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestEntity = e;
                }
            }
        }

        return closestEntity;
    }

    /**
     * Точка прицеливания: сначала проверяем сущность на линии взгляда,
     * если её нет - ищем блок, иначе - точка на максимальной дальности.
     */
    private Vec3 getAimedPoint(World world, EntityPlayer player, double range) {
        Entity target = this.getEntityLookedAt(world, player, range);
        if (target != null) {
            double centerY = (target.boundingBox.minY + target.boundingBox.maxY) / 2.0D;
            return Vec3.createVectorHelper(target.posX, centerY, target.posZ);
        }

        Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLook(1.0F);
        Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);

        MovingObjectPosition blockHit = world.rayTraceBlocks(start, end, false);
        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return blockHit.hitVec;
        }
        return end;
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

        Vec3 aim = this.getAimedPoint(world, player, CAST_RANGE);

        if (this.consumeAllVis(itemstack, player, this.getVisCost(itemstack), !world.isRemote, false)) {
            if (!world.isRemote) {
                world.playSoundAtEntity(player, "ambient.weather.thunder", 1.5F, world.rand.nextFloat() * 0.2F + 0.8F);

                double targetX = aim.xCoord;
                double targetY = aim.yCoord;
                double targetZ = aim.zCoord;

                for (int b = 0; b < BOLT_COUNT; ++b) {
                    double offsetX = (world.rand.nextDouble() - world.rand.nextDouble()) * SPREAD;
                    double offsetZ = (world.rand.nextDouble() - world.rand.nextDouble()) * SPREAD;

                    double startX = targetX + offsetX;
                    double startY = targetY + SPAWN_HEIGHT;
                    double startZ = targetZ + offsetZ;

                    EntityThunderbolt thunderbolt = new EntityThunderbolt(world, player, DAMAGE_MULTIPLIER);
                    thunderbolt.setPosition(startX, startY, startZ);

                    Vec3 dir = Vec3.createVectorHelper(targetX - startX, targetY - startY, targetZ - startZ).normalize();
                    thunderbolt.motionX = dir.xCoord * BOLT_SPEED;
                    thunderbolt.motionY = dir.yCoord * BOLT_SPEED;
                    thunderbolt.motionZ = dir.zCoord * BOLT_SPEED;

                    world.spawnEntityInWorld(thunderbolt);
                }
            }
            player.swingItem();
        }

        return itemstack;
    }
}