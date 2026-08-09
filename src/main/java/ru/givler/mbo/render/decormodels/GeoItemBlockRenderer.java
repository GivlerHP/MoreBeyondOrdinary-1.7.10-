package ru.givler.mbo.render.decormodels;

import net.geckominecraft.client.renderer.GlStateManager;
import net.minecraft.block.BlockDirectional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

/** Exact non-animated Gecko render path used by model items before the refactor. */
public class GeoItemBlockRenderer<T extends TileEntity> extends TileEntitySpecialRenderer
        implements IGeoRenderer<T> {

    private final GeoModelProvider<T> modelProvider;

    public GeoItemBlockRenderer(GeoModelProvider<T> modelProvider) {
        this.modelProvider = modelProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        render((T) te, x, y, z, partialTicks);
    }

    public void render(T tile, double x, double y, double z, float partialTicks) {
        ResourceLocation modelLocation = modelProvider.getModelLocation(tile);
        if (modelLocation == null) return;
        GeoModel model = modelProvider.getModel(modelLocation);
        if (model == null) return;
        ResourceLocation texture = modelProvider.getTextureLocation(tile);
        if (texture == null) return;

        int light = 15;
        if (tile.getWorldObj() != null) {
            light = tile.getWorldObj().getLightBrightnessForSkyBlocks(
                    tile.xCoord, tile.yCoord, tile.zCoord, 0);
        }
        int lx = light % 65536;
        int ly = light / 65536;
        if (tile.xCoord != 0 && tile.yCoord != 0 && tile.zCoord != 0) {
            OpenGlHelper.setLightmapTextureCoords(
                    OpenGlHelper.lightmapTexUnit, (float) lx, (float) ly);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.0F, 0.01F, 0.0F);
        GlStateManager.translate(0.5, 0.0, 0.5);
        rotateBlock(getFacing(tile));
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        Color color = getRenderColor(tile, partialTicks);
        render(model, tile, partialTicks,
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                color.getAlpha() / 255.0F);
        GlStateManager.popMatrix();
    }

    @Override
    public void renderCube(Tessellator builder, GeoCube cube,
                           float red, float green, float blue, float alpha) {
        if (cube == null || cube.quads == null) return;
        for (GeoQuad quad : cube.quads) {
            if (quad == null || quad.normal == null || quad.vertices == null) return;
        }
        IGeoRenderer.super.renderCube(builder, cube, red, green, blue, alpha);
    }

    @Override
    public GeoModelProvider<T> getGeoModelProvider() {
        return modelProvider;
    }

    @Override
    public ResourceLocation getTextureLocation(T instance) {
        return modelProvider.getTextureLocation(instance);
    }

    private void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH: GlStateManager.rotate(180.0F, 0, 1, 0); break;
            case WEST:  GlStateManager.rotate(90.0F, 0, 1, 0); break;
            case EAST:  GlStateManager.rotate(270.0F, 0, 1, 0); break;
            case UP:    GlStateManager.rotate(90.0F, 1, 0, 0); break;
            case DOWN:  GlStateManager.rotate(90.0F, -1, 0, 0); break;
            case NORTH:
            default: break;
        }
    }

    private EnumFacing getFacing(TileEntity tile) {
        EnumFacing[] faces = {EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST};
        if (tile.getBlockType() instanceof BlockDirectional) {
            return faces[BlockDirectional.getDirection(tile.getBlockMetadata())];
        }
        return EnumFacing.SOUTH;
    }
}
