package ru.givler.mbo.handler;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import ru.givler.mbo.block.fauna.BlockPowderSnow;
import ru.givler.mbo.entity.fauna.EntityMBOCamelHusk;
import ru.givler.mbo.entity.fauna.EntityMBOCamelSeat;
import ru.givler.mbo.entity.monster.*;
import ru.givler.mbo.integration.minefantasy2.UndeadMineFantasy;
import ru.givler.mbo.item.weapon.ItemEffectArrow;
import ru.givler.mbo.registry.ItemRegistry;

public final class UndeadEvents {
  public static IEntityLivingData naturalSpawn(EntityLiving entity, IEntityLivingData data) {
    if (entity instanceof EntityMBOHusk)
      entity.getEntityData().setBoolean("MBONaturalUndeadSpawn", true);
    return entity.onSpawnWithEgg(data);
  }

  public static void createCamelJockey(EntityMBOHusk rider) {
    World world = rider.worldObj;
    if (world.isRemote || !rider.isEntityAlive() || !Loader.isModLoaded("minefantasy2")) return;
    EntityMBOCamelHusk camel = new EntityMBOCamelHusk(world);
    camel.getEntityData().setBoolean("MBOHostileCamel", true);
    camel.setLocationAndAngles(rider.posX, rider.posY, rider.posZ, rider.rotationYaw, 0);
    if (!world.getCollidingBoundingBoxes(camel, camel.boundingBox).isEmpty()
        || !world.checkNoEntityCollision(camel.boundingBox, rider)) return;
    camel.onSpawnWithEgg(null);
    if (!world.spawnEntityInWorld(camel)) return;
    EntityMBOParched passenger = new EntityMBOParched(world);
    passenger.setLocationAndAngles(rider.posX, rider.posY, rider.posZ, rider.rotationYaw, 0);
    passenger.onSpawnWithEgg(null);
    passenger.getEntityData().setString("MBOCamelJockeyRole", "archer");
    passenger.getEntityData().setBoolean("giveMFWeapon", true);
    passenger.setCanPickUpLoot(false);
    clearJockeyArmor(passenger);
    passenger.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
    if (!world.spawnEntityInWorld(passenger)) {
      camel.setDead();
      return;
    }
    rider.getEntityData().setString("MBOCamelJockeyRole", "spearman");
    rider.getEntityData().setBoolean("giveMFWeapon", true);
    rider.setCanPickUpLoot(false);
    clearJockeyArmor(rider);
    rider.setCurrentItemOrArmor(0, UndeadMineFantasy.jockeySpear());
    rider.mountEntity(camel);
    if (!camel.attachSecondPassenger(passenger)) {
      rider.mountEntity(null);
      passenger.setDead();
      camel.setDead();
      return;
    }
    passenger.getEntityData().setString("MBOJockeyCamel", camel.getUniqueID().toString());
  }

  private static void clearJockeyArmor(EntityLivingBase rider) {
    for (int slot = 1; slot <= 4; slot++)
      if (rider.getEquipmentInSlot(slot) != null) rider.setCurrentItemOrArmor(slot, null);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void jockeyEquipment(LivingUpdateEvent event) {
    if (event.entityLiving.worldObj.isRemote) return;
    EntityLivingBase living = event.entityLiving;
    boolean spearman =
        living instanceof EntityMBOHusk
            && living.ridingEntity instanceof EntityMBOCamelHusk
            && living.ridingEntity.getEntityData().getBoolean("MBOHostileCamel");
    boolean archer =
        living instanceof EntityMBOParched
            && (living.ridingEntity instanceof EntityMBOCamelSeat
                || living.getEntityData().hasKey("MBOJockeyCamel"));
    if (!spearman && !archer) return;
    living.getEntityData().setBoolean("giveMFWeapon", true);
    living.getEntityData().setString("MBOCamelJockeyRole", spearman ? "spearman" : "archer");
    ((EntityLiving) living).setCanPickUpLoot(false);
    clearJockeyArmor(living);
    if (spearman
        && Loader.isModLoaded("minefantasy2")
        && !UndeadMineFantasy.isJockeySpear(living.getHeldItem()))
      living.setCurrentItemOrArmor(0, UndeadMineFantasy.jockeySpear());
    if (archer
        && (living.getHeldItem() == null || !(living.getHeldItem().getItem() instanceof ItemBow)))
      living.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void jockey(LivingUpdateEvent event) {
    if (event.entityLiving.worldObj.isRemote || !(event.entityLiving instanceof EntityMBOParched))
      return;
    EntityMBOParched passenger = (EntityMBOParched) event.entityLiving;
    NBTTagCompound data = passenger.getEntityData();
    if (passenger.ridingEntity != null
        || passenger.ticksExisted % 20 != 0
        || !data.hasKey("MBOJockeyCamel")) return;
    for (Object raw :
        passenger.worldObj.getEntitiesWithinAABB(
            EntityMBOCamelHusk.class, passenger.boundingBox.expand(16, 16, 16))) {
      EntityMBOCamelHusk camel = (EntityMBOCamelHusk) raw;
      if (!camel.getUniqueID().toString().equals(data.getString("MBOJockeyCamel"))) continue;
      if (camel.riddenByEntity instanceof EntityMBOHusk && camel.attachSecondPassenger(passenger))
        data.removeTag("MBOJockeySearchTicks");
      return;
    }
    int ticks = data.getInteger("MBOJockeySearchTicks") + 20;
    if (ticks >= 200) {
      data.removeTag("MBOJockeyCamel");
      data.removeTag("MBOJockeySearchTicks");
    } else data.setInteger("MBOJockeySearchTicks", ticks);
  }

  private static final String EFFECT = "MBOArrowEffect";
  private static final Field IN_GROUND =
      ReflectionHelper.findField(EntityArrow.class, "inGround", "field_70254_i");

  public static boolean pickupArrow(EntityArrow arrow, EntityPlayer player) {
    if (!arrow.getEntityData().hasKey("MBOEffectArrowItem")
        && !arrow.getEntityData().hasKey("MBOEffectArrowStack")) return false;
    ItemStack ammunition =
        arrow.getEntityData().hasKey("MBOEffectArrowStack")
            ? ItemStack.loadItemStackFromNBT(
                arrow.getEntityData().getCompoundTag("MBOEffectArrowStack"))
            : new ItemStack(
                ItemRegistry.effectArrow,
                1,
                arrow.getEntityData().getInteger("MBOEffectArrowItem"));
    if (ammunition == null || !(ammunition.getItem() instanceof ItemEffectArrow)) return false;
    try {
      if (!arrow.worldObj.isRemote && IN_GROUND.getBoolean(arrow) && arrow.arrowShake <= 0) {
        boolean picked = arrow.canBePickedUp == 2 && player.capabilities.isCreativeMode;
        if (arrow.canBePickedUp == 1)
          picked = player.inventory.addItemStackToInventory(ammunition.copy());
        if (picked) {
          arrow.playSound(
              "random.pop",
              .2F,
              ((arrow.worldObj.rand.nextFloat() - arrow.worldObj.rand.nextFloat()) * .7F + 1F)
                  * 2F);
          player.onItemPickup(arrow, 1);
          arrow.setDead();
        }
      }
    } catch (IllegalAccessException failure) {
      throw new IllegalStateException("Cannot read potion arrow pickup state", failure);
    }
    return true;
  }

  public static boolean burnsInDaylight(World world, EntityLivingBase entity) {
    return world.isDaytime()
        && !(entity instanceof EntityMBOHusk)
        && !(entity instanceof EntityMBOParched);
  }

  public static void markArrow(Entity arrow, PotionEffect effect) {
    NBTTagCompound tag = new NBTTagCompound();
    effect.writeCustomPotionEffectToNBT(tag);
    arrow.getEntityData().setTag(EFFECT, tag);
  }

  public static void markArrow(Entity arrow, ItemStack ammunition) {
    NBTTagList effects = new NBTTagList();
    for (PotionEffect effect : ItemEffectArrow.effects(ammunition))
      effects.appendTag(effect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
    arrow.getEntityData().setTag("MBOArrowEffects", effects);
    ItemStack pickup = ammunition.copy();
    pickup.stackSize = 1;
    arrow.getEntityData().setTag("MBOEffectArrowStack", pickup.writeToNBT(new NBTTagCompound()));
  }

  public static boolean arrowHit(Entity target, DamageSource source, float damage) {
    Entity shooter = source.getEntity();
    if (source.getSourceOfDamage() instanceof EntityArrow
        && shooter != null
        && shooter.ridingEntity instanceof EntityMBOCamelSeat) {
      EntityMBOCamelSeat seat = (EntityMBOCamelSeat) shooter.ridingEntity;
      if (seat.getCamel() != null
          && (target == seat.getCamel()
              || target == seat.getCamel().riddenByEntity
              || target == seat)) return false;
    }
    boolean hit = target.attackEntityFrom(source, damage);
    Entity arrow = source.getSourceOfDamage();
    if (hit
        && !target.worldObj.isRemote
        && target instanceof EntityLivingBase
        && arrow instanceof EntityArrow) {
      List<PotionEffect> effects = new ArrayList<PotionEffect>();
      if (arrow.getEntityData().hasKey("MBOArrowEffects")) {
        NBTTagList list = arrow.getEntityData().getTagList("MBOArrowEffects", 10);
        for (int i = 0; i < list.tagCount(); i++) {
          PotionEffect effect =
              PotionEffect.readCustomPotionEffectFromNBT(list.getCompoundTagAt(i));
          if (effect != null) effects.add(effect);
        }
      } else {
        PotionEffect effect =
            arrow.getEntityData().hasKey(EFFECT)
                ? PotionEffect.readCustomPotionEffectFromNBT(
                    arrow.getEntityData().getCompoundTag(EFFECT))
                : null;
        if (effect == null && source.getEntity() instanceof EntityMBOVariantSkeleton)
          effect = ((EntityMBOVariantSkeleton) source.getEntity()).arrowEffect();
        if (effect != null) effects.add(effect);
      }
      ItemEffectArrow.apply((EntityLivingBase) target, source.getEntity(), effects);
    }
    return hit;
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void arrow(EntityJoinWorldEvent event) {
    if (event.world.isRemote || !(event.entity instanceof EntityArrow)) return;
    EntityArrow arrow = (EntityArrow) event.entity;
    Entity shooter =
        Loader.isModLoaded("minefantasy2")
            ? UndeadMineFantasy.shooter(arrow)
            : arrow.shootingEntity;
    if (shooter instanceof EntityMBOVariantSkeleton)
      markArrow(arrow, ((EntityMBOVariantSkeleton) shooter).arrowEffect());
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void freeze(LivingUpdateEvent event) {
    if (event.entityLiving.worldObj.isRemote
        || event.entityLiving.getClass() != EntitySkeleton.class) return;
    EntitySkeleton skeleton = (EntitySkeleton) event.entityLiving;
    if (skeleton.getSkeletonType() != 0 || !skeleton.isEntityAlive()) return;
    NBTTagCompound data = skeleton.getEntityData();
    if (!BlockPowderSnow.contains(skeleton)) {
      data.removeTag("MBOStraySnowTicks");
      data.removeTag("MBOStrayConversionTicks");
      return;
    }
    if (data.hasKey("MBOStrayConversionTicks")) {
      int ticks = data.getInteger("MBOStrayConversionTicks") - 1;
      if (ticks < 0) {
        if (!convert(skeleton, new EntityMBOStray(skeleton.worldObj))) ticks = 20;
      }
      data.setInteger("MBOStrayConversionTicks", ticks);
    } else {
      int ticks = data.getInteger("MBOStraySnowTicks") + 1;
      data.setInteger("MBOStraySnowTicks", ticks);
      if (ticks >= 140) data.setInteger("MBOStrayConversionTicks", 300);
    }
  }

  public static boolean convert(EntityLiving original, EntityLiving replacement) {
    if (original.worldObj.isRemote || !original.isEntityAlive()) return false;
    NBTTagCompound tag = new NBTTagCompound();
    original.writeToNBT(tag);
    replacement.readFromNBT(tag);
    replacement.getEntityData().removeTag("MBOStraySnowTicks");
    replacement.getEntityData().removeTag("MBOStrayConversionTicks");
    replacement.setLocationAndAngles(
        original.posX, original.posY, original.posZ, original.rotationYaw, original.rotationPitch);
    if (original instanceof EntityCreature
        && replacement instanceof EntityCreature
        && ((EntityCreature) original).getAttackTarget() != null)
      ((EntityCreature) replacement).setAttackTarget(((EntityCreature) original).getAttackTarget());
    if (!original.worldObj.spawnEntityInWorld(replacement)) return false;
    Entity vehicle = original.ridingEntity, passenger = original.riddenByEntity;
    if (vehicle != null) {
      original.mountEntity(null);
      replacement.mountEntity(vehicle);
    }
    if (passenger != null) passenger.mountEntity(replacement);
    for (int slot = 0; slot < 5; slot++) original.setCurrentItemOrArmor(slot, null);
    original.setDead();
    replacement.playSound(
        original instanceof EntityMBOHusk
            ? "mbo:entity.husk.converted_to_zombie"
            : "mbo:entity.skeleton.converted_to_stray",
        1F,
        1F);
    return true;
  }
}
