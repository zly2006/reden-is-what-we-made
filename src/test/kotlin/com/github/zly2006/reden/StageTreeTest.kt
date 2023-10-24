package com.github.zly2006.reden

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.StageTree
import org.junit.jupiter.api.Test

class StageTreeTest {
    fun getMutableChildrenTree(): StageTree {
        val tree = StageTree()
        // init the tree
        tree.initRoot(object : TickStage("root", null) {
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
        }, false)
        return tree
    }
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

    @Test
    fun insertTest01() {
        val tree = getMutableChildrenTree()
        val list = mutableListOf<String>()
        repeat(3) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        tree.insert2child(object : TickStage("insert-1", null) {
            override fun tick() {
            }
        })
        while (tree.hasNext()) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        assert(list == listOf(
            "root",
            "1",
            "1-1",
            "insert-1",
            "1-1-1",
            "2",
            "2-1",
            "2-1-1",
            "2-2",
            "2-2-1",
        ))
    }
    @Test
    fun insertTest02() {
        val tree = getMutableChildrenTree()
        val list = mutableListOf<String>()
        repeat(4) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        tree.insert2child(tree.peekLeaf(), object : TickStage("insert-1", null) {
            override fun tick() {
            }
        })
        while (tree.hasNext()) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        assert(list == listOf(
            "root",
            "1",
            "1-1",
            "1-1-1",
            "insert-1",
            "2",
            "2-1",
            "2-1-1",
            "2-2",
            "2-2-1",
        ))
    }
}
