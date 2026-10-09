package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.WorldEvent;
import ru.givler.mbo.block.fauna.BlockPowderSnow;
import ru.givler.mbo.entity.fauna.EntityMBOAxolotl;
import ru.givler.mbo.entity.fauna.EntityMBOPolarBear;
import ru.givler.mbo.entity.monster.EntityMBOStray;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.registry.PotionRegistry;

public final class FaunaEvents {
  @SubscribeEvent
  public void axolotlAssist(LivingDeathEvent event) {
    if (event.entityLiving.worldObj.isRemote
        || event.isCanceled()
        || !(event.source.getEntity() instanceof EntityPlayer)) return;
    EntityPlayer player = (EntityPlayer) event.source.getEntity();
    List<EntityMBOAxolotl> animals =
        player.worldObj.getEntitiesWithinAABB(
            EntityMBOAxolotl.class, player.boundingBox.expand(20, 20, 20));
    for (EntityMBOAxolotl animal : animals)
      if (animal.getAttackTarget() == event.entityLiving) {
        PotionEffect old = player.getActivePotionEffect(Potion.regeneration);
        player.addPotionEffect(
            new PotionEffect(
                Potion.regeneration.id,
                Math.min(2400, 100 + (old == null ? 0 : old.getDuration()))));
        player.removePotionEffect(Potion.digSlowdown.id);
        break;
      }
  }

  @SubscribeEvent
  public void seagrassBonemeal(BonemealEvent event) {
    if (event.isCanceled() || event.world.isRemote) return;
    int x = event.x, y = event.y + 1, z = event.z;
    if (!BlockRegistry.seagrassBlock.canPlaceBlockAt(event.world, x, y, z)) return;
    boolean placed = false;
    for (int i = 0; i < 48; i++) {
      int sx = i == 0 ? x : x + event.world.rand.nextInt(9) - 4,
          sz = i == 0 ? z : z + event.world.rand.nextInt(9) - 4,
          sy = i == 0 ? y : y + event.world.rand.nextInt(3) - 1;
      if (event.world.blockExists(sx, sy, sz)
          && event.world.canMineBlock(event.entityPlayer, sx, sy, sz)
          && event.entityPlayer.canPlayerEdit(
              sx, sy, sz, 1, event.entityPlayer.getCurrentEquippedItem())
          && BlockRegistry.seagrassBlock.canPlaceBlockAt(event.world, sx, sy, sz))
        placed |= event.world.setBlock(sx, sy, sz, BlockRegistry.seagrassBlock, 0, 3);
    }
    if (placed) event.setResult(Event.Result.ALLOW);
  }

  public static final DamageSource FREEZING =
      new DamageSource("mbo.freeze").setDamageBypassesArmor();

  @SubscribeEvent
  public void worldLoad(WorldEvent.Load event) {
    if (!event.world.isRemote && !event.world.getGameRules().hasRule("doFreezeDamage"))
      event.world.getGameRules().addGameRule("doFreezeDamage", "true");
  }

  public static boolean freezeImmune(EntityLivingBase entity) {
    if (entity instanceof EntityMBOStray
        || entity.getClass() == EntitySkeleton.class
            && ((EntitySkeleton) entity).getSkeletonType() == 0) return true;
    if (entity instanceof EntityMBOPolarBear
        || entity instanceof EntitySnowman
        || entity instanceof EntityWither) return true;
    for (int slot = 1; slot <= 4; slot++) {
      ItemStack armor = entity.getEquipmentInSlot(slot);
      if (armor != null
          && armor.getItem() instanceof ItemArmor
          && ((ItemArmor) armor.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.CLOTH)
        return true;
    }
    return false;
  }

  @SubscribeEvent
  public void livingTick(LivingEvent.LivingUpdateEvent event) {
    EntityLivingBase entity = event.entityLiving;
    if (entity.worldObj.isRemote) return;
    NBTTagCompound data = entity.getEntityData();
    int frozen = data.getInteger("MBOFrozenTicks");
    boolean inside = BlockPowderSnow.contains(entity);
    frozen = inside && !freezeImmune(entity) ? Math.min(140, frozen + 1) : Math.max(0, frozen - 2);
    if (frozen > 0) data.setInteger("MBOFrozenTicks", frozen);
    else data.removeTag("MBOFrozenTicks");
    if (inside && frozen == 140 && entity.ticksExisted % 20 == 0 && PotionRegistry.Frost != null)
      entity.addPotionEffect(new PotionEffect(PotionRegistry.Frost.id, 40, 0));
    if (frozen == 140
        && entity.ticksExisted % 40 == 0
        && entity.worldObj.getGameRules().getGameRuleBooleanValue("doFreezeDamage"))
      entity.attackEntityFrom(
          FREEZING, entity instanceof EntityBlaze || entity instanceof EntityMagmaCube ? 5F : 2F);
  }

  @SubscribeEvent
  public void fillBucket(FillBucketEvent event) {
    if (event.isCanceled() || event.current.getItem() != Items.bucket || event.target == null)
      return;
    int x = event.target.blockX, y = event.target.blockY, z = event.target.blockZ;
    if (event.world.getBlock(x, y, z) == BlockRegistry.seagrassBlock) {
      event.setCanceled(true);
      return;
    }
    if (event.world.getBlock(x, y, z) != BlockRegistry.powderSnow) return;
    if (!event.world.canMineBlock(event.entityPlayer, x, y, z)
        || !event.entityPlayer.canPlayerEdit(x, y, z, event.target.sideHit, event.current)) return;
    if (!event.world.isRemote) {
      event.world.setBlockToAir(x, y, z);
      event.result = new ItemStack(ItemRegistry.powderSnowBucket);
      event.world.playSoundEffect(x + .5, y + .5, z + .5, "mbo:item.bucket.fill_powder_snow", 1, 1);
      event.setResult(Event.Result.ALLOW);
    }
  }
}
