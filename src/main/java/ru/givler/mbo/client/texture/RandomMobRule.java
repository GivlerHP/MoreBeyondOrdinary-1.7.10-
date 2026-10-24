package ru.givler.mbo.client.texture;

import java.util.*;
import java.util.regex.Pattern;

/** OptiFine E7 skins.N rules, evaluated in numeric priority order. */
public final class RandomMobRule {
  public final int[] skins;
  private final int[] cumulative;
  private final int total;
  private final int[][] heights;
  private final String biomes,professions;
  private final Pattern name;
  private final boolean inverseName;
  public RandomMobRule(Properties p,int index) {
    ArrayList<Integer> ids=new ArrayList<>();
    int[][] ranges=PackRuleUtil.ranges(p.getProperty("skins."+index));
    if(ranges==null)throw new IllegalArgumentException("Missing skins");
    for(int[] range:ranges) {
      if(range[0]<1||range[1]>10000)throw new IllegalArgumentException("Invalid skin range");
      for(int i=range[0];i<=range[1];i++)ids.add(i);
    }
    skins=new int[ids.size()];for(int i=0;i<skins.length;i++)skins[i]=ids.get(i);
    if(skins.length==0)throw new IllegalArgumentException("Empty skins");
    String weightText=p.getProperty("weights."+index);int[] weights=null;
    if(weightText!=null) {
      String[] values=weightText.trim().split("\\s+");weights=new int[values.length];
      for(int i=0;i<values.length;i++){weights[i]=Integer.parseInt(values[i]);if(weights[i]<0)throw new IllegalArgumentException("Negative weight");}
    }
    cumulative=new int[skins.length];long sum=0,average=0;
    if(weights!=null) {for(int w:weights)average+=w;average/=weights.length;}
    for(int i=0;i<skins.length;i++) {
      sum+=weights==null?1:i<weights.length?weights[i]:average;
      if(sum>Integer.MAX_VALUE)throw new IllegalArgumentException("Weight overflow");cumulative[i]=(int)sum;
    }
    total=(int)sum;if(total<=0)throw new IllegalArgumentException("Empty weights");
    String h=p.getProperty("heights."+index);
    heights=PackRuleUtil.ranges(h!=null?h:p.containsKey("minHeight."+index)||p.containsKey("maxHeight."+index)
        ?p.getProperty("minHeight."+index,"0")+"-"+p.getProperty("maxHeight."+index,"256"):null);
    biomes=p.getProperty("biomes."+index);professions=p.getProperty("professions."+index);
    String n=p.getProperty("name."+index);inverseName=n!=null&&n.startsWith("!");if(inverseName)n=n.substring(1);
    name=n==null?null:parseName(n);
  }
  private static Pattern parseName(String n) {
    int flags=0;
    if(n.startsWith("iregex:"))return Pattern.compile(n.substring(7),Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    if(n.startsWith("regex:"))return Pattern.compile(n.substring(6));
    if(n.startsWith("ipattern:")){flags=Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE;n=n.substring(9);}
    else if(n.startsWith("pattern:"))n=n.substring(8);
    else return Pattern.compile(Pattern.quote(n));
    StringBuilder r=new StringBuilder();for(char c:n.toCharArray())r.append(c=='*'?".*":c=='?'?".":Pattern.quote(String.valueOf(c)));
    return Pattern.compile(r.toString(),flags);
  }
  public boolean matches(int biome,String biomeName,int y,String customName,int profession) {
    if(!PackRuleUtil.contains(heights,y)||!PackRuleUtil.biome(biomes,biome,biomeName))return false;
    if(name!=null && (name.matcher(customName==null?"":customName).matches()==inverseName))return false;
    if(professions!=null) {
      boolean matched=false;
      for(String token:professions.trim().split("\\s+")) {
        String[] names={"farmer","librarian","priest","blacksmith","butcher"};
        if(token.equals(Integer.toString(profession))||profession>=0&&profession<names.length&&token.equals(names[profession]))matched=true;
      }
      if(!matched)return false;
    }
    return true;
  }
  public int choose(int seed){int value=(seed&Integer.MAX_VALUE)%total;for(int i=0;i<skins.length;i++)if(value<cumulative[i])return skins[i];return skins[0];}
}
