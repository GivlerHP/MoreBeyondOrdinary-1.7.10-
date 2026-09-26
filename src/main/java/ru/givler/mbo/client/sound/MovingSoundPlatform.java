package ru.givler.mbo.client.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

@SideOnly(Side.CLIENT)
public class MovingSoundPlatform extends MovingSound {
  private final EntityMovingPlatform platform;
  private final String soundName;

  public MovingSoundPlatform(EntityMovingPlatform platform, String soundName) {
    super(new ResourceLocation(soundName));
    this.platform = platform;
    this.soundName = soundName;
    repeat = true;
    volume = 1F;
    field_147663_c = 1F;
    field_147665_h = 0;
    update();
  }

  public String getSoundName() { return soundName; }
  public boolean isStopped() { return donePlaying; }
  public void stop() { donePlaying = true; }

  public static boolean isListenerNearby(EntityMovingPlatform platform) {
    return listenerDistance(platform) < 32D;
  }

  public static boolean isListenerFarAway(EntityMovingPlatform platform) {
    return listenerDistance(platform) >= 48D;
  }

  @Override
  public ISound.AttenuationType getAttenuationType() {
    return ISound.AttenuationType.NONE;
  }

  private static double listenerDistance(EntityMovingPlatform platform) {
    Entity listener = Minecraft.getMinecraft().renderViewEntity;
    if (listener == null) return Double.POSITIVE_INFINITY;
    double x = listener.posX, y = listener.posY + listener.getEyeHeight(), z = listener.posZ;
    double dx = Math.max(platform.posX - x, Math.max(0D, x - platform.posX - platform.getSizeX()));
    double dy = Math.max(platform.posY - y, Math.max(0D, y - platform.posY - platform.getSizeY()));
    double dz = Math.max(platform.posZ - z, Math.max(0D, z - platform.posZ - platform.getSizeZ()));
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  @Override
  public void update() {
    if (platform.isDead || !platform.isMoving() || !soundName.equals(platform.getMovementSound())) {
      donePlaying = true;
      return;
    }
    double distance = listenerDistance(platform);
    if (distance >= 48D) { donePlaying = true; return; }
    volume = (float) Math.max(0.01D, 0.4D * (1D - distance / 48D));
    Entity listener = Minecraft.getMinecraft().renderViewEntity;
    if (listener == null) { donePlaying = true; return; }
    xPosF = (float) listener.posX;
    yPosF = (float) (listener.posY + listener.getEyeHeight());
    zPosF = (float) listener.posZ;
  }
}
