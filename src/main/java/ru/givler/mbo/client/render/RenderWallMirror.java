package ru.givler.mbo.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Original 32x32 front/back atlas mapped to a one-pixel-deep hanging, without GeckoLib. */
public final class RenderWallMirror extends TileEntitySpecialRenderer {
    private static final ResourceLocation TEXTURE=new ResourceLocation("mbo","textures/models/decor/mirror.png");
    private static final ResourceLocation SILVER_TEXTURE=new ResourceLocation("mbo","textures/models/decor/mirror_silver.png");
    static ResourceLocation texture(boolean silver) { return silver ? SILVER_TEXTURE : TEXTURE; }

    @Override public void renderTileEntityAt(TileEntity tile,double x,double y,double z,float tick) {
        if((tile.getBlockMetadata()&4)!=0)return;
        renderStaticTile(tile,x,y,z);
        if (MirrorRenderer.isRenderingReflection()) return;
        if (tile instanceof ru.givler.mbo.tileentity.TileSilverMirror) {
            MirrorRenderer.INSTANCE.renderLinkedView(tile,null,x,y,z,tick);
        } else {
            MirrorRenderer.INSTANCE.renderMirror(tile,x,y,z,tick);
        }
    }
    public static void renderStaticTile(TileEntity tile,double x,double y,double z) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(texture(tile instanceof ru.givler.mbo.tileentity.TileSilverMirror));
        renderPanel(x,y,z,tile.getBlockMetadata()&3,false,tile instanceof ru.givler.mbo.tileentity.TileSilverMirror);
    }
    public static void renderPanel(double x,double y,double z,int facing) {
        renderPanel(x,y,z,facing,false);
    }
    public static void renderPanel(double x,double y,double z,int facing,boolean shadedItem) {
        renderPanel(x,y,z,facing,shadedItem,false);
    }
    public static void renderPanel(double x,double y,double z,int facing,boolean shadedItem,boolean silver) {
        double uScale = silver ? 1.0/3 : .5;
        int program=MirrorRenderer.pauseShader();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try {
            GL11.glTranslated(x+.5,y,z+.5);GL11.glRotatef(-90*facing,0,1,0);
            GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDisable(GL11.GL_BLEND);GL11.glColor4f(1,1,1,1);
            // Four untouched strips of the original PNG. Front is exactly 1/16 from the wall.
            rect(-.5,0,.5,.125,-.4375,0,1,1,.9375,uScale);
            rect(-.5,1.875,.5,2,-.4375,0,.0625,1,0,uScale);
            rect(-.5,.125,-.375,1.875,-.4375,0,.9375,.125,.0625,uScale);
            rect(.375,.125,.5,1.875,-.4375,.875,.9375,1,.0625,uScale);
            if(shadedItem)GL11.glColor4f(.8F,.8F,.8F,1);
            rect(-.5,0,.5,2,-.5,2,1,1,0,uScale);
            // Edge strips close the one-pixel thickness.
            if(shadedItem)GL11.glColor4f(.6F,.6F,.6F,1);
            GL11.glPushMatrix();GL11.glTranslated(-.5,0,-.5);GL11.glRotatef(-90,0,1,0);
            rect(0,0,.0625,2,0,0,1,.0625,0,uScale);GL11.glPopMatrix();
            GL11.glPushMatrix();GL11.glTranslated(.5,0,-.5);GL11.glRotatef(-90,0,1,0);
            rect(0,0,.0625,2,0,.9375,1,1,0,uScale);GL11.glPopMatrix();
            if(shadedItem)GL11.glColor4f(.9F,.9F,.9F,1);
            GL11.glPushMatrix();GL11.glTranslated(0,2,-.5);GL11.glRotatef(90,1,0,0);
            rect(-.5,0,.5,.0625,0,0,0,1,.03125,uScale);GL11.glPopMatrix();
            if(shadedItem)GL11.glColor4f(.55F,.55F,.55F,1);
            GL11.glPushMatrix();GL11.glTranslated(0,0,-.5);GL11.glRotatef(90,1,0,0);
            rect(-.5,0,.5,.0625,0,0,.96875,1,1,uScale);GL11.glPopMatrix();
            // Original PNG remains visible whenever the live reflection is outside its range.
            GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glColor4f(1,1,1,1);
            double panel = silver && !shadedItem ? 2 : 0;
            rect(-.375,.125,.375,1.875,-.4376,panel+.125,.9375,panel+.875,.0625,uScale);
        } finally { GL11.glPopMatrix();GL11.glPopAttrib();MirrorRenderer.resumeShader(program); }
    }
    private static void rect(double x0,double y0,double x1,double y1,double z,double u0,double v0,double u1,double v1,double uScale) {
        // Existing U coordinates address the 16-pixel front; U=1..2 addresses the new back half.
        u0*=uScale;u1*=uScale;
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setNormal(0,0,1);
        t.addVertexWithUV(x0,y0,z,u0,v0);t.addVertexWithUV(x1,y0,z,u1,v0);
        t.addVertexWithUV(x1,y1,z,u1,v1);t.addVertexWithUV(x0,y1,z,u0,v1);t.draw();
    }
}
