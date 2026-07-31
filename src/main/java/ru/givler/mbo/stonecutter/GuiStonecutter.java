package ru.givler.mbo.stonecutter;

import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiStonecutter extends GuiContainer {
    private static final ResourceLocation GUI=new ResourceLocation("mbo","textures/gui/stonecutter.png");
    private static final ResourceLocation SCROLLER=
            new ResourceLocation("mbo","textures/gui/loom/scroller.png");
    private static final ResourceLocation SCROLLER_DISABLED=
            new ResourceLocation("mbo","textures/gui/loom/scroller_disabled.png");
    private final ContainerStonecutter container;
    private int first;
    private ItemStack hoveredResult;

    public GuiStonecutter(InventoryPlayer player,World world,int x,int y,int z) {
        super(new ContainerStonecutter(player,world,x,y,z));
        container=(ContainerStonecutter)inventorySlots;
        xSize=176; ySize=166;
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX,int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal("container.mbo.stonecutter"),8,6,0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"),8,73,0x404040);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY) {
        GL11.glColor4f(1,1,1,1);
        mc.getTextureManager().bindTexture(GUI);
        drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);
        List<StonecutterRecipe> recipes=container.getRecipes();
        hoveredResult=null;
        for(int visible=0;visible<9;visible++) {
            int index=first+visible;
            if(index>=recipes.size()) break;
            int px=guiLeft+51+(visible%3)*22;
            int py=guiTop+15+(visible/3)*18;
            if(index==container.getSelected()) drawRect(px,py,px+18,py+18,0x80FFFFFF);
            ItemStack stack=recipes.get(index).getOutput();
            itemRender.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),stack,px+1,py+1);
            if(mouseX>=px&&mouseX<px+18&&mouseY>=py&&mouseY<py+18) hoveredResult=stack;
        }
        int max=Math.max(0,recipes.size()-9);
        mc.getTextureManager().bindTexture(max>0?SCROLLER:SCROLLER_DISABLED);
        int scrollY=max>0?first*39/max:0;
        drawTextureRegion(guiLeft+118,guiTop+15+scrollY,12,15,12,15);
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(hoveredResult!=null) renderToolTip(hoveredResult,mouseX,mouseY);
    }
    @Override protected void mouseClicked(int mouseX,int mouseY,int button) {
        super.mouseClicked(mouseX,mouseY,button);
        if(button!=0)return;
        int rx=mouseX-(guiLeft+51), ry=mouseY-(guiTop+15);
        if(rx<0||ry<0||rx>=62||ry>=54||rx%22>=18)return;
        int index=first+(ry/18)*3+(rx/22);
        if(index<container.getRecipes().size()) {
            container.enchantItem(mc.thePlayer,index);
            mc.playerController.sendEnchantPacket(inventorySlots.windowId,index);
        }
    }
    @Override public void handleMouseInput() {
        super.handleMouseInput();
        int wheel=Mouse.getEventDWheel();
        if(wheel!=0) {
            int max=Math.max(0,container.getRecipes().size()-9);
            first=Math.max(0,Math.min(max,first+(wheel<0?3:-3)));
        }
    }
    private void drawTextureRegion(int x,int y,int width,int height,int textureWidth,int textureHeight) {
        Tessellator t=Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x,y+height,zLevel,0,1);
        t.addVertexWithUV(x+width,y+height,zLevel,1,1);
        t.addVertexWithUV(x+width,y,zLevel,1,0);
        t.addVertexWithUV(x,y,zLevel,0,0);
        t.draw();
    }
}
