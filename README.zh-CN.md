# Reden is What We Made

[![Modrinth 下载](https://img.shields.io/modrinth/dt/reden?style=flat-square&label=Modrinth)](https://modrinth.com/mod/reden)
[![Discord](https://img.shields.io/discord/1140304794976792707?logo=discord&label=discord)](https://discord.gg/fCxmEyFgAd)
[![Curse forge 下载](https://cf.way2muchnoise.eu/reden.svg)](https://www.curseforge.com/minecraft/mc-mods/reden)
[官方网站](https://redenmc.com)

红石天堂

[English](./README.md) | **简体中文**

## 撤销&重做

按 Ctrl+Z 可以立即撤销上一步操作，让红石机器的开发方便许多！

所有方块、方块实体和实体都可以撤销。
只有玩家造成的变化才能撤销，例如放置方块和执行命令。

## RVC
> 开发中

RVC（Redstone Version Control，红石版本控制），基于 git 的机器历史记录与差异分析。

如果你在 https://redenmc.com 绑定了 GitHub 账号，RVC 可以把你的机器上传到 GitHub。
这样机器就能轻松分享给别人，你只需要复制一个 GitHub 链接。
RVC 支持子区域和子模块，因此你可以在自己的作品中复用别人的成果。

### RVC 选择

> 即将完成

RVC 带来了一种全新、更简单的方块选择方式。
手持烈焰棒，左键点击选中一组方块，右键点击忽略。

### RVC 包管理器

> 规划中

RVC 包管理器是一个面向红石机器的包管理器，它可以帮你：

+ 管理机器的依赖
+ 把依赖更新到最新版本
+ 把你的机器发布到 RVC 仓库
+ 让别人轻松使用你的机器
+ 为每一位贡献者署名

它使用 git 子模块来管理依赖，因此你可以在自己的作品中复用别人的成果。

### RVC 活动

> 规划中

当你关联 GitHub 账号后，reden 可以自动追踪你在 GitHub 上的活动，
例如新建仓库、推送新提交、发布新版本。
这些活动会发送给游戏内的关注者，这样当他们的依赖更新时就能收到通知。

### RVC 引用

> 开发中

在存储技术等许多领域中，人们会制作一些切片（slice），例如每个切片负责过滤一种物品。

但在测试时，你必须把它们堆叠起来，非常不方便。
RVC 引用（RVC Reference）可以帮你放置一个切片的「引用」，当你完成切片后，
按下一个快捷键，引用就会更新到该切片的最新版本。
祝你调试愉快！

## RDebugger
> 开发中

微时序分析与模拟：方块更新断点（NC、PP、CU、BE），BED 调试器，逐步更新，逐步刻，更新重置

### RVC 与 RDebugger 配合

> 规划中

RDebugger 可以与 RVC 配合使用，你可以配置一个流程（flow）来调试你的机器，
该流程会保存在 RVC 历史记录中。
一个流程的示例如下：

```
[steps]
Use: button at 0 0 0
Use: lever  at 1 3 5
Fill-Area:  name=area1  from=0 0 0  to=10 10 10  with=stone
Wait: 10 ticks

[check]
Require: block at 0 0 0   is stone
Require: block at 1 3 5   is lever
Has-Item:chest at 0 0 0   has 1 diamond
```

## 初心

为红石机器开发者提供一站式工作环境，成为最好的红石调试与教学工具。

## 其他特性

+ 命令快捷键：`Super Right -> runCommand`，使用 masa 风格的快捷键运行命令
+ 强制同步实体位置：`Micro Ticking -> toggleForceEntityPosSync`，强制把实体位置同步到客户端，冻结游戏时可能会用到
+ 禁用超时：`General -> noTimeout`，在客户端禁用超时，如果你在调试服务端，大概会需要它
+ 物品分身探测器：carpet `redenDebuggerItemShadow`，检测背包中是否存在分身物品，开发中，**未来将支持禁用可能破坏链接的操作**
+ 结构方块快捷键：`Ctrl+S` 保存、`Ctrl+L` 加载你最后交互的结构方块。

## 构建

只需在项目根目录下运行 `./gradlew build`。

## 调试

调试属性：

| 属性名                               | 描述                                          |
|-----------------------------------|---------------------------------------------|
| `reden.transformer.printBytecode` | 把最终字节码打印到标准输出。该过程在 mixin postApply 阶段运行。 |
| `reden.transformer.export.pre`    | 导出转换后的类。该过程在 mixin preApply 阶段运行。        |
