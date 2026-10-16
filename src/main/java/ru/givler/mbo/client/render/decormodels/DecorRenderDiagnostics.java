package ru.givler.mbo.client.render.decormodels;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/** Opt-in measurements of real scenes. No world edits and no per-object timers when idle. */
public final class DecorRenderDiagnostics extends CommandBase {
    public static final DecorRenderDiagnostics INSTANCE=new DecorRenderDiagnostics();
    private String label;
    private long[] frames;
    private int index;
    private long frameStart,previousStart,interval,decorNanos;
    private long totalDecor,totalModels,totalCached,totalDynamic;
    private int models,cached,dynamic;
    private long gpuNanos,totalGpu,gpuVisible,gpuDraws,totalGpuVisible,totalGpuDraws;
    private long gpuAllocationStart=-1;
    private DecorRenderDiagnostics() {}
    public static boolean isMeasuring(){return INSTANCE.frames!=null;}

    public static long beginModel() { return INSTANCE.frames==null?0:System.nanoTime(); }
    public static long beginGpu() {INSTANCE.gpuAllocationStart=ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.start();return INSTANCE.frames==null?0:System.nanoTime();}
    public static void endGpu(long start,int visible,int draws) {
        ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.end(ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.GPU,INSTANCE.gpuAllocationStart);
        if(start==0)return;INSTANCE.gpuNanos+=System.nanoTime()-start;INSTANCE.gpuVisible+=visible;INSTANCE.gpuDraws+=draws;
    }
    public static void endModel(long start,boolean usedCache) {
        if(start==0)return;
        INSTANCE.decorNanos+=System.nanoTime()-start;INSTANCE.models++;
        if(usedCache)INSTANCE.cached++;else INSTANCE.dynamic++;
    }

    private static String[] mf2Diagnostics(String[] args) {
        if(!cpw.mods.fml.common.Loader.isModLoaded("minefantasy2"))return new String[0];
        try { return (String[])Class.forName("minefantasy.mf2.client.render.block.DecorRenderDiagnostics").getMethod("execute",String[].class).invoke(null,(Object)args); }
        catch(ClassNotFoundException | NoSuchMethodException oldVersion){return new String[0];}
        catch(ReflectiveOperationException error){return new String[]{"MF2 diagnostics failed: "+error.getCause()};}
    }
    @Override public String getCommandName() { return "mbodecor"; }
    @Override public String getCommandUsage(ICommandSender sender) {
        return "/mbodecor stats | report | inspect | gpu on/off | cache on/off | chunk on/off | sample <label> [frames]";
    }
    @Override public int getRequiredPermissionLevel() { return 0; }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public void processCommand(ICommandSender sender,String[] args) {
        if(frames!=null && args.length>0 && ("cache".equals(args[0]) || "chunk".equals(args[0]) || "gpu".equals(args[0]) || "sample".equals(args[0]))) { reply("Wait for the active sample to finish.");return; }
        if(args.length==1 && "inspect".equals(args[0])) { inspectTarget();return; }
        String[] mf2=mf2Diagnostics(args);
        for(String line:mf2)reply(line);
        if("gpu".equals(args.length==0?"":args[0]) && args.length==2 && ("on".equals(args[1])||"off".equals(args[1]))) {
            if(frames!=null){reply("Wait for the active sample to finish.");return;}
            reply(InstancedDecorRenderer.INSTANCE.setEnabled("on".equals(args[1])));return;
        }
        if(args.length==1 && "report".equals(args[0])) { reply(ChunkDecorRenderer.INSTANCE.writeReport());return; }
        if(args.length==0 || "stats".equals(args[0])) {
            reply(StaticDecorRenderCache.INSTANCE.statistics());
            reply(InstancedDecorRenderer.INSTANCE.statistics());
            reply(ChunkDecorRenderer.INSTANCE.statistics()); return;
        }
        if("chunk".equals(args[0]) && args.length==2 && ("on".equals(args[1])||"off".equals(args[1]))) {
            if(frames!=null) { reply("Wait for the active sample to finish.");return; }
            ChunkDecorRenderer.INSTANCE.setEnabled("on".equals(args[1]));
            reply(ChunkDecorRenderer.INSTANCE.statistics()); return;
        }
        if("cache".equals(args[0]) && args.length==2 && ("on".equals(args[1])||"off".equals(args[1]))) {
            if(frames!=null) { reply("Wait for the active sample to finish.");return; }
            StaticDecorRenderCache.INSTANCE.setEnabled("on".equals(args[1]));
            reply(StaticDecorRenderCache.INSTANCE.statistics());return;
        }
        if("sample".equals(args[0]) && args.length>=2 && args.length<=3) {
            if(frames!=null) { reply("A sample is already running.");return; }
            int count=args.length==3?parseIntBounded(sender,args[2],60,10000):300;
            label=args[1].replaceAll("[^a-zA-Z0-9_.-]","_");
            ru.givler.mbo.client.lighting.ColoredLightDiagnostics.reset();
            ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.reset();
            frames=new long[count];index=0;previousStart=0;
            totalDecor=totalModels=totalCached=totalDynamic=0;
            totalGpu=totalGpuVisible=totalGpuDraws=0;
            reply("Measuring "+label+" for "+count+" frames. Keep the camera still; close chat.");return;
        }
        reply(getCommandUsage(sender));
    }

    private static void inspectTarget() {
        Minecraft mc=Minecraft.getMinecraft();
        net.minecraft.util.MovingObjectPosition hit=mc.objectMouseOver;
        if(mc.theWorld==null || hit==null || hit.typeOfHit!=net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
            reply("Look at a block.");return;
        }
        int x=hit.blockX,y=hit.blockY,z=hit.blockZ;
        net.minecraft.block.Block block=mc.theWorld.getBlock(x,y,z);
        int meta=mc.theWorld.getBlockMetadata(x,y,z);
        Object id=net.minecraft.block.Block.blockRegistry.getNameForObject(block);
        net.minecraft.tileentity.TileEntity tile=mc.theWorld.getTileEntity(x,y,z);
        boolean declared=block.hasTileEntity(meta);
        reply("Block: "+id+" meta="+meta+" at "+x+", "+y+", "+z+" (client world)");
        reply("Storage: "+(tile!=null ? "TileEntity present" : declared ? "TileEntity expected, absent on client" : "ordinary block (no TileEntity)")
            +"; hasTileEntity="+declared);
        reply("Block class: "+block.getClass().getName()+"; renderType="+block.getRenderType());
        if(tile!=null) reply("TileEntity: "+tile.getClass().getName()+"; canUpdate="+tile.canUpdate());
        if(block instanceof ru.givler.mbo.block.BlockModels) {
            reply(ChunkDecorRenderer.INSTANCE.inspect((ru.givler.mbo.block.BlockModels)block,x,y,z,meta));
        } else if(id!=null && id.toString().startsWith("minefantasy2:")) {
            for(String line:mf2Diagnostics(new String[] {"inspect"})) reply(line);
        }
    }

    @SubscribeEvent public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(frames==null)return;
        if(event.phase==TickEvent.Phase.START) {
            frameStart=System.nanoTime();interval=previousStart==0?0:frameStart-previousStart;
            previousStart=frameStart;decorNanos=0;models=cached=dynamic=0;
            gpuNanos=gpuVisible=gpuDraws=0;
        } else if(interval>0) {
            // Frame start intervals include normal pacing; model time measures CPU submission.
            frames[index++]=interval;totalDecor+=decorNanos;totalModels+=models;
            totalCached+=cached;totalDynamic+=dynamic;
            totalGpu+=gpuNanos;totalGpuVisible+=gpuVisible;totalGpuDraws+=gpuDraws;
            if(index==frames.length)finish();
        }
    }

    private void finish() {
        long sum=0;for(long value:frames)sum+=value;
        long[] ordered=frames.clone();Arrays.sort(ordered);
        double average=sum/(double)frames.length/1e6;
        double p95=ordered[(int)Math.ceil(ordered.length*.95)-1]/1e6;
        double modelTime=totalDecor/(double)frames.length/1e6;
        // Keep the existing CSV schema; record chunk mode in the label for comparable samples.
        String row=String.format(Locale.ROOT,"%s,%s,%d,%.6f,%.6f,%.6f,%.3f,%.3f,%.3f%n",label+(ru.givler.mbo.block.BlockModels.decorInstancedRendering?"_gpu":ru.givler.mbo.block.BlockModels.decorChunkRendering?"_chunk":"_tesr"),
            StaticDecorRenderCache.INSTANCE.isEnabled(),frames.length,average,p95,modelTime,
            totalModels/(double)frames.length,totalCached/(double)frames.length,totalDynamic/(double)frames.length);
        File report=new File(Minecraft.getMinecraft().mcDataDir,"logs/mbo-decor-render.csv");
        try {
            File parent=report.getParentFile();if(!parent.isDirectory()&&!parent.mkdirs())throw new IOException("Cannot create "+parent);
            boolean header=!report.isFile()||report.length()==0;
            try(FileWriter writer=new FileWriter(report,true)) {
                if(header)writer.write("label,cache,frames,frame_ms,p95_frame_ms,decor_cpu_ms,models_per_frame,cached_per_frame,dynamic_per_frame\n");
                writer.write(row);
            }
            File gpuReport=new File(parent,"mbo-decor-gpu-render.csv");
            boolean gpuHeader=!gpuReport.isFile()||gpuReport.length()==0;
            try(FileWriter writer=new FileWriter(gpuReport,true)) {
                if(gpuHeader)writer.write("label,gpu,frames,frame_ms,p95_frame_ms,gpu_cpu_ms,visible_per_frame,draws_per_frame\n");
                writer.write(String.format(Locale.ROOT,"%s,%s,%d,%.6f,%.6f,%.6f,%.3f,%.3f%n",
                    label,ru.givler.mbo.block.BlockModels.decorInstancedRendering,frames.length,average,p95,
                    totalGpu/(double)frames.length/1e6,totalGpuVisible/(double)frames.length,totalGpuDraws/(double)frames.length));
            }
            reply(String.format(Locale.ROOT,"%s: frame %.3f ms (%.1f FPS), p95 %.3f ms; decor CPU %.3f ms, %.1f models/frame. CSV: logs/mbo-decor-render.csv",
                label,average,1000/average,p95,modelTime,totalModels/(double)frames.length));
            reply(ru.givler.mbo.client.lighting.ColoredLightDiagnostics.finish(label,frames.length));
            reply(ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.finish(label,frames.length));
            if(ru.givler.mbo.block.BlockModels.decorInstancedRendering)reply(String.format(Locale.ROOT,
                "GPU instances: CPU %.3f ms, %.1f visible models/frame, %.1f draws/frame",
                totalGpu/(double)frames.length/1e6,totalGpuVisible/(double)frames.length,totalGpuDraws/(double)frames.length));
        } catch(IOException error) { reply("Cannot write benchmark report: "+error.getMessage()); }
        frames=null;
    }
    private static void reply(String message) {
        if(Minecraft.getMinecraft().thePlayer!=null)
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[MBO decor] "+message));
    }
}
