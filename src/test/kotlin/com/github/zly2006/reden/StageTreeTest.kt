package com.github.zly2006.reden

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.StageTree
import org.junit.jupiter.api.Test

class StageTreeTest {
    @Test
    fun mutableChildrenTickTest01() {
        val x = object : TickStage("test", null) {
            override fun tick() {
                children.add(object : TickStage("1", this) {
                    override fun tick() {
                        children.add(object : TickStage("1-1", this) {
                            override fun tick() {
                                children.add(object : TickStage("1-1-1", this) {
                                    override fun tick() {
                                    }
                                })
                            }
                        })
                    }
                })
                children.add(object : TickStage("2", this) {
                    override fun tick() {
                        children.add(object : TickStage("2-1", this) {
                            override fun tick() {
                                children.add(object : TickStage("2-1-1", this) {
                                    override fun tick() {
                                    }
                                })
                            }
                        })
                        children.add(object : TickStage("2-2", this) {
                            override fun tick() {
                                children.add(object : TickStage("2-2-1", this) {
                                    override fun tick() {
                                    }
                                })
                            }
                        })
                    }
                })
            }
        }
        val tree = StageTree()
        tree.root = StageTree.TreeNode(null, x, false, null)
        tree.child = tree.root

        val list = mutableListOf<String>()
        while (tree.hasNext()) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        assert(
            list == listOf(
                "test",
                "1",
                "1-1",
                "1-1-1",
                "2",
                "2-1",
                "2-1-1",
                "2-2",
                "2-2-1"
            )
        )
    }
}
