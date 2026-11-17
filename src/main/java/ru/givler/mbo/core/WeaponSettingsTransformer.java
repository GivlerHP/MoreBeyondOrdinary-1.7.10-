package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Apply individual stats at the stack and MF2 damage-ratio boundaries, on both sides. */
public final class WeaponSettingsTransformer implements IClassTransformer, Opcodes {
    private static final String HOOK="ru/givler/mbo/item/WeaponSettings";
    public byte[] transform(String name,String transformedName,byte[] bytes) {
        boolean stack="net.minecraft.item.ItemStack".equals(transformedName);
        boolean ratio="minefantasy.mf2.api.helpers.ArmourCalculator".equals(transformedName);
        if(bytes==null || !stack && !ratio) return bytes;
        ClassNode node=new ClassNode(); new ClassReader(bytes).accept(node,0); boolean changed=false;
        for(MethodNode method:node.methods) {
            String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
            String desc=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
            String hook=null,hookDesc=null; int local=0,opcode=ARETURN;
            if(stack && (mapped.equals("getMaxDamage") || mapped.equals("func_77958_k")) && desc.equals("()I")) {
                hook="maxDamage"; hookDesc="(ILnet/minecraft/item/ItemStack;)I"; opcode=IRETURN;
            } else if(stack && (mapped.equals("getAttributeModifiers") || mapped.equals("func_111283_C"))
                    && desc.equals("()Lcom/google/common/collect/Multimap;")) {
                hook="attributes"; hookDesc="(Lcom/google/common/collect/Multimap;Lnet/minecraft/item/ItemStack;)Lcom/google/common/collect/Multimap;";
            } else if(ratio && method.name.equals("getRatioForWeapon") && desc.endsWith("Lnet/minecraft/item/ItemStack;)[F")) {
                hook="ratio"; hookDesc="([FLnet/minecraft/item/ItemStack;)[F"; local=Type.getArgumentTypes(desc).length-1;
            }
            if(hook==null) continue;
            boolean already=false;
            for(AbstractInsnNode insn:method.instructions.toArray()) if(insn instanceof MethodInsnNode && HOOK.equals(((MethodInsnNode)insn).owner)) already=true;
            if(already) continue;
            for(AbstractInsnNode insn:method.instructions.toArray()) if(insn.getOpcode()==opcode) {
                InsnList patch=new InsnList(); patch.add(new VarInsnNode(ALOAD,local));
                patch.add(new MethodInsnNode(INVOKESTATIC,HOOK,hook,hookDesc,false));
                method.instructions.insertBefore(insn,patch); changed=true;
            }
        }
        if(!changed) return bytes;
        ClassWriter writer=SafeClassWriter.create(); node.accept(writer); return writer.toByteArray();
    }
}
