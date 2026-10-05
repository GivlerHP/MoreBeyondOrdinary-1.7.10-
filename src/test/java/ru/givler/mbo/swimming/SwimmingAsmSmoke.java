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
import org.objectweb.asm.tree.JumpInsnNode;
import ru.givler.mbo.core.WaterloggingRenderTransformer;
import ru.givler.mbo.core.SmoothOpeningTransformer;
import ru.givler.mbo.core.PlatformClippingTransformer;
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
    verifyFogFade();
    verifyWaterSurface();
    verifyWaterloggedSourceRegeneration();
    verifyHalfFilledPane();
    verifyPaneWaterChunkRendering();
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
    verifyWaterTopUnderside();
    verifyDungeonRenderPass();
    ru.givler.mbo.movingplatform.PlatformClippingSmoke.check();
    verifyPlatformClippingAsm();
    verifyPassengerBodyAsm();
    ru.givler.mbo.core.TickRateSmoke.check();
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

  private static void verifyFogFade() {
    ru.givler.mbo.client.handler.DenseFogRenderEvents.Fade fade =
        new ru.givler.mbo.client.handler.DenseFogRenderEvents.Fade();
    float initial = fade.update(true,2,0);
    if (fade.update(false,0,10) != initial
        || Math.abs(fade.update(false,0,1_000_000_010L)-initial*0.5F) > 0.000001F
        || fade.update(false,0,2_000_000_010L) != 0F)
      throw new AssertionError("Fog must fade continuously to zero over two seconds");
    fade.update(true,1,3_000_000_000L);
    fade.update(false,0,3_000_000_001L);
    if (fade.update(true,3,3_500_000_000L) != 0.16F)
      throw new AssertionError("Reapplying fog must cancel its fade and restore the new amplifier");
    fade.reset();
    if (fade.update(false,0,4_000_000_000L) != 0F)
      throw new AssertionError("Fog must reset when changing camera or world");
  }

  private static void verifyPassengerBodyAsm() throws Exception {
    String name="net.minecraft.entity.EntityBodyHelper";
    byte[] bytes=new ru.givler.mbo.core.PlatformPassengerBodyTransformer().transform(name,name,classBytes(name));
    checkClass(name,bytes);
    ClassNode node=new ClassNode(); new ClassReader(bytes).accept(node,0);
    int hooks=0;
    for (MethodNode method:node.methods) for (AbstractInsnNode instruction=method.instructions.getFirst();instruction!=null;instruction=instruction.getNext())
      if (instruction instanceof MethodInsnNode && ((MethodInsnNode)instruction).name.startsWith("animationPrevious")) ++hooks;
    if (hooks!=2) throw new AssertionError("Passenger body rotation must use relative movement on both axes");
  }

  private static void verifyPaneWaterChunkRendering() throws Exception {
    String name = "net.minecraft.client.renderer.WorldRenderer";
    byte[] transformed = new ru.givler.mbo.core.WaterloggedPaneChunkTransformer()
        .transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    int pass = 0, canRender = 0, render = 0;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        if (!call.owner.equals("ru/givler/mbo/client/render/WaterloggedPaneChunkHooks")) continue;
        if (call.name.equals("renderPass")) ++pass;
        if (call.name.equals("canRender")) ++canRender;
        if (call.name.equals("render")) ++render;
      }
    if (pass != 1 || canRender != 1 || render != 2)
      throw new AssertionError("Pane water must enter the same translucent chunk buffer as ordinary water");
    Class<?> renderer = ru.givler.mbo.client.render.WaterloggedBlockRenderer.class;
    java.lang.reflect.Field quadField = renderer.getDeclaredField("CHUNK_QUAD");
    java.lang.reflect.Field indexField = renderer.getDeclaredField("CHUNK_VERTEX");
    quadField.setAccessible(true); indexField.setAccessible(true);
    ThreadLocal<double[]> quad = (ThreadLocal<double[]>) quadField.get(null);
    ThreadLocal<Integer> index = (ThreadLocal<Integer>) indexField.get(null);
    java.lang.reflect.Method vertex = renderer.getDeclaredMethod("addFaceVertex",
        net.minecraft.client.renderer.Tessellator.class, double.class, double.class, double.class, double.class, double.class);
    vertex.setAccessible(true);
    java.lang.reflect.Constructor<net.minecraft.client.renderer.Tessellator> constructor =
        net.minecraft.client.renderer.Tessellator.class.getDeclaredConstructor(int.class);
    constructor.setAccessible(true);
    net.minecraft.client.renderer.Tessellator tessellator = constructor.newInstance(4096);
    tessellator.startDrawingQuads();
    quad.set(new double[20]); index.set(0);
    try {
      for (int i = 0; i < 4; ++i) vertex.invoke(null, tessellator, (double) i, 0D, 0D, 0D, 0D);
      java.lang.reflect.Field count = net.minecraft.client.renderer.Tessellator.class.getDeclaredField("vertexCount");
      count.setAccessible(true);
      if (count.getInt(tessellator) != 8 || index.get() != 0)
        throw new AssertionError("Water quads must remain visible on both sides in the culled chunk pass");
    } finally { quad.remove(); index.remove(); }
    System.out.println("Half-filled pane water joins sorted translucent chunks and emits both face windings");
  }

  private static void verifyWaterloggedSourceRegeneration() throws Exception {
    String name = "net.minecraft.block.BlockDynamicLiquid";
    byte[] transformed = new ru.givler.mbo.core.WaterloggingFlowTransformer()
        .transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    boolean found = false;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)
            || !((MethodInsnNode) insn).name.equals("isWaterloggedFlowSource")) continue;
        found = true;
        for (AbstractInsnNode next = insn.getNext(); next != null && next.getOpcode() != org.objectweb.asm.Opcodes.IRETURN;
            next = next.getNext())
          if (next.getOpcode() == org.objectweb.asm.Opcodes.PUTFIELD)
            throw new AssertionError("Waterlogged supply must not increment the real source counter");
      }
    if (!found) throw new AssertionError("Waterlogged flow supply hook missing");
  }

  private static void verifyHalfFilledPane() {
    for (int wetSide = 0; wetSide < 2; wetSide++) {
      boolean[] wet = new boolean[8];
      for (int y = 0; y < 2; y++)
        for (int z = 0; z < 2; z++)
          wet[ru.givler.mbo.waterlogging.WaterloggedGeometry.index(wetSide, y, z)] = true;
      double[] bounds = ru.givler.mbo.waterlogging.WaterloggedGeometry.paneWaterCellBounds(wet, wetSide, 1, 0);
      if (wetSide == 0 ? bounds[0] != 0D || bounds[1] != 7D / 16D
          : bounds[0] != 9D / 16D || bounds[1] != 1D)
        throw new AssertionError("Half-filled pane water must end at the glass surface");
      if (bounds[2] != 0D || bounds[3] != 0.5D)
        throw new AssertionError("Water must still reach the ordinary-water boundary along the pane");
    }
    boolean[] full = new boolean[8];
    java.util.Arrays.fill(full, true);
    double[] bounds = ru.givler.mbo.waterlogging.WaterloggedGeometry.paneWaterCellBounds(full, 0, 1, 0);
    if (bounds[1] != 0.5D || bounds[3] != 0.5D)
      throw new AssertionError("Fully submerged panes must retain joined water cells");
  }

  private static void verifyWaterSurface() {
    // Vanilla getLiquidHeight returns float; renderBlockLiquid promotes it to
    // double and subtracts a float-derived epsilon exactly once.
    float rawSource = 1.0F - net.minecraft.block.BlockLiquid.getLiquidHeightPercent(0);
    double vanillaSurface = (double) rawSource - (double) 0.001F;
    if (ru.givler.mbo.client.render.WaterloggedLiquidHeightHooks.sourceHeight() != rawSource
        || ru.givler.mbo.client.render.WaterloggedLiquidHeightHooks.sourceSurface() != vanillaSurface)
      throw new AssertionError("Ordinary and waterlogged source surfaces must match exactly");
    try {
      java.lang.reflect.Field surface = ru.givler.mbo.client.render.WaterloggedBlockRenderer.class
          .getDeclaredField("SOURCE_SURFACE");
      surface.setAccessible(true);
      if (surface.getDouble(null) != vanillaSurface)
        throw new AssertionError("Waterlogged renderer must use the vanilla rendered height");
    } catch (ReflectiveOperationException error) {
      throw new AssertionError(error);
    }
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

  private static void verifyWaterTopUnderside() throws Exception {
    String name = "net.minecraft.client.renderer.RenderBlocks";
    byte[] transformed = new WaterloggingRenderTransformer().transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    for (MethodNode method : node.methods) {
      int topVertices = 0;
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        if (call.name.equals("addVertexWithUV")) topVertices++;
        if (!call.name.equals("shouldHideTopUnderside")) continue;
        JumpInsnNode branch = (JumpInsnNode) call.getNext();
        int undersideVertices = 0;
        for (AbstractInsnNode skipped = branch.getNext(); skipped != branch.label; skipped = skipped.getNext()) {
          if (skipped instanceof MethodInsnNode
              && ((MethodInsnNode) skipped).name.equals("addVertexWithUV")) undersideVertices++;
        }
        if (topVertices != 4 || undersideVertices != 4)
          throw new AssertionError("Liquid underside patch must preserve the upper quad and skip only the lower quad");
        return;
      }
    }
    throw new AssertionError("Liquid underside hook missing");
  }

  private static void verifyDungeonRenderPass() throws Exception {
    String name = "net.minecraft.client.renderer.RenderGlobal";
    byte[] transformed = new SmoothOpeningTransformer().transform(name, name, classBytes(name));
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    int hooks = 0;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
        if (insn instanceof MethodInsnNode
            && ((MethodInsnNode) insn).owner.equals("ru/givler/mbo/client/render/SmoothOpeningRenderer")
            && ((MethodInsnNode) insn).name.equals("renderBeforeTranslucent")) hooks++;
    if (hooks != 1) throw new AssertionError("World translucent pass hook missing");
    new ClassReader(classBytes("ru.givler.mbo.client.render.SmoothOpeningRenderer")).accept(node = new ClassNode(), 0);
    int verified = 0;
    for (MethodNode method : node.methods) {
      if (!method.name.equals("renderBeforeTranslucent")
          && !method.name.equals("renderBeforeNeodymiumTranslucent")) continue;
      boolean wallRendered = false;
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        if (call.owner.equals("ru/givler/mbo/client/render/DungeonAreaWorldRenderer")) wallRendered = true;
        if (call.owner.equals("ru/givler/mbo/client/render/WaterloggedBlockRenderer")) {
          if (!wallRendered) throw new AssertionError("Dungeon walls must render before water");
          verified++;
        }
      }
    }
    if (verified != 2) throw new AssertionError("Dungeon render order missing in vanilla or Neodymium");
  }

  private static void verifyPlatformClippingAsm() throws Exception {
    String name = "net.minecraft.client.renderer.RenderBlocks";
    byte[] transformed = new SmoothOpeningTransformer().transform(name, name, classBytes(name));
    transformed = new WaterloggingRenderTransformer().transform(name, name, transformed);
    transformed = new PlatformClippingTransformer().transform(name, name, transformed);
    checkClass(name, transformed);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    for (MethodNode method : node.methods)
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
        if (insn instanceof MethodInsnNode
            && ((MethodInsnNode) insn).owner.equals("ru/givler/mbo/client/render/PlatformClippingHooks")
            && ((MethodInsnNode) insn).name.equals("shouldHideBlock")) return;
    throw new AssertionError("Stationary elevator clipping hook missing");
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
