package ru.givler.mbo.client.render.light;

import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.IOffhandRender;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.client.utils.BattlegearRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** Linked only behind the MF2 mod-presence check. */
final class MF2PortableCarryBridge {
  static ItemStack offhand(EntityPlayer player) {
    ItemStack main=player.getCurrentEquippedItem();
    if(main!=null && main.getItem() instanceof minefantasy.mf2.api.weapon.IDualWield)return null;
    return player.inventory instanceof InventoryPlayerBattle && !BattlegearUtils.isMainHandTwoHanded(player)
        ? ((InventoryPlayerBattle)player.inventory).getCurrentOffhandWeapon() : null;
  }
  static ItemStack cached(ItemRenderer renderer,EntityPlayer player) {
    return renderer instanceof IOffhandRender && !BattlegearUtils.isMainHandTwoHanded(player)
        ? ((IOffhandRender)renderer).getItemToRender() : null;
  }
  static float equip(ItemRenderer renderer,float partial) {
    if(!(renderer instanceof IOffhandRender))return 0;
    IOffhandRender r=(IOffhandRender)renderer;
    return 1-(r.getPrevEquippedProgress()+(r.getEquippedProgress()-r.getPrevEquippedProgress())*partial);
  }
  static float swing(EntityPlayer player) {return swing(player,1);}
  static float swing(EntityPlayer player,float partial) {return player instanceof IBattlePlayer?((IBattlePlayer)player).getOffSwingProgress(partial):0;}
  static void renderRemaining(ItemRenderer renderer,float partial) {
    BattlegearRenderHelper.renderItemInFirstPerson(partial,Minecraft.getMinecraft(),renderer);
  }
}
