package ru.givler.mbo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Layout reads editable GUI artwork from one resource-pack-overridable sprite sheet. */
public final class DescriptionWorkbenchSkin {
    public static final ResourceLocation TEXTURE = new ResourceLocation("mbo", "textures/gui/description_workbench.png");
    private DescriptionWorkbenchSkin() {}
    public static void background(int x, int y, int w, int h) { nine(x,y,w,h,0,0,64,64,7); }
    public static void panel(int x, int y, int w, int h) { nine(x,y,w,h,64,0,64,64,4); }
    public static void field(int x, int y, int w, int h, boolean active) { nine(x,y,w,h,active?64:0,64,64,16,2); }
    public static void inputSlot(int x, int y) { part(x,y,18,18,170,160,18,18); }
    public static void inventory(int x, int y) { part(x,y,162,76,0,160,162,76); }
    public static void swatch(int x, int y, int index) { part(x,y,8,10,index*8,120,8,10); }
    public static void tab(int x,int y,boolean selected) { nine(x,y,28,22,144,0,28,22,3); }
    public static void scrollTrack(int x, int y, int height) {
        part(x, y, 14, height, 242, 0, 14, 112);
    }
    public static void scrollThumb(int x, int y, boolean dragging) {
        part(x, y, 12, 15, dragging ? 230 : 218, 0, 12, 15);
    }
    /** Fixed-size corners; edges and centre repeat native pixels, with partial final tiles. */
    private static void nine(int x, int y, int w, int h, int u, int v, int sw, int sh, int border) {
        int[] xs={x,x+border,x+w-border,x+w}, ys={y,y+border,y+h-border,y+h};
        int[] us={u,u+border,u+sw-border,u+sw}, vs={v,v+border,v+sh-border,v+sh};
        bind();
        Tessellator t=Tessellator.instance; t.startDrawingQuads();
        for(int row=0;row<3;row++) for(int col=0;col<3;col++) {
            int sourceW=us[col+1]-us[col], sourceH=vs[row+1]-vs[row];
            for(int py=ys[row];py<ys[row+1];py+=sourceH)
                for(int px=xs[col];px<xs[col+1];px+=sourceW) {
                    int tileW=Math.min(sourceW,xs[col+1]-px), tileH=Math.min(sourceH,ys[row+1]-py);
                    quad(t,px,py,tileW,tileH,us[col],vs[row],tileW,tileH);
                }
        }
        t.draw();
    }
    private static void bind() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1,1,1,1);
    }
    private static void part(int x,int y,int w,int h,int u,int v,int sw,int sh) {
        bind();
        Tessellator t=Tessellator.instance; t.startDrawingQuads();
        quad(t,x,y,w,h,u,v,sw,sh); t.draw();
    }
    private static void quad(Tessellator t,int x,int y,int w,int h,int u,int v,int sw,int sh) {
        t.addVertexWithUV(x,y+h,0,u/256D,(v+sh)/256D);
        t.addVertexWithUV(x+w,y+h,0,(u+sw)/256D,(v+sh)/256D);
        t.addVertexWithUV(x+w,y,0,(u+sw)/256D,v/256D);
        t.addVertexWithUV(x,y,0,u/256D,v/256D);
    }
}
