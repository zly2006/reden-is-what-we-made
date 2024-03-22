# Reden is What We Made

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/reden?style=flat-square&label=Modrinth)](https://modrinth.com/mod/reden)
[![Discord](https://img.shields.io/discord/1140304794976792707?logo=discord&label=discord)](https://discord.gg/fCxmEyFgAd)
[Official Website](https://redenmc.com)

Redstone EDEN

**English** | [简体中文](./README.zh-CN.md)

## Undo & Redo

Press Ctrl+Z to immediately undo the previous operation, making it much more convenient to develop redstone machines!
For more information, please check [our wiki page](https://wiki.redenmc.com/Undo-and-Redo).

All blocks, block entities and entities are undoable.
Undo is available only if that change is caused by a player, for example, placing blocks and commands.

## RVC
> WIP

RVC, Redstone Version Control, machine history and diff analysis based on git.

RVC can upload your machines to GitHub if you have bound your GitHub account at https://redenmc.com.
RVC supports subregions and submodules, so you can use other people's work in your own creations.

RVC brings a totally new way of selecting blocks that is much easier.
You can hold a blaze rod in your hand, then left click to select a group of blocks, and right click to ignore.

## RDebugger
> WIP

Micro-timing analysis and simulation: block update breakpoints (NC, PP, CU, BE), BED debugger, step-by-step update, step-by-step tick, update reset

## Our Wishes

Provide a one-stop working environment for redstone machine developers and become the best redstone debugging and teaching tool.

## Other Features

+ Command Hotkeys: `Super Right -> runCommand` use masa-style hotkeys to run commands
+ Force Sync Entity Pos: `Micro Ticking -> toggleForceEntityPosSync` force sync entity pos to clients, maybe useful when you freeze the game
+ No Time Out: `General -> noTimeout` disable timeout on clients, if you are debugging the server, you probably need this
+ Item Shadowing Detector: carpet `redenDebuggerItemShadow` Detect if there are shadowed items in the inventory, wip, **it will support to disable operations that may break the link in the future**
+ Structure Block Hotkey: `Ctrl+S` to save and `Ctrl+L` to load for you last interacted struct block.

## Bug fixes

+ carpet `fixInvisibleShadowingItems`: fix invisible shadowing item entity, for more information, check [Igna's video](https://www.youtube.com/watch?v=HSOSWHIg7Mk)

## Build

Just run `./gradlew build` in the root directory of the project.

## Debugging

Debug Properties:

| Property Name                     | Description                                                                 |
|-----------------------------------|-----------------------------------------------------------------------------|
| `reden.transformer.printBytecode` | Print final bytecode to stdout. This process runs at mixin postApply stage. |
| `reden.transformer.export.pre`    | Export transformed class. This process runs at mixin preApply stage.        |

## 🎊 Thanks

> <span style="font-size: 0.96em">**IntelliJ IDEA**</span><br/>Capable and Ergonomic IDE for JVM

Special Thanks to [JetBrains](https://www.jetbrains.com/) for providing us free Licenses for Open Source Development for IDEs such as [IntelliJ IDEA](https://www.jetbrains.com/idea/)

[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" height="96"/>](https://www.jetbrains.com/)
[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA.png" height="96"/>](https://www.jetbrains.com/idea/)

<sup>Copyright © 2000-2024 JetBrains s.r.o. JetBrains and the JetBrains logo are registered trademarks of JetBrains s.r.o.</sup>
<br/>
<sup>Copyright © 2024 JetBrains s.r.o. IntelliJ IDEA and the IntelliJ IDEA logo are registered trademarks of JetBrains s.r.o.</sup>
