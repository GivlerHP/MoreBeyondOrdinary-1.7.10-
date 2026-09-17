package ru.givler.mbo.item.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.magic.item.SpectralEffects;
import ru.givler.mbo.magic.item.SpectralImpactEffect;
import ru.givler.mbo.magic.item.SpectralLifetime;

/** Configurable temporary melee item used by all MBO spectral weapons. */
public class ItemSpectralWeapon extends ItemSword {
  private final int lifetime;
  private SpectralImpactEffect impactEffect = SpectralEffects.NONE;
  private float effectChance;
  private boolean enchantedAppearance = true;
  private String descriptionKey;
  private EnumChatFormatting descriptionColor = EnumChatFormatting.GRAY;
  private EnumRarity rarity = EnumRarity.rare;

  public ItemSpectralWeapon(ToolMaterial material, int lifetime) {
    super(material);
    this.lifetime = lifetime;
    setMaxDamage(lifetime);
    setMaxStackSize(1);
    setNoRepair();
  }

  public ItemSpectralWeapon setImpactEffect(SpectralImpactEffect effect, float chance) {
    this.impactEffect = effect == null ? SpectralEffects.NONE : effect;
    this.effectChance = Math.max(0.0F, Math.min(1.0F, chance));
    return this;
  }

  public ItemSpectralWeapon setEnchantedAppearance(boolean value) {
    this.enchantedAppearance = value;
    return this;
  }

  public ItemSpectralWeapon setDescription(String key, EnumChatFormatting color) {
    this.descriptionKey = key;
    this.descriptionColor = color;
    return this;
  }

  public ItemSpectralWeapon setRarityValue(EnumRarity value) {
    this.rarity = value;
    return this;
  }

  @Override
  public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
    boolean result = super.hitEntity(stack, target, attacker);
    if (!attacker.worldObj.isRemote && attacker.getRNG().nextFloat() < effectChance) {
      impactEffect.apply(attacker, target);
    }
    return result;
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
    return enchantedAppearance;
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
  public EnumRarity getRarity(ItemStack stack) {
    return rarity;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
    super.addInformation(stack, player, list, advanced);
    if (descriptionKey != null) {
      String translated = StatCollector.translateToLocal(descriptionKey);
      for (String line : translated.replace("\\n", "\n").split("\n")) {
        list.add(descriptionColor + line);
      }
    }
  }
}
