package ru.givler.mbo.client.render.weaponskin;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.item.skin.EquipmentSkinData;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition.Pose;
import ru.givler.mbo.registry.EquipmentSkinRegistry;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public final class WeaponSkinRenderer implements IGeoRenderer<EquipmentSkinDefinition> {
    public static final WeaponSkinRenderer INSTANCE = new WeaponSkinRenderer();
    private final GeoModelProvider<EquipmentSkinDefinition> models = new GeoModelProvider<EquipmentSkinDefinition>() {
        @Override public ResourceLocation getModelLocation(EquipmentSkinDefinition skin) { return skin.model; }
        @Override public ResourceLocation getTextureLocation(EquipmentSkinDefinition skin) { return skin.texture; }
    };

    private WeaponSkinRenderer() {}

    public static void register() {
        for (Object entry : Item.itemRegistry) {
            Item item = (Item) entry;
            for (EquipmentSkinDefinition skin : EquipmentSkinRegistry.ALL) {
                if (skin.category != EquipmentSkinDefinition.Category.ARMOR && skin.supports(item)) {
                    MinecraftForgeClient.registerItemRenderer(item, new ModelItemRenderer(item));
                    break;
                }
            }
        }
    }

    public boolean available(EquipmentSkinDefinition skin) {
        return skin != null && GeckoLibCache.getInstance().getGeoModels().get(skin.model) != null;
    }

    void drawArmorBone(EquipmentSkinDefinition skin, String name) {
        GeoModel model = GeckoLibCache.getInstance().getGeoModels().get(skin.model);
        if (model == null) return;
        GeoBone bone = model.getBone(name).orElse(null);
        if (bone == null) return;
        GeoModel part = new GeoModel();
        part.properties = model.properties;
        part.topLevelBones.add(bone);
        render(part, skin, 0, 1, 1, 1, 1);
    }

    private void draw(EquipmentSkinDefinition skin, Pose pose) {
        GeoModel model = GeckoLibCache.getInstance().getGeoModels().get(skin.model);
        if (model == null) return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT
                | GL11.GL_TEXTURE_BIT | GL11.GL_POLYGON_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(pose.x, pose.y, pose.z);
            GL11.glRotatef(pose.rotateZ, 0, 0, 1);
            GL11.glRotatef(pose.rotateY, 0, 1, 0);
            GL11.glRotatef(pose.rotateX, 1, 0, 0);
            GL11.glScalef(pose.scale, pose.scale, pose.scale);
            GeoBone grip = model.getBone("grip").orElse(null);
            if (grip != null) GL11.glTranslatef(-grip.getPivotX() / 16F, -grip.getPivotY() / 16F, -grip.getPivotZ() / 16F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(skin.texture);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glColor4f(1, 1, 1, 1);
            render(model, skin, 0, 1, 1, 1, 1);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    public void preview(EquipmentSkinDefinition skin, Pose pose, float x, float y, float scale) {
        preview(skin, pose, x, y, scale, 80);
    }

    private void preview(EquipmentSkinDefinition skin, Pose pose, float x, float y, float scale, float depth) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glTranslatef(x, y, depth);
            GL11.glScalef(scale, -scale, scale);
            draw(skin, pose);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    @Override public void renderCube(Tessellator builder, GeoCube cube, float r, float g, float b, float a) {
        if (cube == null || cube.quads == null) return;
        for (GeoQuad quad : cube.quads) {
            if (quad != null && (quad.normal == null || quad.vertices == null)) return;
        }
        IGeoRenderer.super.renderCube(builder, cube, r, g, b, a);
    }

    @Override public GeoModelProvider<EquipmentSkinDefinition> getGeoModelProvider() { return models; }
    @Override public ResourceLocation getTextureLocation(EquipmentSkinDefinition skin) { return skin.texture; }

    private static final class ModelItemRenderer implements IItemRenderer {
        private final Map<ItemRenderType, IItemRenderer> previous = new EnumMap<>(ItemRenderType.class);

        ModelItemRenderer(Item item) {
            ItemStack probe = new ItemStack(item);
            for (ItemRenderType type : ItemRenderType.values()) {
                IItemRenderer renderer = MinecraftForgeClient.getItemRenderer(probe, type);
                if (renderer != null) previous.put(type, renderer);
            }
        }

        private EquipmentSkinDefinition visibleSkin(ItemStack stack, ItemRenderType type) {
            if (type == ItemRenderType.FIRST_PERSON_MAP || type == ItemRenderType.INVENTORY) return null;
            EquipmentSkinDefinition skin = EquipmentSkinData.resolve(stack);
            return INSTANCE.available(skin) ? skin : null;
        }

        @Override public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
            IItemRenderer fallback = previous.get(type);
            return visibleSkin(stack, type) != null || fallback != null && fallback.handleRenderType(stack, type);
        }

        @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
            if (visibleSkin(stack, type) != null) {
                return type == ItemRenderType.ENTITY && (helper == ItemRendererHelper.ENTITY_BOBBING
                        || helper == ItemRendererHelper.ENTITY_ROTATION);
            }
            IItemRenderer fallback = previous.get(type);
            return fallback != null && fallback.shouldUseRenderHelper(type, stack, helper);
        }

        @Override public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
            EquipmentSkinDefinition skin = visibleSkin(stack, type);
            if (skin == null) {
                IItemRenderer fallback = previous.get(type);
                if (fallback != null) fallback.renderItem(type, stack, data);
                return;
            }
            switch (type) {
                case EQUIPPED_FIRST_PERSON: INSTANCE.draw(skin, skin.firstPerson); break;
                case EQUIPPED: INSTANCE.draw(skin, skin.thirdPerson); break;
                case ENTITY: INSTANCE.draw(skin, skin.ground); break;
                default: break;
            }
        }
    }
}
