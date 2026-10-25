package ru.givler.mbo.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class ParticleConfig {
  public static volatile boolean cubicParticles=true, rgbLighting=true;
  private static File file;
  private ParticleConfig() {}
  public static void load(File directory) {
    file=new File(directory,"MoreBeyondOrdinary/particles.cfg");
    Configuration config=new Configuration(file);
    config.load();
    cubicParticles=config.getBoolean("CubicParticles","client",true,"Render full-block debris as small textured cubes.");
    rgbLighting=config.getBoolean("RGBLighting","client",true,"Apply nearby colored lighting to flat and cubic particles.");
    if(config.hasChanged()) config.save();
  }
  public static void save() {
    if(file==null) return;
    Configuration config=new Configuration(file);
    config.load();
    config.get("client","CubicParticles",true).set(cubicParticles);
    config.get("client","RGBLighting",true).set(rgbLighting);
    config.save();
  }
}
