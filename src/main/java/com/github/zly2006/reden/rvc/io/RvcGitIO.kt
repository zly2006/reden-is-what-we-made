package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import java.nio.file.Path

object RvcGitIO: StructureIO {
    override fun save(path: Path, structure: IStructure) {
        // Preform git push operation
        TODO("Not yet implemented")
    }

    override fun load(path: Path, structure: IWritableStructure) {
        // Read git blob object
        TODO("Not yet implemented")
    }

    fun push(/*We'll have to decide what are the parameters based on the git library after we set one up*/) {
        // Preform git push operation
        TODO("Not yet implemented")
    }

    fun pull(/*We'll have to decide what are the parameters based on the git library after we set one up*/) {
        // Preform git pull operation
        // We have to deal with conflicts with saved data
        TODO("Not yet implemented")
    }

    fun sync(path: Path, structure: IWritableStructure /*We might need to add other git related params*/) {
        // This is the sync order used by Visual Studio 2022 sync action
        // Reference: https://learn.microsoft.com/en-us/visualstudio/version-control/git-fetch-pull-sync?view=vs-2022#sync
        // We might need to rethink about this order,
        // or if we are going to do this at all (operations executed, or this function at all)
        save(path, structure)
        pull()
        load(path, structure)
        save(path, structure)
        push()
    }
}
