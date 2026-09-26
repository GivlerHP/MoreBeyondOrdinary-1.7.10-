package ru.givler.mbo.potion;

/** Surrounds the affected player with a persistent cloud of fog particles. */
public class DenseFog extends MagicStatus {
    public DenseFog(int id, boolean harmful, int colour) {
        super(id, harmful, colour, "dense_fog", 0);
        setShowEntityParticles(false);
    }
}
