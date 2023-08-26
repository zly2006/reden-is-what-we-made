package com.github.zly2006.reden.rvc.tracking.reader

import com.github.zly2006.reden.rvc.tracking.IRvcFileReader
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import java.io.BufferedInputStream

class RvcReaderV1(
    header: IRvcFileReader.RvcHeader
): IRvcFileReader {
    override fun read(content: BufferedInputStream, structure: TrackedStructure) {
        TODO("Not yet implemented")
    }
}