package ru.givler.mbo.integration.thaumcraft.item.focus;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import thaumcraft.api.wands.ItemFocusBasic;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemFocusPartyBasic extends ItemFocusBasic {

    protected double getPartyRadius(ItemStack focusstack) {
        return 8.0D;
    }

    protected List<EntityPlayer> getPartyTargets(World world, EntityPlayer player, ItemStack focusstack) {
        List<EntityPlayer> targets = new ArrayList<>();
        double radius = getPartyRadius(focusstack);

        PlayerData playerData = PlayerData.get(player);
        Party party = playerData.partyUUID != null
                ? PartyController.Instance().getParty(playerData.partyUUID)
                : null;

        List<EntityPlayer> nearby = world.getEntitiesWithinAABB(
                EntityPlayer.class,
                player.boundingBox.expand(radius, radius, radius)
        );

        for (EntityPlayer candidate : nearby) {
            if (candidate == player) {
                targets.add(candidate);
                continue;
            }
            if (party != null && party.hasPlayer(candidate)) {
                targets.add(candidate);
            }
        }
        return targets;
    }

    protected boolean hasThaumcraftArmor(EntityPlayer player) {
        for (int i = 0; i < 4; i++) {
            ItemStack armor = player.inventory.armorItemInSlot(i);
            if (armor == null) return false;

            String modId = GameRegistry.findUniqueIdentifierFor(armor.getItem()).modId;
            if (!modId.equals("Thaumcraft") && !modId.equals("thaumicdyes")) {
                return false;
            }
        }
        return true;
    }
}