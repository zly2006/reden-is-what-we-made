package com.github.zly2006.reden.rvc.tracking

// TODO: Serialization and deserialization for `TrackedDiff`s.
class TrackedDiffStorageImpl : TrackedDiffStorage {
    private var pDiffs: MutableMap<Long, TrackedDiff> = mutableMapOf()

    override fun get(id: Long): TrackedDiff? {
        return pDiffs[id]
    }

    // FIXME: A minimal implementation. Should be improved later.
    override fun store(trackedDiff: TrackedDiff?): Long {
        trackedDiff ?: return -1
        val id = pDiffs.size.toLong()
        pDiffs[id] = trackedDiff
        return id
    }

    // What fuck are these two functions do?
    override fun getRef(tag: String?): Long {
        TODO("Not yet implemented")
    }

    override fun addRef(tag: String?, id: Long): Boolean {
        TODO("Not yet implemented")
    }
}