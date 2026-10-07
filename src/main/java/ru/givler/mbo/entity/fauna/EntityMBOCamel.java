package ru.givler.mbo.entity.fauna;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketCamelDash;

/** Uses the horse riding protocol, including MF2's prediction and collision guard. */
public final class EntityMBOCamel extends EntityHorse {
  private EntityMBOCamelSeat secondSeat;
  private int idleTicks;

  public EntityMBOCamel(World world) {
    super(world);
    setHorseTamed(true);
    setScaleForAge(false);
    stepHeight = 1.5F;
    tasks.taskEntries.clear();
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(1, new EntityAIPanic(this, 2));
    tasks.addTask(2, new EntityAIMate(this, 1));
    tasks.addTask(3, new EntityAITempt(this, 1.25, Item.getItemFromBlock(Blocks.cactus), false));
    tasks.addTask(4, new EntityAIFollowParent(this, 1.25));
    tasks.addTask(5, new EntityAIWander(this, 1));
    tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6));
    tasks.addTask(7, new EntityAILookIdle(this));
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(26, Byte.valueOf((byte) 0));
    dataWatcher.addObject(27, Integer.valueOf(52));
    dataWatcher.addObject(28, Integer.valueOf(0));
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(32);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.09);
  }

  @Override
  public double getHorseJumpStrength() {
    return .42;
  }

  @Override
  public String getCommandSenderName() {
    return hasCustomNameTag()
        ? getCustomNameTag()
        : StatCollector.translateToLocal("entity.MBOCamel.name");
  }

  @Override
  public void setScaleForAge(boolean child) {
    setSize(1.7F, 2.375F - (sitting() ? 1.43F : 0));
    setScale(child ? .6F : 1F);
  }

  public boolean sitting() {
    return dataWatcher.getWatchableObjectByte(26) != 0;
  }

  public int poseTicks() {
    return dataWatcher.getWatchableObjectInt(27);
  }

  public int dashCooldown() {
    return dataWatcher.getWatchableObjectInt(28);
  }

  public boolean transitioning() {
    return poseTicks() < (sitting() ? 40 : 52);
  }

  public void setSitting(boolean value) {
    if (value == sitting()) return;
    dataWatcher.updateObject(26, Byte.valueOf((byte) (value ? 1 : 0)));
    dataWatcher.updateObject(27, Integer.valueOf(0));
    getNavigator().clearPathEntity();
    setScaleForAge(isChild());
    if (worldObj != null) playSound("mbo:entity.camel." + (value ? "sit" : "stand"), 1, 1);
  }

  private boolean canStand() {
    return worldObj
        .getCollidingBoundingBoxes(
            this,
            boundingBox.addCoord(0, 1.43 * (isChild() ? .6 : 1), 0).contract(.001, .001, .001))
        .isEmpty();
  }

  @Override
  protected boolean isMovementBlocked() {
    return sitting() || transitioning() || super.isMovementBlocked();
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.cactus);
  }

  @Override
  public boolean canMateWith(EntityAnimal other) {
    return other != this && other instanceof EntityMBOCamel && isInLove() && other.isInLove();
  }

  @Override
  public EntityAgeable createChild(EntityAgeable other) {
    return new EntityMBOCamel(worldObj);
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack stack = player.getCurrentEquippedItem();
    if (isBreedingItem(stack)) {
      if (!worldObj.isRemote) {
        boolean used = false;
        if (getHealth() < getMaxHealth()) {
          heal(2);
          used = true;
        }
        if (isChild()) {
          setGrowingAge(Math.min(0, getGrowingAge() + 200));
          used = true;
        } else if (getGrowingAge() == 0 && !isInLove()) {
          func_146082_f(player);
          if (sitting() && canStand()) setSitting(false);
          used = true;
        }
        if (used) {
          playSound("mbo:entity.camel.eat", 1, 1);
          if (!player.capabilities.isCreativeMode && --stack.stackSize == 0)
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
      }
      return true;
    }
    if (stack != null && stack.getItem() == Items.saddle && !isChild() && !isHorseSaddled()) {
      if (!worldObj.isRemote) {
        setHorseSaddled(true);
        playSound("mbo:entity.camel.saddle", 1, 1);
        if (!player.capabilities.isCreativeMode && --stack.stackSize == 0)
          player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
      }
      return true;
    }
    if (stack != null && stack.getItem() == Items.shears && isHorseSaddled()) {
      if (!worldObj.isRemote) {
        setHorseSaddled(false);
        entityDropItem(new ItemStack(Items.saddle), 0);
        if (!player.capabilities.isCreativeMode) stack.damageItem(1, player);
        playSound("mob.sheep.shear", 1, 1);
      }
      return true;
    }
    if (player.isSneaking()) return false;
    if (isChild() || player.ridingEntity != null) return false;
    if (!worldObj.isRemote) {
      if (riddenByEntity == null) player.mountEntity(this);
      else {
        if (secondSeat == null || secondSeat.isDead) {
          EntityMBOCamelSeat seat = new EntityMBOCamelSeat(worldObj, this);
          if (!worldObj.spawnEntityInWorld(seat)) return false;
          secondSeat = seat;
        }
        if (secondSeat.riddenByEntity != null) return false;
        player.mountEntity(secondSeat);
      }
    }
    return true;
  }

  @Override
  public void openGUI(EntityPlayer player) {} // Camels have no horse armour/chest inventory.

  @Override
  public void setRearing(boolean value) {
    super.setRearing(false);
  }

  @Override
  public void setEatingHaystack(boolean value) {
    super.setEatingHaystack(false);
  }

  @Override
  public void setJumpPower(int charge) {
    jumpPower = 0;
    if (!isHorseSaddled() || sitting() || transitioning() || dashCooldown() > 0 || !onGround)
      return;
    float power = charge >= 90 ? 1F : .4F + .4F * MathHelper.clamp_int(charge, 0, 90) / 90F;
    double angle = rotationYaw * Math.PI / 180;
    motionX = -Math.sin(angle) * 1.0 * power;
    motionZ = Math.cos(angle) * 1.0 * power;
    motionY = .42 * power;
    onGround = false;
    setHorseJumping(true);
    beginDash();
    if (worldObj.isRemote) PacketManager.INSTANCE.sendToServer(new PacketCamelDash(this));
  }

  public void beginDash() {
    if (dashCooldown() == 0) {
      dataWatcher.updateObject(28, Integer.valueOf(55));
      getEntityData().setInteger("MBOCamelDashTicks", 55);
      playSound("mbo:entity.camel.dash", 1, 1);
    }
  }

  @Override
  public void moveEntityWithHeading(float strafe, float forward) {
    EntityPlayer driver =
        riddenByEntity instanceof EntityPlayer ? (EntityPlayer) riddenByEntity : null;
    if (driver == null || !isHorseSaddled()) {
      super.moveEntityWithHeading(strafe, forward);
      return;
    }
    if (sitting() && driver.moveForward > 0 && !transitioning() && canStand()) setSitting(false);
    if (sitting() || transitioning()) {
      float input = driver.moveForward, turn = driver.moveStrafing;
      driver.moveForward = driver.moveStrafing = 0;
      motionX = motionZ = 0;
      try {
        super.moveEntityWithHeading(0, 0);
      } finally {
        driver.moveForward = input;
        driver.moveStrafing = turn;
      }
      return;
    }
    // MF2 steers in its horse ASM hook; do not apply that turn twice.
    if (Loader.isModLoaded("minefantasy2")) {
      super.moveEntityWithHeading(strafe, forward);
      return;
    }
    if (worldObj.isRemote) rotationYaw -= MathHelper.clamp_float(driver.moveStrafing, -1, 1) * 3;
    float yaw = driver.rotationYaw, oldStrafe = driver.moveStrafing, pitch = driver.rotationPitch;
    driver.rotationYaw = rotationYaw;
    driver.rotationPitch = 0;
    driver.moveStrafing = 0;
    try {
      super.moveEntityWithHeading(0, forward);
    } finally {
      driver.rotationYaw = yaw;
      driver.rotationPitch = pitch;
      driver.moveStrafing = oldStrafe;
    }
  }

  @Override
  public void onLivingUpdate() {
    getEntityAttribute(SharedMonsterAttributes.movementSpeed)
        .setBaseValue(
            riddenByEntity instanceof EntityPlayer
                    && riddenByEntity.isSprinting()
                    && dashCooldown() == 0
                ? .19
                : .09);
    super.onLivingUpdate();
    if (!worldObj.isRemote) {
      if (poseTicks() < 52) dataWatcher.updateObject(27, Integer.valueOf(poseTicks() + 1));
      if (dashCooldown() > 0) {
        dataWatcher.updateObject(28, Integer.valueOf(dashCooldown() - 1));
        getEntityData().setInteger("MBOCamelDashTicks", dashCooldown());
        if (dashCooldown() == 0) playSound("mbo:entity.camel.dash_ready", 1, 1);
      }
      if (onGround) setHorseJumping(false);
      if (riddenByEntity == null
          && secondSeat != null
          && secondSeat.riddenByEntity instanceof EntityPlayer
          && !secondSeat.riddenByEntity.isDead) secondSeat.riddenByEntity.mountEntity(this);
      if (riddenByEntity == null
          && (secondSeat == null || secondSeat.riddenByEntity == null)
          && onGround
          && !isInWater()
          && !isInLove()
          && getAttackTarget() == null) {
        if (++idleTicks >= 200 && rand.nextInt(200) == 0) {
          if (!sitting() || canStand()) setSitting(!sitting());
          idleTicks = 0;
        }
      } else idleTicks = 0;
      if (secondSeat != null && secondSeat.riddenByEntity == null) {
        secondSeat.setDead();
        secondSeat = null;
      }
    }
    setScaleForAge(isChild());
    stepHeight = 1.5F;
  }

  @Override
  public void updateRiderPosition() {
    if (riddenByEntity != null) positionPassenger(riddenByEntity, true);
  }

  @Override
  public void moveEntity(double x, double y, double z) {
    stepHeight = 1.5F;
    super.moveEntity(x, y, z);
  }

  public void positionPassenger(Entity rider, boolean front) {
    double angle = rotationYaw * Math.PI / 180, offset = front ? .5 : -.7;
    double drop = sitting() ? 1.43 : 0;
    if (transitioning())
      drop = sitting() ? 1.43 * Math.min(1, poseTicks() / 40D) : 1.43 * (1 - poseTicks() / 52D);
    rider.setPosition(
        posX - Math.sin(angle) * offset,
        posY + 2 - drop + rider.getYOffset(),
        posZ + Math.cos(angle) * offset);
  }

  public void setSecondSeat(EntityMBOCamelSeat seat) {
    secondSeat = seat;
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float damage) {
    if (!worldObj.isRemote && sitting() && canStand()) setSitting(false);
    return super.attackEntityFrom(source, damage);
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.camel.ambient";
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.camel.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.camel.death";
  }

  @Override
  protected String getAngrySoundName() {
    return "mbo:entity.camel.ambient";
  }

  @Override
  protected void func_145780_a(int x, int y, int z, Block block) {
    playSound("mbo:entity.camel.step", .15F, 1);
  }

  @Override
  protected Item getDropItem() {
    return null;
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    // Horse's implementation randomizes health, speed and even changes the species to a donkey.
    setHorseType(0);
    setHorseTamed(true);
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(32);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.09);
    setHealth(32);
    return data;
  }

  @Override
  protected void dropFewItems(boolean player, int looting) {
    if (isHorseSaddled()) entityDropItem(new ItemStack(Items.saddle), 0);
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    return worldObj.getBlock(x, y - 1, z) == Blocks.sand
        && FaunaConfig.allowsPosition("camel", worldObj, x, y, z)
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
        && !worldObj.isAnyLiquid(boundingBox);
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setBoolean("CamelSitting", sitting());
    tag.setInteger("CamelPoseTicks", poseTicks());
    tag.setInteger("CamelDashCooldown", dashCooldown());
    tag.setBoolean("CamelSaddle", isHorseSaddled());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    setHorseTamed(true);
    setHorseSaddled(tag.getBoolean("CamelSaddle"));
    dataWatcher.updateObject(26, Byte.valueOf((byte) (tag.getBoolean("CamelSitting") ? 1 : 0)));
    dataWatcher.updateObject(
        27,
        Integer.valueOf(
            tag.hasKey("CamelPoseTicks")
                ? MathHelper.clamp_int(tag.getInteger("CamelPoseTicks"), 0, 52)
                : 52));
    dataWatcher.updateObject(
        28, Integer.valueOf(MathHelper.clamp_int(tag.getInteger("CamelDashCooldown"), 0, 55)));
    setScaleForAge(isChild());
  }
}
