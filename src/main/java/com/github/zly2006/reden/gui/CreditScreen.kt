package com.github.zly2006.reden.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.report.key
import com.github.zly2006.reden.sponsor.SponsorScreen
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.AbstractSoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import net.minecraft.util.math.random.Random
import java.net.URI

class CreditScreen(val parent: Screen? = null): BaseOwoScreen<FlowLayout>() {
    object MikuSinging: AbstractSoundInstance(
        Reden.identifier("miku_miku"),
        SoundCategory.MUSIC,
        Random.create()
    )

    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
        rootComponent.verticalAlignment(VerticalAlignment.CENTER)
        rootComponent.child(Containers.horizontalFlow(
            Sizing.content(),
            Sizing.fill(20)
        ).apply {
            this.verticalAlignment(VerticalAlignment.CENTER)
            this.child(Components.texture(
                Reden.identifier("reden_16.png"),
                0, 0, 16, 16, 16, 16
            ))
            this.child(Components.label(Text.literal("Reden Credits"))
                .shadow(true)
                .margins(Insets.of(3)))
        })
        rootComponent.child(Containers.verticalScroll(Sizing.fill(70), Sizing.fill(80), creditsScreenContent()).apply {
            scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
        })
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(context)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        client!!.soundManager.stop(MikuSinging)
        client!!.setScreen(parent)
    }
}

private fun center(component: Component): Component {
    return Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).configure<FlowLayout> {
        it.child(component)
        it.horizontalAlignment(HorizontalAlignment.CENTER)
    }
}

private fun labelComponent(text: Text): LabelComponent {
    return Components.label(text).configure {
        it.horizontalSizing(Sizing.fill(100))
    }
}

fun Screen.creditsScreenContent(): FlowLayout {
    val content = Containers.verticalFlow(Sizing.fill(100), Sizing.content())
    content.child(labelComponent(
        Text.literal("Reden is an open source project under LGPL-3.0 license.").styled {
            it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to view Source and License.")))
        }
    ).configure {
        it.mouseDown().subscribe { _, _, _ ->
            Util.getOperatingSystem().open(URI("https://github.com/zly2006/reden-is-what-we-made"))
            true
        }
    })

    content.child(center(Components.label(Text.literal("Contributors"))
        .margins(Insets.vertical(15))))

    content.child(labelComponent(Text.literal("zly2006").append(
        Text.literal(" - Project Owner, Developer").formatted(Formatting.GRAY)
    )).margins(Insets.vertical(3)))
    content.child(labelComponent(Text.literal("Cubik65536").append(
        Text.literal(" - Developer, Moderator").formatted(Formatting.GRAY)
    )).margins(Insets.vertical(3)))
    content.child(labelComponent(Text.literal("Wafarm").append(
        Text.literal(" - Developer").formatted(Formatting.GRAY)
    )).margins(Insets.vertical(3)))
    content.child(labelComponent(Text.literal("Kikugie").append(
        Text.literal(" - Designed our icon, and provided many great ideas.").formatted(Formatting.GRAY)
    )).margins(Insets.vertical(3)))
    content.child(labelComponent(Text.literal("View all contributors on Github").formatted(Formatting.GRAY).formatted(Formatting.UNDERLINE)).configure {
        it.mouseDown().subscribe { _, _, _ ->
            Util.getOperatingSystem().open(URI("https://github.com/zly2006/reden-is-what-we-made/graphs/contributors"))
            true
        }
    }).margins(Insets.vertical(3))

    content.child(center(Components.label(Text.literal("Sponsors"))
        .margins(Insets.vertical(15))))

    content.child(center(Components.button(Text.literal("Click to see sponsors")) {
        client!!.setScreen(SponsorScreen(this))
    }))

    content.child(center(Components.label(Text.literal("Special Thanks"))
        .margins(Insets.vertical(15))))

    content.child(labelComponent(Text.literal("Hatsune Miku & Producers").styled {
        it.withColor(0x39C5BB)
    }.append(
        Text.literal(" - For signing songs that accompany me through the development of Reden.").formatted(Formatting.GRAY)
    )).apply {
        margins(Insets.vertical(3))
        mouseEnter().subscribe {
            client!!.soundManager.play(CreditScreen.MikuSinging)
        }
        mouseLeave().subscribe {
            client!!.soundManager.stop(CreditScreen.MikuSinging)
        }
    })

    content.child(center(Components.label(Text.literal("Open Source Projects used by Reden"))
        .margins(Insets.vertical(15))))

    class OpenSourceProject(val name: String, val url: String, val license: String)
    val openSourceProjects = listOf(
        OpenSourceProject("malilib", "https://github.com/maruohon/malilib", "LGPL-3.0"),
        OpenSourceProject("carpet", "https://github.com/gnembon/fabric-carpet", "MIT"),
        OpenSourceProject("fabric-api", "https://github.com/FabricMC/fabric", "Apache-2.0"),
        OpenSourceProject("kotlin-stdlib", "https://github.com/JetBrains/kotlin/tree/master/license", "Other"),
        OpenSourceProject("okio", "https://github.com/square/okio", "Apache-2.0"),
        OpenSourceProject("okhttp", "https://github.com/square/okhttp", "Apache-2.0"),
        OpenSourceProject("jgit", "https://www.eclipse.org/org/documents/edl-v10.php", "Eclipse Distribution License"),
        OpenSourceProject("kotlinx-serialization-json", "https://github.com/Kotlin/kotlinx.serialization", "Apache-2.0"),
        OpenSourceProject("owo", "https://github.com/wisp-forest/owo-lib", "MIT"),
        OpenSourceProject("yarn", "https://github.com/FabricMC/yarn", "CC0-1.0"),
        OpenSourceProject("slf4j", "https://www.slf4j.org/license.html", "MIT"),
        OpenSourceProject("asm", "https://asm.ow2.io/license.html", "BSD-3-Clause"),
    )
    content.child(Containers.grid(Sizing.fill(100), Sizing.content(), 20, 2).apply {
        this.child(labelComponent(Text.literal("Project")), 0, 0)
        this.child(labelComponent(Text.literal("License")), 0, 1)
        openSourceProjects.forEachIndexed { index, project ->
            this.child(labelComponent(Text.literal(project.name))
                .margins(Insets.vertical(3)), index + 1, 0)
            this.child(labelComponent(Text.literal(project.license).styled {
                it.withUnderline(true)
            }).margins(Insets.vertical(3)).configure {
                it.mouseDown().subscribe { _, _, _ ->
                    Util.getOperatingSystem().open(URI(project.url))
                    true
                }
            }, index + 1, 1)
        }
    })

    content.child(center(Components.label(Text.literal("Privacy Policy"))
        .margins(Insets.vertical(15))))

    content.child((Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
        this.child(Components.button(Text.literal("Privacy Settings")) {
            client!!.setScreen(PrivacyScreen(this@creditsScreenContent))
        })
        this.child(Components.button(Text.literal("Privacy Policy")) {
            Util.getOperatingSystem().open(URI("https://www.redenmc.com/policy/PRIVACY.md"))
        })
        this.child(Components.button(Text.literal("Delete My Data").red()) {
            Util.getOperatingSystem().open(URI("https://www.redenmc.com/privacy/deletekey=$key"))
        })
        children().forEach { it.margins(Insets.of(5)) }
    }))
    return content
}
