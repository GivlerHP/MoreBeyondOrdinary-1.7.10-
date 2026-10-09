package ru.givler.mbo.entity.fauna;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.ItemRegistry;

public final class EntityMBOGoat extends EntityAnimal {
  private int ramCooldown, jumpCooldown, lowerHead, previousLowerHead;
  private final GoatRam ram = new GoatRam();
  private final GoatJump longJump = new GoatJump();

  public EntityMBOGoat(World world) {
    super(world);
    setSize(.9F, 1.3F);
    stepHeight = 1F;
    ramCooldown = 600 + rand.nextInt(5401);
    jumpCooldown = 600 + rand.nextInt(601);
    getNavigator().setCanSwim(true);
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(1, new EntityAIPanic(this, 2D));
    tasks.addTask(2, new EntityAIMate(this, 1D));
    tasks.addTask(3, new EntityAITempt(this, 1.25D, Items.wheat, false));
    tasks.addTask(4, ram);
    tasks.addTask(5, longJump);
    tasks.addTask(6, new EntityAIFollowParent(this, 1.25D));
    tasks.addTask(7, new EntityAIWander(this, 1D));
    tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
    tasks.addTask(9, new EntityAILookIdle(this));
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Byte.valueOf((byte) 6));
    dataWatcher.addObject(21, Byte.valueOf((byte) 0));
  }

  @Override
  protected boolean isAIEnabled() {
    return true;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.2);
  }

  public boolean screaming() {
    return (dataWatcher.getWatchableObjectByte(20) & 1) != 0;
  }

  public boolean leftHorn() {
    return (dataWatcher.getWatchableObjectByte(20) & 2) != 0;
  }

  public boolean rightHorn() {
    return (dataWatcher.getWatchableObjectByte(20) & 4) != 0;
  }

  private void flag(int mask, boolean value) {
    int flags = dataWatcher.getWatchableObjectByte(20);
    dataWatcher.updateObject(20, Byte.valueOf((byte) (value ? flags | mask : flags & ~mask)));
  }

  public float ramHead(float partial) {
    return (previousLowerHead + (lowerHead - previousLowerHead) * partial)
        / 20F
        * (isChild() ? 52.5F : 30F)
        * (float) Math.PI
        / 180F;
  }

  private String sound(String name) {
    return "mbo:entity.goat." + (screaming() ? "screaming." : "") + name;
  }

  @Override
  protected String getLivingSound() {
    return sound("ambient");
  }

  @Override
  protected String getHurtSound() {
    return sound("hurt");
  }

  @Override
  protected String getDeathSound() {
    return sound("death");
  }

  @Override
  protected void func_145780_a(int x, int y, int z, Block block) {
    playSound("mbo:entity.goat.step", .15F, 1F);
  }

  @Override
  protected void fall(float distance) {
    super.fall(Math.max(0, distance - 10));
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return stack != null && stack.getItem() == Items.wheat;
  }

  @Override
  public EntityAgeable createChild(EntityAgeable other) {
    EntityMBOGoat baby = new EntityMBOGoat(worldObj);
    baby.flag(
        1,
        (rand.nextBoolean() ? screaming() : ((EntityMBOGoat) other).screaming())
            || rand.nextDouble() < .02);
    baby.resetRamCooldown();
    return baby;
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack held = player.getCurrentEquippedItem();
    if (held != null && held.getItem() == Items.bucket && !isChild()) {
      if (!worldObj.isRemote) {
        playSound(sound("milk"), 1, 1);
        if (!player.capabilities.isCreativeMode) {
          if (--held.stackSize == 0)
            player.inventory.setInventorySlotContents(
                player.inventory.currentItem, new ItemStack(Items.milk_bucket));
          else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.milk_bucket)))
            player.dropPlayerItemWithRandomChoice(new ItemStack(Items.milk_bucket), false);
        }
      }
      return true;
    }
    boolean result = super.interact(player);
    if (result && isBreedingItem(held)) playSound(sound("eat"), 1, 1);
    return result;
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    previousLowerHead = lowerHead;
    lowerHead =
        MathHelper.clamp_int(
            lowerHead + (dataWatcher.getWatchableObjectByte(21) != 0 ? 1 : -2), 0, 20);
    if (!worldObj.isRemote) {
      if (ramCooldown > 0) ramCooldown--;
      if (jumpCooldown > 0) jumpCooldown--;
      PathEntity path = getNavigator().getPath();
      if (path != null)
        for (int i = path.getCurrentPathIndex();
            i < Math.min(path.getCurrentPathLength(), path.getCurrentPathIndex() + 3);
            i++) {
          PathPoint point = path.getPathPointFromIndex(i);
          if (worldObj.getBlock(point.xCoord, point.yCoord, point.zCoord)
                  == BlockRegistry.powderSnow
              || worldObj.getBlock(point.xCoord, point.yCoord - 1, point.zCoord)
                  == BlockRegistry.powderSnow) {
            getNavigator().clearPathEntity();
            break;
          }
        }
    }
    rotationYawHead =
        renderYawOffset
            + MathHelper.clamp_float(
                MathHelper.wrapAngleTo180_float(rotationYawHead - renderYawOffset), -15, 15);
  }

  private void resetRamCooldown() {
    ramCooldown = screaming() ? 100 + rand.nextInt(201) : 600 + rand.nextInt(5401);
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    super.onSpawnWithEgg(data);
    flag(1, rand.nextDouble() < .02);
    resetRamCooldown();
    if (data != null && rand.nextFloat() < .2F) setGrowingAge(-24000);
    if (!isChild() && rand.nextFloat() < .1F) flag(rand.nextBoolean() ? 2 : 4, false);
    return data == null ? new Herd() : data;
  }

  private static final class Herd implements IEntityLivingData {}

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    Block ground = worldObj.getBlock(x, y - 1, z);
    return MobSpawnConfig.allowsPosition("goat", worldObj, x, y, z)
        && (ground == Blocks.grass
            || ground == Blocks.stone
            || ground == Blocks.snow
            || ground == Blocks.snow_layer
            || ground == Blocks.gravel
            || ground == Blocks.packed_ice)
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
        && !worldObj.isAnyLiquid(boundingBox);
  }

  @Override
  protected void dropFewItems(boolean recentlyHit, int looting) {}

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setBoolean("IsScreamingGoat", screaming());
    tag.setBoolean("HasLeftHorn", leftHorn());
    tag.setBoolean("HasRightHorn", rightHorn());
    tag.setInteger("RamCooldown", ramCooldown);
    tag.setInteger("LongJumpCooldown", jumpCooldown);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    flag(1, tag.getBoolean("IsScreamingGoat"));
    flag(2, !tag.hasKey("HasLeftHorn") || tag.getBoolean("HasLeftHorn"));
    flag(4, !tag.hasKey("HasRightHorn") || tag.getBoolean("HasRightHorn"));
    if (tag.hasKey("RamCooldown"))
      ramCooldown = MathHelper.clamp_int(tag.getInteger("RamCooldown"), 0, 6000);
    if (tag.hasKey("LongJumpCooldown"))
      jumpCooldown = MathHelper.clamp_int(tag.getInteger("LongJumpCooldown"), 0, 1200);
  }

  private boolean safeGround(int x, int y, int z) {
    return worldObj.blockExists(x, y, z)
        && worldObj.getBlock(x, y - 1, z).isNormalCube()
        && worldObj.getBlock(x, y - 1, z) != BlockRegistry.powderSnow
        && worldObj.getBlock(x, y, z) != BlockRegistry.powderSnow
        && worldObj
            .getCollidingBoundingBoxes(
                this,
                AxisAlignedBB.getBoundingBox(
                    x + .5 - width / 2,
                    y,
                    z + .5 - width / 2,
                    x + .5 + width / 2,
                    y + height,
                    z + .5 + width / 2))
            .isEmpty();
  }

  public static double jumpVelocity(double rise, int ticks) {
    double factor = (1 - Math.pow(.98, ticks)) / .02;
    return (rise + 3.92 * (ticks - factor)) / factor;
  }

  private boolean hornBlock(Block block) {
    return block == Blocks.stone
        || block == Blocks.packed_ice
        || block == Blocks.iron_ore
        || block == Blocks.coal_ore
        || block == Blocks.emerald_ore
        || block == Blocks.log
        || block == Blocks.log2;
  }

  private void breakHorn() {
    if (isChild() || !leftHorn() && !rightHorn()) return;
    int mask = !leftHorn() ? 4 : !rightHorn() ? 2 : rand.nextBoolean() ? 2 : 4;
    ItemStack drop =
        new ItemStack(
            ItemRegistry.goatHorn,
            1,
            new Random(getUniqueID().hashCode()).nextInt(4) + (screaming() ? 4 : 0));
    if (worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")
        && entityDropItem(drop, .5F) == null) return;
    flag(mask, false);
    playSound("mbo:entity.goat.horn_break", 1, 1);
  }

  private final class GoatRam extends EntityAIBase {
    private EntityLivingBase target;
    private double targetX, targetZ, startX, startZ, dx, dz;
    private int stage, time;

    GoatRam() {
      setMutexBits(3);
    }

    public boolean shouldExecute() {
      if (ramCooldown > 0 || !onGround || isInWater() || isInLove() || isBurning()) return false;
      List<EntityLivingBase> nearby =
          worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(8, 3, 8));
      target = null;
      for (EntityLivingBase candidate : nearby)
        if (candidate != EntityMBOGoat.this
            && !(candidate instanceof EntityMBOGoat)
            && candidate.isEntityAlive()
            && (!(candidate instanceof EntityPlayer)
                || !((EntityPlayer) candidate).capabilities.isCreativeMode)
            && getEntitySenses().canSee(candidate)) {
          target = candidate;
          break;
        }
      if (target == null) {
        ramCooldown = 100;
        return false;
      }
      targetX = target.posX;
      targetZ = target.posZ;
      int y = MathHelper.floor_double(boundingBox.minY);
      for (int axis = 0; axis < 4; axis++) {
        int sx = axis == 0 ? 1 : axis == 1 ? -1 : 0, sz = axis == 2 ? 1 : axis == 3 ? -1 : 0;
        boolean clear = true;
        for (int d = 1; d <= 4; d++)
          if (!safeGround(
              MathHelper.floor_double(targetX) + sx * d,
              y,
              MathHelper.floor_double(targetZ) + sz * d)) {
            clear = false;
            break;
          }
        if (clear) {
          startX = MathHelper.floor_double(targetX) + sx * 4 + .5;
          startZ = MathHelper.floor_double(targetZ) + sz * 4 + .5;
          return true;
        }
      }
      ramCooldown = 100;
      return false;
    }

    public void startExecuting() {
      stage = 0;
      time = 0;
    }

    public boolean continueExecuting() {
      return target != null && target.isEntityAlive() && time < 160 && stage < 3 && !isBurning();
    }

    public void resetTask() {
      target = null;
      getNavigator().clearPathEntity();
      dataWatcher.updateObject(21, Byte.valueOf((byte) 0));
      motionX = motionZ = 0;
      moveForward = moveStrafing = 0;
      resetRamCooldown();
    }

    public void updateTask() {
      time++;
      getLookHelper().setLookPosition(targetX, posY + .5, targetZ, 30, 30);
      if (stage < 2 && target.getDistanceSq(targetX, target.posY, targetZ) > .25) {
        stage = 3;
        return;
      }
      if (stage == 0) {
        if (time % 10 == 1 && !getNavigator().tryMoveToXYZ(startX, posY, startZ, 1.25D)) {
          stage = 3;
          return;
        }
        if (getDistanceSq(startX, posY, startZ) < .5) {
          stage = 1;
          time = 0;
          getNavigator().clearPathEntity();
          dataWatcher.updateObject(21, Byte.valueOf((byte) 1));
          playSound(sound("prepare_ram"), 1, 1);
        }
      } else if (stage == 1 && time >= 20) {
        stage = 2;
        time = 0;
        double length =
            Math.sqrt((targetX - posX) * (targetX - posX) + (targetZ - posZ) * (targetZ - posZ));
        dx = (targetX - posX) / Math.max(.01, length);
        dz = (targetZ - posZ) / Math.max(.01, length);
      } else if (stage == 2) {
        motionX = dx * .6;
        motionZ = dz * .6;
        rotationYaw = (float) (Math.atan2(-dx, dz) * 180 / Math.PI);
        List<EntityLivingBase> victims =
            worldObj.getEntitiesWithinAABB(
                EntityLivingBase.class,
                boundingBox.addCoord(dx * .6, 0, dz * .6).expand(.1, 0, .1));
        for (EntityLivingBase victim : victims)
          if (victim != EntityMBOGoat.this
              && !(victim instanceof EntityMBOGoat)
              && victim.isEntityAlive()
              && (!(victim instanceof EntityPlayer)
                  || !((EntityPlayer) victim).capabilities.isCreativeMode)) {
            victim.attackEntityFrom(
                DamageSource.causeMobDamage(EntityMBOGoat.this), isChild() ? 1 : 2);
            victim.addVelocity(dx * (isChild() ? 1 : 2.5), .2, dz * (isChild() ? 1 : 2.5));
            victim.velocityChanged = true;
            playSound(sound("ram_impact"), 1, 1);
            stage = 3;
            break;
          }
        int x = MathHelper.floor_double(posX + dx * (width / 2 + .4)),
            y = MathHelper.floor_double(posY + .5),
            z = MathHelper.floor_double(posZ + dz * (width / 2 + .4));
        if (isCollidedHorizontally || worldObj.getBlock(x, y, z).isNormalCube()) {
          if (hornBlock(worldObj.getBlock(x, y, z))) breakHorn();
          stage = 3;
        }
        if (time > 40 || getDistanceSq(startX, posY, startZ) > 100) stage = 3;
      }
    }
  }

  /**
   * Validate the entire discrete 1.7 ballistic path before launching, including ceiling clearance.
   */
  private final class GoatJump extends EntityAIBase {
    private double vx, vy, vz;
    private int time;
    private boolean launched;

    GoatJump() {
      setMutexBits(3);
    }

    public boolean shouldExecute() {
      if (jumpCooldown > 0
          || !onGround
          || isInWater()
          || isInLove()
          || !getNavigator().noPath()
          || isBurning()) return false;
      EntityPlayer player = worldObj.getClosestPlayerToEntity(EntityMBOGoat.this, 10);
      if (player != null && isBreedingItem(player.getCurrentEquippedItem())) return false;
      for (int attempt = 0; attempt < 24; attempt++) {
        int x = MathHelper.floor_double(posX) + rand.nextInt(11) - 5,
            z = MathHelper.floor_double(posZ) + rand.nextInt(11) - 5,
            y = MathHelper.floor_double(posY) + rand.nextInt(8) - 2;
        if (!safeGround(x, y, z) || getDistanceSq(x + .5, y, z + .5) < 4) continue;
        for (int ticks = 12; ticks <= 25; ticks++) {
          double initialY = jumpVelocity(y - posY, ticks);
          double horizontalX = (x + .5 - posX) / ticks, horizontalZ = (z + .5 - posZ) / ticks;
          if (initialY <= .4
              || initialY > 1.5
              || horizontalX * horizontalX + horizontalZ * horizontalZ > .5) continue;
          double px = posX, py = posY, pz = posZ, motion = initialY;
          boolean clear = true;
          for (int t = 0; t < ticks; t++) {
            px += horizontalX;
            py += motion;
            pz += horizontalZ;
            motion = (motion - .08) * .98;
            AxisAlignedBB box =
                AxisAlignedBB.getBoundingBox(
                    px - width / 2,
                    py + .02,
                    pz - width / 2,
                    px + width / 2,
                    py + height,
                    pz + width / 2);
            if (!worldObj.blockExists(
                    MathHelper.floor_double(px),
                    MathHelper.floor_double(py),
                    MathHelper.floor_double(pz))
                || !worldObj.getCollidingBoundingBoxes(EntityMBOGoat.this, box).isEmpty()) {
              clear = false;
              break;
            }
          }
          if (clear) {
            vx = horizontalX;
            vy = initialY;
            vz = horizontalZ;
            return true;
          }
        }
      }
      jumpCooldown = 100;
      return false;
    }

    public void startExecuting() {
      time = 0;
      launched = false;
      getNavigator().clearPathEntity();
    }

    public boolean continueExecuting() {
      return time < 80 && (!launched || !onGround) && !isInWater();
    }

    public void updateTask() {
      time++;
      if (time == 40) {
        motionX = vx;
        motionY = vy;
        motionZ = vz;
        onGround = false;
        launched = true;
        velocityChanged = true;
        playSound(sound("long_jump"), 1, 1);
      } else if (launched) {
        motionX = vx;
        motionZ = vz;
      }
    }

    public void resetTask() {
      jumpCooldown = 600 + rand.nextInt(601);
    }
  }
}
