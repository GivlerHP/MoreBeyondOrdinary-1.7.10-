package ru.givler.mbo.block.model;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class RopeConnectionsSmoke {
    public static void main(String[] args) {
        for(int mask=1;mask<64;mask++) {
            boolean angled=((mask&3)!=0 && (mask&60)!=0)
                    || ((mask&12)!=0 && (mask&48)!=0);
            require(RopeConnections.isAngledConnection(mask)==angled,"connector topology " + mask);
        }
        int[] expectedAxes = {0, 0, 2, 2, 1, 1};
        for (int side = 0; side < 6; side++) {
            require(RopeConnections.placementAxis(side) == expectedAxes[side], "clicked face axis");
        }
        require(RopeConnections.visibleMask(0, 0) == 3, "isolated vertical rope");
        require(RopeConnections.visibleMask(0, 1) == 48, "isolated east/west rope");
        require(RopeConnections.visibleMask(0, 2) == 12, "isolated north/south rope");
        require(RopeConnections.decorationFor(RopeConnections.UP) == RopeConnections.NOOSE,
                "lower free end becomes a noose");
        require(RopeConnections.decorationFor(RopeConnections.DOWN) == RopeConnections.NONE,
                "upper free end must not become a noose");
        for (int mask = 1; mask < 64; mask++) {
            int decoration = RopeConnections.decorationFor(mask);
            if (mask == 3 || mask == 12 || mask == 48) {
                require(decoration == RopeConnections.KNOT, "straight rope receives variant 5");
                int metadata = RopeConnections.decoratedMetadata(mask);
                require(RopeConnections.decoration(metadata) == RopeConnections.KNOT, "saved variant 5");
                require((metadata & 4) == 0, "decoration must not use the model top-part bit");
                require(RopeConnections.allowedConnections(metadata) == mask, "variant 5 keeps its axis");
            } else if (mask != 2) {
                require(decoration == RopeConnections.NONE, "junctions cannot receive another knot");
            }
        }
        int noose = RopeConnections.decoratedMetadata(RopeConnections.UP);
        require(RopeConnections.decoration(noose) == RopeConnections.NOOSE, "saved noose");
        require(RopeConnections.allowedConnections(noose) == RopeConnections.UP, "noose connects only above");
        for (int neighbors = 1; neighbors < 64; neighbors++) {
            int canonical = RopeConnections.canonicalMask(neighbors);
            int turn = RopeConnections.rotationQuarterTurns(neighbors);
            require(RopeConnections.rotateMask(canonical, turn) == neighbors,
                    "rendered ends must face the actual neighbors: " + neighbors);
            require((canonical & 3) == (neighbors & 3), "Y rotation must preserve vertical connections");
            require(RopeConnections.rotateMask(neighbors, 4) == neighbors, "full turn");
            for (int rotation = 0; rotation < 4; rotation++) {
                require(RopeConnections.model(RopeConnections.rotateMask(neighbors, rotation))
                        .equals(RopeConnections.model(neighbors)), "rotations must share one model");
            }
            for (int axis = 0; axis < 3; axis++) {
                require(RopeConnections.visibleMask(neighbors, axis) == neighbors,
                        "neighbors must override initial orientation");
            }
            String base = "src/main/resources/assets/mbo/";
            require(Files.isRegularFile(Paths.get(base + ru.givler.mbo.client.model.decormodels.DecorResourcePaths.geometry(RopeConnections.model(neighbors)))),
                    "missing model for mask " + neighbors);
            require(Files.isRegularFile(Paths.get(base + ru.givler.mbo.client.model.decormodels.DecorResourcePaths.texture(RopeConnections.texture(neighbors), ""))),
                    "missing texture for mask " + neighbors);
        }
        require(RopeConnections.model(RopeConnections.EAST | RopeConnections.DOWN).endsWith("rope_7"),
                "rope_7 elbow");
        require(RopeConnections.model(RopeConnections.EAST | RopeConnections.WEST | RopeConnections.DOWN)
                .endsWith("rope_6"), "rope_6 junction");
        require(RopeConnections.rotationQuarterTurns(RopeConnections.NORTH | RopeConnections.DOWN) == 1,
                "north-facing elbow must rotate 90 degrees");
        require(RopeConnections.rotationQuarterTurns(RopeConnections.WEST | RopeConnections.DOWN) == 2,
                "west-facing elbow must rotate 180 degrees");
        require(RopeConnections.rotationQuarterTurns(RopeConnections.SOUTH | RopeConnections.DOWN) == 3,
                "south-facing elbow must rotate 270 degrees");
        System.out.println("Rope placement axes and all 64 connection states passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
