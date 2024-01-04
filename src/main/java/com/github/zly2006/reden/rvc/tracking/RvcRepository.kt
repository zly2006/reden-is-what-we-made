package com.github.zly2006.reden.rvc.tracking

class RvcRepository {
    fun commit(structure: TrackedStructure) {

    }

    fun push() {

    }

    fun fetch() {

    }

    fun clone() {

    }

    fun head(): TrackedStructure {
        TODO()
    }

    fun checkout(tag: String): TrackedStructure {
        TODO()
    }

    companion object {
        fun create(name: String): RvcRepository {
            TODO()
        }

        fun clone(url: String): RvcRepository {
            TODO()
        }
    }
}
