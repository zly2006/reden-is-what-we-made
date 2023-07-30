package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import java.nio.file.Path

interface StructureIO {
    fun save(path: Path, structure: IStructure)
    fun load(path: Path, structure: IWritableStructure)
}