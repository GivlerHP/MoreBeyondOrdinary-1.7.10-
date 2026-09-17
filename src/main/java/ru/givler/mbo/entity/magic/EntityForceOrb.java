package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Exploding force sphere which damages and throws nearby creatures away. */
public final class EntityForceOrb extends EntityThrowable implements IEntityAdditionalSpawnData {
  private float damageMultiplier = 1.0F;
  private float blastMultiplier = 1.0F;

  public EntityForceOrb(World world) {
    super(world);
    setSize(0.6F, 0.6F);
  }

  public EntityForceOrb(World world, EntityLivingBase caster, float damage, float blast) {
    super(world, caster);
    setSize(0.6F, 0.6F);
    damageMultiplier = damage;
    blastMultiplier = blast;
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (worldObj.isRemote) {
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.VANILLA_LARGE_EXPLODE, worldObj, posX, posY, posZ, 0, 0, 0);
      for (int i = 0; i < 20; i++) {
        float brightness = 0.5F + rand.nextFloat() * 0.5F;
        double x = posX - 0.25D + rand.nextDouble() * 0.5D;
        double y = posY - 0.25D + rand.nextDouble() * 0.5D;
        double z = posZ - 0.25D + rand.nextDouble() * 0.5D;
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            worldObj,
            x,
            y,
            z,
            (x - posX) * 2,
            (y - posY) * 2,
            (z - posZ) * 2,
            new ParticleSettings(6, brightness, 1.0F, brightness + 0.2F, 0.8F, false));
      }
      return;
    }
    float pitch = rand.nextFloat() * 0.2F + 0.3F;
    playSound("fireworks.blast", 1.5F, pitch);
    playSound("fireworks.blast", 1.5F, pitch - 0.01F);
    double radius = 4.0D * blastMultiplier;
    @SuppressWarnings("unchecked")
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class, boundingBox.expand(radius, radius, radius));
    for (EntityLivingBase target : targets) {
      if (target == getThrower()) continue;
      double dx = target.posX - posX;
      double dz = target.posZ - posZ;
      double distance = Math.max(0.25D, Math.sqrt(dx * dx + dz * dz));
      target.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(this, getThrower()), 4.0F * damageMultiplier);
      target.motionX = dx / distance * (0.5D + distance / 8.0D);
      target.motionY += 0.4D;
      target.motionZ = dz / distance * (0.5D + distance / 8.0D);
      target.velocityChanged = true;
      if (target instanceof EntityPlayerMP)
        ((EntityPlayerMP) target)
            .playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
    }
    setDead();
  }

  @Override
  protected float getGravityVelocity() {
    return 0.03F;
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }

  @Override
  public void writeSpawnData(ByteBuf buffer) {
    buffer.writeFloat(damageMultiplier);
    buffer.writeFloat(blastMultiplier);
  }

  @Override
  public void readSpawnData(ByteBuf buffer) {
    damageMultiplier = buffer.readFloat();
    blastMultiplier = buffer.readFloat();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setFloat("DamageMultiplier", damageMultiplier);
    tag.setFloat("BlastMultiplier", blastMultiplier);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    damageMultiplier = tag.getFloat("DamageMultiplier");
    blastMultiplier = tag.getFloat("BlastMultiplier");
  }
}
