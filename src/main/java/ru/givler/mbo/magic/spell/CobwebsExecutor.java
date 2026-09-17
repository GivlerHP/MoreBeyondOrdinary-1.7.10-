package ru.givler.mbo.magic.spell;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityTemporaryMagicBlock;

public final class CobwebsExecutor implements SpellExecutor {
  private static final int[][] OFFSETS = {
    {0, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
  };

  @Override
  public SpellResult cast(SpellContext context) {
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
                start.addVector(
                    look.xCoord * 12 * context.range(),
                    look.yCoord * 12 * context.range(),
                    look.zCoord * 12 * context.range()));
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
      return SpellResult.PASS;
    }
    int x = hit.blockX + net.minecraft.util.Facing.offsetsXForSide[hit.sideHit];
    int y = hit.blockY + net.minecraft.util.Facing.offsetsYForSide[hit.sideHit];
    int z = hit.blockZ + net.minecraft.util.Facing.offsetsZForSide[hit.sideHit];
    boolean placed = false;
    for (int[] offset : OFFSETS) {
      int bx = x + offset[0], by = y + offset[1], bz = z + offset[2];
      if (!context.world().isAirBlock(bx, by, bz)) continue;
      placed = true;
      if (!context.world().isRemote) {
        context.world().setBlock(bx, by, bz, BlockRegistry.TemporaryCobweb);
        if (context.world().getTileEntity(bx, by, bz) instanceof TileEntityTemporaryMagicBlock) {
          ((TileEntityTemporaryMagicBlock) context.world().getTileEntity(bx, by, bz))
              .setLifetime(Math.max(1, (int) (400 * context.duration())));
        }
      }
    }
    if (!placed) return SpellResult.PASS;
    context.caster().swingItem();
    context.world().playSoundAtEntity(context.caster(), "random.fizz", 1.0F, 1.0F);
    return SpellResult.SUCCESS;
  }
}
