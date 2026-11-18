package ru.givler.mbo.security;
import ru.givler.mbo.editor.AreaSelection;
public final class AreaVolumeTest {
 public static void main(String[] args) {
  check(AreaSelection.prospectiveVolume(new int[]{0,0,0},1,2,3)==24,"normal");
  check(AreaSelection.prospectiveVolume(new int[]{Integer.MIN_VALUE,0,0},Integer.MAX_VALUE,0,0)==4294967296L,"int overflow");
  check(AreaSelection.prospectiveVolume(new int[]{Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE},Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE)==Long.MAX_VALUE,"long overflow");
  System.out.println("PASS: 3 volume regression cases");
 }
 static void check(boolean pass,String name){if(!pass)throw new AssertionError(name);}
}