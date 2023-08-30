package com.github.zly2006.reden.rvc.tracking

/**
 * Separate and get delimiter separated data read from a RVC file using [RvcFileIO].
 */
class RvcDataReader (val data: String, val delimiters: String) {
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
    fun readNext(): String {
        val next = data.substring(index, data.indexOf(delimiters, index))
        index += next.length + delimiters.length
        return next
    }
}
