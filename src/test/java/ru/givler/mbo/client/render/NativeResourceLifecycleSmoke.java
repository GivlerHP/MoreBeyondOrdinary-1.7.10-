package ru.givler.mbo.client.render;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.opengl.*;
import ru.givler.mbo.banner.BannerPattern;
import ru.givler.mbo.client.render.banner.RenderBanner;
import ru.givler.mbo.dungeon.DungeonAreaRecord;
import ru.givler.mbo.tileentity.TileEntityBanner;
import sun.misc.Unsafe;

/** Real GL texture/list retirement, including TextureManager's retained object map. */
public final class NativeResourceLifecycleSmoke {
  static final class LightWorld extends WorldClient {
    int reads;
    LightWorld(){super(null,null,0,null,null);}
    @Override public int getLightBrightnessForSkyBlocks(int x,int y,int z,int minimum){reads++;return 0x00f000f0;}
  }
  static Field field(Class<?> type,String name)throws Exception {
    Field f=type.getDeclaredField(name);f.setAccessible(true);return f;
  }
  static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
  static int list(){int id=GL11.glGenLists(1);GL11.glNewList(id,GL11.GL_COMPILE);GL11.glEndList();return id;}
  public static void main(String[] args)throws Exception {
    Pbuffer context=new Pbuffer(32,32,new PixelFormat(8,24,0),null,null);context.makeCurrent();
    Unsafe unsafe=(Unsafe)field(Unsafe.class,"theUnsafe").get(null);
    Minecraft mc=(Minecraft)unsafe.allocateInstance(Minecraft.class);
    field(Minecraft.class,"theMinecraft").set(null,mc);
    mc.gameSettings=(net.minecraft.client.settings.GameSettings)unsafe.allocateInstance(net.minecraft.client.settings.GameSettings.class);
    Path assets=Paths.get(args.length==0?"src/main/resources/assets/mbo":args[0]);
    IReloadableResourceManager resources=(IReloadableResourceManager)Proxy.newProxyInstance(
        IResourceManager.class.getClassLoader(),new Class[]{IReloadableResourceManager.class},(p,m,a)-> {
          if(m.getName().equals("getResource")) {
            Path file=assets.resolve(((ResourceLocation)a[0]).getResourcePath());
            return Proxy.newProxyInstance(IResource.class.getClassLoader(),new Class[]{IResource.class},(rp,rm,ra)-> {
              if(rm.getName().equals("getInputStream"))return Files.newInputStream(file);
              if(rm.getName().equals("hasMetadata"))return false;
              return null;
            });
          }
          return null;
        });
    field(Minecraft.class,"mcResourceManager").set(mc,resources);
    mc.renderEngine=new TextureManager(resources);
    // Reproduce the reported world-less creative-inventory banner render with a real model.
    net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher dispatcher=net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance;
    for(Field f:dispatcher.getClass().getDeclaredFields())if(f.getType()==TextureManager.class){f.setAccessible(true);f.set(dispatcher,mc.renderEngine);}
    RenderBanner actualBanner=(RenderBanner)unsafe.allocateInstance(RenderBanner.class);
    field(RenderBanner.class,"model").set(actualBanner,new ru.givler.mbo.client.render.banner.ModelBanner());
    actualBanner.func_147497_a(dispatcher);
    Object previousBanner=dispatcher.mapSpecialRenderers.put(TileEntityBanner.class,actualBanner);
    try {
      ru.givler.mbo.client.render.banner.RenderBannerItem itemRenderer=new ru.givler.mbo.client.render.banner.RenderBannerItem();
      for(net.minecraftforge.client.IItemRenderer.ItemRenderType type:net.minecraftforge.client.IItemRenderer.ItemRenderType.values()) {
        if(type==net.minecraftforge.client.IItemRenderer.ItemRenderType.FIRST_PERSON_MAP)continue;
        int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
        itemRenderer.renderItem(type,new net.minecraft.item.ItemStack(new net.minecraft.item.Item(),1,0));
        check(depth==GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),"Inventory banner leaked matrix stack");
      }
    } finally {
      if(previousBanner==null)dispatcher.mapSpecialRenderers.remove(TileEntityBanner.class);else dispatcher.mapSpecialRenderers.put(TileEntityBanner.class,previousBanner);
      actualBanner.onResourceManagerReload(resources);
    }
    System.out.println("PASS world-less banner: inventory, dropped item and both equipped views rendered with actual model and balanced GL matrices.");
    RenderBanner renderer=(RenderBanner)unsafe.allocateInstance(RenderBanner.class);
    Method texture=RenderBanner.class.getDeclaredMethod("texture",TileEntityBanner.class);texture.setAccessible(true);
    Map cache=(Map)field(RenderBanner.class,"CACHE").get(null);
    TileEntityBanner banner=new TileEntityBanner();
    ResourceLocation first=null;int firstId=0;
    for(int i=0;i<320;i++) {
      field(TileEntityBanner.class,"baseColor").setInt(banner,i&15);
      NBTTagList patterns=new NBTTagList();
      for(int color:new int[]{(i>>4)&15,(i>>8)&15}) {
        NBTTagCompound pattern=new NBTTagCompound();
        pattern.setString("Pattern",BannerPattern.values()[0].id);pattern.setInteger("Color",color);patterns.appendTag(pattern);
      }
      field(TileEntityBanner.class,"patterns").set(banner,patterns);
      ResourceLocation location=(ResourceLocation)texture.invoke(renderer,banner);
      if(i==0){first=location;firstId=mc.renderEngine.getTexture(location).getGlTextureId();check(GL11.glIsTexture(firstId),"texture wasn't uploaded");}
      // GL is allowed to reuse deleted names on later iterations.
      if(i==256)check(!GL11.glIsTexture(firstId),"retired GL texture stays allocated");
    }
    check(cache.size()==256,"unbounded banner cache");
    check(mc.renderEngine.getTexture(first)==null,"retired texture stays in TextureManager");
    Map managed=(Map)field(TextureManager.class,"mapTextureObjects").get(mc.renderEngine);
    check(managed.size()==256,"TextureManager grows beyond cache limit");
    List<Integer> activeTextures=new ArrayList<>();
    for(Object object:managed.values())activeTextures.add(((ITextureObject)object).getGlTextureId());
    renderer.onResourceManagerReload(resources);check(cache.isEmpty()&&managed.isEmpty(),"reload retains banner textures");
    for(int id:activeTextures)check(!GL11.glIsTexture(id),"reload leaves a GL texture");
    texture.invoke(renderer,banner);
    WorldClient world=(WorldClient)unsafe.allocateInstance(WorldClient.class);world.isRemote=true;
    renderer.unload(new WorldEvent.Unload(world));check(cache.isEmpty()&&managed.isEmpty(),"world unload retains banner textures");

    DungeonAreaWorldRenderer dungeon=(DungeonAreaWorldRenderer)unsafe.allocateInstance(DungeonAreaWorldRenderer.class);
    Map<UUID,Object> lists=new HashMap<>();field(DungeonAreaWorldRenderer.class,"cache").set(dungeon,lists);
    Class<?> entry=Class.forName(DungeonAreaWorldRenderer.class.getName()+"$Cache");
    Constructor<?> constructor=entry.getDeclaredConstructor(int.class,int.class);constructor.setAccessible(true);
    UUID live=UUID.randomUUID(),removed=UUID.randomUUID();int liveList=list(),removedList=list();
    lists.put(live,constructor.newInstance(liveList,1));lists.put(removed,constructor.newInstance(removedList,2));
    dungeon.pruneCache(Collections.singleton(live));
    check(lists.size()==1&&GL11.glIsList(liveList)&&!GL11.glIsList(removedList),"removed area GL list retained");
    dungeon.onResourceManagerReload(resources);
    check(lists.isEmpty()&&!GL11.glIsList(liveList),"resource reload retains a dungeon GL list");
    int unloadList=list();lists.put(live,constructor.newInstance(unloadList,1));
    dungeon.unload(new WorldEvent.Unload(world));
    check(lists.isEmpty()&&!GL11.glIsList(unloadList),"world unload retains a dungeon GL list");

    LightWorld lightWorld=(LightWorld)unsafe.allocateInstance(LightWorld.class);mc.theWorld=lightWorld;
    DungeonAreaRecord area=new DungeonAreaRecord();
    Method signature=DungeonAreaWorldRenderer.class.getDeclaredMethod("currentLightSignature",DungeonAreaRecord.class,Minecraft.class);signature.setAccessible(true);
    int sig=(Integer)signature.invoke(dungeon,area,mc);
    Object cached=constructor.newInstance(list(),sig);field(entry,"checkedEpoch").setLong(cached,0);lists.put(area.getId(),cached);
    Method compiled=DungeonAreaWorldRenderer.class.getDeclaredMethod("compiled",DungeonAreaRecord.class,Minecraft.class);compiled.setAccessible(true);
    lightWorld.reads=0;compiled.invoke(dungeon,area,mc);compiled.invoke(dungeon,area,mc);
    check(lightWorld.reads==0,"lighting rescanned per render frame");
    field(DungeonAreaWorldRenderer.class,"lightEpoch").setLong(dungeon,1);compiled.invoke(dungeon,area,mc);
    check(lightWorld.reads==27,"lighting not checked on next tick");compiled.invoke(dungeon,area,mc);
    check(lightWorld.reads==27,"lighting repeatedly checked within one tick");
    dungeon.pruneCache(Collections.emptySet());
    check(GL11.glGetError()==GL11.GL_NO_ERROR,"OpenGL error");
    context.destroy();
    System.out.println("PASS 320 banner variants: cache/TextureManager capped at 256, retired GL textures deleted, reload/world-unload cleared; removed dungeon GL lists deleted; lighting checked once per tick.");
  }
}
