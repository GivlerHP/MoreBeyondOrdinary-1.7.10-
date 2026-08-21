package ru.givler.mbo.client.particle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Port of the tinted falling-leaf particle used by modern vanilla clients. */
@SideOnly(Side.CLIENT)
public final class ParticleFallingLeaf extends EntityFX {
    private static final int VANILLA_LIFETIME = 300;
    private static final double SPEED_SCALE = 0.0025D;

    private float angularVelocity;
    private final float angularAcceleration;
    private float angle;
    private float previousAngle;
    private final double driftX;
    private final double driftZ;

    public ParticleFallingLeaf(World world, double x, double y, double z, IIcon icon, int color) {
        super(world, x, y, z, 0D, 0D, 0D);
        setParticleIcon(icon);
        particleMaxAge = VANILLA_LIFETIME;
        particleScale = rand.nextBoolean() ? 1.0F : 1.5F;
        particleRed = ((color >> 16) & 255) / 255F;
        particleGreen = ((color >> 8) & 255) / 255F;
        particleBlue = (color & 255) / 255F;
        particleAlpha = 1F;
        motionY = -0.021D;
        noClip = false;

        angularVelocity = (float)Math.toRadians(rand.nextBoolean() ? -30D : 30D);
        angularAcceleration = (float)Math.toRadians(rand.nextBoolean() ? -5D : 5D);
        angle = angularVelocity;
        previousAngle = angle;

        // Vanilla chooses a random drift heading and a strength of 10.
        float heading = rand.nextFloat() * 60F;
        driftX = Math.cos(Math.toRadians(heading)) * 10D;
        driftZ = Math.sin(Math.toRadians(heading)) * 10D;
    }

    @Override
    public int getFXLayer() { return 1; }

    @Override
    public void renderParticle(Tessellator t, float partialTicks, float cameraX, float cameraXZ,
                               float cameraZ, float cameraYZ, float cameraXY) {
        float size = 0.1F * particleScale;
        float currentAngle = previousAngle + (angle - previousAngle) * partialTicks;
        float sin = (float)Math.sin(currentAngle);
        float cos = (float)Math.cos(currentAngle);
        double x = prevPosX + (posX - prevPosX) * partialTicks - interpPosX;
        double y = prevPosY + (posY - prevPosY) * partialTicks - interpPosY;
        double z = prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ;
        double minU = particleIcon.getMinU(), maxU = particleIcon.getMaxU();
        double minV = particleIcon.getMinV(), maxV = particleIcon.getMaxV();
        int brightness = getBrightnessForRender(partialTicks);
        t.setBrightness(brightness);
        t.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
        vertex(t,x,y,z,-1,-1,sin,cos,size,cameraX,cameraXZ,cameraZ,cameraYZ,cameraXY,maxU,maxV);
        vertex(t,x,y,z,-1, 1,sin,cos,size,cameraX,cameraXZ,cameraZ,cameraYZ,cameraXY,maxU,minV);
        vertex(t,x,y,z, 1, 1,sin,cos,size,cameraX,cameraXZ,cameraZ,cameraYZ,cameraXY,minU,minV);
        vertex(t,x,y,z, 1,-1,sin,cos,size,cameraX,cameraXZ,cameraZ,cameraYZ,cameraXY,minU,maxV);
    }

    private static void vertex(Tessellator t,double x,double y,double z,float cx,float cy,float sin,float cos,float size,
                               float cameraX,float cameraXZ,float cameraZ,float cameraYZ,float cameraXY,double u,double v){
        float rx=cx*cos-cy*sin, ry=cx*sin+cy*cos;
        t.addVertexWithUV(x+(cameraX*rx+cameraYZ*ry)*size,
                y+cameraXZ*ry*size,
                z+(cameraZ*rx+cameraXY*ry)*size,u,v);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        previousAngle = angle;
        if (particleAge++ >= particleMaxAge) {
            setDead();
            return;
        }

        float progress = Math.min(particleAge / 300F, 1F);
        double drift = Math.pow(progress, 1.25D) * SPEED_SCALE;
        motionX += driftX * drift;
        motionZ += driftZ * drift;
        motionY -= 0.07D * 1.2D * SPEED_SCALE;

        angularVelocity += angularAcceleration / 20F;
        angle += angularVelocity / 20F;
        moveEntity(motionX, motionY, motionZ);

        // Modern vanilla removes a leaf as soon as it lands or its horizontal
        // movement is blocked, rather than leaving it lying on the ground.
        if (onGround || (particleAge > 1 && motionX == 0D && motionZ == 0D)) setDead();
    }
}
