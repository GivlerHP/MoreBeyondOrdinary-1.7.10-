package ru.givler.mbo.magic.spell;

import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityTemporaryMagicBlock;

public final class LightExecutor implements SpellExecutor {
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
                start, start.addVector(look.xCoord * 4, look.yCoord * 4, look.zCoord * 4));
    int x, y, z;
    if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
      x = hit.blockX + Facing.offsetsXForSide[hit.sideHit];
      y = hit.blockY + Facing.offsetsYForSide[hit.sideHit];
      z = hit.blockZ + Facing.offsetsZForSide[hit.sideHit];
    } else {
      x = (int) Math.floor(start.xCoord + look.xCoord * 4);
      y = (int) Math.floor(start.yCoord + look.yCoord * 4);
      z = (int) Math.floor(start.zCoord + look.zCoord * 4);
    }
    if (!context.world().isAirBlock(x, y, z)) return SpellResult.PASS;
    if (!context.world().isRemote) {
      context.world().setBlock(x, y, z, BlockRegistry.TemporaryLight);
      if (context.world().getTileEntity(x, y, z) instanceof TileEntityTemporaryMagicBlock) {
        ((TileEntityTemporaryMagicBlock) context.world().getTileEntity(x, y, z))
            .setLifetime(Math.max(1, (int) (600 * context.duration())));
      }
    }
    context.caster().swingItem();
    context.world().playSoundAtEntity(context.caster(), "mbo:aura", 1.0F, 1.0F);
    return SpellResult.SUCCESS;
  }
}
