package ru.givler.mbo.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public final class ModelSign extends ModelBase {
  public final ModelRenderer board = new ModelRenderer(this, 0, 0);
  public final ModelRenderer verticalStick = new ModelRenderer(this, 0, 14);

  public ModelSign() {
    board.addBox(-12F, -14F, -1F, 24, 12, 2, 0F);
    verticalStick.addBox(-1F, -2F, -1F, 2, 14, 2, 0F);
  }

  public void render(boolean standing) {
    verticalStick.showModel = standing;
    board.render(0.0625F);
    verticalStick.render(0.0625F);
  }
}
