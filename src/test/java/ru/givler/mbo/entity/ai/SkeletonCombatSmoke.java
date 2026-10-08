package ru.givler.mbo.entity.ai;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import minefantasy.mf2.item.archery.ItemBowMF;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import ru.givler.mbo.core.SkeletonCombatTransformer;
import ru.givler.mbo.core.SkeletonPoseTransformer;
import ru.givler.mbo.entity.fauna.EntityMBOCod;
import ru.givler.mbo.handler.SkeletonCombatEvents;
import sun.misc.Unsafe;

public final class SkeletonCombatSmoke {
  public static void main(String[] args) throws Exception {
    verifyAsm();
    Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    TestWorld world =
        (TestWorld) ((Unsafe) unsafeField.get(null)).allocateInstance(TestWorld.class);
    world.isRemote = true;
    world.difficultySetting = EnumDifficulty.NORMAL;
    world.rand = new Random(1);
    world.floor = true;
    Field profiler = World.class.getDeclaredField("theProfiler");
    profiler.setAccessible(true);
    profiler.set(world, new Profiler());
    Field provider = World.class.getDeclaredField("provider");
    provider.setAccessible(true);
    provider.set(world, new WorldProviderSurface());
    Field listeners = World.class.getDeclaredField("worldAccesses");
    listeners.setAccessible(true);
    listeners.set(world, new ArrayList<Object>());
    TestSkeleton skeleton = new TestSkeleton(world);
    SkeletonCombatState.init(skeleton);
    verifyPose(skeleton);
    EntityMBOCod target = new EntityMBOCod(world);
    target.setPosition(0, 0, 8);
    skeleton.target = target;
    skeleton.held = new ItemStack(new ItemBow());
    skeleton.onGround = true;
    EntityAISkeletonBowAttack bow = new EntityAISkeletonBowAttack(skeleton, 1D, 20, 60, 15F);
    if (!bow.shouldExecute()) throw new AssertionError("Bow must enable ranged combat");
    bow.startExecuting();
    tick(skeleton, bow, 20);
    if (skeleton.shots != 0) throw new AssertionError("Bow fired without twenty ticks of drawing");
    tick(skeleton, bow, 1);
    if (skeleton.shots != 1 || skeleton.power != 1F)
      throw new AssertionError("Full draw must invoke the original projectile callback");
    tick(skeleton, bow, 59);
    if (skeleton.shots != 1) throw new AssertionError("Normal cooldown/draw is too short");
    tick(skeleton, bow, 1);
    if (skeleton.shots != 2) throw new AssertionError("Normal shot interval must be sixty ticks");
    skeleton.visible = false;
    tick(skeleton, bow, 100);
    if (skeleton.shots != 2) throw new AssertionError("Cannot shoot through cover");
    skeleton.visible = true;
    tick(skeleton, bow, 20);
    if (skeleton.shots != 2) throw new AssertionError("Long lost visibility must cancel drawing");
    tick(skeleton, bow, 1);
    if (skeleton.shots != 3)
      throw new AssertionError("Reacquired target should be shot after drawing");

    bow.resetTask();
    world.difficultySetting = EnumDifficulty.HARD;
    skeleton.shots = 0;
    tick(skeleton, bow, 61);
    if (skeleton.shots != 2) throw new AssertionError("Hard shot interval must be forty ticks");
    skeleton.held = new ItemStack(new Item());
    if (bow.shouldExecute() || bow.continueExecuting())
      throw new AssertionError("Switching to a melee weapon must stop the bow goal");
    bow.resetTask();
    target.setPosition(0, 0, 1);
    EntityAISkeletonMeleeAttack melee =
        new EntityAISkeletonMeleeAttack(skeleton, EntityLivingBase.class, 1.2D, false);
    if (!melee.shouldExecute())
      throw new AssertionError("Melee revenge must allow non-player targets");
    melee.startExecuting();
    for (int i = 0; i < 40; i++) melee.updateTask();
    if (skeleton.hits != 2) throw new AssertionError("Melee must preserve vanilla hit cooldown");
    skeleton.held = new ItemStack(new ItemBow() {});
    if (melee.shouldExecute() || !bow.shouldExecute())
      throw new AssertionError("Modded bow subclasses must switch to ranged combat");
    if (!ItemBow.class.isAssignableFrom(ItemBowMF.class))
      throw new AssertionError("MF2 bow must inherit the recognized bow class");
    skeleton.target = null;
    if (bow.shouldExecute() || bow.continueExecuting())
      throw new AssertionError("A lost target must stop ranged combat");
    bow.resetTask();
    skeleton.target = target;

    SkeletonMoveHelper.install(skeleton);
    SkeletonMoveHelper movement = (SkeletonMoveHelper) skeleton.getMoveHelper();
    skeleton.rotationYaw = 0;
    skeleton.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.4D);
    skeleton
        .getEntityAttribute(SharedMonsterAttributes.movementSpeed)
        .applyModifier(new AttributeModifier(UUID.randomUUID(), "slowness or armor", -.5D, 2));
    movement.strafe(.5F, .5F, target);
    movement.onUpdateMoveHelper();
    if (Math.abs(skeleton.getAIMoveSpeed() - .05F) > 1E-6 || skeleton.moveStrafing == 0F)
      throw new AssertionError("Strafing must respect the effective potion/armor speed");
    world.floor = false;
    movement.strafe(-.5F, .5F, target);
    movement.onUpdateMoveHelper();
    if (skeleton.moveForward != 0F
        || skeleton.moveStrafing != 0F
        || !movement.consumeBlockedStrafe())
      throw new AssertionError("Strafing must not step off an unsupported edge");
    world.floor = true;
    world.wall = true;
    movement.strafe(.5F, .5F, target);
    movement.onUpdateMoveHelper();
    if (skeleton.moveForward != 0F || skeleton.moveStrafing != 0F)
      throw new AssertionError("Strafing must not force movement through solid walls");
    world.wall = false;
    bow.resetTask();
    if (skeleton.moveStrafing != 0F)
      throw new AssertionError("Stopped goal left residual strafing");

    verifyModes(skeleton);
    System.out.println(
        "Skeleton modern bow timing, cover, melee, equipment, movement modifiers, safety and mode preservation passed");
  }

  private static void tick(TestSkeleton skeleton, EntityAISkeletonBowAttack bow, int ticks) {
    for (int i = 0; i < ticks; i++) {
      skeleton.getEntitySenses().clearSensingCache();
      bow.updateTask();
      skeleton.getMoveHelper().onUpdateMoveHelper();
    }
  }

  private static void verifyModes(TestSkeleton skeleton) {
    skeleton.tasks.taskEntries.clear();
    EntityAIBase panic =
        new EntityAIBase() {
          @Override
          public boolean shouldExecute() {
            return true;
          }
        };
    EntityAIArrowAttack specialized = new EntityAIArrowAttack(skeleton, 1D, 20, 60, 15F) {};
    skeleton.tasks.addTask(0, panic);
    skeleton.tasks.addTask(2, specialized);
    skeleton.tasks.addTask(1, new EntityAIArrowAttack(skeleton, 1D, 20, 60, 15F));
    SkeletonCombatEvents.refresh(skeleton);
    SkeletonCombatEvents.refresh(skeleton);
    int modern = 0;
    for (Object raw : skeleton.tasks.taskEntries) {
      EntityAITaskEntry entry = (EntityAITaskEntry) raw;
      if (entry.action instanceof EntityAISkeletonBowAttack) {
        modern++;
        if (entry.priority != 1) throw new AssertionError("Mode task priority changed");
      }
    }
    if (modern != 1 || skeleton.tasks.taskEntries.size() != 3)
      throw new AssertionError("Mode patch removed specialized tasks or duplicated combat");
    if (((EntityAITaskEntry) skeleton.tasks.taskEntries.get(1)).action != specialized)
      throw new AssertionError("Replacing a goal changed equal-priority task ordering");
    skeleton.tasks.addTask(1, new EntityAIAttackOnCollide(skeleton, 1.25D, true));
    SkeletonCombatEvents.refresh(skeleton);
    if (!(((EntityAITaskEntry) skeleton.tasks.taskEntries.get(3)).action
        instanceof EntityAISkeletonMeleeAttack))
      throw new AssertionError("MF2's native melee task must also use the modern hit delay");
    skeleton.tasks.taskEntries.clear();
    skeleton.tasks.addTask(0, panic);
    SkeletonCombatEvents.refresh(skeleton);
    if (skeleton.tasks.taskEntries.size() != 1)
      throw new AssertionError("Panic must not have removed combat tasks reinstalled");
  }

  private static void verifyAsm() throws Exception {
    String name = "net.minecraft.entity.monster.EntitySkeleton";
    byte[] original;
    try (InputStream input =
        SkeletonCombatSmoke.class.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      for (int read; (read = input.read(buffer)) >= 0; ) output.write(buffer, 0, read);
      original = output.toByteArray();
    }
    SkeletonCombatTransformer transformer = new SkeletonCombatTransformer();
    byte[] patched = transformer.transform(name, name, original);
    if (!Arrays.equals(patched, transformer.transform(name, name, patched)))
      throw new AssertionError("Skeleton transformation must be idempotent");
    StringWriter errors = new StringWriter();
    CheckClassAdapter.verify(new ClassReader(patched), false, new PrintWriter(errors));
    if (!errors.toString().isEmpty()) throw new AssertionError(errors.toString());
    ClassNode before = new ClassNode(), after = new ClassNode();
    new ClassReader(original).accept(before, 0);
    new ClassReader(patched).accept(after, 0);
    if (!before.name.equals(after.name) || before.fields.size() != after.fields.size())
      throw new AssertionError("Skeleton identity or synchronized fields changed");
    for (MethodNode method : before.methods) {
      if ("<init>".equals(method.name) || "setCombatTask".equals(method.name)) continue;
      for (MethodNode other : after.methods)
        if (method.name.equals(other.name)
            && method.desc.equals(other.desc)
            && !methodText(method).equals(methodText(other)))
          throw new AssertionError("Unrelated skeleton method changed: " + method.name);
    }
  }

  private static String methodText(MethodNode method) {
    Textifier text = new Textifier();
    method.accept(new TraceMethodVisitor(text));
    return text.getText().toString();
  }

  private static byte[] poseBytes(String name) throws Exception {
    try (InputStream input =
        SkeletonCombatSmoke.class.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      for (int read; (read = input.read(buffer)) >= 0; ) output.write(buffer, 0, read);
      return new SkeletonPoseTransformer().transform(name, name, output.toByteArray());
    }
  }

  private static void verifyPose(TestSkeleton skeleton) throws Exception {
    for (String name :
        new String[] {
          "net.minecraft.entity.monster.EntitySkeleton",
          "net.minecraft.client.model.ModelSkeleton",
          "net.minecraft.client.model.ModelZombie",
          "net.minecraft.client.model.ModelBiped"
        }) {
      StringWriter errors = new StringWriter();
      CheckClassAdapter.verify(new ClassReader(poseBytes(name)), false, new PrintWriter(errors));
      if (!errors.toString().isEmpty()) throw new AssertionError(errors.toString());
    }
    ClassLoader loader =
        new ClassLoader(SkeletonCombatSmoke.class.getClassLoader()) {
          @Override
          protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.equals("net.minecraft.client.model.ModelSkeleton")
                && !name.equals("net.minecraft.client.model.ModelZombie"))
              return super.loadClass(name, resolve);
            Class<?> type = findLoadedClass(name);
            if (type == null) {
              try {
                byte[] bytes = poseBytes(name);
                type = defineClass(name, bytes, 0, bytes.length);
              } catch (Exception failure) {
                throw new ClassNotFoundException(name, failure);
              }
            }
            if (resolve) resolveClass(type);
            return type;
          }
        };
    ModelBiped model =
        (ModelBiped)
            loader
                .loadClass("net.minecraft.client.model.ModelSkeleton")
                .getConstructor()
                .newInstance();
    skeleton.held = new ItemStack(new ItemBow());
    model.setRotationAngles(0, 0, 0, 0, 0, .0625F, skeleton);
    if (model.aimedBow
        || Math.abs(model.bipedRightArm.rotateAngleX) > .5F
        || Math.abs(model.bipedLeftArm.rotateAngleX) > .5F)
      throw new AssertionError("Idle skeleton must lower both arms and bow");
    SkeletonCombatState.update(skeleton, true);
    model.setRotationAngles(0, 0, 0, 0, 0, .0625F, skeleton);
    if (!model.aimedBow || model.bipedRightArm.rotateAngleX > -1F)
      throw new AssertionError("Combat must restore the aiming pose");
    skeleton.held = new ItemStack(new Item());
    model.setRotationAngles(0, 0, 0, 0, 0, .0625F, skeleton);
    if (model.aimedBow || model.bipedRightArm.rotateAngleX > -1F)
      throw new AssertionError("Melee combat must raise arms without bow aiming");
    SkeletonCombatState.update(skeleton, false);
    model.setRotationAngles(0, 0, 0, 0, 0, .0625F, skeleton);
    if (Math.abs(model.bipedRightArm.rotateAngleX) > .5F)
      throw new AssertionError("Ending combat must lower the melee weapon");
  }

  public static final class TestSkeleton extends EntitySkeleton {
    @Override
    public void swingItem() {}

    ItemStack held;
    EntityLivingBase target;
    boolean visible = true;
    int shots, hits;
    float power;
    TestNavigator navigation;

    public TestSkeleton(World world) {
      super(world);
      navigation = new TestNavigator(this, world);
    }

    @Override
    public ItemStack getHeldItem() {
      return held;
    }

    @Override
    public EntityLivingBase getAttackTarget() {
      return target;
    }

    @Override
    public boolean canEntityBeSeen(Entity entity) {
      return visible;
    }

    @Override
    public PathNavigate getNavigator() {
      return navigation == null ? super.getNavigator() : navigation;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float power) {
      shots++;
      this.power = power;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
      hits++;
      return true;
    }
  }

  public static final class TestNavigator extends PathNavigate {
    public TestNavigator(EntitySkeleton entity, World world) {
      super(entity, world);
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity target, double speed) {
      return true;
    }

    @Override
    public boolean tryMoveToXYZ(double x, double y, double z, double speed) {
      return true;
    }

    @Override
    public PathEntity getPathToEntityLiving(Entity target) {
      return new PathEntity(new PathPoint[] {new PathPoint(0, 0, 0), new PathPoint(0, 0, 1)});
    }

    @Override
    public boolean setPath(PathEntity path, double speed) {
      return true;
    }

    @Override
    public void clearPathEntity() {}

    @Override
    public boolean noPath() {
      return false;
    }
  }

  public static final class TestWorld extends WorldServer {
    private static final Block AIR = new Block(Material.air) {};
    private static final Block ROCK = new Block(Material.rock) {};
    boolean floor = true, wall;

    public TestWorld() {
      super(null, null, "skeleton-test", 0, null, null);
    }

    @Override
    public boolean blockExists(int x, int y, int z) {
      return true;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
      return y < 0 && floor ? ROCK : AIR;
    }

    @Override
    public List getCollidingBoundingBoxes(Entity entity, AxisAlignedBB box) {
      List<AxisAlignedBB> result = new ArrayList<AxisAlignedBB>();
      if (wall) result.add(box);
      return result;
    }
  }
}
