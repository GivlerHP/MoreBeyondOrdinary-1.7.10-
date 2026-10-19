package ru.givler.mbo.client.texture;

import java.lang.reflect.Proxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public final class ConnectedTextureSmoke {
  static final IIcon original=icon("original"),first=icon("variant-a"),second=icon("variant-b");
  static volatile boolean enabled=true;
  static volatile IIcon selected=first;
  public static class AngelicaApi {
    public static IIcon getBlockIcon(IIcon icon,Block block,IBlockAccess world,int x,int y,int z,int face) {
      if(x!=-17 || y!=-64 || z!=31 || face!=5)throw new AssertionError("World position/face was not forwarded intact");
      if(world.getBlockMetadata(x,y,z)!=7)throw new AssertionError("Metadata lookup changed");
      return enabled?selected:icon;
    }
  }
  public static class OptifineApi {
    public static IIcon getConnectedTexture(IBlockAccess world,Block block,int x,int y,int z,int face,IIcon icon) {
      return AngelicaApi.getBlockIcon(icon,block,world,x,y,z,face);
    }
  }
  static class TestBlock extends Block {TestBlock(){super(Material.rock);}}
  private static IIcon icon(String name) {
    return (IIcon)Proxy.newProxyInstance(ConnectedTextureSmoke.class.getClassLoader(),new Class<?>[]{IIcon.class},(p,m,a)->{
      if(m.getName().equals("getIconName") || m.getName().equals("toString"))return name;
      if(m.getReturnType()==float.class)return 0F;
      if(m.getReturnType()==double.class)return 0D;
      if(m.getReturnType()==int.class)return 16;
      return null;
    });
  }
  public static void main(String[] args)throws Throwable {
    Block block=new TestBlock();
    IBlockAccess world=(IBlockAccess)Proxy.newProxyInstance(ConnectedTextureSmoke.class.getClassLoader(),new Class<?>[]{IBlockAccess.class},
        (p,m,a)->m.getName().equals("getBlockMetadata")?7:null);
    for(boolean optifine:new boolean[]{false,true}) {
      ConnectedTextureHooks.Backend backend=ConnectedTextureHooks.bind(optifine?OptifineApi.class:AngelicaApi.class,optifine);
      enabled=true;selected=first;
      same(first,backend.resolve(original,block,world,-17,-64,31,5));
      selected=second;same(second,backend.resolve(original,block,world,-17,-64,31,5));
      enabled=false;same(original,backend.resolve(original,block,world,-17,-64,31,5));
      enabled=true;selected=null;same(original,backend.resolve(original,block,world,-17,-64,31,5));
      selected=first;
      final Throwable[] failure={null};Thread[] threads=new Thread[4];
      for(int i=0;i<threads.length;i++)threads[i]=new Thread(()->{try {
        for(int j=0;j<500;j++)same(first,backend.resolve(original,block,world,-17,-64,31,5));
      }catch(Throwable e){failure[0]=e;}});
      for(Thread thread:threads)thread.start();for(Thread thread:threads)thread.join();
      if(failure[0]!=null)throw failure[0];
    }
    same(original,ConnectedTextureHooks.resolve(original,block,null,0,0,0,0));
    int[][] normals={{0,-127,0},{0,127,0},{0,0,-127},{0,0,127},{-127,0,0},{127,0,0}};
    for(int side=0;side<6;side++) {
      int[] n=normals[side];int packed=(n[0]&255)|((n[1]&255)<<8)|((n[2]&255)<<16);
      for(int rotation=0;rotation<4;rotation++) {
        double x=n[0],y=n[1],z=n[2],rx=x,rz=z;
        if(rotation==0){rx=-x;rz=-z;}else if(rotation==1){rx=z;rz=-x;}else if(rotation==3){rx=-z;rz=x;}
        int actual=ConnectedTextureHooks.rotatedFace(packed,rotation);
        int expected=ConnectedTextureHooks.face(rx,y,rz);
        if(actual!=expected)throw new AssertionError("Rotated CTM face mismatch");
      }
    }
    if(ConnectedTextureHooks.face(new double[]{1,0,0},new double[]{0,0,0},new double[]{0,1,0})!=2)
      throw new AssertionError("JSON north winding");
    if(ConnectedTextureHooks.face(0,0,0)!=-1)throw new AssertionError("Degenerate quad must skip CTM");
    System.out.println("CTM delegate: both API argument orders, negative-height world/metadata, graphics disable, resource reload, null fallback, concurrent workers, inventory isolation and rotated faces passed");
  }
  static void same(Object expected,Object actual){if(expected!=actual)throw new AssertionError("Unexpected texture variant");}
}
