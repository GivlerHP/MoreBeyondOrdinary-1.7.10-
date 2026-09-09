package ru.givler.mbo.item;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformAccess;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformOpen;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemPlatformEditor extends Item {
    public ItemPlatformEditor() {
        setUnlocalizedName("PlatformEditor");
        setTextureName(MoreBeyondOrdinary.MODID + ":admin_key");
        setCreativeTab(CreativeTabRegistry.tabMBOitems);
        setMaxStackSize(1);
    }

    public void selectFirst(ItemStack stack, int x, int y, int z, EntityPlayer player) {
        if (!PlatformAccess.canEdit(player)) return;
        setPoint(stack, "Pos1", player.worldObj.provider.dimensionId, x, y, z);
        if (!player.worldObj.isRemote) player.addChatMessage(new ChatComponentTranslation("mbo.platform.pos1", x, y, z));
    }

    public void selectSecondOrOpen(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        selectSecondOrOpen(stack,player,world,x,y,z,player.isSneaking());
    }

    public void selectSecondOrOpen(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, boolean open) {
        if (!PlatformAccess.canEdit(player)) return;
        if (open) {
            if (!world.isRemote) openOrCreate(stack, player, world, x, y, z);
            return;
        }
        setPoint(stack, "Pos2", world.provider.dimensionId, x, y, z);
        if (!world.isRemote) player.addChatMessage(new ChatComponentTranslation("mbo.platform.pos2", x, y, z));
    }

    @SuppressWarnings("unchecked")
    private void openOrCreate(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        java.util.UUID linked=getLinkedPlatform(stack);
        if(linked!=null)for(Object object:new java.util.ArrayList(world.loadedEntityList)){
            if(object instanceof EntityMovingPlatform&&linked.equals(((EntityMovingPlatform)object).getPlatformId())){
                open(player,(EntityMovingPlatform)object,stack);return;
            }
        }
        for (Object object : new java.util.ArrayList(world.loadedEntityList)) {
            if (!(object instanceof EntityMovingPlatform)) continue;
            EntityMovingPlatform platform = (EntityMovingPlatform)object;
            if (platform.contains(x,y,z)) { open(player, platform, stack); return; }
        }
        int[] a=getPoint(stack,"Pos1",world.provider.dimensionId), b=getPoint(stack,"Pos2",world.provider.dimensionId);
        if(a==null||b==null){player.addChatMessage(new ChatComponentTranslation("mbo.platform.error.selection"));return;}
        int minX=Math.min(a[0],b[0]),minY=Math.min(a[1],b[1]),minZ=Math.min(a[2],b[2]);
        int maxX=Math.max(a[0],b[0]),maxY=Math.max(a[1],b[1]),maxZ=Math.max(a[2],b[2]);
        if((long)(maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1)>4096L){player.addChatMessage(new ChatComponentTranslation("mbo.platform.error.tooLarge"));return;}
        EntityMovingPlatform platform=EntityMovingPlatform.create(world,player,minX,minY,minZ,maxX,maxY,maxZ);
        if(platform!=null&&world.spawnEntityInWorld(platform))open(player,platform,stack);
    }

    public static void open(EntityPlayer player, EntityMovingPlatform platform, ItemStack stack) {
        if(!stack.hasTagCompound())stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("PlatformId",platform.getPlatformId().toString());
        if(player instanceof net.minecraft.entity.player.EntityPlayerMP)
            PacketManager.INSTANCE.sendTo(new PacketPlatformOpen(platform),(net.minecraft.entity.player.EntityPlayerMP)player);
    }

    public static int[] getPoint(ItemStack stack,String key,int dimension){
        if(stack==null||!stack.hasTagCompound()||!stack.getTagCompound().hasKey(key))return null;
        NBTTagCompound p=stack.getTagCompound().getCompoundTag(key);if(p.getInteger("D")!=dimension)return null;
        return new int[]{p.getInteger("X"),p.getInteger("Y"),p.getInteger("Z")};
    }
    public static java.util.UUID getLinkedPlatform(ItemStack stack){
        if(stack==null||!stack.hasTagCompound()||!stack.getTagCompound().hasKey("PlatformId"))return null;
        try{return java.util.UUID.fromString(stack.getTagCompound().getString("PlatformId"));}catch(Exception ignored){return null;}
    }
    private static void setPoint(ItemStack stack,String key,int dimension,int x,int y,int z){
        if(!stack.hasTagCompound())stack.setTagCompound(new NBTTagCompound());NBTTagCompound p=new NBTTagCompound();
        if("Pos1".equals(key))stack.getTagCompound().removeTag("PlatformId");
        p.setInteger("D",dimension);p.setInteger("X",x);p.setInteger("Y",y);p.setInteger("Z",z);stack.getTagCompound().setTag(key,p);
    }
}
