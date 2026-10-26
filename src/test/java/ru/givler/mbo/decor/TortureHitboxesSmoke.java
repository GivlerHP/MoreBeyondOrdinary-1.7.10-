package ru.givler.mbo.decor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.TileEntityModelCollision;

/** Checks gaps, rotated helper cells, ray targeting and saved-envelope replacement. */
public final class TortureHitboxesSmoke {
    static final class PlacementBlock extends net.minecraft.block.Block {
        final boolean air;
        PlacementBlock(boolean air) { super(air?net.minecraft.block.material.Material.air:net.minecraft.block.material.Material.rock); this.air=air; }
        public boolean isAir(net.minecraft.world.IBlockAccess world,int x,int y,int z) { return air; }
        public boolean isReplaceable(net.minecraft.world.IBlockAccess world,int x,int y,int z) { return air; }
    }
    static final class TestWorld extends CollisionHelperLifecycleSmoke.TestWorld {
        static final net.minecraft.block.Block SOLID=new PlacementBlock(false), AIR=new PlacementBlock(true);
        int facing;
        boolean placementFloor, obstruction;
        int obstacleX, obstacleY, obstacleZ;
        public int getBlockMetadata(int x,int y,int z) { return facing; }
        public int getHeight() { return 256; }
        public net.minecraft.block.Block getBlock(int x,int y,int z) {
            if(!placementFloor) return super.getBlock(x,y,z);
            return y<1 || (obstruction && x==obstacleX && y==obstacleY && z==obstacleZ)
                ? SOLID : AIR;
        }
    }

    private static void require(boolean value,String message) {
        if(!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        java.lang.reflect.Field access=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        access.setAccessible(true);
        TestWorld world=(TestWorld)((sun.misc.Unsafe)access.get(null)).allocateInstance(TestWorld.class);
        Method register=ru.givler.mbo.registry.ModelRegistry.class.getDeclaredMethod("registerTortureModels");
        register.setAccessible(true);register.invoke(null);
        checkDescriptions();
        int count=0;
        java.util.Set<String> special=new java.util.HashSet<String>(java.util.Arrays.asList(
            "ModelTortureBedOnChains", "ModelTortureIronCage", "ModelTortureRopeCore", "ModelTortureSuspendedCorpse"));
        for(java.lang.reflect.Field field:ru.givler.mbo.registry.ModelRegistry.class.getFields()) {
            if(!field.getName().startsWith("ModelTorture") || special.contains(field.getName())) continue;
            BlockModels model=(BlockModels)field.get(null);
            if(model==null || !model.hasMultipartCollisionBoxes()) continue;
            String name=field.getName();
            double volume=volume(model.getMultipartBoxesForCell(0,0,0,0));
            for(int[] pos:model.getCollisionPartPositions(0,0,0,0))
                volume+=volume(model.getMultipartBoxesForCell(pos[0],pos[1],pos[2],0));
            for(int facing=0;facing<4;facing++) {
                double partitioned=volume(model.getMultipartBoxesForCell(0,0,0,facing));
                java.util.Set<String> cells=new java.util.HashSet<String>();
                for(int[] pos:model.getCollisionPartPositions(0,0,0,facing)) {
                    require(cells.add(java.util.Arrays.toString(pos)),"Duplicate helper cell: "+name);
                    float[][] boxes=model.getMultipartBoxesForCell(pos[0],pos[1],pos[2],facing);
                    require(boxes.length>0,"Missing rotated helper: "+name);
                    partitioned+=volume(boxes);
                }
                require(Math.abs(partitioned-volume)<.00001,"Shape changed during cell rotation: "+name);
            }
            count++;
        }
        require(count==34,"Expected all 34 ordinary model shapes");
        world.placementFloor=true;
        net.minecraft.entity.player.EntityPlayerMP placer=(net.minecraft.entity.player.EntityPlayerMP)
            ((sun.misc.Unsafe)access.get(null)).allocateInstance(net.minecraft.entity.player.EntityPlayerMP.class);
        for(BlockModels model:new BlockModels[]{ru.givler.mbo.registry.ModelRegistry.ModelTortureGrave,
                ru.givler.mbo.registry.ModelRegistry.ModelTortureEmbalmedCorpse}) {
            for(int facing=0;facing<4;facing++) {
                placer.rotationYaw=facing*90-180;
                require(model.canPlaceStructureAt(world,0,1,0,placer),"Ground decoration cannot be placed on a solid floor");
                java.util.List<int[]> parts=model.getCollisionPartPositions(0,1,0,facing);
                for(int[] pos:parts) require(pos[1]>=1,"Ground decoration reserves part of the supporting floor");
                require(!parts.isEmpty(),"Ground decoration must retain its neighboring parts");
                int[] obstacle=parts.get(0);
                world.obstacleX=obstacle[0];world.obstacleY=obstacle[1];world.obstacleZ=obstacle[2];
                world.obstruction=true;
                require(!model.canPlaceStructureAt(world,0,1,0,placer),"Placement must still reject occupied neighboring cells");
                world.obstruction=false;
            }
        }
        world.placementFloor=false;
        BlockModels grave=ru.givler.mbo.registry.ModelRegistry.ModelTortureGrave;
        world.owner=grave;
        ArrayList<AxisAlignedBB> graveCollision=new ArrayList<AxisAlignedBB>();
        grave.addCollisionBoxesToList(world,0,0,0,
            AxisAlignedBB.getBoundingBox(.1,.01,.7,.4,.12,1),graveCollision,null);
        require(graveCollision.isEmpty(),"Grave mound collision must be lowered to the flat ground layer");

        BlockModels horse=ru.givler.mbo.registry.ModelRegistry.ModelTortureDeathHorse;
        world.owner=horse;
        ArrayList<AxisAlignedBB> horseCollision=new ArrayList<AxisAlignedBB>();
        horse.addCollisionBoxesToList(world,0,0,0,
            AxisAlignedBB.getBoundingBox(-1,.876,-1,2,2,3),horseCollision,null);
        require(horseCollision.isEmpty(),"Torture horse head and spikes must not collide");
        require(horse.getCollisionPartPositions(0,0,0,0).isEmpty(),"Decorative head must not reserve helper blocks");
        require(horse.collisionRayTrace(world,0,0,0,Vec3.createVectorHelper(.5,1,-1),
            Vec3.createVectorHelper(.5,1,3))==null,"Torture horse spikes must not be selectable");
        horse.addCollisionBoxesToList(world,0,0,0,
            AxisAlignedBB.getBoundingBox(.4,.7,.4,.6,.8,.6),horseCollision,null);
        require(!horseCollision.isEmpty(),"Torture horse wooden body must remain solid");

        BlockModels bench=ru.givler.mbo.registry.ModelRegistry.ModelTortureBench;
        world.owner=bench;
        ArrayList<AxisAlignedBB> collision=new ArrayList<AxisAlignedBB>();
        bench.addCollisionBoxesToList(world,0,0,0,AxisAlignedBB.getBoundingBox(.4,0,.2,.6,.1,.8),collision,null);
        require(collision.isEmpty(),"Empty space under a bench must not collide");
        require(bench.collisionRayTrace(world,0,0,0,Vec3.createVectorHelper(.5,.05,-1),
            Vec3.createVectorHelper(.5,.05,2))==null,"Ray hit empty space under the bench");
        require(bench.collisionRayTrace(world,0,0,0,Vec3.createVectorHelper(.5,.45,-1),
            Vec3.createVectorHelper(.5,.45,2))!=null,"Bench seat must remain selectable");

        TileEntityModelCollision old=new TileEntityModelCollision();
        old.configure(0,0,0,new float[]{0,0,0,1,1,1});
        old.xCoord=-1;old.setWorldObj(world);
        require(old.getBoxes().length==3,"Saved solid helper must use the current bench leg, brace and seat");
        require(!contains(old.getBoxes(),.1F,.1F,.1F),"Old helper still fills the void");
        require(contains(old.getBoxes(),.7F,.1F,.5F),"Bench leg disappeared from an existing helper");
        old.yCoord=1;world.available=true;
        old.checkOwner();
        require(world.removed,"Obsolete saved helper must be removed");
        System.out.println("PASS 34 torture shapes, four rotations, open gaps, ray targeting and saved helper migration");
    }

    private static void checkDescriptions() throws Exception {
        for (String language : new String[]{"ru_RU", "en_US"}) {
            try (java.io.InputStream input = TortureHitboxesSmoke.class.getResourceAsStream("/assets/mbo/lang/" + language + ".lang")) {
                net.minecraft.util.StringTranslate.inject(input);
            }
            for (BlockModels model : BlockModels.getAllModels()) {
                java.util.List<String> lines = new ArrayList<String>();
                ru.givler.mbo.item.ItemBlockModels.addModelDescription(model, lines);
                require(!lines.isEmpty(), "Missing model description: " + language + "/" + model.getUnlocalizedName());
                for (String line : lines) require(!line.contains("\\n") && !line.contains(".desc"), "Unresolved description: " + line);
            }
            java.util.List<String> lines = new ArrayList<String>();
            ru.givler.mbo.item.ItemBlockModels.addModelDescription(
                ru.givler.mbo.registry.ModelRegistry.ModelTortureBedOnChains, lines);
            require(lines.size() == 2, "Sleep instructions must contain two tooltip lines");
            lines.clear();
            ru.givler.mbo.item.ItemBlockModels.addModelDescription(new PlacementBlock(false), lines);
            require(lines.isEmpty(), "Missing description must not expose a localization key");
        }
    }

    private static double volume(float[][] boxes) {
        double volume=0;
        for(float[] b:boxes) {
            for(int i=0;i<3;i++) require(b[i]>=0 && b[i+3]<=1 && b[i]<b[i+3],"Invalid cell box");
            volume+=(b[3]-b[0])*(double)(b[4]-b[1])*(b[5]-b[2]);
        }
        return volume;
    }

    private static boolean contains(float[][] boxes,float x,float y,float z) {
        for(float[] b:boxes) if(x>=b[0] && x<=b[3] && y>=b[1] && y<=b[4] && z>=b[2] && z<=b[5]) return true;
        return false;
    }
}
