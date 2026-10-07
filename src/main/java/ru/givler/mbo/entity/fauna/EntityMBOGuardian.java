package ru.givler.mbo.entity.fauna;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.registry.ItemRegistry;

/** Aquatic hostile mob: charged line-of-sight beam and retractable defensive spikes. */
public class EntityMBOGuardian extends EntityMob {
  private int beamTicks, attackDelay;
  private double goalX, goalY, goalZ;

  public EntityMBOGuardian(World world) {
    super(world);
    setSize(.85F, .85F);
    experienceValue = 10;
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Integer.valueOf(0));
    dataWatcher.addObject(21, Integer.valueOf(0));
    dataWatcher.addObject(22, Byte.valueOf((byte) 0));
  }

  @Override
  protected boolean isAIEnabled() {
    return false;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.5);
    getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);
    getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16);
  }

  @Override
  public boolean canBreatheUnderwater() {
    return true;
  }

  public int beamTarget() {
    return dataWatcher.getWatchableObjectInt(20);
  }

  public float beamCharge() {
    return dataWatcher.getWatchableObjectInt(21) / 80F;
  }

  public boolean swimming() {
    return dataWatcher.getWatchableObjectByte(22) != 0;
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (!worldObj.isRemote
        && !swimming()
        && !source.isMagicDamage()
        && source.getSourceOfDamage() instanceof EntityLivingBase
        && source.getEntity() != this)
      ((EntityLivingBase) source.getSourceOfDamage())
          .attackEntityFrom(DamageSource.causeThornsDamage(this), 2);
    return super.attackEntityFrom(source, amount);
  }

  @Override
  public void onLivingUpdate() {
    if (!worldObj.isRemote) {
      if (attackDelay > 0) attackDelay--;
      EntityLivingBase target = getAttackTarget();
      if (target != null
          && (!target.isEntityAlive()
              || getDistanceSqToEntity(target) > 256
              || target instanceof EntityPlayer
                  && ((EntityPlayer) target).capabilities.isCreativeMode)) {
        setAttackTarget(null);
        target = null;
      }
      if (target == null && attackDelay == 0 && ticksExisted % 20 == 0) {
        List<EntityLivingBase> nearby =
            worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(16, 8, 16));
        double nearest = 256;
        for (EntityLivingBase entity : nearby)
          if ((entity instanceof EntityPlayer
                      && !((EntityPlayer) entity).capabilities.isCreativeMode
                  || entity instanceof EntitySquid
                  || entity instanceof EntityMBOAxolotl)
              && entity.isEntityAlive()
              && getDistanceSqToEntity(entity) > 9
              && getDistanceSqToEntity(entity) < nearest
              && getEntitySenses().canSee(entity)) {
            nearest = getDistanceSqToEntity(entity);
            target = entity;
          }
        setAttackTarget(target);
      }
      if (target != null && isInWater()) {
        dataWatcher.updateObject(22, Byte.valueOf((byte) 0));
        motionX *= .7;
        motionY *= .7;
        motionZ *= .7;
        getLookHelper().setLookPositionWithEntity(target, 90, 90);
        AquaticMovement.face(this, target.posX - posX, target.posZ - posZ, 20F);
        if (!getEntitySenses().canSee(target) || getDistanceSqToEntity(target) <= 9) {
          setAttackTarget(null);
          resetBeam();
        } else {
          beamTicks++;
          if (beamTicks == 10) {
            dataWatcher.updateObject(20, Integer.valueOf(target.getEntityId()));
            worldObj.setEntityState(this, (byte) 21);
          }
          dataWatcher.updateObject(21, Integer.valueOf(Math.max(0, beamTicks - 10)));
          if (beamTicks >= 90) {
            float magic = worldObj.difficultySetting.getDifficultyId() == 3 ? 3 : 1;
            target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this), magic);
            target.attackEntityFrom(DamageSource.causeMobDamage(this), 6);
            setAttackTarget(null);
            attackDelay = 20;
            resetBeam();
          }
        }
      } else {
        resetBeam();
        if (isInWater()) {
          if (ticksExisted % 60 == 0 || ticksExisted == 1) {
            goalX = posX + rand.nextInt(13) - 6;
            goalY = posY + rand.nextInt(7) - 3;
            goalZ = posZ + rand.nextInt(13) - 6;
            if (!worldObj.blockExists(
                    MathHelper.floor_double(goalX),
                    MathHelper.floor_double(goalY),
                    MathHelper.floor_double(goalZ))
                || worldObj
                        .getBlock(
                            MathHelper.floor_double(goalX),
                            MathHelper.floor_double(goalY),
                            MathHelper.floor_double(goalZ))
                        .getMaterial()
                    != Material.water) {
              goalX = posX;
              goalY = posY;
              goalZ = posZ;
            }
          }
          double dx = goalX - posX,
              dy = goalY - posY,
              dz = goalZ - posZ,
              len = Math.sqrt(dx * dx + dy * dy + dz * dz);
          dataWatcher.updateObject(22, Byte.valueOf((byte) (len > 1 ? 1 : 0)));
          if (len > 1) {
            motionX += dx / len * .025;
            motionY += dy / len * .025;
            motionZ += dz / len * .025;
            AquaticMovement.face(this, motionX, motionZ, 10F);
          }
        } else if (onGround) {
          motionY = .5;
          motionX += (rand.nextDouble() - .5) * .8;
          motionZ += (rand.nextDouble() - .5) * .8;
          onGround = false;
          playSound("mbo:entity.guardian.flop", 1, 1);
        }
      }
    }
    super.onLivingUpdate();
  }

  private void resetBeam() {
    beamTicks = 0;
    dataWatcher.updateObject(20, Integer.valueOf(0));
    dataWatcher.updateObject(21, Integer.valueOf(0));
  }

  @Override
  protected void updateEntityActionState() {
    despawnEntity();
  }

  @Override
  public void moveEntityWithHeading(float sideways, float forward) {
    if (isInWater()) {
      if (worldObj.isRemote) return; // Do not simulate movement over the network interpolation.
      moveEntity(motionX, motionY, motionZ);
      motionX *= .9;
      motionY *= .9;
      motionZ *= .9;
    } else super.moveEntityWithHeading(sideways, forward);
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(posY),
        z = MathHelper.floor_double(posZ);
    return worldObj.difficultySetting.getDifficultyId() > 0
        && FaunaConfig.allowsPosition("guardian", worldObj, x, y, z)
        && worldObj.getBlock(x, y, z).getMaterial() == Material.water
        && worldObj.getBlock(x, y - 1, z).getMaterial() == Material.water
        && (rand.nextFloat() < .05 || !worldObj.canBlockSeeTheSky(x, y, z))
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.guardian.ambient" + (isInWater() ? "" : "_land");
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.guardian.hurt" + (isInWater() ? "" : "_land");
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.guardian.death" + (isInWater() ? "" : "_land");
  }

  @Override
  public void handleHealthUpdate(byte id) {
    if (id == 21) playSound("mbo:entity.guardian.attack", 1, 1);
    else super.handleHealthUpdate(id);
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {
    int shards = rand.nextInt(3) + rand.nextInt(looting + 1);
    if (shards > 0) entityDropItem(new ItemStack(ItemRegistry.prismarineShard, shards), 0);
    if (rand.nextInt(3) == 0)
      entityDropItem(new ItemStack(isBurning() ? Items.cooked_fished : Items.fish), 0);
  }
}
