package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class EntityMBOTropicalFish extends EntityMBOFish {
  private boolean school = true;
  // Pattern, body and accent of the 22 common variants in the reference client.
  private static final int[][] COMMON = {
    {7, 1, 7}, {6, 7, 7}, {6, 7, 11}, {11, 0, 7}, {1, 11, 7}, {0, 1, 0}, {5, 6, 3}, {9, 10, 4},
    {11, 0, 14}, {5, 0, 4}, {8, 0, 7}, {11, 0, 1}, {3, 9, 6}, {4, 5, 3}, {10, 14, 0}, {2, 7, 14},
    {9, 14, 0}, {6, 0, 4}, {0, 14, 0}, {1, 7, 0}, {3, 9, 4}, {6, 4, 4}
  };

  public EntityMBOTropicalFish(World world) {
    super(world);
  }

  public String species() {
    return "tropical_fish";
  }

  public int foodMeta() {
    return 2;
  }

  @Override
  protected boolean canSchool() {
    return school;
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    super.onSpawnWithEgg(data);
    if (data instanceof Group) {
      Group group = (Group) data;
      setVariant(group.variant);
      follow(group.leader);
    } else if (rand.nextFloat() < .9F) {
      int[] common = COMMON[rand.nextInt(COMMON.length)];
      int pattern = common[0];
      setVariant((pattern >= 6 ? 1 : 0) | (pattern % 6) << 8 | common[1] << 16 | common[2] << 24);
      data = new Group(this, variant());
    } else {
      school = false;
      setVariant(
          rand.nextInt(2) | rand.nextInt(6) << 8 | rand.nextInt(16) << 16 | rand.nextInt(16) << 24);
    }
    return data;
  }

  private static final class Group implements IEntityLivingData {
    final EntityMBOTropicalFish leader;
    final int variant;

    Group(EntityMBOTropicalFish leader, int variant) {
      this.leader = leader;
      this.variant = variant;
    }
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound data) {
    super.writeEntityToNBT(data);
    data.setBoolean("School", school);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound data) {
    super.readEntityFromNBT(data);
    school = !data.hasKey("School") || data.getBoolean("School");
  }
}
