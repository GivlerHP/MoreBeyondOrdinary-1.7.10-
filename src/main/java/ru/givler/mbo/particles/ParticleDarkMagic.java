package ru.givler.mbo.particles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

@SideOnly(Side.CLIENT)
public class ParticleDarkMagic extends EntityFX {

    public static final String NAME = MoreBeyondOrdinary.MODID + "darkmagic";

    private int baseSpellTextureIndex = 128;

    public ParticleDarkMagic(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, float r, float g, float b) {
        super(par1World, par2, par4, par6, par8, par10, par12);
        this.motionY *= 0.20000000298023224D;

        // Устанавливаем цвет
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;

        this.particleScale *= 0.75F;
        this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
        this.noClip = true;
    }

    @Override
    public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7) {
        float f6 = ((float)this.particleAge + par2) / (float)this.particleMaxAge * 32.0F;

        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f6 > 1.0F) {
            f6 = 1.0F;
        }

        super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }

        this.setParticleTextureIndex(this.baseSpellTextureIndex + (7 - this.particleAge * 8 / this.particleMaxAge));
        this.motionY += 0.004D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }

    public void setBaseSpellTextureIndex(int par1) {
        this.baseSpellTextureIndex = par1;
    }
}
