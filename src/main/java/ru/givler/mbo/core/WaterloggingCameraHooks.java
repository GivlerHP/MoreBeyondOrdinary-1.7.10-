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
public final class WaterloggingCameraHooks {
  private static Entity pendingEntity;
  private static Vec3 pendingCamera;
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
    if (minecraft.gameSettings.thirdPersonView != 0 && pendingEntity == view)
      capturedCamera = pendingCamera;
    if (capturedCamera == null) {
      double x = view.prevPosX + (view.posX - view.prevPosX) * partialTicks;
      double y = interpolatedEyeY(view, partialTicks);
      double z = view.prevPosZ + (view.posZ - view.prevPosZ) * partialTicks;
      capturedCamera = Vec3.createVectorHelper(x, y, z);
    }
    capturedEntity = view;
  }

  /** Reset at orientCamera entry so a camera override cannot reuse last frame. */
  public static void beginCameraPosition() { pendingEntity=null; pendingCamera=null; }

  /** Distance comes from orientCamera after its eight collision rays have clipped it. */
  public static void thirdPersonDistance(double distance,float partialTicks) {
    Minecraft mc=Minecraft.getMinecraft();
    Entity view=mc.renderViewEntity;
    if(view==null || mc.gameSettings.thirdPersonView==0)return;
    float yaw=view.prevRotationYaw+(view.rotationYaw-view.prevRotationYaw)*partialTicks;
    float pitch=view.prevRotationPitch+(view.rotationPitch-view.prevRotationPitch)*partialTicks;
    if(mc.gameSettings.thirdPersonView==2)pitch+=180F;
    pendingCamera=thirdPersonPosition(view,partialTicks,distance,yaw,pitch);
    pendingEntity=view;
  }

  public static Vec3 thirdPersonPosition(Entity view,float partialTicks,double distance,float yaw,float pitch) {
    double yr=Math.toRadians(yaw),pr=Math.toRadians(pitch);
    double horizontal=Math.cos(pr)*distance;
    return Vec3.createVectorHelper(
        view.prevPosX+(view.posX-view.prevPosX)*partialTicks+Math.sin(yr)*horizontal,
        interpolatedEyeY(view,partialTicks)+Math.sin(pr)*distance,
        view.prevPosZ+(view.posZ-view.prevPosZ)*partialTicks-Math.cos(yr)*horizontal);
  }

  private static float cameraEyeHeight(Entity view) {
    return view instanceof net.minecraft.entity.player.EntityPlayer
        && ru.givler.mbo.swimming.SwimmingHooks.isSwimming(
            (net.minecraft.entity.player.EntityPlayer) view) ? 0.4F : 1.62F;
  }

  private static double interpolatedEyeY(Entity view, float partialTicks) {
    double y = view.prevPosY + (view.posY - view.prevPosY) * partialTicks;
    if (view instanceof net.minecraft.entity.player.EntityPlayer
        && ru.givler.mbo.swimming.SwimmingHooks.isSwimming((net.minecraft.entity.player.EntityPlayer) view))
      return y + ru.givler.mbo.swimming.SwimmingHooks.swimmingEyeY(
          (net.minecraft.entity.player.EntityPlayer) view) - view.posY;
    return y - view.yOffset + cameraEyeHeight(view);
  }

  public static Block getViewBlock(World world, EntityLivingBase entity, float partialTicks) {
    Vec3 camera = capturedEntity == entity && capturedCamera != null
        ? capturedCamera : Vec3.createVectorHelper(
            entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
            interpolatedEyeY(entity, partialTicks),
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
