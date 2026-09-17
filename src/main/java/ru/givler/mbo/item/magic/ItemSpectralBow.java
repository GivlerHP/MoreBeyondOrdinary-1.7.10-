package ru.givler.mbo.item.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import ru.givler.mbo.entity.magic.EntitySpectralArrow;
import ru.givler.mbo.magic.item.SpectralEffects;
import ru.givler.mbo.magic.item.SpectralImpactEffect;
import ru.givler.mbo.magic.item.SpectralLifetime;

/** Temporary bow with unlimited spectral arrows and a configurable hit effect. */
public class ItemSpectralBow extends ItemBow {
  private final int lifetime;
  private final String texturePrefix;
  private SpectralImpactEffect impactEffect = SpectralEffects.NONE;

  @SideOnly(Side.CLIENT)
  private IIcon[] pullIcons;

  public ItemSpectralBow(int lifetime, String texturePrefix) {
    this.lifetime = lifetime;
    this.texturePrefix = texturePrefix;
    setMaxDamage(lifetime);
    setMaxStackSize(1);
    setNoRepair();
  }

  public ItemSpectralBow setImpactEffect(SpectralImpactEffect effect) {
    impactEffect = effect == null ? SpectralEffects.NONE : effect;
    return this;
  }

  @Override
  public boolean isFull3D() {
    return true;
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    ArrowNockEvent event = new ArrowNockEvent(player, stack);
    MinecraftForge.EVENT_BUS.post(event);
    if (event.isCanceled()) return event.result;
    player.setItemInUse(stack, getMaxItemUseDuration(stack));
    return stack;
  }

  @Override
  public void onPlayerStoppedUsing(
      ItemStack stack, World world, EntityPlayer player, int remaining) {
    int chargeTicks = getMaxItemUseDuration(stack) - remaining;
    ArrowLooseEvent event = new ArrowLooseEvent(player, stack, chargeTicks);
    MinecraftForge.EVENT_BUS.post(event);
    if (event.isCanceled()) return;
    float charge = event.charge / 20.0F;
    charge = (charge * charge + charge * 2.0F) / 3.0F;
    if (charge < 0.1F) return;
    charge = Math.min(charge, 1.0F);

    EntitySpectralArrow arrow =
        new EntitySpectralArrow(world, player, charge * 2.0F, impactEffect.getId());
    if (charge == 1.0F) arrow.setIsCritical(true);
    int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
    if (power > 0) arrow.setDamage(arrow.getDamage() + power * 0.5D + 0.5D);
    int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
    if (punch > 0) arrow.setKnockbackStrength(punch);
    if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0) {
      arrow.setFire(100);
    }
    arrow.canBePickedUp = 2;
    world.playSoundAtEntity(
        player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + charge * 0.5F);
    if (!world.isRemote) world.spawnEntityInWorld(arrow);
  }

  @Override
  public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
    SpectralLifetime.tick(stack, world, entity, slot, lifetime);
  }

  @Override
  public int getMaxDamage(ItemStack stack) {
    return SpectralLifetime.getLifetime(stack, lifetime);
  }

  @Override
  public int getDisplayDamage(ItemStack stack) {
    return SpectralLifetime.getAge(stack, lifetime);
  }

  @Override
  public boolean showDurabilityBar(ItemStack stack) {
    return true;
  }

  @Override
  public double getDurabilityForDisplay(ItemStack stack) {
    return SpectralLifetime.getProgress(stack, lifetime);
  }

  @Override
  public boolean hasEffect(ItemStack stack, int pass) {
    return true;
  }

  @Override
  public boolean getIsRepairable(ItemStack first, ItemStack second) {
    return false;
  }

  @Override
  public int getItemEnchantability() {
    return 0;
  }

  @Override
  public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
    return false;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerIcons(IIconRegister register) {
    itemIcon = register.registerIcon(texturePrefix + "_standby");
    pullIcons = new IIcon[bowPullIconNameArray.length];
    for (int i = 0; i < pullIcons.length; i++) {
      pullIcons[i] = register.registerIcon(texturePrefix + "_" + bowPullIconNameArray[i]);
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(
      ItemStack stack, int pass, EntityPlayer player, ItemStack using, int remaining) {
    if (player.getItemInUse() == null) return itemIcon;
    int time = stack.getMaxItemUseDuration() - remaining;
    if (time >= 18) return pullIcons[2];
    if (time > 9) return pullIcons[1];
    if (time > 0) return pullIcons[0];
    return itemIcon;
  }
}
