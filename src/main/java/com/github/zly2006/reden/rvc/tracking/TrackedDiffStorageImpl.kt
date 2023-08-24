package com.github.zly2006.reden.rvc.tracking

@Deprecated("Deprecated because a new tracking & versioning system is being developed")
// TODO: Serialization and deserialization for `TrackedDiff`s.
class TrackedDiffStorageImpl : TrackedDiffStorage {
    private var pDiffs: MutableMap<Long, TrackedDiff> = mutableMapOf()

    override fun get(id: Long): TrackedDiff {
        return pDiffs[id] ?: throw IllegalArgumentException("No such id: $id")
    }

    // FIXME: A minimal implementation. Should be improved later.
    override fun store(trackedDiff: TrackedDiff): Long {
        val id = pDiffs.size.toLong()
        pDiffs[id] = trackedDiff
        return id
    }

    override fun getRef(tag: String): Long {
        TODO("Not yet implemented")
    }

    override fun addRef(tag: String, id: Long): Boolean {
        TODO("Not yet implemented")
    }
}