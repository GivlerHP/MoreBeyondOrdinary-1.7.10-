package ru.givler.mbo.magic.spell;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityTemporaryMagicBlock;

public final class WallOfFrostExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    Vec3 start =
        Vec3.createVectorHelper(
            context.caster().posX,
            context.caster().posY + context.caster().getEyeHeight(),
            context.caster().posZ);
    Vec3 look = context.caster().getLookVec();
    Vec3 end =
        start.addVector(
            look.xCoord * 10 * context.range(),
            look.yCoord * 10 * context.range(),
            look.zCoord * 10 * context.range());
    MovingObjectPosition hit = context.world().rayTraceBlocks(start, end);
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
      return SpellResult.PASS;
    }
    if (!context.world().isRemote) place(context, hit);
    else particles(context, look);
    if (context.ticksInUse() % 12 == 0) {
      if (context.ticksInUse() == 0)
        context.world().playSoundAtEntity(context.caster(), "mbo:ice", 0.5F, 1.0F);
      context.world().playSoundAtEntity(context.caster(), "mbo:frostray", 0.5F, 1.0F);
    }
    return SpellResult.SUCCESS;
  }

  private static void place(SpellContext context, MovingObjectPosition hit) {
    int x = hit.blockX + net.minecraft.util.Facing.offsetsXForSide[hit.sideHit];
    int y = hit.blockY + net.minecraft.util.Facing.offsetsYForSide[hit.sideHit];
    int z = hit.blockZ + net.minecraft.util.Facing.offsetsZForSide[hit.sideHit];
    int[][] offsets =
        Math.abs(context.caster().getLookVec().xCoord)
                > Math.abs(context.caster().getLookVec().zCoord)
            ? new int[][] {{0, 0, -1}, {0, 0, 0}, {0, 0, 1}}
            : new int[][] {{-1, 0, 0}, {0, 0, 0}, {1, 0, 0}};
    for (int[] offset : offsets)
      for (int height = 0; height < 3; height++) {
        int bx = x + offset[0], by = y + height, bz = z + offset[2];
        if (!context.world().getBlock(bx, by, bz).isReplaceable(context.world(), bx, by, bz))
          continue;
        context.world().setBlock(bx, by, bz, BlockRegistry.TemporaryFrost);
        if (context.world().getTileEntity(bx, by, bz) instanceof TileEntityTemporaryMagicBlock) {
          ((TileEntityTemporaryMagicBlock) context.world().getTileEntity(bx, by, bz))
              .setLifetime(Math.max(20, (int) (600 * context.duration())));
        }
      }
  }

  private static void particles(SpellContext context, Vec3 look) {
    for (int i = 0; i < 20; i++) {
      double distance = i / 2.0D;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          context.world(),
          context.caster().posX + look.xCoord * distance,
          context.caster().posY + context.caster().getEyeHeight() - 0.4D + look.yCoord * distance,
          context.caster().posZ + look.zCoord * distance,
          0,
          0,
          0,
          new ParticleSettings(
              8 + context.world().rand.nextInt(12),
              (i & 1) == 0 ? 0.4F : 1.0F,
              (i & 1) == 0 ? 0.6F : 1.0F,
              1.0F,
              0.8F,
              false));
    }
  }
}
