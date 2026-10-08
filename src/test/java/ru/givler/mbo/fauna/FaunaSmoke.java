package ru.givler.mbo.fauna;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import ru.givler.mbo.client.model.fauna.*;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.entity.fauna.*;
import ru.givler.mbo.integration.minefantasy2.FaunaItemRendering;
import sun.misc.Unsafe;

public final class FaunaSmoke {
  public static void main(String[] args) throws Exception {
    java.lang.reflect.Field home =
        cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
    home.setAccessible(true);
    home.set(null, new File("build/fauna-smoke-config"));
    java.lang.reflect.Field side =
        cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");
    side.setAccessible(true);
    side.set(null, cpw.mods.fml.relauncher.Side.SERVER);
    FaunaConfig.load(new File("build/fauna-smoke-config"));
    BufferedImage haft = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    for (int i = 0; i < 5; i++) haft.setRGB(6 + i, 12 - i, 0xffffffff);
    float[] grip = FaunaItemRendering.gripFromImage(haft);
    if (Math.abs(grip[0] - 8.5 / 16) > .000001
        || Math.abs(grip[1] - 5.5 / 16) > .000001
        || Math.abs(grip[2] + 45) > .001)
      throw new AssertionError("MF2 mouth grip must follow the real haft pixels and diagonal");
    double angle = grip[2] * Math.PI / 180;
    if (Math.abs(Math.sin(angle) + Math.cos(angle)) > .000001)
      throw new AssertionError("Haft axis must lie across the mouth after rotation");
    final IIcon baseLayer = new TextureAtlasSprite("test:head") {};
    final IIcon haftLayer = new TextureAtlasSprite("test:haft") {};
    Item layeredTool =
        new Item() {
          @Override
          public boolean requiresMultipleRenderPasses() {
            return true;
          }

          @Override
          public IIcon getIcon(ItemStack stack, int pass) {
            return pass == 1 ? haftLayer : baseLayer;
          }

          @Override
          public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
            return baseLayer;
          }
        };
    ItemStack layeredStack = new ItemStack(layeredTool);
    if (FaunaItemRendering.spriteIcon(layeredStack, 1) != haftLayer)
      throw new AssertionError("Fox mouth must select the real stack-dependent haft layer");
    EntityMBOTropicalFish source = new EntityMBOTropicalFish(null);
    if (source.getItemIcon(layeredStack, 1) != baseLayer)
      throw new AssertionError(
          "Regression fixture must reproduce vanilla's incorrect layer selection");
    source.setVariant(1 | 5 << 8 | 14 << 16 | 3 << 24);
    source.setHealth(1.5F);
    source.setCustomNameTag("Named fish");
    NBTTagCompound bucket = source.bucketData();
    EntityMBOTropicalFish released = new EntityMBOTropicalFish(null);
    released.readBucketData(bucket);
    if (!released.fromBucket()
        || released.variant() != source.variant()
        || released.getHealth() != 1.5F
        || !released.getCustomNameTag().equals("Named fish"))
      throw new AssertionError("Bucket lost variant/health/name/persistence");
    NBTTagCompound saved = new NBTTagCompound();
    released.writeEntityToNBT(saved);
    EntityMBOTropicalFish reloaded = new EntityMBOTropicalFish(null);
    reloaded.readEntityFromNBT(saved);
    if (!reloaded.fromBucket() || reloaded.variant() != source.variant())
      throw new AssertionError("Save lost fish state");
    EntityMBOPufferfish puffer = new EntityMBOPufferfish(null);
    saved = new NBTTagCompound();
    saved.setByte("PuffState", (byte) 127);
    puffer.readEntityFromNBT(saved);
    if (puffer.puff() != 2) throw new AssertionError("Invalid puff state not clamped");
    new ModelMBOCod(0);
    new ModelMBOSalmon(0);
    new ModelMBOTropicalFishSmall(0);
    new ModelMBOTropicalFishLarge(.008F);
    new ModelMBOPufferfishSmall(0);
    new ModelMBOPufferfishMid(0);
    new ModelMBOPufferfishBig(0);
    EntityMBOPolarBear bear = new EntityMBOPolarBear(null);
    saved = new NBTTagCompound();
    saved.setInteger("AngerTime", 600);
    saved.setString("AngryAt", "00000000-0000-0000-0000-000000000001");
    bear.readEntityFromNBT(saved);
    NBTTagCompound roundTrip = new NBTTagCompound();
    bear.writeEntityToNBT(roundTrip);
    if (roundTrip.getInteger("AngerTime") != 600
        || !roundTrip.getString("AngryAt").equals(saved.getString("AngryAt")))
      throw new AssertionError("Bear lost persistent anger");
    if (bear.isBreedingItem(new net.minecraft.item.ItemStack(net.minecraft.init.Items.fish)))
      throw new AssertionError("Polar bears must not breed using fish");
    new ModelMBOPolarBear(false);
    new ModelMBOPolarBear(true);
    EntityMBOTurtle turtle = new EntityMBOTurtle(null);
    saved = new NBTTagCompound();
    saved.setInteger("HomeX", -123);
    saved.setInteger("HomeY", 63);
    saved.setInteger("HomeZ", 456);
    saved.setBoolean("HomeSet", true);
    saved.setBoolean("HasEgg", true);
    saved.setInteger("Age", -12000);
    turtle.readEntityFromNBT(saved);
    roundTrip = new NBTTagCompound();
    turtle.writeEntityToNBT(roundTrip);
    if (!turtle.hasEgg()
        || !roundTrip.getBoolean("HomeSet")
        || roundTrip.getInteger("HomeX") != -123
        || roundTrip.getInteger("HomeY") != 63
        || roundTrip.getInteger("HomeZ") != 456
        || !turtle.isChild()) throw new AssertionError("Turtle lost home, egg or age");
    for (int eggs = 1; eggs <= 4; eggs++)
      for (int stage = 0; stage <= 2; stage++) {
        int metadata = (eggs - 1) | (stage << 2);
        if (ru.givler.mbo.block.fauna.BlockTurtleEgg.count(metadata) != eggs
            || ru.givler.mbo.block.fauna.BlockTurtleEgg.stage(metadata) != stage)
          throw new AssertionError("Clutch count and hatch stage overlap");
      }
    new ModelMBOAdultTurtle();
    new ModelMBOBabyTurtle();
    EntityMBOTadpole tadpole = new EntityMBOTadpole(null);
    tadpole.setAge(12345);
    tadpole.setHealth(2.5F);
    tadpole.setCustomNameTag("Growing tadpole");
    EntityMBOTadpole releasedTadpole = new EntityMBOTadpole(null);
    releasedTadpole.readBucketData(tadpole.bucketData());
    if (releasedTadpole.age() != 12345
        || releasedTadpole.getHealth() != 2.5F
        || !releasedTadpole.fromBucket()
        || !releasedTadpole.getCustomNameTag().equals("Growing tadpole"))
      throw new AssertionError("Tadpole bucket lost state");
    saved = new NBTTagCompound();
    releasedTadpole.writeEntityToNBT(saved);
    EntityMBOTadpole loadedTadpole = new EntityMBOTadpole(null);
    loadedTadpole.readEntityFromNBT(saved);
    if (loadedTadpole.age() != 12345) throw new AssertionError("Tadpole age lost on save");
    loadedTadpole.setAge(Integer.MAX_VALUE);
    if (loadedTadpole.age() != EntityMBOTadpole.GROWTH_TICKS)
      throw new AssertionError("Invalid growth age not clamped");
    EntityMBOFrog frog = new EntityMBOFrog(null);
    saved = new NBTTagCompound();
    saved.setInteger("Variant", 2);
    saved.setBoolean("Pregnant", true);
    frog.readEntityFromNBT(saved);
    roundTrip = new NBTTagCompound();
    frog.writeEntityToNBT(roundTrip);
    if (frog.variant() != 2 || !frog.pregnant())
      throw new AssertionError("Frog variant/pregnancy lost");
    for (int variant = 0; variant < 3; variant++) {
      saved.setInteger("Variant", variant);
      frog.readEntityFromNBT(saved);
      frog.onSpawnWithEgg(null);
      if (frog.variant() != variant)
        throw new AssertionError("Explicit frog variant must survive spawn initialization");
    }
    frog.setGrowingAge(-24000);
    if (frog.isChild()) throw new AssertionError("Frog must develop through a tadpole");
    for (int count = 1; count <= 4; count++)
      if (ru.givler.mbo.client.render.fauna.RenderTurtleEgg.modelEggCount(count) != count)
        throw new AssertionError("Turtle egg model must contain the matching original cuboids");
    ModelMBOFrog frogModel = new ModelMBOFrog(false);
    new ModelMBOFrog(true);
    if (frogModel.animationCount() != 6)
      throw new AssertionError("Reference frog animations missing");
    if (ModelMBOFrog.cubic(3, 4, 7, 9, 0) != 4 || ModelMBOFrog.cubic(3, 4, 7, 9, 1) != 7)
      throw new AssertionError("Animation interpolation endpoints incorrect");
    EntityMBOGoat goat = new EntityMBOGoat(null);
    saved = new NBTTagCompound();
    saved.setBoolean("IsScreamingGoat", true);
    saved.setBoolean("HasLeftHorn", false);
    saved.setBoolean("HasRightHorn", true);
    saved.setInteger("RamCooldown", 200);
    saved.setInteger("LongJumpCooldown", 800);
    goat.readEntityFromNBT(saved);
    roundTrip = new NBTTagCompound();
    goat.writeEntityToNBT(roundTrip);
    if (!goat.screaming()
        || goat.leftHorn()
        || !goat.rightHorn()
        || roundTrip.getInteger("RamCooldown") != 200
        || roundTrip.getInteger("LongJumpCooldown") != 800)
      throw new AssertionError("Goat lost horns, screaming variant or cooldowns");
    goat.readEntityFromNBT(new NBTTagCompound());
    if (!goat.leftHorn() || !goat.rightHorn())
      throw new AssertionError("Missing horn tags must default to intact");
    for (int ticks = 12; ticks <= 25; ticks++)
      for (int rise = -2; rise <= 4; rise++) {
        double velocity = EntityMBOGoat.jumpVelocity(rise, ticks), height = 0;
        for (int i = 0; i < ticks; i++) {
          height += velocity;
          velocity = (velocity - .08) * .98;
        }
        if (Math.abs(height - rise) > .000001)
          throw new AssertionError("Jump solver missed landing height");
      }
    for (double distance : new double[] {2, 4, 5.9}) {
      int ticks = 14;
      double horizontal = distance / ((1 - Math.pow(.91, ticks)) / .09);
      double vertical = EntityMBOGoat.jumpVelocity(0, ticks), x = 0, y = 0, apex = 0;
      for (int tick = 0; tick < ticks; tick++) {
        x += horizontal;
        y += vertical;
        apex = Math.max(apex, y);
        horizontal *= .91;
        vertical = (vertical - .08) * .98;
      }
      if (Math.abs(x - distance) > .000001 || Math.abs(y) > .000001 || apex > 2)
        throw new AssertionError("Fox pounce must land near prey without an excessive arc");
    }
    for (int main = 0; main < 7; main++)
      for (int hidden = 0; hidden < 7; hidden++) {
        EntityMBOPanda panda = new EntityMBOPanda(null);
        panda.genes(main, hidden);
        saved = new NBTTagCompound();
        panda.writeEntityToNBT(saved);
        EntityMBOPanda loaded = new EntityMBOPanda(null);
        loaded.readEntityFromNBT(saved);
        int expected = (main == 4 || main == 5) && main != hidden ? 0 : main;
        if (loaded.mainGene() != main
            || loaded.hiddenGene() != hidden
            || loaded.variant() != expected)
          throw new AssertionError("Panda inheritance/save phenotype mismatch");
        if (loaded.getMaxHealth() != (expected == 5 ? 10 : 20))
          throw new AssertionError("Weak panda health mismatch");
      }
    new ModelMBOLandAnimal("goat", false);
    new ModelMBOLandAnimal("goat", true);
    new ModelMBOLandAnimal("panda", false);
    new ModelMBOLandAnimal("panda", true);
    EntityMBOFox fox = new EntityMBOFox(null);
    saved = new NBTTagCompound();
    saved.setString("Type", "snow");
    saved.setBoolean("Sleeping", true);
    saved.setString("Trusted1", "00000000-0000-0000-0000-000000000001");
    saved.setString("Trusted2", "00000000-0000-0000-0000-000000000002");
    saved.setInteger("EatingTicks", 550);
    fox.readEntityFromNBT(saved);
    roundTrip = new NBTTagCompound();
    fox.writeEntityToNBT(roundTrip);
    if (!fox.snowy()
        || fox.state() != 1
        || !fox.trusts(java.util.UUID.fromString(saved.getString("Trusted1")))
        || !fox.trusts(java.util.UUID.fromString(saved.getString("Trusted2")))
        || roundTrip.getInteger("EatingTicks") != 550)
      throw new AssertionError("Fox lost variant, sleep, trust or food timer");
    EntityMBOFox babyFox = (EntityMBOFox) fox.createChild(fox);
    if (!babyFox.snowy()) throw new AssertionError("Two snow foxes must produce a snow fox");
    saved.setString("Trusted1", "invalid");
    fox.readEntityFromNBT(saved);
    new ModelMBOLandAnimal("fox", false);
    if (new ModelMBOLandAnimal("fox", true).walkChannels() != 18)
      throw new AssertionError("Cub reference walk channels missing");
    EntityMBOAxolotl axolotl = new EntityMBOAxolotl(null);
    axolotl.variant(4);
    axolotl.setGrowingAge(-12345);
    axolotl.setHealth(6);
    axolotl.setCustomNameTag("Bucket axolotl");
    EntityMBOAxolotl releasedAxolotl = new EntityMBOAxolotl(null);
    releasedAxolotl.readBucketData(axolotl.bucketData());
    if (releasedAxolotl.variant() != 4
        || !releasedAxolotl.fromBucket()
        || releasedAxolotl.getGrowingAge() != -12345
        || releasedAxolotl.getHealth() != 6
        || !releasedAxolotl.getCustomNameTag().equals("Bucket axolotl"))
      throw new AssertionError("Axolotl bucket lost variant, age, health, name or persistence");
    saved = releasedAxolotl.bucketData();
    saved.setInteger("PlayDeadTicks", 200);
    saved.setInteger("HuntingCooldown", 2400);
    releasedAxolotl.readEntityFromNBT(saved);
    roundTrip = releasedAxolotl.bucketData();
    if (releasedAxolotl.deadTicks() != 200 || roundTrip.getInteger("HuntingCooldown") != 2400)
      throw new AssertionError("Axolotl lost behavior cooldowns");
    releasedAxolotl.variant(100);
    if (releasedAxolotl.variant() != 4)
      throw new AssertionError("Invalid axolotl variant not clamped");
    new ModelMBOAxolotl(false);
    if (new ModelMBOAxolotl(true).animationCount() != 8)
      throw new AssertionError("Axolotl baby reference animations missing");
    EntityMBOGlowSquid squid = new EntityMBOGlowSquid(null);
    saved = new NBTTagCompound();
    saved.setInteger("DarkTicksRemaining", 100);
    saved.setInteger("Age", -12000);
    squid.readEntityFromNBT(saved);
    roundTrip = new NBTTagCompound();
    squid.writeEntityToNBT(roundTrip);
    if (!squid.baby() || squid.darkTicks() != 100 || roundTrip.getInteger("Age") != -12000)
      throw new AssertionError("Glow squid lost darkness/baby age");
    new ModelMBOGlowSquid(false);
    new ModelMBOGlowSquid(true);
    new ModelMBOGuardian();
    EntityMBOGuardian guardian = new EntityMBOGuardian(null);
    if (guardian.getMaxHealth() != 30 || guardian.beamTarget() != 0 || guardian.beamCharge() != 0)
      throw new AssertionError("Guardian attributes/beam initialization incorrect");
    EntityMBOElderGuardian elder = new EntityMBOElderGuardian(null);
    ru.givler.mbo.entity.boat.EntityMBOBoat sealedBoat =
        new ru.givler.mbo.entity.boat.EntityMBOBoat(null);
    if (elder.getMaxHealth() != 80 || elder.getAttackDuration() != 60 || elder.width < 1.99F)
      throw new AssertionError("Elder attributes/size/beam duration incorrect");
    elder.setPosition(0, 0, 0);
    sealedBoat.setPosition(10, 0, 0);
    if (elder.blocksBoat(sealedBoat)) throw new AssertionError("Seal starts before apparition");
    Field seal = EntityMBOElderGuardian.class.getDeclaredField("boatSealActive");
    seal.setAccessible(true);
    Field countdown = EntityMBOElderGuardian.class.getDeclaredField("sealCountdown");
    countdown.setAccessible(true);
    countdown.setInt(elder, EntityMBOElderGuardian.APPARITION_TICKS);
    Method advanceSeal = EntityMBOElderGuardian.class.getDeclaredMethod("advanceBoatSeal");
    advanceSeal.setAccessible(true);
    for (int i = 0; i < EntityMBOElderGuardian.APPARITION_TICKS - 1; i++) advanceSeal.invoke(elder);
    if (elder.blocksBoat(sealedBoat)) throw new AssertionError("Seal precedes end of apparition");
    advanceSeal.invoke(elder);
    if (!elder.blocksBoat(sealedBoat))
      throw new AssertionError("Active elder must seal nearby boats");
    sealedBoat.setPosition(50.01, 0, 0);
    if (elder.blocksBoat(sealedBoat)) throw new AssertionError("Boat outside sphere must be free");
    saved = new NBTTagCompound();
    elder.writeEntityToNBT(saved);
    EntityMBOElderGuardian loadedElder = new EntityMBOElderGuardian(null);
    loadedElder.readEntityFromNBT(saved);
    sealedBoat.setPosition(10, 0, 0);
    if (!loadedElder.blocksBoat(sealedBoat)) throw new AssertionError("Reload lost active seal");
    loadedElder.setHealth(0);
    if (loadedElder.blocksBoat(sealedBoat))
      throw new AssertionError("Dead elder still seals boats");
    sealedBoat.getDataWatcher().updateObject(26, Byte.valueOf((byte) 1));
    sealedBoat.motionX = 1;
    sealedBoat.motionY = .2;
    sealedBoat.motionZ = 1;
    Method boatControl = sealedBoat.getClass().getDeclaredMethod("controlBoat");
    boatControl.setAccessible(true);
    boatControl.invoke(sealedBoat);
    if (sealedBoat.motionX != 0 || sealedBoat.motionZ != 0 || sealedBoat.motionY != .2)
      throw new AssertionError("Seal must stop horizontal movement while preserving buoyancy");
    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    WorldServer camelWorld =
        (WorldServer) ((Unsafe) unsafeField.get(null)).allocateInstance(WorldServer.class);
    camelWorld.isRemote = true;
    camelWorld.rand = new Random(1);
    Field provider = World.class.getDeclaredField("provider");
    provider.setAccessible(true);
    provider.set(camelWorld, new WorldProviderSurface());
    Field listeners = World.class.getDeclaredField("worldAccesses");
    listeners.setAccessible(true);
    listeners.set(camelWorld, new ArrayList<Object>());
    EntityMBOCamel camel = new EntityMBOCamel(camelWorld);
    if (camel.getMaxHealth() != 32 || camel.getHorseJumpStrength() != .42 || !camel.isTame())
      throw new AssertionError("Camel attributes/taming incorrect");
    camel.setHorseSaddled(true);
    camel.onGround = true;
    camel.rotationYaw = 0;
    camel.motionZ = .5;
    camelWorld.isRemote = false;
    camel.setJumpPower(90);
    camelWorld.isRemote = true;
    if (Math.abs(camel.motionZ - 1.0) > .000001 || Math.abs(camel.motionY - .42) > .000001)
      throw new AssertionError("Camel dash must not stack onto existing forward speed");
    camel.getDataWatcher().updateObject(28, Integer.valueOf(0));
    frog.worldObj = camelWorld;
    frog.setPosition(0, 3, 0);
    frog.motionX = .2;
    frog.motionZ = -.2;
    net.minecraft.entity.monster.EntitySlime tonguePrey =
        new net.minecraft.entity.monster.EntitySlime(null);
    tonguePrey.setPosition(1, 3, 0);
    tonguePrey.motionX = .03;
    tonguePrey.motionY = .04;
    Method tongueAttack =
        EntityMBOFrog.class.getDeclaredMethod("beginTongueAttack", EntitySlime.class);
    tongueAttack.setAccessible(true);
    tongueAttack.invoke(frog, tonguePrey);
    if (frog.posX != 0
        || frog.posY != 3
        || frog.posZ != 0
        || frog.motionX != 0
        || frog.motionZ != 0)
      throw new AssertionError("Tongue attack must keep the frog stationary");
    if (tonguePrey.motionX != .03 || tonguePrey.motionY != .04 || tonguePrey.motionZ != 0)
      throw new AssertionError("Tongue attack must not launch prey into the frog");
    camel.setSitting(true);
    camel.setHorseSaddled(true);
    saved = new NBTTagCompound();
    camel.writeEntityToNBT(saved);
    EntityMBOCamel loadedCamel = new EntityMBOCamel(camelWorld);
    loadedCamel.readEntityFromNBT(saved);
    if (!loadedCamel.sitting()
        || !loadedCamel.isHorseSaddled()
        || loadedCamel.poseTicks() != 0
        || Math.abs(loadedCamel.height - .945F) > .001)
      throw new AssertionError(
          "Camel save state: sitting="
              + loadedCamel.sitting()
              + " saddle="
              + loadedCamel.isHorseSaddled()
              + " pose="
              + loadedCamel.poseTicks()
              + " height="
              + loadedCamel.height
              + " tags="
              + saved);
    loadedCamel.setSitting(false);
    if (Math.abs(loadedCamel.height - 2.375F) > .001 || !loadedCamel.transitioning())
      throw new AssertionError("Camel standing height/transition incorrect");
    loadedCamel.beginDash();
    saved = new NBTTagCompound();
    loadedCamel.writeEntityToNBT(saved);
    camel.readEntityFromNBT(saved);
    if (camel.dashCooldown() != 55) throw new AssertionError("Camel dash cooldown lost on save");
    camel.setPosition(10, 3, 10);
    camel.rotationYaw = 90;
    EntityMBOCod front = new EntityMBOCod(null), rear = new EntityMBOCod(null);
    camel.positionPassenger(front, true);
    camel.positionPassenger(rear, false);
    if (Math.abs(front.posX - 9.5) > .00001
        || Math.abs(rear.posX - 10.7) > .00001
        || Math.abs(front.posZ - rear.posZ) > .00001)
      throw new AssertionError("Camel passenger offsets do not follow its heading");
    if (new ModelMBOCamel(false).animationCount() != 6
        || new ModelMBOCamel(true).animationCount() != 6)
      throw new AssertionError("Camel reference animations missing");
    ModelMBOCamel babyCamel = new ModelMBOCamel(true);
    Method apply =
        ModelMBOFrog.class.getDeclaredMethod("apply", String.class, float.class, float.class);
    apply.setAccessible(true);
    apply.invoke(babyCamel, "CAMEL_BABY_SIT_POSE", 1.25F, 1F);
    Field nodes = ModelMBOFrog.class.getDeclaredField("nodes");
    nodes.setAccessible(true);
    for (Object node : ((Map<?, ?>) nodes.get(babyCamel)).values())
      for (String component : new String[] {"position", "rotation", "scale"}) {
        Field vector = node.getClass().getDeclaredField(component);
        vector.setAccessible(true);
        for (float value : (float[]) vector.get(node))
          if (Float.isNaN(value) || Float.isInfinite(value))
            throw new AssertionError("Zero-duration sitting animation produced invalid geometry");
      }
    if (Math.abs(AquaticMovement.approachYaw(179F, -179F, 10F) - 181F) > .001F
        || Math.abs(AquaticMovement.approachYaw(0F, 90F, 10F) - 10F) > .001F
        || Math.abs(AquaticMovement.approachYaw(0F, 5F, 10F) - 5F) > .001F)
      throw new AssertionError("Aquatic turning must wrap, limit speed and avoid overshooting");
    Field inWater = Entity.class.getDeclaredField("inWater");
    inWater.setAccessible(true);
    for (EntityLivingBase animal : new EntityLivingBase[] {source, axolotl, guardian, squid}) {
      animal.worldObj = camelWorld;
      inWater.setBoolean(animal, true);
      animal.setPosition(7, 4, 9);
      animal.motionX = .2;
      animal.motionY = .1;
      animal.motionZ = .3;
      animal.moveEntityWithHeading(0, 0);
      if (animal.posX != 7
          || animal.posY != 4
          || animal.posZ != 9
          || animal.motionX != .2
          || animal.motionY != .1
          || animal.motionZ != .3)
        throw new AssertionError("Client water physics must not run over tracked interpolation");
    }
    AquaticWorld ramWorld =
        (AquaticWorld) ((Unsafe) unsafeField.get(null)).allocateInstance(AquaticWorld.class);
    ramWorld.isRemote = true;
    ramWorld.rand = new Random(2);
    provider.set(ramWorld, new WorldProviderSurface());
    listeners.set(ramWorld, new ArrayList<Object>());
    goat.worldObj = ramWorld;
    goat.setPosition(0, 3, 0);
    RamVictim victim = new RamVictim();
    victim.setPosition(1.2, 3, 0);
    ramWorld.ramVictim = victim;
    Field ramField = EntityMBOGoat.class.getDeclaredField("ram");
    ramField.setAccessible(true);
    Object ram = ramField.get(goat);
    for (String fieldName : new String[] {"target", "stage", "dx", "dz"}) {
      Field field = ram.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      if (fieldName.equals("target")) field.set(ram, victim);
      else if (fieldName.equals("stage")) field.setInt(ram, 2);
      else field.setDouble(ram, fieldName.equals("dx") ? 1 : 0);
    }
    Method ramUpdate = ram.getClass().getDeclaredMethod("updateTask");
    ramUpdate.setAccessible(true);
    ramUpdate.invoke(ram);
    if (!victim.hit || victim.motionX < 2 || victim.motionY <= 0 || !victim.velocityChanged)
      throw new AssertionError("Goat ram must hit and push a victim along the next movement step");
    AquaticWorld waterWorld =
        (AquaticWorld) ((Unsafe) unsafeField.get(null)).allocateInstance(AquaticWorld.class);
    waterWorld.isRemote = true;
    provider.set(waterWorld, new WorldProviderSurface());
    FaunaHabitatSmoke.check(waterWorld);
    FaunaSpawnSmoke.check(waterWorld);
    waterWorld.waterPresent = true;
    for (EntityLivingBase animal :
        new EntityLivingBase[] {source, axolotl, guardian, squid, turtle}) {
      animal.worldObj = waterWorld;
      animal.fallDistance = 4;
      for (int tick = 0; tick < 20; tick++)
        if (!animal.handleWaterMovement() || !animal.isInWater())
          throw new AssertionError("Small aquatic mob incorrectly left the water");
      if (animal.fallDistance != 0) throw new AssertionError("Water must reset falling distance");
    }
    if (waterWorld.particles != 0)
      throw new AssertionError("Repeated water checks must not spawn splash/bubble bursts");
    for (double height : new double[] {.175, .2, .3, .4, .42, .8, .85}) {
      AxisAlignedBB box =
          AquaticMovement.waterBox(AxisAlignedBB.getBoundingBox(0, 4, 0, .5, 4 + height, .5));
      if (box.minY >= box.maxY || Math.abs((box.maxY - box.minY) - (height - .002)) > .000001)
        throw new AssertionError("Water probe inverted a small hitbox");
    }
    waterWorld.waterPresent = false;
    if (source.handleWaterMovement() || source.isInWater())
      throw new AssertionError("Dry fish must clear its water state");
    source.rotationYaw = 0;
    source.motionX = 0;
    source.motionZ = .1;
    for (int tick = 0; tick < 70; tick++) {
      float previousYaw = source.rotationYaw;
      AquaticMovement.swim(source, 0, -1, .012, 3F);
      double radians = source.rotationYaw * Math.PI / 180;
      double forward = -Math.sin(radians) * source.motionX + Math.cos(radians) * source.motionZ;
      double sideways = Math.cos(radians) * source.motionX + Math.sin(radians) * source.motionZ;
      if (forward < -.000001 || Math.abs(sideways) > .000001)
        throw new AssertionError("Aquatic steering must not propel backwards or sideways");
      if (Math.abs(source.rotationYaw - previousYaw) > 3.001)
        throw new AssertionError("Changing the swim goal must not snap the body");
      source.motionX *= .9;
      source.motionZ *= .9;
    }
    if (Math.abs(Math.abs(source.rotationYaw) - 180) > .001 || source.motionZ >= 0)
      throw new AssertionError("Aquatic mob must complete a reverse turn and resume swimming");
    waterWorld.waterPresent = true;
    waterWorld.obstacle = AxisAlignedBB.getBoundingBox(1, 3, 0, 2, 4, 2);
    source.setPosition(.8, 3.2, .8);
    Method recover = EntityMBOFish.class.getDeclaredMethod("recoverFromObstacle");
    recover.setAccessible(true);
    recover.invoke(source);
    Field escapeGoal = EntityMBOFish.class.getDeclaredField("goalX");
    escapeGoal.setAccessible(true);
    if (escapeGoal.getDouble(source) >= source.posX)
      throw new AssertionError("Blocked fish must choose clear water away from the bank");
    Field escapeTimer = EntityMBOFish.class.getDeclaredField("escapeTicks");
    escapeTimer.setAccessible(true);
    if (escapeTimer.getInt(source) != 40)
      throw new AssertionError("Escape goal must persist while the fish turns away");
    waterWorld.obstacle = null;
    AquaticMovement.ClientTurn turn = new AquaticMovement.ClientTurn();
    source.rotationYaw = 179;
    turn.update(source);
    source.rotationYaw = -179;
    source.renderYawOffset = 0;
    turn.update(source);
    if (Math.abs(source.renderYawOffset - 181) > .001
        || Math.abs(source.prevRenderYawOffset - 179) > .001)
      throw new AssertionError("Client body must interpolate across angle wrapping");
    source.rotationYaw = 90;
    for (int tick = 0; tick < 40; tick++) {
      source.renderYawOffset = -90; // Simulate vanilla's body helper changing the visual angle.
      turn.update(source);
      if (Math.abs(source.renderYawOffset - source.prevRenderYawOffset) > 3.001)
        throw new AssertionError("Client aquatic body snapped during a turn");
    }
    if (Math.abs(source.renderYawOffset - 90) > .001)
      throw new AssertionError("Client body failed to reach its final angle");
    System.out.println(
        "Fauna persistence, bucket data, camel poses, panda genetics, goat jump trajectories and reference models passed.");
  }

  public static final class RamVictim extends EntitySlime {
    boolean hit;

    public RamVictim() {
      super(null);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
      hit = true;
      return true;
    }
  }

  public static final class AquaticWorld extends WorldServer {
    private static final Block TEST_AIR = new Block(Material.air) {};
    private static final Block TEST_WATER = new Block(Material.water) {};
    boolean waterPresent;
    boolean habitatFixture;
    @Override
    public int getHeightValue(int x, int z) {
      return 63;
    }

    List<Entity> spawnedFauna;

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB box) {
      return true;
    }

    @Override
    public boolean spawnEntityInWorld(Entity entity) {
      if (spawnedFauna == null) return false;
      spawnedFauna.add(entity);
      return true;
    }

    int roofY = -1000, floorY = -60, habitatLight;
    BiomeGenBase habitatBiome;
    private static final Block TEST_ROCK = new Block(Material.rock) {};
    private static final Block TEST_GROUND = new Block(Material.ground) {};
    int particles;
    EntityLivingBase ramVictim;
    AxisAlignedBB obstacle;

    public AquaticWorld() {
      super(null, null, "fauna-test", 0, null, new Profiler());
    }

    @Override
    public List getEntitiesWithinAABB(Class type, AxisAlignedBB box) {
      List<EntityLivingBase> result = new ArrayList<EntityLivingBase>();
      if (ramVictim != null && box.intersectsWith(ramVictim.boundingBox)) result.add(ramVictim);
      return result;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
      if (habitatFixture) {
        if (y == roofY) return TEST_ROCK;
        if (y <= floorY) return TEST_GROUND;
        return y >= 63 ? TEST_AIR : TEST_WATER;
      }
      return waterPresent ? TEST_WATER : TEST_AIR;
    }

    @Override
    public int getFullBlockLightValue(int x, int y, int z) {
      return habitatLight;
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
      return 0;
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
      return getBlock(x, y, z).getMaterial() == Material.air;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
      return habitatBiome;
    }

    @Override
    public List getCollidingBoundingBoxes(Entity entity, AxisAlignedBB box) {
      List<AxisAlignedBB> result = new ArrayList<AxisAlignedBB>();
      if (obstacle != null && obstacle.intersectsWith(box)) result.add(obstacle);
      return result;
    }

    @Override
    public boolean blockExists(int x, int y, int z) {
      return true;
    }

    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB box, Material material, Entity animal) {
      if (box.minX >= box.maxX || box.minY >= box.maxY || box.minZ >= box.maxZ)
        throw new AssertionError("Inverted water probe");
      return waterPresent;
    }

    @Override
    public void spawnParticle(
        String name, double x, double y, double z, double mx, double my, double mz) {
      particles++;
    }
  }
}
