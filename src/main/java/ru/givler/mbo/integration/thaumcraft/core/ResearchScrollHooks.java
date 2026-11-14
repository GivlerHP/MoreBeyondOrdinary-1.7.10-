package ru.givler.mbo.integration.thaumcraft.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

/** Runtime-only scroll state. Never loaded during core transformer registration. */
public final class ResearchScrollHooks {
    private ResearchScrollHooks() {}
    private static final net.minecraft.util.ResourceLocation PAPER=new net.minecraft.util.ResourceLocation("mbo","textures/map/scroll_empty.png");
    private static net.minecraft.entity.Entity posed;
    public static void beginPose(net.minecraft.entity.Entity entity){posed=entity;}
    public static void endPose(){posed=null;}
    private static Item notes;
    private static IItemRenderer renderer;
    public static void bind(Item item,IItemRenderer visual){notes=item;renderer=visual;}
    public static boolean skipOverlay(ItemStack stack,int pass,IItemRenderer.ItemRenderType type) {
        return pass>0 && notes!=null && stack!=null && stack.getItem()==notes
                && (type==IItemRenderer.ItemRenderType.EQUIPPED || type==IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON)
                && MinecraftForgeClient.getItemRenderer(stack,type)==renderer;
    }
    public static boolean mapItem(Item item) {return item instanceof net.minecraft.item.ItemMap || notes!=null && item==notes;}
    public static net.minecraft.world.storage.MapData mapData(Item item,ItemStack stack,net.minecraft.world.World world) {
        return item instanceof net.minecraft.item.ItemMap?((net.minecraft.item.ItemMap)item).getMapData(stack,world):null;
    }
    public static net.minecraft.util.ResourceLocation mapPaper(net.minecraft.util.ResourceLocation original,ItemStack stack) {
        return stack!=null && stack.getItem()==notes?PAPER:original;
    }
    public static boolean holding(net.minecraft.entity.Entity entity) {
        if(!(entity instanceof net.minecraft.entity.player.EntityPlayer) || notes==null)return false;
        ItemStack stack=((net.minecraft.entity.player.EntityPlayer)entity).getHeldItem();
        return stack!=null && stack.getItem()==notes;
    }
    public static boolean holdingLocal(){return holding(net.minecraft.client.Minecraft.getMinecraft().thePlayer);}
    public static void pose(net.minecraft.client.model.ModelBiped model,net.minecraft.entity.Entity entity) {
        if(entity!=posed || !holding(entity))return;
        // Both arms support the same sheet in front of the torso, including armour models.
        model.bipedRightArm.rotateAngleX=-1.4F;model.bipedLeftArm.rotateAngleX=-1.4F;
        model.bipedRightArm.rotateAngleY=.12F;model.bipedLeftArm.rotateAngleY=-.12F;
        model.bipedRightArm.rotateAngleZ=0;model.bipedLeftArm.rotateAngleZ=0;
    }
}
