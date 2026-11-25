package ru.givler.mbo.client.render.weaponskin;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.battlegear2.api.RenderPlayerEventChild.PlayerElementType;
import mods.battlegear2.api.RenderPlayerEventChild.PreRenderPlayerElement;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.client.utils.BattlegearRenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.client.gui.GuiWardrobe;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition;
import ru.givler.mbo.item.skin.WardrobeData;

public final class WardrobeOffhand {
    static void register() {
        WardrobeOffhand handler = new WardrobeOffhand();
        BattlegearUtils.RENDER_BUS.register(handler);
        MinecraftForge.EVENT_BUS.register(handler);
    }

    @SubscribeEvent public void skin(PreRenderPlayerElement event) {
        if (event.type != PlayerElementType.ItemOffhand || event.element == null
                || !(event.element.getItem() instanceof IShield)) return;
        EquipmentSkinDefinition skin = WardrobeData.resolve(event.element, event.entityPlayer);
        if (skin != null && skin.category == EquipmentSkinDefinition.Category.SHIELD
                && WeaponSkinRenderer.INSTANCE.available(skin)) {
            WeaponSkinRenderer.INSTANCE.drawHeld(skin, event.isFirstPerson);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent public void previewShield(RenderPlayerEvent.Specials.Post event) {
        EntityPlayer player = event.entityPlayer;
        if (!(player instanceof GuiWardrobe.PreviewPlayer) || !(player.inventory instanceof InventoryPlayerBattle)
                || !BattlegearUtils.isMainHandTwoHanded(player)) return;
        InventoryPlayerBattle inventory = (InventoryPlayerBattle) player.inventory;
        if (inventory.getCurrentOffhandWeapon() == null) return;
        int selected = inventory.currentItem;
        ItemStack main = inventory.getCurrentItem();
        inventory.setInventorySlotContents(selected, null);
        try {
            BattlegearRenderHelper.renderItemIn3rdPerson(player, event.renderer.modelBipedMain, event.partialRenderTick, null);
        } finally {
            inventory.setInventorySlotContents(selected, main);
        }
    }
}
