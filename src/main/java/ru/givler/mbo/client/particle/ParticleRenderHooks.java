package ru.givler.mbo.client.particle;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import ru.givler.mbo.client.lighting.*;
import ru.givler.mbo.config.ColoredLightConfig;
import ru.givler.mbo.config.ParticleConfig;

/** Per-pass spatial RGB cache and a scoped vertex-color multiplier. */
public final class ParticleRenderHooks {
  private static final int SIZE=2048;
  private static final long[] KEYS=new long[SIZE];
  private static final int[] STAMPS=new int[SIZE], RGB=new int[SIZE];
  private static int epoch=1;
  private static World cacheWorld;
  private static volatile boolean active;
  private static final ThreadLocal<State> STATE=new ThreadLocal<State>() {
    @Override protected State initialValue() { return new State(); }
  };
  private static final class State { int tint=-1; }
  private ParticleRenderHooks() {}

  public static void beginPass() {
    if(++epoch==0) { java.util.Arrays.fill(STAMPS,0);epoch=1; }
    cacheWorld=null;
  }
  static int sample(World world,double x,double y,double z) {
    if(cacheWorld!=world) { beginPass();cacheWorld=world; }
    // Half-block cells share one interpolated light sample within this pass.
    int ix=(int)Math.floor(x*2),iy=(int)Math.floor(y*2),iz=(int)Math.floor(z*2);
    long key=((long)ix & 0x7ffffffL)<<37 | ((long)iz & 0x7ffffffL)<<10 | (iy & 1023L);
    int slot=(int)((key^(key>>>21)^(key>>>42))*0x9E3779B9L)&(SIZE-1);
    for(int probe=0;probe<4;probe++,slot=(slot+1)&(SIZE-1)) {
      if(STAMPS[slot]==epoch && KEYS[slot]==key) return RGB[slot];
      if(STAMPS[slot]!=epoch) {
        int rgb=ColoredLightRenderHooks.worldRGB((ix+.5)*.5,(iy+.5)*.5,(iz+.5)*.5);
        KEYS[slot]=key;RGB[slot]=rgb;STAMPS[slot]=epoch;return rgb;
      }
    }
    // Fixed probe budget: dense effects never resize maps or allocate cache entries.
    return ColoredLightRenderHooks.worldRGB(x,y,z);
  }

  public static void render(EntityFX particle,Tessellator tess,float partial,
      float rotationX,float rotationXZ,float rotationZ,float rotationYZ,float rotationXY) {
    if(!ParticleConfig.rgbLighting || !ColoredLightConfig.enabled || ColoredLightShaderBridge.active
        || particle.worldObj==null || (!ColoredLightClient.cacheActive && ColoredLightClient.volume==null)) {
      draw(particle,tess,partial,rotationX,rotationXZ,rotationZ,rotationYZ,rotationXY);return;
    }
    int rgb=sample(particle.worldObj,
        particle.prevPosX+(particle.posX-particle.prevPosX)*partial,
        particle.prevPosY+(particle.posY-particle.prevPosY)*partial,
        particle.prevPosZ+(particle.posZ-particle.prevPosZ)*partial);
    int brightness=tess instanceof ParticleBrightness?((ParticleBrightness)tess).mbo$particleBrightness():0;
    int tint=ColoredLightRenderHooks.worldTint(-1,brightness,rgb);
    if(tint==-1) { draw(particle,tess,partial,rotationX,rotationXZ,rotationZ,rotationYZ,rotationXY);return; }
    State state=STATE.get();int previous=state.tint;boolean wasActive=active;
    state.tint=tint;active=true;
    try { draw(particle,tess,partial,rotationX,rotationXZ,rotationZ,rotationYZ,rotationXY); }
    finally { state.tint=previous;active=wasActive; }
  }
  private static void draw(EntityFX particle,Tessellator tess,float partial,
      float x,float xz,float z,float yz,float xy) {
    if(!CubicParticleRenderer.render(particle,tess,partial)) particle.renderParticle(tess,partial,x,xz,z,yz,xy);
  }
  public static int tint(int color) {
    if(!active) return color;
    int tint=STATE.get().tint;
    if(tint==-1) return color;
    // Packed native vertex order; alpha is copied exactly.
    return color & 0xFF000000 | ((color & 255)*(tint & 255)+127)/255
        | (((color>>>8 & 255)*(tint>>>8 & 255)+127)/255)<<8
        | (((color>>>16 & 255)*(tint>>>16 & 255)+127)/255)<<16;
  }
}
