package ru.givler.mbo.magic.spell;

import net.minecraft.block.Block;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class PhaseStepExecutor implements SpellExecutor {
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
                start, start.addVector(look.xCoord * 5, look.yCoord * 5, look.zCoord * 5));
    if (hit == null
        || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || hit.sideHit < 2) return SpellResult.PASS;
    ForgeDirection through = ForgeDirection.getOrientation(hit.sideHit).getOpposite();
    int maxThickness = 1 + Math.max(0, Math.round((context.range() - 1.0F) / 0.5F));
    int playerY = (int) Math.floor(context.caster().posY);
    for (int distance = 0; distance <= maxThickness; distance++) {
      int x = hit.blockX + through.offsetX * distance;
      int z = hit.blockZ + through.offsetZ * distance;
      Block feet = context.world().getBlock(x, playerY, z);
      Block head = context.world().getBlock(x, playerY + 1, z);
      if (feet.getBlockHardness(context.world(), x, playerY, z) < 0
          || head.getBlockHardness(context.world(), x, playerY + 1, z) < 0) return SpellResult.PASS;
      if (!feet.getMaterial().blocksMovement() && !head.getMaterial().blocksMovement()) {
        BlinkExecutor.portalParticles(context);
        if (!context.world().isRemote)
          context.caster().setPositionAndUpdate(x + 0.5D, context.caster().posY, z + 0.5D);
        context.world().playSoundAtEntity(context.caster(), "mob.endermen.portal", 1, 1);
        context.caster().swingItem();
        return SpellResult.SUCCESS;
      }
    }
    return SpellResult.PASS;
  }
}
