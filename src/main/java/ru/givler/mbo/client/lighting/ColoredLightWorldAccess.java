package ru.givler.mbo.client.lighting;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorldAccess;

/** Client world notifications, including fire metadata changes and opaque obstacles. */
final class ColoredLightWorldAccess implements IWorldAccess {
  private final ColoredLightClient client;
  ColoredLightWorldAccess(ColoredLightClient client) { this.client = client; }
  @Override public void markBlockForUpdate(int x, int y, int z) { client.changed(x, y, z); }
  @Override public void markBlockForRenderUpdate(int x, int y, int z) { client.changed(x, y, z); }
  @Override public void markBlockRangeForRenderUpdate(int x, int y, int z, int x2, int y2, int z2) {
    client.changedRange(x,y,z,x2,y2,z2);
  }
  @Override public void playSound(String name, double x, double y, double z, float volume, float pitch) {}
  @Override public void playSoundToNearExcept(EntityPlayer player, String name, double x, double y, double z, float volume, float pitch) {}
  @Override public void spawnParticle(String name, double x, double y, double z, double vx, double vy, double vz) {}
  @Override public void onEntityCreate(Entity entity) {}
  @Override public void onEntityDestroy(Entity entity) {}
  @Override public void playRecord(String name, int x, int y, int z) {}
  @Override public void broadcastSound(int id, int x, int y, int z, int data) {}
  @Override public void playAuxSFX(EntityPlayer player, int id, int x, int y, int z, int data) {}
  @Override public void destroyBlockPartially(int id, int x, int y, int z, int progress) {}
  @Override public void onStaticEntitiesChanged() {}
}
