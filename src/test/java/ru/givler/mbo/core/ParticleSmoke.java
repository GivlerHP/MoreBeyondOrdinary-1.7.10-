package ru.givler.mbo.core;

import java.io.*;
import java.util.Arrays;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;
import ru.givler.mbo.config.ParticleConfig;

public final class ParticleSmoke {
  public static void main(String[] args)throws Exception {
    String[] names={"net.minecraft.client.particle.EntityFX","net.minecraft.client.particle.EntityDiggingFX",
        "net.minecraft.client.renderer.Tessellator","net.minecraft.client.particle.EffectRenderer"};
    ParticleTransformer transformer=new ParticleTransformer();
    for(String name:names) {
      ByteArrayOutputStream out=new ByteArrayOutputStream();
      try(InputStream in=ParticleSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
        byte[] buffer=new byte[8192];int count;
        while((count=in.read(buffer))!=-1)out.write(buffer,0,count);
      }
      byte[] original=out.toByteArray(),patched=transformer.transform(name,name,original);
      if(Arrays.equals(original,patched))throw new AssertionError("Missing particle transformation: "+name);
      if(!Arrays.equals(patched,transformer.transform(name,name,patched)))throw new AssertionError("Not idempotent: "+name);
      StringWriter errors=new StringWriter();
      CheckClassAdapter.verify(new ClassReader(patched),false,new PrintWriter(errors));
      if(errors.getBuffer().length()!=0)throw new AssertionError(name+": "+errors);
      if(name.endsWith("EffectRenderer")) {
        ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);
        int render=0,begin=0;
        for(MethodNode m:node.methods)for(AbstractInsnNode ins:m.instructions.toArray())if(ins instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)ins;
          if(call.owner.equals("ru/givler/mbo/client/particle/ParticleRenderHooks")) {
            if(call.name.equals("render"))render++;
            if(call.name.equals("beginPass"))begin++;
          }
        }
        if(render!=2||begin!=2)throw new AssertionError("Normal and lit particle passes: "+render+"/"+begin);
      }
    }
    File folder=new File("build/particle-config-smoke");
    java.lang.reflect.Field home=cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
    home.setAccessible(true);home.set(null,new File("."));
    ParticleConfig.load(folder);
    ParticleConfig.cubicParticles=false;ParticleConfig.rgbLighting=true;ParticleConfig.save();
    ParticleConfig.cubicParticles=true;ParticleConfig.rgbLighting=false;ParticleConfig.load(folder);
    if(ParticleConfig.cubicParticles||!ParticleConfig.rgbLighting)throw new AssertionError("Independent particle settings persistence");
    System.out.println("Particle ASM verification, both render passes, idempotence and independent config persistence passed");
  }
}
