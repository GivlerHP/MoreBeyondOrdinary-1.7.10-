package ru.givler.mbo.integration.minefantasy2;

import minefantasy.mf2.api.archery.AmmoMechanicsMF;
import minefantasy.mf2.api.archery.IAmmo;
import minefantasy.mf2.api.archery.IArrowMF;
import minefantasy.mf2.entity.EntityArrowMF;
import minefantasy.mf2.item.archery.ItemBowMF;
import minefantasy.mf2.item.list.CustomToolListMF;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.weapon.ItemEffectArrow;

public final class UndeadMineFantasy {
  public static Entity shooter(EntityArrow arrow) {
    return arrow instanceof EntityArrowMF
        ? ((EntityArrowMF) arrow).shootingEntity
        : arrow.shootingEntity;
  }

  public static ItemStack jockeySpear() {
    return CustomToolListMF.standard_spear.construct("Iron", "OakWood");
  }

  private UndeadMineFantasy() {}

  public static Item createArrow() {
    Item item = new EffectArrowMF();
    AmmoMechanicsMF.addArrow(item);
    return item;
  }

  public static boolean isBow(ItemStack stack) {
    return stack.getItem() instanceof ItemBowMF;
  }

  private static final class EffectArrowMF extends ItemEffectArrow implements IAmmo, IArrowMF {
    @Override
    public String getAmmoType(ItemStack stack) {
      return "arrow";
    }

    @Override
    public float getDamageModifier(ItemStack stack) {
      return 1F;
    }

    @Override
    public float getGravityModifier(ItemStack stack) {
      return 1F;
    }

    @Override
    public float getBreakChance(Entity entity, ItemStack stack) {
      return 0F;
    }

    @Override
    public void onHitEntity(Entity arrow, Entity shooter, Entity target, float damage) {
      if (target.worldObj.isRemote
          || !(target instanceof EntityLivingBase)
          || !(arrow instanceof EntityArrowMF)) return;
      ItemStack ammunition = ((EntityArrowMF) arrow).getPickedUpItem();
      if (ammunition != null)
        ItemEffectArrow.apply((EntityLivingBase) target, shooter, effects(ammunition));
    }
  }
}
