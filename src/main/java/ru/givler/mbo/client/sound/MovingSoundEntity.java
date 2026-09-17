package ru.givler.mbo.client.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * A sound that tracks a moving entity, updating its position every tick.
 * Stops automatically when the entity is dead.
 */
@SideOnly(Side.CLIENT)
public class MovingSoundEntity extends MovingSound {
  private final Entity source;

  public MovingSoundEntity(Entity entity, String soundName, float volume, float pitch, boolean repeat) {
    super(new ResourceLocation(soundName));
    this.source = entity;
    this.repeat = repeat;
    this.volume = volume;
    this.field_147663_c = pitch;
    this.field_147665_h = 0;
    this.xPosF = (float) entity.posX;
    this.yPosF = (float) entity.posY;
    this.zPosF = (float) entity.posZ;
  }

  @Override
  public void update() {
    if (this.source.isDead) {
      this.donePlaying = true;
    } else {
      this.xPosF = (float) this.source.posX;
      this.yPosF = (float) this.source.posY;
      this.zPosF = (float) this.source.posZ;
    }
  }
}
