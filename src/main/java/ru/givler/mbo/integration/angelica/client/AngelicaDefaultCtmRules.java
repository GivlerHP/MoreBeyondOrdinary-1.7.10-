package ru.givler.mbo.integration.angelica.client;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

/** OptiFine E7 does not apply its stock CTM artwork over a replacement base texture. */
public final class AngelicaDefaultCtmRules {
  private static Method packs;
  private AngelicaDefaultCtmRules() {}
  public static boolean skip(Object properties) {
    if(properties==null)return false;
    try {
      ResourceLocation resource=(ResourceLocation)properties.getClass().getMethod("getResource").invoke(properties);
      String texture=baseTexture(resource.getResourceDomain(),resource.getResourcePath());
      if(texture==null)return false;
      Method sourceMethod;
      try {sourceMethod=resource.getClass().getMethod("getSource");}catch(NoSuchMethodException noSource){return false;}
      IResourcePack source=(IResourcePack)sourceMethod.invoke(resource);
      if(source==null || !bundled(source.getPackName()))return false;
      if(packs==null)packs=Class.forName("com.prupe.mcpatcher.mal.resource.TexturePackAPI").getMethod("getResourcePacks",String.class);
      @SuppressWarnings("unchecked") List<IResourcePack> loaded=(List<IResourcePack>)packs.invoke(null,"minecraft");
      boolean replaced=hasReplacement(new ResourceLocation(texture),loaded);
      if(replaced)System.out.println("[MBO] CTM: skip bundled "+resource+"; resource pack supplies "+texture);
      return replaced;
    }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot resolve Angelica stock CTM source",error);}
  }
  static boolean hasReplacement(ResourceLocation texture,List<IResourcePack> loaded) {
    for(IResourcePack pack:loaded)if(!(pack instanceof DefaultResourcePack) && !bundled(pack.getPackName()) && pack.resourceExists(texture))return true;
    return false;
  }
  static boolean bundled(String name) {
    return "NotFine".equals(name) || "FMLFileResourcePack:NotFine".equals(name) || "Angelica".equals(name) || "MCPatcherForge".equals(name)
        || "FMLFileResourcePack:Angelica".equals(name) || "FMLFileResourcePack:MCPatcherForge".equals(name);
  }
  static String baseTexture(String domain,String path) {
    if(!"minecraft".equals(domain))return null;
    if(path.equals("mcpatcher/ctm/glass/glass.properties") || path.equals("mcpatcher/ctm/glass/glasspane.properties"))return "textures/blocks/glass.png";
    for(String color:new String[]{"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","silver","cyan","purple","blue","brown","green","red","black"}) {
      String prefix="mcpatcher/ctm/stained_glass/glass_"+color+"/"+color+"_stained_glass";
      if(path.equals(prefix+".properties") || path.equals(prefix+"pane.properties"))
        return "textures/blocks/glass_"+(color.equals("light_gray")?"silver":color)+".png";
    }
    return null;
  }
}
