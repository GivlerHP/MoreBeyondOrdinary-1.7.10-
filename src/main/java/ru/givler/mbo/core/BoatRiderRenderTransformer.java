package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class BoatRiderRenderTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK = "ru/givler/mbo/client/render/LivingEntityRenderHooks";
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    boolean render = "net.minecraft.client.renderer.entity.RendererLivingEntity".equals(transformedName);
    boolean model = "net.minecraft.client.model.ModelBiped".equals(transformedName);
    boolean entity = "net.minecraft.entity.Entity".equals(transformedName);
    boolean world = "net.minecraft.client.renderer.EntityRenderer".equals(transformedName);
    if (bytes == null || !render && !model && !entity && !world) return bytes;
    // setAngles is client-only and is stripped from dedicated-server Entity classes.
    if (entity && cpw.mods.fml.relauncher.FMLLaunchHandler.side() == cpw.mods.fml.relauncher.Side.SERVER)
      return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    int changes = 0;
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if(world) {
        boolean patched=false;
        for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode&&((MethodInsnNode)insn).name.equals("finishWaterMasks"))patched=true;
        if(patched)return bytes;
        for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)insn;
          String callName=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
          if((callName.equals("renderEntities")||callName.equals("func_147589_a"))
              &&FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc).equals("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V")) {
            String mask="ru/givler/mbo/client/render/boat/RenderMBOBoat";
            method.instructions.insertBefore(insn,new MethodInsnNode(INVOKESTATIC,mask,"beginWaterMasks","()V",false));
            method.instructions.insert(insn,new MethodInsnNode(INVOKESTATIC,mask,"finishWaterMasks","()V",false));changes+=2;
          }
        }
      }
      if (entity && ("setAngles".equals(mapped) || "func_70082_c".equals(mapped)) && "(FF)V".equals(method.desc)) {
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn.getOpcode() == RETURN) {
          InsnList hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/entity/boat/MBOBoatRiderView", "clampView", "(Lnet/minecraft/entity/Entity;)V", false));
          method.instructions.insertBefore(insn, hook); changes++;
        }
      }
      if (render && ("doRender".equals(mapped) || "func_76986_a".equals(mapped))
          && "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V".equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc))) {
        int yaw = -1, partial = -1, head = -1, pitch = -1;
        for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)insn;
          String callName=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
          if(call.desc.equals("(FFF)F") && insn.getNext() instanceof VarInsnNode) {
            AbstractInsnNode source=previousCode(previousCode(insn));
            if(source instanceof FieldInsnNode) {
              FieldInsnNode field=(FieldInsnNode)source;
              String fieldName=FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(field.owner,field.name,field.desc);
              if(fieldName.equals("rotationYawHead")||fieldName.equals("field_70759_as"))head=((VarInsnNode)insn.getNext()).var;
            }
          }
          if(callName.equals("renderModel")||callName.equals("func_77036_a")) {
            AbstractInsnNode value=previousCode(previousCode(insn));
            if(value instanceof VarInsnNode&&value.getOpcode()==FLOAD)pitch=((VarInsnNode)value).var;
          }
        }
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn instanceof MethodInsnNode) {
          MethodInsnNode call = (MethodInsnNode) insn;
          String callName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
          if (!(callName.equals("rotateCorpse") || callName.equals("func_77043_a"))) continue;
          AbstractInsnNode p = previousCode(call), y = previousCode(p);
          if (p instanceof VarInsnNode && p.getOpcode() == FLOAD && y instanceof VarInsnNode && y.getOpcode() == FLOAD) {
            yaw = ((VarInsnNode) y).var; partial = ((VarInsnNode) p).var; break;
          }
        }
        boolean bodyStarted = false;
        boolean pitchStarted = false;
        for (AbstractInsnNode insn : method.instructions.toArray()) {
          if(insn instanceof VarInsnNode&&insn.getOpcode()==FSTORE&&((VarInsnNode)insn).var==pitch)pitchStarted=true;
          if(pitchStarted&&insn instanceof VarInsnNode&&insn.getOpcode()==FLOAD) {
            int local=((VarInsnNode)insn).var;
            if(local==head||local==pitch) {
              InsnList hook=new InsnList();hook.add(new VarInsnNode(ALOAD,1));hook.add(new VarInsnNode(FLOAD,local));
              if(local==head){hook.add(new VarInsnNode(FLOAD,yaw));hook.add(new VarInsnNode(FLOAD,partial));}
              hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,local==head?"modelHeadYaw":"modelHeadPitch",local==head?"(Lnet/minecraft/entity/EntityLivingBase;FFF)F":"(Lnet/minecraft/entity/EntityLivingBase;F)F",false));
              method.instructions.insertBefore(insn,hook);method.instructions.remove(insn);changes++;continue;
            }
          }
          if (insn instanceof VarInsnNode && insn.getOpcode() == FSTORE && ((VarInsnNode) insn).var == yaw) {
            AbstractInsnNode previous = previousCode(insn);
            if (previous instanceof MethodInsnNode && ((MethodInsnNode) previous).desc.equals("(FFF)F")) bodyStarted = true;
          }
          if (bodyStarted && insn instanceof VarInsnNode && insn.getOpcode() == FLOAD && ((VarInsnNode) insn).var == yaw) {
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 1));
            hook.add(new VarInsnNode(FLOAD, yaw));
            hook.add(new VarInsnNode(FLOAD, partial));
            hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "bodyYaw", "(Lnet/minecraft/entity/EntityLivingBase;FF)F", false));
            method.instructions.insertBefore(insn, hook); method.instructions.remove(insn); changes++;
          }
        }
      }
      if (model && ("setRotationAngles".equals(mapped) || "func_78087_a".equals(mapped))
          && "(FFFFFFLnet/minecraft/entity/Entity;)V".equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc))) {
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn.getOpcode() == RETURN) {
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new VarInsnNode(ALOAD, 7));
          hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "pose", "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V", false));
          method.instructions.insertBefore(insn, hook); changes++;
        }
      }
    }
    if (changes == 0) throw new IllegalStateException("MBO boat rider render anchors missing: " + transformedName);
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray();
  }
  private static AbstractInsnNode previousCode(AbstractInsnNode insn) {
    if (insn == null) return null;
    do { insn = insn.getPrevious(); } while (insn != null && insn.getOpcode() < 0);
    return insn;
  }
}
