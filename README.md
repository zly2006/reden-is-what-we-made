# Reden is What We Made

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
> Planned

Micro-timing analysis and simulation: block update breakpoints (NC, PP, CU, BE), BED debugger, step-by-step update, step-by-step tick, update reset

## Original Intention

Provide a one-stop working environment for redstone machine developers and become the best redstone debugging and teaching tool.

## Build

Just run `./gradlew build` in the root directory of the project.

### Note: This project currently only support the yarn mapping! If you are using other mappings, use this mod in your development environment will crash the game!

You can contribute a compiler plugin and rewrite some thing like
