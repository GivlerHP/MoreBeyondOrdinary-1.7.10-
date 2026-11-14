package ru.givler.mbo.integration.thaumcraft.client.render;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.client.lib.UtilsFX;

/** Vanilla two-handed map pose with a TC4 research puzzle on the parchment. */
public final class ResearchScrollRenderer implements IItemRenderer,IResourceManagerReloadListener {
    private static final ResourceLocation PAPER=new ResourceLocation("mbo","textures/map/scroll_empty.png");
    private static final ResourceLocation UNKNOWN=new ResourceLocation("thaumcraft","textures/aspects/_unknown.png");
    private final Map<ItemStack,Layout> layouts=new WeakHashMap<ItemStack,Layout>();
    private static final int INK=0x555555, WIDTH=192;
    private static final float TITLE_SCALE=1.5F;
    private static final class Layout {
        NBTTagCompound nbt; int damage; Object world;String locale,player;
        long expires;
        ResearchScrollContent content;
        List<String> title,body;
        int aspectRows,bodyY;
        thaumcraft.common.lib.research.ResearchNoteData puzzle;
    }
    @Override public boolean handleRenderType(ItemStack stack,ItemRenderType type) {
        return type==ItemRenderType.FIRST_PERSON_MAP || type==ItemRenderType.EQUIPPED || type==ItemRenderType.EQUIPPED_FIRST_PERSON;
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,ItemRendererHelper helper){return false;}
    @Override public void onResourceManagerReload(IResourceManager manager){layouts.clear();}
    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void unload(net.minecraftforge.event.world.WorldEvent.Unload event){if(event.world.isRemote)layouts.clear();}
    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void playerPre(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
        ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.beginPose(event.entityPlayer);
    }
    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void playerPost(net.minecraftforge.client.event.RenderPlayerEvent.Post event) {
        ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.endPose();
    }
    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void heldPre(net.minecraftforge.client.event.RenderPlayerEvent.Specials.Pre event) {
        if(ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.holding(event.entityPlayer))event.renderItem=false;
    }
    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void heldPost(net.minecraftforge.client.event.RenderPlayerEvent.Specials.Post event) {
        if(ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.holding(event.entityPlayer) && !event.entityPlayer.isInvisible())
            renderItem(ItemRenderType.EQUIPPED,event.entityPlayer.getHeldItem(),Boolean.TRUE);
    }
    private Layout layout(ItemStack stack) {
        Minecraft mc=Minecraft.getMinecraft();String player=mc.thePlayer==null?"":mc.thePlayer.getCommandSenderName();
        Layout saved=layouts.get(stack);long now=System.nanoTime();
        if(saved!=null && saved.world==mc.theWorld && saved.damage==stack.getItemDamage()
                && Objects.equals(saved.locale,mc.gameSettings.language) && saved.player.equals(player)
                && Objects.equals(saved.nbt,stack.getTagCompound()) && now<saved.expires)return saved;
        Layout next=new Layout();next.world=mc.theWorld;next.player=player;next.locale=mc.gameSettings.language;
        next.damage=stack.getItemDamage();next.nbt=stack.getTagCompound()==null?null:(NBTTagCompound)stack.getTagCompound().copy();
        next.expires=now+500000000L;
        next.content=ResearchScrollContent.read(stack,key->!player.isEmpty() && ThaumcraftApiHelper.isResearchComplete(player,key));
        FontRenderer normal=mc.fontRenderer,body=font(next.content.runes);
        next.title=fit(normal,next.content.title,(int)(WIDTH/TITLE_SCALE),2);
        next.puzzle=thaumcraft.common.lib.research.ResearchManager.getData(stack);
        next.aspectRows=next.puzzle!=null && !next.puzzle.hexes.isEmpty()?4:0;
        next.bodyY=40+(int)(next.title.size()*normal.FONT_HEIGHT*TITLE_SCALE)+next.aspectRows*22;
        next.body=fit(body,next.content.text,WIDTH,Math.max(1,(209-next.bodyY)/body.FONT_HEIGHT));
        layouts.put(stack,next);return next;
    }
    private static FontRenderer font(boolean runes) {
        Minecraft mc=Minecraft.getMinecraft();return runes && mc.standardGalacticFontRenderer!=null?mc.standardGalacticFontRenderer:mc.fontRenderer;
    }
    static List<String> fit(FontRenderer font,String text,int width,int maxLines) {
        if(text.length()>4096)text=text.substring(0,4096)+"...";
        List<String> all=font.listFormattedStringToWidth(text,width);
        List<String> lines=new ArrayList<String>(all.subList(0,Math.min(maxLines,all.size())));
        if(all.size()>maxLines && !lines.isEmpty()) {
            String last=lines.get(lines.size()-1);
            int sentence=Math.max(last.lastIndexOf('.'),Math.max(last.lastIndexOf('!'),last.lastIndexOf('?')));
            if(sentence>=last.length()/2)last=last.substring(0,sentence+1);
            else {last=font.trimStringToWidth(last,Math.max(1,width-font.getStringWidth("...")));int space=last.lastIndexOf(' ');if(space>last.length()/2)last=last.substring(0,space);last+="...";}
            lines.set(lines.size()-1,last);
        }
        return lines;
    }
    @Override public void renderItem(ItemRenderType type,ItemStack stack,Object... data) {
        if(stack==null)return;
        // Player sheets are drawn once in torso space by Specials.Post. Some player renderers
        // ignore Specials.Pre.renderItem; their ordinary hand pass must not draw a second sheet.
        if(type==ItemRenderType.EQUIPPED && !(data.length>0 && Boolean.TRUE.equals(data[0])))return;
        Minecraft mc=Minecraft.getMinecraft();Layout page=layout(stack);
        int previousMatrix=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        float lx=OpenGlHelper.lastBrightnessX,ly=OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
        try {
            // Both vanilla and MF2 deliver an item-local equipped transform. Never mirror text.
            if(type==ItemRenderType.FIRST_PERSON_MAP) {
                // Vanilla already drew our parchment over the map's -7..135 pixel square.
                GL11.glTranslatef(-7,-7,-.01F);GL11.glScalef(142F/256F,142F/256F,142F/256F);
            } else if(type==ItemRenderType.EQUIPPED && data.length>0 && Boolean.TRUE.equals(data[0])) {
                GL11.glTranslatef(0,.24F,-.64F);GL11.glRotatef(75,1,0,0);
                GL11.glScalef(.003F,.003F,.003F);GL11.glTranslatef(-128,-128,0);
            } else {
                GL11.glTranslatef(.1F,.3F,.2F);GL11.glRotatef(-90,0,1,0);
                GL11.glScalef(.0038F,-.0038F,.0038F);GL11.glTranslatef(-128,-128,0);
            }
            GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.05F);GL11.glColor4f(1,1,1,1);
            if(type!=ItemRenderType.FIRST_PERSON_MAP){mc.getTextureManager().bindTexture(PAPER);quad(0,0,256,256,0);}
            GL11.glTranslatef(0,0,type==ItemRenderType.FIRST_PERSON_MAP?-.1F:.1F);
            FontRenderer normal=mc.fontRenderer;int y=29;
            GL11.glPushMatrix();
            GL11.glTranslatef(128,y,0);GL11.glScalef(TITLE_SCALE,TITLE_SCALE,1);
            for(String line:page.title){normal.drawString(line,-normal.getStringWidth(line)/2,0,INK);GL11.glTranslatef(0,normal.FONT_HEIGHT,0);}
            GL11.glPopMatrix();
            y+=(int)(page.title.size()*normal.FONT_HEIGHT*TITLE_SCALE);
            y+=8;
            drawPuzzle(page,32,y,192,80);
            GL11.glColor4f(1,1,1,1);
            FontRenderer body=font(page.content.runes);y=page.bodyY;
            for(String line:page.body){body.drawString(line,32,y,INK);y+=body.FONT_HEIGHT;}
            String status=StatCollector.translateToLocal(page.content.complete?"mbo.research_scroll.complete":"mbo.research_scroll.incomplete");
            normal.drawString(normal.trimStringToWidth(status,WIDTH),32,220,INK);
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lx,ly);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();GL11.glPopAttrib();GL11.glMatrixMode(previousMatrix);
        }
    }
    private static void drawPuzzle(Layout page,int left,int top,int width,int height) {
        if(page.puzzle==null || page.puzzle.hexes.isEmpty())return;
        double minX=Double.MAX_VALUE,minY=minX,maxX=-minX,maxY=-minX;
        for(thaumcraft.common.lib.utils.HexUtils.Hex hex:page.puzzle.hexes.values()) {
            thaumcraft.common.lib.utils.HexUtils.Pixel p=hex.toPixel(10);
            minX=Math.min(minX,p.x);maxX=Math.max(maxX,p.x);minY=Math.min(minY,p.y);maxY=Math.max(maxY,p.y);
        }
        double scale=Math.min(1,Math.min((width-14)/(maxX-minX+1),(height-14)/(maxY-minY+1)));
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(left+width/2D,top+height/2D,0);GL11.glScaled(scale,scale,scale);
            GL11.glTranslated(-(minX+maxX)/2,-(minY+maxY)/2,0);
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glColor4f(.33F,.33F,.33F,.65F);
            GL11.glBegin(GL11.GL_LINES);
            for(thaumcraft.common.lib.utils.HexUtils.Hex hex:page.puzzle.hexes.values()) {
                thaumcraft.common.lib.research.ResearchManager.HexEntry entry=page.puzzle.hexEntries.get(hex.toString());
                if(entry==null || entry.type<1 || entry.aspect==null)continue;
                thaumcraft.common.lib.utils.HexUtils.Pixel a=hex.toPixel(10);
                for(int d=0;d<3;d++) {
                    thaumcraft.common.lib.utils.HexUtils.Hex neighbour=hex.getNeighbour(d);
                    thaumcraft.common.lib.research.ResearchManager.HexEntry other=page.puzzle.hexEntries.get(neighbour.toString());
                    if(other!=null && other.type>=1 && other.aspect!=null && linked(entry.aspect,other.aspect)) {
                        thaumcraft.common.lib.utils.HexUtils.Pixel b=neighbour.toPixel(10);
                        GL11.glVertex3d(a.x,a.y,0);GL11.glVertex3d(b.x,b.y,0);
                    }
                }
            }
            GL11.glEnd();GL11.glEnable(GL11.GL_TEXTURE_2D);
            Minecraft mc=Minecraft.getMinecraft();
            String player=mc.thePlayer==null?"":mc.thePlayer.getCommandSenderName();
            for(thaumcraft.common.lib.utils.HexUtils.Hex hex:page.puzzle.hexes.values()) {
                thaumcraft.common.lib.research.ResearchManager.HexEntry entry=page.puzzle.hexEntries.get(hex.toString());
                if(entry==null)continue;
                thaumcraft.common.lib.utils.HexUtils.Pixel p=hex.toPixel(10);
                if(entry.type>=1 && entry.aspect!=null) {
                    boolean known=!player.isEmpty() && Thaumcraft.proxy.getPlayerKnowledge().hasDiscoveredAspect(player,entry.aspect);
                    if(known)UtilsFX.drawTag((int)p.x-8,(int)p.y-8,entry.aspect,0,0,0,771,1,false);
                    else {mc.getTextureManager().bindTexture(UNKNOWN);GL11.glColor4f(.33F,.33F,.33F,1);quad(p.x-8,p.y-8,16,16,0);}
                } else {
                    mc.fontRenderer.drawString(entry.type<0?"x":".",(int)p.x-2,(int)p.y-4,INK);
                }
            }
        } finally {GL11.glPopMatrix();}
    }
    private static boolean linked(Aspect a,Aspect b) {
        return contains(a.getComponents(),b)||contains(b.getComponents(),a);
    }
    private static boolean contains(Aspect[] values,Aspect wanted) {
        if(values!=null)for(Aspect value:values)if(value==wanted)return true;
        return false;
    }
    private static void quad(double x,double y,double w,double h,double z) {
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setNormal(0,0,1);
        t.addVertexWithUV(x,y+h,z,0,1);t.addVertexWithUV(x+w,y+h,z,1,1);
        t.addVertexWithUV(x+w,y,z,1,0);t.addVertexWithUV(x,y,z,0,0);t.draw();
    }
}
