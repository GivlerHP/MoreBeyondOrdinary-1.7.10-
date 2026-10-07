package ru.givler.mbo.fauna;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import ru.givler.mbo.client.model.fauna.*;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.entity.fauna.*;
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
    EntityMBOTropicalFish source = new EntityMBOTropicalFish(null);
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
    frog.setGrowingAge(-24000);
    if (frog.isChild()) throw new AssertionError("Frog must develop through a tadpole");
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
    System.out.println(
        "Fauna persistence, bucket data, camel poses, panda genetics, goat jump trajectories and reference models passed.");
  }
}
