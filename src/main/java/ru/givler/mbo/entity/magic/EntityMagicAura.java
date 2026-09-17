package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.*;

public final class EntityMagicAura extends Entity implements IEntityAdditionalSpawnData {
  public enum Kind {
    FORCEFIELD,
    HEALING
  }

  private int ownerId = -1, lifetime = 600;
  private float power = 1;

  public EntityMagicAura(World w) {
    super(w);
    noClip = true;
    setSize(5, 1);
  }

  public EntityMagicAura(
      World w, double x, double y, double z, EntityLivingBase o, Kind k, int life, float p) {
    this(w);
    setPosition(x, y, z);
    ownerId = o.getEntityId();
    lifetime = life;
    power = p;
    dataWatcher.updateObject(10, (byte) k.ordinal());
    if (k == Kind.FORCEFIELD) setSize(6, 6);
  }

  public Kind kind() {
    return Kind.values()[dataWatcher.getWatchableObjectByte(10)];
  }

  private EntityLivingBase owner() {
    Entity e = worldObj.getEntityByID(ownerId);
    return e instanceof EntityLivingBase ? (EntityLivingBase) e : null;
  }

  @Override
  protected void entityInit() {
    dataWatcher.addObject(10, (byte) 0);
  }

  @Override
  public boolean canBeCollidedWith() {
    return kind() == Kind.FORCEFIELD && !isDead;
  }

  @Override
  public AxisAlignedBB getCollisionBox(Entity e) {
    return e.boundingBox;
  }

  @Override
  public boolean attackEntityFrom(DamageSource s, float a) {
    if (s != null && s.getSourceOfDamage() instanceof IProjectile)
      playSound("mbo:effect", .3F, 1.3F);
    return false;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted >= lifetime) {
      setDead();
      return;
    }
    if (worldObj.isRemote) {
      particles();
      return;
    }
    if (kind() == Kind.FORCEFIELD) force();
    else heal();
  }

  @SuppressWarnings("unchecked")
  private void force() {
    EntityLivingBase o = owner();
    List<EntityLivingBase> list =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox);
    for (EntityLivingBase t : list)
      if (t != o && (o == null || !o.isOnSameTeam(t)) && t.getDistance(posX, posY, posZ) < 3.5) {
        double m = (3.5 - t.getDistance(posX, posY, posZ)) * .1;
        t.addVelocity((t.posX - posX) * m, (t.posY - posY) * m, (t.posZ - posZ) * m);
        if (t instanceof EntityPlayerMP)
          ((EntityPlayerMP) t).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(t));
      }
  }

  @SuppressWarnings("unchecked")
  private void heal() {
    EntityLivingBase o = owner();
    List<EntityLivingBase> list =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(0, 2, 0));
    for (EntityLivingBase t : list) {
      boolean ally = t == o || (o != null && o.isOnSameTeam(t));
      if (ally && ticksExisted % 5 == 0 && t.getHealth() < t.getMaxHealth()) t.heal(power);
      else if (!ally && t.isEntityUndead()) {
        double x = t.motionX, y = t.motionY, z = t.motionZ;
        t.attackEntityFrom(
            o == null ? DamageSource.magic : DamageSource.causeIndirectMagicDamage(this, o), power);
        t.motionX = x;
        t.motionY = y;
        t.motionZ = z;
      }
    }
    if (ticksExisted % 25 == 1) playSound("mbo:sparkle", .1F, 1);
  }

  private void particles() {
    if (kind() == Kind.FORCEFIELD)
      for (int i = 0; i < 39; i++) {
        double yaw = rand.nextDouble() * Math.PI * 2,
            pitch = (rand.nextDouble() - .5) * Math.PI,
            r = 3;
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            worldObj,
            posX + r * Math.cos(yaw) * Math.cos(pitch),
            posY + r * Math.sin(pitch),
            posZ + r * Math.sin(yaw) * Math.cos(pitch),
            0,
            0,
            0,
            new ParticleSettings(50, .7F, .7F, 1, .5F, false));
      }
    else
      for (int i = 0; i < 2; i++) {
        double r = rand.nextDouble() * 2, a = rand.nextDouble() * Math.PI * 2;
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            worldObj,
            posX + r * Math.cos(a),
            posY,
            posZ + r * Math.sin(a),
            0,
            .05,
            0,
            new ParticleSettings(50, 1, 1, .7F, .6F, false));
      }
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound t) {
    t.setByte("Kind", (byte) kind().ordinal());
    t.setInteger("Owner", ownerId);
    t.setInteger("Life", lifetime);
    t.setFloat("Power", power);
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound t) {
    dataWatcher.updateObject(10, t.getByte("Kind"));
    ownerId = t.getInteger("Owner");
    lifetime = t.getInteger("Life");
    power = t.getFloat("Power");
  }

  @Override
  public void writeSpawnData(ByteBuf b) {
    b.writeInt(ownerId);
    b.writeInt(lifetime);
    b.writeFloat(power);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    ownerId = b.readInt();
    lifetime = b.readInt();
    power = b.readFloat();
  }
}
