package ru.givler.mbo.block;
import ru.givler.mbo.tileentity.TileEntityShelf;
import java.lang.reflect.Proxy;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public final class ShelfSmoke {
    public static void main(String[] args) throws Exception {
        if(!(ShelfSmoke.class.getClassLoader() instanceof net.minecraft.launchwrapper.LaunchClassLoader)) {
            String[] entries=System.getProperty("java.class.path").split(java.io.File.pathSeparator);
            java.net.URL[] urls=new java.net.URL[entries.length];
            for(int i=0;i<entries.length;i++)urls[i]=new java.io.File(entries[i]).toURI().toURL();
            net.minecraft.launchwrapper.LaunchClassLoader loader=new net.minecraft.launchwrapper.LaunchClassLoader(urls);
            loader.addClassLoaderExclusion("org.apache.logging.");
            Thread.currentThread().setContextClassLoader(loader);
            Class.forName(ShelfSmoke.class.getName(),true,loader).getMethod("main",String[].class).invoke(null,(Object)args);
            return;
        }
        cpw.mods.fml.common.Loader.injectData("7","99","40","1614","1.7.10","9.05",new java.io.File("."),new java.util.ArrayList<String>());
        Bootstrap.func_151354_b();
        TileEntity.addMapping(TileEntityShelf.class,"mbo:test_shelf");
        TileEntityShelf shelf=new TileEntityShelf();
        ItemStack named=new ItemStack(Items.diamond,64);named.setStackDisplayName("NBT survives");
        if(shelf.swap(0,named)!=null)throw new AssertionError("New shelf not empty");
        NBTTagCompound tag=new NBTTagCompound();shelf.writeToNBT(tag);
        TileEntityShelf restored=new TileEntityShelf();restored.readFromNBT(tag);
        if(!ItemStack.areItemStacksEqual(named,restored.get(0)))throw new AssertionError("NBT/stack round trip");
        if(restored.swap(0,null)==null||restored.get(0)!=null)throw new AssertionError("Retrieval");
        restored.writeToNBT(tag);shelf.readFromNBT(tag);
        if(shelf.get(0)!=null)throw new AssertionError("Stale item resurrected");
        if(shelf.canUpdate())throw new AssertionError("Shelf must not tick");
        for (int mask = 0; mask < 8; mask++) {
            for (int slot = 0; slot < 3; slot++) shelf.swap(slot,
                    (mask & (1 << slot)) == 0 ? null : new ItemStack(Items.diamond, slot + 1));
            if (shelf.comparatorSignal() != mask) throw new AssertionError("Comparator occupied-slot mask");
        }
        final BlockShelf block=new BlockShelf("oak");
        for(final int facing:new int[]{2,3,4,5})for(int length=1;length<=12;length++) {
            final int count=length;
            final int dx=facing==2?-1:facing==3?1:0,dz=facing==4?1:facing==5?-1:0;
            IBlockAccess world=(IBlockAccess)Proxy.newProxyInstance(ShelfSmoke.class.getClassLoader(),new Class[]{IBlockAccess.class},(proxy,method,a)->{
                int n=dx!=0?(Integer)a[0]*dx:(Integer)a[2]*dz;
                boolean present=n>=0&&n<count&&(Integer)a[1]==0&&(dx==0?(Integer)a[0]==0:(Integer)a[2]==0);
                if(method.getName().equals("getBlock"))return present?block:Blocks.air;
                if(method.getName().equals("getBlockMetadata"))return facing|8;
                throw new AssertionError("Mesh scan must only read blocks and metadata: "+method.getName());
            });
            for(int n=0;n<count;n++){
                int[] g=BlockShelf.group(world,n*dx,0,n*dz,facing);
                int first=n+g[0],last=n+g[1];
                if(first!=n/3*3||last!=Math.min(first+3,count))throw new AssertionError("Click-dependent group");
                int connection=BlockShelf.connection(world,n*dx,0,n*dz);
                int expected=last-first==1?0:n==first?1:n==last-1?3:2;
                if(connection!=expected)throw new AssertionError("Connection texture order");
            }
        }
        System.out.println("SHELVES_OK: NBT, full stacks, retrieval, no ticking, 4 rotations and groups of 1..12");
    }
}
