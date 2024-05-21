package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.LoginRedenScreen
import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.ua
import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureLitematicaTask
import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureTask
import com.github.zly2006.reden.rvc.remote.IRemoteRepository
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.github.zly2006.reden.rvc.tracking.io.RvcFileIO
import com.github.zly2006.reden.rvc.tracking.network.ClientNetworkWorker
import com.github.zly2006.reden.rvc.tracking.network.LocalNetworkWorker
import com.github.zly2006.reden.rvc.tracking.network.NetworkWorker
import com.github.zly2006.reden.rvc.tracking.network.ServerNetworkWorker
import com.github.zly2006.reden.task.Task
import com.github.zly2006.reden.task.taskStack
import com.github.zly2006.reden.utils.ResourceLoader
import com.github.zly2006.reden.utils.litematicaInstalled
import com.github.zly2006.reden.utils.redenApiBaseUrl
import com.github.zly2006.reden.utils.redenError
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.minecraft.SharedConstants
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.NetworkSide
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import okhttp3.Request
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.InitCommand
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.jetbrains.annotations.Contract
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists

private var ghCredential = Pair("RedenMC", "")
    get() {
        if (field.second.isEmpty() || tokenUpdated + 600 * 1000 < System.currentTimeMillis()) {
            val obj = httpClient.newCall(Request.Builder().apply {
                ua()
                url("$redenApiBaseUrl/mc/git/github")
            }.build()).execute().use {
                if (it.code == 401) {
                    MinecraftClient.getInstance().setScreen(LoginRedenScreen())
                }
                require(it.isSuccessful) {
                    "Failed to get private key from Reden API: ${it.code} ${it.message}"
                }
                Json.decodeFromString<JsonObject>(it.body!!.string())
            }
            if ("token" !in obj || obj["token"] is JsonNull) {
                MinecraftClient.getInstance().setScreen(LoginRedenScreen())
            }
            field = Pair(obj["name"]!!.jsonPrimitive.content, obj["token"]!!.jsonPrimitive.content)
            tokenUpdated = System.currentTimeMillis()
        }
        return field
    }
private var tokenUpdated = 0L

@OptIn(ExperimentalSerializationApi::class)
class RvcRepository(
    internal val git: Git,
    val name: String = git.repository.workTree.name,
    val side: NetworkSide,
    val owner: ServerPlayerEntity? = null
) {
    var headCache: TrackedStructure? = null
        private set
    var remote = object : IRemoteRepository {
        override fun deleteRepo() {
            TODO("Not yet implemented")
        }

        override val gitUrl = git.repository.config.getString("remote", "origin", "url")
    }
    fun clearCache() {
        headCache = null
    }

    /**
     * At `.git/placement.json`
     */
    private val placementJson = git.repository.directory.resolve("placement.json")

    /**
     * @see TrackedStructure.placementInfo
     */
    var placementInfo: PlacementInfo? =
        if (placementJson.exists()) Json.decodeFromStream(placementJson.inputStream()) else null
        set(value) {
            field = value
            if (value != null) {
                placementJson.writeText(Json.encodeToString(value))
            }
            else {
                placementJson.delete()
            }
        }
    var placed = false

    class CommitResult(
        val totalBlocks: Int,
        val commitHash: String,
    )
    suspend fun commit(
        structure: TrackedStructure,
        message: String,
        committer: PlayerEntity?,
        author: PersonIdent? = null
    ): CommitResult {
        require(structure.repository == this) { "The structure is not from this repository" }
        require(structure.placementInfo != null) { "The structure is not placed in this world" }
        headCache = structure
        this.createReadmeIfNotExists()
        val path = git.repository.workTree.toPath()
        structure.refreshPositions()
        if (git.branchList().call().isEmpty()) {
            // if this is the first commit, reset the origin
            Reden.LOGGER.info("First commit, resetting origin")
            structure.placementInfo = structure.placementInfo!!.copy(origin = structure.minPos)
            structure.repository.placementInfo = structure.placementInfo
        }
        structure.collectAllFromWorld()
        RvcFileIO.save(path, structure)
        git.add().addFilepattern(".").call()
        val cmd = git.commit()
        if (committer != null && author == null) {
            cmd.setAuthor(committer.nameForScoreboard, committer.uuid.toString() + "@mc-player.redenmc.com")
        }
        if (author != null) {
            cmd.setAuthor(author)
        }
        if (committer != null) {
            cmd.setCommitter(committer.nameForScoreboard, committer.uuid.toString() + "@mc-player.redenmc.com")
        }
        cmd.setMessage("$message\n\nUser-Agent: Reden-RVC/${Reden.MOD_VERSION} Minecraft/${SharedConstants.getGameVersion().name}")
        cmd.setSign(false)
        val commit = cmd.call()
        return CommitResult(structure.totalBlocks, commit.name)
    }

    /**
     * @throws TransportException if github think you dont have permission, check github app installation stuff
     */
    fun push(remote: IRemoteRepository, force: Boolean = false) {
        val push = git.push()
            .setRemote(remote.gitUrl)
            .setForce(force)
            .setCredentialsProvider(UsernamePasswordCredentialsProvider(ghCredential.first, ghCredential.second))
            .call()
        push.forEach {
            it.peerUserAgent
        }
    }

    fun fetch() {
        headCache = null
        git.fetch().call()
    }

    @Contract(pure = true)
    fun hasChanged(): Boolean {
        RvcFileIO.save(
            git.repository.workTree.toPath(),
            headCache ?: return false
        )
        return !git.status().call().isClean
    }

    fun getNetworkWorker(worldInfo: WorldInfo, structure: TrackedStructure): NetworkWorker? {
        return when (side) {
            NetworkSide.CLIENTBOUND -> MinecraftClient.getInstance().run {
                if (getWorldInfo() == worldInfo) {
                    if (server != null) LocalNetworkWorker(
                        structure,
                        server!!.getWorld(world!!.registryKey)!!,
                        world!!
                    )
                    else ClientNetworkWorker(structure, world!!)
                }
                else null
            }

            NetworkSide.SERVERBOUND -> ServerNetworkWorker(structure, worldInfo.getWorld() as ServerWorld, owner!!)
        }
    }

    fun configure(structure: TrackedStructure) {
        if (placementInfo != null) {
            structure.placementInfo = placementInfo
            structure.networkWorker = getNetworkWorker(placementInfo!!.worldInfo, structure)
        }
    }

    @JvmOverloads
    fun head(configureCallback: (TrackedStructure) -> Unit = ::configure): TrackedStructure {
        try {
            if (headCache == null) {
                val refs = git.branchList().call()
                headCache =
                    if (refs.isEmpty()) TrackedStructure(name, this)
                    else if (refs.any { it.name == RVC_BRANCH_REF }) checkoutBranch(RVC_BRANCH, configureCallback)
                    else checkout(refs.first().name, configureCallback)
                configureCallback(headCache!!)
            }
            return headCache!!
        } catch (e: Exception) {
            redenError("Failed to load RVC head structure from repository ${this.name}", e, log = true)
        }
    }

    fun checkout(tag: String, configureCallback: (TrackedStructure) -> Unit) = TrackedStructure(name, this).apply {
        configureCallback(this)
        git.checkout().setName(tag).setForced(true).call()
        RvcFileIO.load(git.repository.workTree.toPath(), this)
    }

    fun checkoutBranch(branch: String, configureCallback: (TrackedStructure) -> Unit) =
        checkout("refs/heads/$branch", configureCallback)

    fun createReadmeIfNotExists() {
        git.repository.workTree.resolve("README.md").writeText(
            ResourceLoader.loadString("assets/rvc/README.md")
                .replace("\${name}", name)
        )
        git.add().addFilepattern("README.md").call()
    }

    fun createLicense(license: String, author: String? = null) {
        val year = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy"))
        val content = ResourceLoader.loadString(license)
            .replace("\${year}", year)
            .replace("\${year-start}", year)
            .replace("\${author}", author ?: "")

        if (git.repository.workTree.resolve("LICENSE").exists()) {
            redenError("LICENSE already exists")
        }
        git.repository.workTree.resolve("LICENSE").writeText(content)
    }

    fun setWorld() {
        clearCache()
        val mc = MinecraftClient.getInstance()
        placementInfo = PlacementInfo(mc.getWorldInfo())
    }

    fun delete() {
        val path = git.repository.workTree.toPath()
        git.close()
        if (path.exists()) {
            // java.nio doesn't remove the readonly attribute of file on Windows
            // use the java.io method instead
            path.toFile().deleteRecursively()
        }
    }

    fun updateModulus() {
        val path = git.repository.workTree.toPath()
        git.submoduleUpdate().call()
    }

    fun startPlacing(structure: TrackedStructure, successCallback: (Task) -> Unit = {}) {
        clearCache()
        val mc = MinecraftClient.getInstance()
        Task.all<RvcMoveStructureTask>().forEach { it.onCancel() }

        // Use temporary placement info to initialize the structure and its network worker
        placementInfo = PlacementInfo(mc.getWorldInfo(), BlockPos.ORIGIN)
        configure(structure)
        placementInfo = null

        taskStack.add(
            if (litematicaInstalled)
                RvcMoveStructureLitematicaTask(mc.getWorldInfo(), structure, successCallback)
            else
                RvcMoveStructureTask(mc.getWorldInfo(), structure, successCallback = successCallback)
        )
    }

    val headHash: String get() = git.repository.resolve("HEAD").name()
    val headBranch: String get() = git.repository.branch

    companion object {
        val path = Path("rvc")
        const val RVC_BRANCH = "rvc"
        const val RVC_BRANCH_REF = "refs/heads/$RVC_BRANCH"
        fun create(name: String, worldInfo: WorldInfo?, side: NetworkSide): RvcRepository {
            val git = Git.init()
                .setDirectory(path / name)
                .setInitialBranch(RVC_BRANCH)
                .call()
            return RvcRepository(git, side = side).apply {
                placementInfo = worldInfo?.let {
                    PlacementInfo(it, BlockPos.ORIGIN)
                }
                createReadmeIfNotExists()
            }
        }

        fun clone(url: String, side: NetworkSide): RvcRepository {
            var name = url.split("/").last().removeSuffix(".git")
            var i = 1
            while ((path / name).exists()) {
                name = "$name$i"
                i++
            }
            return RvcRepository(
                git = Git.cloneRepository()
                    .setCredentialsProvider(
                        UsernamePasswordCredentialsProvider(
                            ghCredential.first,
                            ghCredential.second
                        )
                    )
                    .setURI(url)
                    .setDirectory(path / name)
                    .call(),
                side = side
            )
        }

        fun fromArchive(worktreeOrGitPath: Path, side: NetworkSide): RvcRepository {
            return RvcRepository(
                git = Git.open(worktreeOrGitPath.toFile()),
                side = side
            )
        }
    }
}

private fun CloneCommand.setDirectory(path: Path) = setDirectory(path.toFile())
private fun InitCommand.setDirectory(path: Path) = setDirectory(path.toFile())
