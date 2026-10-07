package ru.givler.mbo.entity.fauna;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.integration.biomesoplenty.FaunaBiomesOPlenty;

/** Two inherited genes, recessive brown/weak variants and character-dependent activities. */
public final class EntityMBOPanda extends EntityAnimal {
  public static final String[] GENES = {
    "normal", "lazy", "worried", "playful", "brown", "weak", "aggressive"
  };
  private int activityTicks, unhappyCooldown, attackCooldown;
  private boolean attackedOnce;
  private float sit, previousSit, back, previousBack;

  public EntityMBOPanda(World world) {
    super(world);
    setSize(1.3F, 1.25F);
    equipmentDropChances[0] = 2F;
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(1, new PandaActivity());
    tasks.addTask(
        2,
        new EntityAIPanic(this, 2D) {
          @Override
          public boolean shouldExecute() {
            return isBurning() && super.shouldExecute();
          }
        });
    tasks.addTask(3, new PandaBreed());
    tasks.addTask(
        4,
        new EntityAIAvoidEntity(this, EntityPlayer.class, 8F, 1D, 1.5D) {
          @Override
          public boolean shouldExecute() {
            EntityPlayer player = worldObj.getClosestPlayerToEntity(EntityMBOPanda.this, 8D);
            return variant() == 2
                && activity() == 0
                && player != null
                && !isBreedingItem(player.getCurrentEquippedItem())
                && super.shouldExecute();
          }
        });
    tasks.addTask(4, new EntityAIFollowParent(this, 1.25D));
    if (FaunaBiomesOPlenty.bambooItem() != null)
      tasks.addTask(4, new EntityAITempt(this, 1D, FaunaBiomesOPlenty.bambooItem(), false));
    tasks.addTask(5, new EntityAIWander(this, 1D));
    tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
    tasks.addTask(7, new EntityAILookIdle(this));
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Byte.valueOf((byte) 0));
    dataWatcher.addObject(21, Byte.valueOf((byte) 0));
    dataWatcher.addObject(22, Byte.valueOf((byte) 0));
    dataWatcher.addObject(23, Integer.valueOf(0));
  }

  @Override
  protected boolean isAIEnabled() {
    return true;
  }

  @Override
  public boolean allowLeashing() {
    return false;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.15);
  }

  public int mainGene() {
    return dataWatcher.getWatchableObjectByte(20);
  }

  public int hiddenGene() {
    return dataWatcher.getWatchableObjectByte(21);
  }

  public static int phenotype(int main, int hidden) {
    return (main == 4 || main == 5) && main != hidden ? 0 : main;
  }

  public int variant() {
    return phenotype(mainGene(), hiddenGene());
  }

  public void genes(int main, int hidden) {
    dataWatcher.updateObject(20, Byte.valueOf((byte) MathHelper.clamp_int(main, 0, 6)));
    dataWatcher.updateObject(21, Byte.valueOf((byte) MathHelper.clamp_int(hidden, 0, 6)));
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(variant() == 5 ? 10 : 20);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed)
        .setBaseValue(variant() == 1 ? .07 : .15);
    if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
  }

  private int randomGene() {
    int value = rand.nextInt(16);
    return value == 0
        ? 1
        : value == 1 ? 2 : value == 2 ? 3 : value == 4 ? 6 : value < 9 ? 5 : value < 11 ? 4 : 0;
  }

  private int inheritedGene() {
    return rand.nextBoolean() ? mainGene() : hiddenGene();
  }

  public int activity() {
    return dataWatcher.getWatchableObjectByte(22);
  }

  public int activityTime() {
    return dataWatcher.getWatchableObjectInt(23);
  }

  private void activity(int state, int duration) {
    dataWatcher.updateObject(22, Byte.valueOf((byte) state));
    activityTicks = duration;
    dataWatcher.updateObject(23, Integer.valueOf(0));
    getNavigator().clearPathEntity();
  }

  public float sitting(float partial) {
    return previousSit + (sit - previousSit) * partial;
  }

  public float onBack(float partial) {
    return previousBack + (back - previousBack) * partial;
  }

  @Override
  public EntityAgeable createChild(EntityAgeable partner) {
    EntityMBOPanda other = (EntityMBOPanda) partner, baby = new EntityMBOPanda(worldObj);
    int a = inheritedGene(), b = other.inheritedGene();
    if (rand.nextBoolean()) {
      int swap = a;
      a = b;
      b = swap;
    }
    if (rand.nextInt(32) == 0) a = randomGene();
    if (rand.nextInt(32) == 0) b = randomGene();
    baby.genes(a, b);
    baby.setHealth(baby.getMaxHealth());
    return baby;
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return FaunaBiomesOPlenty.isBamboo(stack);
  }

  private boolean edible(ItemStack stack) {
    return isBreedingItem(stack) || stack != null && stack.getItem() == Items.cake;
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack held = player.getCurrentEquippedItem();
    if (activity() == 5) return false;
    if (activity() == 2) {
      if (!worldObj.isRemote) activity(0, 0);
      return true;
    }
    if (!isBreedingItem(held)) return super.interact(player);
    if (!worldObj.isRemote && getAttackTarget() != null) {
      setAttackTarget(null);
      attackedOnce = false;
    }
    if (isChild() || getGrowingAge() == 0 && !isInLove()) return super.interact(player);
    if (isInWater() || activity() == 1) return false;
    if (!worldObj.isRemote) {
      ItemStack previous = getHeldItem();
      if (previous != null) entityDropItem(previous, 0);
      ItemStack food = held.copy();
      food.stackSize = 1;
      setCurrentItemOrArmor(0, food);
      if (!player.capabilities.isCreativeMode && --held.stackSize == 0)
        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
      activity(1, 160);
    }
    return true;
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float amount) {
    boolean hurt = super.attackEntityFrom(source, amount);
    if (hurt && !worldObj.isRemote) {
      activity(0, 0);
      if (source.getEntity() instanceof EntityLivingBase) {
        setAttackTarget((EntityLivingBase) source.getEntity());
        attackedOnce = false;
        if (variant() == 6) {
          List<EntityMBOPanda> pandas =
              worldObj.getEntitiesWithinAABB(EntityMBOPanda.class, boundingBox.expand(16, 8, 16));
          for (EntityMBOPanda panda : pandas)
            if (panda.variant() == 6 && !panda.isChild())
              panda.setAttackTarget((EntityLivingBase) source.getEntity());
        }
      }
    }
    return hurt;
  }

  @Override
  public boolean attackEntityAsMob(Entity entity) {
    boolean result = entity.attackEntityFrom(DamageSource.causeMobDamage(this), 6);
    if (result) {
      playSound("mbo:entity.panda.bite", 1, 1);
      attackedOnce = true;
      if (variant() != 6) setAttackTarget(null);
    }
    return result;
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    previousSit = sit;
    previousBack = back;
    sit = MathHelper.clamp_float(sit + (activity() == 1 || activity() == 5 ? .15F : -.19F), 0, 1);
    back = MathHelper.clamp_float(back + (activity() == 2 ? .15F : -.19F), 0, 1);
    if (worldObj.isRemote) return;
    if (unhappyCooldown > 0) unhappyCooldown--;
    if (attackCooldown > 0) attackCooldown--;
    EntityLivingBase target = getAttackTarget();
    if (target != null
        && (!target.isEntityAlive()
            || target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.isCreativeMode
            || variant() != 6 && attackedOnce)) setAttackTarget(null);
    if (activity() != 0) {
      int elapsed = activityTime() + 1;
      dataWatcher.updateObject(23, Integer.valueOf(elapsed));
      if (--activityTicks <= 0) {
        if (activity() == 1) {
          setCurrentItemOrArmor(0, null);
        }
        if (activity() == 4) sneeze();
        activity(0, 0);
      } else if (activity() == 1 && elapsed % 5 == 0) {
        playSound("mbo:entity.panda.eat", .5F + rand.nextFloat() * .5F, 1);
        worldObj.setEntityState(this, (byte) 18);
      } else if (activity() == 3) {
        if (elapsed == 1) motionY = .27;
        double angle = rotationYaw * Math.PI / 180;
        motionX = -Math.sin(angle) * .1;
        motionZ = Math.cos(angle) * .1;
      } else if (activity() == 5 && !worldObj.isThundering()) activity(0, 0);
      return;
    }
    if (getAttackTarget() != null || isInLove() || isBurning() || isInWater()) return;
    if (variant() == 2 && worldObj.isThundering()) {
      activity(5, 200);
      return;
    }
    if (isChild() && rand.nextInt(variant() == 5 ? 500 : 6000) == 0) {
      activity(4, 20);
      playSound("mbo:entity.panda.pre_sneeze", 1, 1);
      return;
    }
    if (onGround && (isChild() || variant() == 3) && rand.nextInt(variant() == 3 ? 60 : 500) == 0) {
      activity(3, 32);
      return;
    }
    if (variant() == 1 && rand.nextInt(400) == 0) {
      activity(2, 200 + rand.nextInt(1000));
      return;
    }
    if (getHeldItem() != null && edible(getHeldItem()) && !isChild()) {
      activity(1, 160);
      return;
    }
    if (ticksExisted % 20 == 0
        && !isChild()
        && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
      List<EntityItem> food =
          worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(6, 3, 6));
      for (EntityItem item : food)
        if (!item.isDead && item.delayBeforeCanPickup <= 0 && edible(item.getEntityItem())) {
          if (getDistanceSqToEntity(item) < 2) {
            ItemStack stack = item.getEntityItem();
            ItemStack one = stack.copy();
            one.stackSize = 1;
            setCurrentItemOrArmor(0, one);
            if (--stack.stackSize <= 0) item.setDead();
            else item.setEntityItemStack(stack);
            activity(1, 160);
          } else getNavigator().tryMoveToXYZ(item.posX, item.posY, item.posZ, 1D);
          break;
        }
    }
  }

  private void sneeze() {
    playSound("mbo:entity.panda.sneeze", 1, 1);
    worldObj.setEntityState(this, (byte) 19);
    if (rand.nextInt(700) == 0 && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
      dropItem(Items.slime_ball, 1);
    List<EntityMBOPanda> nearby =
        worldObj.getEntitiesWithinAABB(EntityMBOPanda.class, boundingBox.expand(10, 4, 10));
    for (EntityMBOPanda panda : nearby)
      if (panda != this
          && !panda.isChild()
          && panda.onGround
          && !panda.isInWater()
          && panda.activity() == 0) panda.getJumpHelper().setJumping();
  }

  @Override
  public void handleHealthUpdate(byte id) {
    if (id == 18 || id == 19) {
      for (int i = 0; i < 6; i++)
        worldObj.spawnParticle(
            id == 19 ? "slime" : "happyVillager",
            posX + (rand.nextDouble() - .5) * width,
            posY + .7,
            posZ + (rand.nextDouble() - .5) * width,
            0,
            0,
            0);
    } else super.handleHealthUpdate(id);
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    super.onSpawnWithEgg(data);
    genes(randomGene(), randomGene());
    setHealth(getMaxHealth());
    if (data != null && rand.nextFloat() < .2) setGrowingAge(-24000);
    return data == null ? new Family() : data;
  }

  private static final class Family implements IEntityLivingData {}

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    return FaunaConfig.allowsPosition("panda", worldObj, x, y, z)
        && worldObj.getBlock(x, y - 1, z) == Blocks.grass
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
        && !worldObj.isAnyLiquid(boundingBox);
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.panda."
        + (variant() == 6 ? "aggressive_ambient" : variant() == 2 ? "worried_ambient" : "ambient");
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.panda.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.panda.death";
  }

  @Override
  protected void func_145780_a(int x, int y, int z, Block block) {
    playSound("mbo:entity.panda.step", .15F, 1);
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {
    if (!isChild() && FaunaBiomesOPlenty.bambooItem() != null)
      dropItem(FaunaBiomesOPlenty.bambooItem(), 1);
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setString("MainGene", GENES[mainGene()]);
    tag.setString("HiddenGene", GENES[hiddenGene()]);
  }

  private int gene(String name) {
    for (int i = 0; i < GENES.length; i++) if (GENES[i].equals(name)) return i;
    return 0;
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    genes(gene(tag.getString("MainGene")), gene(tag.getString("HiddenGene")));
    activity(0, 0);
  }

  private final class PandaBreed extends EntityAIMate {
    private int checkAt;
    private boolean bambooNearby;

    PandaBreed() {
      super(EntityMBOPanda.this, 1D);
    }

    @Override
    public boolean shouldExecute() {
      if (activity() != 0 || !super.shouldExecute()) return false;
      if (ticksExisted < checkAt) return bambooNearby;
      checkAt = ticksExisted + 40;
      bambooNearby = false;
      int px = MathHelper.floor_double(posX),
          py = MathHelper.floor_double(posY),
          pz = MathHelper.floor_double(posZ);
      for (int y = 0; y < 3; y++)
        for (int x = -7; x <= 7; x++)
          for (int z = -7; z <= 7; z++)
            if (worldObj.blockExists(px + x, py + y, pz + z)
                && FaunaBiomesOPlenty.isBamboo(worldObj.getBlock(px + x, py + y, pz + z))) {
              bambooNearby = true;
              return true;
            }
      if (unhappyCooldown == 0) {
        unhappyCooldown = 600;
        activity(6, 32);
        playSound("mbo:entity.panda.cant_breed", 1, 1);
      }
      return false;
    }
  }

  private final class PandaActivity extends EntityAIBase {
    PandaActivity() {
      setMutexBits(3);
    }

    public boolean shouldExecute() {
      return activity() != 0 || getAttackTarget() != null;
    }

    public boolean continueExecuting() {
      return shouldExecute();
    }

    public void updateTask() {
      if (activity() != 0) {
        getNavigator().clearPathEntity();
        moveForward = 0;
        moveStrafing = 0;
        return;
      }
      EntityLivingBase target = getAttackTarget();
      if (target == null) return;
      getLookHelper().setLookPositionWithEntity(target, 30, 30);
      getNavigator().tryMoveToEntityLiving(target, 1.2);
      if (getDistanceSqToEntity(target) < width * width * 4 + target.width && attackCooldown == 0) {
        attackCooldown = 20;
        attackEntityAsMob(target);
      }
    }
  }
}
