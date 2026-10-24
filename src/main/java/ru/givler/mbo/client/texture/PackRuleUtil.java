package ru.givler.mbo.client.texture;

import java.util.*;
import java.util.regex.*;

/** Shared OptiFine property syntax, independent of the renderer and GL. */
public final class PackRuleUtil {
  private PackRuleUtil() {}
  public static int[][] ranges(String text) {
    if(text==null)return null;
    ArrayList<int[]> result=new ArrayList<>();
    for(String token:text.trim().split("\\s+")) {
      Matcher m=Pattern.compile("(-?\\d+)(?:-(-?\\d+))?").matcher(token);
      if(!m.matches())throw new IllegalArgumentException("Invalid range: "+token);
      int a=Integer.parseInt(m.group(1)),b=m.group(2)==null?a:Integer.parseInt(m.group(2));
      if(a>b)throw new IllegalArgumentException("Reversed range: "+token);
      result.add(new int[]{a,b});
    }
    return result.toArray(new int[0][]);
  }
  public static boolean contains(int[][] ranges,int value) {
    if(ranges==null)return true;
    for(int[] r:ranges)if(value>=r[0]&&value<=r[1])return true;
    return false;
  }
  public static String normalizeBiome(String name){return name.toLowerCase(Locale.ROOT).replace(" ","").replace("_","");}
  public static boolean biome(String rule,int id,String name) {
    if(rule==null)return true;
    boolean inverse=rule.startsWith("!");if(inverse)rule=rule.substring(1);
    boolean found=false;
    for(String token:rule.trim().split("\\s+"))
      if(token.equals(Integer.toString(id))||normalizeBiome(token).equals(normalizeBiome(name)))found=true;
    return found!=inverse;
  }
}
