package ru.givler.mbo.client.render.weaponskin;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.item.skin.WardrobeData;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition;

public final class ArmorSkinRenderer extends ModelBiped {
    private static final Map<EquipmentSkinDefinition, ArmorSkinRenderer[]> MODELS = new HashMap<>();
    private final EquipmentSkinDefinition skin;
    private final int slot;

    private ArmorSkinRenderer(EquipmentSkinDefinition skin, int slot) {
        this.skin = skin;
        this.slot = slot;
    }

    private static EquipmentSkinDefinition resolve(Object item, Object entity) {
        EquipmentSkinDefinition skin = WardrobeData.resolve((ItemStack) item, (Entity) entity);
        return skin != null && skin.category == EquipmentSkinDefinition.Category.ARMOR
                && WeaponSkinRenderer.INSTANCE.available(skin) ? skin : null;
    }

    public static Object model(Object item, Object entity, Object defaultModel) {
        EquipmentSkinDefinition skin = resolve(item, entity);
        if (skin == null) return null;
        int slot = ((ItemArmor) ((ItemStack) item).getItem()).armorType;
        ArmorSkinRenderer[] slots = MODELS.computeIfAbsent(skin, key -> new ArmorSkinRenderer[4]);
        if (slots[slot] == null) slots[slot] = new ArmorSkinRenderer(skin, slot);
        ArmorSkinRenderer model = slots[slot];
        ModelBiped base = (ModelBiped) defaultModel;
        model.heldItemLeft = base.heldItemLeft;
        model.heldItemRight = base.heldItemRight;
        model.isSneak = base.isSneak;
        model.aimedBow = base.aimedBow;
        return model;
    }

    public static Object texture(Object item, Object entity) {
        EquipmentSkinDefinition skin = resolve(item, entity);
        return skin == null ? null : skin.texture.toString();
    }

    @Override public void render(Entity entity, float swing, float amount, float age,
                                 float yaw, float pitch, float scale) {
        setRotationAngles(swing, amount, age, yaw, pitch, scale, entity);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glDisable(GL11.GL_CULL_FACE);
            if (isChild) {
                if (slot == 0) GL11.glTranslatef(0, 16 * scale, 0);
                else {
                    GL11.glScalef(.5F, .5F, .5F);
                    GL11.glTranslatef(0, 24 * scale, 0);
                }
            }
            if (slot == 0) draw(bipedHead, "head", 0, 24, scale);
            else if (slot == 1) {
                draw(bipedBody, "body", 0, 24, scale);
                draw(bipedRightArm, "right_arm", 5, 22, scale);
                draw(bipedLeftArm, "left_arm", -5, 22, scale);
            } else if (slot == 2) {
                draw(bipedRightLeg, "right_leg", 2, 12, scale);
                draw(bipedLeftLeg, "left_leg", -2, 12, scale);
                float right = bipedRightLeg.rotateAngleX;
                float left = bipedLeftLeg.rotateAngleX;
                drawRobePanel("robe_front", Math.min(0, Math.min(right, left)), scale);
                drawRobePanel("robe_back", Math.max(0, Math.max(right, left)), scale);
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void draw(ModelRenderer pose, String bone, float anchorX, float anchorY, float scale) {
        GL11.glPushMatrix();
        try {
            pose.postRender(scale);
            // Blockbench's source-right is +X and up is +Y. ModelBiped uses the opposite axes.
            GL11.glScalef(-16 * scale, -16 * scale, 16 * scale);
            GL11.glTranslatef(-anchorX / 16, -anchorY / 16, 0);
            WeaponSkinRenderer.INSTANCE.drawArmorBone(skin, bone);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private void drawRobePanel(String bone, float angle, float scale) {
        GL11.glPushMatrix();
        try {
            bipedBody.postRender(scale);
            GL11.glTranslatef(0, 12 * scale, 0);
            GL11.glRotatef((float) Math.toDegrees(angle), 1, 0, 0);
            GL11.glScalef(-16 * scale, -16 * scale, 16 * scale);
            GL11.glTranslatef(0, -12F / 16F, 0);
            WeaponSkinRenderer.INSTANCE.drawArmorBone(skin, bone);
        } finally {
            GL11.glPopMatrix();
        }
    }
}
