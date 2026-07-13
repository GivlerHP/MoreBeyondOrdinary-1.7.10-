package ru.givler.mbo.integration.thaumcraft.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityPechBlast;

public class EntityPechShatter extends EntityPechBlast {

    private static final int SHARD_COUNT = 8;
    private static final double SHARD_SPEED = 0.5D;

    private int myStrength = 0;
    private int myDuration = 0;
    private boolean myNightshade = false;
    private EntityLivingBase myThrower;

    public EntityPechShatter(World world) {
        super(world);
    }

    public EntityPechShatter(World world, EntityLivingBase thrower, int strength, int duration, boolean nightshade) {
        super(world, thrower, strength, duration, nightshade);
        this.myThrower = thrower;
        this.myStrength = strength;
        this.myDuration = duration;
        this.myNightshade = nightshade;
    }

    public EntityPechShatter(World world, double x, double y, double z, int strength, int duration, boolean nightshade) {
        super(world, x, y, z, strength, duration, nightshade);
        this.myStrength = strength;
        this.myDuration = duration;
        this.myNightshade = nightshade;
    }

    @Override
    protected void onImpact(MovingObjectPosition mop) {
        if (this.worldObj.isRemote) {
            for (int a = 0; a < 9; ++a) {
                float fx = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                float fy = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                float fz = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) fx, this.posY + (double) fy, this.posZ + (double) fz,
                        this.posX + (double) (fx * 8.0F), this.posY + (double) (fy * 8.0F), this.posZ + (double) (fz * 8.0F), 0.3F, 3, true, 0.02F);
                fx = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                fy = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                fz = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) fx, this.posY + (double) fy, this.posZ + (double) fz,
                        this.posX + (double) (fx * 8.0F), this.posY + (double) (fy * 8.0F), this.posZ + (double) (fz * 8.0F), 0.3F, 2, true, 0.02F);
                fx = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                fy = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                fz = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) fx, this.posY + (double) fy, this.posZ + (double) fz,
                        this.posX + (double) (fx * 8.0F), this.posY + (double) (fy * 8.0F), this.posZ + (double) (fz * 8.0F), 0.3F, 0, true, 0.02F);
            }
        }

        if (!this.worldObj.isRemote) {
            for (int i = 0; i < SHARD_COUNT; ++i) {
                double angle = 2.0D * Math.PI * i / SHARD_COUNT;
                double dx = Math.cos(angle);
                double dz = Math.sin(angle);

                EntityPechShard shard = new EntityPechShard(this.worldObj, this.myThrower, this.myStrength, this.myDuration, this.myNightshade);
                shard.setPosition(this.posX, this.posY, this.posZ);
                shard.motionX = dx * SHARD_SPEED;
                shard.motionY = 0.1D + this.rand.nextFloat() * 0.1F;
                shard.motionZ = dz * SHARD_SPEED;
                this.worldObj.spawnEntityInWorld(shard);
            }

            this.setDead();
        }
    }
}