package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.editor.BuilderAccess;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

/** Shows every moving platform while an authorized builder holds its editor. */
public final class PlatformTechnicalRenderer {
    @SubscribeEvent public void render(RenderWorldLastEvent event){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.theWorld==null||mc.renderViewEntity==null)return;ItemStack held=mc.thePlayer.getCurrentEquippedItem();if(held==null||!(held.getItem() instanceof ItemPlatformEditor)||!BuilderAccess.canEdit(mc.thePlayer))return;double cx=mc.renderViewEntity.lastTickPosX+(mc.renderViewEntity.posX-mc.renderViewEntity.lastTickPosX)*event.partialTicks,cy=mc.renderViewEntity.lastTickPosY+(mc.renderViewEntity.posY-mc.renderViewEntity.lastTickPosY)*event.partialTicks,cz=mc.renderViewEntity.lastTickPosZ+(mc.renderViewEntity.posZ-mc.renderViewEntity.lastTickPosZ)*event.partialTicks;for(Object value:mc.theWorld.loadedEntityList)if(value instanceof EntityMovingPlatform){EntityMovingPlatform p=(EntityMovingPlatform)value;if(p.isDead)continue;double x=p.prevPosX+(p.posX-p.prevPosX)*event.partialTicks-cx,y=p.prevPosY+(p.posY-p.prevPosY)*event.partialTicks-cy,z=p.prevPosZ+(p.posZ-p.prevPosZ)*event.partialTicks-cz;outline(x-.002D,y-.002D,z-.002D,x+p.getSizeX()+.002D,y+p.getSizeY()+.002D,z+p.getSizeZ()+.002D);}}
    private static void outline(double x,double y,double z,double X,double Y,double Z){GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_LINE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_DEPTH_BUFFER_BIT);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glLineWidth(2F);GL11.glColor4f(.15F,1F,.35F,.9F);Tessellator t=Tessellator.instance;t.startDrawing(GL11.GL_LINES);edge(t,x,y,z,X,y,z);edge(t,X,y,z,X,Y,z);edge(t,X,Y,z,x,Y,z);edge(t,x,Y,z,x,y,z);edge(t,x,y,Z,X,y,Z);edge(t,X,y,Z,X,Y,Z);edge(t,X,Y,Z,x,Y,Z);edge(t,x,Y,Z,x,y,Z);edge(t,x,y,z,x,y,Z);edge(t,X,y,z,X,y,Z);edge(t,X,Y,z,X,Y,Z);edge(t,x,Y,z,x,Y,Z);t.draw();GL11.glPopAttrib();}
    private static void edge(Tessellator t,double x,double y,double z,double X,double Y,double Z){t.addVertex(x,y,z);t.addVertex(X,Y,Z);}
}
