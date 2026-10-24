package ru.givler.mbo.client.texture;

import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

/** Stable UUID variants; definitions are cached until resource reload, never scanned per frame. */
public final class RandomMobTextureHooks implements IResourceManagerReloadListener {
  public static final RandomMobTextureHooks INSTANCE=new RandomMobTextureHooks();
  private static final Map<ResourceLocation,Definition> definitions=new HashMap<>();
  private static final Map<Entity,Origin> origins=new WeakHashMap<>();
  private static java.lang.reflect.Field enabled;
  private static java.lang.reflect.Field features;
  private static boolean initialized;
  private static final class Origin {
    int y,biome,seed;String biomeName;
    Origin(Entity e) {
      y=(int)Math.floor(e.posY);BiomeGenBase b=e.worldObj.getBiomeGenForCoords((int)Math.floor(e.posX),(int)Math.floor(e.posZ));
      biome=b.biomeID;biomeName=b.biomeName;
      Entity identity=e;
      // E7 also obtains the authoritative UUID from the integrated server:
      // legacy 1.7.10 mob spawn packets do not carry it to the client.
      net.minecraft.server.integrated.IntegratedServer server=Minecraft.getMinecraft().getIntegratedServer();
      if(server!=null) {
        net.minecraft.world.WorldServer level=server.worldServerForDimension(e.dimension);
        Entity authoritative=level==null?null:level.getEntityByID(e.getEntityId());
        if(authoritative!=null)identity=authoritative;
      }
      seed=(int)(identity.getUniqueID().getLeastSignificantBits()&Integer.MAX_VALUE);
    }
  }
  static final class Definition {
    final ResourceLocation base;final List<RandomMobRule> rules=new ArrayList<>();
    final Map<Integer,ResourceLocation> skins=new HashMap<>();final List<ResourceLocation> random=new ArrayList<>();
    Definition(ResourceLocation base){this.base=base;}
  }
  private RandomMobTextureHooks() {}
  public void onResourceManagerReload(IResourceManager resources){definitions.clear();}
  @cpw.mods.fml.common.eventhandler.SubscribeEvent public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
    if(event.world.isRemote)origins.clear();
  }
  @cpw.mods.fml.common.eventhandler.SubscribeEvent public void joined(net.minecraftforge.event.entity.EntityJoinWorldEvent event) {
    if(event.world.isRemote&&event.entity instanceof EntityLivingBase&&enabled()&&!origins.containsKey(event.entity))
      origins.put(event.entity,new Origin(event.entity));
  }
  private static boolean enabled() {
    try {
      if(!initialized){initialized=true;enabled=Class.forName("jss.notfine.config.MCPatcherForgeConfig$RandomMobs").getField("enabled");
        features=Class.forName("com.gtnewhorizons.angelica.config.AngelicaConfig").getField("enableMCPatcherForgeFeatures");}
      return enabled!=null&&features!=null&&enabled.getBoolean(null)&&features.getBoolean(null);
    }catch(ReflectiveOperationException absent){return false;}
  }
  public static ResourceLocation resolve(ResourceLocation base,Entity entity) {
    if(base==null||!(entity instanceof EntityLivingBase)||entity.worldObj==null||!enabled())return base;
    String path=base.getResourcePath();if(!path.startsWith("textures/entity/")||!path.endsWith(".png"))return base;
    Definition d=definitions.get(base);if(d==null){d=load(base,Minecraft.getMinecraft().getResourceManager());definitions.put(base,d);}
    if(d.rules.isEmpty()&&d.random.isEmpty())return base;
    Origin origin=origins.get(entity);if(origin==null){origin=new Origin(entity);origins.put(entity,origin);}
    int profession=entity instanceof EntityVillager?((EntityVillager)entity).getProfession():-1;
    String name=entity instanceof EntityLiving&&((EntityLiving)entity).hasCustomNameTag()?((EntityLiving)entity).getCustomNameTag():"";
    for(RandomMobRule r:d.rules)if(r.matches(origin.biome,origin.biomeName,origin.y,name,profession)) {
      ResourceLocation selected=d.skins.get(r.choose(origin.seed));return selected==null?base:selected;
    }
    return d.random.isEmpty()?base:d.random.get(origin.seed%d.random.size());
  }
  private static boolean exists(ResourceLocation resource,IResourceManager resources) {
    try(InputStream in=resources.getResource(resource).getInputStream()){return true;}
    catch(IOException absent){return false;}
  }
  static Definition load(ResourceLocation base,IResourceManager resources) {
    String suffix=base.getResourcePath().substring("textures/entity/".length());
    for(String root:new String[]{"mcpatcher/mob/","optifine/mob/","optifine/random/entity/"}) {
      String path=root+suffix;ResourceLocation property=new ResourceLocation(base.getResourceDomain(),path.substring(0,path.length()-4)+".properties");
      Properties p=read(property,resources);
      if(p==null)for(String dependent:new String[]{"_eyes","_collar","_angry","_tame","_fur","_invulnerable"}) {
        if(path.endsWith(dependent+".png")){p=read(new ResourceLocation(base.getResourceDomain(),path.substring(0,path.length()-dependent.length()-4)+".properties"),resources);break;}
      }
      Definition d=new Definition(base);d.skins.put(1,base);
      if(p!=null) {
        TreeSet<Integer> indices=new TreeSet<>();
        for(String key:p.stringPropertyNames())if(key.matches("skins\\.\\d+"))indices.add(Integer.parseInt(key.substring(6)));
        for(int index:indices)try {
          RandomMobRule rule=new RandomMobRule(p,index);boolean valid=true;
          for(int skin:rule.skins)if(!d.skins.containsKey(skin)) {
            ResourceLocation resource=variant(base,path,skin);
            if(!exists(resource,resources)){valid=false;break;}d.skins.put(skin,resource);
          }
          if(valid)d.rules.add(rule);else System.err.println("[MBO] Missing random mob skin in "+property+" rule "+index);
        }catch(IllegalArgumentException bad){System.err.println("[MBO] Invalid random mob rule "+property+" #"+index+": "+bad.getMessage());}
        System.out.println("[MBO] Random mob rules: "+property+" ("+d.rules.size()+" rules)");return d;
      }
      d.random.add(base);
      for(int skin=2;skin<1000;skin++) {
        ResourceLocation resource=variant(base,path,skin);if(!exists(resource,resources))break;d.random.add(resource);
      }
      if(d.random.size()>1){System.out.println("[MBO] Random mob variants: "+base+" ("+d.random.size()+" skins)");return d;}
    }
    return new Definition(base);
  }
  private static ResourceLocation variant(ResourceLocation base,String path,int index) {
    return index==1?base:new ResourceLocation(base.getResourceDomain(),path.substring(0,path.length()-4)+index+".png");
  }
  private static Properties read(ResourceLocation location,IResourceManager resources) {
    try(InputStream in=resources.getResource(location).getInputStream()) {
      Properties p=new Properties();p.load(in);return p;
    }catch(IOException absent){return null;}
  }
}
