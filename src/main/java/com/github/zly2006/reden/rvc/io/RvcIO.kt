package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import java.nio.file.Path

class RvcIO(val rvcFileIO: RvcFileIO, val rvcGitIO: RvcGitIO) {
    fun save(path: Path, structure: IStructure) {
        TODO("Not yet implemented")
//        rvcFileIO.save(path, structure)
//        rvcGitIO.save(path, structure)
    }

    fun load(path: Path, structure: IWritableStructure) {
        TODO("Not yet implemented")
//        rvcFileIO.load(path, structure)
//        rvcGitIO.load(path, structure)
    }
}
