package ru.givler.mbo.item.weapon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.handler.UndeadEvents;
import ru.givler.mbo.item.ItemBase;

/** Modern tipped-arrow variants using effects already available in Minecraft 1.7.10. */
public class ItemEffectArrow extends ItemBase {
  private IIcon head;
  private static final Map<Integer, Variant> VARIANTS = new LinkedHashMap<Integer, Variant>();

  static {
    add(100, "night_vision", new PotionEffect(Potion.nightVision.id, 450, 0));
    add(101, "long_night_vision", new PotionEffect(Potion.nightVision.id, 1200, 0));
    add(102, "invisibility", new PotionEffect(Potion.invisibility.id, 450, 0));
    add(103, "long_invisibility", new PotionEffect(Potion.invisibility.id, 1200, 0));
    add(104, "leaping", new PotionEffect(Potion.jump.id, 450, 0));
    add(105, "long_leaping", new PotionEffect(Potion.jump.id, 1200, 0));
    add(106, "strong_leaping", new PotionEffect(Potion.jump.id, 225, 1));
    add(107, "fire_resistance", new PotionEffect(Potion.fireResistance.id, 450, 0));
    add(108, "long_fire_resistance", new PotionEffect(Potion.fireResistance.id, 1200, 0));
    add(109, "swiftness", new PotionEffect(Potion.moveSpeed.id, 450, 0));
    add(110, "long_swiftness", new PotionEffect(Potion.moveSpeed.id, 1200, 0));
    add(111, "strong_swiftness", new PotionEffect(Potion.moveSpeed.id, 225, 1));
    add(2, "slowness", new PotionEffect(Potion.moveSlowdown.id, 225, 0));
    add(112, "long_slowness", new PotionEffect(Potion.moveSlowdown.id, 600, 0));
    add(113, "strong_slowness", new PotionEffect(Potion.moveSlowdown.id, 50, 3));
    add(
        114,
        "turtle_master",
        new PotionEffect(Potion.moveSlowdown.id, 50, 3),
        new PotionEffect(Potion.resistance.id, 50, 2));
    add(
        115,
        "long_turtle_master",
        new PotionEffect(Potion.moveSlowdown.id, 100, 3),
        new PotionEffect(Potion.resistance.id, 100, 2));
    add(
        116,
        "strong_turtle_master",
        new PotionEffect(Potion.moveSlowdown.id, 50, 5),
        new PotionEffect(Potion.resistance.id, 50, 3));
    add(117, "water_breathing", new PotionEffect(Potion.waterBreathing.id, 450, 0));
    add(118, "long_water_breathing", new PotionEffect(Potion.waterBreathing.id, 1200, 0));
    add(119, "healing", new PotionEffect(Potion.heal.id, 1, 0));
    add(120, "strong_healing", new PotionEffect(Potion.heal.id, 1, 1));
    add(121, "harming", new PotionEffect(Potion.harm.id, 1, 0));
    add(122, "strong_harming", new PotionEffect(Potion.harm.id, 1, 1));
    add(19, "poison", new PotionEffect(Potion.poison.id, 112, 0));
    add(123, "long_poison", new PotionEffect(Potion.poison.id, 225, 0));
    add(124, "strong_poison", new PotionEffect(Potion.poison.id, 54, 1));
    add(125, "regeneration", new PotionEffect(Potion.regeneration.id, 112, 0));
    add(126, "long_regeneration", new PotionEffect(Potion.regeneration.id, 225, 0));
    add(127, "strong_regeneration", new PotionEffect(Potion.regeneration.id, 56, 1));
    add(128, "strength", new PotionEffect(Potion.damageBoost.id, 450, 0));
    add(129, "long_strength", new PotionEffect(Potion.damageBoost.id, 1200, 0));
    add(130, "strong_strength", new PotionEffect(Potion.damageBoost.id, 225, 1));
    add(18, "weakness", new PotionEffect(Potion.weakness.id, 225, 0));
    add(131, "long_weakness", new PotionEffect(Potion.weakness.id, 600, 0));
  }

  private static void add(int metadata, String name, PotionEffect... effects) {
    VARIANTS.put(metadata, new Variant(name, effects));
  }

  public static List<PotionEffect> effects(ItemStack stack) {
    List<PotionEffect> result = new ArrayList<PotionEffect>();
    Variant variant = VARIANTS.get(stack.getItemDamage());
    if (variant != null)
      for (PotionEffect effect : variant.effects) result.add(new PotionEffect(effect));
    if (stack.hasTagCompound()) {
      NBTTagList custom = stack.getTagCompound().getTagList("CustomPotionEffects", 10);
      for (int i = 0; i < custom.tagCount(); i++) {
        PotionEffect effect =
            PotionEffect.readCustomPotionEffectFromNBT(custom.getCompoundTagAt(i));
        if (effect != null) result.add(effect);
      }
    }
    return result;
  }

  public static ItemStack custom(Item item, List<PotionEffect> potionEffects) {
    ItemStack stack = new ItemStack(item, 8, 0);
    NBTTagList effects = new NBTTagList();
    for (PotionEffect effect : potionEffects)
      effects.appendTag(
          new PotionEffect(
                  effect.getPotionID(),
                  Math.max(1, effect.getDuration() / 8),
                  effect.getAmplifier(),
                  effect.getIsAmbient())
              .writeCustomPotionEffectToNBT(new NBTTagCompound()));
    stack.setTagCompound(new NBTTagCompound());
    stack.getTagCompound().setTag("CustomPotionEffects", effects);
    return stack;
  }

  public static void apply(EntityLivingBase target, Entity source, List<PotionEffect> effects) {
    for (PotionEffect effect : effects) {
      Potion potion = Potion.potionTypes[effect.getPotionID()];
      if (potion == null) continue;
      if (potion.isInstant())
        potion.affectEntity(
            source instanceof EntityLivingBase ? (EntityLivingBase) source : null,
            target,
            effect.getAmplifier(),
            1D);
      else target.addPotionEffect(new PotionEffect(effect));
    }
  }

  public static void registerDispenser(Item item) {
    BlockDispenser.dispenseBehaviorRegistry.putObject(
        item,
        new BehaviorProjectileDispense() {
          @Override
          public ItemStack dispenseStack(IBlockSource source, final ItemStack stack) {
            return new BehaviorProjectileDispense() {
              @Override
              protected IProjectile getProjectileEntity(World world, IPosition position) {
                EntityArrow arrow =
                    new EntityArrow(world, position.getX(), position.getY(), position.getZ());
                arrow.canBePickedUp = 1;
                UndeadEvents.markArrow(arrow, stack);
                return arrow;
              }
            }.dispenseStack(source, stack);
          }

          @Override
          protected IProjectile getProjectileEntity(World world, IPosition position) {
            throw new IllegalStateException("Potion arrows need their ammunition stack");
          }
        });
  }

  private static final class Variant {
    final String name;
    final PotionEffect[] effects;

    Variant(String name, PotionEffect[] effects) {
      this.name = name;
      this.effects = effects;
    }
  }

  public ItemEffectArrow() {
    super("mbo.effect_arrow", "weapon/tipped_arrow_base", 64, false);
    setHasSubtypes(true);
  }

  /** Legacy single-effect lookup used by skeleton drops. */
  public static PotionEffect effect(int metadata) {
    Variant variant = VARIANTS.get(metadata);
    return variant == null || variant.effects.length == 0
        ? null
        : new PotionEffect(variant.effects[0]);
  }

  @Override
  public String getUnlocalizedName(ItemStack stack) {
    Variant variant = VARIANTS.get(stack.getItemDamage());
    return "item.mbo.effect_arrow." + (variant == null ? "custom" : variant.name);
  }

  @Override
  public void getSubItems(Item item, CreativeTabs tab, List list) {
    for (int id : VARIANTS.keySet()) list.add(new ItemStack(this, 1, id));
  }

  @Override
  public void registerIcons(IIconRegister icons) {
    super.registerIcons(icons);
    head = icons.registerIcon("mbo:weapon/tipped_arrow_head");
  }

  @Override
  public boolean requiresMultipleRenderPasses() {
    return true;
  }

  @Override
  public IIcon getIconFromDamageForRenderPass(int metadata, int pass) {
    return pass == 0 ? itemIcon : head;
  }

  @Override
  public int getColorFromItemStack(ItemStack stack, int pass) {
    if (pass == 0) return 0xffffff;
    int red = 0, green = 0, blue = 0, weight = 0;
    for (PotionEffect effect : effects(stack)) {
      int color = Potion.potionTypes[effect.getPotionID()].getLiquidColor();
      int count = effect.getAmplifier() + 1;
      red += (color >> 16 & 255) * count;
      green += (color >> 8 & 255) * count;
      blue += (color & 255) * count;
      weight += count;
    }
    return weight == 0 ? 0xffffff : (red / weight << 16) | (green / weight << 8) | blue / weight;
  }

  @Override
  public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
    for (PotionEffect effect : effects(stack)) {
      String name = StatCollector.translateToLocal(effect.getEffectName());
      if (effect.getAmplifier() > 0) name += " " + (effect.getAmplifier() + 1);
      if (!Potion.potionTypes[effect.getPotionID()].isInstant())
        name += " (" + Potion.getDurationString(effect) + ")";
      lines.add(name);
    }
  }
}
