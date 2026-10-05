package ru.givler.mbo.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.nio.Buffer;

public final class WaterloggingCameraHooks {
  private static final FloatBuffer MODELVIEW = BufferUtils.createFloatBuffer(16);
  private static Entity capturedEntity;
  private static Vec3 capturedCamera;
  private WaterloggingCameraHooks() {}

  /** Called once per world pass, before fog colour, sky and terrain are rendered. */
  public static void captureCamera(float partialTicks) {
    Minecraft minecraft = Minecraft.getMinecraft();
    Entity view = minecraft.renderViewEntity;
    capturedEntity = null;
    capturedCamera = null;
    if (view == null) return;
    boolean swimming = view instanceof net.minecraft.entity.player.EntityPlayer
        && ru.givler.mbo.swimming.SwimmingHooks.isSwimming(
            (net.minecraft.entity.player.EntityPlayer) view);
    if (minecraft.gameSettings.thirdPersonView != 0 || swimming) {
      // Swimming changes the camera's eye height as well as the F5 transform.
      // Both modes must use the same transform that renders this frame, not
      // a second eye-position formula built from the player coordinates.
      ((Buffer) MODELVIEW).clear();
      GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
      ((Buffer) MODELVIEW).rewind();
      Matrix4f matrix = new Matrix4f();
      matrix.load(MODELVIEW);
      Matrix4f inverse = Matrix4f.invert(matrix, null);
      if (inverse != null) {
        Vector4f origin = Matrix4f.transform(inverse,
            new Vector4f(0.0F, 0.0F, 0.0F, 1.0F), null);
        if (Math.abs(origin.w) > 0.0001F) {
          double x = view.prevPosX + (view.posX - view.prevPosX) * partialTicks;
          double y = view.prevPosY + (view.posY - view.prevPosY) * partialTicks;
          double z = view.prevPosZ + (view.posZ - view.prevPosZ) * partialTicks;
          x += origin.x / origin.w;
          y += origin.y / origin.w;
          z += origin.z / origin.w;
          if (swimming && minecraft.gameSettings.thirdPersonView == 0) {
            // orientCamera moves the first-person projection 0.1 block
            // backward. Fluid state belongs to the eye, not that screen
            // offset, which otherwise changes the answer with pitch.
            Vec3 look = ((EntityLivingBase) view).getLook(partialTicks);
            x += look.xCoord * 0.1D;
            y += look.yCoord * 0.1D;
            z += look.zCoord * 0.1D;
          }
          capturedCamera = Vec3.createVectorHelper(x, y, z);
        }
      }
    }
    if (capturedCamera == null) {
      double x = view.prevPosX + (view.posX - view.prevPosX) * partialTicks;
      double y = view.prevPosY + (view.posY - view.prevPosY) * partialTicks
          - view.yOffset + cameraEyeHeight(view);
      double z = view.prevPosZ + (view.posZ - view.prevPosZ) * partialTicks;
      capturedCamera = Vec3.createVectorHelper(x, y, z);
    }
    capturedEntity = view;
  }

  private static float cameraEyeHeight(Entity view) {
    return view instanceof net.minecraft.entity.player.EntityPlayer
        && ru.givler.mbo.swimming.SwimmingHooks.isSwimming(
            (net.minecraft.entity.player.EntityPlayer) view) ? 0.4F : 1.62F;
  }

  public static Block getViewBlock(World world, EntityLivingBase entity, float partialTicks) {
    Vec3 camera = capturedEntity == entity && capturedCamera != null
        ? capturedCamera : Vec3.createVectorHelper(
            entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
            entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks
                - entity.yOffset + cameraEyeHeight(entity),
            entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
    double x = camera.xCoord;
    double y = camera.yCoord;
    double z = camera.zCoord;
    int blockX = MathHelper.floor_double(x);
    int blockY = MathHelper.floor_double(y);
    int blockZ = MathHelper.floor_double(z);
    boolean waterAbove = world.getBlock(blockX, blockY + 1, blockZ).getMaterial() == Material.water
        || WaterloggingEntityHooks.isInsideWaterloggedAt(entity, x, blockY + 1.001D, z);
    if (WaterloggingEntityHooks.isInsideWaterloggedAt(entity, x, y, z)
        && isBelowWaterSurface(y, blockY, 0, waterAbove)) return Blocks.water;
    Block block = world.getBlock(blockX, blockY, blockZ);
    if (block.getMaterial() == Material.water
        && !isBelowWaterSurface(y, blockY,
            world.getBlockMetadata(blockX, blockY, blockZ), waterAbove))
      return world.getBlock(blockX, blockY + 1, blockZ);
    return block;
  }

  /** The rendered source surface is 8/9 of a block high, not a full block. */
  public static boolean isBelowWaterSurface(double cameraY, int blockY, int metadata,
      boolean waterAbove) {
    return waterAbove || cameraY < blockY + 1.0D - BlockLiquid.getLiquidHeightPercent(metadata);
  }

  public static boolean isCameraInsideWater(Entity entity, Material material, float partialTicks) {
    if (material != Material.water || !(entity instanceof EntityLivingBase))
      return entity.isInsideOfMaterial(material);
    // ItemRenderer's legacy full-screen water texture is a second, unrelated
    // colour pass. It can tint the air at the surface and makes F1 look different.
    // The camera fog is the sole underwater colour treatment.
    return false;
  }

  /** Near the surface the real sky remains visible through the water plane. */
  public static boolean shouldRenderSky(float partialTicks) {
    Minecraft minecraft = Minecraft.getMinecraft();
    Entity view = minecraft.renderViewEntity;
    if (!(view instanceof EntityLivingBase) || minecraft.theWorld == null) return true;
    if (getViewBlock(minecraft.theWorld, (EntityLivingBase) view, partialTicks)
        .getMaterial() != Material.water) return true;
    if (capturedEntity != view || capturedCamera == null) return false;
    int x = MathHelper.floor_double(capturedCamera.xCoord);
    int y = MathHelper.floor_double(capturedCamera.yCoord);
    int z = MathHelper.floor_double(capturedCamera.zCoord);
    Block block = minecraft.theWorld.getBlock(x, y, z);
    if (minecraft.theWorld.getBlock(x, y + 1, z).getMaterial() == Material.water
        || WaterloggingEntityHooks.isInsideWaterloggedAt(view,
            capturedCamera.xCoord, y + 1.001D, capturedCamera.zCoord)) return false;
    int metadata = block.getMaterial() == Material.water
        ? minecraft.theWorld.getBlockMetadata(x, y, z) : 0;
    double surface = y + 1.0D - BlockLiquid.getLiquidHeightPercent(metadata);
    return surface - capturedCamera.yCoord < 0.75D;
  }

  /** Keep vanilla's lower sky box out of view while surfacing from a swim. */
  public static double swimmingSkyHorizon(double horizon, float partialTicks) {
    Minecraft minecraft = Minecraft.getMinecraft();
    Entity view = minecraft.renderViewEntity;
    if (!(view instanceof net.minecraft.entity.player.EntityPlayer)
        || !ru.givler.mbo.swimming.SwimmingHooks.isSwimming(
            (net.minecraft.entity.player.EntityPlayer) view)) return horizon;
    // The lower vanilla sky list is finite and un-fogged. Place it beyond the
    // projection far plane while the player's low swimming viewpoint is active.
    return view.prevPosY + (view.posY - view.prevPosY) * partialTicks - 10000.0D;
  }
}
