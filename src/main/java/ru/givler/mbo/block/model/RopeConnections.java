package ru.givler.mbo.block.model;

/** Six world-space directions; these are not block-facing metadata. */
public final class RopeConnections {
    public static float[][] floorBoxes(int mask,boolean anchor) {
        java.util.List<float[]> boxes=new java.util.ArrayList<float[]>();
        if((mask&WEST)!=0) boxes.add(new float[]{0,.1875F,.4375F,.5F,.3125F,.5625F});
        if((mask&EAST)!=0) boxes.add(new float[]{.5F,.1875F,.4375F,1,.3125F,.5625F});
        if((mask&NORTH)!=0) boxes.add(new float[]{.4375F,.1875F,0,.5625F,.3125F,.5F});
        if((mask&SOUTH)!=0) boxes.add(new float[]{.4375F,.1875F,.5F,.5625F,.3125F,1});
        if((mask&DOWN)!=0) boxes.add(new float[]{.4375F,0,.4375F,.5625F,.25F,.5625F});
        if((mask&UP)!=0) boxes.add(new float[]{.4375F,.25F,.4375F,.5625F,1,.5625F});
        boolean straight=mask==(WEST|EAST)||mask==(NORTH|SOUTH)||mask==(DOWN|UP);
        if(anchor) boxes.add(new float[]{.3125F,.0625F,.3125F,.6875F,.4375F,.6875F});
        return boxes.toArray(new float[boxes.size()][]);
    }
    public static float[][] centerBoxes(int mask,boolean anchor) {
        float[][] boxes=floorBoxes(mask,anchor);
        for(float[] b:boxes) {
            if(b[1]==0 && b[4]==.25F) b[4]=.5F;
            else if(b[1]==.25F && b[4]==1) b[1]=.5F;
            else { b[1]+=.25F; b[4]+=.25F; }
        }
        return boxes;
    }
    public static final int DOWN = 1;
    public static final int UP = 2;
    public static final int NORTH = 4;
    public static final int SOUTH = 8;
    public static final int WEST = 16;
    public static final int EAST = 32;
    public static final int HORIZONTAL = NORTH | SOUTH | WEST | EAST;
    public static final int DECORATED = 8;
    public static final int NONE = 0, KNOT = 1, NOOSE = 2;

    private RopeConnections() {}

    /** A connector is needed when arms meet on different axes, including branches. */
    public static boolean isAngledConnection(int mask) {
        int axes=0;
        if((mask & (UP|DOWN))!=0) axes++;
        if((mask & (NORTH|SOUTH))!=0) axes++;
        if((mask & (WEST|EAST))!=0) axes++;
        return axes>1;
    }

    public static int placementAxis(int side) {
        return side < 2 ? 0 : side < 4 ? 2 : 1;
    }

    public static int visibleMask(int neighbors, int axis) {
        if (neighbors != 0) return neighbors;
        return axis == 1 ? WEST | EAST : axis == 2 ? NORTH | SOUTH : DOWN | UP;
    }

    public static int decorationFor(int mask) {
        if (mask == UP) return NOOSE;
        if (mask == (UP | DOWN) || mask == (WEST | EAST) || mask == (NORTH | SOUTH)) return KNOT;
        return NONE;
    }

    public static int decoratedMetadata(int mask) {
        if (decorationFor(mask) == NOOSE) return DECORATED;
        if (mask == (WEST | EAST)) return DECORATED | 1;
        if (mask == (NORTH | SOUTH)) return DECORATED | 2;
        if (mask == (UP | DOWN)) return DECORATED | 3;
        throw new IllegalArgumentException("Cannot decorate a rope junction");
    }

    public static int decoration(int metadata) {
        if ((metadata & DECORATED) == 0) return NONE;
        return (metadata & 3) == 0 ? NOOSE : KNOT;
    }

    public static int allowedConnections(int metadata) {
        int decoration = decoration(metadata);
        if (decoration == NONE) return HORIZONTAL | UP | DOWN;
        if (decoration == NOOSE) return UP;
        return (metadata & 3) == 1 ? EAST | WEST : (metadata & 3) == 2 ? NORTH | SOUTH : UP | DOWN;
    }

    /** Positive renderer Y rotation: east -> north -> west -> south. */
    public static int rotateMask(int mask, int quarterTurns) {
        for (int turn = 0; turn < (quarterTurns & 3); turn++) {
            int rotated = mask & (UP | DOWN);
            if ((mask & EAST) != 0) rotated |= NORTH;
            if ((mask & NORTH) != 0) rotated |= WEST;
            if ((mask & WEST) != 0) rotated |= SOUTH;
            if ((mask & SOUTH) != 0) rotated |= EAST;
            mask = rotated;
        }
        return mask;
    }

    public static int canonicalMask(int mask) {
        int horizontal = mask & HORIZONTAL;
        if (horizontal == 0) return mask;
        int vertical = mask & (UP | DOWN);
        int[] shapes = {EAST, EAST | WEST, EAST | SOUTH, EAST | WEST | SOUTH, HORIZONTAL};
        for (int shape : shapes) {
            for (int turn = 0; turn < 4; turn++) {
                if (rotateMask(shape, turn) == horizontal) return shape | vertical;
            }
        }
        throw new IllegalArgumentException("Invalid rope connections: " + mask);
    }

    public static int rotationQuarterTurns(int mask) {
        int canonical = canonicalMask(mask);
        for (int turn = 0; turn < 4; turn++) {
            if (rotateMask(canonical, turn) == mask) return turn;
        }
        throw new IllegalArgumentException("Invalid rope connections: " + mask);
    }

    public static String model(int mask) {
        int canonical = canonicalMask(mask);
        if ((canonical & HORIZONTAL) == 0) return "instruments_of_torture/rope";
        if (canonical == (WEST | EAST)) return "instruments_of_torture/rope_1";
        if (canonical == (EAST | DOWN)) return "instruments_of_torture/rope_7";
        if (canonical == (EAST | WEST | DOWN)) return "instruments_of_torture/rope_6";
        return "instruments_of_torture/rope_connected/connection_" + canonical;
    }

    public static String texture(int mask) {
        int canonical = canonicalMask(mask);
        if ((canonical & HORIZONTAL) == 0) return "instruments_of_torture/rope";
        if (canonical == (WEST | EAST)) return "instruments_of_torture/rope_1";
        if (canonical == (EAST | DOWN)) return "instruments_of_torture/rope_7";
        return "instruments_of_torture/rope_6";
    }
}
