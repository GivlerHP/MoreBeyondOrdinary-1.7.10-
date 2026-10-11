package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.lwjgl.opengl.*;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.*;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.client.model.decormodels.DecorBlockModel;
import ru.givler.mbo.client.render.decormodels.*;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.*;
import sun.misc.Unsafe;

public class RopeChunkSmoke {
 static class TestWorld extends World {
  Map<String,Block> blocks;Map<String,TileEntity> tiles;int meta,radius;boolean available;
  TestWorld(){super(null,"test",(net.minecraft.world.WorldSettings)null,(net.minecraft.world.WorldProvider)null,null);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected int func_152379_p(){return 0;}
  public Entity getEntityByID(int id){return null;}
  static String key(int x,int y,int z){return x+","+y+","+z;}
  public Block getBlock(int x,int y,int z){return blocks.get(key(x,y,z));}
  public TileEntity getTileEntity(int x,int y,int z){return tiles.get(key(x,y,z));}
  public int getBlockMetadata(int x,int y,int z){return x==8&&y==8&&z==8?meta:0;}
  public boolean blockExists(int x,int y,int z){return available;}
  public int getLightBrightnessForSkyBlocks(int x,int y,int z,int light){return 0x00F000F0;}
  public void markBlockRangeForRenderUpdate(int x,int y,int z,int xx,int yy,int zz){radius=(xx-x)/2;}
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  Pbuffer buffer=new Pbuffer(32,32,new PixelFormat(8,24,0),null,null);buffer.makeCurrent();
  StaticDecorPixelsSmoke.fakeMinecraft();
  Method mapping=TileEntity.class.getDeclaredMethod("addMapping",Class.class,String.class);mapping.setAccessible(true);
  mapping.invoke(null,ModelTileBase.class,"mbo_rope_chunk_test");
  Field field=Minecraft.class.getDeclaredField("mcDataDir");field.setAccessible(true);field.set(Minecraft.getMinecraft(),new java.io.File("build/rope-chunk-validation"));
  net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit=GL13.GL_TEXTURE0;
  net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit=GL13.GL_TEXTURE1;
  field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);Unsafe unsafe=(Unsafe)field.get(null);
  BlockConnectedRope rope=(BlockConnectedRope)unsafe.allocateInstance(BlockConnectedRope.class);
  ChunkDecorSmoke.set(rope,"modelName","instruments_of_torture/rope");
  ChunkDecorSmoke.set(rope,"textureName","instruments_of_torture/rope");
  field=BlockModels.class.getDeclaredField("ALL_MODELS");field.setAccessible(true);
  List<BlockModels> models=(List<BlockModels>)field.get(null);models.clear();models.add(rope);
  TextureMap atlas=(TextureMap)unsafe.allocateInstance(TextureMap.class);
  field=TextureMap.class.getDeclaredField("mapRegisteredSprites");field.setAccessible(true);field.set(atlas,new HashMap<>());
  ChunkDecorRenderer renderer=ChunkDecorRenderer.INSTANCE;renderer.stitch(new TextureStitchEvent.Pre(atlas));
  BlockModels.decorChunkRendering=true;BlockModels.decorChunkRenderId=1234;
  check(rope.usesDecorChunkRendering(),"connected rope bake failed");
  TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);world.available=true;
  world.blocks=new HashMap<>();world.tiles=new HashMap<>();
  ModelTileBase tile=new ModelTileBase("instruments_of_torture/rope","instruments_of_torture/rope");
  tile.xCoord=tile.yCoord=tile.zCoord=8;tile.setWorldObj(world);
  int[][] offset={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
  Method selected=ChunkDecorRenderer.class.getDeclaredMethod("selected",BlockModels.class,net.minecraft.world.IBlockAccess.class,int.class,int.class,int.class,int.class);selected.setAccessible(true);
  Field raw=Tessellator.class.getDeclaredField("rawBuffer");raw.setAccessible(true);
  Field index=Tessellator.class.getDeclaredField("rawBufferIndex");index.setAccessible(true);
  DecorBlockModel provider=new DecorBlockModel();int checked=0;
  for(int mask=1;mask<64;mask++)for(boolean floor:new boolean[]{false,true})for(boolean anchor:new boolean[]{false,true}) {
   world.blocks.clear();world.tiles.clear();world.blocks.put("8,8,8",rope);world.tiles.put("8,8,8",tile);
   tile.ropeFloor=floor;tile.ropeAnchor=anchor;
   for(int direction=0;direction<6;direction++)if((mask&(1<<direction))!=0) {
    int[] d=offset[direction];String key=TestWorld.key(8+d[0],8+d[1],8+d[2]);
    world.blocks.put(key,rope);ModelTileBase neighbour=new ModelTileBase();neighbour.ropeFloor=floor;world.tiles.put(key,neighbour);
   }
   Object mesh=selected.invoke(renderer,rope,world,8,8,8,0);check(mesh!=null,"missing variant "+mask);
   Field vertices=mesh.getClass().getDeclaredField("raw");vertices.setAccessible(true);
   GeoModel expected=GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),provider.getModelLocation(tile));
   Tessellator builder=new Tessellator();builder.startDrawingQuads();
   for(GeoBone bone:expected.topLevelBones)StaticDecorPixelsSmoke.RENDERER.renderRecursively(builder,tile,bone,1,1,1,1);
   int[] expectedRaw=Arrays.copyOf((int[])raw.get(builder),index.getInt(builder));
   check(Arrays.equals(expectedRaw,(int[])vertices.get(mesh)),"TESR/chunk variant mismatch "+mask+" floor="+floor+" anchor="+anchor);
   check(ChunkDecorRenderer.renders(tile),"TESR not suppressed");
   Tessellator.instance.startDrawingQuads();check(renderer.renderWorldBlock(world,8,8,8,rope,1234,null),"chunk does not emit");Tessellator.instance.draw();checked++;
  }
  world.meta=RopeConnections.DECORATED;tile.updateContainingBlockInfo();
  check(!ChunkDecorRenderer.renders(tile),"noose must keep TESR");
  world.meta=0;tile.updateContainingBlockInfo();
  BlockModels hook=(BlockModels)unsafe.allocateInstance(BlockModels.class);ChunkDecorSmoke.set(hook,"modelName","instruments_of_torture/hook");
  world.blocks.put("8,6,8",hook);check(!ChunkDecorRenderer.renders(tile),"strand near attachment must keep TESR");
  world.blocks.remove("8,6,8");check(ChunkDecorRenderer.renders(tile),"attachment removal must restore chunk route");
  world.available=false;check(!ChunkDecorRenderer.renders(tile),"unloaded neighbours must not be forced to load");world.available=true;
  rope.onNeighborBlockChange(world,8,8,8,rope);check(world.radius==3,"neighbour rebuild radius");
  tile.onDataPacket(null,(net.minecraft.network.play.server.S35PacketUpdateTileEntity)tile.getDescriptionPacket());check(world.radius==3,"packet rebuild radius");
  System.out.println("PASS "+checked+" rope selections: all 63 masks, both heights, explicit/automatic anchors match TESR geometry; decoration/camera/unloaded fallback; neighbour and packet rebuilds");
  System.out.println(renderer.statistics());buffer.destroy();
 }
}
