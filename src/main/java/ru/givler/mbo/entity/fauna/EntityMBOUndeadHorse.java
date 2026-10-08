package ru.givler.mbo.entity.fauna;

import java.util.UUID;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.registry.ItemRegistry;

/** Shared feeding, taming and armor support for the two undead horse variants. */
public abstract class EntityMBOUndeadHorse extends EntityHorse {
  public static final UUID HEALTH_BONUS_ID =
      UUID.fromString("a9454e3c-e645-4b7b-9e2a-78dfbc28cdf4");
  public static final UUID SPEED_BONUS_ID = UUID.fromString("a9454e3c-e645-4b7b-9e2a-78dfbc28cdf5");
  private static final String[] ARMOR_TEXTURES = {
    null,
    "textures/entity/horse/armor/horse_armor_iron.png",
    "textures/entity/horse/armor/horse_armor_gold.png",
    "textures/entity/horse/armor/horse_armor_diamond.png"
  };

  protected EntityMBOUndeadHorse(World world) {
    super(world);
    applyUndeadTraits();
    setHealth(getMaxHealth());
  }

  protected abstract double healthBonus();

  protected abstract double speedBonus();

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.2D);
  }

  private void applyUndeadTraits() {
    applyBonus(
        getEntityAttribute(SharedMonsterAttributes.maxHealth),
        HEALTH_BONUS_ID,
        "MBO undead horse health",
        healthBonus());
    applyBonus(
        getEntityAttribute(SharedMonsterAttributes.movementSpeed),
        SPEED_BONUS_ID,
        "MBO undead horse speed",
        speedBonus());
  }

  private static void applyBonus(
      IAttributeInstance attribute, UUID id, String name, double amount) {
    AttributeModifier previous = attribute.getModifier(id);
    if (previous != null) attribute.removeModifier(previous);
    attribute.applyModifier(new AttributeModifier(id, name, amount, 2));
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    IEntityLivingData result = super.onSpawnWithEgg(data);
    // Generate individual base traits using the ordinary vanilla horse distributions.
    getEntityAttribute(SharedMonsterAttributes.maxHealth)
        .setBaseValue(15D + rand.nextInt(8) + rand.nextInt(9));
    getEntityAttribute(SharedMonsterAttributes.movementSpeed)
        .setBaseValue(
            (.45D + rand.nextDouble() * .3D + rand.nextDouble() * .3D + rand.nextDouble() * .3D)
                * .25D);
    applyUndeadTraits();
    setHealth(getMaxHealth());
    return result;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setInteger("MBOUndeadHorseTraitsVersion", 2);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    IAttributeInstance health = getEntityAttribute(SharedMonsterAttributes.maxHealth);
    IAttributeInstance speed = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
    if (health.getModifier(HEALTH_BONUS_ID) != null)
      health.removeModifier(health.getModifier(HEALTH_BONUS_ID));
    if (speed.getModifier(SPEED_BONUS_ID) != null)
      speed.removeModifier(speed.getModifier(SPEED_BONUS_ID));
    super.readEntityFromNBT(tag);
    boolean fullHealth = getHealth() >= getMaxHealth();
    if (tag.getInteger("MBOUndeadHorseTraitsVersion") == 1) {
      // Undo the brief fixed-value implementation before attaching permanent species bonuses.
      health.setBaseValue(health.getBaseValue() / (1D + healthBonus()));
      speed.setBaseValue(speed.getBaseValue() / (1D + speedBonus()));
    }
    applyUndeadTraits();
    setHealth(fullHealth ? getMaxHealth() : Math.min(getHealth(), getMaxHealth()));
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack stack = player.getCurrentEquippedItem();
    if (!isTame()
        && stack != null
        && stack.getItem() == ItemRegistry.Drop
        && stack.getItemDamage() == 0) {
      if (!worldObj.isRemote) {
        consume(player, stack);
        boolean success = rand.nextInt(3) == 0;
        if (success) setTamedBy(player);
        worldObj.setEntityState(this, (byte) (success ? 7 : 6));
      }
      return true;
    }
    if (stack != null && isHealingItem(stack)) {
      if (!worldObj.isRemote && getHealth() < getMaxHealth()) {
        heal(3F);
        consume(player, stack);
        playSound("mob.horse.eat", 1F, 1F);
      }
      return true;
    }
    return super.interact(player);
  }

  public boolean isHealingItem(ItemStack stack) {
    return stack != null
        && (getHorseType() == 3
            ? stack.getItem() == Items.rotten_flesh
            : stack.getItem() == Items.dye && stack.getItemDamage() == 15);
  }

  private static void consume(EntityPlayer player, ItemStack stack) {
    if (!player.capabilities.isCreativeMode && --stack.stackSize == 0)
      player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
  }

  @Override
  public boolean func_110259_cr() {
    return true;
  }

  @Override
  public boolean allowLeashing() {
    return !getLeashed();
  }

  @Override
  public EnumCreatureAttribute getCreatureAttribute() {
    return EnumCreatureAttribute.UNDEAD;
  }

  @Override
  public String getHorseTexture() {
    return "mbo/undead_horse_" + getHorseType() + "_armor_" + func_110241_cb();
  }

  @Override
  public String[] getVariantTexturePaths() {
    return new String[] {
      getHorseType() == 3
          ? "textures/entity/horse/horse_zombie.png"
          : "textures/entity/horse/horse_skeleton.png",
      null,
      ARMOR_TEXTURES[func_110241_cb()]
    };
  }
}
