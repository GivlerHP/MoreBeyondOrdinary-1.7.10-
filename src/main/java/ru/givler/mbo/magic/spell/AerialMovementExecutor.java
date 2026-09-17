package ru.givler.mbo.magic.spell;

import net.minecraft.util.Vec3;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Shared continuous movement controller for glide, levitation and flight. */
public final class AerialMovementExecutor implements SpellExecutor {
  public enum Kind {
    GLIDE,
    LEVITATION,
    FLIGHT
  }

  private final Kind kind;

  public AerialMovementExecutor(Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    Vec3 look = context.caster().getLookVec();
    if (kind == Kind.GLIDE) glide(context, look);
    else if (kind == Kind.LEVITATION) levitate(context);
    else fly(context, look);
    if (context.world().isRemote) particles(context);
    if ((kind == Kind.GLIDE || kind == Kind.FLIGHT) && context.ticksInUse() % 24 == 0) {
      context.world().playSoundAtEntity(context.caster(), "mob.enderdragon.wings", 0.5F, 1);
    }
    return SpellResult.SUCCESS;
  }

  private static void glide(SpellContext context, Vec3 look) {
    if (context.caster().motionY < -0.1D && !context.caster().isInWater()) {
      context.caster().motionY = -0.1D;
      if (Math.abs(context.caster().motionX) < 0.4D && Math.abs(context.caster().motionZ) < 0.4D) {
        context.caster().addVelocity(look.xCoord / 8.0D, 0, look.zCoord / 8.0D);
      }
      context.caster().fallDistance = 0;
    }
  }

  private static void levitate(SpellContext context) {
    context.caster().fallDistance = 0;
    if (context.caster().motionY < 0.5D) context.caster().motionY += 0.1D;
  }

  private static void fly(SpellContext context, Vec3 look) {
    if (context.caster().isInWater()) return;
    if ((Math.abs(context.caster().motionX) < 0.6D
            || opposite(context.caster().motionX, look.xCoord))
        && (Math.abs(context.caster().motionZ) < 0.6D
            || opposite(context.caster().motionZ, look.zCoord))) {
      context.caster().addVelocity(look.xCoord / 20.0D, 0, look.zCoord / 20.0D);
    }
    if (Math.abs(context.caster().motionY) < 0.6D
        || opposite(context.caster().motionY, look.yCoord)) {
      context.caster().motionY += look.yCoord / 20.0D + 0.075D;
    }
    context.caster().fallDistance = 0;
  }

  private static boolean opposite(double velocity, double direction) {
    return Math.abs(direction) > 0.0001D && velocity / direction < 0;
  }

  private void particles(SpellContext context) {
    int count = kind == Kind.FLIGHT ? 2 : 1;
    for (int i = 0; i < count; i++) {
      float red = kind == Kind.LEVITATION ? 0.5F : kind == Kind.FLIGHT && i == 0 ? 0.8F : 1.0F;
      float green = 1.0F;
      float blue = kind == Kind.LEVITATION ? 0.7F : kind == Kind.FLIGHT && i == 0 ? 0.5F : 1.0F;
      double spread = kind == Kind.FLIGHT ? 2.0D : 0.5D;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          context.world(),
          context.caster().posX + (context.world().rand.nextDouble() - 0.5D) * spread,
          context.caster().posY + 0.3D + context.world().rand.nextDouble(),
          context.caster().posZ + (context.world().rand.nextDouble() - 0.5D) * spread,
          0,
          -0.1D,
          0,
          new ParticleSettings(15, red, green, blue, 0.8F, false));
    }
  }
}
