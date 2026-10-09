package ru.givler.mbo.entity.fauna;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EnumCreatureAttribute;
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
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketCamelDash;

/** Uses the horse riding protocol, including MF2's prediction and collision guard. */
public class EntityMBOCamel extends EntityHorse {
  private static final UUID WALKING_SPEED_ID =
      UUID.fromString("6cdad74e-7462-4c93-a069-19f0693106cc");
  private static final double WALKING_SPEED_FACTOR = .09 / .19;
  private EntityMBOCamelSeat secondSeat;
  private int idleTicks;

  public EntityMBOCamel(World world) {
    super(world);
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
    dataWatcher.addObject(29, Float.valueOf(1F));
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(32);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.19);
  }

  @Override
  public double getHorseJumpStrength() {
    return .42;
  }

  @Override
  public String getCommandSenderName() {
    return hasCustomNameTag()
        ? getCustomNameTag()
        : StatCollector.translateToLocal(
            isCamelHusk() ? "entity.MBOCamelHusk.name" : "entity.MBOCamel.name");
  }

  public boolean isCamelHusk() {
    return getHorseType() == 3;
  }

  @Override
  public EnumCreatureAttribute getCreatureAttribute() {
    return isCamelHusk() ? EnumCreatureAttribute.UNDEAD : super.getCreatureAttribute();
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
    EntityMBOCamel child = new EntityMBOCamel(worldObj);
    child.randomizeTraits();
    if (other instanceof EntityMBOCamel) {
      EntityMBOCamel parent = (EntityMBOCamel) other;
      child
          .getEntityAttribute(SharedMonsterAttributes.maxHealth)
          .setBaseValue(
              (getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue()
                      + parent.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue()
                      + child.getMaxHealth())
                  / 3);
      child
          .getEntityAttribute(SharedMonsterAttributes.movementSpeed)
          .setBaseValue(
              (getCamelMaximumSpeed()
                      + parent.getCamelMaximumSpeed()
                      + child.getCamelMaximumSpeed())
                  / 3);
      child.dataWatcher.updateObject(
          29,
          Float.valueOf(
              (getCamelDashSpeed() + parent.getCamelDashSpeed() + child.getCamelDashSpeed()) / 3));
      child.setHealth(child.getMaxHealth());
    }
    return child;
  }

  public double getCamelMaximumSpeed() {
    return getEntityAttribute(SharedMonsterAttributes.movementSpeed).getBaseValue();
  }

  public float getCamelDashSpeed() {
    return dataWatcher.getWatchableObjectFloat(29);
  }

  private void randomizeTraits() {
    getEntityAttribute(SharedMonsterAttributes.maxHealth)
        .setBaseValue(28 + rand.nextInt(7) + rand.nextInt(7));
    getEntityAttribute(SharedMonsterAttributes.movementSpeed)
        .setBaseValue(.16 + (rand.nextDouble() + rand.nextDouble() + rand.nextDouble()) * .02);
    dataWatcher.updateObject(29, Float.valueOf(.85F + rand.nextFloat() * .3F));
    setHealth(getMaxHealth());
  }

  public void updateCamelWalkingSpeed(boolean sprinting) {
    IAttributeInstance speed = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
    AttributeModifier walking = speed.getModifier(WALKING_SPEED_ID);
    if (sprinting) {
      if (walking != null) speed.removeModifier(walking);
    } else if (walking == null) {
      speed.applyModifier(
          new AttributeModifier(
                  WALKING_SPEED_ID, "Camel walking speed", WALKING_SPEED_FACTOR - 1, 2)
              .setSaved(false));
    }
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack stack = player.getCurrentEquippedItem();
    if (!isTame() && stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.deadbush)) {
      if (!worldObj.isRemote) {
        if (!player.capabilities.isCreativeMode && --stack.stackSize == 0)
          player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        playSound("mbo:entity.camel.eat", 1, 1);
        boolean success = rand.nextInt(3) == 0;
        if (success) setTamedBy(player);
        worldObj.setEntityState(this, (byte) (success ? 7 : 6));
      }
      return true;
    }
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
        } else if (isTame() && getGrowingAge() == 0 && !isInLove()) {
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
    if (!isTame()) return false;
    if (stack != null && stack.getItem() == Items.saddle && !isChild() && !isHorseSaddled()) {
      if (!worldObj.isRemote) {
        camelInventory().setInventorySlotContents(0, new ItemStack(Items.saddle));
        playSound("mbo:entity.camel.saddle", 1, 1);
        if (!player.capabilities.isCreativeMode && --stack.stackSize == 0)
          player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
      }
      return true;
    }
    if (stack != null && stack.getItem() == Items.shears && isHorseSaddled()) {
      if (!worldObj.isRemote) {
        camelInventory().setInventorySlotContents(0, null);
        entityDropItem(new ItemStack(Items.saddle), 0);
        if (!player.capabilities.isCreativeMode) stack.damageItem(1, player);
        playSound("mob.sheep.shear", 1, 1);
      }
      return true;
    }
    if (player.isSneaking() && !isChild()) {
      openGUI(player);
      return true;
    }
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
  public void openGUI(EntityPlayer player) {
    if (isTame()
        && !isChild()
        && (riddenByEntity == null || (riddenByEntity == player && player.ridingEntity == this)))
      super.openGUI(player);
  }

  @Override
  public boolean func_110259_cr() {
    return false;
  }

  private static final class InventoryField {
    static final Field CHEST =
        ReflectionHelper.findField(EntityHorse.class, "horseChest", "field_110296_bG");
  }

  private IInventory camelInventory() {
    try {
      return (IInventory) InventoryField.CHEST.get(this);
    } catch (IllegalAccessException error) {
      throw new IllegalStateException("Cannot access camel saddle inventory", error);
    }
  }

  /** Full-charge distance on level ground without steering, extra forward input or obstacles. */
  public double getCamelDashDistance() {
    double distance = 0, height = 0, vertical = .42, horizontal = getCamelDashSpeed();
    for (int tick = 0; tick < 100; tick++) {
      distance += horizontal;
      height += vertical;
      if (height < 0) break;
      vertical = (vertical - .08) * .98;
      horizontal *= .91;
    }
    return distance;
  }

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
    if (!isTame()
        || !isHorseSaddled()
        || sitting()
        || transitioning()
        || dashCooldown() > 0
        || !onGround) return;
    float power = charge >= 90 ? 1F : .4F + .4F * MathHelper.clamp_int(charge, 0, 90) / 90F;
    double angle = rotationYaw * Math.PI / 180;
    motionX = -Math.sin(angle) * getCamelDashSpeed() * power;
    motionZ = Math.cos(angle) * getCamelDashSpeed() * power;
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
    if (driver == null || !isTame() || !isHorseSaddled()) {
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
      float input = driver.moveForward;
      if (MoreBeyondOrdinary.proxy != null)
        driver.moveForward = MoreBeyondOrdinary.proxy.camelForwardInput(driver);
      try {
        super.moveEntityWithHeading(strafe, forward);
      } finally {
        driver.moveForward = input;
      }
      return;
    }
    if (worldObj.isRemote) rotationYaw -= MathHelper.clamp_float(driver.moveStrafing, -1, 1) * 3;
    float yaw = driver.rotationYaw, oldStrafe = driver.moveStrafing, pitch = driver.rotationPitch;
    float oldForward = driver.moveForward;
    if (MoreBeyondOrdinary.proxy != null)
      driver.moveForward = MoreBeyondOrdinary.proxy.camelForwardInput(driver);
    driver.rotationYaw = rotationYaw;
    driver.rotationPitch = 0;
    driver.moveStrafing = 0;
    try {
      super.moveEntityWithHeading(0, forward);
    } finally {
      driver.rotationYaw = yaw;
      driver.rotationPitch = pitch;
      driver.moveStrafing = oldStrafe;
      driver.moveForward = oldForward;
    }
  }

  @Override
  public void onLivingUpdate() {
    updateCamelWalkingSpeed(
        riddenByEntity instanceof EntityZombie && isSprinting()
            || riddenByEntity instanceof EntityPlayer
                && (MoreBeyondOrdinary.proxy == null
                    ? riddenByEntity.isSprinting()
                    : MoreBeyondOrdinary.proxy.camelSprintInput((EntityPlayer) riddenByEntity))
                && dashCooldown() == 0);
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
    if (secondSeat != null && !secondSeat.isDead) {
      positionPassenger(secondSeat, false);
      secondSeat.updateRiderPosition();
    }
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
    // Biped mobs have no player's built-in seated Y offset.
    double riderOffset =
        (rider instanceof EntityZombie || rider instanceof EntitySkeleton)
            ? -.35D
            : rider.getYOffset();
    rider.setPosition(
        posX - Math.sin(angle) * offset,
        posY + 2 - drop + riderOffset,
        posZ + Math.cos(angle) * offset);
  }

  public void setSecondSeat(EntityMBOCamelSeat seat) {
    secondSeat = seat;
  }

  public boolean attachSecondPassenger(Entity passenger) {
    if (worldObj.isRemote || passenger.ridingEntity != null || riddenByEntity == null) return false;
    if (secondSeat == null || secondSeat.isDead) {
      EntityMBOCamelSeat seat = new EntityMBOCamelSeat(worldObj, this);
      if (!worldObj.spawnEntityInWorld(seat)) {
        secondSeat = null;
        return false;
      }
      secondSeat = seat;
    }
    if (secondSeat.riddenByEntity != null) return false;
    passenger.mountEntity(secondSeat);
    return true;
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
    setHorseTamed(false);
    randomizeTraits();
    return data;
  }

  @Override
  protected void dropFewItems(boolean player, int looting) {
    // EntityHorse.onDeath drops the saddle inventory once.
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    return worldObj.getBlock(x, y - 1, z) == Blocks.sand
        && MobSpawnConfig.allowsPosition("camel", worldObj, x, y, z)
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
    tag.setInteger("CamelTamingVersion", 1);
    tag.setInteger("CamelTraitsVersion", 1);
    tag.setFloat("CamelDashSpeed", getCamelDashSpeed());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    if (!tag.hasKey("CamelTraitsVersion")) {
      float health = getHealth();
      boolean fullHealth = health >= getMaxHealth();
      randomizeTraits();
      if (!fullHealth) setHealth(Math.min(health, getMaxHealth()));
    } else {
      dataWatcher.updateObject(
          29, Float.valueOf(MathHelper.clamp_float(tag.getFloat("CamelDashSpeed"), .85F, 1.15F)));
    }
    if (!tag.hasKey("CamelTamingVersion") && tag.getString("OwnerUUID").isEmpty())
      setHorseTamed(false);
    if (camelInventory().getStackInSlot(0) == null && tag.getBoolean("CamelSaddle"))
      camelInventory().setInventorySlotContents(0, new ItemStack(Items.saddle));
    setHorseSaddled(camelInventory().getStackInSlot(0) != null);
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
