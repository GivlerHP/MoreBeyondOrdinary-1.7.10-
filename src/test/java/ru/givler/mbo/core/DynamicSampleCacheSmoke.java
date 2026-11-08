package ru.givler.mbo.core;
import ru.givler.mbo.client.lighting.DynamicLightSampleCache;
public final class DynamicSampleCacheSmoke {
  public static void main(String[] args) {
    DynamicLightSampleCache c=new DynamicLightSampleCache();
    if(c.get(0,0,0)!=-1)throw new AssertionError("Empty cache");
    c.put(-30000000,-64,30000000,0);
    if(c.get(-30000000,-64,30000000)!=0)throw new AssertionError("Zero or signed coordinates lost");
    java.util.Random r=new java.util.Random(1710);
    for(int i=0;i<100000;i++){
      int x=r.nextInt(),y=r.nextInt(),z=r.nextInt(),rgb=r.nextInt(0x1000000);
      c.put(x,y,z,rgb);
      if(c.get(x,y,z)!=rgb)throw new AssertionError("Wrong cache result");
      if(c.get(x,y,z+1)!=-1)throw new AssertionError("Coordinate alias");
      if(c.size()>8192)throw new AssertionError("Unbounded cache");
    }
    c.clear();if(c.size()!=0)throw new AssertionError("Clear size");
    c.put(1,2,3,0xffffff);c.clear();if(c.get(1,2,3)!=-1)throw new AssertionError("Stale generation");
    System.out.println("DYNAMIC_SAMPLE_CACHE_OK: zero, coordinates, collisions, bounded capacity and invalidation");
  }
}
