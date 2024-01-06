package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.remote.IRemoteRepository
import com.github.zly2006.reden.utils.ResourceLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.InitCommand
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
        RvcFileIO.save(git.repository.workTree.toPath(), structure)
        git.add().addFilepattern("*.rvc").call()
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

    fun head(): TrackedStructure {
        if (headCache == null)
            headCache = checkoutBranch(RVC_BRANCH)
        // todo: this line is debug only
        headCache!!.world = MinecraftClient.getInstance().world!!
        return headCache!!
    }

    fun checkout(tag: String) = TrackedStructure(name).apply {
        git.checkout().setName(tag).setForced(true).call()
        RvcFileIO.load(git.repository.workTree.toPath(), this)
    }

    fun checkoutBranch(branch: String) = checkout("refs/heads/$branch")

    companion object {
        val path = Path("rvc")
        const val RVC_BRANCH = "rvc"
        fun create(name: String, description: String? = null): RvcRepository {
            val git = Git.init()
                .setDirectory(path / name)
                .setInitialBranch(RVC_BRANCH)
                .call()
            git.repository.workTree.resolve("README.md").writeText(
                ResourceLoader.loadString("assets/rvc/README.md")
                    .replace("\${name}", name)
                    .replace("\${description}", description ?: "")
            )
            git.add().addFilepattern("README.md").call()
            git.commit()
                .setMessage("Initial commit")
                .setAuthor("Reden-RVC", "info@redenmc.com")
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
