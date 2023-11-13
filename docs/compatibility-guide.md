# Compatibility Guide for Reden Developers

Reden is a powerful mod that provides a lot of features for creative mode QoL.
However, it has nearly **rewritten all minecraft low-level code** to achieve this.

**Reden can even change the vanilla control flow**, which means it will likely break other mods.

If you want your mod to be compatible with Reden, here's some suggestions.

## 0. Uninstall Reden

You can also disable RDebugger in Reden's config file (remember to restart the server to take effect).

## 1. Avoid capturing locals as much as possible

## 2. Avoid At.Shift.AFTER

Reden promise that all At.Shift.BEFORE and BY will be executed before the vanilla stuff runs,
but if you enabled reden debugger, At.Shift.AFTER can also be executed before them.

## 3. HEAD / TAIL

Reden do not promise that HEAD and TAIL will be executed only once during that call. (Because I am lazyyy :/ )
**This is a bug of Reden**, please report!

If you find a bug, please report it in issues, then I will fix that injection point if reden.
There is no technical reason to not do that, but I am lazyyy :/

## 4. Feel free to use @Redirect

Although At.Shift.AFTER was broken, @Redirect is still working fine.

## 5. Do not use @Overwrite that conflicts with Reden's

