package ru.givler.mbo.core;
import ru.givler.mbo.client.lighting.DynamicLightGeometry;
public final class DynamicLightGeometrySmoke {
  public static void main(String[] args) {
    if(DynamicLightGeometry.moved(0,0,0,.06,.06,.06,.1))throw new AssertionError("Diagonal jitter must not rebuild");
    if(!DynamicLightGeometry.moved(0,0,0,.101,0,0,.1))throw new AssertionError("Movement must update");
    int sections=0;
    for(int x=-3;x<=3;x++)for(int y=-3;y<=3;y++)for(int z=-3;z<=3;z++)
      if(DynamicLightGeometry.affects(0,0,0,7.5,x,y,z))sections++;
    if(sections!=8)throw new AssertionError("Boundary source sections="+sections);
    // Every cell within the radius must retain its section, including negative coordinates.
    for(double p:new double[]{-16.1,-.1,0,7.9,15.9,16.1})
      for(int x=-26;x<=26;x++)for(int y=-10;y<=10;y++)for(int z=-10;z<=10;z++) {
        double dx=x+.5-p,dy=y+.5,dz=z+.5;
        if(dx*dx+dy*dy+dz*dz<7.5*7.5 && !DynamicLightGeometry.affects(p,0,0,7.5,x>>4,y>>4,z>>4))
          throw new AssertionError("Lit cell omitted");
      }
    System.out.println("DYNAMIC_LIGHT_GEOMETRY_OK: movement threshold, boundaries, negative coordinates and coverage");
  }
}
