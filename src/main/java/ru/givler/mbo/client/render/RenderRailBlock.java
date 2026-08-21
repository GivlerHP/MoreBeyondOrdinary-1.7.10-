package ru.givler.mbo.client.render;

import com.google.gson.*;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.MoreBeyondOrdinary;

import java.io.*;
import java.util.*;

/** Renders the supplied modern rail JSON models in the 1.7.10 block renderer. */
public final class RenderRailBlock implements ISimpleBlockRenderingHandler {
    private static final String[] TEXTURES = {"rail", "rail0", "rail_base", "rail_corner",
            "powered_rail", "powered_rail_on", "detector_rail", "detector_rail_on",
            "activator_rail", "activator_rail_on"};
    private static final String[] MODELS = {"rail", "rail_curved", "rail_raised_ne", "rail_raised_sw",
            "powered_rail", "powered_rail_on", "powered_rail_raised_ne", "powered_rail_raised_sw",
            "powered_rail_on_raised_ne", "powered_rail_on_raised_sw",
            "detector_rail", "detector_rail_on", "detector_rail_raised_ne", "detector_rail_raised_sw",
            "detector_rail_on_raised_ne", "detector_rail_on_raised_sw",
            "activator_rail", "activator_rail_on", "activator_rail_raised_ne", "activator_rail_raised_sw",
            "activator_rail_on_raised_ne", "activator_rail_on_raised_sw"};

    private final int renderId;
    private final Map<String, Model> models = new HashMap<String, Model>();
    private final Map<String, IIcon> icons = new HashMap<String, IIcon>();

    public RenderRailBlock(int renderId) {
        this.renderId = renderId;
        for (String name : MODELS) models.put(name, load(name));
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        icons.clear();
        for (String name : TEXTURES) {
            icons.put(name, event.map.registerIcon(MoreBeyondOrdinary.MODID + ":rails/" + name));
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        Choice choice = choose(block, world.getBlockMetadata(x, y, z));
        Model model = models.get(choice.model);
        if (model == null) return false;
        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        for (Element element : model.elements) renderElement(t, model, element, choice.yRotation, x, y, z);
        return true;
    }

    private Choice choose(Block block, int meta) {
        String prefix;
        if (block == Blocks.golden_rail) prefix = "powered_rail";
        else if (block == Blocks.detector_rail) prefix = "detector_rail";
        else if (block == Blocks.activator_rail) prefix = "activator_rail";
        else return railChoice(meta);

        boolean on = (meta & 8) != 0;
        int shape = meta & 7;
        String base = prefix + (on ? "_on" : "");
        if (shape == 0) return new Choice(base, 0);
        if (shape == 1) return new Choice(base, 90);
        if (shape == 2) return new Choice(base + "_raised_ne", 90);
        if (shape == 3) return new Choice(base + "_raised_sw", 90);
        if (shape == 4) return new Choice(base + "_raised_ne", 0);
        if (shape == 5) return new Choice(base + "_raised_sw", 0);
        return new Choice(base, 0);
    }

    private static Choice railChoice(int shape) {
        shape &= 15;
        if (shape == 0) return new Choice("rail", 0);
        if (shape == 1) return new Choice("rail", 90);
        if (shape == 2) return new Choice("rail_raised_ne", 90);
        if (shape == 3) return new Choice("rail_raised_sw", 90);
        if (shape == 4) return new Choice("rail_raised_ne", 0);
        if (shape == 5) return new Choice("rail_raised_sw", 0);
        return new Choice("rail_curved", (shape - 6) * 90);
    }

    private void renderElement(Tessellator t, Model model, Element e, int yRotation, int wx, int wy, int wz) {
        double x1=e.from[0]/16D,y1=e.from[1]/16D,z1=e.from[2]/16D;
        double x2=e.to[0]/16D,y2=e.to[1]/16D,z2=e.to[2]/16D;
        emit(t,model,e,"north",.8F,yRotation,wx,wy,wz, p(x2,y1,z1),p(x1,y1,z1),p(x1,y2,z1),p(x2,y2,z1));
        emit(t,model,e,"east", .6F,yRotation,wx,wy,wz, p(x2,y1,z2),p(x2,y1,z1),p(x2,y2,z1),p(x2,y2,z2));
        emit(t,model,e,"south",.8F,yRotation,wx,wy,wz, p(x1,y1,z2),p(x2,y1,z2),p(x2,y2,z2),p(x1,y2,z2));
        emit(t,model,e,"west", .6F,yRotation,wx,wy,wz, p(x1,y1,z1),p(x1,y1,z2),p(x1,y2,z2),p(x1,y2,z1));
        emit(t,model,e,"up",   1F,yRotation,wx,wy,wz, p(x1,y2,z2),p(x2,y2,z2),p(x2,y2,z1),p(x1,y2,z1));
        emit(t,model,e,"down",.5F,yRotation,wx,wy,wz, p(x1,y1,z1),p(x2,y1,z1),p(x2,y1,z2),p(x1,y1,z2));
    }

    private void emit(Tessellator t, Model model, Element e, String side, float shade, int yRotation,
                      int wx, int wy, int wz, double[]... vertices) {
        Face f=e.faces.get(side); if(f==null)return;
        String texture=model.textures.get(f.texture.substring(1));
        IIcon icon=icons.get(baseName(texture)); if(icon==null)return;
        double u1=icon.getInterpolatedU(f.uv[0]),v1=icon.getInterpolatedV(f.uv[1]);
        double u2=icon.getInterpolatedU(f.uv[2]),v2=icon.getInterpolatedV(f.uv[3]);
        double[][] uv={{u1,v2},{u2,v2},{u2,v1},{u1,v1}};
        int turns=((f.rotation/90)%4+4)%4;
        t.setColorOpaque_F(shade,shade,shade);
        for(int n=0;n<4;n++){
            double[] q=transform(vertices[n],e.rotation,yRotation);
            double[] tex=uv[(n-turns+4)%4];
            t.addVertexWithUV(wx+q[0],wy+q[1],wz+q[2],tex[0],tex[1]);
        }
    }

    private static double[] transform(double[] p, Rotation r, int yRot) {
        if(r!=null){
            double ox=r.origin[0]/16D,oy=r.origin[1]/16D,oz=r.origin[2]/16D;
            double x=p[0]-ox,y=p[1]-oy,z=p[2]-oz;
            double a=Math.toRadians(r.angle),c=Math.cos(a),s=Math.sin(a);
            double nx=x,ny=y,nz=z;
            if("x".equals(r.axis)){ny=y*c-z*s;nz=y*s+z*c;}
            else if("y".equals(r.axis)){nx=x*c+z*s;nz=-x*s+z*c;}
            else {nx=x*c-y*s;ny=x*s+y*c;}
            if(r.rescale){double scale=1D/Math.cos(a); if(!"x".equals(r.axis))nx*=scale;if(!"y".equals(r.axis))ny*=scale;if(!"z".equals(r.axis))nz*=scale;}
            p=new double[]{nx+ox,ny+oy,nz+oz};
        }
        int turns=((yRot/90)%4+4)%4;
        for(int i=0;i<turns;i++)p=new double[]{1D-p[2],p[1],p[0]};
        return p;
    }

    private static double[] p(double x,double y,double z){return new double[]{x,y,z};}
    private static String baseName(String path){int i=path.lastIndexOf('/');return i<0?path:path.substring(i+1);}

    private static Model load(String name) {
        InputStream in=RenderRailBlock.class.getResourceAsStream("/assets/mbo/models/rails/"+name+".json");
        if(in==null)return null;
        try {
            Reader reader=new InputStreamReader(in,"UTF-8");
            JsonObject root=new JsonParser().parse(reader).getAsJsonObject();
            Model m=new Model();
            JsonObject textures=root.getAsJsonObject("textures");
            for(Map.Entry<String,JsonElement> v:textures.entrySet())m.textures.put(v.getKey(),v.getValue().getAsString());
            for(JsonElement value:root.getAsJsonArray("elements")){
                JsonObject o=value.getAsJsonObject(); Element e=new Element();
                e.from=array(o.getAsJsonArray("from"));e.to=array(o.getAsJsonArray("to"));
                if(o.has("rotation")){JsonObject ro=o.getAsJsonObject("rotation");e.rotation=new Rotation();e.rotation.angle=ro.get("angle").getAsDouble();e.rotation.axis=ro.get("axis").getAsString();e.rotation.origin=array(ro.getAsJsonArray("origin"));e.rotation.rescale=ro.has("rescale")&&ro.get("rescale").getAsBoolean();}
                for(Map.Entry<String,JsonElement> entry:o.getAsJsonObject("faces").entrySet()){
                    JsonObject fo=entry.getValue().getAsJsonObject();Face f=new Face();f.uv=array(fo.getAsJsonArray("uv"));f.texture=fo.get("texture").getAsString();f.rotation=fo.has("rotation")?fo.get("rotation").getAsInt():0;e.faces.put(entry.getKey(),f);
                }
                m.elements.add(e);
            }
            reader.close();return m;
        } catch(Exception ex){throw new RuntimeException("Could not load rail model "+name,ex);}
        finally {try{in.close();}catch(IOException ignored){}}
    }
    private static double[] array(JsonArray a){double[] r=new double[a.size()];for(int i=0;i<r.length;i++)r[i]=a.get(i).getAsDouble();return r;}

    @Override public void renderInventoryBlock(Block block,int metadata,int modelId,RenderBlocks renderer){
        IIcon icon=block.getIcon(1,metadata);Tessellator t=Tessellator.instance;GL11.glPushMatrix();GL11.glTranslatef(-.5F,-.5F,0F);t.startDrawingQuads();t.setColorOpaque_F(1,1,1);t.setNormal(0,0,1);renderer.renderFaceZPos(block,0,0,0,icon);t.draw();GL11.glPopMatrix();
    }
    @Override public boolean shouldRender3DInInventory(int modelId){return false;}
    @Override public int getRenderId(){return renderId;}

    private static final class Choice{final String model;final int yRotation;Choice(String m,int y){model=m;yRotation=y;}}
    private static final class Model{final Map<String,String> textures=new HashMap<String,String>();final List<Element> elements=new ArrayList<Element>();}
    private static final class Element{double[] from,to;Rotation rotation;final Map<String,Face> faces=new HashMap<String,Face>();}
    private static final class Face{double[] uv;String texture;int rotation;}
    private static final class Rotation{double angle;String axis;double[] origin;boolean rescale;}
}
