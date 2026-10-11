package ru.givler.mbo.block.model;

import java.util.*;

/** Slices a union of boxes into occupied cells without filling the spaces between boxes. */
public final class CollisionShapeParts {
    private CollisionShapeParts() {}
    public static float[] envelope(float[][] boxes) {
        if (boxes.length == 0) throw new IllegalArgumentException("empty shape");
        float[] b = {Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};
        for (float[] box : boxes) for (int i=0;i<3;i++) {
            b[i]=Math.min(b[i],box[i]); b[i+3]=Math.max(b[i+3],box[i+3]);
        }
        return b;
    }
    public static Map<String, float[][]> cells(float[][] boxes) {
        Map<String,List<float[]>> cells=new LinkedHashMap<String,List<float[]>>();
        for(float[] box:boxes) {
            for(int i=0;i<3;i++) if(!(box[i]<box[i+3])) throw new IllegalArgumentException("invalid box");
            for(int x=(int)Math.floor(box[0]);x<Math.ceil(box[3]);x++)
                for(int y=(int)Math.floor(box[1]);y<Math.ceil(box[4]);y++)
                    for(int z=(int)Math.floor(box[2]);z<Math.ceil(box[5]);z++) {
                        String key=x+","+y+","+z;
                        List<float[]> list=cells.get(key);
                        if(list==null) {list=new ArrayList<float[]>();cells.put(key,list);}
                        list.add(new float[]{Math.max(0,box[0]-x),Math.max(0,box[1]-y),Math.max(0,box[2]-z),
                            Math.min(1,box[3]-x),Math.min(1,box[4]-y),Math.min(1,box[5]-z)});
                    }
        }
        Map<String,float[][]> result=new LinkedHashMap<String,float[][]>();
        for(Map.Entry<String,List<float[]>> entry:cells.entrySet())
            result.put(entry.getKey(),entry.getValue().toArray(new float[entry.getValue().size()][]));
        return result;
    }
}
