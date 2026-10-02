package ru.givler.mbo.swimming;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.givler.mbo.core.SwimmingTransformer;
import ru.givler.mbo.core.WaterloggingCameraTransformer;
import ru.givler.mbo.core.WaterloggingCameraHooks;

/** Checks that all four 1.7.10 entry points remain reachable by the coremod. */
public final class SwimmingAsmSmoke {
  private SwimmingAsmSmoke() {}

  public static void main(String[] args) throws Exception {
    if (SwimmingHooks.eyeHeight(1.62F, null) != 1.62F)
      throw new AssertionError("Player size accessor failed");
    verifySwimSpeeds();
    verifyWaterSurface();
    verify("net.minecraft.entity.player.EntityPlayer", "eyeHeight", 1);
    verify("net.minecraft.entity.player.EntityPlayer", "canTriggerWalking", 1);
    verify("net.minecraft.entity.EntityLivingBase", "swimMoveFlying", 1);
    verify("net.minecraft.entity.EntityLivingBase", "waterDrag", 2);
    verify("net.minecraft.entity.EntityLivingBase", "swimGravity", 1);
    verify("net.minecraft.client.entity.EntityPlayerSP", "collisionStopsSprint", 1);
    verify("net.minecraft.client.model.ModelBiped", "animate", 1);
    verify("net.minecraft.client.renderer.entity.RenderPlayer", "rotate", 1);
    verify("net.minecraft.client.renderer.EntityRenderer", "cameraEyeHeight", 1);
    verify("net.minecraft.client.renderer.EntityRenderer", "skipViewBobbing", 1);
    verifyFogCameraOrder();
    verifyUnderwaterOverlay();
    verifyUnderwaterSky();
    System.out.println("Swimming ASM hooks passed");
  }

  private static void verifySwimSpeeds() {
    // 1.13.2: sprint water acceleration 0.02, X/Z drag 0.9,
    // vertical drag 0.8, gravity 0.005, pitch response 0.06 / 0.085.
    double forward = 0.0D, up = 0.0D, down = 0.0D;
    for (int tick = 0; tick < 200; tick++) {
      forward = (forward + 0.02D * 0.98D) * 0.9D;
      up = SwimmingHooks.swimPitchVelocity(up, 1.0D) * 0.8D - 0.005D;
      down = SwimmingHooks.swimPitchVelocity(down, -1.0D) * 0.8D - 0.005D;
    }
    assertClose("forward", forward, 0.1764D);
    assertClose("up", up, 0.173387096774D);
    assertClose("down", down, -0.272388059701D);
  }

  private static void verifyWaterSurface() {
    if (!WaterloggingCameraHooks.isBelowWaterSurface(64.88D, 64, 0, false)
        || WaterloggingCameraHooks.isBelowWaterSurface(64.90D, 64, 0, false)
        || !WaterloggingCameraHooks.isBelowWaterSurface(64.90D, 64, 0, true))
      throw new AssertionError("Camera water boundary must follow the rendered surface");
  }

  private static void assertClose(String axis, double actual, double expected) {
    if (Math.abs(actual - expected) > 0.00001D)
      throw new AssertionError(axis + " swim speed: " + actual + " != " + expected);
  }

  private static void verify(String name, String hook, int minimum) throws Exception {
    byte[] transformed = new SwimmingTransformer().transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    int found = 0;
    for (MethodNode method : node.methods) {
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (instruction instanceof MethodInsnNode
            && hook.equals(((MethodInsnNode) instruction).name)) found++;
      }
    }
    if (found < minimum) throw new AssertionError(name + " missing " + hook);
  }

  private static byte[] classBytes(String name) throws Exception {
    String resource = name.replace('.', '/') + ".class";
    InputStream stream = SwimmingAsmSmoke.class.getClassLoader().getResourceAsStream(resource);
    if (stream == null) throw new AssertionError("Missing " + resource);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int read;
    while ((read = stream.read(buffer)) >= 0) bytes.write(buffer, 0, read);
    stream.close();
    return bytes.toByteArray();
  }

  private static void checkClass(String name, byte[] transformed) {
    StringWriter errors = new StringWriter();
    CheckClassAdapter.verify(new ClassReader(transformed), false, new PrintWriter(errors));
    if (errors.getBuffer().length() != 0) throw new AssertionError(name + ": " + errors);
  }

  private static void verifyFogCameraOrder() throws Exception {
    String name = "net.minecraft.client.renderer.EntityRenderer";
    byte[] transformed = new WaterloggingCameraTransformer().transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    for (MethodNode method : node.methods) {
      if (!"renderWorld".equals(method.name) && !"func_78471_a".equals(method.name)) continue;
      int camera = -1, capture = -1, fog = -1, clear = -1, index = 0;
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (instruction instanceof MethodInsnNode) {
          String call = ((MethodInsnNode) instruction).name;
          if ("updateRenderInfo".equals(call) || "func_74583_a".equals(call)) camera = index;
          if ("captureCamera".equals(call)) capture = index;
          if ("updateFogColor".equals(call) || "func_78466_h".equals(call)) fog = index;
          if ("glClear".equals(call) && clear < 0) clear = index;
        }
        index++;
      }
      if (!(camera >= 0 && capture > camera && fog > capture && clear > fog))
        throw new AssertionError("Fog must follow captured camera and precede clear");
      return;
    }
    throw new AssertionError("Missing renderWorld");
  }

  private static void verifyUnderwaterOverlay() throws Exception {
    String name = "net.minecraft.client.renderer.ItemRenderer";
    byte[] transformed = new WaterloggingCameraTransformer().transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    int modernAlpha = 0;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        if (!"glColor4f".equals(call.name)) continue;
        AbstractInsnNode alpha = instruction.getPrevious();
        while (alpha != null && alpha.getOpcode() < 0) alpha = alpha.getPrevious();
        if (alpha instanceof LdcInsnNode
            && Float.valueOf(0.1F).equals(((LdcInsnNode) alpha).cst)) modernAlpha++;
      }
    if (modernAlpha != 1) throw new AssertionError("Modern underwater overlay alpha missing");
  }

  private static void verifyUnderwaterSky() throws Exception {
    String name = "net.minecraft.client.renderer.RenderGlobal";
    byte[] transformed = new WaterloggingCameraTransformer().transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    int sky = 0, horizons = 0;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
          instruction = instruction.getNext())
        if (instruction instanceof MethodInsnNode) {
          String call = ((MethodInsnNode) instruction).name;
          if ("shouldRenderSky".equals(call)) sky++;
          if ("swimmingSkyHorizon".equals(call)) horizons++;
        }
    if (sky != 1 || horizons != 1)
      throw new AssertionError("Water surface sky hooks missing");
  }
}
