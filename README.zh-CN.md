# Reden is What We Made

[![从 Modrinth 下载](https://img.shields.io/modrinth/dt/reden?style=flat-square&label=Modrinth)](https://modrinth.com/mod/reden)
[![加入 Discord 服务器](https://img.shields.io/discord/1140304794976792707?logo=discord&label=discord)](https://discord.gg/fCxmEyFgAd)

红 石 天 堂

[English](./README.md) | **简体中文**

## 在您开始使用前
请先阅读我们的[隐私政策](./PRIVACY.md)。

此项目依照***LGPL v3.0***协议开源。

## 撤销&重做

Ctrl+Z 可以立即撤销之前的操作，使得开发红石机器更加方便！Ctrl+Y 可以重做。
更多信息请参阅 [wiki页面](https://wiki.redenmc.com/Undo-and-Redo)


|  快捷键 (malilib -> `General`) |    默认     |
|---------------------------------|----------------|
|                          `撤销` |    `Ctrl+Z`    |
|                          `重做` |    `Ctrl+Y`    |
|         (调试模式) `预览调试撤回` | `Ctrl+Shift+Z` |


## 控制游戏刻 （tick）
Reden 提供了 `/tick <function>` 指令，用于方便快捷地控制游戏刻。

### 功能（`<function>` 参数）
- `freeze` 冻结游戏刻
- `query` 显示当前的TPS信息
- `rate <tps>` 调整TPS（默认为`20.0`）
- `sprint <1d|3d|60s|stop>` 加速游戏刻 
- `step` 向前执行一个游戏刻（必须在游戏冻结时才能使用）
- `unfreeze` 解冻游戏刻
- `back` 回到上一个刻  ***规划中***


## 红石版本控制（RVC）
> 开发中，以下列出的功能已实现

![image](https://github.com/ArthurZhou/reden-is-what-we-made/assets/89689293/b2d5f6de-1860-496e-8980-b4a2cd3abe95)

红石版本控制 RVC (Redstone Version Control)，基于 Git 开发，可以帮助您管理机器的修改历史，并分析历史记录之间的差异。按下`R+L`即可快速打开RVC界面。

### 提交或更新您的仓库
***注意：请不要直接使用`R+G`快捷键登录Github [Issue #103](https://github.com/zly2006/reden-is-what-we-made/issues/103)***
如果您的 Reden 账户绑定了 GitHub（您可在[官网](https://redenmc.com/home/edit)完成此操作），RVC 可以将您的机器上传到 GitHub。 RVC 支持子区域和子模块，因此您可以在自己的创作中使用其他人的作品。

首次使用需要登录 Reden 账号，登录完成后将会显示如下界面：

![image](https://github.com/ArthurZhou/reden-is-what-we-made/assets/89689293/f4bf3dee-9eca-4d0b-bada-82ced5fd6745)


随后，您可像使用git那样操作您的仓库

![image](https://github.com/ArthurZhou/reden-is-what-we-made/assets/89689293/de014ed2-e7b9-44d8-b100-9ab9d54523c4)

### 选择区域
RVC 带来了一种全新的、更简单的区域选择方式。你只需要手持一根烈焰棒，左键单击选择区域，右键单击忽略。

![image](https://github.com/ArthurZhou/reden-is-what-we-made/assets/89689293/bf1cca8a-e8e2-4c41-8ef3-a5e78b536935)


## 红石调试 (RDebugger)
> 开发中

红石调试 RDebugger 提供了微时间分析和模拟功能，例如方块更新断点 (NC, PP, CU, BE)，BED 调试器，逐步更新，逐步刻，更新重置等


## 我们的目标

为红石机器开发者提供一站式工作环境，成为最佳的红石调试和教学工具。

## 其他特性

+ 命令快捷键：`Super Right -> runCommand` 使用 masa 风格的快捷键运行命令
+ 强制同步实体位置：`Micro Ticking -> toggleForceEntityPosSync` 强制同步实体位置到客户端，当你冻结游戏时可能会有用
+ 禁用超时：`General -> noTimeout` 在客户端禁用超时，如果你在调试服务器，可能需要这个
+ 物品分身探测器：carpet `redenDebuggerItemShadow` 检测是否有分身物品在库存中，开发中，**未来它将支持禁用可能破坏链接的操作**
+ 快速结构方块操作：`Ctrl+L` 可以执行加载操作，`Ctrl+S` 可以执行保存操作。操作对象是最后一次交互的结构方块

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

## 🎊 鸣谢

> <span style="font-size: 0.96em">**IntelliJ IDEA**</span><br/>功能强大，符合人体工程学的 JVM IDE

特别感谢 [JetBrains](https://www.jetbrains.com/) 为我们提供免费的，[IntelliJ IDEA](https://www.jetbrains.com/idea/) 等 IDE 的开源开发许可证

[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" height="96"/>](https://www.jetbrains.com/)
[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA.png" height="96"/>](https://www.jetbrains.com/idea/)

<sup>Copyright © 2000-2024 JetBrains s.r.o. JetBrains and the JetBrains logo are registered trademarks of JetBrains s.r.o.</sup>
<br/>
<sup>Copyright © 2024 JetBrains s.r.o. IntelliJ IDEA and the IntelliJ IDEA logo are registered trademarks of JetBrains s.r.o.</sup>
