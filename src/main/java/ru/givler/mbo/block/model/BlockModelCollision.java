package ru.givler.mbo.block.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.TileEntityModelCollision;

public class BlockModelCollision extends Block implements ITileEntityProvider {
  // A break packet can arrive after the helper TileEntity was removed locally.
  // Keep only recent client selection data; worlds are weak keys and each cache is bounded.
  private static final java.util.WeakHashMap<World,java.util.LinkedHashMap<String,ParticleSource>> particleSources=new java.util.WeakHashMap<>();
  private static final class ParticleSource {
    BlockModels owner;int x,y,z,meta;float[] bounds;long time;
  }
  private ParticleSource particleSource(World world,int x,int y,int z) {
    String key=x+","+y+","+z;
    java.util.LinkedHashMap<String,ParticleSource> cache=particleSources.get(world);
    TileEntityModelCollision part=tile(world,x,y,z);
    BlockModels owner=part==null?null:part.getOwnerBlock();
    if(owner!=null) {
      ParticleSource source=new ParticleSource();source.owner=owner;
      source.x=part.getOwnerX();source.y=part.getOwnerY();source.z=part.getOwnerZ();
      source.meta=world.getBlockMetadata(source.x,source.y,source.z);source.bounds=part.getBounds().clone();source.time=System.nanoTime();
      if(world.isRemote) {
        if(cache==null){cache=new java.util.LinkedHashMap<>();particleSources.put(world,cache);}
        cache.remove(key);cache.put(key,source);if(cache.size()>256)cache.remove(cache.keySet().iterator().next());
      }
      return source;
    }
    ParticleSource source=cache==null?null:cache.get(key);
    if(source!=null && System.nanoTime()-source.time>5_000_000_000L){cache.remove(key);return null;}
    return source;
  }
  @Override public void onBlockAdded(World world,int x,int y,int z) {
    if(!world.isRemote) world.scheduleBlockUpdate(x,y,z,this,1);
  }
  @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block changed) {
    if(!world.isRemote) world.scheduleBlockUpdate(x,y,z,this,1);
  }
  @Override public void updateTick(World world,int x,int y,int z,java.util.Random random) {
    TileEntityModelCollision part=tile(world,x,y,z);
    if(part!=null) part.checkOwner();
  }
  @Override
  public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z,
      net.minecraft.util.Vec3 start, net.minecraft.util.Vec3 end) {
    if (ru.givler.mbo.core.HangingCameraContext.ignore(world, x, y, z)) return null;
    TileEntityModelCollision part=tile(world,x,y,z);
    if(part!=null) {
      if(world.isRemote)particleSource(world,x,y,z);
      MovingObjectPosition closest=null;
      for(float[] b:part.getBoxes()) {
        setBlockBounds(b[0],b[1],b[2],b[3],b[4],b[5]);
        MovingObjectPosition raw=AxisAlignedBB.getBoundingBox(x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5]).calculateIntercept(start,end);
        MovingObjectPosition hit=raw==null?null:new MovingObjectPosition(x,y,z,raw.sideHit,raw.hitVec);
        if(hit!=null && (closest==null || start.squareDistanceTo(hit.hitVec)<start.squareDistanceTo(closest.hitVec))) closest=hit;
      }
      setBlockBoundsBasedOnState(world,x,y,z);
      return closest;
    }
    return super.collisionRayTrace(world, x, y, z, start, end);
  }
  @Override
  public void addCollisionBoxesToList(World world,int x,int y,int z,AxisAlignedBB query,
      java.util.List list,net.minecraft.entity.Entity entity) {
    TileEntityModelCollision part=tile(world,x,y,z);
    if(part==null) return;
    BlockModels owner=part.getOwnerBlock();
    if(owner==null || !owner.isModelCollisionEnabled()) return;
    for(float[] b:part.getBoxes()) {
      AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5]);
      if(box.intersectsWith(query)) list.add(box);
    }
  }
  @Override
  public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
      int side, float hitX, float hitY, float hitZ) {
    TileEntityModelCollision part = tile(world, x, y, z);
    return part != null && ru.givler.mbo.handler.HangingEvents.activate(world,
        part.getOwnerX(), part.getOwnerY(), part.getOwnerZ(), player);
  }
  public BlockModelCollision() {
    super(Material.rock);
    setBlockName("ModelCollisionPart");
    setHardness(0.0F);
    setResistance(6000000.0F);
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public int getRenderType() {
    return -1;
  }

  @Override
  public boolean hasTileEntity(int meta) {
    return true;
  }

  @Override
  public TileEntity createNewTileEntity(World world, int meta) {
    return new TileEntityModelCollision();
  }

  @Override
  public int quantityDropped(java.util.Random random) {
    return 0;
  }

  private TileEntityModelCollision tile(IBlockAccess world, int x, int y, int z) {
    TileEntity tile = world.getTileEntity(x, y, z);
    return tile instanceof TileEntityModelCollision ? (TileEntityModelCollision) tile : null;
  }

  public ItemStack getOwnerStack(IBlockAccess world, int x, int y, int z, EntityPlayer player) {
    TileEntityModelCollision collision = tile(world, x, y, z);
    BlockModels owner = collision == null ? null : collision.getOwnerBlock();
    if (owner == null) return null;
    World ownerWorld = world instanceof World ? (World) world : null;
    if (ownerWorld == null) return new ItemStack(owner, 1, 0);
    MovingObjectPosition ownerHit =
        new MovingObjectPosition(
            collision.getOwnerX(),
            collision.getOwnerY(),
            collision.getOwnerZ(),
            1,
            net.minecraft.util.Vec3.createVectorHelper(
                collision.getOwnerX() + 0.5D,
                collision.getOwnerY() + 0.5D,
                collision.getOwnerZ() + 0.5D));
    ItemStack stack =
        owner.getPickBlock(
            ownerHit,
            ownerWorld,
            collision.getOwnerX(),
            collision.getOwnerY(),
            collision.getOwnerZ(),
            player);
    return stack == null ? new ItemStack(owner, 1, 0) : stack;
  }

  @Override
  public ItemStack getPickBlock(
      MovingObjectPosition hit, World world, int x, int y, int z, EntityPlayer player) {
    return getOwnerStack(world, x, y, z, player);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer renderer) {
    ParticleSource collision = particleSource(world, target.blockX, target.blockY, target.blockZ);
    if (collision == null) return true;
    float[] b=collision.bounds;setBlockBounds(b[0],b[1],b[2],b[3],b[4],b[5]);
    double px =
        target.blockX
            + getBlockBoundsMinX()
            + world.rand.nextDouble() * (getBlockBoundsMaxX() - getBlockBoundsMinX());
    double py =
        target.blockY
            + getBlockBoundsMinY()
            + world.rand.nextDouble() * (getBlockBoundsMaxY() - getBlockBoundsMinY());
    double pz =
        target.blockZ
            + getBlockBoundsMinZ()
            + world.rand.nextDouble() * (getBlockBoundsMaxZ() - getBlockBoundsMinZ());
    // Keep hit particles outside the contacted face instead of inside the model.
    if(target.hitVec!=null){px=target.hitVec.xCoord;py=target.hitVec.yCoord;pz=target.hitVec.zCoord;}
    if(target.sideHit==0)py-=.01;else if(target.sideHit==1)py+=.01;
    else if(target.sideHit==2)pz-=.01;else if(target.sideHit==3)pz+=.01;
    else if(target.sideHit==4)px-=.01;else if(target.sideHit==5)px+=.01;
    addOwnerParticle(world, collision, renderer, px, py, pz, 0.0D, 0.0D, 0.0D, true);
    return true;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addDestroyEffects(
      World world, int x, int y, int z, int meta, EffectRenderer renderer) {
    ParticleSource collision = particleSource(world, x, y, z);
    if (collision == null) return true;
    float[] b=collision.bounds;setBlockBounds(b[0],b[1],b[2],b[3],b[4],b[5]);
    for (int ix = 0; ix < 4; ix++)
      for (int iy = 0; iy < 4; iy++)
        for (int iz = 0; iz < 4; iz++) {
          double px =
              x
                  + getBlockBoundsMinX()
                  + (ix + 0.5D) / 4.0D * (getBlockBoundsMaxX() - getBlockBoundsMinX());
          double py =
              y
                  + getBlockBoundsMinY()
                  + (iy + 0.5D) / 4.0D * (getBlockBoundsMaxY() - getBlockBoundsMinY());
          double pz =
              z
                  + getBlockBoundsMinZ()
                  + (iz + 0.5D) / 4.0D * (getBlockBoundsMaxZ() - getBlockBoundsMinZ());
          addOwnerParticle(
              world,
              collision,
              renderer,
              px,
              py,
              pz,
              px - x - 0.5D,
              py - y - 0.5D,
              pz - z - 0.5D,
              false);
        }
    return true;
  }

  @SideOnly(Side.CLIENT)
  private void addOwnerParticle(
      World world,
      ParticleSource collision,
      EffectRenderer renderer,
      double x,
      double y,
      double z,
      double mx,
      double my,
      double mz,
      boolean hitParticle) {
    BlockModels owner = collision.owner;
    int meta = collision.meta;
    EntityDiggingFX particle = new EntityDiggingFX(world, x, y, z, mx, my, mz, owner, meta);
    particle.applyColourMultiplier(
        collision.x, collision.y, collision.z);
    if (hitParticle) particle.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
    renderer.addEffect(particle);
  }

  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
    TileEntityModelCollision tile = tile(world, x, y, z);
    float[] b = tile == null ? new float[] {0, 0, 0, 1, 1, 1} : tile.getBounds();
    setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
    TileEntityModelCollision tile = tile(world, x, y, z);
    BlockModels owner = tile == null ? null : tile.getOwnerBlock();
    if (owner == null || !owner.isModelCollisionEnabled()) return null;
    setBlockBoundsBasedOnState(world, x, y, z);
    return super.getCollisionBoundingBoxFromPool(world, x, y, z);
  }

  @Override
  public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
    setBlockBoundsBasedOnState(world, x, y, z);
    return super.getSelectedBoundingBoxFromPool(world, x, y, z);
  }

  @Override
  public float getBlockHardness(World world, int x, int y, int z) {
    TileEntityModelCollision collision = tile(world, x, y, z);
    BlockModels owner = collision == null ? null : collision.getOwnerBlock();
    return owner == null
        ? super.getBlockHardness(world, x, y, z)
        : owner.getBlockHardness(
            world, collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ());
  }

  @Override
  public float getPlayerRelativeBlockHardness(
      EntityPlayer player, World world, int x, int y, int z) {
    TileEntityModelCollision collision = tile(world, x, y, z);
    BlockModels owner = collision == null ? null : collision.getOwnerBlock();
    return owner == null
        ? super.getPlayerRelativeBlockHardness(player, world, x, y, z)
        : owner.getPlayerRelativeBlockHardness(
            player, world, collision.getOwnerX(), collision.getOwnerY(), collision.getOwnerZ());
  }

  @Override
  public boolean removedByPlayer(
      World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
    TileEntityModelCollision tile = tile(world, x, y, z);
    if (tile != null && tile.getOwnerBlock() != null) {
      BlockModels owner=tile.getOwnerBlock();
      int ox=tile.getOwnerX(),oy=tile.getOwnerY(),oz=tile.getOwnerZ();
      int meta=world.getBlockMetadata(ox,oy,oz);
      // The player controller already emits the break effect at the clicked part.
      // destroyBlock would emit a second effect at the owner and always drop it,
      // including for creative players. Remove the owner silently instead.
      if(!world.isRemote && !player.capabilities.isCreativeMode)
        owner.dropBlockAsItem(world,ox,oy,oz,meta,0);
      boolean removed=world.setBlockToAir(ox,oy,oz);
      if(removed && world.getBlock(x,y,z)==this)world.setBlockToAir(x,y,z);
      return removed;
    }
    return super.removedByPlayer(world, player, x, y, z, willHarvest);
  }

  @Override
  public ArrayList<net.minecraft.item.ItemStack> getDrops(
      World world, int x, int y, int z, int metadata, int fortune) {
    return new ArrayList<net.minecraft.item.ItemStack>();
  }
}
