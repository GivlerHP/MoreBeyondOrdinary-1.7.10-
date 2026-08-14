package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Adds facing-aware placement to the actual vanilla BlockTrapDoor class. */
public final class TrapdoorPlacementTransformer implements IClassTransformer,Opcodes {
    private static final String TARGET="net.minecraft.block.BlockTrapDoor";
    private static final String HOOK="ru/givler/mbo/core/TrapdoorPlacementHooks";
    private static final String DESC="(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V";

    @Override public byte[] transform(String name,String transformedName,byte[] bytes){
        if(!TARGET.equals(transformedName))return bytes;
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        for(MethodNode method:node.methods){
            if(("onBlockPlacedBy".equals(method.name)||"func_149689_a".equals(method.name))&&DESC.equals(method.desc)){
                inject(method);return write(node);
            }
        }
        String methodName=node.name.equals(TARGET.replace('.','/'))?"onBlockPlacedBy":"func_149689_a";
        MethodNode method=new MethodNode(ACC_PUBLIC,methodName,DESC,null,null);
        addHook(method.instructions);method.instructions.add(new InsnNode(RETURN));node.methods.add(method);
        System.out.println("[MBO ASM] Added vanilla trapdoor facing placement");
        return write(node);
    }

    private static void inject(MethodNode method){
        for(AbstractInsnNode ins=method.instructions.getFirst();ins!=null;ins=ins.getNext()){
            if(ins.getOpcode()!=RETURN)continue;
            InsnList hook=new InsnList();addHook(hook);method.instructions.insertBefore(ins,hook);
        }
        System.out.println("[MBO ASM] Patched vanilla trapdoor facing placement");
    }

    private static void addHook(InsnList code){
        code.add(new VarInsnNode(ALOAD,1));code.add(new VarInsnNode(ILOAD,2));
        code.add(new VarInsnNode(ILOAD,3));code.add(new VarInsnNode(ILOAD,4));code.add(new VarInsnNode(ALOAD,5));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"placedBy","(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;)V",false));
    }
    private static byte[] write(ClassNode node){ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);node.accept(writer);return writer.toByteArray();}
}
