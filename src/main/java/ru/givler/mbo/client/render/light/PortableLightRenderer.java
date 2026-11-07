package ru.givler.mbo.client.render.light;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** Equipped renderer shared by vanilla/MBO torches, CustomNPC lanterns and MF2 lanterns. */
public final class PortableLightRenderer implements IItemRenderer {
  private final boolean torch;
  private PortableLightModel torchModel,redTorchModel,redTorchUnlitModel;
  private PortableLightModel fallbackLit, fallbackUnlit;
  public PortableLightRenderer(boolean torch) { this.torch=torch; }
  boolean isTorch() { return torch; }
  void renderModel(ItemStack stack) {
    GL11.glColor4f(1,1,1,1);
    if(torch) renderTorch(stack);
    else renderLantern(PortableLights.isLit(stack));
  }
  @Override public boolean handleRenderType(ItemStack stack,ItemRenderType type) {
    return type==ItemRenderType.EQUIPPED || type==ItemRenderType.EQUIPPED_FIRST_PERSON;
  }
  @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,ItemRendererHelper helper) { return helper==ItemRendererHelper.EQUIPPED_BLOCK; }
  @Override public void renderItem(ItemRenderType type,ItemStack stack,Object... data) {
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_LIGHTING_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_TEXTURE_BIT);
    GL11.glPushMatrix();
    try {
      GL11.glColor4f(1,1,1,1);
      GL11.glEnable(32826);
      // Forge's EQUIPPED_BLOCK helper supplies the cube origin, avoiding flat-item rotations.
      if(type==ItemRenderType.EQUIPPED_FIRST_PERSON) {
        GL11.glTranslatef(.5F,torch?.15F:-.1F,.5F);
        if(torch) GL11.glRotatef(-10,1,0,0);
        GL11.glScalef(torch?1F:.7F,torch?1F:.7F,torch?1F:.7F);
      } else {
        GL11.glTranslatef(.5F,torch?.15F:-.15F,.5F);
        GL11.glScalef(.85F,.85F,.85F);
      }
      if(torch) renderTorch(stack);
      else renderLantern(PortableLights.isLit(stack));
    } finally { GL11.glPopMatrix();GL11.glPopAttrib(); }
  }
  private void renderTorch(ItemStack stack) {
    if(PortableLights.isRedTorch(stack)) {
      if(PortableLights.isLit(stack)){if(redTorchModel==null)redTorchModel=new PortableLightModel("redstone_torch");redTorchModel.render();}
      else {if(redTorchUnlitModel==null)redTorchUnlitModel=new PortableLightModel("redstone_torch_unlit");redTorchUnlitModel.render();}
    } else {if(torchModel==null)torchModel=new PortableLightModel();torchModel.render();}
  }
  float lanternGripHeight() {
    return cpw.mods.fml.common.Loader.isModLoaded("customnpcs")?CustomNpcLanternVisual.gripHeight():.6875F;
  }
  float lanternGripX() { return cpw.mods.fml.common.Loader.isModLoaded("customnpcs")?CustomNpcLanternVisual.gripX():-.0625F; }
  public void renderLantern(boolean lit) {
    if(cpw.mods.fml.common.Loader.isModLoaded("customnpcs")) CustomNpcLanternVisual.render(lit);
    else {
      if(lit) { if(fallbackLit==null)fallbackLit=new PortableLightModel("oil_lantern_lit");fallbackLit.render(); }
      else { if(fallbackUnlit==null)fallbackUnlit=new PortableLightModel("oil_lantern_unlit");fallbackUnlit.render(); }
    }
  }
}
