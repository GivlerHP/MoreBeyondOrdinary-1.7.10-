package ru.givler.mbo.editor;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Shared, backwards-compatible Pos1/Pos2 storage used by all area editors. */
public final class AreaSelection {
    public static final String FIRST="Pos1", SECOND="Pos2";
    public final int dimension,minX,minY,minZ,maxX,maxY,maxZ;
    private AreaSelection(int dimension,int[] a,int[] b){this.dimension=dimension;minX=Math.min(a[0],b[0]);minY=Math.min(a[1],b[1]);minZ=Math.min(a[2],b[2]);maxX=Math.max(a[0],b[0]);maxY=Math.max(a[1],b[1]);maxZ=Math.max(a[2],b[2]);}
    public long volume(){return (long)(maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1);}
    public static AreaSelection read(ItemStack stack,int dimension){int[] a=getPoint(stack,FIRST,dimension),b=getPoint(stack,SECOND,dimension);return a==null||b==null?null:new AreaSelection(dimension,a,b);}
    public static int[] getPoint(ItemStack stack,String key,int dimension){if(stack==null||!stack.hasTagCompound()||!stack.getTagCompound().hasKey(key))return null;NBTTagCompound p=stack.getTagCompound().getCompoundTag(key);if(p.getInteger("D")!=dimension)return null;return new int[]{p.getInteger("X"),p.getInteger("Y"),p.getInteger("Z")};}
    public static void setPoint(ItemStack stack,String key,int dimension,int x,int y,int z){if(!stack.hasTagCompound())stack.setTagCompound(new NBTTagCompound());NBTTagCompound p=new NBTTagCompound();p.setInteger("D",dimension);p.setInteger("X",x);p.setInteger("Y",y);p.setInteger("Z",z);stack.getTagCompound().setTag(key,p);}
    public static long prospectiveVolume(int[] a,int x,int y,int z){return (long)(Math.abs(a[0]-x)+1)*(Math.abs(a[1]-y)+1)*(Math.abs(a[2]-z)+1);}
}
