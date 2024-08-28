package com.github.zly2006.reden.minenv

import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.ua
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import okhttp3.Request

class MinenvScreen : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        httpClient.newCall(Request.Builder().apply {
            ua()
            get()
            url("https://minemev.com/api/search")
        }.build())
    }
}
