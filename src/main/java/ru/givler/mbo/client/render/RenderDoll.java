package ru.givler.mbo.client.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.givler.mbo.tileentity.TileDoll;

public final class RenderDoll extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partial) {
        TileDoll doll = (TileDoll) tile;
        DollPlayer player = DollPlayer.update((DollPlayer) doll.clientModel, doll, doll.getWorldObj());
        doll.clientModel = player;
        player.setPosition(doll.xCoord + .5, doll.yCoord, doll.zCoord + .5);
        float yaw = doll.getBlockMetadata() * 22.5F;
        player.renderYawOffset = player.prevRenderYawOffset = player.rotationYaw = player.prevRotationYaw
            = player.rotationYawHead = player.prevRotationYawHead = yaw;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslated(x + .5 + doll.pose(18) * .05, y + doll.pose(19) * .05, z + .5 + doll.pose(20) * .05);
            GL11.glScalef(.5F, .5F, .5F);
            RenderManager.instance.renderEntityWithPosYaw(player, 0, 0, 0, yaw, partial);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}
