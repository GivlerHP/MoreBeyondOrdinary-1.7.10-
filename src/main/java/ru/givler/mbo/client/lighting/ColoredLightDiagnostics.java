package ru.givler.mbo.client.lighting;

import java.io.*;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.client.render.decormodels.DecorRenderDiagnostics;
import ru.givler.mbo.config.ColoredLightConfig;

/** Timers run only during the existing /mbodecor sample measurement. No per-vertex instrumentation. */
public final class ColoredLightDiagnostics {
  private static long tickNanos,scanNanos,propagationNanos,dynamicNanos,ticks,rebuilds,dynamicUpdates;
  private static long uploadNanos,uploads;
  private ColoredLightDiagnostics() {}
  public static void reset(){tickNanos=scanNanos=propagationNanos=dynamicNanos=ticks=rebuilds=dynamicUpdates=uploadNanos=uploads=0;}
  public static long start(){return DecorRenderDiagnostics.isMeasuring()?System.nanoTime():0;}
  public static void scan(long start){if(start!=0)scanNanos+=System.nanoTime()-start;}
  public static void propagation(long start){if(start!=0)propagationNanos+=System.nanoTime()-start;}
  public static void dynamic(long start){if(start!=0)dynamicNanos+=System.nanoTime()-start;}
  public static void end(long start,int dirty){if(start!=0){tickNanos+=System.nanoTime()-start;ticks++;rebuilds+=dirty;}}
  public static void dynamicUpdate(){if(DecorRenderDiagnostics.isMeasuring())dynamicUpdates++;}
  public static void upload(long start){if(start!=0){uploadNanos+=System.nanoTime()-start;uploads++;}}
  public static String finish(String label,int frames) throws IOException {
    File file=new File(Minecraft.getMinecraft().mcDataDir,"logs/mbo-light-render.csv");file.getParentFile().mkdirs();
    boolean header=!file.isFile();
    double divisor=Math.max(1,ticks)*1e6;
    try(PrintWriter out=new PrintWriter(new FileWriter(file,true))) {
      if(header)out.println("label,rgb,entity_rgb,frames,ticks,rgb_cpu_ms_per_tick,scan_ms_per_tick,propagation_ms_per_tick,dynamic_ms_per_tick,section_rebuild_requests,dynamic_updates,shader_upload_cpu_ms_per_frame,shader_uploads");
      out.printf(Locale.ROOT,"%s,%s,%s,%d,%d,%.6f,%.6f,%.6f,%.6f,%d,%d,%.6f,%d%n",label,ColoredLightConfig.enabled,ColoredLightConfig.entityLighting,frames,ticks,
          tickNanos/divisor,scanNanos/divisor,propagationNanos/divisor,dynamicNanos/divisor,rebuilds,dynamicUpdates,uploadNanos/(Math.max(1,frames)*1e6),uploads);
    }
    return String.format(Locale.ROOT,"RGB CPU/tick %.3f ms: scan %.3f, propagation %.3f, dynamic %.3f; section rebuild requests %d, dynamic updates %d; shader upload CPU/frame %.3f ms (%d uploads). CSV: logs/mbo-light-render.csv",
        tickNanos/divisor,scanNanos/divisor,propagationNanos/divisor,dynamicNanos/divisor,rebuilds,dynamicUpdates,uploadNanos/(Math.max(1,frames)*1e6),uploads);
  }
}
