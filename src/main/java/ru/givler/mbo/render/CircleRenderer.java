package ru.givler.mbo.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.MoreBeyondOrdinary;

import java.io.InputStream;

public class CircleRenderer extends TileEntitySpecialRenderer {
    private final ResourceLocation texture = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/blocks/exorcism_circle.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();

        GL11.glTranslated(x - 1, y + 0.02, z - 1);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindTexture(texture);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        t.addVertexWithUV(0, 0, 3, 0, 1); // нижний левый угол
        t.addVertexWithUV(3, 0, 3, 1, 1); // нижний правый
        t.addVertexWithUV(3, 0, 0, 1, 0); // верхний правый
        t.addVertexWithUV(0, 0, 0, 0, 0); // верхний левый

        t.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}