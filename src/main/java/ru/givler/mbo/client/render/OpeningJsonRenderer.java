package ru.givler.mbo.client.render;

import com.google.gson.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.*;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import ru.givler.mbo.block.DoorBase;
import ru.givler.mbo.block.TrapDoorBase;
import ru.givler.mbo.registry.BlockRegistry;

public final class OpeningJsonRenderer {
    private static final String[] COPPER = {"copper", "exposed_copper", "weathered_copper", "oxidized_copper"};
    private static final String[] WOOD_DOORS = {"spruce", "birch", "jungle", "acacia", "dark_oak"};
    public static final OpeningJsonRenderer INSTANCE = new OpeningJsonRenderer();
    private final Map<String, Model> models = new HashMap<String, Model>();
    private final Map<String, IIcon> icons = new HashMap<String, IIcon>();

    private OpeningJsonRenderer() {
        loadDoor("oak_door");
        loadDoor("iron_door");
        for (String wood : WOOD_DOORS) loadDoor(wood + "_door");
        for (String copper : COPPER) {
            loadDoor(copper + "_door");
            loadTrapdoor(copper + "_trapdoor");
        }
        loadTrapdoor("iron_trapdoor");
    }

    private void loadDoor(String stem) {
        for (String half : new String[] {"bottom", "top"})
            for (String hinge : new String[] {"left", "right"})
                load(stem + "_" + half + "_" + hinge);
    }

    private void loadTrapdoor(String stem) {
        for (String state : new String[] {"bottom", "top", "open"}) load(stem + "_" + state);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        icons.clear();
        for (Model model : models.values()) for (String path : model.textures.values()) {
            String name = baseName(path);
            if (!icons.containsKey(name)) icons.put(name, event.map.registerIcon("mbo:openings/" + name));
        }
    }

    public static boolean render(RenderBlocks renderer, Block block, int x, int y, int z) {
        return INSTANCE.renderBlock(renderer, block, x, y, z);
    }

    public static boolean supports(Block block) {
        return block == Blocks.wooden_door || block == Blocks.iron_door
                || block == BlockRegistry.IronTrapdoor
                || block instanceof DoorBase && (
                    ((DoorBase) block).getCopperState() >= 0 || woodenDoorModel((DoorBase) block) != null)
                || block instanceof TrapDoorBase && ((TrapDoorBase) block).getCopperState() >= 0;
    }

    private static String woodenDoorModel(DoorBase door) {
        String name = door.getUnlocalizedName();
        for (String wood : WOOD_DOORS)
            if (("tile.door_" + wood + "_block").equals(name)) return wood + "_door";
        return null;
    }

    private boolean renderBlock(RenderBlocks renderer, Block block, int x, int y, int z) {
        if (icons.isEmpty()) return false;
        IBlockAccess world = renderer.blockAccess;
        String stem;
        int rotation;
        String suffix;
        int doorMeta = -1;
        int meta = world.getBlockMetadata(x, y, z);
        if (block instanceof BlockDoor) {
            if (block == Blocks.wooden_door) stem = "oak_door";
            else if (block == Blocks.iron_door) stem = "iron_door";
            else if (block instanceof DoorBase) {
                DoorBase door = (DoorBase) block;
                if (door.getCopperState() >= 0) stem = COPPER[door.getCopperState() & 3] + "_door";
                else {
                    stem = woodenDoorModel(door);
                    if (stem == null) return false;
                }
            } else return false;
            int full = ((BlockDoor) block).func_150012_g(world, x, y, z);
            doorMeta = full;
            suffix = ((meta & 8) != 0 ? "_top" : "_bottom")
                    + ((full & 16) != 0 ? "_right" : "_left");
            rotation = (full & 3) * 90;
        } else if (block instanceof BlockTrapDoor) {
            if (block == BlockRegistry.IronTrapdoor) stem = "iron_trapdoor";
            else if (block instanceof TrapDoorBase && ((TrapDoorBase) block).getCopperState() >= 0)
                stem = COPPER[((TrapDoorBase) block).getCopperState() & 3] + "_trapdoor";
            else return false;
            suffix = (meta & 4) != 0 ? "_open" : (meta & 8) != 0 ? "_top" : "_bottom";
            rotation = new int[] {0, 180, 270, 90}[meta & 3];
        } else return false;
        Model model = models.get(stem + suffix);
        if (model == null) return false;
        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        for (Element element : model.elements) renderElement(t, model, element, rotation, doorMeta, x, y, z);
        return true;
    }

    private void renderElement(Tessellator t, Model model, Element e, int rotation, int doorMeta, int x, int y, int z) {
        double x1=e.from[0]/16D, y1=e.from[1]/16D, z1=e.from[2]/16D;
        double x2=e.to[0]/16D, y2=e.to[1]/16D, z2=e.to[2]/16D;
        emit(t,model,e,"north",.8F,rotation,doorMeta,x,y,z,p(x2,y1,z1),p(x1,y1,z1),p(x1,y2,z1),p(x2,y2,z1));
        emit(t,model,e,"east",.6F,rotation,doorMeta,x,y,z,p(x2,y1,z2),p(x2,y1,z1),p(x2,y2,z1),p(x2,y2,z2));
        emit(t,model,e,"south",.8F,rotation,doorMeta,x,y,z,p(x1,y1,z2),p(x2,y1,z2),p(x2,y2,z2),p(x1,y2,z2));
        emit(t,model,e,"west",.6F,rotation,doorMeta,x,y,z,p(x1,y1,z1),p(x1,y1,z2),p(x1,y2,z2),p(x1,y2,z1));
        emit(t,model,e,"up",1F,rotation,doorMeta,x,y,z,p(x1,y2,z2),p(x2,y2,z2),p(x2,y2,z1),p(x1,y2,z1));
        emit(t,model,e,"down",.5F,rotation,doorMeta,x,y,z,p(x1,y1,z1),p(x2,y1,z1),p(x2,y1,z2),p(x1,y1,z2));
    }

    private void emit(Tessellator t, Model model, Element e, String side, float shade, int rotation, int doorMeta,
                      int wx, int wy, int wz, double[]... vertices) {
        Face face = e.faces.get(side);
        if (face == null) return;
        String path = face.texture.startsWith("#") ? model.textures.get(face.texture.substring(1)) : face.texture;
        if (path == null) return;
        IIcon icon = icons.get(baseName(path));
        if (icon == null) return;
        double u1=icon.getInterpolatedU(face.uv[0]), v1=icon.getInterpolatedV(face.uv[1]);
        double u2=icon.getInterpolatedU(face.uv[2]), v2=icon.getInterpolatedV(face.uv[3]);
        double[][] uv={{u1,v2},{u2,v2},{u2,v1},{u1,v1}};
        int turns=((face.rotation/90)%4+4)%4;
        t.setColorOpaque_F(shade,shade,shade);
        for (int i=0;i<4;i++) {
            double[] q=transform(vertices[i],e.rotation,rotation);
            if (doorMeta >= 0 && (doorMeta & 4) != 0) rotateOpenDoor(q, doorMeta);
            double[] tex=uv[(i-turns+4)%4];
            t.addVertexWithUV(wx+q[0],wy+q[1],wz+q[2],tex[0],tex[1]);
        }
    }

    private static void rotateOpenDoor(double[] p, int meta) {
        boolean right=(meta & 16) != 0;
        double h=.09375D, far=1D-h, px, pz;
        switch (meta & 3) {
            case 0: px=h; pz=right?far:h; break;
            case 1: px=right?h:far; pz=h; break;
            case 2: px=far; pz=right?h:far; break;
            default: px=right?far:h; pz=far;
        }
        double x=p[0]-px, z=p[2]-pz;
        if (right) {p[0]=px-z; p[2]=pz+x;}
        else {p[0]=px+z; p[2]=pz-x;}
    }

    private static double[] transform(double[] p, Rotation r, int rotation) {
        if (r != null) {
            double ox=r.origin[0]/16D, oy=r.origin[1]/16D, oz=r.origin[2]/16D;
            double x=p[0]-ox, y=p[1]-oy, z=p[2]-oz;
            double a=Math.toRadians(r.angle), c=Math.cos(a), s=Math.sin(a);
            double nx=x, ny=y, nz=z;
            if ("x".equals(r.axis)) {ny=y*c-z*s; nz=y*s+z*c;}
            else if ("y".equals(r.axis)) {nx=x*c+z*s; nz=-x*s+z*c;}
            else {nx=x*c-y*s; ny=x*s+y*c;}
            if (r.rescale) {
                double scale=1D/Math.cos(a);
                if (!"x".equals(r.axis)) nx*=scale;
                if (!"y".equals(r.axis)) ny*=scale;
                if (!"z".equals(r.axis)) nz*=scale;
            }
            p=p(nx+ox,ny+oy,nz+oz);
        }
        for (int i=0;i<((rotation/90)%4+4)%4;i++) p=p(1D-p[2],p[1],p[0]);
        return p;
    }

    private void load(String name) {
        InputStream in=OpeningJsonRenderer.class.getResourceAsStream("/assets/mbo/models/openings/"+name+".json");
        if (in == null) return;
        try {
            JsonObject root=new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonObject();
            Model model=new Model();
            JsonObject textures=root.getAsJsonObject("textures");
            if (textures != null) for (Map.Entry<String,JsonElement> entry:textures.entrySet())
                model.textures.put(entry.getKey(),entry.getValue().getAsString());
            for (JsonElement value:root.getAsJsonArray("elements")) {
                JsonObject o=value.getAsJsonObject();
                Element e=new Element();
                e.from=array(o.getAsJsonArray("from")); e.to=array(o.getAsJsonArray("to"));
                if (o.has("rotation")) {
                    JsonObject ro=o.getAsJsonObject("rotation");
                    e.rotation=new Rotation();
                    e.rotation.angle=ro.get("angle").getAsDouble();
                    e.rotation.axis=ro.get("axis").getAsString();
                    e.rotation.origin=array(ro.getAsJsonArray("origin"));
                    e.rotation.rescale=ro.has("rescale") && ro.get("rescale").getAsBoolean();
                }
                for (Map.Entry<String,JsonElement> entry:o.getAsJsonObject("faces").entrySet()) {
                    JsonObject fo=entry.getValue().getAsJsonObject();
                    Face f=new Face();
                    f.uv=array(fo.getAsJsonArray("uv"));
                    f.texture=fo.get("texture").getAsString();
                    f.rotation=fo.has("rotation")?fo.get("rotation").getAsInt():0;
                    e.faces.put(entry.getKey(),f);
                }
                model.elements.add(e);
            }
            models.put(name,model);
        } catch (Exception ex) {throw new RuntimeException("Could not load opening model "+name,ex);}
        finally {try {in.close();} catch (IOException ignored) {}}
    }

    private static double[] array(JsonArray a) {double[] v=new double[a.size()]; for(int i=0;i<v.length;i++)v[i]=a.get(i).getAsDouble(); return v;}
    private static double[] p(double x,double y,double z) {return new double[]{x,y,z};}
    private static String baseName(String path) {return path.substring(path.lastIndexOf('/')+1);}
    private static final class Model {final Map<String,String> textures=new HashMap<String,String>(); final List<Element> elements=new ArrayList<Element>();}
    private static final class Element {double[] from,to; Rotation rotation; final Map<String,Face> faces=new HashMap<String,Face>();}
    private static final class Face {double[] uv; String texture; int rotation;}
    private static final class Rotation {double angle; String axis; double[] origin; boolean rescale;}
}
