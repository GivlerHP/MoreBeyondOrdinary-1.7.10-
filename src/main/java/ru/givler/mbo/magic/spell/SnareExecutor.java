package ru.givler.mbo.magic.spell;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityMagicSnare;

public final class SnareExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    double range = 10.0D * context.range();
    Vec3 start =
        Vec3.createVectorHelper(
            context.caster().posX,
            context.caster().posY + context.caster().getEyeHeight(),
            context.caster().posZ);
    Vec3 look = context.caster().getLookVec();
    MovingObjectPosition hit =
        context
            .world()
            .rayTraceBlocks(
                start,
                start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range));
    if (hit == null
        || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || hit.sideHit != 1
        || !context.world().isSideSolid(hit.blockX, hit.blockY, hit.blockZ, ForgeDirection.UP)
        || !context.world().isAirBlock(hit.blockX, hit.blockY + 1, hit.blockZ)) {
      return SpellResult.PASS;
    }

    int y = hit.blockY + 1;
    if (!context.world().isRemote) {
      context.world().setBlock(hit.blockX, y, hit.blockZ, BlockRegistry.MagicSnare);
      TileEntity tile = context.world().getTileEntity(hit.blockX, y, hit.blockZ);
      if (tile instanceof TileEntityMagicSnare) {
        ((TileEntityMagicSnare) tile).setCaster(context.caster());
      }
    } else {
      double dx = hit.blockX + 0.5D - context.caster().posX;
      double dy = y + 0.5D - start.yCoord;
      double dz = hit.blockZ + 0.5D - context.caster().posZ;
      for (int i = 1; i < 5; i++) {
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            context.world(),
            context.caster().posX + i * dx / 5.0D,
            start.yCoord + i * dy / 5.0D,
            context.caster().posZ + i * dz / 5.0D,
            0,
            -0.01D,
            0,
            new ParticleSettings(25, 0.25F, 0.35F, 0.0F, 0.7F, false));
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.LEAF,
            context.world(),
            context.caster().posX + i * dx / 5.0D,
            start.yCoord + i * dy / 5.0D,
            context.caster().posZ + i * dz / 5.0D,
            0,
            -0.01D,
            0,
            new ParticleSettings(
                40 + context.world().rand.nextInt(10), 1.0F, 1.0F, 1.0F, 1.4F, false));
      }
    }
    context.caster().swingItem();
    context
        .world()
        .playSoundAtEntity(
            context.caster(), "dig.grass", 1.0F, context.world().rand.nextFloat() * 0.4F + 1.2F);
    return SpellResult.SUCCESS;
  }
}
