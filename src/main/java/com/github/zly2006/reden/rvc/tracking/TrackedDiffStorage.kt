package com.github.zly2006.reden.rvc.tracking

@Deprecated("Deprecated because a new tracking & versioning system is being developed")
interface TrackedDiffStorage {
    operator fun get(id: Long): TrackedDiff?
    fun store(trackedDiff: TrackedDiff): Long
    fun getRef(tag: String): Long
    fun addRef(tag: String, id: Long): Boolean
}
