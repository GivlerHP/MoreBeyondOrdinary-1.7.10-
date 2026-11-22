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
    private MirrorRenderTarget reflection, detailTarget, distantTarget;
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
        if(detailTarget!=null)detailTarget.delete();
        if(distantTarget!=null)distantTarget.delete();
        reflection=detailTarget=distantTarget=null;
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
        int framebuffer = GL11.glGetInteger(36006); // GL_FRAMEBUFFER_BINDING, including EXT implementations.
        int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        int program = pauseShader();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glFrustum(x0, x0 + 2 * HALF_WIDTH, y0, y0 + TOP - BOTTOM, distance, distance + FAR);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GLU.gluLookAt(0, 0, 0, nx, 0, nz, 0, 1, 0);
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
            collectTiles(mc, tile);
            renderBlocks(mc, tile, clip, mx, mz, ex, cy, ez, 0);
            renderTiles(mc, tile, clip, ex, cy, ez, tick, 0);
            renderEntities(mc, tile, clip, facing, mx, mz, ex, cy, ez, tick);
            renderBlocks(mc, tile, clip, mx, mz, ex, cy, ez, 1);
            renderTiles(mc, tile, clip, ex, cy, ez, tick, 1);
        } finally {
            tileCandidates.clear();
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

    private void renderBlocks(Minecraft mc, TileEntity mirror, ClippingHelper clip, double mx, double mz,
                              double ex, double ey, double ez, int pass) {
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
        RenderBlocks blocks = new RenderBlocks(mc.theWorld);
        Tessellator tess = Tessellator.instance;
        int facing=mirror.getBlockMetadata()&3,nx=NX[facing],nz=NZ[facing];
        tess.startDrawingQuads();
        try {
            for(int x=mirror.xCoord-FAR;x<=mirror.xCoord+FAR;x++)
                for(int z=mirror.zCoord-FAR;z<=mirror.zCoord+FAR;z++) {
                    double depth=(x+.5-mx)*nx+(z+.5-mz)*nz;
                    if(depth < -.5 || depth > FAR+.5 || !mc.theWorld.blockExists(x,mirror.yCoord,z))continue;
                    for(int y=Math.max(0,mirror.yCoord-RANGE);y<=Math.min(255,mirror.yCoord+RANGE);y++) {
                        if(!clip.isBoxInFrustum(x,y,z,x+1,y+1,z+1))continue;
                        Block block=mc.theWorld.getBlock(x,y,z);
                        // Decorative MBO blocks use their TESR below, even when the main view batches them.
                        if(block instanceof ru.givler.mbo.block.BlockMirror
                                || block instanceof ru.givler.mbo.block.BlockDecorModel
                                || block.isAir(mc.theWorld,x,y,z) || block.getRenderType()<0
                                || !block.canRenderInPass(pass))continue;
                        blocks.renderBlockByRenderType(block,x,y,z);
                    }
                }
            if(pass==1 && vertexCount(tess)>0)tess.setVertexState(tess.getVertexState((float)ex,(float)ey,(float)ez));
        } finally { tess.draw();GL11.glDepthMask(true); }
    }

    private void collectTiles(Minecraft mc, TileEntity mirror) {
        tileCandidates.clear();
        net.minecraft.world.chunk.IChunkProvider provider=mc.theWorld.getChunkProvider();
        if(provider==null)return;
        int radius=FAR+2;
        // canUpdate()==false tiles never enter World.loadedTileEntityList. Chunk maps contain both kinds.
        for(int cx=(mirror.xCoord-radius)>>4;cx<=(mirror.xCoord+radius)>>4;cx++)
            for(int cz=(mirror.zCoord-radius)>>4;cz<=(mirror.zCoord+radius)>>4;cz++) {
                if(!provider.chunkExists(cx,cz))continue; // Never load a chunk just for a reflection.
                net.minecraft.world.chunk.Chunk chunk=mc.theWorld.getChunkFromChunkCoords(cx,cz);
                for(Object value:chunk.chunkTileEntityMap.values()) {
                    if(value instanceof TileEntity)tileCandidates.add((TileEntity)value);
                }
            }
    }

    private void renderTiles(Minecraft mc, TileEntity mirror, ClippingHelper clip,
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
                    int light=mc.theWorld.getLightBrightnessForSkyBlocks(tile.xCoord,tile.yCoord,tile.zCoord,0);
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

    private void renderEntities(Minecraft mc, TileEntity tile, ClippingHelper clip, int facing,
                                double mx, double mz, double ex, double ey, double ez, float tick) {
        entities.clear();
        if (mc.theWorld.loadedEntityList != null) {
            for (Object value : mc.theWorld.loadedEntityList) {
                if (value instanceof net.minecraft.entity.EntityLivingBase) {
                    net.minecraft.entity.EntityLivingBase living = (net.minecraft.entity.EntityLivingBase)value;
                    if (reflects(living, tile, clip, mx, mz)) entities.add(living);
                }
            }
        }
        // Some client renderers keep the local player outside loadedEntityList.
        if (mc.thePlayer != null && !entities.contains(mc.thePlayer)
                && reflects(mc.thePlayer, tile, clip, mx, mz)) entities.add(mc.thePlayer);
        if (entities.isEmpty()) return;
        entities.sort(java.util.Comparator.comparingDouble(entity ->
            entity.getDistanceSq(mx, tile.yCoord + 1, mz)));
        RenderManager manager = RenderManager.instance;
        float yaw = manager.playerViewY, pitch = manager.playerViewX;
        double savedX = manager.viewerPosX, savedY = manager.viewerPosY, savedZ = manager.viewerPosZ;
        boolean debug = RenderManager.debugBoundingBox;
        try {
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
            entities.clear();
        }
    }

    private static boolean reflects(net.minecraft.entity.EntityLivingBase entity, TileEntity tile,
                                    ClippingHelper clip, double mx, double mz) {
        boolean kind = entity instanceof net.minecraft.entity.player.EntityPlayer
            || entity instanceof net.minecraft.entity.passive.EntityAnimal
            || entity instanceof net.minecraft.entity.passive.EntityWaterMob
            || entity instanceof net.minecraft.entity.passive.EntityAmbientCreature;
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
