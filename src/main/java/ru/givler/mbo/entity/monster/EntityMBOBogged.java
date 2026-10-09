package ru.givler.mbo.entity.monster;

import java.util.ArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public final class EntityMBOBogged extends EntityMBOVariantSkeleton implements IShearable {
  public EntityMBOBogged(World world) {
    super(world);
  }

  @Override
  public String variant() {
    return "bogged";
  }

  @Override
  protected double variantHealth() {
    return 16D;
  }

  @Override
  public PotionEffect arrowEffect() {
    return new PotionEffect(Potion.poison.id, 100);
  }

  @Override
  public int bowCooldown(boolean hard) {
    return hard ? 50 : 70;
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(15, Byte.valueOf((byte) 0));
  }

  public boolean isSheared() {
    return dataWatcher.getWatchableObjectByte(15) != 0;
  }

  public void setSheared(boolean value) {
    dataWatcher.updateObject(15, Byte.valueOf((byte) (value ? 1 : 0)));
  }

  @Override
  public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
    return isEntityAlive() && !isSheared();
  }

  @Override
  public ArrayList<ItemStack> onSheared(
      ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
    ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
    if (worldObj.isRemote || isSheared() || !isEntityAlive()) return drops;
    setSheared(true);
    playSound("mbo:entity.bogged.shear", 1F, 1F);
    for (int i = 0; i < 2; i++)
      drops.add(new ItemStack(rand.nextBoolean() ? Blocks.red_mushroom : Blocks.brown_mushroom));
    return drops;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setBoolean("Sheared", isSheared());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    setSheared(tag.getBoolean("Sheared"));
  }
}
