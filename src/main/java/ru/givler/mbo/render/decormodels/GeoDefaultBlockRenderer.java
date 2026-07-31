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

public class GeoDefaultBlockRenderer<T extends TileEntity> extends TileEntitySpecialRenderer implements IGeoRenderer<T> {
    private final GeoModelProvider<T> modelProvider;

    public GeoDefaultBlockRenderer(GeoModelProvider<T> modelProvider) {
        this.modelProvider = modelProvider;
    }

    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        this.render((T) te, x, y, z, partialTicks);
    }

    public void render(T tile, double x, double y, double z, float partialTicks) {
        ResourceLocation modelLocation = this.modelProvider.getModelLocation(tile);
        if (modelLocation == null) return;

        GeoModel model = this.modelProvider.getModel(modelLocation);
        if (model == null) return;

        ResourceLocation texture = this.getTextureLocation(tile);
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
        this.rotateBlock(this.getFacing(tile));
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        Color renderColor = this.getRenderColor(tile, partialTicks);
        this.render(model, tile, partialTicks,
                (float) renderColor.getRed()   / 255.0F,
                (float) renderColor.getGreen() / 255.0F,
                (float) renderColor.getBlue()  / 255.0F,
                (float) renderColor.getAlpha() / 255.0F);
        GlStateManager.popMatrix();
    }

    @Override
    public void renderCube(Tessellator builder, GeoCube cube, float red, float green, float blue, float alpha) {
        if (cube == null || cube.quads == null) return;
        for (GeoQuad quad : cube.quads) {
            if (quad == null || quad.normal == null || quad.vertices == null) return;
        }
        IGeoRenderer.super.renderCube(builder, cube, red, green, blue, alpha);
    }



    public GeoModelProvider<T> getGeoModelProvider() {
        return this.modelProvider;
    }

    protected void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case NORTH:
            default:
                break;
            case EAST:
                GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
                break;
            case UP:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case DOWN:
                GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
        }

    }

    protected EnumFacing getFacing(TileEntity tile) {
        EnumFacing[] faces = {EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST, };
        if (tile.getBlockType() instanceof BlockDirectional) {
            return faces[BlockDirectional.getDirection(tile.getBlockMetadata())];
        }
        return EnumFacing.SOUTH; //TODO
    }

    public ResourceLocation getTextureLocation(T instance) {
        return this.modelProvider.getTextureLocation(instance);
    }
}

