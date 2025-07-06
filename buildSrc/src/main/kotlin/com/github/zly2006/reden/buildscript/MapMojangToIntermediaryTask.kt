package com.github.zly2006.reden.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class MapMojangToIntermediaryTask : DefaultTask() {

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val minecraftVersion: Property<String>

    init {
        description = "将Mojang官方方法名映射到intermediary名称"
        group = "mapping"
    }

    @TaskAction
    fun mapMethods() {
        val input = inputFile.asFile.get()
        val output = outputFile.asFile.get()
        val mcVersion = minecraftVersion.get()

        logger.lifecycle("开始映射任务：")
        logger.lifecycle("输入文件: ${input.absolutePath}")
        logger.lifecycle("输出文件: ${output.absolutePath}")
        logger.lifecycle("Minecraft版本: $mcVersion")

        try {
            MojangToIntermediaryMapper.mapMethodsFromFile(input, output, mcVersion)
            logger.lifecycle("映射任务完成！")
        } catch (e: Exception) {
            logger.error("映射任务失败: ${e.message}", e)
            throw e
        }
    }
}
