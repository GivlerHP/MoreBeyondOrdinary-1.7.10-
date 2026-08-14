package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.Loader;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.integration.carpentersblocks.client.render.CarpenterOpeningBridge;
import ru.givler.mbo.integration.thaumcraft.client.render.ThaumcraftOpeningBridge;

import java.lang.reflect.Method;
import java.util.*;

/** Rotates vertices produced by Minecraft's own renderer, retaining native models and UVs. */
public final class SmoothOpeningRenderer {
    private static final int DOOR=0, TRAPDOOR=1, GATE=2;
    private static boolean thaumcraftLoaded, carpenters;
    private static final long DURATION=400L;
    private static final long RESTORE_GRACE=150L;
    private static final Map<Key,State> STATES=new HashMap<Key,State>();
    private static final ThreadLocal<Context> ACTIVE=new ThreadLocal<Context>();
    private static final ThreadLocal<Integer> DEPTH=new ThreadLocal<Integer>(){protected Integer initialValue(){return 0;}};
    private static final ThreadLocal<double[]> RESULT=new ThreadLocal<double[]>(){protected double[] initialValue(){return new double[3];}};
    private static final ThreadLocal<float[]> COLOR_RESULT=new ThreadLocal<float[]>(){protected float[] initialValue(){return new float[3];}};
    private static final ThreadLocal<Boolean> SPLITTING_CARPENTER_CUBOID=new ThreadLocal<Boolean>();
    private static Method carpenterRenderCuboid;

    public static void configureIntegrations(){
        thaumcraftLoaded=Loader.isModLoaded("Thaumcraft");
        carpenters=Loader.isModLoaded("CarpentersBlocks");
    }

    public static boolean begin(RenderBlocks renderer,Block block,int x,int y,int z){
        DEPTH.set(DEPTH.get()+1);
        IBlockAccess access=renderer.blockAccess; if(access==null) return false;
        Snapshot snapshot=snapshot(access,block,x,y,z); if(snapshot==null)return false;
        int baseY=snapshot.baseY;
        if(access.getBlock(x,baseY,z)!=block) return false;
        int meta=snapshot.meta;
        boolean open=(meta&4)!=0; long now=System.currentTimeMillis(); Key key=new Key(x,baseY,z);
        State state=STATES.get(key);
        if(state==null||state.block!=block){STATES.put(key,new State(block,x,baseY,z,meta,open,now,snapshot.kind));return false;}
        state.seen=now;
        if(state.open!=open) state.begin(open,meta,now); else state.meta=meta;
        if(state.active(now)) ACTIVE.set(new Context(state,renderer,now,
                isCarpenterBlock(block)&&!(access instanceof World)));
        // Chunk display lists must omit a moving block. The World-backed
        // renderer below draws it every frame instead.
        return state.active(now) && !(access instanceof World) && !isCarpenterBlock(block);
    }

    public static void end(){
        int depth=DEPTH.get()-1;
        if(depth<=0){DEPTH.remove();ACTIVE.remove();}
        else DEPTH.set(depth);
    }

    public static double[] transform(double x,double y,double z){
        double[] out=RESULT.get(); out[0]=x;out[1]=y;out[2]=z;
        Context c=ACTIVE.get(); if(c==null) return out;
        // Keep vertices in the chunk buffer (Carpenter's renderer requires
        // that), but move its static copy out of view during the transition.
        if(c.hideChunkCopy){out[1]-=4096D;return out;}
        State s=c.state; float shown=s.shownAngle(c.now); float target=s.open?90F:0F;
        float delta=shown-target;
        if(Math.abs(delta)<.01F) return out;
        if(s.kind==DOOR) rotateDoor(out,s,delta);
        else if(s.kind==TRAPDOOR) rotateTrapdoor(out,s,delta);
        else rotateGate(out,c,delta);
        return out;
    }

    public static float[] correctColor(float red,float green,float blue){
        float[] out=COLOR_RESULT.get();out[0]=red;out[1]=green;out[2]=blue;
        Context context=ACTIVE.get();
        if(context==null||context.state.kind!=TRAPDOOR)return out;
        float max=Math.max(red,Math.max(green,blue));
        if(max>0F&&max<0.8F){float scale=0.8F/max;out[0]=Math.min(1F,red*scale);out[1]=Math.min(1F,green*scale);out[2]=Math.min(1F,blue*scale);}
        return out;
    }

    private static void rotateDoor(double[] p,State s,float delta){
        int dir=s.meta&3; boolean right=(s.meta&16)!=0; double px,pz;
        final double h=0.09375D, far=1D-h;
        switch(dir){
            case 0:px=s.x+h;pz=s.z+(right?far:h);break;
            case 1:px=s.x+(right?h:far);pz=s.z+h;break;
            case 2:px=s.x+far;pz=s.z+(right?h:far);break;
            default:px=s.x+(right?far:h);pz=s.z+far;
        }
        // Vanilla's four closed directions all use the same hinge rule:
        // left hinge opens -90 degrees, right hinge opens +90 degrees.
        rotateY(p,px,pz,delta*(right?1:-1));
    }

    private static void rotateTrapdoor(double[] p,State s,float delta){
        int dir=s.meta&3;
        boolean top=(s.meta&8)!=0;
        double hingeY=s.y+(top?1D:0D);
        float side=top?-1F:1F;
        if(dir==0) rotateX(p,hingeY,s.z+1,delta*side);
        else if(dir==1) rotateX(p,hingeY,s.z,-delta*side);
        else if(dir==2) rotateZ(p,s.x+1,hingeY,-delta*side);
        else rotateZ(p,s.x,hingeY,delta*side);
    }

    private static void rotateGate(double[] p,Context context,float delta){
        State s=context.state;
        RenderBlocks renderer=context.renderer;
        int dir=s.meta&3; boolean alongX=(dir&1)==0; double px,pz; boolean second;
        boolean carpenterGate=isCarpenterGate(s.block);
        if(carpenterGate){
            // rotateBounds() has already converted Carpenter's local bounds
            // to world orientation before vertices reach the Tessellator.
            if(isCarpenterFixedGatePost(renderer,alongX))return;
            second=alongX
                    ?(renderer.renderMinX+renderer.renderMaxX)*.5D>.5D
                    :(renderer.renderMinZ+renderer.renderMaxZ)*.5D>.5D;
        }else{
            if(isFixedGatePost(renderer,alongX))return;
            second=alongX
                    ?(renderer.renderMinX+renderer.renderMaxX)*.5D>.5D
                    :(renderer.renderMinZ+renderer.renderMaxZ)*.5D>.5D;
        }
        // Select a leaf once from the cuboid bounds, not independently for
        // every vertex. Carpenter's rotates its cuboids before they reach the
        // tessellator, so vertex-based selection tears one rail into pieces.
        if(alongX){
            px=s.x+(second?.875D:.125D);pz=s.z+.5;
        }else{
            px=s.x+.5;pz=s.z+(second?.875D:.125D);
        }
        // Vanilla gate metadata: 0=south, 1=west, 2=north, 3=east.
        // For north/south the opened bounds are mirrored relative to the
        // closed X-aligned rails; west/east use the opposite pair.
        int directionSign=dir<2?-1:1;
        rotateY(p,px,pz,delta*directionSign*(second?1:-1));
    }

    private static boolean isFixedGatePost(RenderBlocks renderer,boolean alongX){
        // Vanilla (and subclasses used by MBO/BoP) has tall stationary posts;
        // its moving rails are shorter.  A footprint test also matches the
        // already-open rails and makes opening appear instantaneous.
        return renderer.renderMaxY-renderer.renderMinY>.65D;
    }

    private static boolean isCarpenterFixedGatePost(RenderBlocks renderer,boolean alongX){
        double axisMin=alongX?renderer.renderMinX:renderer.renderMinZ;
        double axisMax=alongX?renderer.renderMaxX:renderer.renderMaxZ;
        double crossMin=alongX?renderer.renderMinZ:renderer.renderMinX;
        double crossMax=alongX?renderer.renderMaxZ:renderer.renderMaxX;
        double axisCenter=(axisMin+axisMax)*.5D;
        double crossCenter=(crossMin+crossMax)*.5D;
        double axisWidth=axisMax-axisMin;
        double crossWidth=crossMax-crossMin;
        // The permanent hinge posts used by Carpenter's renderer are the
        // narrow, centred-X cuboids at the two local Z edges.  Checking their
        // footprint instead of height keeps tall pickets and wall panels in
        // the animated leaf.
        return Math.abs(crossCenter-.5D)<.02D&&axisWidth<=.14D&&crossWidth<=.14D
                &&(axisCenter<=.1D||axisCenter>=.9D);
    }

    /** Splits a closed Carpenter gate cuboid at local Z=0.5 before vertices are built. */
    public static boolean splitCarpenterGateCuboid(Object handler,net.minecraft.item.ItemStack cover,
            int x,int y,int z,double minX,double minY,double minZ,double maxX,double maxY,double maxZ,
            net.minecraftforge.common.util.ForgeDirection[] rotations){
        Context context=ACTIVE.get();
        if(context==null||!isCarpenterGate(context.state.block)||context.state.open
                ||!context.state.active(context.now)||Boolean.TRUE.equals(SPLITTING_CARPENTER_CUBOID.get())
                ||minZ>=.5D||maxZ<=.5D)return false;
        try{
            if(carpenterRenderCuboid==null){
                carpenterRenderCuboid=handler.getClass().getSuperclass().getDeclaredMethod("renderBlockWithRotation",
                        net.minecraft.item.ItemStack.class,int.class,int.class,int.class,
                        double.class,double.class,double.class,double.class,double.class,double.class,
                        net.minecraftforge.common.util.ForgeDirection[].class);
                carpenterRenderCuboid.setAccessible(true);
            }
            SPLITTING_CARPENTER_CUBOID.set(Boolean.TRUE);
            carpenterRenderCuboid.invoke(handler,cover,x,y,z,minX,minY,minZ,maxX,maxY,.5D,rotations);
            carpenterRenderCuboid.invoke(handler,cover,x,y,z,minX,minY,.5D,maxX,maxY,maxZ,rotations);
            return true;
        }catch(Exception ignored){
            return false;
        }finally{
            SPLITTING_CARPENTER_CUBOID.remove();
        }
    }

    private static void rotateY(double[] p,double px,double pz,float degrees){double a=Math.toRadians(degrees),c=Math.cos(a),s=Math.sin(a),x=p[0]-px,z=p[2]-pz;p[0]=px+x*c-z*s;p[2]=pz+x*s+z*c;}
    private static void rotateX(double[] p,double py,double pz,float degrees){double a=Math.toRadians(degrees),c=Math.cos(a),s=Math.sin(a),y=p[1]-py,z=p[2]-pz;p[1]=py+y*c-z*s;p[2]=pz+y*s+z*c;}
    private static void rotateZ(double[] p,double px,double py,float degrees){double a=Math.toRadians(degrees),c=Math.cos(a),s=Math.sin(a),x=p[0]-px,y=p[1]-py;p[0]=px+x*c-y*s;p[1]=py+x*s+y*c;}

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return; Minecraft mc=Minecraft.getMinecraft(); World w=mc.theWorld;if(w==null){STATES.clear();return;} long now=System.currentTimeMillis();
        Iterator<State> it=STATES.values().iterator();while(it.hasNext()){State s=it.next();if(w.getBlock(s.x,s.y,s.z)!=s.block||now-s.seen>30000){it.remove();continue;}if(!s.active(now)&&s.wasActive){s.wasActive=false;w.markBlockRangeForRenderUpdate(s.x,s.y,s.z,s.x,s.y+(s.kind==DOOR?1:0),s.z);}}
    }

    @SubscribeEvent public void render(RenderWorldLastEvent event){
        Minecraft mc=Minecraft.getMinecraft(); World world=mc.theWorld;
        if(world==null||mc.renderViewEntity==null)return;
        long now=System.currentTimeMillis();
        double cx=mc.renderViewEntity.lastTickPosX+(mc.renderViewEntity.posX-mc.renderViewEntity.lastTickPosX)*event.partialTicks;
        double cy=mc.renderViewEntity.lastTickPosY+(mc.renderViewEntity.posY-mc.renderViewEntity.lastTickPosY)*event.partialTicks;
        double cz=mc.renderViewEntity.lastTickPosZ+(mc.renderViewEntity.posZ-mc.renderViewEntity.lastTickPosZ)*event.partialTicks;
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        RenderBlocks renderer=new RenderBlocks(world); renderer.renderAllFaces=true;
        mc.entityRenderer.enableLightmap(event.partialTicks);
        int previousPass=MinecraftForgeClient.getRenderPass();
        GL11.glPushMatrix(); GL11.glTranslated(-cx,-cy,-cz);
        try{
            for(State state:STATES.values()){
                if(!state.visibleDuringRestore(now))continue;
                if(isCarpenterBlock(state.block)){
                    ForgeHooksClient.setRenderPass(0);
                    renderDynamicState(renderer,state);
                    ForgeHooksClient.setRenderPass(1);
                    renderDynamicState(renderer,state);
                }else renderDynamicState(renderer,state);
            }
        }finally{ForgeHooksClient.setRenderPass(previousPass);ACTIVE.remove();DEPTH.remove();GL11.glPopMatrix();GL11.glColor4f(1,1,1,1);mc.entityRenderer.disableLightmap(event.partialTicks);}
    }

    private static void renderDynamicState(RenderBlocks renderer,State state){
        Tessellator.instance.startDrawingQuads();
        renderer.renderBlockByRenderType(state.block,state.x,state.y,state.z);
        if(state.kind==DOOR)renderer.renderBlockByRenderType(state.block,state.x,state.y+1,state.z);
        Tessellator.instance.draw();
    }

    private static final class Context{final State state;final RenderBlocks renderer;final long now;final boolean hideChunkCopy;Context(State s,RenderBlocks r,long n,boolean h){state=s;renderer=r;now=n;hideChunkCopy=h;}}
    private static final class Key{final int x,y,z;Key(int x,int y,int z){this.x=x;this.y=y;this.z=z;}public int hashCode(){return x*7340033^y^z*19349663;}public boolean equals(Object o){return o instanceof Key&&((Key)o).x==x&&((Key)o).y==y&&((Key)o).z==z;}}
    private static Snapshot snapshot(IBlockAccess access,Block block,int x,int y,int z){
        int raw=access.getBlockMetadata(x,y,z);
        if(block instanceof BlockDoor){int by=(raw&8)!=0?y-1:y;return new Snapshot(DOOR,by,((BlockDoor)block).func_150012_g(access,x,by,z));}
        if(block instanceof BlockTrapDoor)return new Snapshot(TRAPDOOR,y,raw);
        if(block instanceof BlockFenceGate)return new Snapshot(GATE,y,raw);
        if(thaumcraftLoaded&&ThaumcraftOpeningBridge.isArcaneDoor(block)){
            int by=(raw&8)!=0?y-1:y;
            return new Snapshot(DOOR,by,ThaumcraftOpeningBridge.fullDoorMetadata(block,access,x,by,z));
        }
        if(carpenters)return carpenterSnapshot(access,block,x,y,z);
        return null;
    }

    private static Snapshot carpenterSnapshot(IBlockAccess access,Block block,int x,int y,int z){
        if(CarpenterOpeningBridge.isDoor(block)){
            int[] data=CarpenterOpeningBridge.door(access,x,y,z);
            return data==null?null:new Snapshot(DOOR,data[0],data[1]);
        }
        if(CarpenterOpeningBridge.isHatch(block)){
            Integer meta=CarpenterOpeningBridge.hatch(access,x,y,z);
            return meta==null?null:new Snapshot(TRAPDOOR,y,meta);
        }
        if(CarpenterOpeningBridge.isGate(block)){
            Integer meta=CarpenterOpeningBridge.gate(access,x,y,z);
            return meta==null?null:new Snapshot(GATE,y,meta);
        }
        return null;
    }

    private static boolean isCarpenterBlock(Block block){
        return carpenters&&CarpenterOpeningBridge.isAny(block);
    }

    private static boolean isCarpenterGate(Block block){
        return carpenters&&CarpenterOpeningBridge.isGate(block);
    }

    private static final class Snapshot{final int kind,baseY,meta;Snapshot(int k,int y,int m){kind=k;baseY=y;meta=m;}}
    private static final class State{final Block block;final int x,y,z,kind;int meta;boolean open;long start,seen;float from,to;boolean wasActive;State(Block b,int x,int y,int z,int m,boolean o,long n,int k){block=b;this.x=x;this.y=y;this.z=z;meta=m;open=o;start=n-DURATION-RESTORE_GRACE;from=to=o?90:0;seen=n;kind=k;}void begin(boolean o,int m,long n){from=shownAngle(n);to=o?90:0;open=o;meta=m;start=n;wasActive=true;}boolean active(long n){return n-start<DURATION;}boolean visibleDuringRestore(long n){return n-start<DURATION+RESTORE_GRACE;}float shownAngle(long n){float p=Math.max(0,Math.min(1,(n-start)/(float)DURATION));p=p*p*(3-2*p);return from+(to-from)*p;}}
}
