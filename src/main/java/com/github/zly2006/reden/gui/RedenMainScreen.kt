package com.github.zly2006.reden.gui

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter

/**
 * ---
 * | Button: Config | Button: Sponsor | Button: Credits |
 * |-------|
 * // Section: RVC                ===> More
 * // The first three repositories
 * |--------|
 * // Section: Debugger
 * |--------|
 * // Section: Learn (wiki)
 */
class RedenMainScreen() : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!
    override fun build(rootComponent: FlowLayout) {
    }
}
