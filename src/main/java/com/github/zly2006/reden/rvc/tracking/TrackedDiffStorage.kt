package com.github.zly2006.reden.rvc.tracking;

public interface TrackedDiffStorage {
    TrackedDiff get(long id);
    long store(TrackedDiff trackedDiff);
    long getRef(String tag);
    boolean addRef(String tag, long id);
}
