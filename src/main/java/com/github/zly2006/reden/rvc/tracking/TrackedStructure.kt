package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.tracking.network.NetworkWorker
import com.github.zly2006.reden.utils.redenError
import net.minecraft.world.World

class TrackedStructure(
    override var name: String,
    val repository: RvcRepository?,
) : IWritableStructure, IPlacement {
    var networkWorker: NetworkWorker? = null
    override var enabled: Boolean = true
    override val structure get() = this
    val regions = mutableMapOf<String, TrackedStructurePart>()
    override val xSize: Int
        get() = TODO("Not yet implemented")
    override val ySize: Int
        get() = TODO("Not yet implemented")
    override val zSize: Int
        get() = TODO("Not yet implemented")

    /**
     * This is stored in the file `.git/placement_info.json`.
     *
     * When we cloned or created a repository, remember to create this file.
     *
     * @see RvcRepository.placementInfo
     */
    var placementInfo: PlacementInfo? = null
    override val world: World
        get() = if (networkWorker?.world?.info == placementInfo?.worldInfo)
            networkWorker?.world ?: redenError("World is not set for $name")
        else placementInfo?.worldInfo?.getWorld() ?: redenError("World is not found: $name")

}
