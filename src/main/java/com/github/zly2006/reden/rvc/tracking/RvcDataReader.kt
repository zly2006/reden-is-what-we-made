package com.github.zly2006.reden.rvc.tracking

/**
 * Separate and get delimiter separated data read from a RVC file using [RvcFileIO].
 */
class RvcDataReader(val data: String, val delimiters: String): Iterator<String> {
    private var index = 0

    /**
     * @return The string from the current index to the end of the data.
     */
    fun readGreedy(): String {
        val string = data.substring(index)
        index = data.length
        return string
    }

    /**
     * @return The string between the current index and the next delimiter.
     */
    override fun next(): String {
        val endIndex = data.indexOf(delimiters, index)
            .takeIf { it >= 0 } ?: data.length
        val next = data.substring(index, endIndex)
        index += next.length + delimiters.length
        return next
    }

    override fun hasNext(): Boolean {
        return index < data.length
    }
}
