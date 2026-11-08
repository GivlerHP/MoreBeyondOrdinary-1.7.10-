package ru.givler.mbo.client.lighting;

/** Geometry-only decisions, shared by every dynamic-light backend. */
public final class DynamicLightGeometry {
  private DynamicLightGeometry() {}
  public static boolean moved(double ax,double ay,double az,double bx,double by,double bz,double threshold) {
    return Math.abs(ax-bx)>threshold || Math.abs(ay-by)>threshold || Math.abs(az-bz)>threshold;
  }
  /** Include one block of neighbour sampling used by face interpolation/AO. */
  public static boolean affects(double x,double y,double z,double radius,int sx,int sy,int sz) {
    double dx=distance(x,sx*16.0-1,sx*16.0+17);
    double dy=distance(y,sy*16.0-1,sy*16.0+17);
    double dz=distance(z,sz*16.0-1,sz*16.0+17);
    return dx*dx+dy*dy+dz*dz<radius*radius;
  }
  private static double distance(double v,double min,double max) {return v<min?min-v:v>max?v-max:0;}
}
