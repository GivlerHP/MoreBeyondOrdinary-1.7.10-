package ru.givler.mbo.integration.thaumcraft.entities;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;

public class EntityLightMatter extends EntityEldritchOrb {

    private float dmg = 0.0F;
    private int enlarge;
    private boolean corrosive;

    public EntityLightMatter(final World world) {
        super(world);
    }

    public EntityLightMatter(final World world, final EntityLivingBase entity, final float dmg, final int enlarge,
                             final boolean corrosive) {
        super(world, entity);
        this.dmg = dmg;
        this.enlarge = enlarge;
        this.corrosive = corrosive;
    }

    @Override
    public boolean shouldRenderInPass(final int pass) {
        return pass == 1;
    }

    @Override
    public void handleHealthUpdate(byte b) {
        if (b == 16) {
            if (this.worldObj.isRemote) {
                for (int a = 0; a < 30; ++a) {
                    float fx = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                    float fy = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                    float fz = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
                    Thaumcraft.proxy.wispFX3(this.worldObj,
                            this.posX + (double) fx, this.posY + (double) fy, this.posZ + (double) fz,
                            this.posX + (double) (fx * 8.0F), this.posY + (double) (fy * 8.0F), this.posZ + (double) (fz * 8.0F),
                            0.3F, 6, true, 0.02F); // type=6 вместо 5 — белый вместо тёмного
                }
            }
        } else {
            super.handleHealthUpdate(b);
        }
    }

    @Override
    protected void onImpact(final MovingObjectPosition mop) {
        if (!worldObj.isRemote && getThrower() != null) {
            final double expand = 1.5D + enlarge * 0.5D;
            final List<Entity> entities =
                    worldObj.getEntitiesWithinAABBExcludingEntity(getThrower(), boundingBox.expand(expand, expand, expand));

            for (final Entity e : entities) {
                if (e instanceof EntityLivingBase) {
                    final EntityLivingBase entity = (EntityLivingBase) e;
                    float actualDmg = dmg;
                    if (entity.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
                        actualDmg *= 2.0F;
                    }
                    entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, getThrower()), actualDmg);
                }
            }
            worldObj.playSoundAtEntity(this, "random.fizz", 0.5F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
            ticksExisted = 100;
            worldObj.setEntityState(this, (byte) 16);
        }
        setDead();
    }
}