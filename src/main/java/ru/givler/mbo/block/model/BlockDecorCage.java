package ru.givler.mbo.block.model;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.ModelTileBase;

public final class BlockDecorCage extends BlockModels {
 private static final java.util.Map<String,float[][]> CELLS=CollisionShapeParts.cells(closedShape());
 public BlockDecorCage() {
  super(Material.iron,"ModelTortureIronCage","instruments_of_torture/iron_cage","instruments_of_torture/iron_cage");
  withDynamicModelRender();withModelOffsetY(.1875F);
  withMultipartCollisionBoxes(closedShape());
 }
 public static float[][] closedShape() {
  float t=1/32F;
  return new float[][]{{0,0,0,1,t,1},{0,2-t,0,1,2,1},
   {0,t,0,t,2-t,1},{1-t,t,0,1,2-t,1},{0,t,0,1,2-t,t},{0,t,1-t,1,2-t,1}};
 }
 @Override protected float[] rotateBounds(float[] box,int facing) {
  float[] rotated=super.rotateBounds(box,facing);
  // A flush support plane catches falling entities without creating a step.
  // A raised floor below this roof prevents vanilla's automatic step movement.
  if(box[1]==0&&box[4]<=1/32F&&box[0]==0&&box[2]==0&&box[3]==1&&box[5]==1)
   rotated[4]=0;
  return rotated;
 }
 public boolean isOpen(World world,int x,int y,int z) {
  net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
  return tile instanceof ModelTileBase&&((ModelTileBase)tile).cageOpen;
 }
 public float[][] boxesForCell(int dy,int facing) {
  float[][] boxes=CELLS.get("0,"+dy+",0");
  if(boxes==null)return new float[0][];
  float[][] rotated=new float[boxes.length][];
  for(int i=0;i<boxes.length;i++)rotated[i]=rotateBounds(boxes[i],facing);
  return rotated;
 }
 public boolean includesModelCollisionBox(World world,int x,int y,int z,float[] b) {
  if(!isOpen(world,x,y,z))return true;
  // The horizontal roof/floor remain, including in the upper collision cell.
  if(b[4]-b[1]<=1/32F+.0001F)return true;
  int facing=world.getBlockMetadata(x,y,z)&3;
  if(facing==0)return !(b[2]>=.9374F&&b[5]-b[2]<=.063F&&b[3]-b[0]>=.9F);
  if(facing==2)return !(b[5]<=.0626F&&b[5]-b[2]<=.063F&&b[3]-b[0]>=.9F);
  if(facing==1)return !(b[3]<=.0626F&&b[3]-b[0]<=.063F&&b[5]-b[2]>=.9F);
  return !(b[0]>=.9374F&&b[3]-b[0]<=.063F&&b[5]-b[2]>=.9F);
 }
 public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz) {
  if(!canOperateDoor(world.getBlockMetadata(x,y,z)&3,side,player.posX-x,player.posY-y,player.posZ-z))return false;
  if(world.isRemote)return true;
  net.minecraft.tileentity.TileEntity raw=world.getTileEntity(x,y,z);
  if(!(raw instanceof ModelTileBase))return false;
  ModelTileBase tile=(ModelTileBase)raw;
  if(tile.cageOpen&&!canClose(world,x,y,z))return true;
  tile.cageOpen=!tile.cageOpen;tile.markDirty();world.markBlockForUpdate(x,y,z);
  world.playSoundEffect(x+.5,y+1,z+.5,tile.cageOpen?"mbo:copper.door.open":"mbo:copper.door.close",.7F,1);
  return true;
 }
 public boolean canClose(World world,int x,int y,int z) {
  float[] b=rotateBounds(new float[]{0,1/32F,31/32F,1,2-1/32F,1},world.getBlockMetadata(x,y,z)&3);
  return world.getEntitiesWithinAABB(EntityLivingBase.class,AxisAlignedBB.getBoundingBox(x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5])).isEmpty();
 }
 public static boolean canOperateDoor(int facing,int side,double px,double py,double pz) {
  if(px>0&&px<1&&pz>0&&pz<1&&py>=0&&py<2)return true;
  // The closed door is on local +Z; require its exterior face from outside.
  switch(facing&3) {
   case 1:return side==4&&px<=0;
   case 2:return side==2&&pz<=0;
   case 3:return side==5&&px>=1;
   default:return side==3&&pz>=1;
  }
 }
}
