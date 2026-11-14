package ru.givler.mbo.integration.thaumcraft.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** TC4 notes have two icon layers. A custom equipped parchment owns its entire first pass. */
public final class ResearchScrollTransformer implements IClassTransformer,Opcodes {
    private byte[] poseAndOffhand(String target,byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean changed=false;
        String owner="ru/givler/mbo/integration/thaumcraft/core/ResearchScrollHooks";
        for(MethodNode method:node.methods) {
            boolean patched=false;
            for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(owner))patched=true;
            if(patched)continue;
            String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
            if(target.equals("net.minecraft.client.model.ModelBiped") && (mapped.equals("setRotationAngles")||mapped.equals("func_78087_a"))
                    && FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(FFFFFFLnet/minecraft/entity/Entity;)V")) {
                for(AbstractInsnNode insn:method.instructions.toArray())if(insn.getOpcode()==RETURN) {
                    InsnList hook=new InsnList();hook.add(new VarInsnNode(ALOAD,0));hook.add(new VarInsnNode(ALOAD,7));
                    hook.add(new MethodInsnNode(INVOKESTATIC,owner,"pose","(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V",false));
                    method.instructions.insertBefore(insn,hook);changed=true;
                }
            }
            if(target.equals("mods.battlegear2.client.utils.BattlegearRenderHelper") && (method.name.equals("renderItemInFirstPerson")||method.name.equals("renderItemIn3rdPerson"))) {
                InsnList hook=new InsnList();LabelNode normal=new LabelNode();
                if(method.name.equals("renderItemInFirstPerson")) {
                    hook.add(new MethodInsnNode(INVOKESTATIC,owner,"holdingLocal","()Z",false));
                } else {hook.add(new VarInsnNode(ALOAD,0));hook.add(new MethodInsnNode(INVOKESTATIC,owner,"holding","(Lnet/minecraft/entity/Entity;)Z",false));}
                hook.add(new JumpInsnNode(IFEQ,normal));hook.add(new InsnNode(RETURN));
                hook.add(normal);hook.add(new FrameNode(F_SAME,0,null,0,null));method.instructions.insert(hook);changed=true;
            }
        }
        if(!changed)return bytes;
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    public byte[] transform(String name,String target,byte[] bytes) {
        if(bytes!=null && (target.equals("net.minecraft.client.model.ModelBiped") || target.equals("mods.battlegear2.client.utils.BattlegearRenderHelper")))return poseAndOffhand(target,bytes);
        if(bytes==null || !target.equals("net.minecraft.client.renderer.ItemRenderer"))return bytes;
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0,mapChecks=0,mapReads=0,papers=0;
        String owner="ru/givler/mbo/integration/thaumcraft/core/ResearchScrollHooks";
        for(MethodNode method:node.methods) {
            String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
            if((mapped.equals("renderItemInFirstPerson") || mapped.equals("func_78440_a")) && method.desc.equals("(F)V")) {
                int stackLocal=-1;
                for(AbstractInsnNode insn:method.instructions.toArray()) {
                    if(insn instanceof TypeInsnNode) {
                        TypeInsnNode type=(TypeInsnNode)insn;
                        if(FMLDeobfuscatingRemapper.INSTANCE.mapType(type.desc).equals("net/minecraft/item/ItemMap")) {
                            if(type.getOpcode()==INSTANCEOF) {
                                AbstractInsnNode load=type.getPrevious().getPrevious();
                                if(!(load instanceof VarInsnNode))throw new IllegalStateException("Scroll map stack anchor");
                                stackLocal=((VarInsnNode)load).var;
                                method.instructions.set(type,new MethodInsnNode(INVOKESTATIC,owner,"mapItem","(Lnet/minecraft/item/Item;)Z",false));mapChecks++;
                            } else if(type.getOpcode()==CHECKCAST)method.instructions.remove(type);
                        }
                    } else if(insn instanceof MethodInsnNode) {
                        MethodInsnNode call=(MethodInsnNode)insn;
                        if(FMLDeobfuscatingRemapper.INSTANCE.mapType(call.owner).equals("net/minecraft/item/ItemMap") && FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc).equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;)Lnet/minecraft/world/storage/MapData;")) {
                            method.instructions.set(call,new MethodInsnNode(INVOKESTATIC,owner,"mapData","(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;)Lnet/minecraft/world/storage/MapData;",false));mapReads++;
                        }
                    } else if(insn instanceof FieldInsnNode && insn.getOpcode()==GETSTATIC && stackLocal>=0) {
                        FieldInsnNode field=(FieldInsnNode)insn;
                        if(field.owner.equals(node.name) && FMLDeobfuscatingRemapper.INSTANCE.mapDesc(field.desc).equals("Lnet/minecraft/util/ResourceLocation;")) {
                            InsnList paper=new InsnList();paper.add(new VarInsnNode(ALOAD,stackLocal));
                            paper.add(new MethodInsnNode(INVOKESTATIC,owner,"mapPaper","(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/ResourceLocation;",false));
                            method.instructions.insert(insn,paper);papers++;
                        }
                    }
                }
            }
        }
        for(MethodNode method:node.methods)if(method.name.equals("renderItem")
                && FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V")) {
            for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(owner))return bytes;
            LabelNode original=new LabelNode();InsnList code=new InsnList();
            code.add(new VarInsnNode(ALOAD,2));code.add(new VarInsnNode(ILOAD,3));code.add(new VarInsnNode(ALOAD,4));
            code.add(new MethodInsnNode(INVOKESTATIC,owner,"skipOverlay","(Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)Z",false));
            code.add(new JumpInsnNode(IFEQ,original));code.add(new InsnNode(RETURN));code.add(original);code.add(new FrameNode(F_SAME,0,null,0,null));
            method.instructions.insert(code);count++;
        }
        if(mapChecks!=1 || mapReads!=1 || papers!=1)throw new IllegalStateException("Scroll map anchors: "+mapChecks+"/"+mapReads+"/"+papers);
        if(count!=1)throw new IllegalStateException("Research scroll equipped pass anchors: "+count);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
