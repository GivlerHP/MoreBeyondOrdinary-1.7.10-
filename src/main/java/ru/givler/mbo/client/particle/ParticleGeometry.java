package ru.givler.mbo.client.particle;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

/** Lightweight access supplied by the particle transformer; no reflective render path. */
public interface ParticleGeometry {
  Block mbo$particleBlock();
  IIcon mbo$particleIcon();
  float mbo$particleScale();
  float mbo$particleJitterX();
  float mbo$particleJitterY();
  float mbo$particleAlpha();
  int mbo$particleTextureX();
  int mbo$particleTextureY();
}
