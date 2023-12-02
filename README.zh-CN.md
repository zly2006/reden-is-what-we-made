# Reden 是我们创造的

[![Modrinth 下载](https://img.shields.io/modrinth/dt/reden?style=flat-square&label=Modrinth)](https://modrinth.com/mod/reden)

红石伊甸园

[English](./README.md) | **简体中文**]

## 撤销与重做

Ctrl+Z 可以立即撤销之前的操作，使得开发红石机器更加方便！Ctrl+Y可以重做。
更多信息请参阅[wiki页面](https://wiki.redenmc.com/Undo-and-Redo)

## 倒退刻
> 早期访问阶段

如果你的游戏因为使用 `/tick freeze` 而冻结，你可以使用 `/tick back` 来回到上一个刻。这个功能是通过备份实现的。

## RVC
> 开发中

RVC，红石版本控制，机器历史和差异分析

RVCHub，一个通用的机器分享平台，以及自动识别机器是否正确标记了版权

## RDebugger
> 开发中

微时间分析和模拟：方块更新断点 (NC, PP, CU, BE)，BED 调试器，逐步更新，逐步刻，更新重置

让你的 MC 变成红石机器的 IDE！

## 初衷

为红石机器开发者提供一站式工作环境，成为最佳的红石调试和教学工具。

## 其他特性

+ 命令快捷键：`Super Right -> runCommand` 使用 masa 风格的快捷键运行命令
+ 强制同步实体位置：`Micro Ticking -> toggleForceEntityPosSync` 强制同步实体位置到客户端，当你冻结游戏时可能会有用
+ 无超时：`General -> noTimeout` 在客户端禁用超时，如果你在调试服务器，可能需要这个
+ 物品分身探测器：carpet `redenDebuggerItemShadow` 检测是否有分身物品在库存中，开发中，**未来它将支持禁用可能破坏链接的操作**
+ 快速结构方块操作：`Ctrl+L`可以执行加载操作，`Ctrl+S`可以执行保存操作。操作对象是最后一次交互的结构方块

## Bug 修复

+ carpet `fixInvisibleShadowingItems`：修复不可见的分身物品实体，更多信息请查看 [Igna 的视频](https://www.youtube.com/watch?v=HSOSWHIg7Mk)

## 构建

只需在项目根目录下运行 `./gradlew build`。

## 调试

调试属性：

| 属性名                               | 描述                                       |
|-----------------------------------|------------------------------------------|
| `reden.transformer.printBytecode` | 打印最终字节码到标准输出。这个过程在 mixin postApply 阶段运行。 |
| `reden.transformer.export.pre`    | 导出转换后的类。这个过程在 mixin preApply 阶段运行。       |