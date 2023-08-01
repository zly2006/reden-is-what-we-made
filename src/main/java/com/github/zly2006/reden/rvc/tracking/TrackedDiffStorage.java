package com.github.zly2006.reden.rvc.tracking;

public interface TrackedDiffStorage {
    TrackedDiff get(long id);
    boolean set(long id, TrackedDiff diff);
    long getRef(String tag);
    boolean addRef(String tag, long id);
}
