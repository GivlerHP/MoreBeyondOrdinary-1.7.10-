package ru.givler.mbo.integration.thaumcraft.entities;


import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.common.lib.utils.ProtectionUtils;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.monster.EntityPech;
import thaumcraft.common.entities.projectile.EntityPechBlast;

public class EntityPechShard extends EntityPechBlast {

    private static final float SHARD_DAMAGE = 2.0F;

    public EntityPechShard(World world) {
        super(world);
    }

    public EntityPechShard(World world, EntityLivingBase thrower, int strength, int duration, boolean nightshade) {
        super(world, thrower, strength, duration, nightshade);
    }

    public EntityPechShard(World world, double x, double y, double z, int strength, int duration, boolean nightshade) {
        super(world, x, y, z, strength, duration, nightshade);
    }

    @Override
    protected void onImpact(MovingObjectPosition mop) {
        // визуальные эффекты оставляем такими же, как у оригинального EntityPechBlast
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
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.getThrower(), this.boundingBox.expand(2.0F, 2.0F, 2.0F));

            for (Object o : list) {
                Entity entity1 = (Entity) o;
                if (!(entity1 instanceof EntityPech) && entity1 instanceof EntityLivingBase
                        && ProtectionUtils.canEntityDamage(this.getThrower(), entity1)) {
                    // только урон, без ядовитых/слоу/слабость эффектов
                    entity1.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), SHARD_DAMAGE);
                }
            }

            this.setDead();
        }
    }
}
