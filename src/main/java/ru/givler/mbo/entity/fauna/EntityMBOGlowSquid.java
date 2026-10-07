package ru.givler.mbo.entity.fauna;

import net.minecraft.block.material.Material;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.registry.ItemRegistry;

public final class EntityMBOGlowSquid extends EntitySquid {
  public EntityMBOGlowSquid(World world) {
    super(world);
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Integer.valueOf(0));
    dataWatcher.addObject(21, Integer.valueOf(0));
  }

  public int darkTicks() {
    return dataWatcher.getWatchableObjectInt(20);
  }

  public boolean baby() {
    return dataWatcher.getWatchableObjectInt(21) < 0;
  }

  @Override
  public void moveEntityWithHeading(float sideways, float forward) {
    if (!worldObj.isRemote) super.moveEntityWithHeading(sideways, forward);
  }

  @Override
  public void onLivingUpdate() {
    float oldYaw = rotationYaw;
    super.onLivingUpdate();
    if (isInWater()) {
      rotationYaw = oldYaw;
      AquaticMovement.face(this, motionX, motionZ, 10F);
    }
    setSize(baby() ? .4F : .8F, baby() ? .4F : .8F);
    if (worldObj.isRemote) {
      if (darkTicks() == 0 && ticksExisted % 4 == 0)
        worldObj.spawnParticle(
            "reddust",
            posX + (rand.nextDouble() - .5) * width,
            posY + rand.nextDouble() * height,
            posZ + (rand.nextDouble() - .5) * width,
            .1,
            .8,
            .75);
    } else {
      if (darkTicks() > 0) dataWatcher.updateObject(20, Integer.valueOf(darkTicks() - 1));
      int age = dataWatcher.getWatchableObjectInt(21);
      if (age < 0) dataWatcher.updateObject(21, Integer.valueOf(age + 1));
    }
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float amount) {
    boolean hurt = super.attackEntityFrom(source, amount);
    if (hurt && !worldObj.isRemote) {
      dataWatcher.updateObject(20, Integer.valueOf(100));
      playSound("mbo:entity.glow_squid.squirt", 1, 1);
      worldObj.setEntityState(this, (byte) 19);
    }
    return hurt;
  }

  @Override
  public void handleHealthUpdate(byte id) {
    if (id == 19) {
      for (int i = 0; i < 20; i++)
        worldObj.spawnParticle(
            "reddust",
            posX + (rand.nextDouble() - .5),
            posY + rand.nextDouble(),
            posZ + (rand.nextDouble() - .5),
            .1,
            .8,
            .75);
    } else super.handleHealthUpdate(id);
  }

  @Override
  public int getBrightnessForRender(float partial) {
    return darkTicks() == 0 ? 15728880 : super.getBrightnessForRender(partial);
  }

  @Override
  public float getBrightness(float partial) {
    return darkTicks() == 0 ? 1 : super.getBrightness(partial);
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(posY),
        z = MathHelper.floor_double(posZ);
    return FaunaConfig.allowsPosition("glow_squid", worldObj, x, y, z)
        && y <= 30
        && worldObj.getFullBlockLightValue(x, y, z) == 0
        && worldObj.getBlock(x, y, z).getMaterial() == Material.water
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    super.onSpawnWithEgg(data);
    if (rand.nextFloat() < .05) dataWatcher.updateObject(21, Integer.valueOf(-24000));
    return data;
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.glow_squid.ambient";
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.glow_squid.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.glow_squid.death";
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {
    if (!baby())
      entityDropItem(
          new ItemStack(
              ItemRegistry.prismarineCrystal, 1 + rand.nextInt(3) + rand.nextInt(looting + 1)),
          0);
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setInteger("DarkTicksRemaining", darkTicks());
    tag.setInteger("Age", dataWatcher.getWatchableObjectInt(21));
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    dataWatcher.updateObject(
        20, Integer.valueOf(MathHelper.clamp_int(tag.getInteger("DarkTicksRemaining"), 0, 100)));
    dataWatcher.updateObject(
        21, Integer.valueOf(MathHelper.clamp_int(tag.getInteger("Age"), -24000, 0)));
  }
}
