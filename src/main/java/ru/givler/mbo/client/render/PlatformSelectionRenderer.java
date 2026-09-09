package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

public final class PlatformSelectionRenderer {
    @SubscribeEvent public void render(RenderWorldLastEvent event){
        Minecraft mc=Minecraft.getMinecraft();EntityPlayer p=mc.thePlayer;if(p==null)return;ItemStack held=p.getCurrentEquippedItem();
        if(held==null||held.getItem()!=ItemRegistry.PlatformEditor)return;
        int[] a=ItemPlatformEditor.getPoint(held,"Pos1",p.dimension),b=ItemPlatformEditor.getPoint(held,"Pos2",p.dimension);
        java.util.UUID linked=ItemPlatformEditor.getLinkedPlatform(held);EntityMovingPlatform selected=null;
        if(linked!=null)for(Object o:mc.theWorld.loadedEntityList)if(o instanceof EntityMovingPlatform&&linked.equals(((EntityMovingPlatform)o).getPlatformId())){selected=(EntityMovingPlatform)o;break;}
        if(selected==null&&linked!=null)return;if(selected==null&&(a==null||b==null))return;
        double px=p.lastTickPosX+(p.posX-p.lastTickPosX)*event.partialTicks,py=p.lastTickPosY+(p.posY-p.lastTickPosY)*event.partialTicks,pz=p.lastTickPosZ+(p.posZ-p.lastTickPosZ)*event.partialTicks;
        double sx=selected==null?0:selected.prevPosX+(selected.posX-selected.prevPosX)*event.partialTicks;
        double sy=selected==null?0:selected.prevPosY+(selected.posY-selected.prevPosY)*event.partialTicks;
        double sz=selected==null?0:selected.prevPosZ+(selected.posZ-selected.prevPosZ)*event.partialTicks;
        double x0=selected!=null?sx-px:Math.min(a[0],b[0])-px,y0=selected!=null?sy-py:Math.min(a[1],b[1])-py,z0=selected!=null?sz-pz:Math.min(a[2],b[2])-pz;
        double x1=selected!=null?sx+selected.getSizeX()-px:Math.max(a[0],b[0])+1-px,y1=selected!=null?sy+selected.getSizeY()-py:Math.max(a[1],b[1])+1-py,z1=selected!=null?sz+selected.getSizeZ()-pz:Math.max(a[2],b[2])+1-pz;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_LINE_BIT|GL11.GL_CURRENT_BIT);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glColor4f(.2F,.8F,1F,.9F);GL11.glLineWidth(2F);
        Tessellator t=Tessellator.instance;t.startDrawing(GL11.GL_LINES);edge(t,x0,y0,z0,x1,y0,z0);edge(t,x0,y1,z0,x1,y1,z0);edge(t,x0,y0,z1,x1,y0,z1);edge(t,x0,y1,z1,x1,y1,z1);edge(t,x0,y0,z0,x0,y1,z0);edge(t,x1,y0,z0,x1,y1,z0);edge(t,x0,y0,z1,x0,y1,z1);edge(t,x1,y0,z1,x1,y1,z1);edge(t,x0,y0,z0,x0,y0,z1);edge(t,x1,y0,z0,x1,y0,z1);edge(t,x0,y1,z0,x0,y1,z1);edge(t,x1,y1,z0,x1,y1,z1);t.draw();GL11.glPopAttrib();
    }
    private static void edge(Tessellator t,double a,double b,double c,double d,double e,double f){t.addVertex(a,b,c);t.addVertex(d,e,f);}
}
