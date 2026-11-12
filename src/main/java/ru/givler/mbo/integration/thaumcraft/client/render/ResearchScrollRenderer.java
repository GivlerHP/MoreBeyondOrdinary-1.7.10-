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

/** Equipped-only parchment. Forge/MF2 keep ownership of hand animation and the other held item. */
public final class ResearchScrollRenderer implements IItemRenderer,IResourceManagerReloadListener {
    private static final ResourceLocation PAPER=new ResourceLocation("mbo","textures/map/scroll_empty.png");
    private static final ResourceLocation UNKNOWN=new ResourceLocation("thaumcraft","textures/aspects/_unknown.png");
    private final Map<ItemStack,Layout> layouts=new WeakHashMap<ItemStack,Layout>();
    private static final int INK=0x49301D, WIDTH=192;
    private static final class Layout {
        NBTTagCompound nbt; int damage; Object world;String locale,player;
        long expires;
        ResearchScrollContent content;
        List<String> title,body;List<Boolean> known;
        int aspectRows,bodyY;
    }
    @Override public boolean handleRenderType(ItemStack stack,ItemRenderType type) {
        return type==ItemRenderType.EQUIPPED || type==ItemRenderType.EQUIPPED_FIRST_PERSON;
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,ItemRendererHelper helper){return false;}
    @Override public void onResourceManagerReload(IResourceManager manager){layouts.clear();}
    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void unload(net.minecraftforge.event.world.WorldEvent.Unload event){if(event.world.isRemote)layouts.clear();}
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
        next.title=fit(normal,next.content.title,WIDTH,2);
        next.known=new ArrayList<Boolean>();
        for(Aspect aspect:next.content.aspects)next.known.add(!player.isEmpty()
                && Thaumcraft.proxy.getPlayerKnowledge().hasDiscoveredAspect(player,aspect));
        next.aspectRows=(next.content.aspects.size()+7)/8;
        next.bodyY=40+next.title.size()*normal.FONT_HEIGHT+Math.min(3,next.aspectRows)*22;
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
        Minecraft mc=Minecraft.getMinecraft();Layout page=layout(stack);
        int previousMatrix=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        float lx=OpenGlHelper.lastBrightnessX,ly=OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
        try {
            // Both vanilla and MF2 deliver an item-local equipped transform. Never mirror text.
            GL11.glTranslatef(.25F,.5F,.2F);GL11.glRotatef(-45,0,1,0);
            float scale=type==ItemRenderType.EQUIPPED_FIRST_PERSON?.0055F:.0045F;
            GL11.glScalef(scale,-scale,scale);GL11.glTranslatef(-128,-128,0);
            GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.05F);GL11.glColor4f(1,1,1,1);
            mc.getTextureManager().bindTexture(PAPER);quad(0,0,256,256,0);
            GL11.glTranslatef(0,0,.1F);
            FontRenderer normal=mc.fontRenderer;int y=29;
            for(String line:page.title){normal.drawString(line,(256-normal.getStringWidth(line))/2,y,INK);y+=normal.FONT_HEIGHT;}
            y+=8;
            for(int i=0;i<Math.min(24,page.content.aspects.size());i++) {
                int count=Math.min(8,page.content.aspects.size()-(i/8)*8);
                int x=(256-count*22)/2+(i%8)*22;
                if(page.known.get(i))UtilsFX.drawTag(x,y+(i/8)*22,page.content.aspects.get(i),0,0,0,771,1,false);
                else {mc.getTextureManager().bindTexture(UNKNOWN);GL11.glColor4f(.25F,.18F,.1F,1);quad(x,y+(i/8)*22,16,16,0);}
            }
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
    private static void quad(double x,double y,double w,double h,double z) {
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setNormal(0,0,1);
        t.addVertexWithUV(x,y+h,z,0,1);t.addVertexWithUV(x+w,y+h,z,1,1);
        t.addVertexWithUV(x+w,y,z,1,0);t.addVertexWithUV(x,y,z,0,0);t.draw();
    }
}
