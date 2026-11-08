package ru.givler.mbo.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.Event;

/** Shared player block-access checks. Bukkit is optional and fail-closed. */
public final class BlockInteractionAccess {
    private BlockInteractionAccess() {}
    public static boolean allowed(EntityPlayer player,World world,int x,int y,int z,int face) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, face, world);
        if (MinecraftForge.EVENT_BUS.post(event)) return false;
        return allowed(event);
    }

    /** Reuses an event already on the bus; never posts it recursively. */
    public static boolean allowed(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        World world = event.world;
        int x = event.x, y = event.y, z = event.z, face = event.face;
        if (event.isCanceled() || event.useBlock == Event.Result.DENY || event.useItem == Event.Result.DENY) return false;
        if (face < 0 || face > 5 || !world.canMineBlock(player, x, y, z)
                || !player.canPlayerEdit(x, y, z, face, player.getHeldItem())) return false;
        Class<?> bukkit;
        try { bukkit=Class.forName("org.bukkit.Bukkit"); }
        catch(ClassNotFoundException noBukkit) {return true;}
        try {
            Object bp=player.getClass().getMethod("getBukkitEntity").invoke(player);
            Class<?> playerClass=Class.forName("org.bukkit.entity.Player");
            Object bw=playerClass.getMethod("getWorld").invoke(bp);
            Object block=Class.forName("org.bukkit.World").getMethod("getBlockAt",int.class,int.class,int.class).invoke(bw,x,y,z);
            Class<?> action=Class.forName("org.bukkit.event.block.Action");
            Class<?> blockFace=Class.forName("org.bukkit.block.BlockFace");
            Object a=action.getField("RIGHT_CLICK_BLOCK").get(null);
            String[] faces={"DOWN","UP","NORTH","SOUTH","WEST","EAST"};
            Object f=blockFace.getField(faces[face]).get(null);
            Object held=playerClass.getMethod("getItemInHand").invoke(bp);
            Class<?> interact=Class.forName("org.bukkit.event.player.PlayerInteractEvent");
            Object e=interact.getConstructor(playerClass,action,Class.forName("org.bukkit.inventory.ItemStack"),Class.forName("org.bukkit.block.Block"),blockFace).newInstance(bp,a,held,block,f);
            Object pm=bukkit.getMethod("getPluginManager").invoke(null);
            Class.forName("org.bukkit.plugin.PluginManager").getMethod("callEvent",Class.forName("org.bukkit.event.Event")).invoke(pm,e);
            return !(Boolean)interact.getMethod("isCancelled").invoke(e)
                && !"DENY".equals(String.valueOf(interact.getMethod("useInteractedBlock").invoke(e)));
        } catch(Exception error) {
            System.err.println("[MBO] Block interaction protection check failed: "+error);return false;
        }
    }
}
