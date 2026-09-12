package ru.givler.mbo.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.world.World;

public final class WaterloggingCameraHooks {
  private WaterloggingCameraHooks() {}

  public static Block getViewBlock(World world, EntityLivingBase entity, float partialTicks) {
    if (WaterloggingEntityHooks.isInsideWaterlogged(entity, Material.water)) return Blocks.water;
    return ActiveRenderInfo.getBlockAtEntityViewpoint(world, entity, partialTicks);
  }
}
