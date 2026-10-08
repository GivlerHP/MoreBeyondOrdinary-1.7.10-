package ru.givler.mbo.entity.fauna;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.registry.ItemRegistry;

/** Water steering, schools, dry-land flopping and bucket persistence shared by vanilla fish. */
public abstract class EntityMBOFish extends EntityWaterMob implements IBucketableCreature {
  @Override
  public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
    return false;
  }

  private final AquaticMovement.ClientTurn clientTurn = new AquaticMovement.ClientTurn();

  @Override
  public boolean handleWaterMovement() {
    inWater = AquaticMovement.updateWaterContact(this);
    return inWater;
  }

  private EntityMBOFish leader;
  private double goalX, goalY, goalZ;
  private int inflate, deflate, blockedTicks, escapeTicks;

  protected EntityMBOFish(World world) {
    super(world);
    setSize(.5F, .3F);
  }

  public abstract String species();

  public abstract int foodMeta();

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Integer.valueOf(0)); // Variant (also preserved in buckets).
    dataWatcher.addObject(21, Byte.valueOf((byte) 0)); // Pufferfish inflation.
    dataWatcher.addObject(22, Byte.valueOf((byte) 0)); // From bucket.
  }

  public int variant() {
    return dataWatcher.getWatchableObjectInt(20);
  }

  public void setVariant(int value) {
    dataWatcher.updateObject(20, Integer.valueOf(value));
  }

  public int puff() {
    return dataWatcher.getWatchableObjectByte(21);
  }

  public boolean fromBucket() {
    return dataWatcher.getWatchableObjectByte(22) != 0;
  }

  public void setFromBucket(boolean value) {
    dataWatcher.updateObject(22, Byte.valueOf((byte) (value ? 1 : 0)));
  }

  protected boolean canSchool() {
    return foodMeta() != 3;
  }

  protected float bodyWidth() {
    return .5F;
  }

  protected float bodyHeight() {
    return .3F;
  }

  protected final void follow(EntityMBOFish fish) {
    leader = fish;
  }

  @Override
  protected boolean isAIEnabled() {
    return false;
  }

  @Override
  protected void updateEntityActionState() {
    despawnEntity(); // Steering is handled by the school logic, not vanilla's random land AI.
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(3D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.25D);
  }

  @Override
  protected boolean canDespawn() {
    return !fromBucket() && !hasCustomNameTag();
  }

  @Override
  public int getMaxSpawnedInChunk() {
    return 8;
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    return FaunaConfig.allowsPosition(species(), worldObj, x, y, z)
        && water(x, y, z)
        && water(x, y + 1, z)
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
  }

  private boolean water(int x, int y, int z) {
    return worldObj.getBlock(x, y, z).getMaterial() == Material.water;
  }

  public boolean inFishWater() {
    return isInWater();
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    clientTurn.update(this);
  }

  @Override
  public void onLivingUpdate() {
    float size =
        foodMeta() == 3
            ? .35F * (puff() == 0 ? .5F : puff() == 1 ? 1F : 1.5F)
            : foodMeta() == 1
                ? .7F * (variant() == 0 ? .5F : variant() == 2 ? 1.5F : 1F)
                : bodyWidth();
    if (width != size)
      setSize(size, foodMeta() == 1 ? size * (.4F / .7F) : foodMeta() == 3 ? size : bodyHeight());
    if (!worldObj.isRemote && isEntityAlive()) {
      if (isInWater()) {
        if (escapeTicks > 0) escapeTicks--;
        else if (ticksExisted % 20 == 0 || ticksExisted == 1) pickGoal();
        if (leader != null && (!leader.isEntityAlive() || getDistanceSqToEntity(leader) > 121D))
          leader = null;
        if (leader != null && escapeTicks == 0) {
          goalX = leader.posX;
          goalY = leader.posY;
          goalZ = leader.posZ;
        }
        double dx = goalX - posX,
            dy = goalY - posY,
            dz = goalZ - posZ,
            distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > .3D) {
          AquaticMovement.swim(this, dx, dz, .012D * Math.sqrt(dx * dx + dz * dz) / distance, 3F);
          motionY += dy / distance * .012D;
        }
      } else if (onGround) {
        motionX += (rand.nextFloat() * 2F - 1F) * .05D;
        motionZ += (rand.nextFloat() * 2F - 1F) * .05D;
        motionY = .4D;
        onGround = false;
        playSound("mbo:entity." + soundSpecies() + ".flop", 1F, 1F);
      }
      if (foodMeta() == 3) updatePuff();
    }
    // WaterAnimal suffocation is retained. No vanilla gravity while swimming.
    super.onLivingUpdate();
  }

  private void pickGoal() {
    EntityPlayer threat = worldObj.getClosestPlayerToEntity(this, 8D);
    if (threat != null && !threat.capabilities.isCreativeMode) {
      leader = null;
      goalX = posX + (posX - threat.posX) * 2;
      goalY = posY;
      goalZ = posZ + (posZ - threat.posZ) * 2;
      return;
    }
    if (canSchool() && leader == null && ticksExisted % 40 == 0) {
      List<EntityMBOFish> nearby =
          worldObj.getEntitiesWithinAABB(EntityMBOFish.class, boundingBox.expand(8, 4, 8));
      for (EntityMBOFish candidate : nearby)
        if (candidate != this
            && candidate.getClass() == getClass()
            && candidate.canSchool()
            && candidate.leader == null
            && candidate.getEntityId() < getEntityId()
            && followers(candidate, nearby) < (foodMeta() == 1 ? 4 : 7)) {
          leader = candidate;
          break;
        }
    }
    for (int i = 0; i < 8; i++) {
      double x = posX + rand.nextInt(11) - 5,
          y = posY + rand.nextInt(5) - 2,
          z = posZ + rand.nextInt(11) - 5;
      if (canSwimTo(x, y, z)) {
        goalX = x;
        goalY = y;
        goalZ = z;
        return;
      }
    }
    goalX = posX;
    goalY = posY;
    goalZ = posZ;
  }

  private int followers(EntityMBOFish candidate, List<EntityMBOFish> fish) {
    int count = 0;
    for (EntityMBOFish member : fish) if (member.leader == candidate) count++;
    return count;
  }

  @Override
  public void moveEntityWithHeading(float strafe, float forward) {
    if (isInWater()) {
      if (!worldObj.isRemote) {
        double attemptedY = motionY;
        moveEntity(motionX, motionY, motionZ);
        motionX *= .9D;
        motionY *= .9D;
        motionZ *= .9D;
        if (isCollidedHorizontally || isCollidedVertically && Math.abs(attemptedY) > .001) {
          if (++blockedTicks >= 4) recoverFromObstacle();
        } else blockedTicks = 0;
      }
    } else super.moveEntityWithHeading(strafe, forward);
  }

  private boolean canSwimTo(double x, double y, double z) {
    AxisAlignedBB box = boundingBox.copy().offset(x - posX, y - posY, z - posZ);
    return worldObj.blockExists(
            MathHelper.floor_double(box.minX),
            MathHelper.floor_double(box.minY),
            MathHelper.floor_double(box.minZ))
        && worldObj.blockExists(
            MathHelper.floor_double(box.maxX),
            MathHelper.floor_double(box.maxY),
            MathHelper.floor_double(box.maxZ))
        && water(
            MathHelper.floor_double(x),
            MathHelper.floor_double(box.minY + .01),
            MathHelper.floor_double(z))
        && water(
            MathHelper.floor_double(x),
            MathHelper.floor_double(box.maxY - .01),
            MathHelper.floor_double(z))
        && worldObj.getCollidingBoundingBoxes(this, box).isEmpty();
  }

  private void recoverFromObstacle() {
    blockedTicks = 0;
    leader = null;
    double best = Double.MAX_VALUE;
    boolean found = false;
    int cx = MathHelper.floor_double(posX),
        cy = MathHelper.floor_double(posY),
        cz = MathHelper.floor_double(posZ);
    for (int dx = -2; dx <= 2; dx++)
      for (int dy = -1; dy <= 1; dy++)
        for (int dz = -2; dz <= 2; dz++) {
          double x = cx + dx + .5, y = cy + dy + .2, z = cz + dz + .5;
          double distance = getDistanceSq(x, y, z);
          if (distance < .16 || distance >= best || !canSwimTo(x, y, z)) continue;
          best = distance;
          found = true;
          goalX = x;
          goalY = y;
          goalZ = z;
        }
    if (found) {
      escapeTicks = 40;
      motionX = motionZ = 0;
    }
  }

  private void updatePuff() {
    boolean threatened = false;
    List<EntityLivingBase> nearby =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(2, 2, 2));
    for (EntityLivingBase mob : nearby) {
      if (mob == this || mob instanceof EntityWaterMob || !mob.isEntityAlive()) continue;
      if (mob instanceof EntityPlayer && ((EntityPlayer) mob).capabilities.isCreativeMode) continue;
      threatened = true;
      break;
    }
    if (threatened) {
      inflate++;
      deflate = 0;
      if (puff() == 0) setPuff(1);
      else if (inflate > 40 && puff() == 1) setPuff(2);
    } else {
      inflate = 0;
      deflate++;
      if (deflate > 100 && puff() == 1) setPuff(0);
      else if (deflate > 60 && puff() == 2) setPuff(1);
    }
    if (puff() > 0) {
      List<EntityLivingBase> touching =
          worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(.15, .15, .15));
      for (EntityLivingBase mob : touching) {
        if (mob == this || mob instanceof EntityWaterMob) continue;
        if (mob.attackEntityFrom(DamageSource.causeMobDamage(this), 1 + puff())) {
          mob.addPotionEffect(new PotionEffect(Potion.poison.id, 60 * puff(), 0));
          playSound("mbo:entity.puffer_fish.sting", 1F, 1F);
        }
      }
    }
  }

  private void setPuff(int value) {
    dataWatcher.updateObject(21, Byte.valueOf((byte) value));
    setSize(
        .35F * (value == 0 ? .5F : value == 1 ? 1F : 1.5F),
        .35F * (value == 0 ? .5F : value == 1 ? 1F : 1.5F));
    playSound("mbo:entity.puffer_fish." + (value > 0 ? "blow_up" : "blow_out"), 1F, 1F);
  }

  public NBTTagCompound bucketData() {
    NBTTagCompound data = new NBTTagCompound();
    data.setInteger("Variant", variant());
    data.setFloat("Health", getHealth());
    if (hasCustomNameTag()) data.setString("CustomName", getCustomNameTag());
    return data;
  }

  public void readBucketData(NBTTagCompound data) {
    setVariant(data.getInteger("Variant"));
    if (data.hasKey("Health"))
      setHealth(Math.max(.1F, Math.min(getMaxHealth(), data.getFloat("Health"))));
    if (data.hasKey("CustomName")) setCustomNameTag(data.getString("CustomName"));
    setFromBucket(true);
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack held = player.getCurrentEquippedItem();
    if (held == null || held.getItem() != Items.water_bucket || !isEntityAlive())
      return super.interact(player);
    if (!worldObj.isRemote) {
      ItemStack bucket = new ItemStack(ItemRegistry.fishBuckets[foodMeta()]);
      bucket.setTagCompound(bucketData());
      if (hasCustomNameTag()) bucket.setStackDisplayName(getCustomNameTag());
      if (!player.capabilities.isCreativeMode)
        player.inventory.setInventorySlotContents(player.inventory.currentItem, bucket);
      else if (!player.inventory.addItemStackToInventory(bucket))
        player.dropPlayerItemWithRandomChoice(bucket, false);
      playSound("game.neutral.swim", 1F, 1F);
      setDead();
    }
    return true;
  }

  private String soundSpecies() {
    return foodMeta() == 3 ? "puffer_fish" : species();
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity." + soundSpecies() + ".hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity." + soundSpecies() + ".death";
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {
    entityDropItem(
        new ItemStack(
            isBurning() && foodMeta() < 2 ? Items.cooked_fished : Items.fish, 1, foodMeta()),
        0F);
    if (rand.nextFloat() < .05F) entityDropItem(new ItemStack(Items.dye, 1, 15), 0F);
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound data) {
    super.writeEntityToNBT(data);
    data.setInteger("FishVariant", variant());
    data.setBoolean("FromBucket", fromBucket());
    data.setByte("PuffState", (byte) puff());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound data) {
    super.readEntityFromNBT(data);
    setVariant(data.getInteger("FishVariant"));
    setFromBucket(data.getBoolean("FromBucket"));
    if (foodMeta() == 3)
      dataWatcher.updateObject(
          21, Byte.valueOf((byte) MathHelper.clamp_int(data.getByte("PuffState"), 0, 2)));
  }
}
