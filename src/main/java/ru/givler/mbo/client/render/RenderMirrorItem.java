package ru.givler.mbo.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import ru.givler.mbo.registry.DecorBlockRegistry;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** Static front/back model in inventory, hands and as a dropped item; never captures the world. */
public final class RenderMirrorItem implements IItemRenderer {
    @Override public boolean handleRenderType(ItemStack stack,ItemRenderType type) {
        return type!=ItemRenderType.FIRST_PERSON_MAP;
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,ItemRendererHelper helper) {
        return type==ItemRenderType.ENTITY
            || type==ItemRenderType.INVENTORY && helper==ItemRendererHelper.INVENTORY_BLOCK
            || (type==ItemRenderType.EQUIPPED || type==ItemRenderType.EQUIPPED_FIRST_PERSON)
                && (helper==ItemRendererHelper.EQUIPPED_BLOCK || helper==ItemRendererHelper.BLOCK_3D);
    }
    @Override public void renderItem(ItemRenderType type,ItemStack stack,Object... data) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try {
            boolean silver = stack.getItem()==Item.getItemFromBlock(DecorBlockRegistry.ModelSilverMirror);
            Minecraft.getMinecraft().getTextureManager().bindTexture(RenderWallMirror.texture(silver));
            if(type==ItemRenderType.INVENTORY) {
                // Same isometric INVENTORY_BLOCK transform used by GeckoLib's RenderBlockItem.
                GL11.glScalef(.65F,.65F,.65F);
            } else if(type==ItemRenderType.ENTITY) {
                GL11.glTranslatef(0,.5F,0);GL11.glScalef(.45F,.45F,.45F);
            } else {
                GL11.glTranslatef(.5F,.65F,.3F);
                GL11.glRotatef(type==ItemRenderType.EQUIPPED_FIRST_PERSON?-25:25,0,1,0);
                GL11.glScalef(.45F,.45F,.45F);
            }
            GL11.glTranslatef(-.5F,-1,-1F/32);
            RenderWallMirror.renderPanel(0,0,0,0,true,silver);
        } finally { GL11.glPopMatrix();GL11.glPopAttrib(); }
    }
}
