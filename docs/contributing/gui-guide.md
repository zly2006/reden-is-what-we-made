# Reden GUI Contributing Guide

1. Use kotlin, and format your source code by the official style.
2. If you want to get a component, store in a `val`, dont get it from `children()`

   For example, instead of:
   ```kotlin
   val flow = Containers.verticalFlow() //...
   flow.child(Components.button()) //...
   
   // Do not
   (flow.children().first() as Button).text(Text.of("Click me"))
   ```
   Do:
   ```kotlin
   val flow = Containers.verticalFlow() //...
   val button = Components.button() // a peoperty in the class
   flow.child(button) // in init or apply block
   ```

3. For identifiers, use `Reden.identifier()`
4. configure the component in the `apply` block

   For example, instead of:
   ```kotlin
   val button = Components.button()
   
   init {
    button.text(Text.of("Click me"))
   }
   ```
   Do:
   ```kotlin
   val button = Components.button().apply {
     text(Text.of("Click me"))
   }
   ```
