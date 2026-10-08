package ru.givler.mbo.fauna;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import minefantasy.mf2.container.ContainerHorseInventoryMF;
import minefantasy.mf2.item.armour.ItemHorseArmorMF;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import ru.givler.mbo.entity.fauna.CamelRiderInput;
import ru.givler.mbo.entity.fauna.EntityMBOCamelHusk;
import ru.givler.mbo.entity.fauna.EntityMBOSkeletonHorse;
import ru.givler.mbo.entity.fauna.EntityMBOUndeadHorse;
import ru.givler.mbo.entity.fauna.EntityMBOZombieHorse;
import ru.givler.mbo.registry.ItemRegistry;
import sun.misc.Unsafe;

public final class UndeadHorseSmoke {
  private UndeadHorseSmoke() {}

  public static void check() throws Exception {
    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    Unsafe unsafe = (Unsafe) unsafeField.get(null);
    // This headless suite has no Minecraft registries; install distinct vanilla item identities.
    String[] itemNames = {"rotten_flesh", "dye", "apple", "golden_apple", "diamond_horse_armor"};
    Object[] originalItems = new Object[itemNames.length];
    for (int i = 0; i < itemNames.length; i++) {
      Field field = Items.class.getField(itemNames[i]);
      originalItems[i] = field.get(null);
      Object item =
          field.getType() == Item.class ? new Item() : unsafe.allocateInstance(field.getType());
      unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), item);
    }
    TestWorld world = (TestWorld) unsafe.allocateInstance(TestWorld.class);
    world.rand = new Random(0);
    Field provider = World.class.getDeclaredField("provider");
    provider.setAccessible(true);
    provider.set(world, new WorldProviderSurface());
    Field listeners = World.class.getDeclaredField("worldAccesses");
    listeners.setAccessible(true);
    listeners.set(world, new ArrayList<Object>());
    TestPlayer player = (TestPlayer) unsafe.allocateInstance(TestPlayer.class);
    player.worldObj = world;
    player.inventory = new InventoryPlayer(player);
    player.capabilities = new PlayerCapabilities();
    Item previousDrop = ItemRegistry.Drop;
    ItemRegistry.Drop = new Item();
    try {
      for (EntityMBOUndeadHorse horse :
          new EntityMBOUndeadHorse[] {
            new EntityMBOZombieHorse(world), new EntityMBOSkeletonHorse(world)
          }) {
        double healthFactor = horse.getHorseType() == 3 ? 1.6D : .8D;
        double speedFactor = horse.getHorseType() == 3 ? .8D : 1.2D;
        horse.onSpawnWithEgg(null);
        double baseHealth =
            horse.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue();
        double baseSpeed =
            horse.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getBaseValue();
        if (baseHealth < 15D
            || baseHealth > 30D
            || baseSpeed < .1125D
            || baseSpeed > .3375D
            || Math.abs(horse.getMaxHealth() - baseHealth * healthFactor) > 1E-5
            || Math.abs(
                    horse
                            .getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                            .getAttributeValue()
                        - baseSpeed * speedFactor)
                > 1E-8)
          throw new AssertionError(
              "Species bonuses must multiply individually generated base traits");
        horse.setHealth(6F);
        NBTTagCompound traits = new NBTTagCompound();
        horse.writeEntityToNBT(traits);
        for (int reload = 0; reload < 3; reload++) horse.readEntityFromNBT(traits);
        if (horse.getHealth() != 6F
            || Math.abs(horse.getMaxHealth() - baseHealth * healthFactor) > 1E-5
            || Math.abs(
                    horse
                            .getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                            .getAttributeValue()
                        - baseSpeed * speedFactor)
                > 1E-8)
          throw new AssertionError("Reload must not stack species bonuses or heal injuries");
        horse.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(25D);
        horse.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3D);
        horse.writeEntityToNBT(traits);
        horse.readEntityFromNBT(traits);
        if (Math.abs(horse.getMaxHealth() - 25D * healthFactor) > 1E-5
            || Math.abs(
                    horse
                            .getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                            .getAttributeValue()
                        - .3D * speedFactor)
                > 1E-8)
          throw new AssertionError(
              "Bonuses must follow changed base traits instead of fixing final values");
        player.inventory.setInventorySlotContents(0, new ItemStack(ItemRegistry.Drop, 2, 0));
        if (!horse.allowLeashing())
          throw new AssertionError("Wild undead horses must accept leads like ordinary horses");
        horse.getRNG().setSeed(0);
        horse.interact(player);
        if (!horse.isTame() || player.getCurrentEquippedItem().stackSize != 1)
          throw new AssertionError("Spore taming must assign ownership and consume one item");
        if (!horse.allowLeashing())
          throw new AssertionError("Tamed undead horses must accept a lead");
        horse.setLeashedToEntity(player, false);
        if (!horse.getLeashed() || horse.getLeashedToEntity() != player || horse.allowLeashing())
          throw new AssertionError("A lead must attach once to the intended holder");
        horse.clearLeashed(false, false);
        if (horse.getLeashed() || !horse.allowLeashing())
          throw new AssertionError("A removed lead must allow reattachment");
        if (!horse.func_110259_cr() || !horse.isEntityUndead())
          throw new AssertionError("Undead horses must support armor and undead damage rules");
        ItemStack food =
            horse.getHorseType() == 3
                ? new ItemStack(Items.rotten_flesh, 2)
                : new ItemStack(Items.dye, 2, 15);
        horse.setHealth(horse.getMaxHealth() - 5F);
        player.inventory.setInventorySlotContents(0, food);
        horse.interact(player);
        if (horse.getHealth() != horse.getMaxHealth() - 2F || food.stackSize != 1)
          throw new AssertionError("Undead horse food must heal three HP and consume one item");
        horse.setHealth(horse.getMaxHealth());
        horse.interact(player);
        if (food.stackSize != 1) throw new AssertionError("Full health must not consume food");
        if (horse.isHealingItem(new ItemStack(Items.apple))
            || horse.isHealingItem(new ItemStack(Items.golden_apple)))
          throw new AssertionError("Apples must not heal undead horses");
        horse.setHealth(horse.getMaxHealth() - 5F);
        player.inventory.setInventorySlotContents(0, new ItemStack(Items.apple));
        horse.interact(player);
        if (horse.getHealth() != horse.getMaxHealth() - 5F)
          throw new AssertionError("Vanilla feeding must not bypass the undead diet");
        player.ridingEntity = null;
        horse.riddenByEntity = null;
        Field chestField = EntityHorse.class.getDeclaredField("horseChest");
        chestField.setAccessible(true);
        IInventory chest = (IInventory) chestField.get(horse);
        ContainerHorseInventoryMF container =
            new ContainerHorseInventoryMF(new InventoryBasic("player", true, 36), chest, horse);
        ItemStack customArmor =
            new ItemStack((ItemHorseArmorMF) unsafe.allocateInstance(ItemHorseArmorMF.class));
        if (!container.getSlot(1).func_111238_b()
            || !container.getSlot(1).isItemValid(new ItemStack(Items.diamond_horse_armor))
            || !container.getSlot(1).isItemValid(customArmor)
            || container.getSlot(1).isItemValid(new ItemStack(Items.apple)))
          throw new AssertionError(
              "MF2 armor slot must accept vanilla and MF2 armor on undead horses");
        chest.setInventorySlotContents(1, new ItemStack(Items.diamond_horse_armor));
        NBTTagCompound saved = new NBTTagCompound();
        horse.writeEntityToNBT(saved);
        if (!horse.isTame()
            || !saved.hasKey("ArmorItem")
            || horse.func_110241_cb() != 3
            || !horse.getVariantTexturePaths()[2].endsWith("horse_armor_diamond.png")
            || !horse.getVariantTexturePaths()[0].contains(
                horse.getHorseType() == 3 ? "horse_zombie" : "horse_skeleton"))
          throw new AssertionError("Armor must be saved and render over the undead base texture");
      }
      EntityMBOCamelHusk camel = new EntityMBOCamelHusk(world);
      camel.setHorseSaddled(true);
      camel.riddenByEntity = player;
      player.ridingEntity = camel;
      player.usingItem = true;
      if (CamelRiderInput.itemUseSlowsMovement(player))
        throw new AssertionError("Blocking must not slow the cadaver driver's movement or sprint");
      player.ridingEntity = null;
      if (!CamelRiderInput.itemUseSlowsMovement(player))
        throw new AssertionError("Item use on foot must retain vanilla slowdown");
    } finally {
      ItemRegistry.Drop = previousDrop;
      for (int i = 0; i < itemNames.length; i++) {
        Field field = Items.class.getField(itemNames[i]);
        unsafe.putObject(
            unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), originalItems[i]);
      }
    }
  }

  public static final class TestWorld extends WorldServer {
    public TestWorld() {
      super(null, null, "undead-horse-test", 0, null, null);
    }

    @Override
    public void setEntityState(Entity entity, byte state) {}

    @Override
    public void playSoundAtEntity(Entity entity, String sound, float volume, float pitch) {}
  }

  public static final class TestPlayer extends EntityPlayerMP {
    boolean usingItem;

    @Override
    public boolean isSneaking() {
      return false;
    }

    @Override
    public boolean isUsingItem() {
      return usingItem;
    }

    public TestPlayer() {
      super(null, null, null, null);
    }

    @Override
    public UUID getUniqueID() {
      return new UUID(0, 1);
    }

    @Override
    public void mountEntity(Entity entity) {
      ridingEntity = entity;
      if (entity != null) entity.riddenByEntity = this;
    }
  }
}
