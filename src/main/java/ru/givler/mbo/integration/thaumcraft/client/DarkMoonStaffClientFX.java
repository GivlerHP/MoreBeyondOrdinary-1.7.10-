package ru.givler.mbo.integration.thaumcraft.client;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSparkle;

@SideOnly(Side.CLIENT)
public final class DarkMoonStaffClientFX {

    private DarkMoonStaffClientFX() {
    }

    private static final double ORBIT_RADIUS = 1.3D;
    private static final double ORBIT_ROTATION_SPEED = 0.15D;

    /**
     * Рисует кружащиеся сферы. Вызывать только когда player.worldObj.isRemote == true.
     */
    @SideOnly(Side.CLIENT)
    public static void renderOrbitingSpheres(EntityPlayer player, int ticksUsed, int visibleSpheresRaw) {
        int visibleSpheres = Math.max(1, visibleSpheresRaw);

        double baseAngle = ticksUsed * ORBIT_ROTATION_SPEED;

        for (int i = 0; i < visibleSpheres; ++i) {
            double angle = baseAngle + (2.0D * Math.PI * i / visibleSpheres);
            double ox = Math.cos(angle) * ORBIT_RADIUS;
            double oz = Math.sin(angle) * ORBIT_RADIUS;

            double px = player.posX + ox;
            double py = player.posY + player.getEyeHeight() - 0.2D;
            double pz = player.posZ + oz;

            FXSparkle fx = new FXSparkle(player.worldObj, px, py, pz, 1.2F, 0, 5);
            fx.setGravity(0.0F);
            ParticleEngine.instance.addEffect(player.worldObj, fx);
        }
    }
}