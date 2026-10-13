package ru.givler.mbo.client.lighting;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.client.render.decormodels.DecorRenderDiagnostics;

/** Actual allocated Java-heap bytes on the measuring thread, not free-heap/GC estimates. */
public final class ColoredLightAllocationDiagnostics {
  public static final int TICK=0,SCAN=1,PROPAGATION=2,DYNAMIC=3,UPLOAD=4,DECOR=5,GPU=6,PLATFORM=7,ENTITY=8;
  private static final String[] NAMES={"rgb_tick","scan","propagation_publish","dynamic_sources","shader_upload","decor_model","gpu_decor","platform_light_field","entity_light_setup"};
  private static final long[] bytes=new long[9], calls=new long[9], peak=new long[9], units=new long[9];
  private static boolean supported;
  private ColoredLightAllocationDiagnostics() {}
  private static final class Meter {
    static final com.sun.management.ThreadMXBean BEAN=create();
    private static com.sun.management.ThreadMXBean create() {
      try {
        java.lang.management.ThreadMXBean candidate=ManagementFactory.getThreadMXBean();
        if(!(candidate instanceof com.sun.management.ThreadMXBean))return null;
        com.sun.management.ThreadMXBean bean=(com.sun.management.ThreadMXBean)candidate;
        if(!bean.isThreadAllocatedMemorySupported())return null;
        if(!bean.isThreadAllocatedMemoryEnabled())bean.setThreadAllocatedMemoryEnabled(true);
        return bean;
      } catch(RuntimeException e){return null;}
    }
  }
  public static void reset(){java.util.Arrays.fill(bytes,0);java.util.Arrays.fill(calls,0);java.util.Arrays.fill(peak,0);java.util.Arrays.fill(units,0);supported=false;}
  public static long start() {
    if(!DecorRenderDiagnostics.isMeasuring())return -1;
    com.sun.management.ThreadMXBean bean=Meter.BEAN;
    if(bean==null)return -1;
    supported=true;return bean.getThreadAllocatedBytes(Thread.currentThread().getId());
  }
  public static void end(int stage,long start) {
    if(start<0)return;
    long difference=Meter.BEAN.getThreadAllocatedBytes(Thread.currentThread().getId())-start;
    if(difference<0)return;
    bytes[stage]+=difference;calls[stage]++;peak[stage]=Math.max(peak[stage],difference);
  }
  public static void work(int stage,long count){if(DecorRenderDiagnostics.isMeasuring())units[stage]+=count;}
  public static String finish(String label,int frames) throws IOException {
    if(!supported)return "RGB heap allocations: JVM allocation counter unavailable or RGB work was disabled.";
    File report=new File(Minecraft.getMinecraft().mcDataDir,"logs/mbo-light-allocations.csv");report.getParentFile().mkdirs();
    boolean header=!report.isFile() || report.length()==0;
    try(PrintWriter out=new PrintWriter(new FileWriter(report,true))) {
      if(header)out.println("label,stage,calls,total_bytes,bytes_per_call,bytes_per_frame,max_bytes_per_call,work_units");
      for(int i=0;i<NAMES.length;i++)out.printf(Locale.ROOT,"%s,%s,%d,%d,%.3f,%.3f,%d,%d%n",label,NAMES[i],calls[i],bytes[i],
          bytes[i]/(double)Math.max(1,calls[i]),bytes[i]/(double)Math.max(1,frames),peak[i],units[i]);
    }
    return String.format(Locale.ROOT,"RGB heap: tick %.1f KiB/tick (scan %.1f, propagation %.1f, dynamic %.1f); decor %.1f KiB/frame, GPU decor %.1f, platform light %.1f, entity light %.1f. CSV: logs/mbo-light-allocations.csv",
        bytes[TICK]/(double)Math.max(1,calls[TICK])/1024,bytes[SCAN]/(double)Math.max(1,calls[SCAN])/1024,
        bytes[PROPAGATION]/(double)Math.max(1,calls[PROPAGATION])/1024,bytes[DYNAMIC]/(double)Math.max(1,calls[DYNAMIC])/1024,
        bytes[DECOR]/(double)Math.max(1,frames)/1024,bytes[GPU]/(double)Math.max(1,frames)/1024,bytes[PLATFORM]/(double)Math.max(1,frames)/1024,bytes[ENTITY]/(double)Math.max(1,frames)/1024);
  }
}
