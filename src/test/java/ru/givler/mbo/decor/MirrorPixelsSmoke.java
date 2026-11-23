package ru.givler.mbo.decor;

import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;
import ru.givler.mbo.client.render.*;
import sun.misc.Unsafe;

/** Actual block capture, offscreen FBO with vanilla FBO disabled, active shader, and four wall facings. */
public final class MirrorPixelsSmoke {
    static Unsafe unsafe;
    static final class Tile extends TileEntity {
        int facing;
        @Override public int getBlockMetadata(){return facing;}
        @Override public net.minecraft.util.AxisAlignedBB getRenderBoundingBox(){
            return net.minecraft.util.AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord,xCoord+1,yCoord+2,zCoord+1);
        }
    }
    static final class Cube extends Block {
        IIcon icon;
        Cube(){super(Material.rock);}
        @Override public IIcon getIcon(int side,int meta){return icon;}
        @Override public int getMixedBrightnessForBlock(net.minecraft.world.IBlockAccess w,int x,int y,int z){return 0xf000f0;}
        @Override public boolean shouldSideBeRendered(net.minecraft.world.IBlockAccess w,int x,int y,int z,int side){return true;}
    }
    static final class Air extends Block {
        Air(){super(Material.air);}
        @Override public int getRenderType(){return -1;}
        @Override public boolean isOpaqueCube(){return false;}
    }
    static final class Scene extends WorldClient {
        Block cube,air,special;boolean fireFloor;int bx,bz,calls,facing,removals;
        java.util.Map<Long,net.minecraft.world.chunk.Chunk> chunks;
        net.minecraft.world.chunk.IChunkProvider provider;
        static long chunkKey(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
        @Override public net.minecraft.world.chunk.IChunkProvider getChunkProvider(){return provider;}
        @Override public net.minecraft.world.chunk.Chunk getChunkFromChunkCoords(int x,int z){return chunks.get(chunkKey(x,z));}
        void addStaticTile(TileEntity tile){
            int x=tile.xCoord>>4,z=tile.zCoord>>4;
            net.minecraft.world.chunk.Chunk chunk=chunks.get(chunkKey(x,z));
            if(chunk==null){chunk=new net.minecraft.world.chunk.Chunk(this,x,z);chunks.put(chunkKey(x,z),chunk);}
            chunk.chunkTileEntityMap.put(new net.minecraft.world.ChunkPosition(tile.xCoord&15,tile.yCoord,tile.zCoord&15),tile);
        }
        Scene(){super(null,null,0,EnumDifficulty.NORMAL,null);}
        @Override public Block getBlock(int x,int y,int z){if(special!=null && x==0 && y==64 && z==2)return special;if(fireFloor && x==0 && y==63 && z==2)return cube;if(x==0 && (y==64 || y==65) && z==0)return ru.givler.mbo.registry.DecorBlockRegistry.ModelMirror;if(x==bx && y==64 && z==bz){calls++;return cube;}return air;}
        @Override public int getBlockMetadata(int x,int y,int z){return y==65?facing|4:facing;}
        @Override public boolean setBlockToAir(int x,int y,int z){removals++;return true;}
        @Override public boolean blockExists(int x,int y,int z){return true;}
        @Override public int getLightBrightnessForSkyBlocks(int x,int y,int z,int minimum){return 0xf000f0;}
    }
    static final class Lighting extends EntityRenderer {
        Lighting(){super(null,null);}
        @Override public void enableLightmap(double tick){}
        @Override public void disableLightmap(double tick){}
    }
    static final class Fire extends net.minecraft.block.BlockFire {
        IIcon icon;int iconCalls;
        @Override public IIcon getFireIcon(int side){iconCalls++;return icon;}
        @Override public int getMixedBrightnessForBlock(net.minecraft.world.IBlockAccess w,int x,int y,int z){return 0xf000f0;}
    }
    static byte[] image(){
        ByteBuffer b=BufferUtils.createByteBuffer(96*224*4);GL11.glReadPixels(0,0,96,224,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,b);
        byte[] bytes=new byte[b.remaining()];b.get(bytes);return bytes;
    }
    static final class Cow extends net.minecraft.entity.passive.EntityCow {
        Cow(){super(null);}
        @Override public boolean isInvisible(){return false;}
        @Override public int getBrightnessForRender(float tick){return 0xf000f0;}
    }
    static final class Player extends net.minecraft.client.entity.EntityOtherPlayerMP {
        Player(){super(null,null);}
        @Override public boolean isInvisible(){return false;}
        @Override public int getBrightnessForRender(float tick){return 0xf000f0;}
    }
    static final class EntityPicture extends net.minecraft.client.renderer.entity.Render {
        int calls;
        @Override protected ResourceLocation getEntityTexture(net.minecraft.entity.Entity e){return null;}
        @Override public void doRenderShadowAndFire(net.minecraft.entity.Entity e,double x,double y,double z,float yaw,float tick){}
        @Override public void doRender(net.minecraft.entity.Entity e,double x,double y,double z,float yaw,float tick){
            calls++;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glColor4f(0,1,0,1);GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d(x-.4,y,z);GL11.glVertex3d(x+.4,y,z);
            GL11.glVertex3d(x+.4,y+1.4,z);GL11.glVertex3d(x-.4,y+1.4,z);GL11.glEnd();GL11.glPopAttrib();
        }
    }
    static <T extends net.minecraft.entity.EntityLivingBase> T entity(Class<T> type,Scene scene,double z)throws Exception{
        T e=(T)unsafe.allocateInstance(type);e.worldObj=scene;e.width=.6F;e.height=1.4F;
        e.posX=e.lastTickPosX=.5;e.posY=e.lastTickPosY=64;e.posZ=e.lastTickPosZ=z;
        Field b=net.minecraft.entity.Entity.class.getDeclaredField("boundingBox");b.setAccessible(true);
        b.set(e,net.minecraft.util.AxisAlignedBB.getBoundingBox(.1,64,z-.3,.9,65.4,z+.3));return e;
    }
    static int texture(BufferedImage image){
        int id=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,id);
        ByteBuffer data=BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*4);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
            int c=image.getRGB(x,y);data.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));
        }
        data.flip();GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,image.getWidth(),image.getHeight(),0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,data);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);return id;
    }
    static int shader(){
        int vertex=GL20.glCreateShader(GL20.GL_VERTEX_SHADER),fragment=GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(vertex,"void main(){gl_Position=ftransform();}");GL20.glCompileShader(vertex);
        GL20.glShaderSource(fragment,"void main(){gl_FragColor=vec4(1,0,1,1);}");GL20.glCompileShader(fragment);
        int program=GL20.glCreateProgram();GL20.glAttachShader(program,vertex);GL20.glAttachShader(program,fragment);GL20.glLinkProgram(program);
        if(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)==0)throw new AssertionError(GL20.glGetProgramInfoLog(program,4096));return program;
    }
    static int[] pixel(int x,int y){
        ByteBuffer data=BufferUtils.createByteBuffer(4);GL11.glReadPixels(x,y,1,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,data);
        return new int[]{data.get(0)&255,data.get(1)&255,data.get(2)&255};
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Field field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);unsafe=(Unsafe)field.get(null);
        Pbuffer context=new Pbuffer(128,256,new PixelFormat(8,24,0),null,null);context.makeCurrent();
        Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,unsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
        Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);
        mods.set(loader.get(null),new java.util.HashMap<>());
        net.minecraft.init.Bootstrap.func_151354_b();
        StaticDecorPixelsSmoke.fakeMinecraft();Minecraft mc=Minecraft.getMinecraft();mc.gameSettings=new GameSettings();
        mc.gameSettings.fboEnable=false;mc.gameSettings.ambientOcclusion=0;
        OpenGlHelper.initializeTextures();
        check(OpenGlHelper.framebufferSupported,"No FBO support");check(!OpenGlHelper.isFramebufferEnabled(),"Test requires vanilla FBO disabled, no rear support");
        final int original=texture(ImageIO.read(new java.io.File("src/main/resources/assets/mbo/textures/models/decor/mirror.png")));
        BufferedImage red=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);red.setRGB(0,0,0xffff0000);final int atlas=texture(red);
        mc.renderEngine=new TextureManager(mc.getResourceManager()){
            @Override public void bindTexture(ResourceLocation location){OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);GL11.glBindTexture(GL11.GL_TEXTURE_2D,location.getResourcePath().contains("atlas")?atlas:original);}
        };
        mc.entityRenderer=(Lighting)unsafe.allocateInstance(Lighting.class);
        Scene scene=(Scene)unsafe.allocateInstance(Scene.class);mc.theWorld=scene;
        scene.chunks=new java.util.HashMap<>();
        scene.provider=(net.minecraft.world.chunk.IChunkProvider)Proxy.newProxyInstance(
            net.minecraft.world.chunk.IChunkProvider.class.getClassLoader(),new Class[]{net.minecraft.world.chunk.IChunkProvider.class},(p,m,a)->{
                if(m.getName().equals("chunkExists"))return scene.chunks.containsKey(Scene.chunkKey((Integer)a[0],(Integer)a[1]));
                if(m.getReturnType()==boolean.class)return false;if(m.getReturnType()==int.class)return 0;return null;
            });
        ru.givler.mbo.registry.DecorBlockRegistry.ModelMirror=new ru.givler.mbo.block.BlockMirror();
        Cube cube=new Cube();cube.icon=(IIcon)Proxy.newProxyInstance(IIcon.class.getClassLoader(),new Class[]{IIcon.class},(p,m,a)->{
            String n=m.getName();if(n.equals("getMaxU")||n.equals("getMaxV"))return 1F;
            if(n.startsWith("getInterpolated"))return ((Double)a[0]).floatValue()/16;
            if(m.getReturnType()==float.class)return 0F;if(m.getReturnType()==int.class)return 16;return "test";
        });scene.cube=cube;scene.air=new Air();
        MirrorRenderTarget target=new MirrorRenderTarget(),output=new MirrorRenderTarget();
        MirrorRenderer renderer=new MirrorRenderer();field=MirrorRenderer.class.getDeclaredField("reflection");field.setAccessible(true);field.set(renderer,target);
        field=MirrorRenderer.class.getDeclaredField("detailTarget");field.setAccessible(true);field.set(renderer,target);
        Method capture=MirrorRenderer.class.getDeclaredMethod("capture",Minecraft.class,TileEntity.class,double.class,double.class,double.class,float.class);capture.setAccessible(true);
        Method draw=MirrorRenderer.class.getDeclaredMethod("draw",TileEntity.class,double.class,double.class,double.class);draw.setAccessible(true);
        int program=shader();int[] nx={0,-1,0,1},nz={1,0,-1,0};Tile tile=new Tile();tile.yCoord=64;
        for(int f=0;f<4;f++){
            tile.facing=f;scene.facing=f;scene.bx=nx[f]*3;scene.bz=nz[f]*3;
            ru.givler.mbo.block.BlockMirror block=ru.givler.mbo.registry.DecorBlockRegistry.ModelMirror;
            block.setBlockBoundsBasedOnState(scene,0,64,0);
            double thickness=nx[f]!=0?block.getBlockBoundsMaxX()-block.getBlockBoundsMinX():block.getBlockBoundsMaxZ()-block.getBlockBoundsMinZ();
            check(Math.abs(thickness-1.0/16)<1e-8,"Wall mirror is not one pixel deep");
            for(int half=0;half<2;half++) {
                net.minecraft.util.AxisAlignedBB collision=block.getCollisionBoundingBoxFromPool(scene,0,64+half,0);
                check(collision!=null && collision.minY==64+half && collision.maxY==65+half,"Mirror half has no collision");
                double planeX=.5-nx[f]*.4375,planeZ=.5-nz[f]*.4375;
                double actorX=planeX+nx[f]*.6,actorZ=planeZ+nz[f]*.6;
                net.minecraft.util.AxisAlignedBB actor=net.minecraft.util.AxisAlignedBB.getBoundingBox(
                    actorX-.3,64.1+half,actorZ-.3,actorX+.3,65.9+half,actorZ+.3);
                double allowed=nx[f]!=0?collision.calculateXOffset(actor,-nx[f]):collision.calculateZOffset(actor,-nz[f]);
                check(Math.abs(allowed)<.4,"Player can walk through the mirror, facing="+f+" half="+half);
            }
            block.onNeighborBlockChange(scene,0,64,0,scene.air);
            block.onNeighborBlockChange(scene,0,65,0,scene.air);
            check(scene.removals==0,"Mirror requires blocks behind it");
            output.begin();GL20.glUseProgram(program);GL11.glDepthFunc(GL11.GL_EQUAL);
            double ex=.5+nx[f]*2,ez=.5+nz[f]*2;
            capture.invoke(renderer,mc,tile,ex,64.5,ez,0F);
            check(GL11.glGetInteger(36006)==output.framebuffer,"Capture lost destination FBO");
            check(GL11.glGetInteger(35725)==program,"Capture lost shader program");
            OpenGlHelper.func_153171_g(36160,target.framebuffer);GL11.glReadBuffer(36064);
            int[] captured=pixel(48,48);
            check(captured[0]>80 && captured[1]<20 && captured[2]<20,"Actual scene cube absent from capture, facing="+f+" pixel="+java.util.Arrays.toString(captured));
            output.begin();GL20.glUseProgram(0);
            GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-.5,.5,-1,1,.1,8);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GLU.gluLookAt((float)ex,65,(float)ez,.5F,65,.5F,0,1,0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D,original);RenderWallMirror.renderPanel(0,64,0,f);
            GL20.glUseProgram(program);draw.invoke(renderer,tile,0D,0D,0D);
            check(GL11.glGetInteger(35725)==program,"Surface lost shader program");
            int[] surface=pixel(48,32);
            check(surface[0]>70 && surface[1]<40 && surface[2]<40,"Captured scene not shown on mirror, facing="+f+" pixel="+java.util.Arrays.toString(surface));
            int[] edge=pixel(4,112);check(edge[0]>50 && edge[1]>20,"Original frame was overwritten");
            if(f==0) {
                ByteBuffer pixels=BufferUtils.createByteBuffer(96*224*4);
                GL11.glReadPixels(0,0,96,224,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
                BufferedImage image=new BufferedImage(96,224,BufferedImage.TYPE_INT_ARGB);
                for(int y=0;y<224;y++)for(int x=0;x<96;x++){
                    int i=(y*96+x)*4;
                    image.setRGB(x,223-y,0xff000000|((pixels.get(i)&255)<<16)|((pixels.get(i+1)&255)<<8)|(pixels.get(i+2)&255));
                }
                ImageIO.write(image,"png",new java.io.File("build/mirror-preview.png"));
            }
            // Exercise the same entry used by the frame TESR, without a late world-render event.
            scene.facing=f;
            ru.givler.mbo.tileentity.TileMirror realTile=new ru.givler.mbo.tileentity.TileMirror();
            realTile.yCoord=64;realTile.setWorldObj(scene);
            field=World.class.getDeclaredField("loadedTileEntityList");field.setAccessible(true);
            field.set(scene,java.util.Collections.singletonList(realTile));
            net.minecraft.entity.player.EntityPlayerMP viewer=(net.minecraft.entity.player.EntityPlayerMP)unsafe.allocateInstance(net.minecraft.entity.player.EntityPlayerMP.class);
            viewer.posX=viewer.lastTickPosX=ex;viewer.posY=viewer.lastTickPosY=64.5;viewer.posZ=viewer.lastTickPosZ=ez;
            mc.renderViewEntity=viewer;
            output.begin();GL20.glUseProgram(program);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
            GLU.gluLookAt(0,0,0,-nx[f],0,-nz[f],0,1,0);
            renderer.renderMirror(realTile,-ex,64-64.5,-ez,0);
            int[] eventPixel=pixel(48,112);
            check(eventPixel[0]>70 && eventPixel[1]<40 && eventPixel[2]<40,"TESR reflection produced no image, facing="+f+" pixel="+java.util.Arrays.toString(eventPixel));
            check(GL11.glGetInteger(35725)==program,"TESR reflection lost shader");
            check(GL11.glGetError()==GL11.GL_NO_ERROR,"GL error, facing="+f);
        }
        // The same capture must invoke renderers for an animal and another player, but not animals behind the glass.
        tile.facing=scene.facing=0;scene.bx=0;scene.bz=3;
        net.minecraft.client.renderer.entity.RenderManager manager=net.minecraft.client.renderer.entity.RenderManager.instance;
        manager.options=mc.gameSettings;manager.renderEngine=mc.renderEngine;
        EntityPicture picture=new EntityPicture();picture.setRenderManager(manager);
        manager.entityRenderMap.put(Cow.class,picture);manager.entityRenderMap.put(Player.class,picture);
        Cow cow=entity(Cow.class,scene,2);Player player=entity(Player.class,scene,2);
        for(net.minecraft.entity.EntityLivingBase e:new net.minecraft.entity.EntityLivingBase[]{cow,player}){
            scene.loadedEntityList=java.util.Collections.singletonList(e);int calls=picture.calls;
            capture.invoke(renderer,mc,tile,.5,64.5,2.5,0F);
            check(picture.calls==calls+1,"Animal/player renderer was not called");
            OpenGlHelper.func_153171_g(36160,target.framebuffer);GL11.glReadBuffer(36064);
            int[] animal=pixel(48,48);check(animal[1]>100 && animal[0]<20,"Entity image absent from reflection");
        }
        scene.loadedEntityList=java.util.Collections.singletonList(entity(Cow.class,scene,-2));
        int calls=picture.calls;capture.invoke(renderer,mc,tile,.5,64.5,2.5,0F);
        check(picture.calls==calls,"Animal behind mirror was rendered");
        java.util.List<net.minecraft.entity.Entity> herd=new java.util.ArrayList<>();
        for(int i=0;i<40;i++)herd.add(entity(Cow.class,scene,2));scene.loadedEntityList=herd;
        calls=picture.calls;capture.invoke(renderer,mc,tile,.5,64.5,2.5,0F);
        check(picture.calls==calls+32,"Entity rendering is not capped at 32");
        scene.loadedEntityList=java.util.Collections.emptyList();
        output.begin();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-.5,.5,-1,1,.1,32);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GLU.gluLookAt(0,0,0,0,0,-1,0,1,0);
        ru.givler.mbo.tileentity.TileMirror distantTile=new ru.givler.mbo.tileentity.TileMirror();distantTile.yCoord=64;distantTile.setWorldObj(scene);
        renderer.renderMirror(distantTile,-.5,-.5,-10,0);
        field=MirrorRenderer.class.getDeclaredField("distantTarget");field.setAccessible(true);MirrorRenderTarget low=(MirrorRenderTarget)field.get(renderer);
        check(low!=null && low.width==24 && low.height==56,"Distant reflection did not switch to low resolution");
        OpenGlHelper.func_153171_g(36160,low.framebuffer);GL11.glReadBuffer(36064);
        int[] distant=pixel(12,12);check(distant[0]>80 && distant[1]<20,"Distant reflection is empty");
        int blockCalls=scene.calls;renderer.renderMirror(distantTile,-.5,-.5,-30,0);
        check(scene.calls==blockCalls,"Live reflection exceeds 24 blocks");
        // Actual vanilla fire renderer, with a solid support block and no ordinary cube behind it.
        field=MirrorRenderer.class.getDeclaredField("reflection");field.setAccessible(true);field.set(renderer,target);
        scene.bx=50;scene.bz=50;scene.special=null;scene.fireFloor=false;
        capture.invoke(renderer,mc,tile,.5,64.5,2.5,0F);
        OpenGlHelper.func_153171_g(36160,target.framebuffer);GL11.glReadBuffer(36064);byte[] empty=image();
        Fire fire=new Fire();fire.icon=cube.icon;scene.special=fire;scene.fireFloor=true;
        capture.invoke(renderer,mc,tile,.5,64.5,2.5,0F);
        OpenGlHelper.func_153171_g(36160,target.framebuffer);GL11.glReadBuffer(36064);
        check(fire.iconCalls>0 && !java.util.Arrays.equals(empty,image()),"Fire renderer was omitted");
        // Load and render a real MBO wanted.geo.json via its ordinary ModelTileBase TESR.
        scene.fireFloor=false;scene.special=new ru.givler.mbo.block.BlockDecorModel(Material.wood,"mirror_test","wanted","wanted");
        ru.givler.mbo.tileentity.ModelTileBase decor=new ru.givler.mbo.tileentity.ModelTileBase("wanted","wanted");
        decor.yCoord=64;decor.zCoord=2;decor.setWorldObj(scene);
        net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher dispatcher=net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance;
        dispatcher.field_147553_e=mc.renderEngine;dispatcher.field_147550_f=scene;
        software.bernie.geckolib3.GeckoLib.hasInitialized=true;
        ResourceLocation wanted=new ResourceLocation("mbo","geo/wanted.geo.json");
        software.bernie.geckolib3.resource.GeckoLibCache.getInstance().getGeoModels().put(wanted,
            software.bernie.geckolib3.file.GeoModelLoader.getInstance().loadModel(mc.getResourceManager(),wanted));
        ru.givler.mbo.client.render.decormodels.TemplateModelRenderer decorRenderer=new ru.givler.mbo.client.render.decormodels.TemplateModelRenderer();
        decorRenderer.func_147497_a(dispatcher);dispatcher.mapSpecialRenderers.put(ru.givler.mbo.tileentity.ModelTileBase.class,decorRenderer);
        field=World.class.getDeclaredField("loadedTileEntityList");field.setAccessible(true);field.set(scene,java.util.Collections.emptyList());
        check(!decor.canUpdate(),"Regression fixture must be non-ticking MBO decor");
        scene.addStaticTile(decor);
        capture.invoke(renderer,mc,tile,.5,64.5,2.5,0F);
        OpenGlHelper.func_153171_g(36160,target.framebuffer);GL11.glReadBuffer(36064);
        check(!java.util.Arrays.equals(empty,image()),"MBO Geo model is missing from reflection");
        check(!MirrorRenderer.isRenderingReflection(),"Reflection recursion guard leaked");
        scene.special=null;field.set(scene,java.util.Collections.emptyList());scene.chunks.clear();
        // A second mirror is only a PNG model inside the reflection, never another capture.
        scene.special=ru.givler.mbo.registry.DecorBlockRegistry.ModelMirror;
        ru.givler.mbo.tileentity.TileMirror otherMirror=new ru.givler.mbo.tileentity.TileMirror();
        otherMirror.yCoord=64;otherMirror.zCoord=2;otherMirror.setWorldObj(scene);
        check(!otherMirror.canUpdate(),"Regression fixture must be a non-ticking mirror");
        scene.addStaticTile(otherMirror);
        field=World.class.getDeclaredField("loadedTileEntityList");field.setAccessible(true);field.set(scene,java.util.Collections.emptyList());
        capture.invoke(renderer,mc,tile,.5,64.5,2.5,0F);
        OpenGlHelper.func_153171_g(36160,target.framebuffer);GL11.glReadBuffer(36064);
        check(!java.util.Arrays.equals(empty,image()),"Other mirror PNG is absent from reflection");
        scene.special=null;scene.bx=0;scene.bz=3;field.set(scene,java.util.Collections.emptyList());scene.chunks.clear();
        // No capture when looking away, nor when an opaque screen covers the entire aperture.
        output.begin();GL20.glUseProgram(0);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-.5,.5,-1,1,.1,32);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glRotatef(180,0,1,0);
        blockCalls=scene.calls;renderer.renderMirror(distantTile,-.5,-.5,-2,0);
        check(scene.calls==blockCalls,"Offscreen mirror still captures the world");
        GL11.glLoadIdentity();GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthFunc(GL11.GL_LEQUAL);GL11.glDepthMask(true);
        GL11.glBegin(GL11.GL_QUADS);GL11.glVertex3d(-1,-1,-1);GL11.glVertex3d(1,-1,-1);GL11.glVertex3d(1,1,-1);GL11.glVertex3d(-1,1,-1);GL11.glEnd();
        for(int warmup=0;warmup<3;warmup++){renderer.renderMirror(distantTile,-.5,-.5,-2,0);GL11.glFinish();}
        blockCalls=scene.calls;renderer.renderMirror(distantTile,-.5,-.5,-2,0);
        check(scene.calls==blockCalls,"Fully occluded mirror still captures the world");
        GL11.glFinish();output.begin();renderer.renderMirror(distantTile,-.5,-.5,-2,0);GL11.glFinish();
        blockCalls=scene.calls;renderer.renderMirror(distantTile,-.5,-.5,-2,0);
        check(scene.calls>blockCalls,"Newly exposed mirror never resumes capture");
        check(GL11.glGetError()==GL11.GL_NO_ERROR,"Occlusion queries leaked GL errors");
        // Back of the panel must sample the new right-hand atlas half, not the front image.
        output.begin();GL20.glUseProgram(0);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-.5,.5,-1,1,.1,8);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GLU.gluLookAt(.5F,1,-3,.5F,1,0,0,1,0);
        mc.getTextureManager().bindTexture(new ResourceLocation("mbo","textures/models/decor/mirror.png"));
        RenderWallMirror.renderPanel(0,0,0,0);
        int back=ImageIO.read(new java.io.File("src/main/resources/assets/mbo/textures/models/decor/mirror.png")).getRGB(24,16);
        int[] backPixel=pixel(48,112);
        check(Math.abs(backPixel[0]-(back>>16&255))<3 && Math.abs(backPixel[1]-(back>>8&255))<3
            && Math.abs(backPixel[2]-(back&255))<3,"Back panel does not use the added atlas area: "+java.util.Arrays.toString(backPixel));
        // Exercise Forge's real inventory-item hook, including item-renderer selection and GUI transforms.
        ru.givler.mbo.item.ItemBlockMirror mirrorItem=new ru.givler.mbo.item.ItemBlockMirror(ru.givler.mbo.registry.DecorBlockRegistry.ModelMirror);
        net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(mirrorItem);
        check(ru.givler.mbo.registry.DecorBlockRegistry.ModelMirror.stepSound.getBreakSound().equals(Block.soundTypeGlass.getBreakSound()),"Mirror breaking sound is not glass");
        RenderMirrorItem itemRenderer=new RenderMirrorItem();
        check(itemRenderer.shouldUseRenderHelper(net.minecraftforge.client.IItemRenderer.ItemRenderType.INVENTORY,stack,
            net.minecraftforge.client.IItemRenderer.ItemRendererHelper.INVENTORY_BLOCK),"Inventory does not use the Gecko-style model transform");
        net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(mirrorItem,itemRenderer);
        output.begin();GL11.glViewport(0,0,96,96);
        GL11.glClearColor(0,0,0,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(0,16,16,0,-100,100);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
        blockCalls=scene.calls;
        check(net.minecraftforge.client.ForgeHooksClient.renderInventoryItem(new net.minecraft.client.renderer.RenderBlocks(),mc.renderEngine,stack,false,0,0,0),"Forge did not select the mirror item renderer");
        ByteBuffer itemPixels=BufferUtils.createByteBuffer(96*96*4);GL11.glReadPixels(0,0,96,96,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,itemPixels);
        BufferedImage preview=new BufferedImage(96,96,BufferedImage.TYPE_INT_ARGB);
        int visiblePixels=0,minX=96,maxX=-1,minY=96,maxY=-1;
        for(int py=0;py<96;py++)for(int px=0;px<96;px++) {
            int i=(py*96+px)*4,r=itemPixels.get(i)&255,g=itemPixels.get(i+1)&255,b=itemPixels.get(i+2)&255;
            preview.setRGB(px,95-py,0xff000000|(r<<16)|(g<<8)|b);
            if(r+g+b>20){visiblePixels++;minX=Math.min(minX,px);maxX=Math.max(maxX,px);minY=Math.min(minY,py);maxY=Math.max(maxY,py);}
        }
        check(visiblePixels>1000 && minX>2 && maxX<93 && minY>2 && maxY<93,"Inventory model is empty or clipped");
        check(scene.calls==blockCalls,"Inventory item captured the world");
        check(GL11.glGetError()==GL11.GL_NO_ERROR,"Item renderer leaked GL errors");
        ImageIO.write(preview,"png",new java.io.File("build/mirror-inventory-preview.png"));
        // Forge's actual equipped hook must use 3D block transforms and expose the silver front.
        for(net.minecraftforge.client.IItemRenderer.ItemRenderType heldType:new net.minecraftforge.client.IItemRenderer.ItemRenderType[]{
                net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON,
                net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED}) {
            check(itemRenderer.shouldUseRenderHelper(heldType,stack,net.minecraftforge.client.IItemRenderer.ItemRendererHelper.EQUIPPED_BLOCK)
                && itemRenderer.shouldUseRenderHelper(heldType,stack,net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D),"Held mirror uses flat-item transforms");
            output.begin();GL11.glViewport(0,0,96,224);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-1,1,-1,1,-10,10);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
            boolean thirdPerson=heldType==net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED;
            if(thirdPerson)GL11.glRotatef(20,1,0,0);
            GL11.glRotatef(45,0,1,0);
            if(thirdPerson)GL11.glScalef(-1,-1,1);
            net.minecraftforge.client.ForgeHooksClient.renderEquippedItem(heldType,itemRenderer,new net.minecraft.client.renderer.RenderBlocks(),null,stack);
            int[] held=pixel(41,thirdPerson?102:129);
            check(held[2]>held[0]+3,"Held mirror shows back or edge, type="+heldType+" pixel="+java.util.Arrays.toString(held));
            check(GL11.glGetError()==GL11.GL_NO_ERROR,"Held item leaked GL errors");
        }
        // Only the distant exit has a red block. The source must show that block through its aperture.
        Tile portalSource=new Tile();portalSource.facing=0;portalSource.yCoord=64;
        Tile portalExit=new Tile();portalExit.facing=0;portalExit.xCoord=20;portalExit.yCoord=64;
        scene.bx=20;scene.bz=3;
        output.begin();
        GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-.5,.5,-.5,.5,.1,8);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
        renderer.renderLinkedView(portalSource,portalExit,-.5,-.5,-2,0);
        int[] through=pixel(48,112);
        check(through[0]>40 && through[0]<85 && through[1]<20 && through[2]<20,
            "Linked mirror did not show the tinted exit scene: "+java.util.Arrays.toString(through));
        // A streamed chunk must survive encoding, and exist only in the detached render world.
        Field worldProvider=World.class.getDeclaredField("provider");worldProvider.setAccessible(true);
        net.minecraft.world.WorldProviderSurface surfaceProvider=new net.minecraft.world.WorldProviderSurface();
        worldProvider.set(scene,surfaceProvider);
        WorldSettings settings=new WorldSettings(0,WorldSettings.GameType.SURVIVAL,false,false,WorldType.DEFAULT);
        Field worldInfo=World.class.getDeclaredField("worldInfo");worldInfo.setAccessible(true);
        worldInfo.set(scene,new net.minecraft.world.storage.WorldInfo(settings,"mirror-test"));
        surfaceProvider.registerWorld(scene);
        net.minecraft.world.chunk.Chunk sent=new net.minecraft.world.chunk.Chunk(scene,10,11);
        net.minecraft.world.chunk.storage.ExtendedBlockStorage[] sections=new net.minecraft.world.chunk.storage.ExtendedBlockStorage[16];
        sections[4]=new net.minecraft.world.chunk.storage.ExtendedBlockStorage(64,true);
        sections[4].func_150818_a(2,0,3,net.minecraft.init.Blocks.stone);
        sections[4].setExtBlockMetadata(2,0,3,1);
        sent.setStorageArrays(sections);
        ru.givler.mbo.network.packet.PacketSilverMirrorScene outgoing=new ru.givler.mbo.network.packet.PacketSilverMirrorScene();
        outgoing.addChunk(new net.minecraft.network.play.server.S21PacketChunkData(sent,true,1<<4));
        ru.givler.mbo.network.packet.PacketSilverMirrorScene incoming=new ru.givler.mbo.network.packet.PacketSilverMirrorScene();
        io.netty.buffer.ByteBuf wire=io.netty.buffer.Unpooled.buffer();
        try { outgoing.toBytes(wire); incoming.fromBytes(wire); }
        finally { wire.release(); }
        Method buildScene=SilverMirrorView.class.getDeclaredMethod("buildScene",World.class,ru.givler.mbo.network.packet.PacketSilverMirrorScene.class);
        buildScene.setAccessible(true);
        World detached=(World)buildScene.invoke(null,scene,incoming);
        check(!(detached.getChunkProvider() instanceof net.minecraft.client.multiplayer.ChunkProviderClient),
            "Detached world invokes Angelica hooks for the main client world");
        check(detached.getBlock(162,64,179)==net.minecraft.init.Blocks.stone
                && detached.getBlockMetadata(162,64,179)==1,"Streamed block data did not reach the render world");
        check(mc.theWorld==scene && scene.getBlock(162,64,179)!=net.minecraft.init.Blocks.stone,
            "Streamed data changed the main client world");
        ru.givler.mbo.network.packet.PacketSilverMirrorScene delta=new ru.givler.mbo.network.packet.PacketSilverMirrorScene();
        delta.initial=false;delta.streamId=42;
        int centerIndex=(14*29+14)*22+10;
        delta.addChange(centerIndex,Block.getIdFromBlock(net.minecraft.init.Blocks.dirt));
        net.minecraft.entity.passive.EntityCow remoteCow=new net.minecraft.entity.passive.EntityCow(detached);
        remoteCow.setPosition(162,64,179);
        remoteCow.setCurrentItemOrArmor(0,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_sword));
        remoteCow.setCurrentItemOrArmor(4,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_helmet));
        remoteCow.setSneaking(true);
        java.lang.reflect.Constructor<ru.givler.mbo.network.packet.PacketSilverMirrorScene.Actor> actorConstructor=
            ru.givler.mbo.network.packet.PacketSilverMirrorScene.Actor.class.getDeclaredConstructor(int.class,EntityLivingBase.class);
        actorConstructor.setAccessible(true);
        int cowType=net.minecraft.entity.EntityList.getEntityID(remoteCow);
        delta.actors.add(actorConstructor.newInstance(cowType,remoteCow));
        ru.givler.mbo.network.packet.PacketSilverMirrorScene decodedDelta=new ru.givler.mbo.network.packet.PacketSilverMirrorScene();
        io.netty.buffer.ByteBuf deltaWire=io.netty.buffer.Unpooled.buffer();
        try { delta.toBytes(deltaWire); decodedDelta.fromBytes(deltaWire); }
        finally { deltaWire.release(); }
        check(!decodedDelta.initial && decodedDelta.streamId==42 && decodedDelta.chunks.isEmpty()
                && decodedDelta.changes.size()==2 && decodedDelta.actors.get(0).id==remoteCow.getEntityId(),
            "Incremental mirror packet lost its identity, block update or entity ID");
        ru.givler.mbo.mirror.SilverMirrorBlocks.apply(detached,162,64,179,decodedDelta.changes.get(0),decodedDelta.changes.get(1));
        check(!(detached.getChunkProvider() instanceof net.minecraft.client.multiplayer.ChunkProviderClient),
            "Detached world invokes Angelica hooks for the main client world");
        check(detached.getBlock(162,64,179)==net.minecraft.init.Blocks.dirt && mc.theWorld==scene,
            "Incremental mirror block change did not reach the detached world");
        Method actorsUpdate=detached.getClass().getDeclaredMethod("updateActors",delta.getClass(),long.class);
        actorsUpdate.setAccessible(true);
        Method interpolate=detached.getClass().getDeclaredMethod("interpolate",long.class);
        interpolate.setAccessible(true);
        actorsUpdate.invoke(detached,decodedDelta,100_000_000L);
        EntityLivingBase copy=(EntityLivingBase)detached.loadedEntityList.get(0);
        check(copy.getHeldItem()!=null && copy.getHeldItem().getItem()==net.minecraft.init.Items.iron_sword
                && copy.getEquipmentInSlot(4)!=null && copy.getEquipmentInSlot(4).getItem()==net.minecraft.init.Items.iron_helmet
                && copy.isSneaking(),"Remote equipment or pose disappeared during the network round trip");
        java.util.Map<Integer,ru.givler.mbo.network.packet.PacketSilverMirrorScene.Actor> appearance=new java.util.HashMap<>();
        delta.omitUnchangedAppearance(appearance);
        delta.actors.clear();delta.actors.add(actorConstructor.newInstance(cowType,remoteCow));
        delta.omitUnchangedAppearance(appearance);
        ru.givler.mbo.network.packet.PacketSilverMirrorScene motionOnly=new ru.givler.mbo.network.packet.PacketSilverMirrorScene();
        io.netty.buffer.ByteBuf motionWire=io.netty.buffer.Unpooled.buffer();
        try { delta.toBytes(motionWire); motionOnly.fromBytes(motionWire); }
        finally { motionWire.release(); }
        check(!motionOnly.actors.get(0).sendEquipment,"Unchanged equipment was sent again");
        actorsUpdate.invoke(detached,motionOnly,100_000_000L);
        check(copy.getEquipmentInSlot(4)!=null && copy.getHeldItem()!=null,
            "A movement-only update cleared visible equipment");
        remoteCow.setPosition(163,64,179);
        remoteCow.setCurrentItemOrArmor(4,null);
        delta.actors.clear();delta.actors.add(actorConstructor.newInstance(cowType,remoteCow));
        delta.omitUnchangedAppearance(appearance);
        check(delta.actors.get(0).sendEquipment,"Armor removal was omitted");
        actorsUpdate.invoke(detached,delta,200_000_000L);
        interpolate.invoke(detached,250_000_000L);
        check(detached.loadedEntityList.get(0)==copy && Math.abs(copy.posX-162.5)<.001,
            "Remote actor was recreated or jumped instead of interpolating");
        check(copy.getEquipmentInSlot(4)==null,"Remote armor remained after being unequipped");
        delta.actors.clear();actorsUpdate.invoke(detached,delta,300_000_000L);
        check(detached.loadedEntityList.isEmpty(),"Remote actor survived leaving the streamed scene");
        // Register real cut families for stone and both one-step stone brick variants.
        Field modController=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");
        modController.setAccessible(true);
        cpw.mods.fml.common.Loader testLoader=cpw.mods.fml.common.Loader.instance();
        modController.set(testLoader,new cpw.mods.fml.common.LoadController(testLoader));
        int vanillaStairOpacity=net.minecraft.init.Blocks.stone_stairs.getLightOpacity();
        ru.givler.mbo.block.BlockBase greyStone=new ru.givler.mbo.block.BlockBase(
            Material.rock,"opacity_test_grey_stone","stone/stone",false);
        ru.givler.mbo.block.BlockMeta sandstone=new ru.givler.mbo.block.BlockMeta(
            Material.rock,"opacity_test_sandstone","stone/sandstone",3);
        check(vanillaStairOpacity==255
                && new ru.givler.mbo.block.BlockBasicStairs(greyStone).getLightOpacity()==vanillaStairOpacity
                && new ru.givler.mbo.block.BlockMetaStairs(sandstone,0).getLightOpacity()==vanillaStairOpacity,
            "MBO stone and sandstone stairs must use the same light opacity as vanilla stone stairs");
        check(new ru.givler.mbo.block.BlockMeta(Material.glass,"opacity_test_glass","glass/glass",1)
            .withGlass(false).getLightOpacity()==0,"Clear MBO glass must transmit light");
        Class<?> blocks=ru.givler.mbo.registry.BlockRegistry.class;
        Method vertical=blocks.getDeclaredMethod("registerVerticalFamily",Block.class,int.class);vertical.setAccessible(true);
        vertical.invoke(null,net.minecraft.init.Blocks.stone,1);
        vertical.invoke(null,net.minecraft.init.Blocks.stonebrick,4);
        Method quarterShapes=blocks.getDeclaredMethod("registerQuarterColumns");quarterShapes.setAccessible(true);quarterShapes.invoke(null);
        net.minecraft.item.ItemStack stone=new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.stone);
        for (int meta : new int[]{0,3}) {
            ru.givler.mbo.recipes.stonecutter.StonecutterRecipes.add(stone,new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.stonebrick,1,meta));
        }
        Method cutShapes=ru.givler.mbo.registry.StonecutterRegistry.class.getDeclaredMethod("registerCutShapes");
        cutShapes.setAccessible(true);cutShapes.invoke(null);
        java.util.Set<String> stoneShapes=new java.util.HashSet<>();
        for (ru.givler.mbo.recipes.stonecutter.StonecutterRecipe recipe : ru.givler.mbo.recipes.stonecutter.StonecutterRecipes.getRecipes(stone)) {
            net.minecraft.item.ItemStack outputStack=recipe.getOutput();
            Block result=Block.getBlockFromItem(outputStack.getItem());
            if (!(result instanceof ru.givler.mbo.block.BlockVerticalSlab)) continue;
            ru.givler.mbo.block.BlockVerticalSlab shape=(ru.givler.mbo.block.BlockVerticalSlab)result;
            int expected=result instanceof ru.givler.mbo.block.BlockQuarterColumn
                ? (((ru.givler.mbo.block.BlockQuarterColumn)result).isCube()?8:4) : 2;
            check(outputStack.stackSize==expected,"Wrong quantity of stonecutter shapes");
            check(stoneShapes.add(Block.getIdFromBlock(shape.source)+":"+shape.getSourceMetadata()+":"+expected),
                "Duplicate stonecutter shape");
        }
        check(stoneShapes.size()==9,"Stone should list its shapes and both stone-brick shape families");
        low.delete();
        check(scene.calls>0,"No world blocks rendered");
        GL20.glUseProgram(0);OpenGlHelper.func_153171_g(36160,0);target.delete();output.delete();context.destroy();
        System.out.println("PASS mirror capture/surface, four facings, original frame, shader/FBO state, no rear support, distant low resolution, animals/players, entity visibility and cap, fire, real MBO Geo model, non-ticking decor/mirrors from chunk maps with empty ticking list, offscreen/occlusion culling, back atlas UVs, Forge inventory/held model, both-half collisions, isolated chunk and block delta, remote actor interpolation, equipment and pose");
    }
}
