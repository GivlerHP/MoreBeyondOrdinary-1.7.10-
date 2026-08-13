package ru.givler.mbo.client.render.decormodels;

import net.geckominecraft.client.renderer.GlStateManager;
import net.minecraft.block.BlockDirectional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import software.bernie.geckolib3.geo.render.built.GeoCube;

import net.minecraft.client.renderer.Tessellator;

public class GeoAnimatedBlockRenderer<T extends TileEntity & IAnimatable>
        extends TileEntitySpecialRenderer implements IGeoRenderer<T> {

    private final GeoModelProvider<T> modelProvider;
    private final GeoModelProvider<T> itemModelProvider;

    public GeoAnimatedBlockRenderer(GeoModelProvider<T> modelProvider) {
		this(modelProvider, modelProvider);
	}

    public GeoAnimatedBlockRenderer(GeoModelProvider<T> modelProvider,
                                    GeoModelProvider<T> itemModelProvider) {
        this.modelProvider = modelProvider;
        this.itemModelProvider = itemModelProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        this.render((T) te, x, y, z, partialTicks);
    }

    public void render(T tile, double x, double y, double z, float partialTicks) {
        boolean itemRender = tile.getWorldObj() == null;
        GeoModelProvider<T> activeProvider = itemRender ? itemModelProvider : modelProvider;
        ResourceLocation modelLocation = activeProvider.getModelLocation(tile);
        if (modelLocation == null) return;
        GeoModel model = activeProvider.getModel(modelLocation);

        if (model == null) return;

        // Preserve RenderBlockItem's own lightmap exactly as the old static
        // renderer did. Only world TESRs calculate light from block position.
        if (!itemRender) {
            int light = tile.getWorldObj().getLightBrightnessForSkyBlocks(
                    tile.xCoord, tile.yCoord, tile.zCoord, 0);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    (float) (light % 65536), (float) (light / 65536));
        }



        // RenderBlockItem supplies a world-less TileEntity. The Gecko animation
        // pass must only run for animated blocks placed in a world; the old
        // renderer enforced this through AnimatedModelTileBase.isItemRender.
        if (!itemRender && tile instanceof ru.givler.mbo.tileentity.ModelTileBase
                && ((ru.givler.mbo.tileentity.ModelTileBase) tile).isAnimated()
                && this.modelProvider instanceof IAnimatableModel) {
            @SuppressWarnings("unchecked")
            IAnimatableModel<T> animatableModel = (IAnimatableModel<T>) this.modelProvider;
            animatableModel.setLivingAnimations(tile, getInstanceId(tile), null);
        }

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0.0F, 0.01F, 0.0F);
            GlStateManager.translate(0.5, 0.0, 0.5);
            this.rotateBlock(this.getFacing(tile));
            ResourceLocation texture = activeProvider.getTextureLocation(tile);
            if (texture == null) return;
            Minecraft.getMinecraft().renderEngine.bindTexture(texture);
            Color renderColor = this.getRenderColor(tile, partialTicks);
            this.render(model, tile, partialTicks,
                    (float) renderColor.getRed() / 255.0F,
                    (float) renderColor.getGreen() / 255.0F,
                    (float) renderColor.getBlue() / 255.0F,
                    (float) renderColor.getAlpha() / 255.0F);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private int getInstanceId(TileEntity tile) {
        return (tile.xCoord & 0xFFF) ^ ((tile.yCoord & 0xFF) << 12) ^ ((tile.zCoord & 0xFFF) << 20);
    }

    @Override
    public void renderCube(Tessellator builder, GeoCube cube, float red, float green, float blue, float alpha) {
        if (cube == null || cube.quads == null) return;
        boolean hasInvalidQuad = false;
        for (GeoQuad quad : cube.quads) {
            if (quad == null || quad.normal == null || quad.vertices == null) {
                hasInvalidQuad = true;
                break;
            }
        }
        if (hasInvalidQuad) return;
        IGeoRenderer.super.renderCube(builder, cube, red, green, blue, alpha);
    }

    @Override
    public GeoModelProvider<T> getGeoModelProvider() {
        return this.modelProvider;
    }

    protected void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH: GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F); break;
            case WEST:  GlStateManager.rotate(90.0F,  0.0F, 1.0F, 0.0F); break;
            case EAST:  GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F); break;
            case UP:    GlStateManager.rotate(90.0F,  1.0F, 0.0F, 0.0F); break;
            case DOWN:  GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F); break;
            case NORTH:
            default: break;
        }
    }

    protected EnumFacing getFacing(TileEntity tile) {
        EnumFacing[] faces = {EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST};
        if (tile.getBlockType() instanceof BlockDirectional) {
            return faces[BlockDirectional.getDirection(tile.getBlockMetadata())];
        }
        return EnumFacing.SOUTH;
    }

    @Override
    public ResourceLocation getTextureLocation(T instance) {
        return this.modelProvider.getTextureLocation(instance);
    }
}
