package ru.givler.mbo.banner;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.Collections;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiLoom extends GuiContainer {
    private static final ResourceLocation GUI = new ResourceLocation("mbo", "textures/gui/loom.png");
    private final ContainerLoom loom;
    private int firstPattern;
    private int hoveredPattern = -1;

    public GuiLoom(InventoryPlayer inventory, World world, int x, int y, int z) {
        super(new ContainerLoom(inventory, world, x, y, z));
        loom = (ContainerLoom) inventorySlots;
        xSize = 176;
        ySize = 166;
    }

    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal("container.mbo.loom"), 8, 6, 0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 73, 0x404040);
    }

    @Override protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        GL11.glColor4f(1,1,1,1);
        mc.getTextureManager().bindTexture(GUI);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        ItemStack preview = loom.getPreview();
        if (preview != null) drawBanner(preview, guiLeft + 141, guiTop + 9, 20, 40);
        if (loom.getBanner() == null || loom.getDye() == null) return;
        BannerPattern[] values = BannerPattern.values();
        hoveredPattern = -1;
        for (int visible=0; visible<16; visible++) {
            int pattern = firstPattern + visible + 1;
            if (pattern >= values.length) break;
            int x = guiLeft + 60 + (visible % 4) * 14;
            int y = guiTop + 17 + (visible / 4) * 14;
            boolean selected = loom.getSelectedPattern() == pattern;
            boolean hovered = mouseX >= x && mouseX < x+14 && mouseY >= y && mouseY < y+14;
            if (hovered) hoveredPattern = pattern;
            ResourceLocation button = new ResourceLocation("mbo", "textures/gui/loom/"
                    + (selected ? "pattern_selected.png" : hovered ? "pattern_highlighted.png" : "pattern.png"));
            mc.getTextureManager().bindTexture(button);
            drawTextureRegion(x, y, 14, 14, 0, 0, 14, 14, 14, 14);
            mc.getTextureManager().bindTexture(new ResourceLocation("mbo", "textures/entity/banner/" + values[pattern].texture + ".png"));
            setDyeColor(loom.getDye().getItemDamage(), true);
            drawTextureRegion(x+4, y+1, 6, 12, 1, 1, 20, 40, 64, 64);
            GL11.glColor4f(1,1,1,1);
        }
        ResourceLocation scroll = new ResourceLocation("mbo", "textures/gui/loom/"
                + (BannerPattern.values().length > 17 ? "scroller.png" : "scroller_disabled.png"));
        mc.getTextureManager().bindTexture(scroll);
        int max = Math.max(1, BannerPattern.values().length - 17);
        drawTextureRegion(guiLeft+119, guiTop+16+(firstPattern*41/max), 12, 15, 0, 0, 12, 15, 12, 15);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (hoveredPattern > 0)
            drawHoveringText(Collections.singletonList(StatCollector.translateToLocal(
                    "item.banner.pattern." + BannerPattern.values()[hoveredPattern].texture)), mouseX, mouseY, fontRendererObj);
    }

    private void drawBanner(ItemStack stack, int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(new ResourceLocation("mbo", "textures/entity/banner/base.png"));
        setDyeColor(BannerData.getBaseColor(stack), false);
        drawTextureRegion(x,y,width,height,1,1,20,40,64,64);
        NBTTagList patterns=BannerData.getPatterns(stack,false);
        if(patterns!=null) for(int i=0;i<patterns.tagCount();i++) {
            NBTTagCompound entry=patterns.getCompoundTagAt(i);
            BannerPattern pattern=BannerPattern.byId(entry.getString("Pattern"));
            if(pattern==null) continue;
            mc.getTextureManager().bindTexture(new ResourceLocation("mbo","textures/entity/banner/"+pattern.texture+".png"));
            setDyeColor(entry.getInteger("Color"),true);
            drawTextureRegion(x,y,width,height,1,1,20,40,64,64);
        }
        GL11.glColor4f(1,1,1,1);
    }

    private void setDyeColor(int damage, boolean dyeDamage) {
        float[] color=EntitySheep.fleeceColorTable[dyeDamage ? 15-(damage&15) : damage&15];
        GL11.glColor4f(color[0],color[1],color[2],1);
    }

    private void drawTextureRegion(int x,int y,int width,int height,int u,int v,int uw,int vh,int textureW,int textureH) {
        Tessellator t=Tessellator.instance;
        double u0=(double)u/textureW, u1=(double)(u+uw)/textureW;
        double v0=(double)v/textureH, v1=(double)(v+vh)/textureH;
        t.startDrawingQuads();
        t.addVertexWithUV(x,y+height,zLevel,u0,v1);
        t.addVertexWithUV(x+width,y+height,zLevel,u1,v1);
        t.addVertexWithUV(x+width,y,zLevel,u1,v0);
        t.addVertexWithUV(x,y,zLevel,u0,v0);
        t.draw();
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0 || loom.getBanner() == null || loom.getDye() == null) return;
        int relX = mouseX - (guiLeft + 60);
        int relY = mouseY - (guiTop + 17);
        if (relX < 0 || relY < 0 || relX >= 56 || relY >= 56) return;
        int col = relX / 14, row = relY / 14;
        int pattern = firstPattern + row*4 + col + 1;
        if (pattern < BannerPattern.values().length) {
            loom.enchantItem(mc.thePlayer, pattern);
            mc.playerController.sendEnchantPacket(inventorySlots.windowId, pattern);
        }
    }

    @Override public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int max = Math.max(0, BannerPattern.values().length - 1 - 16);
            firstPattern = Math.max(0, Math.min(max, firstPattern + (wheel < 0 ? 4 : -4)));
        }
    }
}
