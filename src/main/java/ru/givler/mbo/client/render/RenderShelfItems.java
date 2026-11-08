package ru.givler.mbo.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.givler.mbo.client.render.banner.RenderBanner;
import ru.givler.mbo.item.ItemBlockBanner;
import ru.givler.mbo.tileentity.TileEntityBanner;
import ru.givler.mbo.tileentity.TileEntityShelf;

/** Original ShelfRenderer layout; no dropped-item bobbing, camera-facing rotation or ground scale. */
public final class RenderShelfItems extends TileEntitySpecialRenderer {
    private final RenderBlocks blocks = new RenderBlocks();
    private final TileEntityBanner banner = new TileEntityBanner();
    private EntityItem customItem;

    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partial) {
        TileEntityShelf shelf = (TileEntityShelf) tile;
        int facing = tile.getBlockMetadata() & 7;
        float angle = facing == 5 ? 90 : facing == 3 ? 180 : facing == 4 ? 270 : 0;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_LIGHTING_BIT
                | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslated(x + .5, y + .5, z + .5);
            GL11.glRotatef(180 - angle, 0, 1, 0);
            for (int i = 0; i < 3; i++) {
                ItemStack stack = shelf.get(i);
                if (stack == null) continue;
                GL11.glPushMatrix();
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT);
                try {
                    // Mojang ShelfRenderer.submitItem: 5/16 spacing, z=-1/4, scale=1/4.
                    GL11.glTranslated((i - 1) * .3125, 0, -.25);
                    GL11.glScalef(.25F, .25F, .25F);
                    GL11.glColor4f(1, 1, 1, 1);
                    renderStack(stack, tile);
                } finally {
                    GL11.glPopAttrib();
                    GL11.glPopMatrix();
                }
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void renderStack(ItemStack stack, TileEntity tile) {
        if (stack.getItem() instanceof ItemBlockBanner) {
            banner.setItemValues(stack);
            TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(banner);
            if (renderer instanceof RenderBanner) ((RenderBanner) renderer).renderOnShelf(banner);
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(mc.getTextureManager().getResourceLocation(stack.getItemSpriteNumber()));
        IItemRenderer custom = MinecraftForgeClient.getItemRenderer(stack, IItemRenderer.ItemRenderType.ENTITY);
        if (custom != null && (stack.getItem() instanceof ItemBlock
                || custom.shouldUseRenderHelper(IItemRenderer.ItemRenderType.ENTITY, stack, IItemRenderer.ItemRendererHelper.BLOCK_3D))) {
            // Older mod renderers have no on_shelf context. Invoke their model without Forge's ground/hand helpers.
            if (customItem == null || customItem.worldObj != tile.getWorldObj()) {
                customItem = new EntityItem(tile.getWorldObj());
                customItem.hoverStart = 0;
            }
            customItem.setEntityItemStack(stack);
            custom.renderItem(IItemRenderer.ItemRenderType.ENTITY, stack, blocks, customItem);
            return;
        }
        if (stack.getItem() instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(stack.getItem());
            if (RenderBlocks.renderItemIn3d(block.getRenderType())) {
                block.setBlockBoundsForItemRender();
                // Original on_shelf block transform rotates 180 degrees; align its actual bounds vertically.
                GL11.glTranslatef(0, (float) (.5 - (block.getBlockBoundsMinY() + block.getBlockBoundsMaxY()) / 2), 0);
                GL11.glRotatef(180, 0, 1, 0);
            }
        }
        boolean blockModel = stack.getItem() instanceof ItemBlock
                && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(stack.getItem()).getRenderType());
        if (!blockModel) GL11.glTranslatef(-.5F, -.5F, .03125F);
        int passes = stack.getItem().requiresMultipleRenderPasses()
                ? stack.getItem().getRenderPasses(stack.getItemDamage()) : 1;
        for (int pass = 0; pass < passes; pass++) {
            int color = stack.getItem().getColorFromItemStack(stack, pass);
            GL11.glColor4f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1);
            if (blockModel) {
                RenderManager.instance.itemRenderer.renderItem(mc.thePlayer, stack, pass, IItemRenderer.ItemRenderType.ENTITY);
            } else {
                // ItemRenderer's entity path applies its own orientation. Shelf sprites must stay in the shelf plane.
                IIcon icon = stack.getItem().getIcon(stack, pass);
                if (icon != null) ItemRenderer.renderItemIn2D(Tessellator.instance,
                        icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(),
                        icon.getIconWidth(), icon.getIconHeight(), .0625F);
            }
        }
        if (!blockModel && stack.hasEffect(0)) renderSpriteGlint();
    }

    private static void renderSpriteGlint() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        Minecraft.getMinecraft().getTextureManager().bindTexture(
                new net.minecraft.util.ResourceLocation("textures/misc/enchanted_item_glint.png"));
        GL11.glColor4f(.38F, .19F, .608F, 1);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        try {
            for (int pass = 0; pass < 2; pass++) {
                GL11.glPushMatrix();
                try {
                    GL11.glScalef(.125F, .125F, .125F);
                    long period = pass == 0 ? 3000 : 4873;
                    float offset = Minecraft.getSystemTime() % period / (float) period * 8;
                    GL11.glTranslatef(pass == 0 ? offset : -offset, 0, 0);
                    GL11.glRotatef(pass == 0 ? -50 : 10, 0, 0, 1);
                    ItemRenderer.renderItemIn2D(Tessellator.instance, 0, 0, 1, 1, 256, 256, .0625F);
                } finally { GL11.glPopMatrix(); }
            }
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopAttrib();
        }
    }
}
