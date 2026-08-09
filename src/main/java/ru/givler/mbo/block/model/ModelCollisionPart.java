package ru.givler.mbo.block.model;

/** One extra collision block expressed in the model's local coordinates. */
public final class ModelCollisionPart {
    public enum Axis { VERTICAL, SIDE, FORWARD }

    public final Axis axis;
    public final int offset;
    public final float[] bounds;

    public ModelCollisionPart(Axis axis, int offset, float[] bounds) {
        if (offset == 0) throw new IllegalArgumentException("collision offset must not be 0");
        this.axis = axis;
        this.offset = offset;
        this.bounds = bounds;
    }
}
