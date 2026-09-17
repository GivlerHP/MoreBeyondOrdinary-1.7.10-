package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** Shared channelled beam for flame ray, frost ray and firestorm. */
public final class ElementalRayExecutor implements SpellExecutor {
  public enum Kind {
    FLAME,
    FROST,
    FIRESTORM
  }

  private final Kind kind;

  public ElementalRayExecutor(Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target =
        SpellTargeting.livingInSight(context.caster(), 10.0D * context.range());
    if (target != null) affectTarget(context, target);
    else if (kind == Kind.FIRESTORM) igniteSurface(context);

    if (context.world().isRemote) drawBeam(context);
    int interval = kind == Kind.FROST ? 12 : 16;
    if (context.ticksInUse() % interval == 0) {
      if (context.ticksInUse() == 0) {
        if (kind == Kind.FROST)
          context.world().playSoundAtEntity(context.caster(), "mbo:ice", 0.5F, 1.0F);
        else
          context
              .world()
              .playAuxSFX(
                  1009,
                  (int) context.caster().posX,
                  (int) context.caster().posY,
                  (int) context.caster().posZ,
                  0);
      }
      context
          .world()
          .playSoundAtEntity(
              context.caster(), kind == Kind.FROST ? "mbo:frostray" : "mbo:flameray", 0.5F, 1.0F);
    }
    return SpellResult.SUCCESS;
  }

  private void affectTarget(SpellContext context, EntityLivingBase target) {
    if (context.world().isRemote) return;
    if (kind == Kind.FROST) {
      target.extinguish();
      if (target instanceof EntitySnowman) return;
      SyncedPotionEffects.apply(
          target,
          new PotionEffect(PotionRegistry.Frost.id, (int) (200 * context.duration()), 0, true));
      damageWithoutKnockback(
          context,
          target,
          (target instanceof EntityBlaze || target instanceof EntityMagmaCube ? 6.0F : 3.0F)
              * context.power(),
          false);
    } else {
      if (target.isImmuneToFire()) return;
      target.setFire(10);
      damageWithoutKnockback(
          context, target, (kind == Kind.FIRESTORM ? 6.0F : 3.0F) * context.power(), true);
    }
  }

  private static void damageWithoutKnockback(
      SpellContext context, EntityLivingBase target, float damage, boolean fire) {
    double x = target.motionX, y = target.motionY, z = target.motionZ;
    DamageSource source = DamageSource.causeIndirectMagicDamage(context.caster(), context.caster());
    if (fire) source.setFireDamage();
    target.attackEntityFrom(source, damage);
    target.motionX = x;
    target.motionY = y;
    target.motionZ = z;
  }

  private void igniteSurface(SpellContext context) {
    Vec3 start = eyes(context);
    Vec3 look = context.caster().getLookVec();
    MovingObjectPosition hit =
        context
            .world()
            .rayTraceBlocks(
                start,
                start.addVector(
                    look.xCoord * 10 * context.range(),
                    look.yCoord * 10 * context.range(),
                    look.zCoord * 10 * context.range()));
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
    ForgeDirection side = ForgeDirection.getOrientation(hit.sideHit);
    int x = hit.blockX + side.offsetX;
    int y = hit.blockY + side.offsetY;
    int z = hit.blockZ + side.offsetZ;
    if (!context.world().isRemote && context.world().isAirBlock(x, y, z)) {
      context.world().setBlock(x, y, z, Blocks.fire);
    }
  }

  private void drawBeam(SpellContext context) {
    Vec3 start = eyes(context);
    Vec3 look = context.caster().getLookVec();
    int count = kind == Kind.FIRESTORM ? 40 : 20;
    for (int i = 0; i < count; i++) {
      double spreadX =
          (context.world().rand.nextDouble() - 0.5D) * (kind == Kind.FIRESTORM ? 0.6D : 0.2D);
      double spreadY =
          (context.world().rand.nextDouble() - 0.5D) * (kind == Kind.FIRESTORM ? 0.4D : 0.2D);
      double spreadZ =
          (context.world().rand.nextDouble() - 0.5D) * (kind == Kind.FIRESTORM ? 0.6D : 0.2D);
      double x = start.xCoord + look.xCoord * i / (kind == Kind.FIRESTORM ? 2.0D : 2.0D) + spreadX;
      double y = start.yCoord - 0.4D + look.yCoord * i / 2.0D + spreadY;
      double z = start.zCoord + look.zCoord * i / 2.0D + spreadZ;
      if (kind == Kind.FROST) {
        spawnFrost(context, x, y, z, look, 0.4F, 0.6F, 1.0F);
        spawnFrost(context, x, y, z, look, 1.0F, 1.0F, 1.0F);
      } else {
        float size = kind == Kind.FIRESTORM ? 3.0F + context.world().rand.nextFloat() : 1.0F;
        for (int j = 0; j < 2; j++)
          MoreBeyondOrdinary.proxy.spawnParticle(
              EnumParticleType.MAGIC_FIRE,
              context.world(),
              x,
              y,
              z,
              look.xCoord * context.range(),
              look.yCoord * context.range(),
              look.zCoord * context.range(),
              new ParticleSettings(8, 1, 1, 1, size, false));
      }
    }
  }

  private static void spawnFrost(
      SpellContext context,
      double x,
      double y,
      double z,
      Vec3 look,
      float red,
      float green,
      float blue) {
    MoreBeyondOrdinary.proxy.spawnParticle(
        EnumParticleType.SPARKLE,
        context.world(),
        x,
        y,
        z,
        look.xCoord * context.range(),
        look.yCoord * context.range(),
        look.zCoord * context.range(),
        new ParticleSettings(8 + context.world().rand.nextInt(12), red, green, blue, 0.8F, false));
  }

  private static Vec3 eyes(SpellContext context) {
    return Vec3.createVectorHelper(
        context.caster().posX,
        context.caster().posY + context.caster().getEyeHeight(),
        context.caster().posZ);
  }
}
