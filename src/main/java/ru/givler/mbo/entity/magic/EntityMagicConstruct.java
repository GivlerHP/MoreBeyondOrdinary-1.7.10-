package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;

/** Shared timed entity for earthquake, lightning hammer and tornado. */
public final class EntityMagicConstruct extends Entity implements IEntityAdditionalSpawnData {
  public enum Kind {
    EARTHQUAKE,
    HAMMER,
    TORNADO
  }

  private Kind kind = Kind.EARTHQUAKE;
  private int ownerId = -1, lifetime = 20;
  private float power = 1, area = 1;
  private double velocityX, velocityZ;

  public EntityMagicConstruct(World world) {
    super(world);
    setSize(1, 1);
  }

  public EntityMagicConstruct(
      World world,
      double x,
      double y,
      double z,
      EntityLivingBase owner,
      Kind kind,
      float power,
      float duration,
      float area) {
    this(world);
    setPosition(x, y, z);
    this.ownerId = owner.getEntityId();
    this.kind = kind;
    this.power = power;
    this.area = area;
    this.lifetime =
        Math.max(
            1,
            (int) ((kind == Kind.EARTHQUAKE ? 20 : kind == Kind.TORNADO ? 200 : 600) * duration));
    if (kind == Kind.TORNADO) {
      velocityX = owner.getLookVec().xCoord / 3;
      velocityZ = owner.getLookVec().zCoord / 3;
      setSize(5, 8);
    }
    if (kind == Kind.HAMMER) {
      setSize(1.1F, 1.9F);
      ignoreFrustumCheck = true;
    }
  }

  @Override
  protected void entityInit() {}

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted > lifetime) {
      setDead();
      return;
    }
    if (kind == Kind.EARTHQUAKE) earthquake();
    else if (kind == Kind.HAMMER) hammer();
    else tornado();
  }

  private EntityLivingBase owner() {
    Entity e = worldObj.getEntityByID(ownerId);
    return e instanceof EntityLivingBase ? (EntityLivingBase) e : null;
  }

  @SuppressWarnings("unchecked")
  private List<EntityLivingBase> living(double radius) {
    return worldObj.getEntitiesWithinAABB(
        EntityLivingBase.class,
        AxisAlignedBB.getBoundingBox(
            posX - radius,
            posY - radius,
            posZ - radius,
            posX + radius,
            posY + radius,
            posZ + radius));
  }

  private boolean valid(EntityLivingBase e) {
    EntityLivingBase o = owner();
    return e != o && e.isEntityAlive() && (o == null || !e.isOnSameTeam(o));
  }

  private DamageSource damage(String name) {
    EntityLivingBase o = owner();
    return o == null
        ? DamageSource.magic
        : new net.minecraft.util.EntityDamageSource(name, o).setMagicDamage();
  }

  private void earthquake() {
    double radius = ticksExisted * .4 + 1.5;
    if (worldObj.isRemote) return;
    for (EntityLivingBase e : living(radius + 1))
      if (valid(e) && e.getDistance(posX, posY, posZ) > radius - 1 && Math.abs(e.posY - posY) < 1) {
        double oldX = e.motionX, oldZ = e.motionZ;
        e.attackEntityFrom(damage("mbo.earthquake"), 10 * power);
        e.addPotionEffect(new PotionEffect(Potion.weakness.id, 400, 1));
        e.motionX = oldX;
        e.motionY = .8;
        e.motionZ = oldZ;
        if (e instanceof EntityPlayerMP)
          ((EntityPlayerMP) e).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(e));
      }
    double step = Math.PI / (lifetime * 1.5D);
    for (double a = 0; a < Math.PI * 2; a += step) {
      int x = MathHelper.floor_double(posX + Math.sin(a) * radius),
          y = MathHelper.floor_double(posY - .5),
          z = MathHelper.floor_double(posZ + Math.cos(a) * radius);
      Block b = worldObj.getBlock(x, y, z);
      if (b != net.minecraft.init.Blocks.air
          && b.getBlockHardness(worldObj, x, y, z) >= 0
          && worldObj.isBlockNormalCubeDefault(x, y, z, false)
          && !worldObj.isBlockNormalCubeDefault(x, y + 1, z, false)) {
        EntityFallingBlock f =
            new EntityFallingBlock(
                worldObj, x + .5, y + .5, z + .5, b, worldObj.getBlockMetadata(x, y, z));
        f.motionY = .3;
        worldObj.spawnEntityInWorld(f);
      }
    }
  }

  public Kind getKind() {
    return kind;
  }

  public boolean isInRangeToRenderVec3D(net.minecraft.util.Vec3 vec) {
    return true;
  }

  private void hammer() {
    if (!onGround) {
      if (ticksExisted % 20 == 1 && worldObj.isRemote)
        MoreBeyondOrdinary.proxy.playMovingSound(this, "mbo:electricityb", 3.0F, 1.0F, false);
      motionY -= .04;
      moveEntity(0, motionY, 0);
      motionY *= .98;
    } else {
      if (worldObj.isRemote && ticksExisted % 80 == 1)
        MoreBeyondOrdinary.proxy.playMovingSound(this, "mbo:electricityb", 1.2F, 1.0F, false);
      if (!worldObj.isRemote && ticksExisted % 40 == 0) {
        if (ticksExisted < 45)
          worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, posX, posY, posZ));
        for (EntityLivingBase e : living(10))
          if (valid(e)) {
            worldObj.spawnEntityInWorld(
                new EntityLightningArc(
                    worldObj, posX, posY + 1, posZ, e.posX, e.posY + e.height * .5, e.posZ, 4));
            worldObj.playSoundAtEntity(e, "mbo:arc", 1, 1.5F + rand.nextFloat() * .4F);
            e.attackEntityFrom(damage("mbo.lightning_hammer"), 6 * power);
          }
      }
    }
    if (worldObj.isRemote && ticksExisted % 3 == 0)
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARK,
          worldObj,
          posX + rand.nextDouble() - .5,
          posY + rand.nextDouble() * 2,
          posZ + rand.nextDouble() - .5,
          0,
          0,
          0);
  }

  private void tornado() {
    moveEntity(velocityX, 0, velocityZ);
    if (worldObj.isRemote) {
      if (ticksExisted % 120 == 1)
        MoreBeyondOrdinary.proxy.playMovingSound(this, "mbo:wind", 1, 1, false);
      for (int i = 0; i < 12; i++) {
        double y = rand.nextDouble() * 8, r = y / 3 + .5;
        int bx = MathHelper.floor_double(posX),
            by = MathHelper.floor_double(posY) - 1,
            bz = MathHelper.floor_double(posZ);
        Block block = worldObj.getBlock(bx, by, bz);
        if (block == Blocks.air) block = Blocks.dirt;
        MoreBeyondOrdinary.proxy.spawnTornadoParticle(
            worldObj,
            posX,
            posY + y,
            posZ,
            velocityX,
            velocityZ,
            r,
            block,
            worldObj.getBlockMetadata(bx, by, bz));
      }
      return;
    }
    for (EntityLivingBase e : living(4))
      if (valid(e)) {
        e.attackEntityFrom(damage("mbo.tornado"), power);
        double dx = posX - e.posX, dz = posZ - e.posZ;
        e.motionX = (dx > 0 ? .5 : -.5) - dx / 8;
        e.motionY += .2;
        e.motionZ = (dz > 0 ? .5 : -.5) - dz / 8;
        if (e instanceof EntityPlayerMP)
          ((EntityPlayerMP) e).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(e));
      }
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound n) {
    kind = Kind.values()[Math.max(0, Math.min(2, n.getByte("Kind")))];
    ownerId = n.getInteger("Owner");
    lifetime = n.getInteger("Life");
    power = n.getFloat("Power");
    area = n.getFloat("Area");
    velocityX = n.getDouble("VX");
    velocityZ = n.getDouble("VZ");
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound n) {
    n.setByte("Kind", (byte) kind.ordinal());
    n.setInteger("Owner", ownerId);
    n.setInteger("Life", lifetime);
    n.setFloat("Power", power);
    n.setFloat("Area", area);
    n.setDouble("VX", velocityX);
    n.setDouble("VZ", velocityZ);
  }

  @Override
  public void writeSpawnData(ByteBuf b) {
    b.writeByte(kind.ordinal());
    b.writeInt(ownerId);
    b.writeInt(lifetime);
    b.writeFloat(power);
    b.writeFloat(area);
    b.writeDouble(velocityX);
    b.writeDouble(velocityZ);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    kind = Kind.values()[b.readByte()];
    ownerId = b.readInt();
    lifetime = b.readInt();
    power = b.readFloat();
    area = b.readFloat();
    velocityX = b.readDouble();
    velocityZ = b.readDouble();
  }
}
