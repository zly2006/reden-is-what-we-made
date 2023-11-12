# Reden is What We Made

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/reden?style=flat-square&label=Modrinth)](https://modrinth.com/mod/reden)

Redstone EDEN

**English** | [简体中文](./README.zh-CN.md)

## Undo & Redo

Ctrl+Z immediately undo the previous operation, making it much more convenient to develop redstone machines!

## Tick Back
> Early Access Stage

If your game was frozen using `/tick freeze`, you can use `/tick back` to go back to the previous tick. This feature is implemented by making backups.

## RVC
> WIP

RVC, Redstone Version Control, machine history and diff analysis

RVCHub, a universal machine sharing platform, and automatic recognition of whether the machine has been correctly marked with copyright

## RDebugger
> WIP

Micro-timing analysis and simulation: block update breakpoints (NC, PP, CU, BE), BED debugger, step-by-step update, step-by-step tick, update reset

## Original Intention

Provide a one-stop working environment for redstone machine developers and become the best redstone debugging and teaching tool.

## Other Features

+ Command Hotkeys: `Super Right -> runCommand` use masa-style hotkeys to run commands
+ Force Sync Entity Pos: `Micro Ticking -> toggleForceEntityPosSync` force sync entity pos to clients, maybe useful when you freeze the game
+ No Time Out: `General -> noTimeout` disable timeout on clients, if you are debugging the server, you probably need this
+ Item Shadowing Detector: carpet `redenDebuggerItemShadow` Detect if there are shadowed items in the inventory, wip, **it will support to disable operations that may break the link in the future**

## Bug fixes

+ carpet `fixInvisibleShadowingItems`: fix invisible shadowing item entity, for more information, check [Igna's VVideo](https://www.youtube.com/watch?v=HSOSWHIg7Mk)

## Build

Just run `./gradlew build` in the root directory of the project.

## Debugging

Debug Properties:

| Property Name                     | Description                                                                 |
|-----------------------------------|-----------------------------------------------------------------------------|
| `reden.transformer.printBytecode` | Print final bytecode to stdout. This process runs at mixin postApply stage. |
| `reden.transformer.export.pre`    | Export transformed class. This process runs at mixin preApply stage.        |
