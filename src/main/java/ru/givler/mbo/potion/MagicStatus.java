package ru.givler.mbo.potion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

/** A status whose behaviour is handled centrally by MBO's PotionEvents. */
public class MagicStatus extends PotionBasic {
    private static final ResourceLocation ICONS = new ResourceLocation(
            MoreBeyondOrdinary.MODID, "textures/gui/effects/magic_effects.png");
    private final int iconIndex;

    public MagicStatus(int id, boolean harmful, int colour, String name, int iconIndex) {
        super(id, harmful, colour);
        this.iconIndex = iconIndex;
        setPotionName("potion." + name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.renderEngine.bindTexture(ICONS);
        drawTexturedRect(x + 6, y + 7, 18 * (iconIndex % 4), 18 * (iconIndex / 4),
                18, 18, 72, 72);
    }
}
