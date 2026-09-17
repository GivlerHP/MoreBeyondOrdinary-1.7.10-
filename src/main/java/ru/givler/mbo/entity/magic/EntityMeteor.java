package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.registry.BlockRegistry;

/** Burning falling block which explodes and ignites the impact area. */
public final class EntityMeteor extends EntityFallingBlock implements IEntityAdditionalSpawnData {
  private float area = 1.0F;

  public EntityMeteor(World world) {
    super(world);
  }

  public EntityMeteor(World world, double x, double y, double z, float area) {
    super(world, x, y, z, BlockRegistry.MeteorBlock);
    this.area = area;
    setSize(.98F, .98F);
    motionY = -1;
    setFire(200);
    ignoreFrustumCheck = true;
  }

  @Override
  public void onUpdate() {
    if (ticksExisted % 16 == 1 && worldObj.isRemote)
      ru.givler.mbo.MoreBeyondOrdinary.proxy.playMovingSound(
          this, "mbo:flameray", 3.0F, 1.0F, false);
    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    motionY -= .1D;
    moveEntity(motionX, motionY, motionZ);
    motionX *= .98D;
    motionY *= .98D;
    motionZ *= .98D;
    if (!worldObj.isRemote && onGround) {
      worldObj.createExplosion(this, posX, posY, posZ, 2 * area, true);
      ignite();
      setDead();
    }
    if (ticksExisted > 200) setDead();
  }

  private void ignite() {
    int cx = (int) Math.floor(posX), cy = (int) Math.floor(posY), cz = (int) Math.floor(posZ);
    for (int x = cx - 3; x <= cx + 3; x++)
      for (int z = cz - 3; z <= cz + 3; z++) {
        int y = worldObj.getTopSolidOrLiquidBlock(x, z);
        double d = getDistance(x, y, z);
        if (d < 4 && worldObj.isAirBlock(x, y, z) && rand.nextInt((int) d * 2 + 1) < 3)
          worldObj.setBlock(x, y, z, Blocks.fire);
      }
  }

  @Override
  protected void fall(float distance) {}

  @Override
  public boolean canRenderOnFire() {
    return true;
  }

  public boolean isInRangeToRenderVec3D(net.minecraft.util.Vec3 par1Vec3) {
    return true;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound t) {
    super.writeEntityToNBT(t);
    t.setFloat("Area", area);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound t) {
    super.readEntityFromNBT(t);
    area = t.getFloat("Area");
  }

  @Override
  public void writeSpawnData(ByteBuf b) {
    b.writeFloat(area);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    area = b.readFloat();
  }
}
