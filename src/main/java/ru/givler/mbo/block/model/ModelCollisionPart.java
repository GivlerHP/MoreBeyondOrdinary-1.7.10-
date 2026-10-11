package ru.givler.mbo.block.model;

/** One extra collision block expressed in the model's local coordinates. */
public final class ModelCollisionPart {
    /** Partitions an intentional envelope; the owner cell is handled by BlockModels. */
    public static java.util.List<ModelCollisionPart> partition(float[] b) {
        java.util.List<ModelCollisionPart> parts = new java.util.ArrayList<ModelCollisionPart>();
        for (int x = (int) Math.floor(b[0]); x < Math.ceil(b[3]); x++)
            for (int y = (int) Math.floor(b[1]); y < Math.ceil(b[4]); y++)
                for (int z = (int) Math.floor(b[2]); z < Math.ceil(b[5]); z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    float[] bounds = {Math.max(0, b[0] - x), Math.max(0, b[1] - y), Math.max(0, b[2] - z),
                        Math.min(1, b[3] - x), Math.min(1, b[4] - y), Math.min(1, b[5] - z)};
                    parts.add(new ModelCollisionPart(x, y, z, bounds));
                }
        return parts;
    }
    public enum Axis { VERTICAL, SIDE, FORWARD }

    public final Axis axis;
    public final int offset;
    public final float[] bounds;
    public final float[][] boxes;
    public final int localX, localY, localZ;

    public ModelCollisionPart(Axis axis, int offset, float[] bounds) {
        if (offset == 0) throw new IllegalArgumentException("collision offset must not be 0");
        this.axis = axis;
        this.offset = offset;
        this.bounds = bounds;
        this.boxes = new float[][] {bounds};
        this.localX = axis == Axis.SIDE ? -offset : 0;
        this.localY = axis == Axis.VERTICAL ? offset : 0;
        this.localZ = axis == Axis.FORWARD ? offset : 0;
    }

    public ModelCollisionPart(int x, int y, int z, float[] bounds) {
        this(x,y,z,new float[][] {bounds});
    }
    public ModelCollisionPart(int x, int y, int z, float[][] boxes) {
        if (x == 0 && y == 0 && z == 0) throw new IllegalArgumentException("part overlaps owner");
        this.axis = null;
        this.offset = 0;
        this.localX = x;
        this.localY = y;
        this.localZ = z;
        this.boxes = boxes;
        this.bounds = CollisionShapeParts.envelope(boxes);
    }
}
