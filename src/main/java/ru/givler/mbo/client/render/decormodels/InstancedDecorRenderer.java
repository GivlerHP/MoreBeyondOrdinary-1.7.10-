package ru.givler.mbo.client.render.decormodels;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.ModelTileBase;

/** Opt-in GL 3.3 compatibility renderer: one VBO per mesh, one draw per visible mesh group. */
public final class InstancedDecorRenderer implements IResourceManagerReloadListener {
    public static final InstancedDecorRenderer INSTANCE=new InstancedDecorRenderer();
    private final Map<ChunkDecorRenderer.Mesh,GpuMesh> meshes=new IdentityHashMap<>();
    private int program,instancesVbo,instanceAttribute,lightAttribute;
    private int submitted,draws,visible;
    private long meshBytes;
    private String failure="";
    private Object world;
    private InstancedDecorRenderer() {}

    private static final String VERTEX=
        "#version 120\nattribute vec4 aInstance; attribute vec2 aLight;\n"
        +"varying vec2 vUv; varying vec2 vLight; varying float vShade; varying float vDistance;\n"
        +"uniform int uRadial;\n"
        +"vec3 turn(vec3 p,float r){if(r<0.5)return vec3(-p.x,p.y,-p.z);"
        +"if(r<1.5)return vec3(p.z,p.y,-p.x);if(r<2.5)return p;return vec3(-p.z,p.y,p.x);}\n"
        +"void main(){vec3 p=turn(gl_Vertex.xyz,aInstance.w)+aInstance.xyz;"
        +"vec3 n=turn(gl_Normal,aInstance.w);"
        +"vec3 l=normalize(vec3(0.2,1.0,-0.7));vec3 q=normalize(vec3(-0.2,1.0,0.7));"
        +"vShade=clamp(0.4+0.6*(max(0.0,dot(n,l))+max(0.0,dot(n,q))),0.0,1.0);"
        +"vUv=gl_MultiTexCoord0.xy;vLight=(gl_TextureMatrix[1]*vec4(aLight,0.0,1.0)).xy;"
        +"vec4 eye=gl_ModelViewMatrix*vec4(p,1.0);vDistance=uRadial!=0?length(eye.xyz):abs(eye.z);"
        +"gl_Position=gl_ProjectionMatrix*eye;}\n";
    private static final String FRAGMENT=
        "#version 120\nuniform sampler2D uTexture;uniform sampler2D uLightmap;"
        +"uniform int uFog;uniform int uFogMode;uniform float uAlpha;\n"
        +"varying vec2 vUv;varying vec2 vLight;varying float vShade;varying float vDistance;\n"
        +"void main(){vec4 tex=texture2D(uTexture,vUv);if(tex.a<=uAlpha)discard;"
        +"vec4 color=tex*vec4(vec3(vShade),1.0)*texture2D(uLightmap,vLight);"
        +"if(uFog!=0){float f;if(uFogMode==9729)f=(gl_Fog.end-vDistance)*gl_Fog.scale;"
        +"else if(uFogMode==2048)f=exp(-gl_Fog.density*vDistance);"
        +"else {float d=gl_Fog.density*vDistance;f=exp(-d*d);}"
        +"color.rgb=mix(gl_Fog.color.rgb,color.rgb,clamp(f,0.0,1.0));}gl_FragColor=color;}\n";

    public String setEnabled(boolean enabled) {
        if(enabled) {
            if(!GLContext.getCapabilities().OpenGL33)return "GPU instances unavailable: OpenGL 3.3 compatibility required.";
            if(GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)!=0)return "GPU instances unavailable while another shader program is active.";
            try { initialize();failure=""; } catch(RuntimeException error) { clear();failure=error.toString();return "GPU initialization failed: "+failure; }
        }
        BlockModels.decorInstancedRendering=enabled;
        if(!enabled)clear();
        Minecraft mc=Minecraft.getMinecraft();if(mc.renderGlobal!=null)mc.renderGlobal.loadRenderers();
        return statistics();
    }
    public boolean renders(ModelTileBase tile) {
        if(!BlockModels.decorInstancedRendering || program==0 || tile.getClass()!=ModelTileBase.class
                || tile.getWorldObj()==null || !(tile.getBlockType() instanceof BlockModels) || tile.usesDynamicRender())return false;
        BlockModels block=(BlockModels)tile.getBlockType();
        return block.isDecorChunkReady() && ChunkDecorRenderer.INSTANCE.selected(block,tile.getWorldObj(),
            tile.xCoord,tile.yCoord,tile.zCoord,tile.getBlockMetadata())!=null;
    }
    public String statistics() {
        return "GPU instances="+(BlockModels.decorInstancedRendering?"on":"off")+", meshes="+meshes.size()
            +", geometry="+meshBytes+" bytes, submitted="+submitted+", visible="+visible+", draws="+draws
            +(failure.isEmpty()?"":", error="+failure);
    }

    private static int shader(int type,String source) {
        int shader=GL20.glCreateShader(type);GL20.glShaderSource(shader,source);GL20.glCompileShader(shader);
        if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==GL11.GL_FALSE) {
            String message=GL20.glGetShaderInfoLog(shader,8192);GL20.glDeleteShader(shader);throw new IllegalStateException(message);
        }
        return shader;
    }
    private void initialize() {
        if(program!=0)return;
        int vertex=0,fragment=0,newProgram=0;
        try {
            vertex=shader(GL20.GL_VERTEX_SHADER,VERTEX);fragment=shader(GL20.GL_FRAGMENT_SHADER,FRAGMENT);
            newProgram=GL20.glCreateProgram();GL20.glAttachShader(newProgram,vertex);GL20.glAttachShader(newProgram,fragment);
            GL20.glBindAttribLocation(newProgram,6,"aInstance");GL20.glBindAttribLocation(newProgram,7,"aLight");
            GL20.glLinkProgram(newProgram);
            if(GL20.glGetProgrami(newProgram,GL20.GL_LINK_STATUS)==GL11.GL_FALSE)
                throw new IllegalStateException(GL20.glGetProgramInfoLog(newProgram,8192));
            program=newProgram;newProgram=0;
            instanceAttribute=GL20.glGetAttribLocation(program,"aInstance");lightAttribute=GL20.glGetAttribLocation(program,"aLight");
            instancesVbo=GL15.glGenBuffers();
        } finally {
            if(vertex!=0)GL20.glDeleteShader(vertex);if(fragment!=0)GL20.glDeleteShader(fragment);
            if(newProgram!=0)GL20.glDeleteProgram(newProgram);
        }
    }

    @SubscribeEvent public void renderWorld(RenderWorldLastEvent event) {
        if(!BlockModels.decorInstancedRendering || program==0)return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc.theWorld==null || mc.renderViewEntity==null)return;
        if(GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)!=0) {
            failure="External shader detected; GPU mode disabled";setEnabled(false);return;
        }
        if(world!=mc.theWorld) {clearMeshes();world=mc.theWorld;}
        long start=DecorRenderDiagnostics.beginGpu();
        Entity camera=mc.renderViewEntity;
        double cx=camera.lastTickPosX+(camera.posX-camera.lastTickPosX)*event.partialTicks;
        double cy=camera.lastTickPosY+(camera.posY-camera.lastTickPosY)*event.partialTicks;
        double cz=camera.lastTickPosZ+(camera.posZ-camera.lastTickPosZ)*event.partialTicks;
        Frustrum frustum=new Frustrum();frustum.setPosition(cx,cy,cz);
        for(GpuMesh mesh:meshes.values())mesh.instances.clear();
        submitted=visible=draws=0;
        int oldBuffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int oldActive=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),oldClient=GL11.glGetInteger(GL13.GL_CLIENT_ACTIVE_TEXTURE);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glMatrixMode(GL11.GL_TEXTURE);GL11.glPushMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW);
        try {
            // Non-ticking decorative TileEntities are absent from World.loadedTileEntityList.
            // RenderGlobal maintains the TESR candidates collected from rebuilt chunk sections,
            // independently of the per-object frustum checks done by vanilla's TESR dispatcher.
            for(Object object:mc.renderGlobal.tileEntities) {
                if(!(object instanceof ModelTileBase))continue;
                ModelTileBase tile=(ModelTileBase)object;if(!renders(tile))continue;
                submitted++;
                BlockModels block=(BlockModels)tile.getBlockType();
                ChunkDecorRenderer.Mesh mesh=ChunkDecorRenderer.INSTANCE.selected(block,mc.theWorld,tile.xCoord,tile.yCoord,tile.zCoord,tile.getBlockMetadata());
                int facing=ChunkDecorRenderer.renderFacing(block,mc.theWorld,tile.xCoord,tile.yCoord,tile.zCoord,tile.getBlockMetadata());
                double[] b=mesh.bounds[facing];
                double minX=tile.xCoord+b[0],minY=tile.yCoord+b[1],minZ=tile.zCoord+b[2];
                double maxX=tile.xCoord+b[3],maxY=tile.yCoord+b[4],maxZ=tile.zCoord+b[5];
                if(distanceSquared(cx,cy,cz,minX,minY,minZ,maxX,maxY,maxZ)>tile.getMaxRenderDistanceSquared()
                    || !frustum.isBoxInFrustum(minX,minY,minZ,maxX,maxY,maxZ))continue;
                GpuMesh gpu=meshes.get(mesh);
                if(gpu==null) {gpu=new GpuMesh(mesh,block);meshes.put(mesh,gpu);meshBytes+=gpu.bytes;}
                gpu.instances.add(tile);visible++;
            }
            mc.entityRenderer.enableLightmap(event.partialTicks);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
            GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            useProgram();
            for(GpuMesh gpu:meshes.values()) {
                int count=gpu.instances.size();if(count==0)continue;
                gpu.ensureCapacity(count);gpu.data.clear();
                for(ModelTileBase tile:gpu.instances) {
                    BlockModels block=(BlockModels)tile.getBlockType();
                    int facing=ChunkDecorRenderer.renderFacing(block,mc.theWorld,tile.xCoord,tile.yCoord,tile.zCoord,tile.getBlockMetadata());
                    int light=mc.theWorld.getLightBrightnessForSkyBlocks(tile.xCoord,tile.yCoord,tile.zCoord,0);
                    gpu.data.put((float)(tile.xCoord-cx+.5)).put((float)(tile.yCoord-cy+.01+block.getModelOffsetY()))
                        .put((float)(tile.zCoord-cz+.5)).put(facing).put(light&65535).put(light>>>16);
                }
                gpu.data.flip();mc.renderEngine.bindTexture(gpu.texture);draw(gpu,gpu.data,count);draws++;
            }
        } catch(RuntimeException error) {
            failure="GPU render failed: "+error;
            BlockModels.decorInstancedRendering=false;
            System.err.println("[MBO decor] "+failure);
            if(mc.renderGlobal!=null)mc.renderGlobal.loadRenderers();
        } finally {
            GL20.glUseProgram(0);
            if(instanceAttribute>=0)GL33.glVertexAttribDivisor(instanceAttribute,0);
            if(lightAttribute>=0)GL33.glVertexAttribDivisor(lightAttribute,0);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glMatrixMode(GL11.GL_TEXTURE);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopClientAttrib();GL11.glPopAttrib();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,oldBuffer);
            GL13.glActiveTexture(oldActive);GL13.glClientActiveTexture(oldClient);
            DecorRenderDiagnostics.endGpu(start,visible,draws);
        }
    }
    static double distanceSquared(double x,double y,double z,double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        double dx=Math.max(0,Math.max(minX-x,x-maxX)),dy=Math.max(0,Math.max(minY-y,y-maxY)),dz=Math.max(0,Math.max(minZ-z,z-maxZ));
        return dx*dx+dy*dy+dz*dz;
    }
    private void useProgram() {
        GL20.glUseProgram(program);
        GL20.glUniform1i(GL20.glGetUniformLocation(program,"uTexture"),0);
        GL20.glUniform1i(GL20.glGetUniformLocation(program,"uLightmap"),1);
        GL20.glUniform1i(GL20.glGetUniformLocation(program,"uFog"),GL11.glIsEnabled(GL11.GL_FOG)?1:0);
        GL20.glUniform1i(GL20.glGetUniformLocation(program,"uFogMode"),GL11.glGetInteger(GL11.GL_FOG_MODE));
        boolean radial=GLContext.getCapabilities().GL_NV_fog_distance && GL11.glGetInteger(0x855A)==0x855B;
        GL20.glUniform1i(GL20.glGetUniformLocation(program,"uRadial"),radial?1:0);
        GL20.glUniform1f(GL20.glGetUniformLocation(program,"uAlpha"),GL11.glIsEnabled(GL11.GL_ALPHA_TEST)?GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF):.1F);
    }
    private void draw(GpuMesh gpu,FloatBuffer data,int count) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,gpu.vbo);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glVertexPointer(3,GL11.GL_FLOAT,32,0L);GL11.glTexCoordPointer(2,GL11.GL_FLOAT,32,12L);GL11.glNormalPointer(GL11.GL_FLOAT,32,20L);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,instancesVbo);GL15.glBufferData(GL15.GL_ARRAY_BUFFER,data,GL15.GL_STREAM_DRAW);
        GL20.glEnableVertexAttribArray(instanceAttribute);GL20.glEnableVertexAttribArray(lightAttribute);
        GL20.glVertexAttribPointer(instanceAttribute,4,GL11.GL_FLOAT,false,24,0L);
        GL20.glVertexAttribPointer(lightAttribute,2,GL11.GL_FLOAT,false,24,16L);
        GL33.glVertexAttribDivisor(instanceAttribute,1);GL33.glVertexAttribDivisor(lightAttribute,1);
        GL31.glDrawArraysInstanced(GL11.GL_QUADS,0,gpu.vertices,count);
        GL33.glVertexAttribDivisor(instanceAttribute,0);GL33.glVertexAttribDivisor(lightAttribute,0);
        GL20.glDisableVertexAttribArray(instanceAttribute);GL20.glDisableVertexAttribArray(lightAttribute);
    }
    private static final class GpuMesh {
        final int vbo,vertices;final long bytes;final ResourceLocation texture;
        final ArrayList<ModelTileBase> instances=new ArrayList<>();FloatBuffer data=BufferUtils.createFloatBuffer(6*16);
        GpuMesh(ChunkDecorRenderer.Mesh mesh,BlockModels block) {
            int[] raw=mesh.raw;vertices=raw.length/8;bytes=vertices*32L;
            FloatBuffer buffer=BufferUtils.createFloatBuffer(vertices*8);
            boolean hourglass=block.getModelName().endsWith("/hourglass");
            for(int i=0;i<raw.length;i+=8) {
                buffer.put(Float.intBitsToFloat(raw[i])+(hourglass?1:0)).put(Float.intBitsToFloat(raw[i+1]))
                    .put(Float.intBitsToFloat(raw[i+2])+(hourglass?1:0));
                buffer.put(Float.intBitsToFloat(raw[i+3])).put(Float.intBitsToFloat(raw[i+4]));
                int normal=raw[i+6];buffer.put(Math.max(-1,(byte)normal/127F)).put(Math.max(-1,(byte)(normal>>8)/127F)).put(Math.max(-1,(byte)(normal>>16)/127F));
            }
            buffer.flip();vbo=GL15.glGenBuffers();GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,vbo);GL15.glBufferData(GL15.GL_ARRAY_BUFFER,buffer,GL15.GL_STATIC_DRAW);
            ResourceLocation icon=new ResourceLocation(mesh.icon.getIconName());
            texture=mesh.icon instanceof DecorAtlasSprite ? ((DecorAtlasSprite)mesh.icon).source
                : new ResourceLocation(icon.getResourceDomain(),"textures/blocks/"+icon.getResourcePath()+".png");
        }
        void ensureCapacity(int count) {if(data.capacity()<count*6)data=BufferUtils.createFloatBuffer(Integer.highestOneBit(count*6-1)<<1);}
    }
    private void clearMeshes() {
        for(GpuMesh mesh:meshes.values())GL15.glDeleteBuffers(mesh.vbo);meshes.clear();meshBytes=0;submitted=visible=draws=0;
    }
    private void clear() {
        clearMeshes();if(instancesVbo!=0)GL15.glDeleteBuffers(instancesVbo);if(program!=0)GL20.glDeleteProgram(program);
        instancesVbo=program=0;world=null;
    }
    @SubscribeEvent public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
        if(world==event.world) {clearMeshes();world=null;}
    }
    @Override public void onResourceManagerReload(IResourceManager resources) {
        clear();
        if(BlockModels.decorInstancedRendering) {
            try {initialize();}catch(RuntimeException error){failure=error.toString();BlockModels.decorInstancedRendering=false;}
        }
    }
}
