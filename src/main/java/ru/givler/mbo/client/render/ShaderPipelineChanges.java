package ru.givler.mbo.client.render;

/** One invalidation per actual pack reload or enable/disable transition. */
public final class ShaderPipelineChanges {
  private boolean observed, active;
  private Object revision;
  public boolean update(boolean nextActive, Object nextRevision) {
    boolean changed=observed && (active!=nextActive || !java.util.Objects.equals(revision,nextRevision));
    observed=true;active=nextActive;revision=nextRevision;
    return changed;
  }
}
