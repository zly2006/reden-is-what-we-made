# Coding Conventions

This file introduces some basic concepts and reden-project-specific stuff that you should know before contributing.

## Code Style

### GUI

Our gui depends on owo lib, so all gui class should be an owo component.

A GUI class should be named `XxxScreen` or `XxxComponent` if it's a component.

A GUI class includes of the following parts:

+ **Definitions**, all children components should be defined here using `val` or `var` if you believe it should be
  mutable.

  except the wrappers (e.g. scroll container) and immutable components.

  set their initial style and listeners such as onClick here.

+ **Properties**, the properties of the gui, they may have a custom setter.
  > [!NOTE]
  >
  > We prefer custom setters for related properties than to set them manually each time.
  > Please specify the proper access modifier for the property.

+ **Initialization**, the build function

## Knowledge

### Kotlin

Reden is written mainly in kotlin, except some entrypoint and mixins.

Goto <https://kotlinlang.org>

### Conditional mixin (the `otherMods` package)

Mixins in the `otherMods` package are only loaded if the specified mod is present.
For example, the `otherMods/jei` package contains mixins that are only loaded if JEI is present.

<details>
<summary> Code </summary>

> [!TIP]
>
> See: com.github.zly2006.reden.transformers.RedenMixinExtension.shouldApplyMixin

</details>

### Disable litematica time check for chunk building

`-Dreden.ignoreLitematicaTaskTime=true`
