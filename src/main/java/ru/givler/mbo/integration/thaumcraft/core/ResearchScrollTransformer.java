package ru.givler.mbo.integration.thaumcraft.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** TC4 notes have two icon layers. A custom equipped parchment owns its entire first pass. */
public final class ResearchScrollTransformer implements IClassTransformer,Opcodes {
    private static Item notes;
    private static IItemRenderer renderer;
    public static void bind(Item item,IItemRenderer visual){notes=item;renderer=visual;}
    public static boolean skipOverlay(ItemStack stack,int pass,IItemRenderer.ItemRenderType type) {
        return pass>0 && notes!=null && stack!=null && stack.getItem()==notes
                && (type==IItemRenderer.ItemRenderType.EQUIPPED || type==IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON)
                && MinecraftForgeClient.getItemRenderer(stack,type)==renderer;
    }
    public byte[] transform(String name,String target,byte[] bytes) {
        if(bytes==null || !target.equals("net.minecraft.client.renderer.ItemRenderer"))return bytes;
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
        String owner="ru/givler/mbo/integration/thaumcraft/core/ResearchScrollTransformer";
        for(MethodNode method:node.methods)if(method.name.equals("renderItem")
                && FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V")) {
            for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(owner))return bytes;
            LabelNode original=new LabelNode();InsnList code=new InsnList();
            code.add(new VarInsnNode(ALOAD,2));code.add(new VarInsnNode(ILOAD,3));code.add(new VarInsnNode(ALOAD,4));
            code.add(new MethodInsnNode(INVOKESTATIC,owner,"skipOverlay","(Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)Z",false));
            code.add(new JumpInsnNode(IFEQ,original));code.add(new InsnNode(RETURN));code.add(original);code.add(new FrameNode(F_SAME,0,null,0,null));
            method.instructions.insert(code);count++;
        }
        if(count!=1)throw new IllegalStateException("Research scroll equipped pass anchors: "+count);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
