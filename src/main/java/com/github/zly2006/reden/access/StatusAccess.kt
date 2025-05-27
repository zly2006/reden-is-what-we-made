package com.github.zly2006.reden.access

interface StatusAccess {
    var status: Long

    fun addStatus(status: Long): Long {
        this.status = this.status or status
        return this.status
    }

    fun removeStatus(status: Long): Long {
        this.status = this.status and status.inv()
        return this.status
    }

    fun hasStatus(status: Long): Boolean {
        return this.status and status != 0L
    }
}
