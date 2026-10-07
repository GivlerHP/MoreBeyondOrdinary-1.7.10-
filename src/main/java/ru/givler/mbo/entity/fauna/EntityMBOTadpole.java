package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public final class EntityMBOTadpole extends EntityMBOFish {
  public static final int GROWTH_TICKS = 24000;
  private int age;
  private boolean ageLocked;
  private int conversionRetry;

  public EntityMBOTadpole(World world) {
    super(world);
    setSize(.4F, .3F);
  }

  public String species() {
    return "tadpole";
  }

  public int foodMeta() {
    return 4;
  }

  @Override
  protected boolean canSchool() {
    return false;
  }

  @Override
  protected float bodyWidth() {
    return .4F;
  }

  @Override
  protected boolean canDespawn() {
    return false;
  }

  @Override
  public boolean fromBucket() {
    return true;
  }

  public int age() {
    return age;
  }

  public void setAge(int value) {
    age = MathHelper.clamp_int(value, 0, GROWTH_TICKS);
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1D);
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (worldObj.isRemote || !isEntityAlive() || ageLocked) return;
    if (age < GROWTH_TICKS) age++;
    if (age == GROWTH_TICKS) {
      if (conversionRetry > 0) {
        conversionRetry--;
        return;
      }
      conversionRetry = 40;
      EntityMBOFrog frog = new EntityMBOFrog(worldObj);
      frog.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
      frog.setVariant(EntityMBOFrog.variantFor(worldObj, posX, posZ));
      frog.func_110163_bv();
      if (hasCustomNameTag()) frog.setCustomNameTag(getCustomNameTag());
      if (worldObj.spawnEntityInWorld(frog)) {
        playSound("mbo:entity.tadpole.grow_up", 1F, 1F);
        setDead();
      }
    }
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack food = player.getCurrentEquippedItem();
    if (!ageLocked && food != null && food.getItem() == Items.slime_ball) {
      if (!worldObj.isRemote) {
        age = Math.min(GROWTH_TICKS, age + Math.max(1, (GROWTH_TICKS - age) / 10));
        if (!player.capabilities.isCreativeMode && --food.stackSize == 0)
          player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        worldObj.setEntityState(this, (byte) 18);
      }
      return true;
    }
    return super.interact(player);
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {}

  @Override
  public void handleHealthUpdate(byte status) {
    if (status == 18) {
      for (int i = 0; i < 7; i++)
        worldObj.spawnParticle(
            "happyVillager",
            posX + (rand.nextDouble() - .5) * width,
            posY + rand.nextDouble() * height,
            posZ + (rand.nextDouble() - .5) * width,
            0,
            0,
            0);
    } else super.handleHealthUpdate(status);
  }

  @Override
  public NBTTagCompound bucketData() {
    NBTTagCompound data = super.bucketData();
    data.setInteger("Age", age);
    data.setBoolean("AgeLocked", ageLocked);
    return data;
  }

  @Override
  public void readBucketData(NBTTagCompound data) {
    super.readBucketData(data);
    setAge(data.getInteger("Age"));
    ageLocked = data.getBoolean("AgeLocked");
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound data) {
    super.writeEntityToNBT(data);
    data.setInteger("Age", age);
    data.setBoolean("AgeLocked", ageLocked);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound data) {
    super.readEntityFromNBT(data);
    setAge(data.getInteger("Age"));
    ageLocked = data.getBoolean("AgeLocked");
  }
}
