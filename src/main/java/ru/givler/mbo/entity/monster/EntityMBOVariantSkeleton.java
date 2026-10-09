package ru.givler.mbo.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.registry.ItemRegistry;

/** Keep the vanilla skeleton's combat, equipment and mod integration hooks. */
public abstract class EntityMBOVariantSkeleton extends EntitySkeleton {
  protected EntityMBOVariantSkeleton(World world) {
    super(world);
  }

  @Override
  public boolean canBreatheUnderwater() {
    return true;
  }

  public abstract String variant();

  public abstract PotionEffect arrowEffect();

  public int bowCooldown(boolean hard) {
    return hard ? 20 : 40;
  }

  protected double variantHealth() {
    return 20D;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(variantHealth());
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    data = super.onSpawnWithEgg(data);
    setSkeletonType(0);
    setCombatTask();
    return data;
  }

  @Override
  public boolean getCanSpawnHere() {
    return MobSpawnConfig.allowsUndead(variant(), this) && super.getCanSpawnHere();
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity." + variant() + ".ambient";
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity." + variant() + ".hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity." + variant() + ".death";
  }

  @Override
  protected void func_145780_a(int x, int y, int z, Block block) {
    playSound("mbo:entity." + variant() + ".step", .15F, 1F);
  }

  @Override
  protected void dropFewItems(boolean player, int looting) {
    super.dropFewItems(player, looting);
    if (player && rand.nextInt(2) + (looting > 0 ? rand.nextInt(looting + 1) : 0) > 0)
      entityDropItem(new ItemStack(ItemRegistry.effectArrow, 1, arrowEffect().getPotionID()), 0F);
  }
}
