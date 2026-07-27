package ru.givler.mbo.render.boat;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOChestBoat;

public class RenderMBOBoat extends Render {
    /**
     * В 1.7.10 прозрачная поверхность воды рисуется иначе, чем в версиях,
     * для которых сделана новая модель. Небольшая поправка не даёт воде
     * накладывать подводное затемнение на борта.
     */
    private static final String[] VANILLA_TEXTURES =
            {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static final ResourceLocation BOP_PLACEHOLDER =
            new ResourceLocation("mbo:textures/entity/boat/oak.png");
    private final ModelMBOBoat model = new ModelMBOBoat();
    private final TileEntityChest chest = new TileEntityChest();

    public RenderMBOBoat() { shadowSize = 0.5F; }

    @Override
    public void doRender(Entity raw, double x, double y, double z, float yaw, float partial) {
        EntityMBOBoat boat = (EntityMBOBoat)raw;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.375F + EntityMBOBoat.VISUAL_Y_OFFSET, (float)z);
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
        renderWaterMask(boat, x, y, z, yaw, partial);
    }

    private void applyBoatLight(EntityMBOBoat boat, float partial) {
        int light = boat.getBrightnessForRender(partial);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                light & 65535, light >>> 16);
    }

    private void renderWaterMask(EntityMBOBoat boat, double x, double y, double z,
                                 float yaw, float partial) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.375F + EntityMBOBoat.VISUAL_Y_OFFSET, (float)z);
        GL11.glRotatef(180F - yaw, 0, 1, 0);
        float hit = boat.getTimeSinceHit() - partial;
        float damage = Math.max(0, boat.getDamageTaken() - partial);
        if (hit > 0) GL11.glRotatef(MathHelper.sin(hit) * hit * damage / 10F
                * boat.getForwardDirection(), 1, 0, 0);
        GL11.glScalef(-1, -1, 1);
        bindEntityTexture(boat);
        applyBoatLight(boat, partial);
        model.renderNoWater(0.0625F);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        int type = ((EntityMBOBoat)entity).getBoatType();
        return type < VANILLA_TEXTURES.length
                ? new ResourceLocation("mbo:textures/entity/boat/" + VANILLA_TEXTURES[type] + ".png")
                : BOP_PLACEHOLDER;
    }
}
