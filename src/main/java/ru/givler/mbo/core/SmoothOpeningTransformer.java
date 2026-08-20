package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class SmoothOpeningTransformer implements IClassTransformer, Opcodes {
    private static final String RENDER_BLOCKS = "net.minecraft.client.renderer.RenderBlocks";
    private static final String TESSELLATOR = "net.minecraft.client.renderer.Tessellator";
    private static final String CARPENTER_BASE = "com.carpentersblocks.renderer.BlockHandlerBase";
    private static final String HOOK = "ru/givler/mbo/client/render/SmoothOpeningRenderer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;
        try {
            if (RENDER_BLOCKS.equals(transformedName)) return patchRenderBlocks(bytes);
            if (TESSELLATOR.equals(transformedName)) return patchTessellator(bytes);
            if (CARPENTER_BASE.equals(transformedName)) return patchCarpenterRenderer(bytes);
            return bytes;
        } catch (Throwable error) {
            System.err.println("[MBO ASM] Smooth-opening patch failed for " + transformedName
                    + "; keeping the original class: " + error);
            error.printStackTrace();
            return bytes;
        }
    }

    private byte[] patchCarpenterRenderer(byte[] bytes){
        ClassNode node=read(bytes);
        String desc="(Lnet/minecraft/item/ItemStack;IIIDDDDDD[Lnet/minecraftforge/common/util/ForgeDirection;)V";
        for(MethodNode m:node.methods){
            if(!"renderBlockWithRotation".equals(m.name)||!desc.equals(m.desc))continue;
            InsnList code=new InsnList();
            code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(ALOAD,1));
            code.add(new VarInsnNode(ILOAD,2));code.add(new VarInsnNode(ILOAD,3));code.add(new VarInsnNode(ILOAD,4));
            code.add(new VarInsnNode(DLOAD,5));code.add(new VarInsnNode(DLOAD,7));code.add(new VarInsnNode(DLOAD,9));
            code.add(new VarInsnNode(DLOAD,11));code.add(new VarInsnNode(DLOAD,13));code.add(new VarInsnNode(DLOAD,15));
            code.add(new VarInsnNode(ALOAD,17));
            code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"splitCarpenterGateCuboid",
                    "(Ljava/lang/Object;Lnet/minecraft/item/ItemStack;IIIDDDDDD[Lnet/minecraftforge/common/util/ForgeDirection;)Z",false));
            LabelNode normal=new LabelNode();code.add(new JumpInsnNode(IFEQ,normal));code.add(new InsnNode(RETURN));code.add(normal);
            m.instructions.insert(code);
            System.out.println("[MBO ASM] Patched Carpenter gate cuboid splitting");
            return write(node);
        }
        System.err.println("[MBO ASM] Carpenter cuboid renderer not found");return bytes;
    }

    private byte[] patchRenderBlocks(byte[] bytes) {
        ClassNode node=read(bytes);
        String desc="(Lnet/minecraft/block/Block;III)Z";
        for(MethodNode m:node.methods){
            if(!matches(node.name,m,desc,"renderBlockByRenderType","func_147805_b")) continue;
            InsnList head=new InsnList();
            head.add(new VarInsnNode(ALOAD,0)); head.add(new VarInsnNode(ALOAD,1));
            head.add(new VarInsnNode(ILOAD,2)); head.add(new VarInsnNode(ILOAD,3)); head.add(new VarInsnNode(ILOAD,4));
            head.add(new MethodInsnNode(INVOKESTATIC,HOOK,"begin","(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;III)Z",false));
            LabelNode vanilla=new LabelNode();
            head.add(new JumpInsnNode(IFEQ,vanilla));
            head.add(new InsnNode(ICONST_1));
            head.add(new InsnNode(IRETURN));
            head.add(vanilla);
            m.instructions.insert(head);
            for(AbstractInsnNode ins=m.instructions.getFirst();ins!=null;ins=ins.getNext()){
                if(ins.getOpcode()==IRETURN){ InsnList end=new InsnList(); end.add(new MethodInsnNode(INVOKESTATIC,HOOK,"end","()V",false)); m.instructions.insertBefore(ins,end); }
            }
            System.out.println("[MBO ASM] Patched RenderBlocks vertex animation scope");
            return write(node);
        }
        System.err.println("[MBO ASM] RenderBlocks animation method not found"); return bytes;
    }

    private byte[] patchTessellator(byte[] bytes){
        ClassNode node=read(bytes); String desc="(DDDDD)V"; boolean vertexPatched=false;
        for(MethodNode m:node.methods){
            if(matches(node.name,m,desc,"addVertexWithUV","func_78374_a")) {
                int local=m.maxLocals; m.maxLocals+=1;
                InsnList code=new InsnList();
                code.add(new VarInsnNode(DLOAD,1)); code.add(new VarInsnNode(DLOAD,3)); code.add(new VarInsnNode(DLOAD,5));
                code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"transform","(DDD)[D",false));
                code.add(new VarInsnNode(ASTORE,local));
                code.add(new VarInsnNode(ALOAD,local)); code.add(new InsnNode(ICONST_0)); code.add(new InsnNode(DALOAD)); code.add(new VarInsnNode(DSTORE,1));
                code.add(new VarInsnNode(ALOAD,local)); code.add(new InsnNode(ICONST_1)); code.add(new InsnNode(DALOAD)); code.add(new VarInsnNode(DSTORE,3));
                code.add(new VarInsnNode(ALOAD,local)); code.add(new InsnNode(ICONST_2)); code.add(new InsnNode(DALOAD)); code.add(new VarInsnNode(DSTORE,5));
                m.instructions.insert(code); vertexPatched=true;
            } else if(matches(node.name,m,"(FFF)V","setColorOpaque_F","func_78386_a")) {
                int local=m.maxLocals; m.maxLocals+=1;
                InsnList code=new InsnList();
                code.add(new VarInsnNode(FLOAD,1));code.add(new VarInsnNode(FLOAD,2));code.add(new VarInsnNode(FLOAD,3));
                code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"correctColor","(FFF)[F",false));code.add(new VarInsnNode(ASTORE,local));
                code.add(new VarInsnNode(ALOAD,local));code.add(new InsnNode(ICONST_0));code.add(new InsnNode(FALOAD));code.add(new VarInsnNode(FSTORE,1));
                code.add(new VarInsnNode(ALOAD,local));code.add(new InsnNode(ICONST_1));code.add(new InsnNode(FALOAD));code.add(new VarInsnNode(FSTORE,2));
                code.add(new VarInsnNode(ALOAD,local));code.add(new InsnNode(ICONST_2));code.add(new InsnNode(FALOAD));code.add(new VarInsnNode(FSTORE,3));
                m.instructions.insert(code);
            } else if(matches(node.name,m,"(I)V","setBrightness","func_78380_c")) {
                InsnList code=new InsnList();
                code.add(new VarInsnNode(ILOAD,1));
                code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"correctBrightness","(I)I",false));
                code.add(new VarInsnNode(ISTORE,1));
                m.instructions.insert(code);
            }
        }
        if(vertexPatched){System.out.println("[MBO ASM] Patched Tessellator vertex animation and color");return write(node);}
        System.err.println("[MBO ASM] Tessellator UV vertex method not found"); return bytes;
    }

    private static boolean matches(String owner,MethodNode m,String desc,String... names){
        FMLDeobfuscatingRemapper r=FMLDeobfuscatingRemapper.INSTANCE;
        if(!desc.equals(m.desc)&&!desc.equals(r.mapMethodDesc(m.desc))) return false;
        String mapped=r.mapMethodName(owner,m.name,m.desc);
        for(String n:names) if(n.equals(m.name)||n.equals(mapped)) return true;
        return false;
    }
    private static ClassNode read(byte[] b){ClassNode n=new ClassNode();new ClassReader(b).accept(n,0);return n;}
    private static byte[] write(ClassNode n){
        /*
         * Do not use ClassWriter's default getCommonSuperClass here. It loads
         * classes through the system class loader while LaunchWrapper may still
         * be defining RenderBlocks itself. In an obfuscated client that recursive
         * lookup makes LaunchClassLoader report RenderBlocks as missing.
         */
        ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES){
            @Override
            protected String getCommonSuperClass(String type1,String type2){
                return "java/lang/Object";
            }
        };
        n.accept(w);
        return w.toByteArray();
    }
}
