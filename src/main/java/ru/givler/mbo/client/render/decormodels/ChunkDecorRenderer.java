package ru.givler.mbo.client.render.decormodels;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.BlockConnectedRope;
import ru.givler.mbo.block.model.RopeConnections;
import ru.givler.mbo.client.model.decormodels.DecorResources;
import ru.givler.mbo.tileentity.ModelTileBase;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

/** Experimental atlas/chunk path. Unsupported models keep their original TESR. */
public final class ChunkDecorRenderer implements ISimpleBlockRenderingHandler {
    public static final ChunkDecorRenderer INSTANCE = new ChunkDecorRenderer();
    private static final Field RAW = ReflectionHelper.findField(Tessellator.class, "rawBuffer", "field_78405_h");
    private static final Field INDEX = ReflectionHelper.findField(Tessellator.class, "rawBufferIndex", "field_78411_s");
    private static final Field COUNT = ReflectionHelper.findField(Tessellator.class, "vertexCount", "field_78406_i");
    private static int[] canonicalVertices(Tessellator builder) throws IllegalAccessException {
        int count=COUNT.getInt(builder),length=INDEX.getInt(builder);
        int[] raw=(int[])RAW.get(builder);
        if(count==0)return new int[0];
        int stride=DecorVertexCompatibility.stride(builder,length,count);
        if(stride<8)throw new IllegalStateException("Unsupported decor vertex layout: length="+length+", count="+count);
        count=length/stride;
        int[] result=new int[count*8];
        for(int vertex=0;vertex<count;vertex++)System.arraycopy(raw,vertex*stride,result,vertex*8,8);
        return result;
    }
    private final Map<BlockModels, Mesh> meshes = new IdentityHashMap<>();
    private final Map<String,Mesh> ropeMeshes=new LinkedHashMap<>();
    private int ropeBlocks;
    private final Map<String,String> decisions = new LinkedHashMap<>();
    private int rejected;
    private ChunkDecorRenderer() {}

    @SubscribeEvent public void stitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        meshes.clear(); ropeMeshes.clear(); decisions.clear(); rejected = 0;ropeBlocks=0;
        TemplateModelRenderer renderer = new TemplateModelRenderer();
        for (BlockModels block : BlockModels.getAllModels()) {
            block.setDecorChunkReady(false);
            // Unknown subclasses can change their model or use a custom TESR.
            String name = block.getModelName();
            if (block.usesDynamicModelRender()) { reject(block,"animation or animated texture"); continue; }
            if(block.getClass()==BlockConnectedRope.class) {
                if(block.getModelOffsetY()!=0) {reject(block,"custom rope offset requires separate variant bounds");continue;}
                try {
                    if(ropeMeshes.isEmpty())bakeRopes(event,renderer,block);
                    if(ropeMeshes.isEmpty())throw new IllegalStateException("No connected rope meshes");
                    block.setDecorChunkReady(true);ropeBlocks++;
                    decisions.put(name,"CHUNK ordinary rope; decorations, camera attachments and boundaries use TESR");
                } catch(Exception error) {reject(block,"connected rope bake error: "+error);}
                continue;
            }
            if (block.getClass() != BlockModels.class
                    && block.getClass() != ru.givler.mbo.block.model.BlockModelFood.class) {
                reject(block,"custom block class: "+block.getClass().getSimpleName()); continue;
            }
            if (name.endsWith("/hook") || name.endsWith("/hanged_corpse") || name.endsWith("/suspended_corpse")
                    || name.endsWith("/rope_around_the_neck")) {
                reject(block,"first-person attachment visibility"); continue;
            }
            try {
                ResourceLocation texture = DecorResources.texture(block.getTextureName(), "");
                BufferedImage image;
                try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream()) {
                    image = ImageIO.read(stream);
                }
                if (image == null || !binaryAlpha(image)) { reject(block,"translucent texture or unreadable PNG"); continue; }
                GeoModel model = GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),
                    DecorResources.model(name));
                if (model == null) { reject(block,"missing geometry"); continue; }
                Tessellator builder = new Tessellator();
                int[] raw=bakeQuads(builder,renderer,model);
                String problem=geometryProblem(raw,block);
                if (problem!=null) { reject(block,problem); continue; }
                Mesh mesh=new Mesh(raw,DecorAtlasSprite.register(event.map,texture),block);
                meshes.put(block, mesh);
                decisions.put(name,"CHUNK; vanilla section visibility margin: 6 blocks");
                block.setDecorChunkReady(true);
            } catch (Exception error) {
                reject(block,"resource/bake error: "+error);
                // Keep the TESR on any failed resource; never suppress a model that wasn't baked.
                System.err.println("[MBO decor] Chunk bake skipped " + name + ": " + error);
            }
        }
        writeReport();
    }

    private void bakeRopes(TextureStitchEvent.Pre event,TemplateModelRenderer renderer,BlockModels block)throws Exception {
        ResourceLocation texture=DecorResources.texture("instruments_of_torture/rope_6","");
        BufferedImage image;
        try(InputStream stream=Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream()) {image=ImageIO.read(stream);}
        if(image==null || !binaryAlpha(image))throw new IllegalStateException("Unsupported rope alpha");
        IIcon icon=DecorAtlasSprite.register(event.map,texture);
        java.util.Set<Integer> masks=new java.util.HashSet<>();
        for(int mask=1;mask<64;mask++)masks.add(RopeConnections.canonicalMask(mask));
        Map<String,Mesh> prepared=new LinkedHashMap<>();
        for(int mask:masks)for(boolean floor:new boolean[]{false,true})for(boolean anchor:new boolean[]{false,true}) {
            String key=(floor?"floor_":"center_")+(anchor?"anchor_":"")+mask;
            GeoModel model=GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),
                DecorResources.model("instruments_of_torture/rope_connected/"+key));
            if(model==null)throw new IllegalStateException("Missing rope mesh "+key);
            Tessellator builder=new Tessellator();
            int[] raw=bakeQuads(builder,renderer,model);
            String problem=geometryProblem(raw,block);
            if(problem!=null)throw new IllegalStateException(key+": "+problem);
            prepared.put(key,new Mesh(raw,icon,block));
        }
        ropeMeshes.putAll(prepared);
    }

    private static int[] bakeQuads(Tessellator builder,TemplateModelRenderer renderer,GeoModel model)throws IllegalAccessException {
            DecorGeometryRecorder recorder=new DecorGeometryRecorder();
            ModelTileBase tile=new ModelTileBase();
            for(GeoBone bone:model.topLevelBones)renderer.renderRecursively(recorder,tile,bone,1,1,1,1);
            return recorder.finish();
    }
    Mesh selected(BlockModels block,IBlockAccess world,int x,int y,int z,int meta) {
        if(!(block instanceof BlockConnectedRope))return meshes.get(block);
        if(RopeConnections.decoration(meta)!=RopeConnections.NONE || cameraAttachmentNearby(world,x,y,z))return null;
        net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
        if(!(tile instanceof ModelTileBase))return null;
        BlockConnectedRope rope=(BlockConnectedRope)block;
        int mask=rope.getConnectionMask(world,x,y,z);
        String key=(((ModelTileBase)tile).ropeFloor?"floor_":"center_")
            +(rope.hasConnector(world,x,y,z)?"anchor_":"")+RopeConnections.canonicalMask(mask);
        return ropeMeshes.get(key);
    }
    private static boolean cameraAttachmentNearby(IBlockAccess world,int x,int y,int z) {
        // Static, conservative reserve around attachment blocks. Their strands stay
        // on TESR even before mounting, so first-person hiding never needs a chunk rebuild.
        for(int dx=-1;dx<=1;dx++)for(int dy=-3;dy<=3;dy++)for(int dz=-1;dz<=1;dz++) {
            if(world instanceof net.minecraft.world.World
                && !((net.minecraft.world.World)world).blockExists(x+dx,y+dy,z+dz))return true;
            Block other=world.getBlock(x+dx,y+dy,z+dz);
            if(other instanceof BlockConnectedRope) {
                if(RopeConnections.decoration(world.getBlockMetadata(x+dx,y+dy,z+dz))==RopeConnections.NOOSE)return true;
            } else if(other instanceof BlockModels) {
                String name=((BlockModels)other).getModelName();
                if(name!=null && (name.endsWith("/hook") || name.endsWith("/rope_around_the_neck")
                    || name.endsWith("/hanged_corpse") || name.endsWith("/suspended_corpse")))return true;
            }
        }
        return false;
    }
    static int renderFacing(BlockModels block,IBlockAccess world,int x,int y,int z,int meta) {
        if(!(block instanceof BlockConnectedRope))return meta&3;
        int turn=RopeConnections.rotationQuarterTurns(((BlockConnectedRope)block).getConnectionMask(world,x,y,z));
        return new int[]{2,1,0,3}[turn];
    }

    private void reject(BlockModels block,String reason) {
        rejected++;decisions.put(block.getModelName(),"TESR; "+reason);
    }
    public String writeReport() {
        File file=new File(Minecraft.getMinecraft().mcDataDir,"logs/mbo-decor-models.tsv");
        try {
            File parent=file.getParentFile();if(!parent.isDirectory()&&!parent.mkdirs())throw new java.io.IOException("Cannot create "+parent);
            try(PrintWriter writer=new PrintWriter(file,"UTF-8")) {
                writer.println("model\trouting and reason");
                for(Map.Entry<String,String> entry:decisions.entrySet())writer.println(entry.getKey()+"\t"+entry.getValue());
            }
            return "Model report: "+file.getPath();
        } catch(java.io.IOException error) {return "Cannot write model report: "+error.getMessage();}
    }

    static boolean binaryAlpha(BufferedImage image) {
        for (int y=0; y<image.getHeight(); y++) for (int x=0; x<image.getWidth(); x++)
            if ((image.getRGB(x,y) >>> 24) != 255 && (image.getRGB(x,y) >>> 24) != 0) return false;
        return true;
    }

    static boolean supported(int[] raw, BlockModels block) {
        return geometryProblem(raw,block)==null;
    }
    static String geometryProblem(int[] raw, BlockModels block) {
        if (raw.length == 0 || raw.length % 32 != 0) return "empty or malformed geometry";
        for (int i=0; i<raw.length; i+=8) {
            double[] p = position(raw, i, block, 0);
            if (!Double.isFinite(p[0]) || !Double.isFinite(p[1]) || !Double.isFinite(p[2])) return "non-finite geometry";
            float u=Float.intBitsToFloat(raw[i+3]), v=Float.intBitsToFloat(raw[i+4]);
            // Repeating UVs would sample neighbouring atlas sprites instead of repeating this PNG.
            if (!Float.isFinite(u) || !Float.isFinite(v) || u<0 || u>1 || v<0 || v>1) return "UV outside texture (atlas repetition unsupported)";
        }
        return null;
    }

    static double[] position(int[] raw, int i, BlockModels block, int meta) {
        double x=Float.intBitsToFloat(raw[i]), y=Float.intBitsToFloat(raw[i+1]), z=Float.intBitsToFloat(raw[i+2]);
        if (block.getModelName().endsWith("/hourglass")) { x-=1; z-=1; }
        double rx=x, rz=z;
        switch (meta & 3) {
            case 0: rx=-x; rz=-z; break; // SOUTH
            case 1: rx=z; rz=-x; break; // WEST
            case 2: break; // NORTH
            case 3: rx=-z; rz=x; break; // EAST
        }
        return new double[]{rx+.5, y+.01+block.getModelOffsetY(), rz+.5};
    }

    public static boolean renders(ModelTileBase tile) {
        return tile.getClass()==ModelTileBase.class && tile.getBlockType() instanceof BlockModels
            && ((BlockModels)tile.getBlockType()).usesDecorChunkRendering() && !tile.usesDynamicRender()
            && INSTANCE.fits((BlockModels)tile.getBlockType(),tile.getWorldObj(),tile.xCoord,tile.yCoord,tile.zCoord,tile.getBlockMetadata());
    }
    private boolean fits(BlockModels block,IBlockAccess world,int x,int y,int z,int meta) {
        Mesh mesh=selected(block,world,x,y,z,meta);
        return mesh!=null && fitsBounds(mesh.bounds[renderFacing(block,world,x,y,z,meta)],x,y,z);
    }
    public boolean fits(BlockModels block,int x,int y,int z,int meta) {
        Mesh mesh=meshes.get(block);return mesh!=null && fitsBounds(mesh.bounds[meta&3],x,y,z);
    }
    static boolean fitsBounds(double[] bounds,int x,int y,int z) {
        int[] position={x,y,z};
        for(int axis=0;axis<3;axis++) {
            int start=(position[axis]>>4)<<4;
            // WorldRenderer.setPosition builds BOTH its frustum bounds and occlusion
            // display list with a 6-block margin. Geometry may safely cross the
            // owning section boundary within those existing visibility bounds.
            if(position[axis]+bounds[axis]<start-6 || position[axis]+bounds[axis+3]>start+22) return false;
        }
        return true;
    }
    public String statistics() {
        return "chunk experiment="+(BlockModels.decorChunkRendering?"on":"off")
            +", eligible="+(meshes.size()+ropeBlocks)+", rope variants="+ropeMeshes.size()+", TESR fallback="+rejected;
    }
    public String inspect(BlockModels block,int x,int y,int z,int meta) {
        String reason=decisions.get(block.getModelName());
        if(BlockModels.decorInstancedRendering && block.isDecorChunkReady())return block.getModelName()+": "
            +(selected(block,Minecraft.getMinecraft().theWorld,x,y,z,meta)!=null?"GPU instances":"TESR (decoration/camera attachment)");
        if(!BlockModels.decorChunkRendering)return block.getModelName()+": TESR (chunk mode off); "+reason;
        if(!block.usesDecorChunkRendering())return block.getModelName()+": "+reason;
        return block.getModelName()+": "+(fits(block,Minecraft.getMinecraft().theWorld,x,y,z,meta)?"CHUNK"
            :"TESR (boundary, decoration or camera attachment)");
    }
    public void setEnabled(boolean value) {
        BlockModels.decorChunkRendering=value;
        if (Minecraft.getMinecraft().renderGlobal != null) Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @Override public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int id, RenderBlocks renderer) {
        if (!(block instanceof BlockModels) || !((BlockModels)block).usesDecorChunkRendering()) return false;
        int meta=world.getBlockMetadata(x,y,z);
        Mesh mesh=selected((BlockModels)block,world,x,y,z,meta);
        if (mesh==null) return false;
        Tessellator builder=Tessellator.instance;
        builder.setBrightness(world.getLightBrightnessForSkyBlocks(x,y,z,0));
        builder.setColorOpaque_F(1,1,1);
        if (!block.hasTileEntity(meta)) return false;
        meta=renderFacing((BlockModels)block,world,x,y,z,meta);
        if (!fitsBounds(mesh.bounds[meta&3],x,y,z)) return false;
        // TESR disables face culling. Emit both windings without touching GL chunk state.
        long rgbScope=ru.givler.mbo.client.lighting.ColoredLightRenderHooks.begin(renderer,x,y,z,8);
        try {
        for (int quad=0; quad<mesh.raw.length; quad+=32) for (int winding=0; winding<2; winding++)
            for (int vertex=0; vertex<4; vertex++) {
                int i=quad+(winding==0?vertex:3-vertex)*8;
                double[] p=position(mesh.raw,i,(BlockModels)block,meta);
                int shade=mesh.shading[meta&3][i/8];
                builder.setColorOpaque_I(shade | shade<<8 | shade<<16);
                builder.addVertexWithUV(x+p[0],y+p[1],z+p[2],
                    mesh.icon.getInterpolatedU(Float.intBitsToFloat(mesh.raw[i+3])*16),
                    mesh.icon.getInterpolatedV(Float.intBitsToFloat(mesh.raw[i+4])*16));
            }
        return true;
        } finally { ru.givler.mbo.client.lighting.ColoredLightRenderHooks.end(rgbScope); }
    }
    @Override public void renderInventoryBlock(Block block,int meta,int id,RenderBlocks renderer) {}
    @Override public boolean shouldRender3DInInventory(int id) { return false; }
    @Override public int getRenderId() { return BlockModels.decorChunkRenderId; }
    static final class Mesh {
        final int[] raw; final IIcon icon;
        final double[][] bounds=new double[4][6];
        final int[][] shading=new int[4][];
        Mesh(int[] raw,IIcon icon,BlockModels block) {
            this.raw=raw; this.icon=icon;
            for(int meta=0;meta<4;meta++) {
                shading[meta]=new int[raw.length/8];
                Arrays.fill(bounds[meta],0,3,Double.POSITIVE_INFINITY);
                Arrays.fill(bounds[meta],3,6,Double.NEGATIVE_INFINITY);
                for(int i=0;i<raw.length;i+=8) {
                    shading[meta][i/8]=DecorLighting.shade(raw[i+6],meta);
                    double[] p=position(raw,i,block,meta);
                    for(int axis=0;axis<3;axis++) {
                        bounds[meta][axis]=Math.min(bounds[meta][axis],p[axis]);
                        bounds[meta][axis+3]=Math.max(bounds[meta][axis+3],p[axis]);
                    }
                }
            }
        }
    }
}
