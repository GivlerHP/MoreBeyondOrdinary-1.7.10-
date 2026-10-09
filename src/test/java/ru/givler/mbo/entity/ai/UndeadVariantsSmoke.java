package ru.givler.mbo.entity.ai;

import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.Side;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import ru.givler.mbo.block.fauna.BlockPowderSnow;
import ru.givler.mbo.client.model.monster.ModelMBOHusk;
import ru.givler.mbo.client.model.monster.ModelMBOVariantSkeleton;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.core.UndeadMechanicsTransformer;
import ru.givler.mbo.entity.monster.*;
import ru.givler.mbo.handler.FaunaEvents;
import ru.givler.mbo.handler.UndeadEvents;
import ru.givler.mbo.potion.Frost;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.PotionRegistry;
import sun.misc.Unsafe;

public final class UndeadVariantsSmoke {
  public static void main(String[] args) throws Exception {
    Field home = FMLInjectionData.class.getDeclaredField("minecraftHome");
    home.setAccessible(true);
    home.set(null, new File("build/undead-smoke-config"));
    Field side = FMLRelaunchLog.class.getDeclaredField("side");
    side.setAccessible(true);
    side.set(null, Side.SERVER);
    MobSpawnConfig.load(new File("build/undead-smoke-config"));
    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    TestWorld world =
        (TestWorld) ((Unsafe) unsafeField.get(null)).allocateInstance(TestWorld.class);
    world.isRemote = true;
    world.rand = new Random(1);
    world.difficultySetting = EnumDifficulty.NORMAL;
    Field provider = World.class.getDeclaredField("provider");
    provider.setAccessible(true);
    provider.set(world, new WorldProviderSurface());
    Field profiler = World.class.getDeclaredField("theProfiler");
    profiler.setAccessible(true);
    profiler.set(world, new Profiler());
    Field listeners = World.class.getDeclaredField("worldAccesses");
    listeners.setAccessible(true);
    listeners.set(world, new ArrayList<Object>());
    EntityMBOParched parched = new EntityMBOParched(world);
    EntityMBOBogged bogged = new EntityMBOBogged(world);
    EntityMBOStray stray = new EntityMBOStray(world);
    EntityMBOHusk husk = new EntityMBOHusk(world);
    check(
        parched.getMaxHealth() == 16
            && bogged.getMaxHealth() == 16
            && stray.getMaxHealth() == 20
            && husk.getMaxHealth() == 20,
        "variant health");
    check(
        !parched.isPotionApplicable(new PotionEffect(Potion.weakness.id, 100)),
        "weakness immunity");
    check(
        !UndeadEvents.burnsInDaylight(world, parched)
            && !UndeadEvents.burnsInDaylight(world, husk)
            && UndeadEvents.burnsInDaylight(world, stray)
            && UndeadEvents.burnsInDaylight(world, bogged),
        "daylight immunity must be specific");
    check(
        !husk.isImmuneToFire() && !parched.isImmuneToFire(),
        "sun protection must not grant fire immunity");
    check(
        parched.bowCooldown(false) == 70
            && parched.bowCooldown(true) == 50
            && bogged.bowCooldown(false) == 70
            && stray.bowCooldown(false) == 40,
        "variant bow intervals");
    check(FaunaEvents.freezeImmune(stray), "stray freezing immunity");
    ModelMBOHusk huskModel = new ModelMBOHusk();
    check(
        huskModel.bipedLeftArm.mirror && huskModel.bipedLeftLeg.mirror,
        "Excalibur husk must mirror opaque right-limb UVs for the left limbs");
    for (String variant : new String[] {"parched", "bogged", "stray"})
      new ModelMBOVariantSkeleton(variant);
    world.isRemote = false;
    TestTarget target = new TestTarget(world);
    target.accept = false;
    check(
        !husk.attackEntityAsMob(target) && !target.isPotionActive(Potion.hunger),
        "rejected husk attack applies no hunger");
    target.accept = true;
    check(
        husk.attackEntityAsMob(target)
            && target.getActivePotionEffect(Potion.hunger).getDuration() == 280,
        "successful empty-handed hunger uses local difficulty");
    target.clearActivePotions();
    husk.setCurrentItemOrArmor(0, new ItemStack(new Item()));
    check(
        husk.attackEntityAsMob(target) && !target.isPotionActive(Potion.hunger),
        "armed husk does not apply hunger");
    husk.setCurrentItemOrArmor(0, null);
    for (EntityMBOVariantSkeleton shooter :
        new EntityMBOVariantSkeleton[] {parched, bogged, stray}) {
      target.clearActivePotions();
      EntityArrow arrow = new EntityArrow(world);
      arrow.shootingEntity = shooter;
      UndeadEvents.markArrow(arrow, shooter.arrowEffect());
      target.accept = false;
      check(
          !UndeadEvents.arrowHit(target, DamageSource.causeArrowDamage(arrow, shooter), 5F)
              && !target.isPotionActive(shooter.arrowEffect().getPotionID()),
          "rejected hit applies no potion");
      target.accept = true;
      check(
          UndeadEvents.arrowHit(target, DamageSource.causeArrowDamage(arrow, shooter), 5F),
          "accepted original damage callback");
      PotionEffect applied =
          target.getActivePotionEffect(Potion.potionTypes[shooter.arrowEffect().getPotionID()]);
      check(
          applied != null
              && applied.getDuration() == shooter.arrowEffect().getDuration()
              && target.damage == 5F,
          "arrow effect and original damage");
    }
    check(bogged.isShearable(null, world, 0, 0, 0), "initial shearing state");
    // Minimal vanilla block fixtures: Forge shears drops vanilla mushrooms.
    setBlock("red_mushroom", new Block(Material.plants) {});
    setBlock("brown_mushroom", new Block(Material.plants) {});
    check(
        bogged.onSheared(null, world, 0, 0, 0, 0).size() == 2
            && !bogged.isShearable(null, world, 0, 0, 0)
            && bogged.onSheared(null, world, 0, 0, 0, 0).isEmpty(),
        "single-use two mushroom shearing");
    NBTTagCompound tag = new NBTTagCompound();
    bogged.writeEntityToNBT(tag);
    EntityMBOBogged loaded = new EntityMBOBogged(world);
    loaded.readEntityFromNBT(tag);
    check(loaded.isSheared(), "shearing NBT");
    world.spawned = null;
    Item tool = new Item();
    Method rawRegister =
        Item.itemRegistry
            .getClass()
            .getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class);
    rawRegister.setAccessible(true);
    rawRegister.invoke(Item.itemRegistry, 3002, "mbo:test_gear", tool);
    ItemStack gear = new ItemStack(tool);
    gear.setStackDisplayName("Named MF2 gear");
    gear.setTagCompound(new NBTTagCompound());
    gear.getTagCompound().setString("MF_Material", "Iron");
    husk.setCurrentItemOrArmor(0, gear);
    husk.setCustomNameTag("Named husk");
    husk.setHealth(9F);
    husk.getEntityData().setString("MF_TestMode", "keep");
    for (int i = 0; i < 600; i++) husk.tickConversion(true);
    check(world.spawned == null, "husk conversion must wait for immersion");
    for (int i = 0; i < 300; i++) husk.tickConversion(false);
    check(world.spawned == null, "husk conversion must complete its additional countdown");
    husk.tickConversion(false);
    check(
        husk.isDead
            && world.spawned instanceof EntityZombie
            && world.spawned.getClass() == EntityZombie.class,
        "husk converts to ordinary zombie");
    EntityZombie zombie = (EntityZombie) world.spawned;
    check(
        zombie.getHealth() == 9F
            && zombie.getCustomNameTag().equals("Named husk")
            && zombie.getEntityData().getString("MF_TestMode").equals("keep"),
        "conversion preserves health, name and mod data");
    check(
        zombie.getHeldItem() != null
            && zombie.getHeldItem().getItem() == tool
            && zombie.getHeldItem().getTagCompound().getString("MF_Material").equals("Iron")
            && husk.getHeldItem() == null,
        "conversion transfers modded gear without duplication");
    EntitySkeleton ordinary = new EntitySkeleton(world);
    ordinary.setHealth(7F);
    world.spawned = null;
    world.snow = true;
    BlockPowderSnow powder = new BlockPowderSnow();
    BlockRegistry.powderSnow = powder;
    check(
        !powder.isOpaqueCube() && !powder.renderAsNormalBlock(),
        "sinking snow must not hide adjacent faces or count as an opaque full cube");
    int frostId = 24;
    while (Potion.potionTypes[frostId] != null) frostId++;
    PotionRegistry.Frost = new Frost(frostId, true, 0x38ddec);
    TestTarget freezing = new TestTarget(world);
    FaunaEvents faunaEvents = new FaunaEvents();
    for (int tick = 1; tick <= 140; tick++) {
      freezing.ticksExisted = tick;
      faunaEvents.livingTick(new LivingUpdateEvent(freezing));
    }
    check(
        freezing.isPotionActive(PotionRegistry.Frost),
        "snow applies the existing MBO Frost effect after freezing");
    freezing.setPosition(0, 1, 0);
    check(!BlockPowderSnow.contains(freezing), "standing above powder snow is not immersion");
    faunaEvents.livingTick(new LivingUpdateEvent(freezing));
    check(
        freezing.getEntityData().getInteger("MBOFrozenTicks") == 138,
        "leaving snow thaws the entity");

    UndeadEvents events = new UndeadEvents();
    for (int i = 0; i < 440; i++) events.freeze(new LivingUpdateEvent(ordinary));
    check(world.spawned == null, "snow conversion must not finish early");
    events.freeze(new LivingUpdateEvent(ordinary));
    check(
        world.spawned instanceof EntityMBOStray
            && ordinary.isDead
            && ((EntityMBOStray) world.spawned).getHealth() == 7F,
        "snow conversion and health");
    EntitySkeleton interrupted = new EntitySkeleton(world);
    for (int i = 0; i < 200; i++) events.freeze(new LivingUpdateEvent(interrupted));
    world.snow = false;
    events.freeze(new LivingUpdateEvent(interrupted));
    check(
        !interrupted.getEntityData().hasKey("MBOStrayConversionTicks"),
        "leaving powder snow cancels conversion");
    world.cancelSpawn = true;
    EntityMBOHusk canceled = new EntityMBOHusk(world);
    check(
        !UndeadEvents.convert(canceled, new EntityZombie(world)) && !canceled.isDead,
        "cancelled spawn preserves old entity");
    verifySpawns(world);
    verifyAsm();
    System.out.println(
        "Undead variants: effects, immunities, timers, conversion persistence, shearing, biome weights and ASM passed");
  }

  private static void setBlock(String name, Block block) throws Exception {
    Field field = Blocks.class.getDeclaredField(name);
    field.setAccessible(true);
    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    Unsafe unsafe = (Unsafe) unsafeField.get(null);
    unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), block);
  }

  private static void verifySpawns(TestWorld world) {
    BiomeGenBase desert = BiomeGenBase.desert,
        snow = BiomeGenBase.icePlains,
        swamp = BiomeGenBase.swampland;
    int before = weight(desert, EntitySkeleton.class) + weight(desert, EntityZombie.class);
    MobSpawnConfig.registerUndeadSpawns();
    check(
        weight(desert, EntityMBOHusk.class) == 80
            && weight(desert, EntityMBOParched.class) == 50
            && weight(snow, EntityMBOStray.class) == 80
            && weight(swamp, EntityMBOBogged.class) == 30,
        "reference variant percentages");
    int after =
        weight(desert, EntitySkeleton.class)
            + weight(desert, EntityZombie.class)
            + weight(desert, EntityMBOHusk.class)
            + weight(desert, EntityMBOParched.class);
    check(
        before == after && weight(BiomeGenBase.plains, EntityMBOHusk.class) == 0,
        "unchanged spawn total and untouched unrelated biomes");
    MobSpawnConfig.registerUndeadSpawns();
    check(weight(desert, EntityMBOHusk.class) == 80, "no duplicate spawn registration");
    world.biome = desert;
    world.sky = true;
    check(MobSpawnConfig.allowsUndead("husk", new EntityMBOHusk(world)), "husk desert surface");
    world.sky = false;
    check(!MobSpawnConfig.allowsUndead("husk", new EntityMBOHusk(world)), "no cave husks");
  }

  private static int weight(BiomeGenBase biome, Class<?> type) {
    int weight = 0;
    for (Object raw : biome.getSpawnableList(EnumCreatureType.monster)) {
      SpawnListEntry entry = (SpawnListEntry) raw;
      if (entry.entityClass == type) weight += entry.itemWeight;
    }
    return weight;
  }

  private static void verifyAsm() throws Exception {
    for (String name :
        new String[] {
          "net.minecraft.entity.monster.EntityZombie",
          "net.minecraft.entity.monster.EntitySkeleton",
          "net.minecraft.entity.projectile.EntityArrow",
          "net.minecraft.world.SpawnerAnimals",
          "minefantasy.mf2.entity.EntityArrowMF"
        }) {
      try (InputStream input =
          UndeadVariantsSmoke.class.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int read; (read = input.read(buffer)) >= 0; ) output.write(buffer, 0, read);
        byte[] transformed =
            new UndeadMechanicsTransformer().transform(name, name, output.toByteArray());
        StringWriter errors = new StringWriter();
        CheckClassAdapter.verify(new ClassReader(transformed), false, new PrintWriter(errors));
        check(errors.toString().isEmpty(), errors.toString());
      }
    }
  }

  private static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }

  public static final class TestWorld extends WorldServer {
    Entity spawned;
    boolean cancelSpawn, snow, sky;
    BiomeGenBase biome;
    private static final Block AIR = new Block(Material.air) {};

    private TestWorld() {
      super(null, null, null, 0, null, null);
    }

    @Override
    public boolean isDaytime() {
      return true;
    }

    @Override
    public float func_147462_b(double x, double y, double z) {
      return 2F;
    }

    @Override
    public boolean spawnEntityInWorld(Entity entity) {
      if (cancelSpawn) return false;
      spawned = entity;
      return true;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
      return snow && y == 0 ? BlockRegistry.powderSnow : AIR;
    }

    @Override
    public boolean blockExists(int x, int y, int z) {
      return true;
    }

    @Override
    public boolean canBlockSeeTheSky(int x, int y, int z) {
      return sky;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
      return biome == null ? BiomeGenBase.desert : biome;
    }

    @Override
    public void playSoundAtEntity(Entity entity, String sound, float volume, float pitch) {}
  }

  public static final class TestTarget extends EntityLivingBase {
    boolean accept;
    float damage;

    TestTarget(World world) {
      super(world);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float value) {
      damage = value;
      return accept;
    }

    @Override
    public ItemStack[] getLastActiveItems() {
      return new ItemStack[5];
    }

    @Override
    public ItemStack getHeldItem() {
      return null;
    }

    @Override
    public ItemStack getEquipmentInSlot(int slot) {
      return null;
    }

    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack stack) {}
  }
}
