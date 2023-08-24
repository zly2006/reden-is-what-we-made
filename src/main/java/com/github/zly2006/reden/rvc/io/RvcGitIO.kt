package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import java.nio.file.Path

object RvcGitIO: StructureIO {
    override fun save(path: Path, structure: IStructure) {
        // git commit & push action
        TODO("Not yet implemented")
    }

    override fun load(path: Path, structure: IWritableStructure) {
        // git pull action
        TODO("Not yet implemented")
    }
}
