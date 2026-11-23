package ru.givler.mbo.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;

import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import java.nio.FloatBuffer;
import org.lwjgl.util.glu.GLU;

/** Small, bounded reflection pass. Never invokes the world renderer or other mirror TESRs. */
public final class MirrorRenderer {
    private static final java.lang.reflect.Field VERTICES = cpw.mods.fml.relauncher.ReflectionHelper.findField(
        Tessellator.class, "vertexCount", "field_78406_i");
    private static int vertexCount(Tessellator tess) {
        try { return VERTICES.getInt(tess); }
        catch (IllegalAccessException error) { throw new IllegalStateException(error); }
    }
    private static final int RANGE = 8, VIEW_RANGE = 24, FAR = 13, MAX_ENTITIES = 32;
    private static final double HALF_WIDTH = 6.0 / 16, BOTTOM = 2.0 / 16,
        TOP = 30.0 / 16;
    private static final int[] NX = {0, -1, 0, 1}, NZ = {1, 0, -1, 0};
    private static final net.minecraft.util.ResourceLocation GLASS =
        new net.minecraft.util.ResourceLocation("mbo", "textures/models/decor/mirror.png");
    private MirrorRenderTarget reflection, detailTarget, distantTarget, portalTarget;
    private final java.util.ArrayList<net.minecraft.entity.EntityLivingBase> entities =
        new java.util.ArrayList<net.minecraft.entity.EntityLivingBase>();
    private final java.util.ArrayList<TileEntity> tileCandidates = new java.util.ArrayList<TileEntity>();
    private final FloatBuffer cameraMatrix = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
    private final Matrix4f inverseView = new Matrix4f();

    private static final class Visibility {
        final int query=GL15.glGenQueries();
        boolean pending,hasResult,visible=true;
        long queryView,resultView;
    }
    private final java.util.LinkedHashMap<TileEntity,Visibility> visibility=
        new java.util.LinkedHashMap<TileEntity,Visibility>(16,.75F,true);
    private net.minecraft.world.World visibilityWorld;
    private static boolean renderingReflection;
    public static boolean isRenderingReflection() { return renderingReflection; }

    public static final MirrorRenderer INSTANCE = new MirrorRenderer();

    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void worldUnloaded(net.minecraftforge.event.world.WorldEvent.Unload event) {
        if(!event.world.isRemote)return;
        for(Visibility state:visibility.values())GL15.glDeleteQueries(state.query);
        visibility.clear();visibilityWorld=null;
        SilverMirrorView.clear();
        if(detailTarget!=null)detailTarget.delete();
        if(distantTarget!=null)distantTarget.delete();
        if(portalTarget!=null)portalTarget.delete();
        reflection=detailTarget=distantTarget=portalTarget=null;
    }

    /** Runs beside the frame TESR, with its exact camera-relative origin and render destination. */
    public void renderMirror(TileEntity tile, double x, double y, double z, float tick) {
        Minecraft mc = Minecraft.getMinecraft();
        if (renderingReflection || mc.theWorld == null || !OpenGlHelper.framebufferSupported) return;
        double ox = tile.xCoord - x, oy = tile.yCoord - y, oz = tile.zCoord - z;
        cameraMatrix.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, cameraMatrix);
        inverseView.load(cameraMatrix);
        if (inverseView.invert() == null) return;
        double ex = ox + inverseView.m30, ey = oy + inverseView.m31, ez = oz + inverseView.m32;
        int facing = tile.getBlockMetadata() & 3;
        double dx = ex - (tile.xCoord + .5 - NX[facing] * .4375);
        double dz = ez - (tile.zCoord + .5 - NZ[facing] * .4375);
        double dy = ey - tile.yCoord - 1;
        if (dx * NX[facing] + dz * NZ[facing] <= .01
                || dx * dx + dy * dy + dz * dz > VIEW_RANGE * VIEW_RANGE) return;
        if (!visibleOnScreen(tile,x,y,z,ex,ey,ez)) return;
        if (dx * dx + dy * dy + dz * dz <= RANGE * RANGE) {
            if (detailTarget == null) detailTarget = new MirrorRenderTarget();
            reflection = detailTarget;
        } else {
            if (distantTarget == null) distantTarget = new MirrorRenderTarget(24, 56);
            reflection = distantTarget;
        }
        capture(mc, tile, ex, ey, ez, tick);
        draw(tile, ox, oy, oz);
    }

    /** Exact surface frustum plus asynchronous depth queries; never waits for a GPU result. */
    private boolean visibleOnScreen(TileEntity tile,double x,double y,double z,double ex,double ey,double ez) {
        int facing=tile.getBlockMetadata()&3,nx=NX[facing],nz=NZ[facing];
        double mx=x+.5-nx*.4375,mz=z+.5-nz*.4375;
        double rx=Math.abs(nz)*HALF_WIDTH+Math.abs(nx)*.005;
        double rz=Math.abs(nx)*HALF_WIDTH+Math.abs(nz)*.005;
        ClippingHelper clip=ClippingHelperImpl.getInstance();
        if(!clip.isBoxInFrustum(mx-rx,y+BOTTOM,mz-rz,mx+rx,y+TOP,mz+rz))return false;
        org.lwjgl.opengl.ContextCapabilities capabilities=org.lwjgl.opengl.GLContext.getCapabilities();
        if(!capabilities.OpenGL15 || GL15.glGetQueryi(GL15.GL_SAMPLES_PASSED,GL15.GL_CURRENT_QUERY)!=0)return true;
        if((capabilities.OpenGL33 || capabilities.GL_ARB_occlusion_query2)
                && GL15.glGetQueryi(org.lwjgl.opengl.GL33.GL_ANY_SAMPLES_PASSED,GL15.GL_CURRENT_QUERY)!=0)return true;
        if(visibilityWorld!=tile.getWorldObj()) {
            for(Visibility old:visibility.values())GL15.glDeleteQueries(old.query);
            visibility.clear();visibilityWorld=tile.getWorldObj();
        }
        Visibility state=visibility.get(tile);
        if(state==null) {
            if(visibility.size()>=64) {
                java.util.Iterator<Visibility> oldest=visibility.values().iterator();
                GL15.glDeleteQueries(oldest.next().query);oldest.remove();
            }
            state=new Visibility();visibility.put(tile,state);
        }
        // A result from a different camera must not hide a newly exposed mirror.
        long view=Double.doubleToLongBits(ex);
        view=31*view+Double.doubleToLongBits(ey);view=31*view+Double.doubleToLongBits(ez);
        projectionMatrix.clear();GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX,projectionMatrix);
        for(int i=0;i<16;i++) {
            view=31*view+Float.floatToIntBits(cameraMatrix.get(i));
            view=31*view+Float.floatToIntBits(projectionMatrix.get(i));
        }
        if(state.pending && GL15.glGetQueryObjecti(state.query,GL15.GL_QUERY_RESULT_AVAILABLE)!=0) {
            state.visible=GL15.glGetQueryObjecti(state.query,GL15.GL_QUERY_RESULT)!=0;
            state.resultView=state.queryView;state.hasResult=true;state.pending=false;
        }
        boolean visible=!state.hasResult || state.visible || state.resultView!=view;
        if(!state.pending) {
            int program=pauseShader();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            try {
                GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glDepthMask(false);GL11.glColorMask(false,false,false,false);
                GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);
                GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED,state.query);
                try { quad(mx+nx*.001,y+BOTTOM,mz+nz*.001,-nz,nx); }
                finally { GL15.glEndQuery(GL15.GL_SAMPLES_PASSED); }
                state.pending=true;state.queryView=view;
            } finally { GL11.glPopAttrib();resumeShader(program); }
        }
        return visible;
    }

    private void capture(Minecraft mc, TileEntity tile, double cx, double cy, double cz, float tick) {
        int facing = tile.getBlockMetadata() & 3;
        int nx = NX[facing], nz = NZ[facing], tx = -nz, tz = nx;
        double mx = tile.xCoord + .5 - nx * .4375, mz = tile.zCoord + .5 - nz * .4375;
        double distance = (cx - mx) * nx + (cz - mz) * nz;
        double ex = cx - 2 * distance * nx, ez = cz - 2 * distance * nz;
        double x0 = (mx - ex) * tx + (mz - ez) * tz - HALF_WIDTH;
        double y0 = tile.yCoord + BOTTOM - cy;
        captureScene(mc, tile, ex, cy, ez, nx, 0, nz, 0, 1, 0,
            x0, y0, HALF_WIDTH * 2, TOP - BOTTOM, distance, facing, mx, mz, tick);
    }

    /** Projects the exit aperture into the same bounded scene renderer used by ordinary mirrors. */
    public void renderLinkedView(TileEntity source, TileEntity exit, double x, double y, double z, float tick) {
        Minecraft mc = Minecraft.getMinecraft();
        if (renderingReflection || !OpenGlHelper.framebufferSupported || mc.theWorld == null) return;
        net.minecraftforge.common.util.ForgeDirection entryNormal = mirrorNormal(source);
        net.minecraft.util.Vec3 normal = direction(entryNormal);
        net.minecraft.util.Vec3 up = apertureUp(entryNormal);
        net.minecraft.util.Vec3 right = up.crossProduct(normal);
        net.minecraft.util.Vec3 center = net.minecraft.util.Vec3.createVectorHelper(x+.5,y+1,z+.5);
        center = center.addVector(-normal.xCoord*.4365,-normal.yCoord*.4365,-normal.zCoord*.4365);
        cameraMatrix.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,cameraMatrix);
        inverseView.load(cameraMatrix);
        if (inverseView.invert() == null) return;
        net.minecraft.util.Vec3 offset = net.minecraft.util.Vec3.createVectorHelper(
            inverseView.m30-center.xCoord,inverseView.m31-center.yCoord,inverseView.m32-center.zCoord);
        double distance = offset.dotProduct(normal), rangeSq = offset.dotProduct(offset);
        if (distance <= .01 || rangeSq > VIEW_RANGE * VIEW_RANGE) return;
        double half = HALF_WIDTH;
        double halfHeight = (TOP-BOTTOM)/2;
        double ox=source.xCoord-x, oy=source.yCoord-y, oz=source.zCoord-z;
        if (!visibleOnScreen(source,x,y,z,ox+inverseView.m30,oy+inverseView.m31,oz+inverseView.m32)) return;
        if (exit == null && source instanceof ru.givler.mbo.tileentity.TileSilverMirror) {
            ru.givler.mbo.tileentity.TileSilverMirror silver = (ru.givler.mbo.tileentity.TileSilverMirror)source;
            exit = silver.destination();
            if (exit == null) exit = SilverMirrorView.remoteExit(silver);
        }
        if (exit == null) return;
        net.minecraftforge.common.util.ForgeDirection exitNormal = mirrorNormal(exit);
        net.minecraft.util.Vec3 outward = direction(exitNormal), exitUp = apertureUp(exitNormal);
        net.minecraft.util.Vec3 exitRight = outward.crossProduct(exitUp);
        double lateral=offset.dotProduct(right), vertical=offset.dotProduct(up);
        double mx=exit.xCoord+.5-outward.xCoord*.4365, my=exit.yCoord+1;
        double mz=exit.zCoord+.5-outward.zCoord*.4365;
        double ex=mx-outward.xCoord*distance+exitRight.xCoord*lateral+exitUp.xCoord*vertical;
        double ey=my-outward.yCoord*distance+exitRight.yCoord*lateral+exitUp.yCoord*vertical;
        double ez=mz-outward.zCoord*distance+exitRight.zCoord*lateral+exitUp.zCoord*vertical;
        if (portalTarget == null) portalTarget = new MirrorRenderTarget();
        reflection = portalTarget;
        int facing = exitNormal == net.minecraftforge.common.util.ForgeDirection.WEST ? 1
            : exitNormal == net.minecraftforge.common.util.ForgeDirection.NORTH ? 2
            : exitNormal == net.minecraftforge.common.util.ForgeDirection.EAST ? 3 : 0;
        captureScene(mc,exit,ex,ey,ez,outward.xCoord,outward.yCoord,outward.zCoord,
            exitUp.xCoord,exitUp.yCoord,exitUp.zCoord,-lateral-half,-vertical-halfHeight,half*2,halfHeight*2,distance,facing,mx,mz,tick);
        int program=pauseShader();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            reflection.bindTexture();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL11.GL_TEXTURE_ENV_MODE,GL11.GL_MODULATE);
            float mix = smoothstep((VIEW_RANGE - Math.sqrt(rangeSq)) / 12);
            double blur = (1 - smoothstep((16 - Math.sqrt(rangeSq)) / 8)) * 4;
            if (blur < .1) {
                portalQuad(center,right,up,half,halfHeight,0,0,0,1,1,.3F,.7F,1F,mix);
            } else {
                // Equal-weight samples, composited over the distant artwork with total opacity 'mix'.
                double du = blur / reflection.width, dv = blur / reflection.height;
                for (int sample = 0; sample < 5; sample++) {
                    double u = sample == 1 ? du : sample == 2 ? -du : 0;
                    double v = sample == 3 ? dv : sample == 4 ? -dv : 0;
                    float alpha = mix / (5 - (4 - sample) * mix);
                    portalQuad(center,right,up,half,halfHeight,0,u,v,1+u,1+v,.3F,.7F,1F,alpha);
                }
            }
            if (source instanceof ru.givler.mbo.tileentity.TileSilverMirror) {
                mc.getTextureManager().bindTexture(RenderWallMirror.texture(true));
                GL11.glDepthMask(false);
                double seconds = (System.nanoTime() % 3_000_000_000L) / 3_000_000_000.0;
                float pulse = .075F + .025F * (float)Math.sin(seconds * 2 * Math.PI);
                portalQuad(center,right,up,half,halfHeight,.001,46.0/48,30.0/32,34.0/48,2.0/32,
                    1F,1F,1F,pulse*mix);
            }
        } finally {
            GL11.glPopAttrib();
            resumeShader(program);
        }
    }

    private static net.minecraftforge.common.util.ForgeDirection mirrorNormal(TileEntity tile) {
        int facing = tile.getBlockMetadata() & 3;
        return net.minecraftforge.common.util.ForgeDirection.getOrientation(new int[]{3,4,2,5}[facing]);
    }

    private static net.minecraft.util.Vec3 direction(net.minecraftforge.common.util.ForgeDirection facing) {
        return net.minecraft.util.Vec3.createVectorHelper(facing.offsetX,facing.offsetY,facing.offsetZ);
    }

    private static net.minecraft.util.Vec3 apertureUp(net.minecraftforge.common.util.ForgeDirection facing) {
        return net.minecraft.util.Vec3.createVectorHelper(0,facing.offsetY==0?1:0,-facing.offsetY);
    }

    private static float smoothstep(double value) {
        double t = Math.max(0, Math.min(1, value));
        return (float)(t * t * (3 - 2 * t));
    }

    private static void portalQuad(net.minecraft.util.Vec3 center,net.minecraft.util.Vec3 right,
                                   net.minecraft.util.Vec3 up,double half,double height,double depth,
                                   double u0,double v0,double u1,double v1,float r,float g,float b,float alpha) {
        net.minecraft.util.Vec3 normal = right.crossProduct(up);
        center = center.addVector(normal.xCoord*depth,normal.yCoord*depth,normal.zCoord*depth);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r,g,b,alpha);
        portalVertex(tess,center,right,up,-half,-height,u0,v0);
        portalVertex(tess,center,right,up, half,-height,u1,v0);
        portalVertex(tess,center,right,up, half, height,u1,v1);
        portalVertex(tess,center,right,up,-half, height,u0,v1);
        tess.draw();
    }

    private static void portalVertex(Tessellator tess,net.minecraft.util.Vec3 center,net.minecraft.util.Vec3 right,
                                     net.minecraft.util.Vec3 up,double x,double y,double u,double v) {
        tess.addVertexWithUV(center.xCoord+right.xCoord*x+up.xCoord*y,
            center.yCoord+right.yCoord*x+up.yCoord*y,center.zCoord+right.zCoord*x+up.zCoord*y,u,v);
    }

    private void captureScene(Minecraft mc, TileEntity tile, double ex, double cy, double ez,
                              double nx, double ny, double nz, double ux, double uy, double uz,
                              double x0, double y0, double width, double height, double distance,
                              int facing, double mx, double mz, float tick) {
        int framebuffer = GL11.glGetInteger(36006); // GL_FRAMEBUFFER_BINDING, including EXT implementations.
        int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        int program = pauseShader();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glFrustum(x0, x0 + width, y0, y0 + height, distance, distance + FAR);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GLU.gluLookAt(0,0,0,(float)nx,(float)ny,(float)nz,(float)ux,(float)uy,(float)uz);
        GL11.glTranslated(-ex, -cy, -ez);
        boolean previousReflection = renderingReflection;
        int previousPass = net.minecraftforge.client.MinecraftForgeClient.getRenderPass();
        renderingReflection = true;
        try {
            reflection.begin();
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            RenderHelper.disableStandardItemLighting();
            mc.entityRenderer.enableLightmap(tick);
            mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            ClippingHelper clip = ClippingHelperImpl.getInstance();
            boolean remote = SilverMirrorView.isRemoteScene(tile);
            net.minecraft.world.World world = remote ? tile.getWorldObj() : mc.theWorld;
            collectTiles(world, tile);
            captureBlocks = new RenderBlocks(world);
            collectBlocks(world,tile,clip);
            renderBlocks(mc, tile, ex, cy, ez, 0);
            renderTiles(world, tile, clip, ex, cy, ez, tick, 0);
            renderEntities(mc, world, tile, clip, facing, mx, mz, ex, cy, ez, tick);
            renderBlocks(mc, tile, ex, cy, ez, 1);
            renderTiles(world, tile, clip, ex, cy, ez, tick, 1);
        } finally {
            tileCandidates.clear();
            java.util.Arrays.fill(visibleBlocks,0,visibleBlockCount,null);
            visibleBlockCount=0;
            captureBlocks=null;
            renderingReflection = previousReflection;
            net.minecraftforge.client.ForgeHooksClient.setRenderPass(previousPass);
            mc.entityRenderer.disableLightmap(tick);
            OpenGlHelper.func_153171_g(36160, framebuffer);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(matrixMode);
            GL11.glPopAttrib();
            resumeShader(program);
        }
    }

    // Visible block candidates are shared by the opaque and transparent passes.
    private static final int BLOCK_COLUMNS = (FAR * 2 + 1) * (FAR * 2 + 1);
    private final Block[] visibleBlocks = new Block[BLOCK_COLUMNS * (RANGE * 2 + 1)];
    private final int[] visiblePositions = new int[visibleBlocks.length];
    private int visibleBlockCount;
    private net.minecraft.client.renderer.RenderBlocks captureBlocks;

    private void collectBlocks(net.minecraft.world.World world, TileEntity mirror, ClippingHelper clip) {
        int minY = Math.max(0,mirror.yCoord-RANGE), maxY = Math.min(255,mirror.yCoord+RANGE);
        for(int dx=-FAR;dx<=FAR;dx++) {
            int x=mirror.xCoord+dx;
            for(int dz=-FAR;dz<=FAR;dz++) {
                int z=mirror.zCoord+dz;
                if(!clip.isBoxInFrustum(x,minY,z,x+1,maxY+1,z+1)
                        || !world.blockExists(x,mirror.yCoord,z))continue;
                for(int y=minY;y<=maxY;y++) {
                    if(!clip.isBoxInFrustum(x,y,z,x+1,y+1,z+1))continue;
                    Block block=world.getBlock(x,y,z);
                    // Decorative MBO blocks are handled by the tile render pass.
                    if(block instanceof ru.givler.mbo.block.BlockMirror
                            || block instanceof ru.givler.mbo.block.BlockDecorModel
                            || block.isAir(world,x,y,z) || block.getRenderType()<0)continue;
                    visibleBlocks[visibleBlockCount]=block;
                    visiblePositions[visibleBlockCount++]=(dx+FAR)<<13 | (dz+FAR)<<8 | y;
                }
            }
        }
    }

    private void renderBlocks(Minecraft mc, TileEntity mirror, double ex, double ey, double ez, int pass) {
        net.minecraftforge.client.ForgeHooksClient.setRenderPass(pass);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1F);
        GL11.glColor4f(1,1,1,1);
        if(pass==1) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
        } else { GL11.glDisable(GL11.GL_BLEND);GL11.glDepthMask(true); }
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        try {
            for (int i=0;i<visibleBlockCount;i++) {
                Block block=visibleBlocks[i];
                if (!block.canRenderInPass(pass)) continue;
                int packed=visiblePositions[i];
                int x=mirror.xCoord+(packed>>>13)-FAR;
                int y=packed&255;
                int z=mirror.zCoord+(packed>>>8&31)-FAR;
                captureBlocks.renderBlockByRenderType(block,x,y,z);
            }
            if(pass==1 && vertexCount(tess)>0)tess.setVertexState(tess.getVertexState((float)ex,(float)ey,(float)ez));
        } finally { tess.draw();GL11.glDepthMask(true); }
    }

    private void collectTiles(net.minecraft.world.World world, TileEntity mirror) {
        tileCandidates.clear();
        net.minecraft.world.chunk.IChunkProvider provider=world.getChunkProvider();
        if(provider==null)return;
        int radius=FAR+2;
        // canUpdate()==false tiles never enter World.loadedTileEntityList. Chunk maps contain both kinds.
        for(int cx=(mirror.xCoord-radius)>>4;cx<=(mirror.xCoord+radius)>>4;cx++)
            for(int cz=(mirror.zCoord-radius)>>4;cz<=(mirror.zCoord+radius)>>4;cz++) {
                if(!provider.chunkExists(cx,cz))continue; // Never load a chunk just for a reflection.
                net.minecraft.world.chunk.Chunk chunk=world.getChunkFromChunkCoords(cx,cz);
                for(Object value:chunk.chunkTileEntityMap.values()) {
                    if(value instanceof TileEntity)tileCandidates.add((TileEntity)value);
                }
            }
    }

    private void renderTiles(net.minecraft.world.World world, TileEntity mirror, ClippingHelper clip,
                             double ex, double ey, double ez, float tick, int pass) {
        if(tileCandidates.isEmpty())return;
        net.minecraftforge.client.ForgeHooksClient.setRenderPass(pass);
        net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher dispatcher=
            net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance;
        double sx=dispatcher.staticPlayerX,sy=dispatcher.staticPlayerY,sz=dispatcher.staticPlayerZ;
        dispatcher.staticPlayerX=ex;dispatcher.staticPlayerY=ey;dispatcher.staticPlayerZ=ez;
        try {
            for(TileEntity tile:tileCandidates) {
                boolean flatMirror=tile.getBlockType() instanceof ru.givler.mbo.block.BlockMirror;
                if(tile==mirror || tile.isInvalid()
                        || tile.xCoord==mirror.xCoord && tile.yCoord==mirror.yCoord && tile.zCoord==mirror.zCoord
                        || (flatMirror ? pass!=0 || (tile.getBlockMetadata()&4)!=0
                            : !tile.shouldRenderInPass(pass) || dispatcher.getSpecialRenderer(tile)==null)
                        || tile.getDistanceFrom(mirror.xCoord+.5,mirror.yCoord+1,mirror.zCoord+.5)>(FAR+2)*(FAR+2))continue;
                net.minecraft.util.AxisAlignedBB box=tile.getRenderBoundingBox();
                if(box==null || !clip.isBoxInFrustum(box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ))continue;
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
                int program=pauseShader();
                try {
                    OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
                    int light=world.getLightBrightnessForSkyBlocks(tile.xCoord,tile.yCoord,tile.zCoord,0);
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>>16);
                    GL11.glColor4f(1,1,1,1);
                    if(flatMirror) RenderWallMirror.renderStaticTile(tile,tile.xCoord,tile.yCoord,tile.zCoord);
                    else dispatcher.renderTileEntityAt(tile,tile.xCoord,tile.yCoord,tile.zCoord,tick);
                } finally { resumeShader(program);GL11.glPopMatrix();GL11.glPopAttrib(); }
            }
        } finally {
            dispatcher.staticPlayerX=sx;dispatcher.staticPlayerY=sy;dispatcher.staticPlayerZ=sz;
        }
    }

    private void renderEntities(Minecraft mc, net.minecraft.world.World world, TileEntity tile, ClippingHelper clip, int facing,
                                double mx, double mz, double ex, double ey, double ez, float tick) {
        entities.clear();
        if (world.loadedEntityList != null) {
            for (Object value : world.loadedEntityList) {
                if (value instanceof net.minecraft.entity.EntityLivingBase) {
                    net.minecraft.entity.EntityLivingBase living = (net.minecraft.entity.EntityLivingBase)value;
                    if (reflects(living, tile, clip, mx, mz)) entities.add(living);
                }
            }
        }
        // Some client renderers keep the local player outside loadedEntityList.
        if (world == mc.theWorld && mc.thePlayer != null && !entities.contains(mc.thePlayer)
                && reflects(mc.thePlayer, tile, clip, mx, mz)) entities.add(mc.thePlayer);
        if (entities.isEmpty()) return;
        entities.sort(java.util.Comparator.comparingDouble(entity ->
            entity.getDistanceSq(mx, tile.yCoord + 1, mz)));
        RenderManager manager = RenderManager.instance;
        float yaw = manager.playerViewY, pitch = manager.playerViewX;
        double savedX = manager.viewerPosX, savedY = manager.viewerPosY, savedZ = manager.viewerPosZ;
        boolean debug = RenderManager.debugBoundingBox;
        net.minecraft.world.World previousWorld = manager.worldObj;
        boolean previousShadows=manager.options!=null && manager.options.fancyGraphics;
        try {
            manager.worldObj=world;
            if(world!=mc.theWorld && manager.options!=null) manager.options.fancyGraphics=false;
            manager.playerViewY = facing * 90;
            manager.playerViewX = 0;
            manager.viewerPosX = ex;manager.viewerPosY = ey;manager.viewerPosZ = ez;
            RenderManager.debugBoundingBox = false;
            RenderHelper.enableStandardItemLighting();
            for (int i=0; i<Math.min(MAX_ENTITIES, entities.size()); i++) {
                net.minecraft.entity.EntityLivingBase entity = entities.get(i);
                double px = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * tick;
                double py = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * tick;
                double pz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * tick;
                int light = entity.getBrightnessForRender(tick);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light & 65535, light >>> 16);
                GL11.glColor4f(1,1,1,1);
                float rotation = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * tick;
                manager.renderEntityWithPosYaw(entity, px, py, pz, rotation, tick);
            }
        } finally {
            manager.playerViewY = yaw;manager.playerViewX = pitch;
            manager.viewerPosX = savedX;manager.viewerPosY = savedY;manager.viewerPosZ = savedZ;
            RenderManager.debugBoundingBox = debug;
            manager.worldObj=previousWorld;
            if(manager.options!=null) manager.options.fancyGraphics=previousShadows;
            entities.clear();
        }
    }

    private static boolean reflects(net.minecraft.entity.EntityLivingBase entity, TileEntity tile,
                                    ClippingHelper clip, double mx, double mz) {
        boolean kind = ru.givler.mbo.mirror.MirrorActors.isVisibleKind(entity);
        if (!kind || entity.isDead || entity.isInvisible()
                || entity.getDistanceSq(mx, tile.yCoord + 1, mz) > FAR * FAR) return false;
        net.minecraft.util.AxisAlignedBB box = entity.boundingBox;
        return box != null && clip.isBoxInFrustum(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    private void draw(TileEntity tile, double cx, double cy, double cz) {
        int facing = tile.getBlockMetadata() & 3;
        int nx = NX[facing], nz = NZ[facing], tx = -nz, tz = nx;
        double mx = tile.xCoord + .5 - nx * .4375 + nx * .001 - cx;
        double mz = tile.zCoord + .5 - nz * .4375 + nz * .001 - cz;
        int program = pauseShader();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            reflection.bindTexture();
            quad(mx, tile.yCoord + BOTTOM - cy, mz, tx, tz);
            // Preserve the original centre artwork as a faint overlay; the frame stays fully opaque.
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Minecraft.getMinecraft().getTextureManager().bindTexture(GLASS);
            GL11.glDepthMask(false);
            GL11.glColor4f(1, 1, 1, .055F);
            quad(mx + nx * .001, tile.yCoord + BOTTOM - cy, mz + nz * .001, tx, tz,
                14.0 / 32, 30.0 / 32, 2.0 / 32, 2.0 / 32);
        } finally { GL11.glPopAttrib(); resumeShader(program); }
    }

    static int pauseShader() {
        if (!org.lwjgl.opengl.GLContext.getCapabilities().OpenGL20) return -1;
        int program = GL11.glGetInteger(35725);
        org.lwjgl.opengl.GL20.glUseProgram(0);
        return program;
    }
    static void resumeShader(int program) {
        if (program >= 0) org.lwjgl.opengl.GL20.glUseProgram(program);
    }

    private static void quad(double x, double y, double z, int tx, int tz) {
        quad(x, y, z, tx, tz, 0, 0, 1, 1);
    }

    private static void quad(double x, double y, double z, int tx, int tz,
                             double u0, double v0, double u1, double v1) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x - tx * HALF_WIDTH, y, z - tz * HALF_WIDTH, u0, v0);
        tess.addVertexWithUV(x + tx * HALF_WIDTH, y, z + tz * HALF_WIDTH, u1, v0);
        tess.addVertexWithUV(x + tx * HALF_WIDTH, y + TOP - BOTTOM, z + tz * HALF_WIDTH, u1, v1);
        tess.addVertexWithUV(x - tx * HALF_WIDTH, y + TOP - BOTTOM, z - tz * HALF_WIDTH, u0, v1);
        tess.draw();
    }
}
