package com.github.zly2006.reden.indexing

import java.io.File

interface Index<T> {
    fun of(identifier: T): Int
    fun checkExtra(output: File?): List<T>
}
