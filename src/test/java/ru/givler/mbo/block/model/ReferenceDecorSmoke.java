package ru.givler.mbo.block.model;

import java.util.Map;
import java.nio.file.*;
import ru.givler.mbo.client.model.decormodels.DecorResourcePaths;

public final class ReferenceDecorSmoke {
    public static void main(String[] args) {
        float[][] bench={{-.5F,.375F,.125F,1.5F,.5F,.875F},
            {-.375F,0,.25F,-.25F,.375F,.75F},{1.25F,0,.25F,1.375F,.375F,.75F}};
        Map<String,float[][]> cells=CollisionShapeParts.cells(bench);
        check(cells.size()==3,"bench occupies three cells");
        check(!contains(cells.get("0,0,0"),.5F,.0625F,.5F),"space under bench must stay empty");
        check(contains(cells.get("0,0,0"),.5F,.4375F,.5F),"seat must remain solid");
        Map<String,float[][]> separated=CollisionShapeParts.cells(new float[][] {
            {0,0,0,.125F,.5F,.125F},{2,0,0,2.125F,.5F,.125F}});
        check(!separated.containsKey("1,0,0"),"empty intermediate cells cannot be reserved");
        for(int mask=1;mask<64;mask++) for(boolean anchor:new boolean[]{false,true}) {
            float[][] boxes=RopeConnections.floorBoxes(mask,anchor);
            for(float[] b:boxes) for(int i=0;i<3;i++)
                check(b[i]>=0 && b[i+3]<=1 && b[i]<b[i+3],"floor box outside its cell");
            if((mask&3)==0) check(CollisionShapeParts.envelope(boxes)[4]<=.4375F,"floor rope cannot remain at wall height");
            String name="instruments_of_torture/rope_connected/floor_"+(anchor?"anchor_":"")+RopeConnections.canonicalMask(mask);
            check(Files.isRegularFile(Paths.get("src/main/resources/assets/mbo/"+DecorResourcePaths.geometry(name))),"missing floor geometry "+name);
            if((mask&RopeConnections.UP)!=0)
                check(contains(boxes,.5F,.99F,.5F),"floor-to-wall riser must meet upper block");
        }
        System.out.println("Separate furniture parts, floor rope geometry and upward transitions passed");
    }
    private static boolean contains(float[][] boxes,float x,float y,float z) {
        for(float[] b:boxes) if(x>b[0]&&x<b[3]&&y>b[1]&&y<b[4]&&z>b[2]&&z<b[5]) return true;
        return false;
    }
    private static void check(boolean value,String message) {if(!value) throw new AssertionError(message);}
}
