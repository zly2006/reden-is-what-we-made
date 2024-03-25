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
So, RVC machines can be shared easily with others, all you need to do is to copy a GitHub link.
RVC supports subregions and submodules, so you can use other people's work in your own creations.

### RVC: Selection

> Almost done

RVC brings a totally new way of selecting blocks that is much easier.
You can hold a blaze rod in your hand, then left click to select a group of blocks, and right click to ignore.

### RVC Package Manager

> Planing

RVC Package Manager is a package manager for redstone machines, it can help you:

+ Manage the dependencies of your machines
+ Update the dependencies to the latest version
+ Publish your machines to the RVC repository
+ Allow other people to use your machines with ease
+ Give credit to everyone who contributes to your machines

It uses git submodules to manage the dependencies, so you can use other people's work in your own creations.

### RVC: Activity

> Planing

When you link your GitHub account, reden can automatically track your activities on GitHub,
such as creating a new repository, pushing a new commit, and creating a new release.
And your activities will be sent to your followers in the game, so people can get notified when their dependencies are
updated.

### RVC: Reference

> WIP

For many fields such as storage tech, people will make some slices that,
for example, each slice can filter a single type of item.

However, when you want to test them, you have to stack them together, which is very inconvenient.
RVC Reference can help you to place a "reference" of a slice, once you finished your slice,
you can press a hotkey, and the reference will be updated to the latest version of the slice.
Happy debugging!

## RDebugger
> WIP

Micro-timing analysis and simulation: block update breakpoints (NC, PP, CU, BE), BED debugger, step-by-step update, step-by-step tick, update reset

### RVC with RDebugger

> Planning

R-Debugger can be used with RVC, you can configure a flow to debug your machine,
and the flow will be saved in the RVC history.
A flow might look like this, for example:

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
