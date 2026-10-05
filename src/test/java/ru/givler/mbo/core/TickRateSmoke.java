package ru.givler.mbo.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;

public final class TickRateSmoke {
  private TickRateSmoke() { }

  public static void check() throws Exception {
    TickRateClock clock = new TickRateClock(0L);
    if (clock.timeMillis(1000000000L) != 1000L || clock.sleepMillis(50L) != 50L)
      throw new AssertionError("Default server pacing must stay at 20 TPS");
    clock.setRate(200, 1000000000L);
    if (clock.timeMillis(1000000000L) != 1000L
        || clock.timeMillis(1005000000L) != 1050L || clock.sleepMillis(50L) != 5L)
      throw new AssertionError("200 TPS must be ten times faster without a clock jump");
    clock.setRate(1, 1005000000L);
    if (clock.timeMillis(2005000000L) != 1100L || clock.sleepMillis(50L) != 1000L)
      throw new AssertionError("Slow pacing must preserve time continuity");
    clock.setRate(60, 2005000000L);
    if (clock.timeMillis(3005000000L) != 4100L)
      throw new AssertionError("Rates must not be rounded to integer tick durations");
    clock.setRate(1000, 3005000000L);
    if (clock.sleepMillis(50L) != 1L || clock.timeMillis(3006000000L) != 4150L)
      throw new AssertionError("Maximum tick rate must keep a positive sleep");
    for (int invalid : new int[] {0, -1, 1001}) {
      try { clock.setRate(invalid, 3006000000L); throw new AssertionError("Accepted invalid rate"); }
      catch (IllegalArgumentException expected) { }
    }
    TickRateHooks.setClientRate(200);
    if (TickRateHooks.clientTicksPerSecond(20F) != 200F || TickRateHooks.clientTickLimit() != 100)
      throw new AssertionError("Client pacing and catch-up cap must scale together");
    TickRateHooks.setClientRate(20);
    if (TickRateHooks.clientTicksPerSecond(20F) != 20F || TickRateHooks.clientTickLimit() != 10)
      throw new AssertionError("Disconnect reset must restore vanilla client pacing");
    verify("net.minecraft.server.MinecraftServer", "serverTimeMillis", 2);
    verify("net.minecraft.server.MinecraftServer", "sleepServer", 1);
    verify("net.minecraft.util.Timer", "clientTicksPerSecond", 1);
    verify("net.minecraft.util.Timer", "clientTickLimit", 2);
    System.out.println("Tick rate clock continuity, 1/20/60/200/1000 TPS and server/client ASM passed");
  }

  private static void verify(String name, String hook, int expected) throws Exception {
    byte[] transformed = new TickRateTransformer().transform(name, name, bytes(name));
    StringWriter errors = new StringWriter();
    CheckClassAdapter.verify(new ClassReader(transformed), false, new PrintWriter(errors));
    if (errors.getBuffer().length() != 0) throw new AssertionError(name + ": " + errors);
    ClassNode node = new ClassNode();
    new ClassReader(transformed).accept(node, 0);
    int found = 0;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
        if (insn instanceof MethodInsnNode) {
          MethodInsnNode call = (MethodInsnNode) insn;
          if (call.owner.equals("ru/givler/mbo/core/TickRateHooks") && call.name.equals(hook)) ++found;
        }
    if (found != expected) throw new AssertionError("Missing " + name + "." + hook);
  }

  private static byte[] bytes(String name) throws Exception {
    try (InputStream stream = TickRateSmoke.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class")) {
      if (stream == null) throw new AssertionError("Missing " + name);
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int length;
      while ((length = stream.read(buffer)) != -1) result.write(buffer, 0, length);
      return result.toByteArray();
    }
  }
}
