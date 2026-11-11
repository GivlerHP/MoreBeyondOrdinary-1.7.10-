package ru.givler.mbo.client.render.light;

import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.*;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.network.packet.PacketLanternBeltVisual;
import io.netty.buffer.*;
import sun.misc.Unsafe;

/** Real offscreen first/third-person torch draws, including the reflected MF2 offhand path. */
public final class PortableLightSmoke {
  static void check(boolean value,String message) {if(!value)throw new AssertionError(message);}
  public static void main(String[] args)throws Exception {
    for(int state=0;state<3;state++) {
      ByteBuf a=Unpooled.buffer(),b=Unpooled.buffer();
      new PacketLanternBeltVisual(1234,state).toBytes(a);
      PacketLanternBeltVisual decoded=new PacketLanternBeltVisual();decoded.fromBytes(a);decoded.toBytes(b);
      check(b.readInt()==1234 && b.readUnsignedByte()==state,"Belt state roundtrip");a.release();b.release();
    }
    Field bootField=Unsafe.class.getDeclaredField("theUnsafe");bootField.setAccessible(true);Unsafe bootUnsafe=(Unsafe)bootField.get(null);
    Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);loader.set(null,bootUnsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
    Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);mods.set(loader.get(null),new java.util.HashMap<String,cpw.mods.fml.common.ModContainer>());
    net.minecraft.init.Bootstrap.func_151354_b();
    Pbuffer context=new Pbuffer(256,256,new PixelFormat(8,24,0),null,null);context.makeCurrent();
    try {
      verifySwing();
      Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe unsafe=(Unsafe)f.get(null);
      net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit=GL13.GL_TEXTURE0;
      net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit=GL13.GL_TEXTURE1;
      Minecraft mc=(Minecraft)unsafe.allocateInstance(Minecraft.class);
      f=Minecraft.class.getDeclaredField("theMinecraft");f.setAccessible(true);f.set(null,mc);
      final int texture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
      BufferedImage atlas=new BufferedImage(64,16,BufferedImage.TYPE_INT_ARGB);
      String[] names={"torch","torch_base","lantern","unlit_lantern"};
      f=PortableLights.class.getDeclaredField("ICONS");f.setAccessible(true);
      Map<String,IIcon> icons=(Map<String,IIcon>)f.get(null);
      for(int i=0;i<names.length;i++) {
        BufferedImage image=ImageIO.read(PortableLightSmoke.class.getResourceAsStream("/assets/mbo/textures/blocks/portablelights/"+names[i]+".png"));
        for(int y=0;y<16;y++)for(int x=0;x<16;x++)atlas.setRGB(i*16+x,y,image.getRGB(x,y));
        TextureAtlasSprite sprite=new TextureAtlasSprite(names[i]){};
        sprite.setIconWidth(16);sprite.setIconHeight(16);sprite.initSprite(64,16,i*16,0,false);
        icons.put("mbo:portablelights/"+names[i],sprite);
      }
      icons.put("minecraft:fire_layer_0",icons.get("mbo:portablelights/torch"));
      ByteBuffer pixels=BufferUtils.createByteBuffer(64*16*4);
      for(int y=0;y<16;y++)for(int x=0;x<64;x++){int c=atlas.getRGB(x,y);pixels.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}pixels.flip();
      GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,64,16,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
      mc.renderEngine=new TextureManager(null){@Override public void bindTexture(ResourceLocation resource){
        if(resource.getResourceDomain().equals("customnpcs")) bindNpcTexture();
        else GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
      }};
      PortableLightRenderer renderer=new PortableLightRenderer(true);
      check(!renderer.handleRenderType(null,ItemRenderType.INVENTORY),"Inventory keeps original icons");
      BufferedImage sheet=new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB);
      for(int view=0;view<4;view++) {
        GL11.glViewport(0,0,256,256);GL11.glClearColor(.1F,.13F,.16F,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-1.4,1.4,-.4,1.7,-10,10);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();if(view%2==1)GL11.glScalef(-1,1,1);
        GL11.glRotatef(20,0,1,0);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1F);
        int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),attrs=GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
        net.minecraftforge.client.ForgeHooksClient.renderEquippedItem(view<2?ItemRenderType.EQUIPPED_FIRST_PERSON:ItemRenderType.EQUIPPED,renderer,null,null,null);
        check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth && GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH)==attrs,"GL stacks restored");
        check(GL11.glGetError()==GL11.GL_NO_ERROR,"GL render error");
        ByteBuffer out=BufferUtils.createByteBuffer(256*256*4);GL11.glReadPixels(0,0,256,256,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,out);
        int drawn=0;
        for(int y=0;y<256;y++)for(int x=0;x<256;x++) {int at=(y*256+x)*4;int color=(out.get(at)&255)<<16|(out.get(at+1)&255)<<8|out.get(at+2)&255;sheet.setRGB(view%2*256+x,view/2*256+255-y,color);if((color>>16&255)>60)drawn++;}
        check(drawn>50,"Torch is visible in view "+view);
      }
      Path output=Paths.get("build/portable-lights/torch-hands.png");Files.createDirectories(output.getParent());ImageIO.write(sheet,"png",output.toFile());
      verifyAttachedParticles(mc,unsafe);
      verifyCarryReference();
      verifyNpcLantern();
      GL11.glDeleteTextures(texture);
      System.out.println("PORTABLE_LIGHTS_OK: four real hand draws, GL state, icons and belt packet");
    } finally {context.destroy();}
  }
  static void verifySwing() {
    GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
    try {
      for(int side:new int[]{-1,1})for(float progress:new float[]{0,.1F,.25F,.5F,.75F,.9F,.98F,1}) {
        GL11.glLoadIdentity();PortableCarry.swingTranslation(side,progress);PortableCarry.vanillaArm(side,progress);
        FloatBuffer actual=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,actual);
        GL11.glLoadIdentity();
        // Reference: vanilla translation -> base placement/yaw -> swing rotations -> arm mesh placement.
        float root=net.minecraft.util.MathHelper.sqrt_float(progress),arc=net.minecraft.util.MathHelper.sin(root*(float)Math.PI);
        if(progress>0 && progress<1)GL11.glTranslatef(-side*arc*.4F,net.minecraft.util.MathHelper.sin(root*(float)Math.PI*2)*.2F,-net.minecraft.util.MathHelper.sin(progress*(float)Math.PI)*.2F);
        GL11.glTranslatef(side*.64000005F,-.6F,-.71999997F);GL11.glRotatef(side*45,0,1,0);
        if(progress>0 && progress<1) {
          GL11.glRotatef(-side*net.minecraft.util.MathHelper.sin(progress*progress*(float)Math.PI)*20,0,1,0);
          GL11.glRotatef(-side*arc*20,0,0,1);GL11.glRotatef(-arc*80,1,0,0);
        }
        GL11.glTranslatef(-side,3.6F,3.5F);GL11.glRotatef(side*120,0,0,1);GL11.glRotatef(200,1,0,0);GL11.glRotatef(side*-135,0,1,0);GL11.glTranslatef(side*5.6F,0,0);
        FloatBuffer expected=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,expected);
        for(int i=0;i<16;i++)check(Math.abs(actual.get(i)-expected.get(i))<1e-5,"Vanilla swing matrix at "+progress+" side "+side);
      }
    } finally {GL11.glPopMatrix();}
  }
  public static class ParticleWorld extends net.minecraft.client.multiplayer.WorldClient {
    long time;
    public ParticleWorld(){super(null,null,0,null,null);}
    @Override public long getTotalWorldTime(){return time;}
    @Override public int getLightBrightnessForSkyBlocks(int x,int y,int z,int minimum){return 0xF000F0;}
  }
  public static class ParticlePlayer extends net.minecraft.entity.player.EntityPlayerMP {
    public ParticlePlayer(){super(null,null,null,null);}
    @Override public int getBrightnessForRender(float partial){return 0xF000F0;}
  }
  private static void verifyAttachedParticles(Minecraft mc,Unsafe unsafe)throws Exception {
    ParticleWorld world=(ParticleWorld)unsafe.allocateInstance(ParticleWorld.class);
    Field provider=net.minecraft.world.World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(world,new net.minecraft.world.WorldProviderSurface());
    mc.theWorld=world;
    ParticlePlayer player=(ParticlePlayer)unsafe.allocateInstance(ParticlePlayer.class);player.worldObj=world;
    net.minecraft.item.ItemStack torch=new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.torch);
    net.minecraft.item.ItemStack red=new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.redstone_torch);
    check(PortableLights.isRedTorch(red) && PortableLights.isLit(red),"Redstone torch identification");
    GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
    PortableCarryParticles.tick();PortableCarryParticles.draw(torch,player,1,.5F);
    Field hands=PortableCarryParticles.class.getDeclaredField("HANDS");hands.setAccessible(true);
    java.util.Map<Long,?> states=(java.util.Map<Long,?>)hands.get(null);
    Object hand=states.get(0L);Field particles=hand.getClass().getDeclaredField("particles");particles.setAccessible(true);
    java.util.List<?> nativeParticles=(java.util.List<?>)particles.get(hand);
    check(nativeParticles.size()==2 && nativeParticles.get(0) instanceof net.minecraft.client.particle.EntityFlameFX
        && nativeParticles.get(1) instanceof net.minecraft.client.particle.EntitySmokeFX,"Native flame and smoke providers");
    for(int i=0;i<20;i++)PortableCarryParticles.draw(torch,player,1,.5F);
    check(nativeParticles.size()==2,"Particles must not spawn per frame");
    for(Object value:nativeParticles) {
      net.minecraft.client.particle.EntityFX particle=(net.minecraft.client.particle.EntityFX)value;
      check(particle.prevPosX==particle.posX && particle.prevPosY==particle.posY
          && particle.prevPosZ==particle.posZ,"New particles must interpolate from the torch tip");
    }
    Field age=net.minecraft.client.particle.EntityFX.class.getDeclaredField("particleAge");age.setAccessible(true);
    Object flame=nativeParticles.get(0);world.time=1;PortableCarryParticles.tick();int first=age.getInt(flame);PortableCarryParticles.tick();
    check(age.getInt(flame)==first && first==1,"Particle aging must run once per world tick");
    PortableCarryParticles.draw(red,player,-1,.5F);
    Object redHand=states.get(1L);java.util.List<?> redParticles=(java.util.List<?>)particles.get(redHand);
    check(redParticles.size()==1 && redParticles.get(0) instanceof net.minecraft.client.particle.EntityReddustFX,"Native redstone particles");
    check(GL11.glGetError()==GL11.GL_NO_ERROR,"Attached particle GL state");
    mc.theWorld=null;PortableCarryParticles.tick();check(states.isEmpty(),"Particle cache cleared on world unload");
  }
  private static void verifyCarryReference() throws Exception {
    com.google.gson.JsonObject expected;
    try(java.io.InputStreamReader reader=new java.io.InputStreamReader(PortableLightSmoke.class.getResourceAsStream("/portablelights/carry_reference_poses.json"),"UTF-8")) {
      expected=new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
    }
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    for(boolean lantern:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
      GL11.glLoadIdentity();PortableCarry.beforeArm(lantern,side,0,0);PortableCarry.vanillaArm(side);
      String key=(lantern?"lantern":"torch")+(side<0?"_left":"_right");
      compareMatrix(expected.getAsJsonObject(key).getAsJsonArray("arm"),key+" arm");
      PortableCarry.block(lantern,side,0);
      compareMatrix(expected.getAsJsonObject(key).getAsJsonArray("block"),key+" block");
    }
  }
  private static void compareMatrix(com.google.gson.JsonArray expected,String name) {
    java.nio.FloatBuffer actual=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,actual);
    for(int i=0;i<16;i++)check(Math.abs(actual.get(i)-expected.get(i).getAsFloat())<.00002F,"LastDawn reference mismatch: "+name+"["+i+"]");
  }
  private static int npcTexture;
  private static void bindNpcTexture() {
    if(npcTexture==0) try {
      BufferedImage image=ImageIO.read(PortableLightSmoke.class.getResourceAsStream("/assets/customnpcs/textures/models/lantern.png"));
      ByteBuffer data=BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*4);
      for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getRGB(x,y);data.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}data.flip();
      npcTexture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,npcTexture);
      GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,image.getWidth(),image.getHeight(),0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,data);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
    } catch(Exception e){throw new IllegalStateException(e);}
    GL11.glBindTexture(GL11.GL_TEXTURE_2D,npcTexture);
  }
  private static void verifyNpcLantern() throws Exception {
    // The dependency is a production SRG jar. Remap its two client classes only for this standalone MCP test.
    final java.util.Map<String,String> names=new java.util.HashMap<>();
    Path mappings=Paths.get(System.getProperty("portableLightMappings"));
    for(String file:new String[]{"fields.csv","methods.csv"})for(String line:Files.readAllLines(mappings.resolve(file))) {
      String[] values=line.split(",");if(values.length>1)names.put(values[0],values[1]);
    }
    ClassLoader loader=new ClassLoader(PortableLightSmoke.class.getClassLoader()) {
      @Override protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
        if(!name.equals("noppes.npcs.client.model.blocks.lantern.LanternFloor") && !name.equals("ru.givler.mbo.client.render.light.CustomNpcLanternVisual"))return super.loadClass(name,resolve);
        Class<?> cached=findLoadedClass(name);if(cached!=null)return cached;
        try(java.io.InputStream in=getParent().getResourceAsStream(name.replace('.','/')+".class")) {
          org.objectweb.asm.ClassReader reader=new org.objectweb.asm.ClassReader(in);
          org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(0);
          reader.accept(new org.objectweb.asm.commons.RemappingClassAdapter(writer,new org.objectweb.asm.commons.Remapper(){
            @Override public String mapFieldName(String owner,String name,String desc){return names.containsKey(name)?names.get(name):name;}
            @Override public String mapMethodName(String owner,String name,String desc){return names.containsKey(name)?names.get(name):name;}
          }),org.objectweb.asm.ClassReader.EXPAND_FRAMES);
          byte[] bytes=writer.toByteArray();Class<?> type=defineClass(name,bytes,0,bytes.length);if(resolve)resolveClass(type);return type;
        } catch(Exception e){throw new ClassNotFoundException(name,e);}
      }
    };
    Method render=loader.loadClass("ru.givler.mbo.client.render.light.CustomNpcLanternVisual").getDeclaredMethod("render",boolean.class);render.setAccessible(true);
    BufferedImage sheet=new BufferedImage(512,256,BufferedImage.TYPE_INT_RGB);
    byte[] unlitPixels=null;
    for(int state=0;state<2;state++) {
      GL11.glViewport(0,0,256,256);GL11.glClearColor(.1F,.13F,.16F,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
      GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-.6,.6,-.1,1.1,-10,10);
      GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glRotatef(25,0,1,0);
      GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_LIGHTING);
      // With environmental light held constant, switching off must not tint the texture grey.
      GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glDisable(GL11.GL_TEXTURE_2D);GL13.glActiveTexture(GL13.GL_TEXTURE0);
      GL11.glColor4f(1,1,1,1);
      render.invoke(null,state==1);
      Method grip=render.getDeclaringClass().getDeclaredMethod("gripHeight");grip.setAccessible(true);
      float height=((Number)grip.invoke(null)).floatValue();check(height>.6F && height<1.2F,"Lantern handle height");
      Method hook=render.getDeclaringClass().getDeclaredMethod("gripX");hook.setAccessible(true);
      float hookX=((Number)hook.invoke(null)).floatValue();
      GL11.glPushMatrix();GL11.glLoadIdentity();PortableLights.beltTransform(height,hookX,.4F,2);
      java.nio.FloatBuffer attachment=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,attachment);
      check(Math.abs(attachment.get(0)*hookX+attachment.get(4)*height+attachment.get(12)-.12F)<.0001,"Belt hook X");
      check(Math.abs(attachment.get(1)*hookX+attachment.get(5)*height+attachment.get(13)-.70F)<.0001,"Belt hook Y");
      check(Math.abs(attachment.get(2)*hookX+attachment.get(6)*height+attachment.get(14)-.16F)<.0001,"Belt hook Z");GL11.glPopMatrix();
      check(GL11.glGetError()==GL11.GL_NO_ERROR,"CustomNPC model GL error");
      ByteBuffer out=BufferUtils.createByteBuffer(256*256*4);GL11.glReadPixels(0,0,256,256,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,out);
      byte[] pixels=new byte[out.capacity()];out.get(pixels);
      if(state==0)unlitPixels=pixels;
      else check(java.util.Arrays.equals(unlitPixels,pixels),"Unlit lantern must retain texture colors without an artificial dark tint");
      for(int y=0;y<256;y++)for(int x=0;x<256;x++){int at=(y*256+x)*4;sheet.setRGB(state*256+x,255-y,(out.get(at)&255)<<16|(out.get(at+1)&255)<<8|out.get(at+2)&255);}
    }
    ImageIO.write(sheet,"png",Paths.get("build/portable-lights/lantern-states.png").toFile());
    GL11.glDeleteTextures(npcTexture);
  }
}
