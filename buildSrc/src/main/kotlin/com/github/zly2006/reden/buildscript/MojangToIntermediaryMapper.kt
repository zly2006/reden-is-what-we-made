package com.github.zly2006.reden.build

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MemoryMappingTree
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

object MojangToIntermediaryMapper {
    private var mappingTree: MappingTree? = null

    /**
     * 初始化映射数据，从Fabric Maven获取intermediary映射文件
     */
    @Throws(IOException::class)
    fun initializeMappings(minecraftVersion: String) {
        // 尝试从本地缓存中找到映射文件
        val cacheDir = File(System.getProperty("user.home"), ".gradle/caches/reden-mapping")
        val mappingsFile = File(cacheDir, "intermediary-$minecraftVersion.tiny")

        if (!mappingsFile.exists()) {
            downloadIntermediaryMappings(minecraftVersion)
        }

        val tree = MemoryMappingTree()
        // 修正此处，将 mappingFile 转为 Path
        Paths.get(mappingsFile.absolutePath).let { path ->
            MappingReader.read(path, tree)
        }

        mappingTree = tree
        println("已加载 Minecraft $minecraftVersion 的映射数据")
    }

    /**
     * 下载intermediary映射文件
     */
    private fun downloadIntermediaryMappings(minecraftVersion: String) {
        val cacheDir = File(System.getProperty("user.home"), ".gradle/caches/reden-mapping")
        cacheDir.mkdirs()

        val mappingsFile = File(cacheDir, "intermediary-$minecraftVersion.tiny")

        try {
            // Fabric Maven intermediary mappings URL
            val downloadUrl = "https://maven.fabricmc.net/net/fabricmc/intermediary/$minecraftVersion/intermediary-$minecraftVersion-v2.jar"

            println("正在下载 Minecraft $minecraftVersion 的 intermediary 映射文件...")

            // 下载jar文件
            val jarFile = File(cacheDir, "intermediary-$minecraftVersion-v2.jar")
            URI(downloadUrl).toURL().openStream().use { input ->
                Files.copy(input, jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }

            // 从jar中提取tiny文件
            ZipFile(jarFile).use { zipFile ->
                val entry = zipFile.getEntry("mappings/mappings.tiny")
                    ?: throw IOException("找不到mappings.tiny文件")

                zipFile.getInputStream(entry).use { input ->
                    Files.copy(input, mappingsFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }

            jarFile.delete() // 清理临时jar文件

        } catch (e: Exception) {
            throw IOException("下载 Minecraft $minecraftVersion 的 intermediary 映射文件失败", e)
        }
    }

    /**
     * 将Mojang官方方法名映射到intermediary名称
     */
    fun mapToIntermediary(mojangMethodName: String): String {
        val tree = mappingTree ?: throw IllegalStateException("映射尚未初始化，请先调用 initializeMappings()")

        val parts = mojangMethodName.split(".")
        if (parts.size < 2) {
            throw IllegalArgumentException("无效的方法名格式: $mojangMethodName")
        }

        val methodName = parts.last()
        val className = parts.dropLast(1).joinToString("/") // 使用斜杠分隔符

        // 在映射树中查找类
        val classMapping = tree.classes.find { clazz ->
            val officialName = clazz.getName("official")
            val namedName = clazz.getName("named")
            officialName == className || namedName == className ||
            officialName == className.replace("/", ".") || namedName == className.replace("/", ".")
        } ?: throw NoSuchElementException("找不到类: $className")

        // 查找匹配的方法
        val matchingMethods = classMapping.methods.filter { method ->
            val officialName = method.getName("official")
            val namedName = method.getName("named")
            officialName == methodName || namedName == methodName
        }

        when (matchingMethods.size) {
            0 -> throw NoSuchElementException("找不到方法: $mojangMethodName")
            1 -> {
                val method = matchingMethods.first()
                val intermediaryClassName = classMapping.getName("intermediary")?.replace("/", ".")
                    ?: className.replace("/", ".")
                val intermediaryMethodName = method.getName("intermediary") ?: methodName
                return "$intermediaryClassName.$intermediaryMethodName"
            }
            else -> throw IllegalArgumentException(
                "找到多个匹配的方法 $mojangMethodName (${matchingMethods.size} 个重载方法)。" +
                "请指定方法签名以解决歧义。"
            )
        }
    }

    /**
     * 批量映射方法名
     */
    fun mapMethodsFromFile(inputFile: File, outputFile: File, minecraftVersion: String) {
        println("正在初始化映射数据...")
        initializeMappings(minecraftVersion)

        val inputLines = inputFile.readLines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }

        val outputLines = mutableListOf<String>()
        val errors = mutableListOf<String>()

        println("开始映射 ${inputLines.size} 个方法名...")

        for ((index, line) in inputLines.withIndex()) {
            try {
                val intermediaryName = mapToIntermediary(line)
                outputLines.add("$line -> $intermediaryName")
                println("✓ [${index + 1}/${inputLines.size}] $line -> $intermediaryName")
            } catch (e: Exception) {
                val errorMsg = "✗ [${index + 1}/${inputLines.size}] 映射失败 $line: ${e.message}"
                errors.add("$line: ${e.message}")
                println(errorMsg)
            }
        }

        // 创建输出目录
        outputFile.parentFile?.mkdirs()

        // 写入输出文件
        outputFile.writeText(outputLines.joinToString("\n"))

        // 如果有错误，写入错误报告
        if (errors.isNotEmpty()) {
            val errorFile = File(outputFile.parent, "${outputFile.nameWithoutExtension}-errors.txt")
            errorFile.writeText(errors.joinToString("\n"))
            println("错误报告已写入: ${errorFile.absolutePath}")
        }

        println()
        println("映射完成！")
        println("结果已写入: ${outputFile.absolutePath}")
        println("成功映射: ${outputLines.size}/${inputLines.size} 个方法")
        if (errors.isNotEmpty()) {
            println("失败: ${errors.size} 个方法")
        }
    }
}
