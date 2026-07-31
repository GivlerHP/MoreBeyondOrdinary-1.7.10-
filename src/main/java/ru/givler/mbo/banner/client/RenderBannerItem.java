package ru.givler.mbo.banner.client;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.banner.TileEntityBanner;

public class RenderBannerItem implements IItemRenderer {
    private final TileEntityBanner banner=new TileEntityBanner();
    @Override public boolean handleRenderType(ItemStack stack,ItemRenderType type) { return type!=ItemRenderType.FIRST_PERSON_MAP; }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,ItemRendererHelper helper) { return true; }
    @Override public void renderItem(ItemRenderType type,ItemStack stack,Object... data) {
        banner.setItemValues(stack); banner.standing=true;
        GL11.glPushMatrix();
        if(type==ItemRenderType.INVENTORY) { GL11.glTranslatef(0,-.25F,.6F); GL11.glRotatef(22.5F,0,1,0); GL11.glScalef(.9F,.9F,.9F); }
        else if(type==ItemRenderType.ENTITY) { GL11.glTranslatef(-.5F,-.75F,-.5F); GL11.glRotatef(90,0,1,0); }
        else if(type==ItemRenderType.EQUIPPED) { GL11.glRotatef(130,0,1,0); GL11.glRotatef(270,0,0,1); GL11.glTranslatef(-1,-1.1F,-.25F); }
        else { GL11.glRotatef(225,0,1,0); GL11.glTranslatef(-.25F,.0625F,-1); }
        TileEntityRendererDispatcher.instance.renderTileEntityAt(banner,0,0,0,0);
        GL11.glPopMatrix();
    }
}
