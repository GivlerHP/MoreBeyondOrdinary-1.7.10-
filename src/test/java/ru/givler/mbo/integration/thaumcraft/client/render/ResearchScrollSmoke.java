package ru.givler.mbo.integration.thaumcraft.client.render;

import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraftforge.client.IItemRenderer.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import thaumcraft.api.aspects.*;
import thaumcraft.api.research.*;

public final class ResearchScrollSmoke {
    static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    static ItemStack note(String key,boolean complete) {
        ItemStack stack=new ItemStack(Items.paper);NBTTagCompound tag=new NBTTagCompound();
        tag.setString("key",key);tag.setBoolean("complete",complete);
        NBTTagList grid=new NBTTagList();
        for(int i=0;i<3;i++) {
            NBTTagCompound hex=new NBTTagCompound();hex.setByte("hexq",(byte)i);hex.setByte("hexr",(byte)0);
            hex.setByte("type",(byte)(i==2?2:1));hex.setString("aspect",i==0?Aspect.FIRE.getTag():i==1?Aspect.WATER.getTag():Aspect.ORDER.getTag());grid.appendTag(hex);
        }
        tag.setTag("hexgrid",grid);stack.setTagCompound(tag);return stack;
    }
    static ResearchItem research(String key,ResearchPage...pages) {
        ResearchItem item=new ResearchItem(key,"MBO_SCROLL_TEST",new AspectList().add(Aspect.AIR,7),0,0,1,new ResourceLocation("mbo:test"));
        item.setPages(pages);ResearchCategories.getResearchList("MBO_SCROLL_TEST").research.put(key,item);return item;
    }
    static void translations(String text)throws Exception {StringTranslate.inject(new ByteArrayInputStream(text.getBytes("UTF-8")));}
    public static void main(String[] args)throws Exception {
        final String transformer="ru.givler.mbo.integration.thaumcraft.core.ResearchScrollTransformer";
        ClassLoader early=new ClassLoader(ResearchScrollSmoke.class.getClassLoader()) {
            @Override protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
                if(name.startsWith("net.minecraft.") && !name.startsWith("net.minecraft.launchwrapper."))throw new ClassNotFoundException("Minecraft is not available during transformer registration: "+name);
                if(name.equals(transformer)) {
                    Class<?> loaded=findLoadedClass(name);
                    if(loaded==null)try(java.io.InputStream in=getParent().getResourceAsStream(name.replace('.','/')+".class")) {
                        java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;
                        while((n=in.read(buffer))>=0)out.write(buffer,0,n);
                        byte[] bytes=out.toByteArray();loaded=defineClass(name,bytes,0,bytes.length);
                    } catch(java.io.IOException e){throw new ClassNotFoundException(name,e);}
                    if(resolve)resolveClass(loaded);return loaded;
                }
                return super.loadClass(name,resolve);
            }
        };
        Class.forName(transformer,true,early).getDeclaredConstructor().newInstance();
        System.out.println("PASS early transformer registration with Minecraft classes unavailable");

        Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
        Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);loader.set(null,unsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
        Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);mods.set(loader.get(null),new HashMap<>());
        net.minecraft.init.Bootstrap.func_151354_b();
        translations("tc.research_name.MBO_SCROLL_SCRIPT=Scripted research\ntc.research_text.MBO_SCROLL_SCRIPT=Fallback summary\n"
                +"research.scroll.intro=A script-defined introduction.<BR>Its second paragraph belongs to this research.\n"
                +"mbo.research_scroll.unknown=Unknown notes\nmbo.research_scroll.missing=Unavailable research\n"
                +"mbo.research_scroll.unavailable=Description unavailable\nmbo.research_scroll.complete=Research complete\nmbo.research_scroll.incomplete=Unfinished notes\n");
        ResearchCategories.registerCategory("MBO_SCROLL_TEST",new ResourceLocation("mbo:test"),new ResourceLocation("mbo:test"));
        ResearchItem entry=research("MBO_SCROLL_SCRIPT",new ResearchPage("SECRET","Forbidden hidden text"),new ResearchPage("research.scroll.intro"));
        ItemStack stack=note(entry.key,false);NBTTagCompound original=(NBTTagCompound)stack.getTagCompound().copy();
        ResearchScrollContent content=ResearchScrollContent.read(stack,key->false);
        check(content.runes&&!content.complete&&!content.text.contains("script-defined"),"Unfinished research must not expose its text");
        check(content.aspects.size()==2&&content.aspects.contains(Aspect.FIRE)&&content.aspects.contains(Aspect.WATER)&&!content.aspects.contains(Aspect.ORDER),"Show original anchors, not the player's connector or new registry tags");
        stack.getTagCompound().setBoolean("complete",true);
        content=ResearchScrollContent.read(stack,key->false);
        check(content.complete&&!content.runes&&content.text.contains("script-defined")&&!content.text.contains("Forbidden"),"Read completed script page without leaking concealed pages");
        check(content.text.contains("\n")&&!content.text.contains("<BR>"),"Paragraphs and markup");
        check(ResearchScrollContent.read(stack,key->true).text.equals("Forbidden hidden text"),"Concealed page becomes eligible only with its prerequisite");
        entry.setPages(new ResearchPage("Updated script introduction"));
        check(ResearchScrollContent.read(stack,key->false).text.equals("Updated script introduction"),"Script reload must use live pages");
        entry.setPages(new ResearchPage("Singleword"));
        check(ResearchScrollContent.read(stack,key->false).text.equals("Singleword"),"Literal text pages may contain one word");
        entry.setPages(new ResearchPage("missing.translation.key"));
        check(ResearchScrollContent.read(stack,key->false).text.equals("Fallback summary"),"Missing page localization must not print a technical key");
        translations("mbo.research_scroll.MBO_SCROLL_SCRIPT.text=Custom scroll introduction\n");
        check(ResearchScrollContent.read(stack,key->false).text.equals("Custom scroll introduction"),"Script/localization override must take priority");
        check(ResearchScrollContent.plain("A<IMG>domain:texture:0:0:16:16:1</IMG><LINE/>B").equals("A \nB"),"Text preview excludes illustrations");
        check(ResearchScrollContent.read(note("REMOVED_RESEARCH",true),key->true).aspects.size()==2,"Removed research keeps note anchors");
        check(ResearchScrollContent.read(null,key->false).runes,"Empty note safety");
        ItemStack unknown=note(entry.key,true);unknown.setItemDamage(42);check(ResearchScrollContent.read(unknown,key->true).runes,"Unidentified notes cannot expose a stored key");
        stack.getTagCompound().setBoolean("complete",false);check(stack.getTagCompound().equals(original),"Reading must not alter NBT");
        check(stack.stackSize==1,"Reading cannot consume research notes");
        System.out.println("PASS live script research, localization override, concealed pages, anchor aspects, runes/completion, unknown notes and unchanged NBT");
        pixels(args[0],args[1],unsafe,stack);
    }
    private static void pixels(String minecraftJar,String thaumcraftAssets,sun.misc.Unsafe unsafe,ItemStack stack)throws Exception {
        Pbuffer context=new Pbuffer(512,512,new PixelFormat(8,24,0),null,null);context.makeCurrent();
        try {
            net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit=GL13.GL_TEXTURE0;
            net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit=GL13.GL_TEXTURE1;
            Minecraft mc=(Minecraft)unsafe.allocateInstance(Minecraft.class);
            Field singleton=Minecraft.class.getDeclaredField("theMinecraft");singleton.setAccessible(true);singleton.set(null,mc);
            mc.gameSettings=(GameSettings)unsafe.allocateInstance(GameSettings.class);mc.gameSettings.language="en_US";
            IMetadataSerializer metadata=new IMetadataSerializer();
            metadata.registerMetadataSectionType(new TextureMetadataSectionSerializer(),TextureMetadataSection.class);
            metadata.registerMetadataSectionType(new FontMetadataSectionSerializer(),FontMetadataSection.class);
            SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(metadata);
            resources.reloadResourcePack(new FileResourcePack(new File(minecraftJar)));
            resources.reloadResourcePack(new FolderResourcePack(new File(thaumcraftAssets)));
            resources.reloadResourcePack(new FolderResourcePack(new File("src/main/resources")));
            for(Field field:Minecraft.class.getDeclaredFields())if(field.getType()==IReloadableResourceManager.class){field.setAccessible(true);field.set(mc,resources);}
            mc.renderEngine=new TextureManager(resources);
            mc.fontRenderer=new FontRenderer(mc.gameSettings,new ResourceLocation("textures/font/ascii.png"),mc.renderEngine,false);
            mc.standardGalacticFontRenderer=new FontRenderer(mc.gameSettings,new ResourceLocation("textures/font/ascii_sga.png"),mc.renderEngine,false);
            mc.fontRenderer.onResourceManagerReload(resources);
            mc.standardGalacticFontRenderer.onResourceManagerReload(resources);
            check(mc.fontRenderer.getStringWidth("Research")>20,"Real font metrics must be loaded");
            ResearchScrollRenderer renderer=new ResearchScrollRenderer();
            net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(Items.paper,renderer);
            ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.bind(Items.paper,renderer);
            check(renderer.handleRenderType(stack,ItemRenderType.FIRST_PERSON_MAP),"Use vanilla two-arm map dispatch");
            check(ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.mapItem(Items.paper)
                    && ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.mapItem(Items.filled_map)
                    && !ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.mapItem(Items.stick),"Only notes and native maps enter the map pose");
            check(ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.mapData(Items.paper,stack,null)==null,"Notes must never be cast to ItemMap");
            net.minecraft.entity.player.EntityPlayerMP player=(net.minecraft.entity.player.EntityPlayerMP)unsafe.allocateInstance(net.minecraft.entity.player.EntityPlayerMP.class);
            player.inventory=new net.minecraft.entity.player.InventoryPlayer(player);player.inventory.mainInventory[0]=stack;
            net.minecraft.client.model.ModelBiped model=new net.minecraft.client.model.ModelBiped();
            ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.pose(model,player);
            check(model.bipedRightArm.rotateAngleX==0,"Do not change vanilla first-person arms");
            ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.beginPose(player);
            ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.pose(model,player);
            check(model.bipedRightArm.rotateAngleX< -1 && model.bipedLeftArm.rotateAngleX==model.bipedRightArm.rotateAngleX,"Both third-person arms hold the sheet");
            ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.endPose();
            check(!ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.skipOverlay(stack,0,ItemRenderType.EQUIPPED_FIRST_PERSON),"Base parchment pass must remain");
            check(ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.skipOverlay(stack,1,ItemRenderType.EQUIPPED_FIRST_PERSON),"Do not draw parchment twice in the main hand");
            check(ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.skipOverlay(stack,1,ItemRenderType.EQUIPPED),"MF2 and third-person overlay suppression");
            check(!ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.skipOverlay(stack,1,ItemRenderType.INVENTORY),"Inventory overlays remain native");
            check(!ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.skipOverlay(new ItemStack(Items.stick),1,ItemRenderType.EQUIPPED),"Other items remain untouched");
            check(!renderer.handleRenderType(stack,ItemRenderType.INVENTORY)&&!renderer.handleRenderType(stack,ItemRenderType.ENTITY),"Keep inventory and dropped-item rendering native");
            check(renderer.handleRenderType(stack,ItemRenderType.EQUIPPED)&&renderer.handleRenderType(stack,ItemRenderType.EQUIPPED_FIRST_PERSON),"Both equipped render paths");
            List<String> fitted=ResearchScrollRenderer.fit(mc.fontRenderer,"A very long introduction with repeated words and more sentences. A second sentence is too large for the available space.",90,2);
            check(fitted.size()<=2,"Bounded text height");for(String line:fitted)check(mc.fontRenderer.getStringWidth(line)<=90,"Bounded text width");
            byte[] first=null;BufferedImage sheet=new BufferedImage(1024,512,BufferedImage.TYPE_INT_RGB);
            for(int state=0;state<2;state++) {
                stack.getTagCompound().setBoolean("complete",state==1);
                GL11.glViewport(0,0,512,512);GL11.glClearColor(.12F,.15F,.18F,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
                GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-15,143,143,-15,10,-10);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glColor4f(.6F,.7F,.8F,1);
                int matrices=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),attributes=GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
                // Vanilla map branch draws the selected background before dispatching FIRST_PERSON_MAP.
                mc.getTextureManager().bindTexture(ru.givler.mbo.integration.thaumcraft.core.ResearchScrollHooks.mapPaper(new ResourceLocation("textures/map/map_background.png"),stack));
                GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glColor4f(1,1,1,1);
                net.minecraft.client.renderer.Tessellator t=net.minecraft.client.renderer.Tessellator.instance;t.startDrawingQuads();
                t.addVertexWithUV(-7,135,0,0,1);t.addVertexWithUV(135,135,0,1,1);t.addVertexWithUV(135,-7,0,1,0);t.addVertexWithUV(-7,-7,0,0,0);t.draw();
                GL11.glColor4f(.6F,.7F,.8F,1);
                renderer.renderItem(ItemRenderType.FIRST_PERSON_MAP,stack);
                check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==matrices&&GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH)==attributes,"Balanced GL stacks");
                FloatBuffer color=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,color);check(Math.abs(color.get(0)-.6F)<.001F,"Caller color restored");
                check(GL11.glGetError()==GL11.GL_NO_ERROR,"Scroll GL error");
                ByteBuffer out=BufferUtils.createByteBuffer(512*512*4);GL11.glReadPixels(0,0,512,512,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,out);
                byte[] image=new byte[out.capacity()];out.get(image);if(state==0)first=image;else check(!Arrays.equals(first,image),"Completed scroll must change its visual content");
                int paper=0;Set<Integer> textureColors=new HashSet<Integer>();
                for(int y=0;y<512;y++)for(int x=0;x<512;x++){
                    int i=(y*512+x)*4,r=image[i]&255,g=image[i+1]&255,b=image[i+2]&255;
                    if(r>110&&g>70){paper++;textureColors.add(r<<16|g<<8|b);}
                    sheet.setRGB(state*512+x,511-y,r<<16|g<<8|b);
                }
                check(paper>10000,"Parchment must be visibly drawn");
                check(textureColors.size()>256,"Parchment texture must be sampled, not an untextured white quad");
            }
            ByteBuffer before=BufferUtils.createByteBuffer(512*512*4),after=BufferUtils.createByteBuffer(512*512*4);
            GL11.glReadPixels(0,0,512,512,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,before);
            renderer.renderItem(ItemRenderType.EQUIPPED,stack);
            GL11.glReadPixels(0,0,512,512,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,after);
            check(before.equals(after),"Ordinary equipped pass must not duplicate the torso sheet");
            renderer.renderItem(ItemRenderType.EQUIPPED,stack,Boolean.TRUE);
            check(GL11.glGetError()==GL11.GL_NO_ERROR,"Third-person/MF2 equipped path");
            Files.createDirectories(Paths.get("build/research-scroll"));ImageIO.write(sheet,"png",new File("build/research-scroll/states.png"));
            System.out.println("PASS real parchment/font draws, both equipped paths, state changes, bounded layout, inventory fallback and GL restoration");
        } finally {context.destroy();}
    }
}
