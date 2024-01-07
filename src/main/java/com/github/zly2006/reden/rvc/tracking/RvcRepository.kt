package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.remote.IRemoteRepository
import com.github.zly2006.reden.utils.ResourceLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.InitCommand
import org.jetbrains.annotations.Contract
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists

class RvcRepository(
    private val git: Git,
    val name: String = git.repository.workTree.name
) {
    var headCache: TrackedStructure? = null
        private set

    fun commit(structure: TrackedStructure, message: String, committer: PlayerEntity?) {
        headCache = structure
        this.createReadmeIfNotExists()
        val path = git.repository.workTree.toPath()
        RvcFileIO.save(path, structure)
        git.add().addFilepattern(".").call()
        val cmd = git.commit()
        if (committer != null) {
            cmd.setAuthor(committer.nameForScoreboard, committer.uuid.toString() + "@mc-player.redenmc.com")
        }
        cmd.setMessage("$message\n\nUser-Agent: Reden-RVC")
        cmd.call()
    }

    fun push(remote: IRemoteRepository) {
        git.push()
            .setRemote(remote.gitUrl)
            .setForce(false)
            .call()
    }

    fun fetch() {
        headCache = null
        TODO() // Note: currently we have no gui for this
    }

    @Contract(pure = true)
    fun hasChanged(): Boolean {
        RvcFileIO.save(
            git.repository.workTree.toPath(),
            headCache ?: return false
        )
        return !git.status().call().isClean
    }

    fun head(): TrackedStructure {
        if (headCache == null) {
            val refs = git.branchList().call()
            headCache = if (refs.isEmpty()) {
                TrackedStructure(name)
            } else if (refs.any { it.name == RVC_BRANCH_REF }) {
                checkoutBranch(RVC_BRANCH)
            } else {
                checkout(refs.first().name)
            }
        }
        // todo: this line is debug only
        headCache!!.world = MinecraftClient.getInstance().world!!
        return headCache!!
    }

    fun checkout(tag: String) = TrackedStructure(name).apply {
        git.checkout().setName(tag).setForced(true).call()
        RvcFileIO.load(git.repository.workTree.toPath(), this)
    }

    fun checkoutBranch(branch: String) = checkout("refs/heads/$branch")

    fun createReadmeIfNotExists() {
        git.repository.workTree.resolve("README.md").writeText(
            ResourceLoader.loadString("assets/rvc/README.md")
                .replace("\${name}", name)
        )
        git.add().addFilepattern("README.md").call()
    }

    companion object {
        val path = Path("rvc")
        const val RVC_BRANCH = "rvc"
        const val RVC_BRANCH_REF = "refs/heads/$RVC_BRANCH"
        fun create(name: String): RvcRepository {
            val git = Git.init()
                .setDirectory(path / name)
                .setInitialBranch(RVC_BRANCH)
                .call()
            return RvcRepository(git)
        }

        fun clone(url: String): RvcRepository {
            var name = url.split("/").last().removeSuffix(".git")
            var i = 1
            while ((path / name).exists()) {
                name = "$name$i"
                i++
            }
            return RvcRepository(
                Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(path / name)
                    .call()
            )
        }
    }
}

private fun CloneCommand.setDirectory(path: Path) = setDirectory(path.toFile())
private fun InitCommand.setDirectory(path: Path) = setDirectory(path.toFile())
