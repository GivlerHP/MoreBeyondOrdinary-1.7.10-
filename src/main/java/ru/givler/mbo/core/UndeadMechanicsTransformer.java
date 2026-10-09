package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class UndeadMechanicsTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK = "ru/givler/mbo/handler/UndeadEvents";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    boolean undead =
        "net.minecraft.entity.monster.EntityZombie".equals(transformedName)
            || "net.minecraft.entity.monster.EntitySkeleton".equals(transformedName);
    boolean arrow =
        "net.minecraft.entity.projectile.EntityArrow".equals(transformedName)
            || "minefantasy.mf2.entity.EntityArrowMF".equals(transformedName);
    boolean spawner = "net.minecraft.world.SpawnerAnimals".equals(transformedName);
    if (bytes == null || !(undead || arrow || spawner)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int changes = 0;
    for (MethodNode method : node.methods) {
      String mapped =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if ("net.minecraft.entity.projectile.EntityArrow".equals(transformedName)
          && (mapped.equals("onCollideWithPlayer") || mapped.equals("func_70100_b"))) {
        InsnList pickup = new InsnList();
        pickup.add(new VarInsnNode(ALOAD, 0));
        pickup.add(new VarInsnNode(ALOAD, 1));
        pickup.add(
            new MethodInsnNode(
                INVOKESTATIC,
                HOOK,
                "pickupArrow",
                "(Lnet/minecraft/entity/projectile/EntityArrow;Lnet/minecraft/entity/player/EntityPlayer;)Z",
                false));
        LabelNode vanilla = new LabelNode();
        pickup.add(new JumpInsnNode(IFEQ, vanilla));
        pickup.add(new InsnNode(RETURN));
        pickup.add(vanilla);
        pickup.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(pickup);
      }
      if (undead && !(mapped.equals("onLivingUpdate") || mapped.equals("func_70636_d"))) continue;
      if (arrow && !(mapped.equals("onUpdate") || mapped.equals("func_70071_h_"))) continue;
      for (AbstractInsnNode instruction : method.instructions.toArray()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        String target =
            FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner, call.name, call.desc);
        if (spawner && (target.equals("onSpawnWithEgg") || target.equals("func_110161_a"))) {
          method.instructions.set(
              call,
              new MethodInsnNode(
                  INVOKESTATIC,
                  HOOK,
                  "naturalSpawn",
                  "(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/entity/IEntityLivingData;)Lnet/minecraft/entity/IEntityLivingData;",
                  false));
          changes++;
        }
        if (undead && (target.equals("isDaytime") || target.equals("func_72935_r"))) {
          method.instructions.insertBefore(call, new VarInsnNode(ALOAD, 0));
          method.instructions.set(
              call,
              new MethodInsnNode(
                  INVOKESTATIC,
                  HOOK,
                  "burnsInDaylight",
                  "(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)Z",
                  false));
          changes++;
        } else if (arrow
            && (target.equals("attackEntityFrom") || target.equals("func_70097_a"))
            && call.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z")) {
          method.instructions.set(
              call,
              new MethodInsnNode(
                  INVOKESTATIC,
                  HOOK,
                  "arrowHit",
                  "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;F)Z",
                  false));
          changes++;
        }
      }
    }
    if (changes != (spawner ? 2 : 1))
      throw new IllegalStateException(
          "Undead mechanic anchor count " + changes + ": " + transformedName);
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    node.accept(writer);
    return writer.toByteArray();
  }
}
