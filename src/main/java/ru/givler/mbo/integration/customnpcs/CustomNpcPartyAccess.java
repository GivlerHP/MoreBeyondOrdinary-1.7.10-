package ru.givler.mbo.integration.customnpcs;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;

/** Loaded reflectively only when CustomNPC+ is installed. */
public final class CustomNpcPartyAccess {
    private CustomNpcPartyAccess() {}

    public static boolean isPartyMember(EntityPlayer owner, EntityPlayer candidate) {
        PlayerData data = PlayerData.get(owner);
        if (data.partyUUID == null) return false;
        Party party = PartyController.Instance().getParty(data.partyUUID);
        return party != null && party.hasPlayer(candidate);
    }
}
