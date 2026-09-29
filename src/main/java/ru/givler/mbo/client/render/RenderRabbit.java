package ru.givler.mbo.client.render;

import ru.givler.mbo.client.model.ModelRabbit;
import ru.givler.mbo.entity.EntityRabbit;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderRabbit extends RenderLiving {

	private static final ResourceLocation BROWN = new ResourceLocation("mbo:textures/entity/rabbit/brown.png");
	private static final ResourceLocation WHITE = new ResourceLocation("mbo:textures/entity/rabbit/white.png");
	private static final ResourceLocation BLACK = new ResourceLocation("mbo:textures/entity/rabbit/black.png");
	private static final ResourceLocation GOLD = new ResourceLocation("mbo:textures/entity/rabbit/gold.png");
	private static final ResourceLocation SALT = new ResourceLocation("mbo:textures/entity/rabbit/salt.png");
	private static final ResourceLocation WHITE_SPLOTCHED = new ResourceLocation("mbo:textures/entity/rabbit/white_splotched.png");
	private static final ResourceLocation TOAST = new ResourceLocation("mbo:textures/entity/rabbit/toast.png");

	public RenderRabbit() {
		super(new ModelRabbit(), 0.3F);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityliving, float patialTickTime) {
		GL11.glScalef(0.65F, 0.65F, 0.65F);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		EntityRabbit rabbit = (EntityRabbit) entity;
		String s = EnumChatFormatting.getTextWithoutFormattingCodes(rabbit.getCommandSenderName());

		if (s != null && s.equals("Toast"))
			return TOAST;
		switch (rabbit.getRabbitType()) {
			case 0:
			default:
				return BROWN;
			case 1:
				return WHITE;
			case 2:
				return BLACK;
			case 3:
				return WHITE_SPLOTCHED;
			case 4:
				return GOLD;
			case 5:
				return SALT;
		}
	}
}
