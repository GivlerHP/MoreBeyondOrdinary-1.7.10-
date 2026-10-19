package ru.givler.mbo.integration.angelica.client;

import net.minecraft.block.Block;

/** Resolve OptiFine's numeric block selector before Angelica's name-based parser. */
public final class AngelicaLegacyBlockIds {
  private AngelicaLegacyBlockIds() {}
  public static String resolve(String selector) {
    if(selector==null || selector.isEmpty())return selector;
    int colon=selector.indexOf(':');
    String number=colon<0?selector:selector.substring(0,colon);
    for(int i=0;i<number.length();i++)if(number.charAt(i)<'0' || number.charAt(i)>'9')return selector;
    if(number.isEmpty())return selector;
    try {
      int id=Integer.parseInt(number);
      Block block=Block.getBlockById(id);
      if(block==null || Block.getIdFromBlock(block)!=id)return selector;
      String name=Block.blockRegistry.getNameForObject(block);
      return name==null?selector:name+(colon<0?"":selector.substring(colon));
    }catch(NumberFormatException invalid){return selector;}
  }
}
