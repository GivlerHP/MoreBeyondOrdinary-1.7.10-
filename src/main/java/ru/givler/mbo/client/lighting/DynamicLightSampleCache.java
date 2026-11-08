package ru.givler.mbo.client.lighting;

/** Thread-owned, bounded RGB cache. Collisions evict entries, never alias coordinates. */
public final class DynamicLightSampleCache {
  private static final int CAPACITY=8192;
  private final int[] xs=new int[CAPACITY],ys=new int[CAPACITY],zs=new int[CAPACITY],rgb=new int[CAPACITY],stamps=new int[CAPACITY];
  private int epoch=1;
  private volatile int size;
  public int size(){return size;}
  public void clear(){if(++epoch==0){java.util.Arrays.fill(stamps,0);epoch=1;}size=0;}
  private static int slot(int x,int y,int z){
    int h=x*0x8da6b343 ^ y*0xd8163841 ^ z*0xcb1ab31f;
    h^=h>>>16;return h&(CAPACITY-1);
  }
  /** -1 is absent; zero is a cached unlit cell. */
  public int get(int x,int y,int z){int i=slot(x,y,z);return stamps[i]==epoch&&xs[i]==x&&ys[i]==y&&zs[i]==z?rgb[i]:-1;}
  public void put(int x,int y,int z,int color){
    int i=slot(x,y,z);if(stamps[i]!=epoch)size++;
    xs[i]=x;ys[i]=y;zs[i]=z;rgb[i]=color;stamps[i]=epoch;
  }
}
