package ru.givler.mbo.block.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Standalone checks for the occupied cells used by model placement and removal. */
public final class ModelCollisionPartsSmoke {
    public static void main(String[] args) {
        List<ModelCollisionPart> grave = verify(new float[] {0, 0, 0, 1, .125F, 2});
        require(grave.size() == 1, "grave must occupy exactly two blocks");
        ModelCollisionPart end = grave.get(0);
        require(end.localX == 0 && end.localY == 0 && end.localZ == 1, "grave end position");
        require(end.bounds[4] == .125F, "grave height must be two pixels");
        verify(new float[] {-.375F, -.875F, -1, 1.375F, 1.625F, 2});
        verify(new float[] {.125F, 0, 0, 1.875F, .125F, .875F});
        require(verify(new float[] {0, 0, 0, 1, 1, 1}).isEmpty(), "no phantom edge blocks");
        require(new ModelCollisionPart(ModelCollisionPart.Axis.SIDE, 1,
            new float[] {0, 0, 0, 1, 1, 1}).localX == -1, "legacy side direction");
        System.out.println("Model collision partition checks passed");
    }

    private static List<ModelCollisionPart> verify(float[] b) {
        List<ModelCollisionPart> parts = ModelCollisionPart.partition(b);
        double volume = (Math.min(1, b[3]) - Math.max(0, b[0]))
            * (Math.min(1, b[4]) - Math.max(0, b[1]))
            * (Math.min(1, b[5]) - Math.max(0, b[2]));
        Set<String> cells = new HashSet<String>();
        for (ModelCollisionPart part : parts) {
            require(cells.add(part.localX + ":" + part.localY + ":" + part.localZ), "duplicate cell");
            for (int axis = 0; axis < 3; axis++)
                require(part.bounds[axis] >= 0 && part.bounds[axis + 3] <= 1
                    && part.bounds[axis] < part.bounds[axis + 3], "bounds outside own cell");
            volume += (part.bounds[3] - part.bounds[0]) * (part.bounds[4] - part.bounds[1])
                * (part.bounds[5] - part.bounds[2]);
        }
        double expected = (b[3] - b[0]) * (b[4] - b[1]) * (b[5] - b[2]);
        require(Math.abs(volume - expected) < .00001, "partition loses volume");
        for (int facing = 0; facing < 4; facing++) {
            Set<String> rotated = new HashSet<String>();
            for (ModelCollisionPart part : parts) {
                int x = part.localX, z = part.localZ;
                for (int i = 0; i < facing; i++) { int oldX = x; x = -z; z = oldX; }
                require(rotated.add(x + ":" + part.localY + ":" + z), "rotation overlaps cells");
                require(x != 0 || part.localY != 0 || z != 0, "part rotates onto owner");
            }
        }
        return parts;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
