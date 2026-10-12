package ru.givler.mbo.core;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.TileEntityModelCollision;

/** Vanilla landing dust and sprint particles carry a helper ID, without its owner. */
public final class DecorParticleHooks {
 private DecorParticleHooks(){}
 public static EntityFX resolve(EntityFX particle,String name,double x,double y,double z) {
  if(!(particle instanceof EntityDiggingFX)||name==null||
      !(name.startsWith("blockdust_")||name.startsWith("blockcrack_")))return particle;
  World world=particle.worldObj;
  if(world==null)return particle;
  int bx=MathHelper.floor_double(x),bz=MathHelper.floor_double(z);
  // Landing dust is emitted at blockY+1; sprint dust just above the surface.
  int[] heights={MathHelper.floor_double(y-.2),MathHelper.floor_double(y)};
  for(int by:heights) {
   TileEntity raw=world.getTileEntity(bx,by,bz);
   if(!(raw instanceof TileEntityModelCollision))continue;
   TileEntityModelCollision part=(TileEntityModelCollision)raw;
   BlockModels owner=part.getOwnerBlock();
   if(owner!=null) {
    particle.setParticleIcon(owner.getIcon(0,world.getBlockMetadata(part.getOwnerX(),part.getOwnerY(),part.getOwnerZ())));
    break;
   }
  }
  return particle;
 }
}
