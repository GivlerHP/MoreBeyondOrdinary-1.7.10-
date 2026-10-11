package ru.givler.mbo.client.model.decormodels;

import java.util.LinkedHashMap;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

/** Resource identifiers do not change when a resource pack is reloaded. */
public final class DecorResources {
    private static final LinkedHashMap<String,ResourceLocation> CACHE=new LinkedHashMap<String,ResourceLocation>(128,.75F,true);
    private static final LinkedHashMap<String,ResourceLocation> MODELS=new LinkedHashMap<String,ResourceLocation>(128,.75F,true);
    private static final LinkedHashMap<String,ResourceLocation> TEXTURES=new LinkedHashMap<String,ResourceLocation>(128,.75F,true);
    private DecorResources() {}
    public static synchronized ResourceLocation model(String name) {
        ResourceLocation value=MODELS.get(name);
        if(value==null) {
            value=get(DecorResourcePaths.geometry(name));MODELS.put(name,value);
            if(MODELS.size()>4096)MODELS.remove(MODELS.keySet().iterator().next());
        }
        return value;
    }
    public static synchronized ResourceLocation texture(String name,String suffix) {
        String key=suffix.isEmpty()?name:name+suffix;
        ResourceLocation value=TEXTURES.get(key);
        if(value==null) {
            value=get(DecorResourcePaths.texture(name,suffix));TEXTURES.put(key,value);
            if(TEXTURES.size()>4096)TEXTURES.remove(TEXTURES.keySet().iterator().next());
        }
        return value;
    }
    public static synchronized ResourceLocation get(String path) {
        ResourceLocation value=CACHE.get(path);
        if(value==null) {
            value=new ResourceLocation(MoreBeyondOrdinary.MODID,path);CACHE.put(path,value);
            if(CACHE.size()>4096)CACHE.remove(CACHE.keySet().iterator().next());
        }
        return value;
    }
}
