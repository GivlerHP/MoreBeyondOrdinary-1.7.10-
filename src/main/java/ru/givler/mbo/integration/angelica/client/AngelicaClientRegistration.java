package ru.givler.mbo.integration.angelica.client;

import java.lang.reflect.Field;
import ru.givler.mbo.integration.angelica.client.render.AngelicaTileEntityHooks;

/** Keep MBO's existing render hooks; no Angelica classes are linked when it is absent. */
public final class AngelicaClientRegistration {
  private static java.lang.reflect.Method shaderPackInUse;
  private static Object irisApi;
  private static boolean handStateLogged;
  private AngelicaClientRegistration() {}
  public static void register() {
    if(!cpw.mods.fml.common.Loader.isModLoaded("angelica"))return;
    try {
      Class<?> config=Class.forName("com.gtnewhorizons.angelica.config.AngelicaConfig");
      final Field natural=config.getField("enableNaturalTextures");
      final Field packFeatures=config.getField("enableMCPatcherForgeFeatures");
      final Field paneCtm=Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures").getField("glassPane");
      final Field connected=Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures").getField("enabled");
      ru.givler.mbo.integration.angelica.client.render.AngelicaPaneRendering.configure(()->{
        try{return packFeatures.getBoolean(null)&&connected.getBoolean(null)&&paneCtm.getBoolean(null);}catch(IllegalAccessException error){throw new IllegalStateException(error);}
      });
      ru.givler.mbo.client.texture.NaturalTextureHooks.configure(()->{
        try{return packFeatures.getBoolean(null)&&natural.getBoolean(null);}catch(IllegalAccessException error){throw new IllegalStateException(error);}
      });
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ru.givler.mbo.client.texture.NaturalTextureHooks.INSTANCE);
      ((net.minecraft.client.resources.IReloadableResourceManager)net.minecraft.client.Minecraft.getMinecraft().getResourceManager())
          .registerReloadListener(ru.givler.mbo.client.texture.RandomMobTextureHooks.INSTANCE);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ru.givler.mbo.client.texture.RandomMobTextureHooks.INSTANCE);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(AngelicaSkyHooks.INSTANCE);
      for(String flag:new String[]{"enableEntityBatching","enableCubeInstancing","enableFontRenderer","enableTESRChestCache","enableTESRSignCache","enableTESRProviderDispatch","enableTESRSkullCache","enableTESRJarCache"})
        config.getField(flag).setBoolean(null,false);
      Class<?> proxy=Class.forName("com.gtnewhorizons.angelica.proxy.ClientProxy");
      Object options=proxy.getMethod("options").invoke(null);
      set(options,"quality","useCeleritasSmoothLighting",false);
      // Preserve the user's culling options. The lighting-related TESR flicker
      // came from Carpenter's Blocks writing Forge's render pass on worker threads.
      set(options,"advanced","enableDeferredBatching",false);
      Class<?> api=Class.forName("net.irisshaders.iris.api.v0.IrisApi");
      irisApi=api.getMethod("getInstance").invoke(null);
      shaderPackInUse=api.getMethod("isShaderPackInUse");
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new AngelicaClientRegistration());
      ru.givler.mbo.client.render.WaterloggedBlockRenderer.enableAngelicaChunkWater();
      net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new AngelicaTextureReport());
      net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new ru.givler.mbo.client.diagnostics.TileEntityTraceCommand());
      cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(ru.givler.mbo.client.diagnostics.TileEntityRenderTrace.INSTANCE);
      HandRenderTrace.enabled=true;
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new HandRenderTrace());
      System.out.println("[MBO] Angelica compatibility: preserve MBO vertex lighting and immediate entity/TESR rendering");
    } catch(ReflectiveOperationException error) {
      throw new IllegalStateException("MBO cannot configure the installed Angelica rendering API",error);
    }
  }
  /** Establish the vanilla foreground state before other mods can own RenderHandEvent. */
  @cpw.mods.fml.common.eventhandler.SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST)
  public void beforeHand(net.minecraftforge.client.event.RenderHandEvent event) {
    HandRenderTrace.beginEvent();
    try {
      if(Boolean.TRUE.equals(shaderPackInUse.invoke(irisApi)))return;
    } catch(ReflectiveOperationException error) { throw new IllegalStateException(error); }
    if(!handStateLogged) {
      handStateLogged=true;
      System.out.println("[MBO] Angelica hand boundary: depthWrite="+org.lwjgl.opengl.GL11.glGetBoolean(org.lwjgl.opengl.GL11.GL_DEPTH_WRITEMASK)
          +", depthFunc="+org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_DEPTH_FUNC)
          +", stencil="+org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_STENCIL_TEST)
          +", scissor="+org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST));
    }
    // Forge fires this before vanilla clears depth. A stale write mask also masks that clear.
    org.lwjgl.opengl.GL11.glDepthMask(true);
    org.lwjgl.opengl.GL11.glDepthFunc(org.lwjgl.opengl.GL11.GL_LEQUAL);
    org.lwjgl.opengl.GL11.glColorMask(true,true,true,true);
    org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
    org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
  }
  @cpw.mods.fml.common.eventhandler.SubscribeEvent
  public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
    if(event.world.isRemote) AngelicaTileEntityHooks.reset();
  }
  private static void set(Object options,String section,String option,boolean value) throws ReflectiveOperationException {
    Object category=options.getClass().getField(section).get(options);
    Field field=category.getClass().getField(option); field.setBoolean(category,value);
  }
}
