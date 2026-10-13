package ru.givler.mbo.client.render.decormodels;

import java.util.LinkedHashMap;
import java.util.Arrays;
import java.lang.reflect.Field;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.geckominecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import ru.givler.mbo.tileentity.ModelTileBase;

/** Shares immutable geometry, never world transforms, textures or lightmap coordinates. */
public final class StaticDecorRenderCache implements IResourceManagerReloadListener {
    public static final StaticDecorRenderCache INSTANCE=new StaticDecorRenderCache();
    private static final int LIMIT=512;
    private final LinkedHashMap<Key,TesselatorVertexState> meshes=new LinkedHashMap<Key,TesselatorVertexState>(64,.75F,true);
    private static final Field RAW=ReflectionHelper.findField(Tessellator.class,"rawBuffer","field_78405_h");
    private static final Field INDEX=ReflectionHelper.findField(Tessellator.class,"rawBufferIndex","field_78411_s");
    private static final Field COUNT=ReflectionHelper.findField(Tessellator.class,"vertexCount","field_78406_i");
    private long hits,misses;
    private boolean enabled=true;
    private StaticDecorRenderCache() {}

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled=value; }
    public String statistics() {
        long bytes=0;for(TesselatorVertexState mesh:meshes.values())bytes+=mesh.getRawBuffer().length*4L;
        return "geometry meshes="+meshes.size()+"/"+LIMIT+", vertex memory="+bytes+" bytes, hits="+hits+", builds="+misses
            +", cache="+(enabled?"on":"off");
    }

    public boolean render(GeoAnimatedBlockRenderer<ModelTileBase> renderer,ModelTileBase tile,
            ResourceLocation location,float red,float green,float blue,float alpha) {
        return render(renderer,tile,location,red,green,blue,alpha,0,0);
    }
    public boolean render(GeoAnimatedBlockRenderer<ModelTileBase> renderer,ModelTileBase tile,
            ResourceLocation location,float red,float green,float blue,float alpha,int rgb,int light) {
        if(!enabled) return false;
        Key key=new Key(location,red,green,blue,alpha);
        TesselatorVertexState mesh=meshes.get(key);
        GlStateManager.disableCull();
        GlStateManager.enableRescaleNormal();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Tessellator builder=Tessellator.instance;
        if(mesh==null) {
            // Load an independent rest pose: Gecko's shared model may have been animated elsewhere.
            GeoModel model=GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),location);
            if(model==null) return false;
            builder.startDrawingQuads();
            for(GeoBone bone:model.topLevelBones)
                renderer.renderRecursively(builder,tile,bone,red,green,blue,alpha);
            // getVertexState() sorts transparent quads. Capture the original order instead.
            try {
                int length=INDEX.getInt(builder);
                int[] raw=(int[])RAW.get(builder);
                mesh=new TesselatorVertexState(length==0?new int[0]:Arrays.copyOf(raw,length),
                    length,COUNT.getInt(builder),true,false,true,true);
            } catch(IllegalAccessException error) { throw new IllegalStateException("Cannot cache decor vertices",error); }
            meshes.put(key,mesh);
            misses++;
            if(meshes.size()>LIMIT) {
                Key oldest=meshes.keySet().iterator().next();
                meshes.remove(oldest);
            }
        } else {
            hits++;
            builder.startDrawingQuads();
            if(mesh.getVertexCount()>0) builder.setVertexState(mesh);
        }
        // Tint only the Tessellator copy; shared immutable geometry remains reusable at any position.
        if(rgb!=0) try {
            int[] raw=(int[])RAW.get(builder); int length=INDEX.getInt(builder);
            for(int i=5;i<length;i+=8) raw[i]=ru.givler.mbo.client.lighting.ColoredLightRenderHooks.worldTint(raw[i],light,rgb);
        } catch(IllegalAccessException error) { throw new IllegalStateException("Cannot tint decor vertices",error); }
        builder.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        return true;
    }

    public void clear() {
        meshes.clear(); hits=misses=0;
    }
    @Override public void onResourceManagerReload(IResourceManager resources) { clear(); }

    private static final class Key {
        private final ResourceLocation location;
        private final int r,g,b,a;
        Key(ResourceLocation location,float r,float g,float b,float a) {
            this.location=location;this.r=Float.floatToIntBits(r);this.g=Float.floatToIntBits(g);
            this.b=Float.floatToIntBits(b);this.a=Float.floatToIntBits(a);
        }
        @Override public int hashCode() { return (((location.hashCode()*31+r)*31+g)*31+b)*31+a; }
        @Override public boolean equals(Object value) {
            if(!(value instanceof Key))return false;Key key=(Key)value;
            return location.equals(key.location)&&r==key.r&&g==key.g&&b==key.b&&a==key.a;
        }
    }
}
