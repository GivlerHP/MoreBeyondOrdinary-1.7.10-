package ru.givler.mbo.client.render;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.item.skin.EquipmentSkinData;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition;
import ru.givler.mbo.item.skin.ItemSkinTemplate;
import ru.givler.mbo.tileentity.TileDoll;

/** Client-side model proxy. The doll's inventory and pose live in its block entity. */
public final class DollPlayer extends EntityOtherPlayerMP {
    private static final GameProfile DEFAULT = new GameProfile(new UUID(0, 0), "Doll");
    private static final Map<EquipmentSkinDefinition, Item> SAMPLES = new HashMap<>();
    private static final Item[] ARMOR = {Items.iron_helmet, Items.iron_chestplate, Items.iron_leggings, Items.iron_boots};
    private final GameProfile sourceProfile;
    private final TileDoll doll;
    private final ItemStack[] accessories = new ItemStack[5];
    private final ItemStack[] equipment = new ItemStack[TileDoll.SIZE];
    private ResourceLocation skin;

    private DollPlayer(TileDoll doll, GameProfile profile) {
        super(doll.getWorldObj(), profile == null || profile.getId() == null ? DEFAULT : profile);
        this.doll = doll;
        sourceProfile = profile;
        if (Loader.isModLoaded("minefantasy2")) BattlegearOffhand.prepare(this);
        if (profile != null && profile.getId() != null) {
            Minecraft.getMinecraft().func_152342_ad().func_152790_a(profile, (type, resource) -> {
                if (type == com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN) skin = resource;
            }, false);
        }
    }

    @Override public ResourceLocation getLocationSkin() { return skin == null ? super.getLocationSkin() : skin; }

    public static DollPlayer update(DollPlayer current, TileDoll doll, World world) {
        if (current == null || current.worldObj != world || !java.util.Objects.equals(current.sourceProfile, doll.profile())) {
            current = new DollPlayer(doll, doll.profile());
        }
        for (int slot = 0; slot < TileDoll.SIZE; slot++) {
            ItemStack source = doll.getStackInSlot(slot);
            if (ItemStack.areItemStacksEqual(source, current.equipment[slot])) continue;
            current.equipment[slot] = source == null ? null : source.copy();
            ItemStack visual = materialize(source);
            if (slot < 4) {
                boolean armor = visual != null && visual.getItem() instanceof ItemArmor
                    && ((ItemArmor) visual.getItem()).armorType == slot;
                current.setCurrentItemOrArmor(4 - slot, armor ? visual : null);
                current.accessories[slot] = armor ? null : visual;
            } else if (slot == 4) current.setCurrentItemOrArmor(0, visual);
            else current.accessories[4] = visual;
        }
        return current;
    }

    private static ItemStack materialize(ItemStack source) {
        EquipmentSkinDefinition template = ItemSkinTemplate.skin(source);
        if (template == null || ItemSkinTemplate.slot(source) == null) return source;
        int part = ItemSkinTemplate.slot(source).part();
        Item sample = template.category == EquipmentSkinDefinition.Category.ARMOR
            ? ARMOR[part] : SAMPLES.computeIfAbsent(template, DollPlayer::findSample);
        if (sample == null) return source;
        ItemStack visual = new ItemStack(sample);
        return EquipmentSkinData.set(visual, template.id) ? visual : source;
    }

    private static Item findSample(EquipmentSkinDefinition template) {
        for (Object value : Item.itemRegistry) if (template.supports((Item) value)) return (Item) value;
        return null;
    }

    public static void applyPose(Object modelObject, Object entity) {
        if (!(entity instanceof DollPlayer)) return;
        ModelBiped model = (ModelBiped) modelObject;
        DollPlayer player = (DollPlayer) entity;
        player.rotate(model.bipedHead, 0);
        player.rotate(model.bipedHeadwear, 0);
        player.rotate(model.bipedBody, 1);
        player.rotate(model.bipedRightArm, 2);
        player.rotate(model.bipedLeftArm, 3);
        player.rotate(model.bipedRightLeg, 4);
        player.rotate(model.bipedLeftLeg, 5);
    }

    private void rotate(ModelRenderer model, int part) {
        model.rotateAngleX = (float) (doll.pose(part * 3) * Math.PI / 180);
        model.rotateAngleY = (float) (doll.pose(part * 3 + 1) * Math.PI / 180);
        model.rotateAngleZ = (float) (doll.pose(part * 3 + 2) * Math.PI / 180);
    }

    public static final class Events {
        @SubscribeEvent public void name(net.minecraftforge.client.event.RenderLivingEvent.Specials.Pre event) {
            if (event.entity instanceof DollPlayer) event.setCanceled(true);
        }

        @SubscribeEvent public void accessories(RenderPlayerEvent.Specials.Post event) {
            if (!(event.entityPlayer instanceof DollPlayer)) return;
            DollPlayer doll = (DollPlayer) event.entityPlayer;
            ModelBiped model = event.renderer.modelBipedMain;
            draw(doll, doll.accessories[0], model.bipedHead, 0, -.35F);
            draw(doll, doll.accessories[1], model.bipedBody, 0, .30F);
            draw(doll, doll.accessories[2], model.bipedRightLeg, .10F, .30F);
            draw(doll, doll.accessories[3], model.bipedLeftLeg, -.10F, .65F);
            ItemStack offhand = doll.accessories[4];
            if (offhand == null) return;
            if (Loader.isModLoaded("minefantasy2") && BattlegearOffhand.draw(doll, offhand, model, event.partialRenderTick)) return;
            draw(doll, offhand, model.bipedLeftArm, .06F, .45F);
        }

        private static void draw(DollPlayer doll, ItemStack stack, ModelRenderer bone, float x, float y) {
            if (stack == null) return;
            GL11.glPushMatrix();
            try {
                bone.postRender(.0625F);
                GL11.glTranslatef(x, y, -.12F);
                GL11.glRotatef(-90, 1, 0, 0);
                GL11.glRotatef(25, 0, 1, 0);
                GL11.glScalef(.65F, .65F, .65F);
                for (int pass = 0; pass < stack.getItem().getRenderPasses(stack.getItemDamage()); pass++) {
                    int color = stack.getItem().getColorFromItemStack(stack, pass);
                    GL11.glColor3ub((byte) (color >> 16), (byte) (color >> 8), (byte) color);
                    RenderManager.instance.itemRenderer.renderItem(doll, stack, pass);
                }
            } finally {
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glPopMatrix();
            }
        }
    }

    private static final class BattlegearOffhand {
        static void prepare(DollPlayer doll) {
            if (!(doll.inventory instanceof mods.battlegear2.api.core.InventoryPlayerBattle)) {
                doll.inventory = new mods.battlegear2.api.core.InventoryPlayerBattle(doll);
            }
        }

        static boolean draw(DollPlayer doll, ItemStack stack, ModelBiped model, float partial) {
            if (!(doll.inventory instanceof mods.battlegear2.api.core.InventoryPlayerBattle)) return false;
            mods.battlegear2.api.core.InventoryPlayerBattle inventory =
                (mods.battlegear2.api.core.InventoryPlayerBattle) doll.inventory;
            int slot = inventory.isBattlemode() ? inventory.currentItem + mods.battlegear2.api.core.InventoryPlayerBattle.WEAPON_SETS
                : mods.battlegear2.api.core.InventoryPlayerBattle.OFFHAND_SLOT;
            int mainSlot = inventory.currentItem;
            ItemStack previousOffhand = inventory.getStackInSlot(slot);
            ItemStack main = inventory.getCurrentItem();
            boolean twoHanded = mods.battlegear2.api.core.BattlegearUtils.isMainHandTwoHanded(doll);
            inventory.setInventorySlotContents(slot, stack, false);
            if (twoHanded) inventory.setInventorySlotContents(mainSlot, null, false);
            try {
                if (stack.getItem() instanceof mods.battlegear2.api.shield.IShield) {
                    drawShield(doll, model, partial);
                } else {
                    mods.battlegear2.client.utils.BattlegearRenderHelper.renderItemIn3rdPerson(doll, model, partial, null);
                }
                return true;
            } finally {
                inventory.setInventorySlotContents(slot, previousOffhand, false);
                if (twoHanded) inventory.setInventorySlotContents(mainSlot, main, false);
            }
        }

        private static void drawShield(DollPlayer doll, ModelBiped model, float partial) {
            int activeTexture = GL11.glGetInteger(org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE);
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_TEXTURE_BIT);
            try {
                // Match the inventory icon's unlit layers while keeping Battlegear's grip.
                GL11.glDisable(GL11.GL_LIGHTING);
                OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
                mods.battlegear2.client.utils.BattlegearRenderHelper.renderItemIn3rdPerson(doll, model, partial, null);
            } finally {
                GL11.glPopAttrib();
                OpenGlHelper.setActiveTexture(activeTexture);
            }
        }
    }
}
