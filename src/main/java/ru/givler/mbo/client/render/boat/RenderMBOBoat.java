package ru.givler.mbo.client.render.boat;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOChestBoat;
import ru.givler.mbo.item.ItemMBOBoat;

public class RenderMBOBoat extends Render {
    private static final java.util.List<WaterMask> waterMasks = new java.util.ArrayList<WaterMask>();
    private static int maskCount;
    private static final class WaterMask {
        RenderMBOBoat renderer; EntityMBOBoat boat;
        double x,y,z; float yaw,partial;
    }

    public static void beginWaterMasks() {
        if(MinecraftForgeClient.getRenderPass()==0)maskCount=0;
    }

    private static void queueWaterMask(RenderMBOBoat renderer,EntityMBOBoat boat,double x,double y,double z,float yaw,float partial) {
        if(MinecraftForgeClient.getRenderPass()!=0)return;
        if(maskCount==waterMasks.size())waterMasks.add(new WaterMask());
        WaterMask mask=waterMasks.get(maskCount++);
        mask.renderer=renderer;mask.boat=boat;mask.x=x;mask.y=y;mask.z=z;mask.yaw=yaw;mask.partial=partial;
    }

    public static void finishWaterMasks() {
        if(MinecraftForgeClient.getRenderPass()!=0 || maskCount==0)return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthFunc(GL11.GL_LEQUAL);GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            for(int i=0;i<maskCount;i++) {
                WaterMask mask=waterMasks.get(i);
                mask.renderer.renderWaterMask(mask.boat,mask.x,mask.y,mask.z,mask.yaw,mask.partial);
            }
        } finally {
            GL11.glPopAttrib();
            for(int i=0;i<maskCount;i++){waterMasks.get(i).renderer=null;waterMasks.get(i).boat=null;}
            maskCount=0;
        }
    }
    /**
     * В 1.7.10 прозрачная поверхность воды рисуется иначе, чем в версиях,
     * для которых сделана новая модель. Небольшая поправка не даёт воде
     * накладывать подводное затемнение на борта.
     */
    private final ModelMBOBoat model = new ModelMBOBoat();
    private final TileEntityChest chest = new TileEntityChest();

    public RenderMBOBoat() { shadowSize = 0.5F; }

    @Override
    public void doRender(Entity raw, double x, double y, double z, float yaw, float partial) {
        EntityMBOBoat boat = (EntityMBOBoat)raw;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.375F + boat.getVisualYOffset(), (float)z);
        GL11.glRotatef(180F - yaw, 0, 1, 0);
        float hit = boat.getTimeSinceHit() - partial;
        float damage = boat.getDamageTaken() - partial;
        if (damage < 0) damage = 0;
        if (hit > 0) GL11.glRotatef(MathHelper.sin(hit) * hit * damage / 10F
                * boat.getForwardDirection(), 1, 0, 0);
        GL11.glScalef(-1, -1, 1);
        bindEntityTexture(boat);
        applyBoatLight(boat, partial);
        GL11.glColor4f(1, 1, 1, 1);
        model.render(boat, partial, 0, -0.1F, 0, 0, 0.0625F);
        // setupRotation инвертирует X/Y для модели. Et Futurum отменяет
        // эту инверсию перед отрисовкой содержимого лодки.
        GL11.glScalef(-1, -1, 1);
        if (boat instanceof EntityMBOChestBoat) {
            GL11.glRotatef(180, 0, 1, 0);
            GL11.glScalef(0.8F, 0.8F, 0.8F);
            GL11.glTranslatef(-0.5F, -0.2F, -1.1F);
            TileEntityRendererDispatcher.instance.renderTileEntityAt(chest, 0, 0, 0, partial);
        }
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
        // All passengers must have drawn their color before this depth-only water mask.
        queueWaterMask(this,boat,x,y,z,yaw,partial);
    }

    private void applyBoatLight(EntityMBOBoat boat, float partial) {
        int light = boat.getBrightnessForRender(partial);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                light & 65535, light >>> 16);
    }

    private void renderWaterMask(EntityMBOBoat boat, double x, double y, double z,
                                 float yaw, float partial) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x,
                (float)y + 0.375F + boat.getVisualYOffset(), (float)z);
        GL11.glRotatef(180F - yaw, 0, 1, 0);
        float hit = boat.getTimeSinceHit() - partial;
        float damage = Math.max(0, boat.getDamageTaken() - partial);
        if (hit > 0) {
            GL11.glRotatef(MathHelper.sin(hit) * hit * damage / 10F
                    * boat.getForwardDirection(), 1, 0, 0);
        }
        GL11.glScalef(-1, -1, 1);
        model.renderNoWater(0.0625F);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        int type = ((EntityMBOBoat)entity).getBoatType();
        if (type < 0 || type >= ItemMBOBoat.NAMES.length) type = 0;
        return new ResourceLocation("mbo:textures/entity/boat/" + ItemMBOBoat.NAMES[type] + ".png");
    }
}
