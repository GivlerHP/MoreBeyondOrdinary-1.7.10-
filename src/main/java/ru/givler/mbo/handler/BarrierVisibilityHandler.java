package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.registry.BlockRegistry;

public class BarrierVisibilityHandler {

    private boolean wasHoldingBarrier;
    private int lastRenderX;
    private int lastRenderZ;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.thePlayer;
        WorldClient world = minecraft.theWorld;
        if (player == null || world == null) {
            this.wasHoldingBarrier = false;
            return;
        }

        ItemStack held = player.getHeldItem();
        boolean holdingBarrier = held != null
                && held.getItem() == Item.getItemFromBlock(BlockRegistry.Barrier);

        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);
        boolean movedWhileHolding = holdingBarrier
                && (Math.abs(x - this.lastRenderX) >= 16 || Math.abs(z - this.lastRenderZ) >= 16);

        if (holdingBarrier != this.wasHoldingBarrier || movedWhileHolding) {
            world.markBlockRangeForRenderUpdate(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32);
            this.wasHoldingBarrier = holdingBarrier;
            this.lastRenderX = x;
            this.lastRenderZ = z;
        }
    }
}
