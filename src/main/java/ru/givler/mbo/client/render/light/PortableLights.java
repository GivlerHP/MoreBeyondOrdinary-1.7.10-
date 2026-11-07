package ru.givler.mbo.client.render.light;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

/** Client registration and the equipped Baubles belt visual. No extra item or equipment slot is created. */
public final class PortableLights {
  private static final PortableLightRenderer LANTERN=new PortableLightRenderer(false);
  private static boolean registered;
  private static final java.util.Map<net.minecraft.world.World,java.util.Map<Integer,Integer>> BELTS=new java.util.WeakHashMap<>();
  public static void beltState(net.minecraft.world.World world,int entityId,int state) {
    java.util.Map<Integer,Integer> states=BELTS.get(world);
    if(states==null){states=new java.util.HashMap<>();BELTS.put(world,states);}
    if(state==0)states.remove(entityId);else states.put(entityId,state);
  }
  private static final java.util.Map<String,net.minecraft.util.IIcon> ICONS=new java.util.HashMap<>();
  static net.minecraft.util.IIcon icon(String texture) { return ICONS.get(texture); }
  @SubscribeEvent public void textures(net.minecraftforge.client.event.TextureStitchEvent.Pre event) {
    if(event.map.getTextureType()!=0)return;
    ICONS.clear();
    for(String texture:new String[]{"redstone_torch_on","redstone_torch_off"})
      ICONS.put("minecraft:"+texture,event.map.registerIcon("minecraft:"+texture));
    for(String name:new String[]{"torch","torch_base","lantern","unlit_lantern"}) {
      String id="mbo:portablelights/"+name;ICONS.put(id,event.map.registerIcon(id));
    }
  }
  public static boolean isMFLantern(ItemStack stack) {
    if(stack==null)return false;
    String id=String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
    return id.startsWith("minefantasy2:lantern_");
  }
  static boolean isRedTorch(ItemStack stack) {
    if(stack==null)return false;
    Block block=Block.getBlockFromItem(stack.getItem());return block==Blocks.redstone_torch || block==Blocks.unlit_redstone_torch;
  }
  static boolean isLit(ItemStack stack) {
    if(stack==null)return false;
    if(isRedTorch(stack))return Block.getBlockFromItem(stack.getItem())==Blocks.redstone_torch;
    if(isMFLantern(stack)) return stack.hasTagCompound() && stack.getTagCompound().getBoolean("lit") && stack.getTagCompound().getInteger("fuel")>0;
    String id=String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
    return !id.toLowerCase(java.util.Locale.ROOT).contains("unlit");
  }
  public static void register() {
    if(registered)return;registered=true;
    PortableLightRenderer torch=new PortableLightRenderer(true);
    MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Blocks.torch),torch);
    MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Blocks.redstone_torch),torch);
    Item unlitRed=Item.getItemFromBlock(Blocks.unlit_redstone_torch);
    if(unlitRed!=null)MinecraftForgeClient.registerItemRenderer(unlitRed,torch);
    MinecraftForgeClient.registerItemRenderer(ru.givler.mbo.registry.ItemRegistry.TorchWeapon,torch);
    for(Object entry:Item.itemRegistry) {
      Item item=(Item)entry;
      if(isMFLantern(new ItemStack(item)))MinecraftForgeClient.registerItemRenderer(item,LANTERN);
      else {
        Block block=Block.getBlockFromItem(item);
        if(block!=null && block.getClass().getName().equals("noppes.npcs.blocks.BlockLantern"))MinecraftForgeClient.registerItemRenderer(item,LANTERN);
      }
    }
    MinecraftForge.EVENT_BUS.register(new PortableLights());
    PortableCarry carry=new PortableCarry();
    MinecraftForge.EVENT_BUS.register(carry);
    cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(carry);
  }
  static void beltTransform(float grip,float hookX,float bodyPitch,float sway) {
    // Seat the hook slightly inside the belt, toward the back of the hip.
    GL11.glTranslatef(.12F,.70F,.16F);
    GL11.glRotatef(-bodyPitch*180F/(float)Math.PI,1,0,0);
    float motion=net.minecraft.util.MathHelper.clamp_float(sway,-10,10);
    GL11.glRotatef(20+motion*.15F,0,1,0);
    GL11.glRotatef(-(14+net.minecraft.util.MathHelper.clamp_float(motion*.3F,-3,3)),0,0,1);
    GL11.glRotatef(motion*.2F,1,0,0);
    GL11.glScalef(.55F,-.55F,.55F);GL11.glTranslatef(-hookX,-grip,0);
  }
  @SubscribeEvent public void belt(RenderPlayerEvent.Specials.Post event) {
    if(event.entityPlayer.isInvisible())return;
    int state;
    if(event.entityPlayer==net.minecraft.client.Minecraft.getMinecraft().thePlayer) state=ru.givler.mbo.handler.LanternBeltVisualSync.state(event.entityPlayer);
    else {
      java.util.Map<Integer,Integer> states=BELTS.get(event.entityPlayer.worldObj);
      state=states==null?0:states.getOrDefault(event.entityPlayer.getEntityId(),0);
    }
    if(state==0)return;
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_LIGHTING_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_TEXTURE_BIT);
    GL11.glPushMatrix();
    try {
      event.renderer.modelBipedMain.bipedBody.postRender(.0625F);
      beltTransform(LANTERN.lanternGripHeight(),LANTERN.lanternGripX(),event.renderer.modelBipedMain.bipedBody.rotateAngleX,
          PortableCarry.sway(event.entityPlayer,event.partialRenderTick));
      GL11.glColor4f(1,1,1,1);
      LANTERN.renderLantern(state==2);
    } finally { GL11.glPopMatrix();GL11.glPopAttrib(); }
  }
}
