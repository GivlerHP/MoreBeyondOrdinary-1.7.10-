package ru.givler.mbo.integration.thaumcraft.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import thaumcraft.common.lib.utils.ProtectionUtils;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

/**
 * Снаряд для ItemStaffDarkMoon.
 * Если задана цель (target != null) - самонаводится на неё и наносит урон при попадании.
 * Если цели нет - летит по прямой вперёд и просто исчезает через короткое время (без урона по прямому пути истечения времени).
 */
public class EntityDarkMoonOrb extends EntityThrowable {

    private EntityLivingBase target;
    private int strength = 0;

    private static final double HOMING_SPEED = 0.6D;
    private static final int HOMING_RECALC_TICKS = 5;
    private static final int HOMING_MAX_LIFETIME = 100;
    private static final int STRAIGHT_MAX_LIFETIME = 40;

    public EntityDarkMoonOrb(World world) {
        super(world);
    }

    /** Самонаводящийся снаряд. */
    public EntityDarkMoonOrb(World world, EntityLivingBase thrower, EntityLivingBase target, int strength) {
        super(world, thrower);
        this.target = target;
        this.strength = strength;
        initThrow(thrower);
    }

    /** Снаряд без цели - летит по прямой и исчезает. */
    public EntityDarkMoonOrb(World world, EntityLivingBase thrower, int strength) {
        super(world, thrower);
        this.target = null;
        this.strength = strength;
        initThrow(thrower);
    }

    private void initThrow(EntityLivingBase thrower) {
        Vec3 vec = thrower.getLookVec();
        setLocationAndAngles(thrower.posX + vec.xCoord / 2.0D, thrower.posY + thrower.getEyeHeight() + vec.yCoord / 2.0D,
                thrower.posZ + vec.zCoord / 2.0D, thrower.rotationYaw, thrower.rotationPitch);

        float f = 0.6F;
        float ry = thrower.rotationYaw;
        float rp = thrower.rotationPitch;
        motionX = -MathHelper.sin(ry / 180.0F * (float) Math.PI) * MathHelper.cos(rp / 180.0F * (float) Math.PI) * f;
        motionZ = MathHelper.cos(ry / 180.0F * (float) Math.PI) * MathHelper.cos(rp / 180.0F * (float) Math.PI) * f;
        motionY = -MathHelper.sin(rp / 180.0F * (float) Math.PI) * f;
    }

    @Override
    protected float getGravityVelocity() {
        return 0.0F;
    }

    @Override
    public void onUpdate() {
        double motion = Math.abs(motionX) + Math.abs(motionY) + Math.abs(motionZ);
        if (motion > 0.0D && worldObj.isRemote) {
            Thaumcraft.proxy.sparkle((float) posX + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.25F,
                    (float) posY + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.25F,
                    (float) posZ + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.25F, 0.5F, 0, 0.0F);
        }

        super.onUpdate();

        if (target != null) {
            // самонаводящийся режим
            if (!worldObj.isRemote && (target.isDead || target.getDistanceSqToEntity(this) > 1250.0D)) {
                setDead();
                return;
            }

            if (!worldObj.isRemote && ticksExisted % HOMING_RECALC_TICKS == 0 && !target.isDead) {
                double d = getDistanceToEntity(target);
                if (d > 0.0D) {
                    double dx = (target.posX - posX) / d;
                    double dy = (target.boundingBox.minY + target.height * 0.6D - posY) / d;
                    double dz = (target.posZ - posZ) / d;

                    motionX = dx * HOMING_SPEED;
                    motionY = dy * HOMING_SPEED;
                    motionZ = dz * HOMING_SPEED;
                }
            }

            if (ticksExisted > HOMING_MAX_LIFETIME) {
                setDead();
            }
        } else {
            // без цели - просто летит вперёд и исчезает
            if (ticksExisted > STRAIGHT_MAX_LIFETIME) {
                setDead();
            }
        }
    }

    @Override
    protected void onImpact(MovingObjectPosition mop) {
        if (!worldObj.isRemote) {
            if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                    && (getThrower() == null || mop.entityHit != getThrower())) {
                if (ProtectionUtils.canEntityDamage(this.getThrower(), mop.entityHit)) {
                    mop.entityHit.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, getThrower()),
                            2.0F + strength * 0.5F);
                }
                worldObj.playSoundAtEntity(this, "thaumcraft:zap", 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
            }
            setDead();
        }
    }
}
