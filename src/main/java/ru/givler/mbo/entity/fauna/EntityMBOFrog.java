package ru.givler.mbo.entity.fauna;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.registry.BlockRegistry;

public final class EntityMBOFrog extends EntityAnimal {
  private int jumpTicks, croakTicks, tongueTicks, huntCooldown;
  private EntitySlime prey;
  private int previousFlags;
  private final int[] animationStarts = {-1, -1, -1};

  public EntityMBOFrog(World world) {
    super(world);
    setSize(.5F, .5F);
    stepHeight = 1F;
    getNavigator().setCanSwim(true);
    getNavigator().setAvoidsWater(false);
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(1, new EntityAIPanic(this, 1D));
    tasks.addTask(2, new Breed());
    tasks.addTask(3, new EntityAITempt(this, .5D, Items.slime_ball, false));
    tasks.addTask(5, new EntityAIWander(this, .3D));
    tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
    tasks.addTask(7, new EntityAILookIdle(this));
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Byte.valueOf((byte) 0));
    dataWatcher.addObject(21, Byte.valueOf((byte) 0));
    dataWatcher.addObject(22, Byte.valueOf((byte) 0));
  }

  @Override
  protected boolean isAIEnabled() {
    return true;
  }

  @Override
  public boolean canBreatheUnderwater() {
    return true;
  }

  @Override
  public void setGrowingAge(int age) {
    super.setGrowingAge(Math.max(0, age));
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1D);
  }

  public static int variantFor(World world, double x, double z) {
    float temperature =
        world.getBiomeGenForCoords(MathHelper.floor_double(x), MathHelper.floor_double(z))
            .temperature;
    return temperature < .2F ? 2 : temperature >= 1F ? 1 : 0;
  }

  public int variant() {
    return dataWatcher.getWatchableObjectByte(20);
  }

  public void setVariant(int value) {
    dataWatcher.updateObject(20, Byte.valueOf((byte) MathHelper.clamp_int(value, 0, 2)));
  }

  public boolean pregnant() {
    return dataWatcher.getWatchableObjectByte(22) != 0;
  }

  private void setPregnant(boolean value) {
    dataWatcher.updateObject(22, Byte.valueOf((byte) (value ? 1 : 0)));
  }

  public boolean activity(int index) {
    return (dataWatcher.getWatchableObjectByte(21) & (1 << index)) != 0;
  }

  public float animationTime(int index, float partial) {
    return animationStarts[index] < 0 ? 0 : (ticksExisted - animationStarts[index] + partial) / 20F;
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return !pregnant() && stack != null && stack.getItem() == Items.slime_ball;
  }

  @Override
  public EntityAgeable createChild(EntityAgeable mate) {
    return new EntityMBOFrog(worldObj);
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    setVariant(variantFor(worldObj, posX, posZ));
    return super.onSpawnWithEgg(data);
  }

  @Override
  protected void jump() {
    super.jump();
    motionY = .5D;
    jumpTicks = 12;
  }

  @Override
  protected void fall(float distance) {
    super.fall(Math.max(0F, distance - 10F));
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (!worldObj.isRemote && isEntityAlive()) {
      if (jumpTicks > 0) jumpTicks--;
      if (croakTicks > 0) croakTicks--;
      if (huntCooldown > 0) huntCooldown--;
      if (tongueTicks > 0) {
        tongueTicks--;
        if (prey != null && prey.isEntityAlive()) {
          getLookHelper().setLookPositionWithEntity(prey, 90F, 90F);
          if (tongueTicks == 4
              && getDistanceSqToEntity(prey) < 9D
              && getEntitySenses().canSee(prey)) eat(prey);
        }
        if (tongueTicks == 0) {
          prey = null;
          huntCooldown = 40;
        }
      } else if (huntCooldown == 0 && ticksExisted % 20 == 0 && !isInLove() && !pregnant()) hunt();
      if (pregnant() && ticksExisted % 40 == 0) laySpawn();
      if (onGround
          && !isInWater()
          && tongueTicks == 0
          && croakTicks == 0
          && ticksExisted % 30 == 0
          && !getNavigator().noPath()) {
        jump();
        playSound("mbo:entity.frog.long_jump", 1F, 1F);
      }
      if (onGround && getNavigator().noPath() && tongueTicks == 0 && rand.nextInt(1200) == 0) {
        croakTicks = 60;
        playSound("mbo:entity.frog.ambient", 1F, 1F);
      }
      dataWatcher.updateObject(
          21,
          Byte.valueOf(
              (byte)
                  ((jumpTicks > 0 ? 1 : 0)
                      | (croakTicks > 0 ? 2 : 0)
                      | (tongueTicks > 0 ? 4 : 0))));
    }
    int flags = dataWatcher.getWatchableObjectByte(21);
    for (int i = 0; i < 3; i++)
      if ((flags & (1 << i)) != 0 && (previousFlags & (1 << i)) == 0)
        animationStarts[i] = ticksExisted;
    previousFlags = flags;
  }

  private void hunt() {
    List<EntitySlime> slimes =
        worldObj.getEntitiesWithinAABB(EntitySlime.class, boundingBox.expand(8, 3, 8));
    EntitySlime closest = null;
    double nearest = 64;
    for (EntitySlime slime : slimes)
      if (slime.getSlimeSize() == 1 && slime.isEntityAlive() && getEntitySenses().canSee(slime)) {
        double distance = getDistanceSqToEntity(slime);
        if (distance < nearest) {
          closest = slime;
          nearest = distance;
        }
      }
    if (closest == null) return;
    if (nearest > 1.75D * 1.75D) getNavigator().tryMoveToEntityLiving(closest, .5D);
    else {
      prey = closest;
      tongueTicks = 10;
      getNavigator().clearPathEntity();
      double dx = posX - prey.posX, dy = posY - prey.posY, dz = posZ - prey.posZ;
      double distance = Math.max(.01D, Math.sqrt(dx * dx + dy * dy + dz * dz));
      prey.motionX = dx / distance * .75D;
      prey.motionY = dy / distance * .75D;
      prey.motionZ = dz / distance * .75D;
      rotationYaw = (float) (Math.atan2(-dz, -dx) * 180D / Math.PI) - 90F;
      renderYawOffset = rotationYaw;
      playSound("mbo:entity.frog.tongue", 2F, 1F);
    }
  }

  private void eat(EntitySlime slime) {
    // Normal slime drops remain vanilla; a tiny magma cube has no cream drop.
    if (slime.attackEntityFrom(DamageSource.causeMobDamage(this), 10F) && slime.getHealth() <= 0) {
      if (slime instanceof EntityMagmaCube
          && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
        entityDropItem(new ItemStack(BlockRegistry.froglight, 1, variant()), 0F);
      slime.setDead();
    }
    playSound("mbo:entity.frog.eat", 1F, 1F);
  }

  private void laySpawn() {
    int cx = MathHelper.floor_double(posX),
        cy = MathHelper.floor_double(boundingBox.minY),
        cz = MathHelper.floor_double(posZ);
    for (int i = 0; i < 40; i++) {
      int x = cx + worldObj.rand.nextInt(9) - 4,
          y = cy + worldObj.rand.nextInt(3) - 1,
          z = cz + worldObj.rand.nextInt(9) - 4;
      if (!worldObj.blockExists(x, y, z)
          || worldObj.getBlock(x, y, z).getMaterial() != Material.water
          || worldObj.getBlockMetadata(x, y, z) != 0
          || !worldObj.isAirBlock(x, y + 1, z)) continue;
      if (getDistanceSq(x + .5, y + 1, z + .5) > 4D) {
        getNavigator().tryMoveToXYZ(x + .5, y + 1, z + .5, .4D);
        continue;
      }
      if (worldObj.setBlock(x, y + 1, z, BlockRegistry.frogspawn, 0, 3)) {
        setPregnant(false);
        playSound("mbo:entity.frog.lay_spawn", 1F, 1F);
        return;
      }
    }
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    return FaunaConfig.allowsPosition("frog", worldObj, x, y, z)
        && worldObj.getBlock(x, y - 1, z).getMaterial().isSolid()
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.frog.ambient";
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.frog.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.frog.death";
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {}

  @Override
  public void writeEntityToNBT(NBTTagCompound data) {
    super.writeEntityToNBT(data);
    data.setInteger("Variant", variant());
    data.setBoolean("Pregnant", pregnant());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound data) {
    super.readEntityFromNBT(data);
    setVariant(data.getInteger("Variant"));
    setPregnant(data.getBoolean("Pregnant"));
  }

  private final class Breed extends EntityAIBase {
    private EntityMBOFrog mate;
    private int timer;

    Breed() {
      setMutexBits(3);
    }

    public boolean shouldExecute() {
      if (!isInLove() || pregnant()) return false;
      List<EntityMBOFrog> frogs =
          worldObj.getEntitiesWithinAABB(EntityMBOFrog.class, boundingBox.expand(8, 4, 8));
      for (EntityMBOFrog frog : frogs)
        if (frog != EntityMBOFrog.this && !frog.pregnant() && canMateWith(frog)) {
          mate = frog;
          return true;
        }
      return false;
    }

    public boolean continueExecuting() {
      return mate != null && mate.isEntityAlive() && mate.isInLove() && isInLove() && timer < 60;
    }

    public void startExecuting() {
      timer = 0;
    }

    public void resetTask() {
      mate = null;
      timer = 0;
    }

    public void updateTask() {
      getLookHelper().setLookPositionWithEntity(mate, 30F, 30F);
      getNavigator().tryMoveToEntityLiving(mate, .4D);
      timer++;
      if (timer >= 60
          && isInLove()
          && mate.isInLove()
          && !mate.pregnant()
          && getDistanceSqToEntity(mate) < 9D) {
        setPregnant(true);
        setGrowingAge(6000);
        mate.setGrowingAge(6000);
        resetInLove();
        mate.resetInLove();
        if (worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
          worldObj.spawnEntityInWorld(
              new EntityXPOrb(worldObj, posX, posY, posZ, rand.nextInt(7) + 1));
      }
    }
  }
}
