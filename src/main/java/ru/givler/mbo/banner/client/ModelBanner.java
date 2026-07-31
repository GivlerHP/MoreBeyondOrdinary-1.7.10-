package ru.givler.mbo.banner.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelBanner extends ModelBase {
    public final ModelRenderer slate, stand, top;
    public ModelBanner() {
        textureWidth=64; textureHeight=64;
        slate=new ModelRenderer(this,0,0); slate.addBox(-10,0,-2,20,40,1);
        stand=new ModelRenderer(this,44,0); stand.addBox(-1,-30,-1,2,42,2);
        top=new ModelRenderer(this,0,42); top.addBox(-10,-32,-1,20,2,2);
    }
    public void renderAll() {
        slate.rotationPointY=-32;
        slate.render(.0625F); stand.render(.0625F); top.render(.0625F);
    }
}
