package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.magic.PacketMagicBurst;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** One persistent floor trap with fire, frost and lightning variants. */
public final class EntityMagicSigil extends Entity implements IEntityAdditionalSpawnData {
  public enum Kind {
    FIRE,
    FROST,
    LIGHTNING
  }

  private Kind kind = Kind.FIRE;
  private WeakReference<EntityLivingBase> caster;
  private UUID casterUuid;
  private int casterEntityId = -1;
  private float damageMultiplier = 1;

  public EntityMagicSigil(World world) {
    super(world);
    setSize(2, 0.2F);
    noClip = true;
  }

  public EntityMagicSigil(
      World world, double x, double y, double z, EntityLivingBase caster, Kind kind, float damage) {
    this(world);
    setPosition(x, y, z);
    this.caster = new WeakReference<EntityLivingBase>(caster);
    casterUuid = caster.getUniqueID();
    casterEntityId = caster.getEntityId();
    this.kind = kind;
    damageMultiplier = damage;
  }

  public Kind kind() {
    return kind;
  }

  public EntityLivingBase caster() {
    EntityLivingBase value = caster == null ? null : caster.get();
    if (value == null
        && casterEntityId >= 0
        && worldObj.getEntityByID(casterEntityId) instanceof EntityLivingBase) {
      value = (EntityLivingBase) worldObj.getEntityByID(casterEntityId);
      caster = new WeakReference<EntityLivingBase>(value);
    }
    if (value == null && casterUuid != null)
      for (Object object : worldObj.loadedEntityList)
        if (object instanceof EntityLivingBase
            && casterUuid.equals(((Entity) object).getUniqueID())) {
          value = (EntityLivingBase) object;
          caster = new WeakReference<EntityLivingBase>(value);
          break;
        }
    return value;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (worldObj.isRemote) {
      ambientParticle();
      return;
    }
    @SuppressWarnings("unchecked")
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class, boundingBox.expand(0.1D, 0.8D, 0.1D));
    for (EntityLivingBase target : targets)
      if (valid(target)) {
        trigger(target);
        setDead();
        break;
      }
  }

  private boolean valid(EntityLivingBase target) {
    EntityLivingBase owner = caster();
    return target.isEntityAlive()
        && target != owner
        && (owner == null || !owner.isOnSameTeam(target));
  }

  private void trigger(EntityLivingBase target) {
    double vx = target.motionX, vy = target.motionY, vz = target.motionZ;
    if (kind == Kind.FIRE) {
      target.attackEntityFrom(source().setFireDamage(), 6 * damageMultiplier);
      target.setFire(10);
      worldObj.playAuxSFX(1009, (int) posX, (int) posY, (int) posZ, 0);
    } else if (kind == Kind.FROST) {
      target.attackEntityFrom(source(), 8 * damageMultiplier);
      SyncedPotionEffects.apply(target, new PotionEffect(PotionRegistry.Frost.id, 200, 1, true));
      playSound("mbo:freeze", 1, 1);
    } else {
      if (target.attackEntityFrom(source(), 6 * damageMultiplier)) {
        playSound("mbo:arc", 1, 1);
        chain(target);
      }
    }
    target.motionX = vx;
    target.motionY = vy;
    target.motionZ = vz;
  }

  @SuppressWarnings("unchecked")
  private void chain(EntityLivingBase origin) {
    List<EntityLivingBase> list =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, origin.boundingBox.expand(5, 5, 5));
    int count = 0;
    for (EntityLivingBase target : list) {
      if (target == origin || !valid(target)) continue;
      target.attackEntityFrom(source(), 4 * damageMultiplier);
      playSound("mbo:arc", 1, 1.5F + rand.nextFloat() * 0.4F);
      PacketManager.INSTANCE.sendToAllAround(
          new PacketMagicBurst(target, 8, 0.4F, 0.7F, 1),
          new NetworkRegistry.TargetPoint(dimension, target.posX, target.posY, target.posZ, 64));
      if (++count >= 3) break;
    }
  }

  private DamageSource source() {
    EntityLivingBase owner = caster();
    return owner == null ? DamageSource.magic : DamageSource.causeIndirectMagicDamage(this, owner);
  }

  private void ambientParticle() {
    if (rand.nextInt(15) != 0) return;
    double radius = .5D + rand.nextDouble() * .3D,
        angle = rand.nextDouble() * Math.PI * 2,
        x = posX + radius * Math.cos(angle),
        z = posZ + radius * Math.sin(angle);
    if (kind == Kind.FIRE) worldObj.spawnParticle("flame", x, posY + .1, z, 0, 0, 0);
    else
      MoreBeyondOrdinary.proxy.spawnParticle(
          kind == Kind.FROST ? EnumParticleType.SNOW : EnumParticleType.SPARK,
          worldObj,
          x,
          posY + .1,
          z,
          0,
          0,
          0,
          new ParticleSettings(
              kind == Kind.FROST ? 45 : 3, 1, 1, 1, kind == Kind.FROST ? .6F : 1.2F, false));
  }

  @Override
  protected void entityInit() {}

  @Override
  protected void writeEntityToNBT(NBTTagCompound t) {
    if (caster() != null) t.setString("Caster", caster().getUniqueID().toString());
    t.setByte("Kind", (byte) kind.ordinal());
    t.setFloat("DamageMultiplier", damageMultiplier);
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound t) {
    try {
      casterUuid = UUID.fromString(t.getString("Caster"));
    } catch (IllegalArgumentException ignored) {
    }
    int id = t.getByte("Kind") & 255;
    kind = id < Kind.values().length ? Kind.values()[id] : Kind.FIRE;
    damageMultiplier = t.getFloat("DamageMultiplier");
  }

  @Override
  public void writeSpawnData(ByteBuf b) {
    b.writeByte(kind.ordinal());
    b.writeInt(casterEntityId);
    b.writeFloat(damageMultiplier);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    int id = b.readUnsignedByte();
    kind = id < Kind.values().length ? Kind.values()[id] : Kind.FIRE;
    casterEntityId = b.readInt();
    damageMultiplier = b.readFloat();
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }

  @Override
  public boolean isPushedByWater() {
    return false;
  }
}
