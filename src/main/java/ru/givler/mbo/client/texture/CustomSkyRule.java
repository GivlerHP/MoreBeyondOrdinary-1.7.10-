package ru.givler.mbo.client.texture;

import java.util.*;

/** E7 linear daily fades and optional day/weather/location conditions. */
public final class CustomSkyRule {
  public final int start,end,startOut,endOut,daysLoop;
  public final int[][] days,heights;
  public final String biomes;
  public final float transition;
  private final Set<String> weather;
  public CustomSkyRule(Properties p) {
    start=time(p.getProperty("startFadeIn"));end=time(p.getProperty("endFadeIn"));endOut=time(p.getProperty("endFadeOut"));
    int out=p.containsKey("startFadeOut")?time(p.getProperty("startFadeOut")):norm(endOut-norm(end-start));
    if(!p.containsKey("startFadeOut")&&between(out,start,end))out=end;
    startOut=out;
    if(norm(end-start)+norm(startOut-end)+norm(endOut-startOut)+norm(start-endOut)!=24000)
      throw new IllegalArgumentException("Fade intervals must add up to one day");
    daysLoop=Integer.parseInt(p.getProperty("daysLoop","8"));if(daysLoop<=0)throw new IllegalArgumentException("daysLoop");
    days=PackRuleUtil.ranges(p.getProperty("days"));heights=PackRuleUtil.ranges(p.getProperty("heights"));biomes=p.getProperty("biomes");
    transition=Float.parseFloat(p.getProperty("transition","1"));if(!Float.isFinite(transition)||transition<0)throw new IllegalArgumentException("transition");
    weather=new HashSet<>(Arrays.asList(p.getProperty("weather","clear").trim().split("\\s+")));
    if(!Arrays.asList("clear","rain","thunder").containsAll(weather))throw new IllegalArgumentException("weather");
  }
  public static int norm(int value){return Math.floorMod(value,24000);}
  public static int time(String text) {
    if(text==null)throw new IllegalArgumentException("Missing sky time");
    String[] a=text.trim().split(":");if(a.length!=2)throw new IllegalArgumentException("Invalid time: "+text);
    int h=Integer.parseInt(a[0]),m=Integer.parseInt(a[1]);
    if(h<0||h>23||m<0||m>59)throw new IllegalArgumentException("Invalid time: "+text);
    return norm((h-6)*1000+(int)(m/60.0*1000));
  }
  private static boolean between(int t,int a,int b){return a<=b?t>=a&&t<=b:t>=a||t<=b;}
  public float fade(int t) {
    t=norm(t);
    if(between(t,start,end))return norm(end-start)==0?0:(float)norm(t-start)/norm(end-start);
    if(between(t,end,startOut))return 1;
    if(between(t,startOut,endOut))return norm(endOut-startOut)==0?0:1-(float)norm(t-startOut)/norm(endOut-startOut);
    return 0;
  }
  public float brightness(long worldTime,float rain,float thunder) {
    int day=(int)Math.floorMod(Math.floorDiv(worldTime-start,24000L),(long)daysLoop);
    if(!PackRuleUtil.contains(days,day))return 0;
    float amount=(weather.contains("clear")?1-rain:0)+(weather.contains("rain")?rain-thunder:0)+(weather.contains("thunder")?thunder:0);
    return Math.max(0,Math.min(1,amount))*fade((int)Math.floorMod(worldTime,24000L));
  }
  public boolean position(int biomeId,String biomeName,int y){return PackRuleUtil.contains(heights,y)&&PackRuleUtil.biome(biomes,biomeId,biomeName);}
  public static float smooth(float previous,float target,float seconds,float transition) {
    if(seconds<=0)return previous;
    if(transition<=0||seconds>=transition||Math.abs(target-previous)<=1e-6f)return target;
    float k=transition/seconds;
    float factor=Math.max(0,Math.min(1,seconds/transition*(k-4.61f/(1+0.13f*k/10))));
    return previous+(target-previous)*factor;
  }
}
