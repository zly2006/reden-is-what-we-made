package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.io.StructureIO
import com.github.zly2006.reden.utils.isClient
import net.minecraft.client.MinecraftClient
import org.eclipse.jgit.api.Git
import java.nio.file.Path

/**
 * Perform git operations on RVC files operated by [RvcFileIO] saving and loading [TrackedStructure]s.
 */
object RvcGitIO: StructureIO {
    override fun save(path: Path, structure: IStructure) {
        commit(path, structure, RvcFileIO, "RVC file saved by RVC Git IO")
        // todo: Preform git push operation (require discussion)
    }

    fun commit(path: Path, structure: IStructure, io: StructureIO, message: String) {
        val environment = if (!isClient) {
            "Server"
        } else {
            val mc = MinecraftClient.getInstance()
            if (mc.isInSingleplayer) {
                "Singleplayer"
            } else {
                "Multiplayer"
            }
        }
        Git.open(path.toFile()).use {
            io.save(path, structure)
            it.add().addFilepattern(".").call()
            it.commit()
                .setAuthor("PlayerName", "id@hub.redenmc.com")
                .setMessage("""
                    $message
                    
                    ======BEGIN RVC COMMIT DATA======
                    Structure-Name: ${structure.name}
                    Structure-Size: ${structure.xSize}x${structure.ySize}x${structure.zSize}
                    Platform: Reden Mod
                    Reden-Environment: $environment
                    Reden-Version: ${RvcFileIO.CURRENT_VERSION}
                    MC-Username: ${MinecraftClient.getInstance().session.username}
                    MC-UUID: ${MinecraftClient.getInstance().session.uuid}
                    ======END RVC COMMIT DATA======
                """.trimIndent()).call()
        }
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
