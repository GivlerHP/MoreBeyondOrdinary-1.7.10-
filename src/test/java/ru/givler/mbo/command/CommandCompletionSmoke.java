package ru.givler.mbo.command;
import java.util.*;
public final class CommandCompletionSmoke {
  static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
  public static void main(String[] args){
    CommandSpeed speed=new CommandSpeed();
    check(speed.addTabCompletionOptions(null,new String[]{"re"}).equals(Collections.singletonList("reset")),"Speed reset prefix");
    check(speed.addTabCompletionOptions(null,new String[]{"1"}).equals(Arrays.asList("1","10")),"Speed numeric prefix");
    check(speed.addTabCompletionOptions(null,new String[]{"1",""})==null,"Speed has one argument");
    ru.givler.mbo.client.lighting.ColoredLightCommand rgb=new ru.givler.mbo.client.lighting.ColoredLightCommand();
    check(rgb.addTabCompletionOptions(null,new String[]{"in"}).equals(Collections.singletonList("inspect")),"RGB subcommands");
    System.out.println("PASS speed and RGB command context and prefix filtering");
  }
}
