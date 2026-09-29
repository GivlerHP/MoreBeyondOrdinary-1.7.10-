package ru.givler.mbo.particles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/** Large, softly fading smoke using the twelve Campfire Backport smoke variants. */
@SideOnly(Side.CLIENT)
public final class ParticleCampfireSmoke extends EntityFX {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[12];
    static {
        for (int i = 0; i < TEXTURES.length; i++)
            TEXTURES[i] = new ResourceLocation("mbo", "textures/particle/campfire/big_smoke_" + i + ".png");
    }

    private final ResourceLocation texture;

    public ParticleCampfireSmoke(World world, int x, int y, int z) {
        super(world, x + .5, y + .5, z + .5);
        setPosition(x + .5 + (rand.nextDouble() - .5) * .67,
                y + rand.nextDouble() + rand.nextDouble(),
                z + .5 + (rand.nextDouble() - .5) * .67);
        particleScale = 6F * (.5F + rand.nextFloat() * .5F);
        particleMaxAge = 80 + rand.nextInt(50);
        particleAlpha = .9F;
        motionY = .075 + rand.nextFloat() / 500F;
        particleGravity = .000003F;
        setSize(.25F, .25F);
        noClip = false;
        texture = TEXTURES[rand.nextInt(TEXTURES.length)];
    }

    @Override public int getFXLayer() { return 3; }

    @Override public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge || particleAlpha <= 0) {
            setDead();
            return;
        }
        motionX += (rand.nextFloat() - .5F) / 2500F;
        motionZ += (rand.nextFloat() - .5F) / 2500F;
        motionY -= particleGravity;
        moveEntity(motionX, motionY, motionZ);
        if (particleMaxAge - particleAge < 60) particleAlpha = Math.max(0, particleAlpha - .015F);
    }

    @Override public void renderParticle(Tessellator buffer, float partialTicks,
                                          float rotationX, float rotationXZ, float rotationZ,
                                          float rotationYZ, float rotationXY) {
        Entity camera = Minecraft.getMinecraft().renderViewEntity;
        if (camera == null) return;
        double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
        float px = (float) (prevPosX + (posX - prevPosX) * partialTicks - cx);
        float py = (float) (prevPosY + (posY - prevPosY) * partialTicks - cy);
        float pz = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - cz);
        float size = .1F * particleScale;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        buffer.startDrawingQuads();
        buffer.setColorRGBA_F(1, 1, 1, particleAlpha);
        buffer.setBrightness(0xE000E0);
        buffer.addVertexWithUV(px-rotationX*size-rotationYZ*size, py-rotationXZ*size,
                pz-rotationZ*size-rotationXY*size, 1, 1);
        buffer.addVertexWithUV(px-rotationX*size+rotationYZ*size, py+rotationXZ*size,
                pz-rotationZ*size+rotationXY*size, 1, 0);
        buffer.addVertexWithUV(px+rotationX*size+rotationYZ*size, py+rotationXZ*size,
                pz+rotationZ*size+rotationXY*size, 0, 0);
        buffer.addVertexWithUV(px+rotationX*size-rotationYZ*size, py-rotationXZ*size,
                pz+rotationZ*size-rotationXY*size, 0, 1);
        buffer.draw();
        GL11.glPopAttrib();
    }
}
