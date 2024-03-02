# Coding Conventions

This file introduces some basic concepts and reden-project-specific stuff that you should know before contributing.

### Kotlin

Reden is written mainly in kotlin, except some entrypoint and mixins.

Goto <https://kotlinlang.org>

### Conditional mixin (the `otherMods` package)

Mixins in the `otherMods` package are only loaded if the specified mod is present.
For example, the `otherMods/jei` package contains mixins that are only loaded if JEI is present.

<details>
<summary> Code </summary>

> [!TIP]
> See: com.github.zly2006.reden.transformers.RedenMixinExtension.shouldApplyMixin

</details>

### Disable litematica time check for chunk building

`-Dreden.ignoreLitematicaTaskTime=true`
